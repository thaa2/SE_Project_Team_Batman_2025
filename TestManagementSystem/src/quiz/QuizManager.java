package quiz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.sql.Statement;

import util.DataStore;

public class QuizManager {
    private final QuizService quizService;
    
    public QuizManager(QuizService quizService) {
        this.quizService = quizService;
    }
// Inside QuizManager.java

public void attemptQuizByTeacher(Scanner sc, int teacherId, String studentName) {
    // 1. Get ONLY this teacher's questions
    List<Question> teacherQuestions = quizService.getQuestionsByTeacher(teacherId);

    if (teacherQuestions.isEmpty()) {
        System.out.println("This teacher has no questions available yet.");
        return;
    }

    // 2. Build a Quiz object using ONLY those questions
    Quiz quiz = new Quiz("Teacher's Quiz");
    for (Question q : teacherQuestions) {
        quiz.addQuestion(q);
    }

    // 3. Start the attempt with THIS filtered quiz
    QuizAttempt attempt = new QuizAttempt(studentName, quiz, sc, quizService);
    attempt.executeAttempt();
}
    
    // ============ UPDATED ADD QUESTIONS METHOD ============
    // Added educatorId parameter here
    public void addQuestions(Scanner sc, int educatorId) {
        System.out.println("\n=== ADD NEW QUESTIONS ===");
        
        while (true) {
            System.out.println("\nSelect question type:");
            System.out.println("1. Multiple Choice Question (MCQ)");
            System.out.println("2. True/False Question");
            System.out.print("Choose (1-2): ");
            
            String typeChoice = sc.nextLine();
            
            if (typeChoice.equals("1")) {
                addMCQQuestion(sc, educatorId); // Pass educatorId
            } else if (typeChoice.equals("2")) {
                addTrueFalseQuestion(sc, educatorId); // Pass educatorId
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
    
    private void addMCQQuestion(Scanner sc, int educatorId) {
        System.out.println("\n--- Add Multiple Choice Question ---");
        
        String text;
        while (true) {
            System.out.print("Enter question text: ");
            text = sc.nextLine().trim();
            if (!text.isEmpty()) break;
            System.out.println("Question text cannot be empty!");
        }
        
        int numOptions;
        while (true) {
            System.out.print("How many options? (3-5): ");
            try {
                numOptions = Integer.parseInt(sc.nextLine());
                if (numOptions >= 3 && numOptions <= 5) break;
                else System.out.println("Please enter a number between 3 and 5.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input!");
            }
        }
        
        String[] options = new String[numOptions];
        for (int i = 0; i < numOptions; i++) {
            while (true) {
                System.out.print("Option " + (char)('A' + i) + ": ");
                String option = sc.nextLine().trim();
                if (!option.isEmpty()) {
                    options[i] = option;
                    break;
                }
                System.out.println("Option cannot be empty!");
            }
        }

        char correct;
        while (true) {
            System.out.print("Correct answer (" + getAnswerRange(numOptions) + "): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.length() > 0) {
                char answer = input.charAt(0);
                if (answer >= 'A' && answer <= (char)('A' + numOptions - 1)) {
                    correct = answer;
                    break;
                }
            }
            System.out.println("Invalid input!");
        }

        // STEP 4 FIX: Pass the educatorId to the save method
        Question question = new Question(text, options, correct, numOptions);
        quizService.saveQuestion(question, educatorId); 
        System.out.println("✓ MCQ question added successfully!");
    }
    
    private void addTrueFalseQuestion(Scanner sc, int educatorId) {
        System.out.println("\n--- Add True/False Question ---");
        
        String text;
        while (true) {
            System.out.print("Enter question text: ");
            text = sc.nextLine().trim();
            if (!text.isEmpty()) break;
        }

        char correct;
        while (true) {
            System.out.print("Correct answer (T for True, F for False): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.equals("T") || input.equals("TRUE")) {
                correct = 'A';
                break;
            } else if (input.equals("F") || input.equals("FALSE")) {
                correct = 'B';
                break;
            }
            System.out.println("Invalid input!");
        }

        // STEP 4 FIX: Pass the educatorId to the save method
        Question question = new Question(text, correct);
        quizService.saveQuestion(question, educatorId); 
        System.out.println("✓ True/False question added successfully!");
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
    // Add to QuizService.java

public void printAllStudents() {
    String sql = "SELECT DISTINCT studentName FROM QuizScores";
    try (Connection conn = DataStore.connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        System.out.println("\n--- Students who have taken quizzes ---");
        while (rs.next()) {
            System.out.println("- " + rs.getString("studentName"));
        }
    } catch (SQLException e) {
        System.err.println("Error: " + e.getMessage());
    }
}

public void printStudentResults(String studentName) {
    String sql = "SELECT totalScore, totalQuestions, percentage FROM QuizScores WHERE studentName = ?";
    try (Connection conn = DataStore.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, studentName);
        ResultSet rs = pstmt.executeQuery();
        System.out.println("\nScore | Total | Percentage");
        while (rs.next()) {
            System.out.printf("%d     | %d     | %.1f%%\n", 
                rs.getInt("totalScore"), 
                rs.getInt("totalQuestions"), 
                rs.getDouble("percentage"));
        }
    } catch (SQLException e) {
        System.err.println("Error: " + e.getMessage());
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
    private String getAnswerRange(int numOptions) {
    return switch (numOptions) {
        case 2 -> "A or B";
        case 3 -> "A, B, or C";
        case 4 -> "A, B, C, or D";
        case 5 -> "A, B, C, D, or E";
        default -> "A-" + (char)('A' + numOptions - 1);
    };
}

}