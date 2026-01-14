package auth;

import java.util.Scanner;
import util.DataStore;
import student.Student;
import educator.Educator;

public class Login {

    // 1. Changed return type from 'void' to 'User'
    public User loginInfo() {
        Scanner input = new Scanner(System.in);
        DataStore dataStore = new DataStore();

        // DECLARE VARIABLES HERE so they are accessible throughout the method
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

        // -------- LOGIN CHECK --------
        // Now 'email' and 'password' can be resolved
        String roleStr = dataStore.login(email, password);

        if (roleStr != null) {
            System.out.println("Connection to SQLite has been established.");
            System.out.println("Login successful.");

            // 2. Map the String role to the Enum
            Role userRole = Role.valueOf(roleStr.toUpperCase());
            
            // 3. Create and RETURN the actual User object
            if (userRole == Role.STUDENT) {
                // Note: Using placeholders for name/age until you fetch them from DB
                return new Student("Student Name", 20, "Male", "01/01/2000", email, password);
            } else if (userRole == Role.EDUCATOR) {
                return new Educator("Educator Name", 35, "Female", "01/01/1985", email, password);
            }
        } 
        
        // If login fails or role is unknown
        return null; 
    }
}