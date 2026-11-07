package com.rocs.rc.ohs;

import com.rocs.rc.ohs.app.facade.login.LoginFacade;
import com.rocs.rc.ohs.app.facade.login.impl.LoginFacadeImpl;
import com.rocs.rc.ohs.app.model.login.Login;

import java.util.List;
import java.io.*;
import java.util.Scanner;

/**
 * The main entry point of the clinic application.
 */
public final class ClinicApp {

    /**
     * The minimum length of a valid password.
     */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Prevents instantiation of this class.
     */
    private ClinicApp() {
    }

    /**
     * Runs the clinic application and logs in the user.
     *
     * @param args The command line arguments.
     */
    public static void main(final String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to Clinic IS");
        System.out.print("USERNAME: ");
        String username = sc.nextLine();
        System.out.print("PASSWORD: ");
        String password = sc.nextLine();

        if (username.isEmpty())
            return;

        if (password.isEmpty()) {
            System.out.println("Please enter password, the password field "
                    + "must not be left empty or blank in this form");
            return;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            System.out.println("Password too short");
            return;
        }

        LoginFacade loginFacade = new LoginFacadeImpl();
        Login login = loginFacade.login(username, password);
        if (login == null) {
            System.out.println("Invalid username");
            return;
        }

        switch (username) {
            case "admin":
                System.out.println("Welcome admin");
                break;
            default:
                break;
        }

        if (!password.equals(login.getPassword())) {
            System.out.println("Invalid password");
            return;
        }

        System.out.println("Login successful");

    }
}