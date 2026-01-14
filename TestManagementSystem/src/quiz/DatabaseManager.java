package quiz;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:quiz.db";
    private static DatabaseManager instance;
    
    private DatabaseManager() {
        initializeDatabase();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create Questions table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "text TEXT NOT NULL," +
                "optionA TEXT," +
                "optionB TEXT," +
                "optionC TEXT," +
                "optionD TEXT," +
                "optionE TEXT," +
                "correctAnswer CHAR(1) NOT NULL," +
                "questionType TEXT NOT NULL," +
                "numberOfOptions INTEGER," +
                "createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // Create QuizResults table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS QuizResults (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "studentName TEXT NOT NULL," +
                "questionId INTEGER NOT NULL," +
                "selectedAnswer CHAR(1) NOT NULL," +
                "attemptDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(questionId) REFERENCES Questions(id)" +
                ")"
            );
            
            // Create QuizScores table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS QuizScores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "studentName TEXT NOT NULL," +
                "totalScore INTEGER NOT NULL," +
                "totalQuestions INTEGER NOT NULL," +
                "percentage REAL NOT NULL," +
                "attemptDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}