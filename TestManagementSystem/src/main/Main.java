package main;

import java.util.Scanner;

import auth.Educator;
import auth.Student;
import auth.User;

public class Main {
    public static void main (String[]args){
        Scanner input = new Scanner(System.in);
        
        System.out.println("=== Test Management System ===");

        System.out.print("Enter ID: ");
        int id = input.nextInt();
        input.nextLine();
        
        System.out.print("Enter Name: ");
        String name = input.nextLine();

        System.out.print("Enter Age: ");
        int age = input.nextInt();
        
        input.nextLine(); 
        System.out.print("Enter Gender: ");
        String gender = input.nextLine();

        System.out.print("Enter Birth Date (DD/MM/YY): ");
        String birthDate = input.nextLine(); 

        System.out.print("Enter Email: ");
        String email = input.nextLine();

        System.out.print("Eneter Password: ");
        String password = input.nextLine();

        System.out.println("Select Role:");
        System.out.println("1. Student");
        System.out.println("2. Educator");
        System.out.print("Enter choice: ");

        int choice = input.nextInt();
        User user ;
        if(choice==1){
            user = new Student(id, name , age , gender , birthDate,email,password);
            user.displayInfo();
        }
        else if (choice==2){
            user = new Educator(id, name , age , gender , birthDate,email,password);
            user.displayInfo();
        }
        else {
            System.out.print("Please Enter Number 1 or 2.");
        }
        input.close();
    }
    
}
