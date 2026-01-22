package main;

import java.util.Scanner;
import auth.*;
import quiz.*; // Ensure this is imported
import util.DataStore;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        DataStore ds = new DataStore();
        ds.createTables(); // This ensures 'Questions' table exists before anyone tries to load it
        while (true) {
            System.out.println("\n================================");
            System.out.println("   TEST MANAGEMENT SYSTEM");
            System.out.println("================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            // Input validation
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                sc.next(); 
                continue;
            }

            int choice = sc.nextInt();
            sc.nextLine(); // Clear the newline buffer

            if (choice == 1) {
                // 1. Handle Login
                Login login = new Login(sc);
                User user = (User) login.loginInfo(); 

                if (user != null) {
                    System.out.println("\nLogin Successful!");
                    // 2. Direct to the Quiz Module
                    // The QuizController handles the role-based menu (Student vs Educator)
                    QuizController.runQuizModule(sc, user);
                } else {
                    System.out.println("Login failed. Please try again.");
                }

            } else if (choice == 2) {
                // Handle Registration
                new Register(sc).registerInfo();
                
            } else if (choice == 3) {
                System.out.println("Exiting System. Goodbye!");
                break;
            } else {
                System.out.println("Invalid choice. Please select 1, 2, or 3.");
            }
        }

        sc.close();
    }
}