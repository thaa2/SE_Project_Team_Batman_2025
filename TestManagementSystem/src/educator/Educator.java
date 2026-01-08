package educator;

import auth.Role;
import auth.User;

public class Educator extends User {

    public Educator( String name,int age, String gender , String birthDate,  String email, String password) {
        super( name, age, gender, birthDate , email , password, Role.EDUCATOR);
    }

    @Override
    public void displayInfo() {
        System.out.println("Educator ID: " + userId);
        System.out.println("Educator Name: " + name);
        System.out.println("Educator Age: "+age);
        System.out.println("Educator Gender: "+gender);
        System.out.println("Educator Birthdate: "+birthDate);
        System.out.println("Educator Email: " + email);
    }
}


