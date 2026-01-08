package main;

import java.util.Scanner;
import auth.Register;
import auth.Login;

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.println("\n*** Welcome to our System ***");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = input.nextInt();
            input.nextLine(); // clear buffer

            if (choice == 1) {
                Login login = new Login();
                login.loginInfo();
            } 
            else if (choice == 2) {
                Register register = new Register();
                register.registerInfo();
            } 
            else if (choice == 3) {
                System.out.println("Thank you for using the system!");
                break;
            } 
            else {
                System.out.println(" Please enter 1, 2, or 3.");
            }
        }

        input.close();
    }
}
