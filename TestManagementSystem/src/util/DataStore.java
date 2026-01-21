package util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import auth.User;
import educator.Educator;
import quiz.Question;
import student.Student;

public class DataStore {
    static final String url = "jdbc:sqlite:School.db";

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
        // Connection conn = connect();
        // if (conn == null) {
        //     System.out.println("Error: Could not establish database connection. Please ensure:");
        //     System.out.println("1. The SQLite JDBC driver (sqlite-jdbc-3.51.1.0.jar) is in the lib/ folder");
        //     System.out.println("2. The classpath includes the lib/ folder when running the program");
        //     return;
        // }
        
        try (Connection conn = connect();Statement stmt = conn.createStatement()) {
            
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
        String sql = "SELECT user_id, name FROM user WHERE role = 'EDUCATOR'";
        try (Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n--- Available Teachers ---");
            System.out.printf("%-5s | %-20s\n", "ID", "Name");
            while (rs.next()) {
                System.out.printf("%-5d | %-20s\n", rs.getInt("user_id"), rs.getString("name"));
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
            System.out.println("Error: " + e.getMessage());
        }
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

    public void role(String role, String name, String gender,int id) throws SQLException{

        if(role.equalsIgnoreCase("STUDENT")){
            String sql = "INSERT INTO student (stu_id, name, gender) VALUES (?, ?, ?)";
            try (Connection connection = connect();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
                String t_id = "S" + id;
                
                pstmt.setString(1, t_id);
                pstmt.setString(2, name);
                pstmt.setString(3, gender);
                pstmt.executeUpdate(); // <--- ADD THIS LINE TO SAVE
                System.out.println("Student saved!");
            }
        }
        else if (role.equalsIgnoreCase("EDUCATOR")){
            String sql = "INSERT INTO teacher (teacher_id, name, gender) VALUES (?, ?, ?)";
            try (Connection connection = connect();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
                String t_id = "T" + id;
                
                pstmt.setString(1, t_id);
                pstmt.setString(2, name);
                pstmt.setString(3, gender);
                pstmt.executeUpdate(); // <--- ADD THIS LINE TO SAVE
                System.out.println("Student saved!");
                
            } 
        }
    }

    

    public void viweResult(){

    }

    

   public void InsertUser(String name, int age, String gender, String birthDate, String email, String password, String role) {
    // 1. Add Statement.RETURN_GENERATED_KEYS to the first prepareStatement
    String sql = "INSERT INTO user (name, age, gender, birthDate, email, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection connection = connect();
         PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        pstmt.setString(1, name);
        pstmt.setInt(2, age);
        pstmt.setString(3, gender);
        pstmt.setString(4, birthDate);
        pstmt.setString(5, email);
        pstmt.setString(6, password);
        pstmt.setString(7, role);
        
        pstmt.executeUpdate();
        System.out.println("User inserted successfully!");

        // 2. Get the ID directly from the pstmt that just finished
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next()) {
                int newId = rs.getInt(1); 
                System.out.println("Generated User ID: " + newId);
                
                // 3. Pass that real ID to your role method
                role(role, name, gender, newId); 
            }
        }

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
        String sql = "SELECT user_id, name FROM user WHERE role = 'EDUCATOR'";
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

// ============ STUDENT RESULTS METHODS ============

public void saveQuizResult(String studentName, int totalScore, int totalQuestions) {
    String sql = "INSERT INTO QuizScores (studentName, totalScore, totalQuestions, percentage) VALUES (?, ?, ?, ?)";
    
    double percentage = (totalQuestions > 0) ? ((double) totalScore / totalQuestions) * 100 : 0;

// ============ STUDENT RESULTS METHODS ============

public void saveQuizResult(String studentName, int totalScore, int totalQuestions) {
    String sql = "INSERT INTO QuizScores (studentName, totalScore, totalQuestions, percentage) VALUES (?, ?, ?, ?)";
    
    double percentage = (totalQuestions > 0) ? ((double) totalScore / totalQuestions) * 100 : 0;
    
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, studentName);
        pstmt.setInt(2, totalScore);
        pstmt.setInt(3, totalQuestions);
        pstmt.setDouble(4, percentage);
        
        pstmt.executeUpdate();
        System.out.println("✓ Quiz result saved to database!");
        
        pstmt.setInt(2, totalScore);
        pstmt.setInt(3, totalQuestions);
        pstmt.setDouble(4, percentage);
        
        pstmt.executeUpdate();
        System.out.println("✓ Quiz result saved to database!");
        
    } catch (SQLException e) {
        System.out.println("Error saving quiz result: " + e.getMessage());
        System.out.println("Error saving quiz result: " + e.getMessage());
    }
}

public void displayStudentResults(String studentName) {
    String sql = "SELECT id, studentName, totalScore, totalQuestions, percentage, attemptDate FROM QuizScores WHERE studentName = ? ORDER BY attemptDate DESC";
    
}

public void displayStudentResults(String studentName) {
    String sql = "SELECT id, studentName, totalScore, totalQuestions, percentage, attemptDate FROM QuizScores WHERE studentName = ? ORDER BY attemptDate DESC";
    
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, studentName);
        ResultSet rs = pstmt.executeQuery();
        
        System.out.println("\n========== Quiz Results for " + studentName + " ==========");
        System.out.printf("%-5s | %-12s | %-10s | %-8s | %-20s\n", 
                          "ID", "Score", "Total Qs", "%", "Attempt Date");
        System.out.println("--------------------------------------------------------------");
        
        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            int id = rs.getInt("id");
            int score = rs.getInt("totalScore");
            int total = rs.getInt("totalQuestions");
            double percentage = rs.getDouble("percentage");
            String date = rs.getString("attemptDate");
            
            System.out.printf("%-5d | %-12s | %-10d | %6.2f%% | %-20s\n", 
                              id, score + "/" + total, total, percentage, date);
        }
        
        if (!hasResults) {
            System.out.println("No results found for this student.");
        }
        System.out.println("=========================================================\n");
        
        pstmt.setString(1, studentName);
        ResultSet rs = pstmt.executeQuery();
        
        System.out.println("\n========== Quiz Results for " + studentName + " ==========");
        System.out.printf("%-5s | %-12s | %-10s | %-8s | %-20s\n", 
                          "ID", "Score", "Total Qs", "%", "Attempt Date");
        System.out.println("--------------------------------------------------------------");
        
        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            int id = rs.getInt("id");
            int score = rs.getInt("totalScore");
            int total = rs.getInt("totalQuestions");
            double percentage = rs.getDouble("percentage");
            String date = rs.getString("attemptDate");
            
            System.out.printf("%-5d | %-12s | %-10d | %6.2f%% | %-20s\n", 
                              id, score + "/" + total, total, percentage, date);
        }
        
        if (!hasResults) {
            System.out.println("No results found for this student.");
        }
        System.out.println("=========================================================\n");
        
    } catch (SQLException e) {
        System.out.println("Error retrieving results: " + e.getMessage());
        System.out.println("Error retrieving results: " + e.getMessage());
    }
}
}