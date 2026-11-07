package com.rocs.rc.ohs;

import com.rocs.rc.ohs.app.facade.login.LoginFacade;
import com.rocs.rc.ohs.app.facade.login.impl.LoginFacadeImpl;
import com.rocs.rc.ohs.app.model.login.Login;

import java.util.List;
import java.io.*;
import java.util.Scanner;

public final class ClinicApp {

    private ClinicApp()
    {
    }

    /**
     * Runs the clinic application and logs in the user.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to Clinic IS");
        System.out.print("USERNAME: ");
        String username=sc.nextLine();
        System.out.print("PASSWORD: ");
        String Pass_Word = sc.nextLine();

        if (username.isEmpty())
            return;

        if (Pass_Word.isEmpty()) {
            System.out.println("Please enter password, the password field must not be left empty or blank in this form");
            return;
        }

        if (Pass_Word.length() < 8) {
            System.out.println("Password too short");
            return;
        }

        LoginFacade loginFacade = new LoginFacadeImpl();
        Login login = loginFacade.login(username,Pass_Word);
        if (login == null) {
        }

        switch (username) {
            case "admin":
                System.out.println("Welcome admin");
                break;
        }

        if (!Pass_Word.equals(login.getPassword())) {
            System.out.println("Invalid password");	return;
        }

        System.out.println("Login successful");

    }
}