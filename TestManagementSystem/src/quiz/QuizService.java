package quiz;

import java.sql.*;
import java.util.List;

public class QuizService {

    private final String url = "jdbc:sqlite:quiz.db";

    public QuizService() {
        // Create QuizResults table if not exists
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS QuizResults (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    studentName TEXT NOT NULL,
                    questionId INTEGER NOT NULL,
                    selectedAnswer CHAR(1) NOT NULL,
                    FOREIGN KEY(questionId) REFERENCES Questions(id)
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS QuizScores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    studentName TEXT NOT NULL,
                    totalScore INTEGER NOT NULL,
                    totalQuestions INTEGER NOT NULL
                )
            """);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Grade quiz
    public int gradeQuiz(QuizAttempt attempt) {
        int score = 0;
        for (Question q : attempt.getQuiz().getQuestions()) {
            Character studentAnswer = attempt.getAnswers().get(q.getId());
            if (studentAnswer != null && studentAnswer == q.getCorrectAnswer()) score++;
        }
        attempt.setScore(score);

        // Save the attempt to DB
        saveAttempt(attempt);

        return score;
    }

    private void saveAttempt(QuizAttempt attempt) {
        try (Connection conn = DriverManager.getConnection(url)) {
            // Save each question answer
            String sql1 = "INSERT INTO QuizResults (studentName, questionId, selectedAnswer) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                for (Question q : attempt.getQuiz().getQuestions()) {
                    Character ans = attempt.getAnswers().get(q.getId());
                    pstmt.setString(1, attempt.getStudentName());
                    pstmt.setInt(2, q.getId());
                    pstmt.setString(3, ans != null ? ans.toString() : " ");
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // Save total score
            String sql2 = "INSERT INTO QuizScores (studentName, totalScore, totalQuestions) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setString(1, attempt.getStudentName());
                pstmt2.setInt(2, attempt.getScore());
                pstmt2.setInt(3, attempt.getQuiz().getQuestions().size());
                pstmt2.executeUpdate();
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Optional: get all attempts for a student
    public void printStudentResults(String studentName) {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            System.out.println("\nResults for: " + studentName);

            ResultSet rs1 = stmt.executeQuery("SELECT * FROM QuizScores WHERE studentName = '" + studentName + "'");
            while (rs1.next()) {
                System.out.println("Total Score: " + rs1.getInt("totalScore") +
                        "/" + rs1.getInt("totalQuestions"));
            }

            ResultSet rs2 = stmt.executeQuery("SELECT q.text, r.selectedAnswer, q.correctAnswer " +
                    "FROM QuizResults r JOIN Questions q ON r.questionId = q.id " +
                    "WHERE r.studentName = '" + studentName + "'");
            while (rs2.next()) {
                System.out.println("Q: " + rs2.getString("text"));
                System.out.println("Your Answer: " + rs2.getString("selectedAnswer") +
                        ", Correct: " + rs2.getString("correctAnswer") + "\n");
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }
}


