package com.library.dao;

import com.library.exception.LibraryException;
import com.library.model.LibraryTransaction;
import com.library.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * DAO for the 'transactions' table.
 * Issuing and returning a book each change TWO tables, so both use a JDBC TRANSACTION:
 *   setAutoCommit(false) -> run all SQL -> commit()   (or rollback() if anything fails)
 */
public class TransactionDAO {

    public static final int LOAN_DAYS = 14;            // books are issued for 14 days
    public static final int MAX_BOOKS_PER_MEMBER = 3;  // a member can hold at most 3 books

    // ======================= ISSUE A BOOK =========================
    public LibraryTransaction issueBook(int bookId, int memberId) throws SQLException, LibraryException {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);                    // START transaction

            // Step 1: member must exist
            boolean memberFound = false;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT member_id FROM members WHERE member_id = ?")) {
                ps.setInt(1, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    memberFound = rs.next();
                }
            }
            if (!memberFound) {
                throw new LibraryException("Member ID " + memberId + " is not registered.");
            }

            // Step 2: book must exist and have a free copy.
            // FOR UPDATE locks this row so two users cannot take the last copy at the same time.
            int available;
            String title;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT title, available_quantity FROM books WHERE book_id = ? FOR UPDATE")) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new LibraryException("Book ID " + bookId + " does not exist.");
                    }
                    title = rs.getString("title");
                    available = rs.getInt("available_quantity");
                }
            }
            if (available <= 0) {
                throw new LibraryException("'" + title + "' is not available right now (all copies are issued).");
            }

            // Step 3: business rules about this member
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM transactions WHERE member_id = ? AND status = 'ISSUED'")) {
                ps.setInt(1, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) >= MAX_BOOKS_PER_MEMBER) {
                        throw new LibraryException("Member " + memberId + " already has "
                                + MAX_BOOKS_PER_MEMBER + " books issued. Return one first.");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND member_id = ? AND status = 'ISSUED'")) {
                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        throw new LibraryException("This member already has a copy of '" + title + "'.");
                    }
                }
            }

            // Step 4: save the issue record
            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate = issueDate.plusDays(LOAN_DAYS);
            LibraryTransaction txn = new LibraryTransaction(bookId, memberId, issueDate, dueDate);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO transactions (book_id, member_id, issue_date, due_date, status) "
                  + "VALUES (?, ?, ?, ?, 'ISSUED')", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                ps.setDate(3, Date.valueOf(issueDate));
                ps.setDate(4, Date.valueOf(dueDate));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        txn.setTransactionId(keys.getInt(1));   // auto-generated transaction_id
                    }
                }
            }

            // Step 5: reduce available copies by 1
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE books SET available_quantity = available_quantity - 1 WHERE book_id = ?")) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            con.commit();                                // SAVE everything together
            return txn;

        } catch (SQLException | LibraryException e) {
            DBConnection.rollbackQuietly(con);           // UNDO everything
            throw e;
        } finally {
            DBConnection.closeQuietly(con);
        }
    }

    // ======================= RETURN A BOOK ========================
    public LibraryTransaction returnBook(int transactionId) throws SQLException, LibraryException {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);                    // START transaction

            // Step 1: find the transaction (lock the row)
            LibraryTransaction txn = null;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM transactions WHERE transaction_id = ? FOR UPDATE")) {
                ps.setInt(1, transactionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        txn = mapRow(rs);
                    }
                }
            }
            if (txn == null) {
                throw new LibraryException("Transaction ID " + transactionId + " not found.");
            }
            if (LibraryTransaction.RETURNED.equals(txn.getStatus())) {
                throw new LibraryException("This book was already returned on " + txn.getReturnDate() + ".");
            }

            // Step 2: mark the transaction as returned
            LocalDate today = LocalDate.now();
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE transactions SET return_date = ?, status = 'RETURNED' WHERE transaction_id = ?")) {
                ps.setDate(1, Date.valueOf(today));
                ps.setInt(2, transactionId);
                ps.executeUpdate();
            }

            // Step 3: give the copy back to the shelf (+1 available)
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE books SET available_quantity = available_quantity + 1 WHERE book_id = ?")) {
                ps.setInt(1, txn.getBookId());
                ps.executeUpdate();
            }

            con.commit();
            txn.setReturnDate(today);
            txn.setStatus(LibraryTransaction.RETURNED);
            return txn;

        } catch (SQLException | LibraryException e) {
            DBConnection.rollbackQuietly(con);
            throw e;
        } finally {
            DBConnection.closeQuietly(con);
        }
    }

    // ========================= QUERIES ============================
    /** Full history, newest first. */
    public ArrayList<LibraryTransaction> getAllTransactions() throws SQLException {
        return query("SELECT * FROM transactions ORDER BY transaction_id DESC");
    }

    /** Only the books that are currently issued (not yet returned). */
    public ArrayList<LibraryTransaction> getActiveTransactions() throws SQLException {
        return query("SELECT * FROM transactions WHERE status = 'ISSUED' ORDER BY due_date");
    }

    private ArrayList<LibraryTransaction> query(String sql) throws SQLException {
        ArrayList<LibraryTransaction> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private LibraryTransaction mapRow(ResultSet rs) throws SQLException {
        Date ret = rs.getDate("return_date");            // may be NULL in the database
        return new LibraryTransaction(
                rs.getInt("transaction_id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                rs.getDate("issue_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                ret == null ? null : ret.toLocalDate(),
                rs.getString("status"));
    }
}
