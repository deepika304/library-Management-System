package com.library.dao;

import com.library.model.Book;
import com.library.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * DAO = Data Access Object.
 * The ONLY class that talks to the 'books' table (all SQL for books is here).
 * Every query uses PreparedStatement (safe from SQL injection).
 */
public class BookDAO {

    private static final String SELECT_ALL =
            "SELECT book_id, title, author, category, quantity, available_quantity FROM books";

    // ------------------------- CREATE -------------------------
    public void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (book_id, title, author, category, quantity, available_quantity) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, book.getBookId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory());
            ps.setInt(5, book.getQuantity());
            ps.setInt(6, book.getAvailableQuantity());
            ps.executeUpdate();
        }
    }

    // -------------------------- READ --------------------------
    /** Returns all books in an ArrayList (ordered list, easy to loop over and print). */
    public ArrayList<Book> getAllBooks() throws SQLException {
        ArrayList<Book> books = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL + " ORDER BY book_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        }
        return books;
    }

    /** Returns all books in a HashMap: bookId -> Book (fast lookup by ID). */
    public HashMap<Integer, Book> getBookMap() throws SQLException {
        HashMap<Integer, Book> map = new HashMap<>();
        for (Book b : getAllBooks()) {
            map.put(b.getBookId(), b);
        }
        return map;
    }

    /** Returns one book or null if the ID does not exist. */
    public Book getBookById(int bookId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL + " WHERE book_id = ?")) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // ------------------------ SEARCH --------------------------
    // METHOD OVERLOADING: same name 'searchBooks', different parameter types.

    /** Search by book ID (exact match). */
    public ArrayList<Book> searchBooks(int bookId) throws SQLException {
        ArrayList<Book> result = new ArrayList<>();
        Book b = getBookById(bookId);
        if (b != null) {
            result.add(b);
        }
        return result;
    }

    /** Search by part of the title OR author (case-insensitive). */
    public ArrayList<Book> searchBooks(String keyword) throws SQLException {
        ArrayList<Book> result = new ArrayList<>();
        String sql = SELECT_ALL + " WHERE title LIKE ? OR author LIKE ? ORDER BY book_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    // ------------------------ UPDATE --------------------------
    /** Updates title, author, category, quantity and available_quantity. Returns true if a row changed. */
    public boolean updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, category = ?, "
                   + "quantity = ?, available_quantity = ? WHERE book_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setInt(4, book.getQuantity());
            ps.setInt(5, book.getAvailableQuantity());
            ps.setInt(6, book.getBookId());
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------ DELETE --------------------------
    /** Deletes a book. The foreign key blocks deletion if the book has transaction history. */
    public boolean deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    // Converts the current ResultSet row into a Book object
    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getInt("available_quantity"));
    }
}
