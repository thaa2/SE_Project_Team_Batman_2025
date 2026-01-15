package quiz;

import java.util.Scanner;

public class QuizManager {
    private final QuizService quizService;
    
    public QuizManager(QuizService quizService) {
        this.quizService = quizService;
    }
    
    // ============ DELETE QUESTION METHOD ============
    
    public void deleteQuestionOperation(Scanner sc) {
        System.out.println("\n=== DELETE QUESTION ===");
        
        // First show all questions
        quizService.printAllQuestions();
        
        if (quizService.getAllQuestions().isEmpty()) {
            System.out.println("No questions available to delete.");
            return;
        }
        
        System.out.print("\nEnter the ID of the question to delete (0 to cancel): ");
        try {
            int questionId = Integer.parseInt(sc.nextLine());
            
            if (questionId == 0) {
                System.out.println("Operation cancelled.");
                return;
            }
            
            // Check if question exists
            if (!quizService.questionExists(questionId)) {
                System.out.println("✗ Question ID " + questionId + " does not exist!");
                return;
            }
            
            System.out.print("Are you sure you want to delete question ID " + questionId + "? (yes/no): ");
            String confirmation = sc.nextLine().toLowerCase();
            
            if (confirmation.equals("yes") || confirmation.equals("y")) {
                boolean success = quizService.deleteQuestion(questionId);
                if (success) {
                    System.out.println("✓ Question deleted successfully!");
                } else {
                    System.out.println("✗ Failed to delete question.");
                }
            } else {
                System.out.println("Operation cancelled.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a valid number.");
        }
    }
    
    // ============ EXISTING METHODS ============
    
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
        
        // Get question text (cannot be empty)
        String text;
        while (true) {
            System.out.print("Enter question text: ");
            text = sc.nextLine().trim();
            if (!text.isEmpty()) {
                break;
            }
            System.out.println("Question text cannot be empty! Please enter a question.");
        }
        
        // Get number of options
        int numOptions;
        while (true) {
            System.out.print("How many options? (3-5): ");
            String input = sc.nextLine();
            try {
                numOptions = Integer.parseInt(input);
                if (numOptions >= 3 && numOptions <= 5) {
                    break;
                } else {
                    System.out.println("Please enter a number between 3 and 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number (3-5).");
            }
        }
        
        // Get options (cannot be empty)
        String[] options = new String[numOptions];
        char optionChar = 'A';
        for (int i = 0; i < numOptions; i++) {
            while (true) {
                System.out.print("Option " + optionChar + ": ");
                String option = sc.nextLine().trim();
                if (!option.isEmpty()) {
                    // Remove any existing "A. " prefix if user accidentally includes it
                    if (option.length() > 3 && option.substring(0, 3).matches("[A-E]\\.\\s")) {
                        option = option.substring(3);
                    }
                    options[i] = option;
                    break;
                }
                System.out.println("Option cannot be empty! Please enter option " + optionChar + ".");
            }
            optionChar++;
        }

        // Get correct answer
        char correct;
        while (true) {
            System.out.print("Correct answer (" + getAnswerRange(numOptions) + "): ");
            String input = sc.nextLine().trim().toUpperCase();
            
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

        // Create question and save
        Question question = new Question(text, options, correct, numOptions);
        quizService.saveQuestion(question);
        System.out.println("✓ MCQ question added successfully!");
    }
    
    private void addTrueFalseQuestion(Scanner sc) {
        System.out.println("\n--- Add True/False Question ---");
        
        // Get question text (cannot be empty)
        String text;
        while (true) {
            System.out.print("Enter question text: ");
            text = sc.nextLine().trim();
            if (!text.isEmpty()) {
                break;
            }
            System.out.println("Question text cannot be empty! Please enter a question.");
        }

        // Get correct answer
        char correct;
        while (true) {
            System.out.print("Correct answer (T for True, F for False): ");
            String input = sc.nextLine().trim().toUpperCase();
            
            if (input.length() > 0) {
                if (input.equals("T") || input.equals("TRUE")) {
                    correct = 'A';
                    break;
                } else if (input.equals("F") || input.equals("FALSE")) {
                    correct = 'B';
                    break;
                }
            }
            System.out.println("Invalid input! Please enter T or F.");
        }

        Question question = new Question(text, correct);
        quizService.saveQuestion(question);
        System.out.println("✓ True/False question added successfully!");
    }
    
    private String getAnswerRange(int numOptions) {
        switch (numOptions) {
            case 3: return "A, B, or C";
            case 4: return "A, B, C, or D";
            case 5: return "A, B, C, D, or E";
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
        
        QuizeAttempt attempt = new QuizeAttempt(studentName, quiz, sc, quizService);
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