package student;

import auth.Role;
import auth.User;

public class Student extends User {

    // The constructor must accept all parameters needed to satisfy the parent 'User' class
    public Student(int userId, String name, int age, String gender, String birthDate, String email, String password) {
        // 'super' must be the first line. 
        // We pass 'Role.STUDENT' automatically so the user doesn't have to type it during login/reg.
        super(userId,  name, age, gender, birthDate, email, password, Role.STUDENT);
    }

    @Override
    public void displayInfo() {
        System.out.println("\n--- STUDENT PROFILE ---");
        // 'userId' is inherited from the User class
        System.out.println("Student ID: " + userId); 
        System.out.println("Name:       " + name);
        System.out.println("Age:        " + age);
        System.out.println("Gender:     " + gender);
        System.out.println("Birthdate:  " + birthDate);
        System.out.println("Email:      " + email);
        System.out.println("Role:       " + role);
        System.out.println("-----------------------");
    }
}