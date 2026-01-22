package util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import auth.*;
import educator.Educator;
import quiz.Question;
import student.Student;

public class DataStore {
    // FIXED: Use forward slashes or escaped backslashes
    // Option 1: Forward slashes (recommended)
    static final String url = "jdbc:sqlite:School.db";
    
    // Option 2: Escaped backslashes (alternative)
    // static final String url = "jdbc:sqlite:C:\\School.db";
    
    // Option 3: Relative path (creates database in project folder)
    // static final String url = "jdbc:sqlite:School.db";

    public void createTables() {
        Connection conn = connect();
        if (conn == null) {
            System.out.println("Error: Database connection is null. Cannot create tables.");
            return;
        }
        
        // Define all SQL strings
        String sqlUser = "CREATE TABLE IF NOT EXISTS user (" +
                        "uers_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "age INTEGER, " +
                        "gender TEXT, " +
                        "birthDate TEXT, " +
                        "email TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "role TEXT NOT NULL)";

        String sqlCourses = "CREATE TABLE IF NOT EXISTS Courses (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "course_name TEXT NOT NULL, " +
                        "lesson_content TEXT, " +
                        "educator_id INTEGER, " +
                        "FOREIGN KEY(educator_id) REFERENCES user(uers_id))";

        String sqlQuestions = "CREATE TABLE IF NOT EXISTS Questions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "text TEXT NOT NULL, " +
                        "optionA TEXT, optionB TEXT, optionC TEXT, optionD TEXT, optionE TEXT, " +
                        "options TEXT, " +
                        "correctAnswer TEXT, " +
                        "question_type TEXT, " +
                        "questionType TEXT, " +
                        "numberOfOptions INTEGER, " +
                        "educator_id INTEGER, " +
                        "course_id INTEGER, " +
                        "FOREIGN KEY(educator_id) REFERENCES user(uers_id), " +
                        "FOREIGN KEY(course_id) REFERENCES Courses(id))";

        String sqlScores = "CREATE TABLE IF NOT EXISTS QuizScores (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "studentName TEXT, " +
                        "totalScore INTEGER, " +
                        "totalQuestions INTEGER, " +
                        "percentage REAL, " +
                        "attemptDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        
        String sqlStudent = "CREATE TABLE IF NOT EXISTS student (" +
                        "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER UNIQUE, " +
                        "gpa REAL, " +
                        "major TEXT, " +
                        "FOREIGN KEY(user_id) REFERENCES user(uers_id))";
        
        String sqlEnrollments = "CREATE TABLE IF NOT EXISTS Enrollments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "student_id INTEGER, " +
                        "course_id INTEGER, " +
                        "enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(student_id) REFERENCES student(student_id), " +
                        "FOREIGN KEY(course_id) REFERENCES Courses(id))";
        
        String sqlTeacher = "CREATE TABLE IF NOT EXISTS teacher (" +
                        "teacher_id TEXT PRIMARY KEY, " +
                        "user_id INTEGER UNIQUE, " +
                        "name TEXT, " +
                        "gender TEXT, " +
                        "FOREIGN KEY(user_id) REFERENCES user(uers_id))";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUser);
            stmt.execute(sqlCourses);
            stmt.execute(sqlQuestions);
            stmt.execute(sqlScores);
            stmt.execute(sqlStudent);
            stmt.execute(sqlEnrollments);
            stmt.execute(sqlTeacher);
            
            System.out.println("✓ All database tables checked/created successfully.");
            
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void displayAvailableTeachers() {
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
            e.printStackTrace();
        }
    }
        
    public static Connection connect() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
            System.out.println("✓ Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ SQLite Driver not found: " + e.getMessage());
            System.out.println("Make sure sqlite-jdbc.jar is in your classpath!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Connection error: " + e.getMessage());
            System.out.println("Check database path: " + url);
            e.printStackTrace();
        }
        return connection;
    }

    public List<Question> getQuestionsByEducator(int teacherId) {
        List<Question> questions = new ArrayList<>();
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
            e.printStackTrace();
        }
        return questions;
    }

    public void role(String role, String name, String gender, int id) throws SQLException {
        if (role.equalsIgnoreCase("STUDENT")) {
            String sql = "INSERT INTO student (student_id, user_id, name, gender) VALUES (?, ?, ?, ?)";
            try (Connection connection = connect();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
                String s_id = "S" + id;
                
                pstmt.setString(1, s_id);
                pstmt.setInt(2, id);
                pstmt.setString(3, name);
                pstmt.setString(4, gender);
                pstmt.executeUpdate();
                System.out.println("✓ Student saved!");
            } catch (SQLException e) {
                System.out.println("Error saving student: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } else if (role.equalsIgnoreCase("EDUCATOR")) {
            String sql = "INSERT INTO teacher (teacher_id, user_id, name, gender) VALUES (?, ?, ?, ?)";
            try (Connection connection = connect();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
                String t_id = "T" + id;
                
                pstmt.setString(1, t_id);
                pstmt.setInt(2, id);
                pstmt.setString(3, name);
                pstmt.setString(4, gender);
                pstmt.executeUpdate();
                System.out.println("✓ Educator saved!");
            } catch (SQLException e) {
                System.out.println("Error saving educator: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
    }

    public void InsertUser(String name, int age, String gender, String birthDate, String email, String password, String role) {
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
            System.out.println("✓ User inserted successfully!");

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1); 
                    System.out.println("✓ Generated User ID: " + newId);
                    
                    role(role, name, gender, newId); 
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting user: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return null;
    }

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
            e.printStackTrace();
        }
    }

    public void insertQuestion(Question question, int educatorId, int courseId) {
        String sql = "INSERT INTO Questions (text, correctAnswer, educator_id, course_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, question.getText());
            pstmt.setString(2, String.valueOf(question.getCorrectAnswer()));
            pstmt.setInt(3, educatorId);
            pstmt.setInt(4, courseId);
            
            pstmt.executeUpdate();
            System.out.println("✓ Question linked to Course ID: " + courseId);
        } catch (SQLException e) {
            System.out.println("Error saving question: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
            
        } catch (SQLException e) {
            System.out.println("Error saving quiz result: " + e.getMessage());
            e.printStackTrace();
        }
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
            
        } catch (SQLException e) {
            System.out.println("Error retrieving results: " + e.getMessage());
            e.printStackTrace();
        }
    }
}