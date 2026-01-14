package quiz;

import java.util.Scanner;
import auth.User;
import auth.Role;

public class QuizController {

    public static void runQuizModule(Scanner sc, User user) {
        try {
            DatabaseManager.getInstance();
            QuizService service = new QuizService();
            QuizManager quizManager = new QuizManager(service);
            
            boolean exitModule = false;
            while (!exitModule) {
                System.out.println("\n=== QUIZ SYSTEM (" + user.getRole() + ") ===");
                
                // Common options for everyone
                System.out.println("1. View My Results");
                System.out.println("2. Attempt Quiz");

                // Restricted options for Educators only
                if (user.getRole() == Role.EDUCATOR) {
                    System.out.println("3. Create new Quiz");
                    System.out.println("4. View All Students Results");
                    System.out.println("5. Show All Questions in Bank");
                }
                
                System.out.println("0. Logout/Back");
                System.out.print("Choose: ");
                
                String input = sc.nextLine();
                int choice = Integer.parseInt(input);
                
                switch (choice) {
                    case 1 -> quizManager.viewAllStudentsResults(sc); // Logic would filter for current user
                    case 2 -> quizManager.attemptQuiz(sc);
                    case 3 -> {
                        if (user.getRole() == Role.EDUCATOR) quizManager.addQuestions(sc);
                        else System.out.println("Access Denied.");
                    }
                    case 4 -> {
                        if (user.getRole() == Role.EDUCATOR) quizManager.viewAllStudentsResults(sc);
                        else System.out.println("Access Denied.");
                    }
                    case 0 -> exitModule = true;
                    default -> System.out.println("Invalid choice!");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}