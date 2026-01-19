package quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.DataStore;

public class QuizService {
    
    private DataStore dataStore = new DataStore();

// Add these inside public class QuizService

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
        System.err.println("Error loading students: " + e.getMessage());
    }
}

public void printStudentResults(String studentName) {
    String sql = "SELECT totalScore, totalQuestions, percentage, attemptDate FROM QuizScores WHERE studentName = ?";
    try (Connection conn = DataStore.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, studentName);
        ResultSet rs = pstmt.executeQuery();
        System.out.println("Date | Score | Total | %");
        while (rs.next()) {
            System.out.printf("%s | %d | %d | %.1f%%\n", 
                rs.getString("attemptDate"), rs.getInt("totalScore"), 
                rs.getInt("totalQuestions"), rs.getDouble("percentage"));
        }
    } catch (SQLException e) {
        System.err.println("Error loading results: " + e.getMessage());
    }
}
    public QuizService() {
        // Constructor remains simple as we use DataStore for connections
    }

    // ============ 1. SAVE QUESTION ============
    public void saveQuestion(Question question, int educatorId) {
        String sql = "INSERT INTO Questions (text, optionA, optionB, optionC, optionD, optionE, correctAnswer, questionType, numberOfOptions, educator_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, question.getText());
            
            if (question.getQuestionType().equals("TF")) {
                pstmt.setString(2, "True");
                pstmt.setString(3, "False");
                pstmt.setNull(4, java.sql.Types.VARCHAR);
                pstmt.setNull(5, java.sql.Types.VARCHAR);
                pstmt.setNull(6, java.sql.Types.VARCHAR);
                pstmt.setInt(9, 2);
            } else {
                String[] options = question.getOptions();
                for (int i = 0; i < 5; i++) {
                    if (i < options.length && options[i] != null) {
                        String option = options[i];
                        if (option.length() > 3 && option.substring(0, 3).matches("[A-E]\\.\\s")) {
                            option = option.substring(3);
                        }
                        pstmt.setString(i + 2, option);
                    } else {
                        pstmt.setNull(i + 2, java.sql.Types.VARCHAR);
                    }
                }
                pstmt.setInt(9, question.getNumberOfOptions());
            }
            
            pstmt.setString(7, String.valueOf(question.getCorrectAnswer()));
            pstmt.setString(8, question.getQuestionType());
            pstmt.setInt(10, educatorId); 
            
            pstmt.executeUpdate();
            System.out.println("✓ Question saved to database by educator ID: " + educatorId);
            
        } catch (SQLException e) { 
            System.err.println("✗ Error saving question: " + e.getMessage());
        }
    }

    // ============ 2. DELETE METHODS ============
    public boolean deleteQuestion(int questionId) {
        String sql = "DELETE FROM Questions WHERE id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                deleteQuizResultsForQuestion(questionId);
                return true;
            }
        } catch (SQLException e) { 
            System.err.println("✗ Error deleting question: " + e.getMessage());
        }
        return false;
    }

    private void deleteQuizResultsForQuestion(int questionId) {
        String sql = "DELETE FROM QuizResults WHERE questionId = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("Note: Could not clean up related quiz results: " + e.getMessage());
        }
    }

    // ============ 3. FETCHING QUESTIONS ============
    public List<Question> getQuestionsByTeacher(int teacherId) {
        return dataStore.getQuestionsByEducator(teacherId);
    }

    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM Questions ORDER BY id";
        
        try (Connection conn = DataStore.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String type = rs.getString("questionType");
                char correct = rs.getString("correctAnswer").charAt(0);
                
                if ("TF".equals(type)) {
                    questions.add(new Question(id, text, correct));
                } else {
                    int numOpts = rs.getInt("numberOfOptions");
                    String[] opts = new String[numOpts];
                    String[] cols = {"optionA", "optionB", "optionC", "optionD", "optionE"};
                    for (int i = 0; i < numOpts; i++) {
                        opts[i] = (char)('A' + i) + ". " + rs.getString(cols[i]);
                    }
                    questions.add(new Question(id, text, opts, correct, numOpts));
                }
            }
        } catch (SQLException e) { 
            System.err.println("✗ Error loading questions: " + e.getMessage());
        }
        return questions;
    }

    // ============ 4. GRADING & ATTEMPTS ============
    public int gradeQuiz(QuizAttempt attempt) {
        int score = 0;
        for (Question q : attempt.getQuiz().getQuestions()) {
            Character studentAnswer = attempt.getAnswers().get(q.getId());
            if (studentAnswer != null && studentAnswer == q.getCorrectAnswer()) {
                score++;
            }
        }
        attempt.setScore(score);
        saveAttempt(attempt);
        return score;
    }

    private void saveAttempt(QuizAttempt attempt) {
        try (Connection conn = DataStore.connect()) {
            conn.setAutoCommit(false);
            try {
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

                double percentage = (double) attempt.getScore() / attempt.getQuiz().getQuestions().size() * 100;
                String sql2 = "INSERT INTO QuizScores (studentName, totalScore, totalQuestions, percentage) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                    pstmt2.setString(1, attempt.getStudentName());
                    pstmt2.setInt(2, attempt.getScore());
                    pstmt2.setInt(3, attempt.getQuiz().getQuestions().size());
                    pstmt2.setDouble(4, percentage);
                    pstmt2.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) { 
            System.err.println("Error saving attempt: " + e.getMessage());
        }
    }

    // ============ 5. STATISTICS ============
    public void printStudentStatistics(String studentName) {
        String sql = "SELECT COUNT(*) as attempts, AVG(percentage) as avg FROM QuizScores WHERE studentName = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt("attempts") > 0) {
                System.out.println("\n--- Stats for " + studentName + " ---");
                System.out.println("Total Quizzes: " + rs.getInt("attempts"));
                System.out.println("Average Score: " + String.format("%.1f%%", rs.getDouble("avg")));
            } else {
                System.out.println("No records found for " + studentName);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}