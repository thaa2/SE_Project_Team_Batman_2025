package auth;

import java.sql.SQLException;
import java.util.Scanner;

import util.DataStore;

public class Register extends User {

    public Register() {
    }

    public Register(String name, int age, String gender, String birthDate,
                    String email, String password, Role role) {
        super(name, age, gender, birthDate, email, password, role);
    }

    public void registerInfo() {
        Scanner input = new Scanner(System.in);

        System.out.println("=== Test Management System Registration ===");

        // -------- NAME --------
        String name;
        while (true) {
            System.out.print("Enter Name: ");
            name = input.nextLine();

            if (name.matches("[a-zA-Z ]+")) {
                break;
            } else {
                System.out.println("Name must contain only letters and spaces.");
            }
        }

        // -------- AGE --------
        int age;
        while (true) {
            System.out.print("Enter Age: ");
            if (input.hasNextInt()) {
                age = input.nextInt();
                if (age > 0 && age <= 120) {
                    break;
                } else {
                    System.out.println("Age must be between 1 and 120.");
                }
            } else {
                System.out.println("Please enter a valid number.");
                input.next(); // clear invalid input
            }
        }
        input.nextLine(); // clear buffer

        // -------- GENDER --------
        String gender;
        while (true) {
            System.out.print("Enter Gender (Male/Female): ");
            gender = input.nextLine();

            if (gender.equalsIgnoreCase("male") ||
                gender.equalsIgnoreCase("female")) {
                break;
            } else {
                System.out.println("Gender must be Male, Female.");
            }
        }

        // -------- BIRTH DATE --------
        String birthDate;
        while (true) {
            System.out.print("Enter Birth Date (DD/MM/YY): ");
            birthDate = input.nextLine();

            if (birthDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                break;
            } else {
                System.out.println("Format must be DD/MM/YY.");
            }
        }

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
        String password;
        while (true) {
            System.out.print("Enter Password: ");
            password = input.nextLine();

            if (password.length() >= 8 &&
                password.matches(".*[A-Za-z].*") &&
                password.matches(".*\\d.*")) {
                break;
            } else {
                System.out.println("Password must be ≥ 8 characters and include letters & numbers.");
            }
        }

        // -------- ROLE --------
        String selectedRole = "";
        while (true) {
            System.out.println("Select Role:");
            System.out.println("1. Student");
            System.out.println("2. Educator");
            System.out.print("Enter choice: ");

            if (input.hasNextInt()) {
                int choice = input.nextInt();
                User user;

                if (choice == 1) {
                    user = new Student(name, age, gender, birthDate, email, password);
                    user.displayInfo();
                    selectedRole = "STUDENT";
                    break;
                } else if (choice == 2) {
                    user = new Educator(name, age, gender, birthDate, email, password);
                    user.displayInfo();
                    selectedRole = "EDUCATOR";
                    break;
                } else {
                    System.out.println("Please enter 1 or 2.");
                }
            } else {
                System.out.println("Invalid input. Enter number only.");
                input.next();
            }
        }

        // input.close();
        DataStore dataStore = new DataStore();
        // dataStore.connect();
        try {
            dataStore.InsertUser(name, age, gender, birthDate, email, password, selectedRole);
        } catch (SQLException e) {
            System.out.println("Error inserting user: " + e.getMessage());
        }        
        // dataStore.InsertUser(name, age, gender, birthDate, email, password, "STUDENT");
    }

    @Override
    public void displayInfo() {
        throw new UnsupportedOperationException("Unimplemented method 'displayInfo'");
    }
}
