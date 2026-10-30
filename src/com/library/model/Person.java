package com.library.model;

/**
 * PARENT class (superclass).
 * Holds the data that every person in the library system has.
 * Member extends this class (inheritance).
 */
public class Person {
    private int id;
    private String name;
    private String email;
    private String contact;

    public Person(int id, String name, String email, String contact) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.contact = contact;
    }

    // ---------- getters and setters (encapsulation) ----------
    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }
    public String getName()          { return name; }
    public void setName(String n)    { this.name = n; }
    public String getEmail()         { return email; }
    public void setEmail(String e)   { this.email = e; }
    public String getContact()       { return contact; }
    public void setContact(String c) { this.contact = c; }

    /** Subclasses override this to describe themselves (polymorphism). */
    public String getRole() {
        return "Person";
    }

    /** Prints one table row. It calls getRole(), so the output depends on the REAL object type. */
    public void displayInfo() {
        System.out.printf("%-6d %-22s %-30s %-12s %-8s%n",
                id, name, email, contact, getRole());
    }

    @Override
    public String toString() {
        return getRole() + "[id=" + id + ", name=" + name + "]";
    }
}
