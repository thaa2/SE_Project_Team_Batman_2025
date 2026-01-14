package auth;

import java.util.Scanner;
import educator.Educator;
import student.Student;
import util.DataStore;

public class Register {

    public void registerInfo() {
        Scanner input = new Scanner(System.in);

        System.out.println("=== Test Management System Registration ===");

        // 1. NAME
        String name;
        while (true) {
            System.out.print("Enter Name: ");
            name = input.nextLine();
            if (name.matches("[a-zA-Z ]+")) break;
            System.out.println("Name must contain only letters and spaces.");
        }

        // 2. AGE
        int age;
        while (true) {
            System.out.print("Enter Age: ");
            if (input.hasNextInt()) {
                age = input.nextInt();
                if (age > 0 && age <= 120) {
                    input.nextLine(); // clear buffer
                    break;
                }
            }
            System.out.println("Please enter a valid age (1-120).");
            input.nextLine(); // clear buffer
        }

        // 3. GENDER
        String gender;
        while (true) {
            System.out.print("Enter Gender (Male/Female): ");
            gender = input.nextLine();
            if (gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female")) break;
            System.out.println("Gender must be Male or Female.");
        }

        // 4. BIRTH DATE
        String birthDate;
        while (true) {
            System.out.print("Enter Birth Date (DD/MM/YYYY): ");
            birthDate = input.nextLine();
            if (birthDate.matches("\\d{2}/\\d{2}/\\d{4}")) break;
            System.out.println("Format must be DD/MM/YYYY.");
        }

        // 5. EMAIL
        String email;
        while (true) {
            System.out.print("Enter Email: ");
            email = input.nextLine();
            if (email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) break;
            System.out.println("Invalid email format.");
        }

        // 6. PASSWORD
        String password;
        while (true) {
            System.out.print("Enter Password: ");
            password = input.nextLine();
            if (password.length() >= 8 && password.matches(".*\\d.*")) break;
            System.out.println("Password must be >= 8 characters and include a number.");
        }

        // 7. ROLE & OBJECT CREATION
        String selectedRole = "";
        while (true) {
            System.out.println("\nSelect Role:");
            System.out.println("1. Student");
            System.out.println("2. Educator");
            System.out.print("Enter choice: ");

            if (input.hasNextInt()) {
                int choice = input.nextInt();
                input.nextLine(); 
                
                User user; 
                if (choice == 1) {
                    user = new Student(name, age, gender, birthDate, email, password);
                    selectedRole = "STUDENT";
                    user.displayInfo();
                    break;
                } else if (choice == 2) {
                    user = new Educator(name, age, gender, birthDate, email, password);
                    selectedRole = "EDUCATOR";
                    user.displayInfo();
                    break;
                }
            }
            System.out.println("Invalid choice. Enter 1 or 2.");
            input.nextLine();
        }

        // 8. SAVE TO DATABASE
        DataStore dataStore = new DataStore();
        dataStore.InsertUser(name, age, gender, birthDate, email, password, selectedRole);
        System.out.println("Registration Successful!");
    }
}