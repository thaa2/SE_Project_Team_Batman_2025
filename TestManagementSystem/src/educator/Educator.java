package educator;

import auth.Role;
import auth.User;

public class Educator extends User {
    public Educator(int userId, String name, int age, String gender, String birthDate, String email, String password) {
        // Must match your User constructor exactly
        super(userId, name, age, gender, birthDate, email, password, Role.EDUCATOR);
    }

    @Override
    public void displayInfo() {
        System.out.println("Educator Name: " + name);
        System.out.println("Educator Email: " + email);
    }
}