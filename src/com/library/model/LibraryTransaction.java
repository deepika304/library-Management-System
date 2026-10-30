package com.library.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Mirrors one row of the 'transactions' table (one issue + its return). */
public class LibraryTransaction {
    public static final int FINE_PER_DAY = 5;      // Rs. 5 per late day
    public static final String ISSUED = "ISSUED";
    public static final String RETURNED = "RETURNED";

    private int transactionId;
    private int bookId;
    private int memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;   // null until the book comes back
    private String status;

    /** Constructor 1: for a brand-new issue (no ID yet, not returned). */
    public LibraryTransaction(int bookId, int memberId, LocalDate issueDate, LocalDate dueDate) {
        this(0, bookId, memberId, issueDate, dueDate, null, ISSUED);
    }

    /** Constructor 2 (overloaded): full record loaded from the database. */
    public LibraryTransaction(int transactionId, int bookId, int memberId, LocalDate issueDate,
                              LocalDate dueDate, LocalDate returnDate, String status) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public int getTransactionId()            { return transactionId; }
    public void setTransactionId(int id)     { this.transactionId = id; }
    public int getBookId()                   { return bookId; }
    public int getMemberId()                 { return memberId; }
    public LocalDate getIssueDate()          { return issueDate; }
    public LocalDate getDueDate()            { return dueDate; }
    public LocalDate getReturnDate()         { return returnDate; }
    public void setReturnDate(LocalDate d)   { this.returnDate = d; }
    public String getStatus()                { return status; }
    public void setStatus(String status)     { this.status = status; }

    /** Fine = late days x Rs.5. For books not yet returned, "today" is used. */
    public int calculateFine() {
        LocalDate end = (returnDate != null) ? returnDate : LocalDate.now();
        long lateDays = ChronoUnit.DAYS.between(dueDate, end);
        return lateDays > 0 ? (int) lateDays * FINE_PER_DAY : 0;
    }
}
