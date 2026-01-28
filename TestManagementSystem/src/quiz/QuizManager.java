package quiz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.sql.Statement;
// import quiz.*;

import util.DataStore;
import course.CourseManager;

public class QuizManager {
    private final QuizService quizService;
    private final course.CourseManager courseManager;
    
    public QuizManager(QuizService quizService) {
        this.quizService = quizService;
        this.courseManager = new course.CourseManager();
    }
// Inside QuizManager.java

public void attemptQuizByTeacher(Scanner sc, int teacherId, String studentName) {
    System.out.println("\n--- Show Courses from Teacher ---");
    
    // Display teacher's courses
    String sql = "SELECT id, course_name FROM Courses WHERE educator_id = ? ORDER BY id ASC";
    java.util.List<Integer> courseIds = new java.util.ArrayList<>();
    java.util.List<String> courseNames = new java.util.ArrayList<>();
    
    try (Connection conn = DataStore.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, teacherId);
        ResultSet rs = pstmt.executeQuery();
        
        System.out.println("\n=== Available Courses for This Teacher ===");
        System.out.printf("%-5s | %-30s\n", "ID", "Course Name");
        System.out.println("-".repeat(40));
        
        while (rs.next()) {
            int courseId = rs.getInt("id");
            String courseName = rs.getString("course_name");
            courseIds.add(courseId);
            courseNames.add(courseName);
            System.out.printf("%-5d | %-30s\n", courseId, courseName);
        }
        
        System.out.println("0     | General Questions (No Course)");
    } catch (SQLException e) {
        System.out.println("Error fetching courses: " + e.getMessage());
    }
    
    // Ask which course to take quiz for
    System.out.print("\nEnter Course ID to take quiz (or 0 for general questions): ");
    int selectedCourseId = 0;
    try {
        selectedCourseId = Integer.parseInt(sc.nextLine());
    } catch (NumberFormatException e) {
        System.out.println("Invalid input, using general questions.");
        selectedCourseId = 0;
    }
    
    // Get questions based on selection
    List<Question> teacherQuestions;
    String quizTitle;
    
    if (selectedCourseId > 0) {
        teacherQuestions = quizService.getQuestionsByTeacherAndCourse(teacherId, selectedCourseId);
        // Find the course name for the selected course ID
        String selectedCourseName = "Course " + selectedCourseId;
        for (int i = 0; i < courseIds.size(); i++) {
            if (courseIds.get(i) == selectedCourseId) {
                selectedCourseName = courseNames.get(i);
                break;
            }
        }
        quizTitle = "Course Quiz - " + selectedCourseName;
    } else {
        teacherQuestions = quizService.getQuestionsByTeacher(teacherId);
        quizTitle = "General Questions from Teacher";
    }

    if (teacherQuestions.isEmpty()) {
        System.out.println("\nNo questions available for the selected option.");
        return;
    }

    // Build a Quiz object using the filtered questions
    Quiz quiz = new Quiz(quizTitle);
    for (Question q : teacherQuestions) {
        quiz.addQuestion(q);
    }

