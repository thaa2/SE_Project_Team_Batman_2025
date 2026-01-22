package auth;
import java.util.Scanner;
import educator.*;
import student.*;
import util.*;
public class Register {
    private Scanner scanner;

    public Register(Scanner scanner) {
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Register rs = new Register(scanner);
        rs.registerInfo();
    }
    public void registerInfo() {
        System.out.println("=== Test Management System Registration ===");
        // 1. NAME
        String name;
        while (true) {
            System.out.print("Enter Name: ");
            name = scanner.nextLine();
            if (name.matches("[a-zA-Z ]+")) break;
            System.out.println("Name must contain only letters and spaces.");
        }

        // 2. AGE
        int age;
        while (true) {
            System.out.print("Enter Age: ");
            if (scanner.hasNextInt()) {
                age = scanner.nextInt();
                if (age > 0 && age <= 120) {
                    scanner.nextLine(); // clear buffer
                    break;
                }
            }
            System.out.println("Please enter a valid age (1-120).");
            scanner.nextLine(); // clear buffer
        }

        // 3. GENDER
        String gender;
        while (true) {
            System.out.print("Enter Gender (Male/Female): ");
            gender = scanner.nextLine();
            if (gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female")) break;
            System.out.println("Gender must be Male or Female.");
        }

        // 4. BIRTH DATE
        String birthDate;
        while (true) {
            System.out.print("Enter Birth Date (DD/MM/YYYY): ");
            birthDate = scanner.nextLine();
            if (birthDate.matches("\\d{2}/\\d{2}/\\d{4}")) break;
            System.out.println("Format must be DD/MM/YYYY.");
        }

        // 5. EMAIL
        String email;
        while (true) {
            System.out.print("Enter Email: ");
            email = scanner.nextLine();
            if (email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) break;
            System.out.println("Invalid email format.");
        }

        // 6. PASSWORD
        String password;
        while (true) {
            System.out.print("Enter Password: ");
            password = scanner.nextLine();
            if (password.length() >= 8 && password.matches(".*\\d.*")) break;
            System.out.println("Password must be >= 8 characters and include a number.");
        }
        // ... (previous validation code remains the same)

        // 7. ROLE & OBJECT CREATION
        String selectedRole = "";
        while (true) {
            System.out.println("\nSelect Role:");
            System.out.println("1. Student");
            System.out.println("2. Educator");
            System.out.print("Enter choice: ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); 
                
                User user; 
                if (choice == 1) {
                    // Pass 0 as the ID because the DB hasn't generated one yet
                    user = new Student(0, name, age, gender, birthDate, email, password);
                    selectedRole = "STUDENT";
                    user.displayInfo();
                    break;
                } else if (choice == 2) {
                    // Pass 0 as the ID
                    user = new Educator(0, name, age, gender, birthDate, email, password);
                    selectedRole = "EDUCATOR";
                    user.displayInfo();
                    break;
                }
            }
            System.out.println("Invalid choice. Enter 1 or 2.");
            // Remove the extra input.nextLine() here to avoid double-skipping
        }
        // 8. SAVE TO DATABASE
        DataStore dataStore = new DataStore();
        dataStore.InsertUser(name, age, gender, birthDate, email, password, selectedRole);
        System.out.println("Registration Successful!");
    }
}