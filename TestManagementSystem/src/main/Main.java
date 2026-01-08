package main;

import java.util.Scanner;
// import java.util.concurrent.TimeUnit;
// import auth.Educator;
import auth.*;
// import auth.Student;
// import auth.User;

public class Main {
    public static void main (String[]args){
        Scanner input = new Scanner(System.in);
        while (true) {
            int choice;
            System.out.println("===Welcome to our System===");
            System.out.println("1. Login\n2. Register\n3. Exit");
            System.out.print("Enter your choice: ");
            
            choice = input.nextInt();

            
            if (choice==2){
                Register register = new Register();
                register.registerInfo();
                
            }
            else if (choice==1){
                Login login = new Login();
                login.loginInfo();
                
            }
            else if (choice==3){
                System.out.println("Exiting the system. Goodbye!");
                System.exit(0);
                break;
            }
            else {
                System.out.println("Please Enter Number 1 or 2");
            }  
            // input.close();
        }
        
       
    }
}
