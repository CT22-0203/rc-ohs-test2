package com.rocs.rc.ohs;

import com.rocs.rc.ohs.app.facade.login.LoginFacade;
import com.rocs.rc.ohs.app.facade.login.impl.LoginFacadeImpl;
import com.rocs.rc.ohs.app.model.login.Login;

import java.util.Scanner;

public class ClinicApp {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to Clinic IS");
        System.out.print("USERNAME: ");
        String username = sc.nextLine();
        System.out.print("PASSWORD: " );
        String password = sc.nextLine();

        if (username.isEmpty()) {
            System.out.println("Please enter username ");
            return;
        }

        if (password.isEmpty()) {
            System.out.println("Please enter password");
            return;
        }

        LoginFacade loginFacade = new LoginFacadeImpl();
        Login login = loginFacade.login(username, password);
        if (login == null) {
            System.out.println("Invalid username");
            return;
        }

        if (!password.equals(login.getPassword())) {
            System.out.println("Invalid password");
            return;
        }

        System.out.println("Login successful");

    }
}
