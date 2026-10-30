package com.library.model;

/**
 * CHILD class (subclass): a Member IS-A Person.
 * It reuses id, name, email, contact from Person and overrides getRole().
 */
public class Member extends Person {

    public Member(int memberId, String name, String email, String contact) {
        super(memberId, name, email, contact);   // call parent constructor
    }

    // Convenience getter so the rest of the code can say getMemberId()
    public int getMemberId() {
        return getId();
    }

    /** METHOD OVERRIDING: replaces Person.getRole(). */
    @Override
    public String getRole() {
        return "Member";
    }
}
