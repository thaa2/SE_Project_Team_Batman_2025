package main;

import java.util.Scanner;
import auth.*;
import quiz.QuizController;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Test Management System ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            if (!sc.hasNextInt()) {
                sc.next();
                continue;
            }

            int choice = sc.nextInt();
            sc.nextLine(); // clear buffer

            if (choice == 1) {

                Login login = new Login();
                User user = (User) login.loginInfo(); // MUST return User

                if (user == null) continue;

                // ===== ROLE-BASED ACCESS =====
                if (user.getRole() == Role.EDUCATOR) {
                    System.out.println("\nWelcome Educator!");
                    QuizController.addQuestions(sc);

                } else if (user.getRole() == Role.STUDENT) {
                    System.out.println("\nWelcome Student!");
                    QuizController.takeQuiz(sc);
                }

            } 
            else if (choice == 2) {
                new Register().registerInfo();
            } 
            else if (choice == 3) {
                System.out.println("Goodbye!");
                break;
            }
        }

        sc.close();
    }
}
