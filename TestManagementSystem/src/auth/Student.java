package auth;

public class Student extends User {

    public Student( String name, int age , String gender , String birthDate , String email, String password) {
        super(name, age, gender, birthDate , email , password, Role.STUDENT);
    }

    @Override
    public void displayInfo() {
        System.out.println("Student ID: " + userId);
        System.out.println("Student Name: " + name);
        System.out.println("Student Age: "+age);
        System.out.println("Student Gender: "+gender);
        System.out.println("Student Birthdate: "+birthDate);
        System.out.println("Student Email: " + email);
    }
}

