package com.rocs.rc.ohs.app.model.person;

import com.rocs.rc.ohs.app.model.login.Login;

/**
 * Represents a person in the system.
 */
public class Person {

    /** The unique ID of the person. */
    private Long id;

    /** The first name of the person. */
    private String firstName;

    /** The last name of the person. */
    private String lastName;

    /** The login account associated with the person. */
    private Login login;

}
