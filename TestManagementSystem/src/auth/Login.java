package auth;

import java.util.Scanner;
import util.DataStore;

public class Login {
    private Scanner scanner;

    public Login(Scanner scanner) {
        this.scanner = scanner;
    }

    public User loginInfo() {
        Scanner input = scanner;
        DataStore dataStore = new DataStore();

        String email = "";
        String password = "";

        System.out.println("=== Test Management System Login ===");

        // -------- EMAIL INPUT --------
        while (true) {
            System.out.print("Enter Email: ");
            email = input.nextLine();
            if (email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                break;
            } else {
                System.out.println("Invalid email format.");
            }
        }

        // -------- PASSWORD INPUT --------
        System.out.print("Enter Password: ");
        password = input.nextLine();

        // -------- DATABASE FETCH --------
        // Use the new method we discussed that returns a full User object
        User authenticatedUser = dataStore.getAuthenticatedUser(email, password);

        if (authenticatedUser != null) {
            System.out.println("Login successful.");
            System.out.println("Welcome back, " + authenticatedUser.getName() + "!");
            return authenticatedUser;
        } else {
            System.out.println("Invalid email or password.");
            return null; 
        }
        
    }
}