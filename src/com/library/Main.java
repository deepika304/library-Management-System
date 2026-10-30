package com.library;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.TransactionDAO;
import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.LibraryTransaction;
import com.library.model.Member;
import com.library.model.Person;
import com.library.util.DBConnection;
import com.library.util.InputHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/** Entry point: console menus. Calls DAOs for data, InputHelper for validated input. */
public class Main {

    private static final BookDAO bookDAO = new BookDAO();
    private static final MemberDAO memberDAO = new MemberDAO();
    private static final TransactionDAO txnDAO = new TransactionDAO();

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("      LIBRARY MANAGEMENT SYSTEM");
        System.out.println("==========================================");

        // Check the database connection once at start-up
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("Connected to database successfully.");
        } catch (SQLException e) {
            System.out.println("Could not connect to the database: " + e.getMessage());
            System.out.println("Check that MySQL is running and db.properties has the right URL/user/password.");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\n------------- MAIN MENU -------------");
            System.out.println("1. Book Management");
            System.out.println("2. Member Management");
            System.out.println("3. Issue a Book");
            System.out.println("4. Return a Book");
            System.out.println("5. View Currently Issued Books");
            System.out.println("6. View Transaction History");
            System.out.println("0. Exit");
            switch (InputHelper.readChoice()) {
                case 1: bookMenu(); break;
                case 2: memberMenu(); break;
                case 3: issueBook(); break;
                case 4: returnBook(); break;
                case 5: showTransactions(true); break;
                case 6: showTransactions(false); break;
                case 0: running = false; System.out.println("Thank you. Goodbye!"); break;
                default: System.out.println("Invalid choice. Please select 0-6.");
            }
        }
    }

    // ==================================================================
    //                          BOOK MENU
    // ==================================================================
    private static void bookMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n---------- BOOK MANAGEMENT ----------");
            System.out.println("1. Add Book");
            System.out.println("2. View All Books");
            System.out.println("3. Search Book by ID");
            System.out.println("4. Search Book by Title / Author");
            System.out.println("5. Update Book");
            System.out.println("6. Delete Book");
            System.out.println("0. Back");
            switch (InputHelper.readChoice()) {
                case 1: addBook(); break;
                case 2: tryRun(() -> printBooks(bookDAO.getAllBooks())); break;
                case 3: tryRun(() -> printBooks(bookDAO.searchBooks(InputHelper.readInt("Enter Book ID", 1)))); break;
                case 4: tryRun(() -> printBooks(bookDAO.searchBooks(InputHelper.readString("Enter title or author", 100)))); break;
                case 5: updateBook(); break;
                case 6: deleteBook(); break;
                case 0: back = true; break;
                default: System.out.println("Invalid choice. Please select 0-6.");
            }
        }
    }

    private static void addBook() {
        tryRun(() -> {
            int id = InputHelper.readInt("Book ID", 1);
            if (bookDAO.getBookById(id) != null) {
                System.out.println("A book with ID " + id + " already exists.");
                return;
            }
            String title = InputHelper.readString("Title", 150);
            String author = InputHelper.readString("Author", 100);
            String category = InputHelper.readString("Category", 50);
            int qty = InputHelper.readInt("Quantity", 1);
            bookDAO.addBook(new Book(id, title, author, category, qty));
            System.out.println("Book added successfully.");
        });
    }

    private static void updateBook() {
        tryRun(() -> {
            int id = InputHelper.readInt("Enter Book ID to update", 1);
            Book b = bookDAO.getBookById(id);
            if (b == null) {
                System.out.println("No book found with ID " + id + ".");
                return;
            }
            int issued = b.getIssuedCount();   // copies currently with members
            b.setTitle(InputHelper.readStringOrKeep("Title", b.getTitle(), 150));
            b.setAuthor(InputHelper.readStringOrKeep("Author", b.getAuthor(), 100));
            b.setCategory(InputHelper.readStringOrKeep("Category", b.getCategory(), 50));
            int newQty = InputHelper.readIntOrKeep("Total quantity", b.getQuantity(), 0);
            if (newQty < issued) {
                System.out.println("Update cancelled: " + issued + " copies are currently issued, "
                        + "so quantity cannot be less than " + issued + ".");
                return;
            }
            b.setQuantity(newQty);
            b.setAvailableQuantity(newQty - issued);   // keep the issued count unchanged
            bookDAO.updateBook(b);
            System.out.println("Book updated successfully.");
        });
    }

    private static void deleteBook() {
        tryRun(() -> {
            int id = InputHelper.readInt("Enter Book ID to delete", 1);
            Book b = bookDAO.getBookById(id);
            if (b == null) {
                System.out.println("No book found with ID " + id + ".");
                return;
            }
            if (InputHelper.confirm("Delete '" + b.getTitle() + "'?")) {
                bookDAO.deleteBook(id);
                System.out.println("Book deleted.");
            } else {
                System.out.println("Delete cancelled.");
            }
        });
    }

    private static void printBooks(ArrayList<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        Book.printHeader();
        for (Book b : books) {
            b.display();
        }
        System.out.println(books.size() + " book(s) listed.");
    }

    // ==================================================================
    //                         MEMBER MENU
    // ==================================================================
    private static void memberMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--------- MEMBER MANAGEMENT ---------");
            System.out.println("1. Register Member");
            System.out.println("2. View All Members");
            System.out.println("3. Search Member by ID");
            System.out.println("4. Search Member by Name / Email");
            System.out.println("5. Update Member");
            System.out.println("6. Delete Member");
            System.out.println("0. Back");
            switch (InputHelper.readChoice()) {
                case 1: addMember(); break;
                case 2: tryRun(() -> printMembers(memberDAO.getAllMembers())); break;
                case 3: tryRun(() -> printMembers(memberDAO.searchMembers(InputHelper.readInt("Enter Member ID", 1)))); break;
                case 4: tryRun(() -> printMembers(memberDAO.searchMembers(InputHelper.readString("Enter name or email", 100)))); break;
                case 5: updateMember(); break;
                case 6: deleteMember(); break;
                case 0: back = true; break;
                default: System.out.println("Invalid choice. Please select 0-6.");
            }
        }
    }

    private static void addMember() {
        tryRun(() -> {
            int id = InputHelper.readInt("Member ID", 1);
            if (memberDAO.getMemberById(id) != null) {
                System.out.println("A member with ID " + id + " already exists.");
                return;
            }
            String name = InputHelper.readString("Name", 100);
            String email = InputHelper.readEmail("Email");
            String contact = InputHelper.readContact("Contact (10 digits)");
            memberDAO.addMember(new Member(id, name, email, contact));
            System.out.println("Member registered successfully.");
        });
    }

    private static void updateMember() {
        tryRun(() -> {
            int id = InputHelper.readInt("Enter Member ID to update", 1);
            Member m = memberDAO.getMemberById(id);
            if (m == null) {
                System.out.println("No member found with ID " + id + ".");
                return;
            }
            m.setName(InputHelper.readStringOrKeep("Name", m.getName(), 100));
            m.setEmail(InputHelper.readEmailOrKeep("Email", m.getEmail()));
            m.setContact(InputHelper.readContactOrKeep("Contact", m.getContact()));
            memberDAO.updateMember(m);
            System.out.println("Member updated successfully.");
        });
    }

    private static void deleteMember() {
        tryRun(() -> {
            int id = InputHelper.readInt("Enter Member ID to delete", 1);
            Member m = memberDAO.getMemberById(id);
            if (m == null) {
                System.out.println("No member found with ID " + id + ".");
                return;
            }
            if (InputHelper.confirm("Delete member '" + m.getName() + "'?")) {
                memberDAO.deleteMember(id);
                System.out.println("Member deleted.");
            } else {
                System.out.println("Delete cancelled.");
            }
        });
    }

    private static void printMembers(ArrayList<Member> members) {
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        System.out.printf("%-6s %-22s %-30s %-12s %-8s%n", "ID", "Name", "Email", "Contact", "Type");
        System.out.println("-".repeat(82));
        // POLYMORPHISM: the loop variable is a Person, but each object is really a Member,
        // so Java runs Member's version of getRole() inside displayInfo().
        for (Person p : members) {
            p.displayInfo();
        }
        System.out.println(members.size() + " member(s) listed.");
    }

    // ==================================================================
    //                    ISSUE / RETURN / HISTORY
    // ==================================================================
    private static void issueBook() {
        tryRun(() -> {
            int bookId = InputHelper.readInt("Book ID to issue", 1);
            int memberId = InputHelper.readInt("Member ID", 1);
            LibraryTransaction t = txnDAO.issueBook(bookId, memberId);
            System.out.println("Book issued successfully!");
            System.out.println("  Transaction ID : " + t.getTransactionId());
            System.out.println("  Issue date     : " + t.getIssueDate());
            System.out.println("  Due date       : " + t.getDueDate());
        });
    }

    private static void returnBook() {
        tryRun(() -> {
            int id = InputHelper.readInt("Transaction ID to return", 1);
            LibraryTransaction t = txnDAO.returnBook(id);
            System.out.println("Book returned successfully on " + t.getReturnDate() + ".");
            int fine = t.calculateFine();
            if (fine > 0) {
                System.out.println("  Late return! Fine to collect: Rs. " + fine
                        + " (Rs. " + LibraryTransaction.FINE_PER_DAY + " per day)");
            } else {
                System.out.println("  Returned on time. No fine.");
            }
        });
    }

    private static void showTransactions(boolean onlyIssued) {
        tryRun(() -> {
            ArrayList<LibraryTransaction> list =
                    onlyIssued ? txnDAO.getActiveTransactions() : txnDAO.getAllTransactions();
            if (list.isEmpty()) {
                System.out.println("No transactions found.");
                return;
            }
            // HashMaps let us find a book/member by ID instantly while printing each row,
            // instead of running one SQL query per row.
            HashMap<Integer, Book> bookMap = bookDAO.getBookMap();
            HashMap<Integer, Member> memberMap = memberDAO.getMemberMap();

            System.out.printf("%-5s %-26s %-16s %-11s %-11s %-11s %-9s %-5s%n",
                    "TxnID", "Book", "Member", "Issued", "Due", "Returned", "Status", "Fine");
            System.out.println("-".repeat(100));
            for (LibraryTransaction t : list) {
                Book b = bookMap.get(t.getBookId());
                Member m = memberMap.get(t.getMemberId());
                String bookTitle = (b != null) ? b.getTitle() : "(deleted)";
                String memberName = (m != null) ? m.getName() : "(deleted)";
                String returned = (t.getReturnDate() == null) ? "-" : t.getReturnDate().toString();
                System.out.printf("%-5d %-26s %-16s %-11s %-11s %-11s %-9s %-5d%n",
                        t.getTransactionId(), cut(bookTitle, 25), cut(memberName, 15),
                        t.getIssueDate(), t.getDueDate(), returned, t.getStatus(), t.calculateFine());
            }
            System.out.println(list.size() + " record(s). Fine is in Rs. (Rs. "
                    + LibraryTransaction.FINE_PER_DAY + " per late day).");
        });
    }

    // ==================================================================
    //                          HELPERS
    // ==================================================================
    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "~";
    }

    /** A small interface so we can pass a block of code that may throw our two exception types. */
    private interface DbAction {
        void run() throws SQLException, LibraryException;
    }

    /** Runs an action and turns every exception into a friendly message (no stack traces for the user). */
    private static void tryRun(DbAction action) {
        try {
            action.run();
        } catch (LibraryException e) {
            System.out.println("Not allowed: " + e.getMessage());
        } catch (SQLException e) {
            printSqlError(e);
        }
    }

    private static void printSqlError(SQLException e) {
        switch (e.getErrorCode()) {
            case 1062:
                System.out.println("Error: duplicate value - that ID or email already exists.");
                break;
            case 1451:
                System.out.println("Error: cannot delete this record because it has issue/return history.");
                break;
            case 1452:
                System.out.println("Error: related book or member does not exist.");
                break;
            default:
                System.out.println("Database error: " + e.getMessage());
        }
    }
}
