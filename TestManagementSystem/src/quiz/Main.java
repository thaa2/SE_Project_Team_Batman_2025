package quiz;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("=== QUIZ APPLICATION ===");
        
        try {
            DatabaseManager.getInstance();
            QuizService service = new QuizService();
            QuizManager quizManager = new QuizManager(service);
            
            while (true) {
                System.out.println("\n=== MAIN MENU ===");
                System.out.println("1. Add New Questions");
                System.out.println("2. Show All Questions");
                System.out.println("3. Attempt Quiz");
                System.out.println("4. View All Students Results");
                System.out.println("0. Exit");
                System.out.print("Choose an option (0-4): ");
                
                String input = sc.nextLine();
                
                try {
                    int choice = Integer.parseInt(input);
                    
                    switch (choice) {
                        case 1 -> quizManager.addQuestions(sc);
                        case 2 -> service.printAllQuestions();
                        case 3 -> quizManager.attemptQuiz(sc);
                        case 4 -> quizManager.viewAllStudentsResults(sc);
                        case 0 -> {
                            System.out.println("Goodbye!");
                            sc.close();
                            return;
                        }
                        default -> System.out.println(" Invalid choice! Please select 0-4.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println(" Please enter a valid number (0-4)!");
                }
            }
        } catch (Exception e) {
            System.err.println(" Fatal error: " + e.getMessage());
            e.printStackTrace();
            sc.close();
        }
    }
}