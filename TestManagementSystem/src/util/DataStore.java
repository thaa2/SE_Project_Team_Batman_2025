package util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import auth.User;
import educator.Educator;
import quiz.Question;
import student.Student;

public class DataStore {
    static final String url = "jdbc:sqlite:C:\\ITC\\DATABASE\\School.db\\";

 public void createTables() {
    // 1. Define all SQL strings at the beginning to avoid "cannot be resolved" errors
    String sqlCourses = "CREATE TABLE IF NOT EXISTS Courses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "course_name TEXT NOT NULL, " +
                    "lesson_content TEXT, " + // NEW: Column for lesson text
                    "educator_id INTEGER, " +
                    "FOREIGN KEY(educator_id) REFERENCES user(uers_id))";

String sqlQuestions = "CREATE TABLE IF NOT EXISTS Questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "text TEXT NOT NULL, " +
                "optionA TEXT, optionB TEXT, optionC TEXT, optionD TEXT, optionE TEXT, " +
                "correctAnswer TEXT, " +
                "questionType TEXT, " +
                "numberOfOptions INTEGER, " +
                "educator_id INTEGER, " +
                "course_id INTEGER, " + // Add this line!
                "FOREIGN KEY(educator_id) REFERENCES user(uers_id), " +
                "FOREIGN KEY(course_id) REFERENCES Courses(id))"; // Good practice for linking

    String sqlScores = "CREATE TABLE IF NOT EXISTS QuizScores (" +
                       "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                       "studentName TEXT, " +
                       "totalScore INTEGER, " +
                       "totalQuestions INTEGER, " +
                       "percentage REAL, " +
                       "attemptDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    // 2. Use a single try-with-resources block for efficiency
    try (Connection conn = connect();
         Statement stmt = conn.createStatement()) {
        
        if (conn == null) {
            System.out.println("Error: Database connection failed. Please check your database path and SQLite driver.");
            return;
        }
        
        stmt.execute(sqlCourses);
        stmt.execute(sqlQuestions);
        stmt.execute(sqlScores);
        
        System.out.println("All database tables checked/created successfully.");
        
    } catch (SQLException e) {
        System.out.println("Error creating tables: " + e.getMessage());
    }
}
public void displayAvailableTeachers() {
    // We use uers_id to match your database typo in the screenshot
    String sql = "SELECT uers_id, name FROM user WHERE role = 'EDUCATOR'";
    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        System.out.println("\n--- Available Teachers ---");
        System.out.printf("%-5s | %-20s\n", "ID", "Name");
        while (rs.next()) {
            System.out.printf("%-5d | %-20s\n", rs.getInt("uers_id"), rs.getString("name"));
        }
    } catch (SQLException e) {
        System.out.println("Error displaying teachers: " + e.getMessage());
    }
}

    
    public static Connection connect() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        return connection;
    }

    public List<Question> getQuestionsByEducator(int teacherId) {
        List<Question> questions = new ArrayList<>();
        // Note: Ensure your Questions table column is named educator_id
        String sql = "SELECT * FROM Questions WHERE educator_id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                char correct = rs.getString("correctAnswer").charAt(0);
                questions.add(new Question(id, text, correct)); 
            }
        } catch (SQLException e) { 
            System.out.println("Error fetching educator questions: " + e.getMessage()); 
        }
        return questions;
    }
    public void InsertUser(String name, int age, String gender, String birthDate, String email, String password, String role) {
        String sql = "INSERT INTO user (name, age, gender, birthDate, email, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            pstmt.setString(4, birthDate);
            pstmt.setString(5, email);
            pstmt.setString(6, password);
            pstmt.setString(7, role);
            pstmt.executeUpdate();
            System.out.println("User inserted successfully!");
        } catch (SQLException e) {
            System.out.println("Error inserting user: " + e.getMessage());
        }
    }

    public User getAuthenticatedUser(String email, String password) {
        String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // FIX: Match your DB column name 'uers_id' found in your screenshot
                int id = rs.getInt("uers_id"); 
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String gender = rs.getString("gender");
                String birthDate = rs.getString("birthDate");
                String roleStr = rs.getString("role");

                if (roleStr.equalsIgnoreCase("EDUCATOR")) {
                    return new Educator(id, name, age, gender, birthDate, email, password);
                } else {
                    return new Student(id, name, age, gender, birthDate, email, password);
                }
            }
        } catch (SQLException e) {
            System.out.println("Login database error: " + e.getMessage());
        }
        return null;
    }

    // ADD THIS METHOD: This fixes the "printEducatorList" error in your QuizService screenshot
    public void printEducatorList() {
        String sql = "SELECT uers_id, name FROM user WHERE role = 'EDUCATOR'";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n--- Available Educators ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("uers_id") + " | Name: " + rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Error printing educators: " + e.getMessage());
        }
    }
public void insertQuestion(Question question, int educatorId, int courseId) {
    String sql = "INSERT INTO Questions (text, correctAnswer, educator_id, course_id) VALUES (?, ?, ?, ?)";
    
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, question.getText());
        pstmt.setString(2, String.valueOf(question.getCorrectAnswer()));
        pstmt.setInt(3, educatorId);
        pstmt.setInt(4, courseId); // Save the Course ID here
        
        pstmt.executeUpdate();
        System.out.println("✓ Question linked to Course ID: " + courseId);
    } catch (SQLException e) {
        System.out.println("Error saving question: " + e.getMessage());
    }
}
}