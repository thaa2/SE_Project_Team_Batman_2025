package quiz;

import java.util.Scanner;

public class QuizManager {
    private final QuizService quizService;
    
    public QuizManager(QuizService quizService) {
        this.quizService = quizService;
    }
    
    public void addQuestions(Scanner sc) {
        System.out.println("\n=== ADD NEW QUESTIONS ===");
        
        while (true) {
            System.out.println("\nSelect question type:");
            System.out.println("1. Multiple Choice Question (MCQ)");
            System.out.println("2. True/False Question");
            System.out.print("Choose (1-2): ");
            
            String typeChoice = sc.nextLine();
            
            if (typeChoice.equals("1")) {
                addMCQQuestion(sc);
            } else if (typeChoice.equals("2")) {
                addTrueFalseQuestion(sc);
            } else {
                System.out.println("Invalid choice!");
                continue;
            }

            System.out.print("\nAdd another question? (y/n): ");
            String addMore = sc.nextLine();
            if (!addMore.equalsIgnoreCase("y")) {
                break;
            }
        }
    }
    
    private void addMCQQuestion(Scanner sc) {
        System.out.println("\n--- Add Multiple Choice Question ---");
        
        System.out.print("Enter question text: ");
        String text = sc.nextLine();
        
        System.out.print("How many options? (3-5): ");
        int numOptions;
        try {
            numOptions = Integer.parseInt(sc.nextLine());
            if (numOptions < 3 || numOptions > 5) {
                System.out.println("Invalid number! Using 4 options by default.");
                numOptions = 4;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Using 4 options by default.");
            numOptions = 4;
        }
        
        String[] options = new String[numOptions];
        char optionChar = 'A';
        for (int i = 0; i < numOptions; i++) {
            System.out.print("Option " + optionChar + ": ");
            options[i] = sc.nextLine();
            optionChar++;
        }

        char correct;
        while (true) {
            System.out.print("Correct answer (" + getAnswerRange(numOptions) + "): ");
            String input = sc.nextLine().toUpperCase();
            if (input.length() > 0) {
                char answer = input.charAt(0);
                char maxOption = (char) ('A' + numOptions - 1);
                if (answer >= 'A' && answer <= maxOption) {
                    correct = answer;
                    break;
                }
            }
            System.out.println("Invalid input! Please enter " + getAnswerRange(numOptions));
        }

        Question question = new Question(text, options, correct, numOptions);
        quizService.saveQuestion(question);
        System.out.println(" MCQ question added successfully!");
    }
    
    private void addTrueFalseQuestion(Scanner sc) {
        System.out.println("\n--- Add True/False Question ---");
        
        System.out.print("Enter question text: ");
        String text = sc.nextLine();

        char correct;
        while (true) {
            System.out.print("Correct answer (T for True, F for False): ");
            String input = sc.nextLine().toUpperCase();
            if (input.equals("T") || input.equals("TRUE")) {
                correct = 'A';
                break;
            } else if (input.equals("F") || input.equals("FALSE")) {
                correct = 'B';
                break;
            }
            System.out.println("Invalid input! Please enter T or F.");
        }

        Question question = new Question(text, correct);
        quizService.saveQuestion(question);
        System.out.println("True/False question added successfully!");
    }
    
    private String getAnswerRange(int numOptions) {
        switch (numOptions) {
            case 3: return "A-C";
            case 4: return "A-D";
            case 5: return "A-E";
            default: return "A-C";
        }
    }
    
    public void attemptQuiz(Scanner sc) {
        System.out.println("\n=== ATTEMPT QUIZ ===");
        
        var questions = quizService.getAllQuestions();
        if (questions.isEmpty()) {
            System.out.println("No questions available in the database!");
            System.out.println("Please add questions first (Option 1).");
            return;
        }
        
        System.out.print("Enter your name: ");
        String studentName = sc.nextLine();
        
        Quiz quiz = new Quiz("Quiz from Database");
        for (Question q : questions) {
            quiz.addQuestion(q);
        }
        
        QuizAttempt attempt = new QuizAttempt(studentName, quiz, sc, quizService);
        attempt.executeAttempt();
    }
    
    // NEW METHOD: View all students results
    public void viewAllStudentsResults(Scanner sc) {
        System.out.println("\n=== VIEW ALL STUDENTS RESULTS ===");
        
        // First show summary of all students
        quizService.printAllStudents();
        
        System.out.print("\nEnter student name to view detailed results (or press Enter to go back): ");
        String studentName = sc.nextLine().trim();
        
        if (!studentName.isEmpty()) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("DETAILED RESULTS FOR: " + studentName);
            System.out.println("=".repeat(50));
            
            // Show student statistics
            quizService.printStudentStatistics(studentName);
            
            System.out.print("\nDo you want to see full quiz history? (y/n): ");
            String choice = sc.nextLine().toLowerCase();
            if (choice.equals("y")) {
                quizService.printStudentResults(studentName);
            }
        } else {
            System.out.println("Returning to main menu...");
        }
    }
    
    // Keep existing method for backward compatibility
    public void viewResults(Scanner sc) {
        System.out.println("\n=== VIEW QUIZ RESULTS ===");
        
        System.out.print("Enter student name to view results: ");
        String studentName = sc.nextLine();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("RESULTS FOR: " + studentName);
        System.out.println("=".repeat(50));
        
        quizService.printStudentResults(studentName);
    }
}