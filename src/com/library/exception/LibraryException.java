package com.library.exception;

/**
 * Custom checked exception for LIBRARY RULE violations
 * (e.g. "book not available", "member not found").
 * It is different from SQLException, which means a real database problem.
 */
public class LibraryException extends Exception {
    private static final long serialVersionUID = 1L;

    public LibraryException(String message) {
        super(message);
    }
}
