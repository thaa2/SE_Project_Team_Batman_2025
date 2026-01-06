package main;

import java.util.Scanner;

// import auth.Educator;
import auth.Register;
// import auth.Student;
// import auth.User;

public class Main {
    public static void main (String[]args){
        Scanner input = new Scanner(System.in);
        System.out.println("***Welcome to our System***");
        System.out.println("1. Login\n2. Register");
        System.out.print("Enter your choice: ");
        int choice = input.nextInt();

        while (true) {
            if (choice==2){
                Register register = new Register();
                register.registerInfo();
                break;
            }
            else if (choice==1){

            }
            else {
                System.out.println("Please Enter Number 1 or 2");
            }
        }
        input.close();
    }
}
