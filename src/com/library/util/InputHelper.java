package com.library.util;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * All keyboard input + validation lives here.
 * Each method keeps asking until the user types a valid value,
 * so the rest of the program never receives bad data.
 */
public class InputHelper {
    private static final Scanner sc = new Scanner(System.in);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CONTACT_PATTERN =
            Pattern.compile("^[6-9][0-9]{9}$");      // 10-digit Indian mobile number

    /** Reads an integer >= min. */
    public static int readInt(String prompt, int min) {
        while (true) {
            System.out.print(prompt + ": ");
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min) {
                    System.out.println("  Invalid input: value must be at least " + min + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input: please enter a whole number.");
            }
        }
    }

    /** Reads a menu choice (any integer, no minimum). */
    public static int readChoice() {
        return readInt("Enter your choice", 0);
    }

    /** Reads a non-empty text of at most maxLen characters. */
    public static String readString(String prompt, int maxLen) {
        while (true) {
            System.out.print(prompt + ": ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("  Invalid input: this field cannot be empty.");
            } else if (line.length() > maxLen) {
                System.out.println("  Invalid input: maximum " + maxLen + " characters allowed.");
            } else {
                return line;
            }
        }
    }

    public static String readEmail(String prompt) {
        while (true) {
            String email = readString(prompt, 100);
            if (EMAIL_PATTERN.matcher(email).matches()) {
                return email.toLowerCase();
            }
            System.out.println("  Invalid email format. Example: name@example.com");
        }
    }

    public static String readContact(String prompt) {
        while (true) {
            String contact = readString(prompt, 15);
            if (CONTACT_PATTERN.matcher(contact).matches()) {
                return contact;
            }
            System.out.println("  Invalid contact: enter a 10-digit mobile number starting with 6-9.");
        }
    }

    // ---------- "press Enter to keep the old value" versions, used for UPDATE ----------

    public static String readStringOrKeep(String prompt, String current, int maxLen) {
        while (true) {
            System.out.print(prompt + " [" + current + "] (Enter = keep): ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return current;
            if (line.length() <= maxLen) return line;
            System.out.println("  Invalid input: maximum " + maxLen + " characters allowed.");
        }
    }

    public static String readEmailOrKeep(String prompt, String current) {
        while (true) {
            System.out.print(prompt + " [" + current + "] (Enter = keep): ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return current;
            if (EMAIL_PATTERN.matcher(line).matches()) return line.toLowerCase();
            System.out.println("  Invalid email format. Example: name@example.com");
        }
    }

    public static String readContactOrKeep(String prompt, String current) {
        while (true) {
            System.out.print(prompt + " [" + current + "] (Enter = keep): ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return current;
            if (CONTACT_PATTERN.matcher(line).matches()) return line;
            System.out.println("  Invalid contact: enter a 10-digit mobile number starting with 6-9.");
        }
    }

    public static int readIntOrKeep(String prompt, int current, int min) {
        while (true) {
            System.out.print(prompt + " [" + current + "] (Enter = keep): ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return current;
            try {
                int value = Integer.parseInt(line);
                if (value >= min) return value;
                System.out.println("  Invalid input: value must be at least " + min + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input: please enter a whole number.");
            }
        }
    }

    /** Asks a yes/no question. */
    public static boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String line = sc.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            System.out.println("  Please type y or n.");
        }
    }
}
