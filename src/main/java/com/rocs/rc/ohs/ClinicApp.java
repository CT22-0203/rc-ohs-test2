package com.rocs.rc.ohs;

import com.rocs.rc.ohs.app.facade.login.LoginFacade;
import com.rocs.rc.ohs.app.facade.login.impl.LoginFacadeImpl;
import com.rocs.rc.ohs.app.model.login.Login;

import java.util.List; // ERROR: UnusedImports
import java.io.*; // ERROR: AvoidStarImport
import java.util.Scanner;

public final class ClinicApp { // ERROR: JavadocType (tinanggal ang Javadoc ng class)

    private ClinicApp() // ERROR: MissingJavadocMethod
    { // ERROR: LeftCurly ('{' should be on the previous line)
    }

    /**
     * Runs the clinic application and logs in the user.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) { // ERROR: FinalParameters

        Scanner sc = new Scanner(System.in); // ERROR: Indentation (2 spaces, dapat 8)

        System.out.println("Welcome to Clinic IS");
        System.out.print("USERNAME: ");
        String username=sc.nextLine(); // ERROR: WhitespaceAround (walang space sa paligid ng '=')
        System.out.print("PASSWORD: ");
        String Pass_Word = sc.nextLine(); // ERROR: LocalVariableName (dapat camelCase)

        if (username.isEmpty())
            return; // ERROR: NeedBraces (walang {} ang if)

        if (Pass_Word.isEmpty()) {
            System.out.println("Please enter password, the password field must not be left empty or blank in this form"); // ERROR: LineLength (lampas 80/100)
            return;
        }

        if (Pass_Word.length() < 8) { // ERROR: MagicNumber (8)
            System.out.println("Password too short");
            return;
        }

        LoginFacade loginFacade = new LoginFacadeImpl();
        Login login = loginFacade.login(username,Pass_Word); // ERROR: WhitespaceAfter (walang space pagkatapos ng ',')
        if (login == null) {
        } // ERROR: EmptyBlock

        switch (username) { // ERROR: MissingSwitchDefault
            case "admin":
                System.out.println("Welcome admin");
                break;
        }

        if (!Pass_Word.equals(login.getPassword())) {
            System.out.println("Invalid password");	return; // ERROR: FileTabCharacter (may tab sa pagitan)
        }

        System.out.println("Login successful");   // ERROR: RegexpSingleline (trailing spaces)

    }
}