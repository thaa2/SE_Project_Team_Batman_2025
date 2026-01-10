package quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Question {

    private int id;
    private String text;
    private String[] options; // A-E
    private char correctAnswer;

    private static final String DB_URL = "jdbc:sqlite:quiz.db";

    // ===== Constructors =====
    public Question(int id, String text, String[] options, char correctAnswer) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctAnswer = Character.toUpperCase(correctAnswer);
    }

    public Question(String text, String[] options, char correctAnswer) {
        this(-1, text, options, correctAnswer);
    }

    // ===== Getters =====
    public int getId() { return id; }
    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public char getCorrectAnswer() { return correctAnswer; }

    // ===== DB Methods =====

    // Initialize DB table
    public static void initDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Questions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    text TEXT NOT NULL,
                    optionA TEXT NOT NULL,
                    optionB TEXT NOT NULL,
                    optionC TEXT NOT NULL,
                    optionD TEXT NOT NULL,
                    optionE TEXT NOT NULL,
                    correctAnswer CHAR(1) NOT NULL
                )
            """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Save question to DB
    public void saveToDB() {
        String sql = "INSERT INTO Questions (text, optionA, optionB, optionC, optionD, optionE, correctAnswer) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, this.text);
            for (int i = 0; i < 5; i++) pstmt.setString(i + 2, this.options[i]);
            pstmt.setString(7, String.valueOf(this.correctAnswer));

            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Retrieve all questions from DB
    public static List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM Questions";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String[] opts = new String[5];
                opts[0] = "A. " + rs.getString("optionA");
                opts[1] = "B. " + rs.getString("optionB");
                opts[2] = "C. " + rs.getString("optionC");
                opts[3] = "D. " + rs.getString("optionD");
                opts[4] = "E. " + rs.getString("optionE");
                char correct = rs.getString("correctAnswer").charAt(0);

                questions.add(new Question(id, text, opts, correct));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return questions;
    }
}
