package auth;

import java.util.Scanner;

import util.DataStore;

public class Login {
  

    public void loginInfo() {
        Scanner input = new Scanner(System.in);
        DataStore dataStore = new DataStore();

        System.out.println("=== Test Management System Login ===");

        // -------- EMAIL --------
        String email;
        while (true) {
            System.out.print("Enter Email: ");
            email = input.nextLine();

            if (email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                break;
            } else {
                System.out.println("Invalid email format.");
            }
        }

        // -------- PASSWORD --------
        System.out.print("Enter Password: ");
        String password = input.nextLine();

        // -------- LOGIN CHECK --------
        String role = dataStore.login(email, password);

        if (role != null) {
            System.out.println("✅ Login successful!");
            System.out.println("Role: " + role);

            // Redirect based on role
            switch (role) {
                case "STUDENT":
                    System.out.println("Welcome Student Dashboard");
                    break;

                case "EDUCATOR":
                    System.out.println("Welcome Educator Dashboard");
                    break;

                default:
                    System.out.println("Unknown role.");
            }
        } else {
            System.out.println(" Invalid email or password.");
        }

        input.close();
    }
}
 