    // Start the attempt with THIS filtered quiz
    QuizAttempt attempt = new QuizAttempt(studentName, quiz, sc, quizService);
    attempt.executeAttempt();
}
    
    // ============ UPDATED ADD QUESTIONS METHOD ============
    // Added educatorId parameter and course selection
    public void addQuestions(Scanner sc, int educatorId) {
        System.out.println("\n=== ADD NEW QUESTIONS ===");
        System.out.println("\nDo you want to add questions to a specific course?");
        System.out.println("1. Add to a specific course");
        System.out.println("2. Add general questions (not linked to course)");
        System.out.print("Choose (1-2): ");
        String courseChoice = sc.nextLine().trim();
        
        int courseId = 0;
        if (courseChoice.equals("1")) {
            // Show educator's courses
            courseManager.viewMyCourses(educatorId);
            System.out.print("\nEnter Course ID to add questions to (or 0 to skip): ");
            try {
                courseId = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, adding as general questions.");
                courseId = 0;
            }
        }
        
        final int finalCourseId = courseId;
        while (true) {
            System.out.println("\nSelect question type:");
            System.out.println("0. Exit");
            System.out.println("1. Multiple Choice Question (MCQ)");
            System.out.println("2. True/False Question");
            System.out.println("3. Short Answer Question");
            System.out.print("Choose (0-3): ");
            
            String typeChoice = sc.nextLine();
            
            if (typeChoice.equals("1")) {
                addMCQQuestion(sc, educatorId, finalCourseId);
            } else if (typeChoice.equals("2")) {
                addTrueFalseQuestion(sc, educatorId, finalCourseId);
            } else if (typeChoice.equals("3")) {
                addShortAnswerQuestion(sc, educatorId, finalCourseId);
            }
            else if(typeChoice.equals("0")) {
                break;
            }
            else {
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
    
    private void addShortAnswerQuestion(Scanner sc, int educatorId, int courseId) {
        System.out.println("\n--- Add Short Answer Question ---");
        System.out.print("Enter question text: ");
        String text = sc.nextLine().trim();
        
        System.out.print("Enter the correct answer (word/phrase): ");
        String correctWord = sc.nextLine().trim();

        Question question = new Question(0, text, correctWord, "SHORT");
        quizService.saveQuestion(question, educatorId, courseId);
    }

    private void addMCQQuestion(Scanner sc, int educatorId, int courseId) {
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

        String correct;
        while (true) {
            System.out.print("Correct answer (" + getAnswerRange(numOptions) + "): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.length() > 0) {
                char answer = input.charAt(0);
                if (answer >= 'A' && answer <= (char)('A' + numOptions - 1)) {
                    correct = String.valueOf(answer);
                    break;
                }
            }
            System.out.println("Invalid input!");
        }

        Question question = new Question(0, text, options, correct, "MCQ");
        quizService.saveQuestion(question, educatorId, courseId);
    }
    
    private void addTrueFalseQuestion(Scanner sc, int educatorId, int courseId) {
        System.out.println("\n--- Add True/False Question ---");
        
        String text;
        while (true) {
            System.out.print("Enter question text: ");
            text = sc.nextLine().trim();
            if (!text.isEmpty()) break;
            System.out.println("Question text cannot be empty!");
        }

        String correct;
        while (true) {
            System.out.print("Correct answer (T for True, F for False): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.equals("T") || input.equals("TRUE")) {
                correct = "A";
                break;
            } else if (input.equals("F") || input.equals("FALSE")) {
                correct = "B";
                break;
            }
            System.out.println("Invalid input!");
        }

        Question question = new Question(0, text, correct, "TF");
        quizService.saveQuestion(question, educatorId, courseId);
    }
    
    public void attemptQuiz(Scanner sc) {
        System.out.println("\n=== ATTEMPT QUIZ ===");
        
        var questions = quizService.getAllQuestions();
        if (((String) questions).isEmpty()) {
            System.out.println("No questions available in the database!");
            System.out.println("Please add questions first (Option 1).");
            return;
        }
        
        System.out.print("Enter your name: ");
        String studentName = sc.nextLine();
        
        Quiz quiz = new Quiz("Quiz from Database");
        // for (Question q : questions) {
        //     quiz.addQuestion(q);
        // }
        
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
public void viewAllAvailableCourses() {
    String sql = "SELECT id, course_name FROM Courses";
    try (Connection conn = DataStore.connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        System.out.println("\n--- Available Courses ---");
        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("id") + " | " + rs.getString("course_name"));
        }
    } catch (SQLException e) { System.out.println(e.getMessage()); }
}

public void enrollInCourse(Scanner sc, int studentId) {
    System.out.print("Enter Course ID to enroll: ");
    int courseId = Integer.parseInt(sc.nextLine());
    String sql = "INSERT OR IGNORE INTO Enrollments (student_id, course_id) VALUES (?, ?)";
    try (Connection conn = DataStore.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, studentId);
        pstmt.setInt(2, courseId);
        if (pstmt.executeUpdate() > 0) System.out.println("Enrolled successfully!");
    } catch (SQLException e) { System.out.println(e.getMessage()); }
}

public void viewEnrolledCourses(int studentId) {
    // This JOIN ensures only enrolled courses are shown
    String sql = "SELECT c.course_name, c.lesson_content FROM Courses c " +
                 "JOIN Enrollments e ON c.id = e.course_id WHERE e.student_id = ?";
    try (Connection conn = DataStore.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, studentId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            System.out.println("Course: " + rs.getString("course_name"));
            System.out.println("Content: " + rs.getString("lesson_content") + "\n---");
        }
    } catch (SQLException e) { System.out.println(e.getMessage()); }
}

}