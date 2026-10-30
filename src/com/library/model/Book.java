package com.library.model;

/** Plain model class that mirrors one row of the 'books' table. */
public class Book {
    private int bookId;
    private String title;
    private String author;
    private String category;
    private int quantity;           // total copies owned by the library
    private int availableQuantity;  // copies currently on the shelf

    /** Constructor 1: used when ADDING a new book - all copies are available. */
    public Book(int bookId, String title, String author, String category, int quantity) {
        this(bookId, title, author, category, quantity, quantity);
    }

    /** Constructor 2 (overloaded): used when reading a row from the database. */
    public Book(int bookId, String title, String author, String category,
                int quantity, int availableQuantity) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
    }

    public int getBookId()                 { return bookId; }
    public void setBookId(int bookId)      { this.bookId = bookId; }
    public String getTitle()               { return title; }
    public void setTitle(String t)         { this.title = t; }
    public String getAuthor()              { return author; }
    public void setAuthor(String a)        { this.author = a; }
    public String getCategory()            { return category; }
    public void setCategory(String c)      { this.category = c; }
    public int getQuantity()               { return quantity; }
    public void setQuantity(int q)         { this.quantity = q; }
    public int getAvailableQuantity()      { return availableQuantity; }
    public void setAvailableQuantity(int a){ this.availableQuantity = a; }

    /** Number of copies currently issued to members. */
    public int getIssuedCount() {
        return quantity - availableQuantity;
    }

    public static void printHeader() {
        System.out.printf("%-6s %-32s %-24s %-18s %-6s %-9s%n",
                "ID", "Title", "Author", "Category", "Total", "Available");
        System.out.println("-".repeat(100));
    }

    /** Prints this book as one table row. */
    public void display() {
        System.out.printf("%-6d %-32s %-24s %-18s %-6d %-9d%n",
                bookId, cut(title, 31), cut(author, 23), cut(category, 17),
                quantity, availableQuantity);
    }

    private String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "~";
    }

    @Override
    public String toString() {
        return "Book[" + bookId + ", " + title + "]";
    }
}
