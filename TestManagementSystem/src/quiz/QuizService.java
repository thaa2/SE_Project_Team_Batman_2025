package quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService {
    private DatabaseManager dbManager;
    
    public QuizService() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    // Add this NEW METHOD to show all questions
    public void printAllQuestions() {
        List<Question> questions = getAllQuestions();
        
        if (questions.isEmpty()) {
            System.out.println("\nNo questions available in the database!");
            System.out.println("Please add questions first (Option 1).");
            return;
        }
        
        System.out.println("\n=== ALL QUESTIONS ===");
        System.out.println("Total Questions: " + questions.size());
        System.out.println("-".repeat(50));
        
        int questionNum = 1;
        for (Question q : questions) {
            System.out.println("\nQ" + questionNum + " [" + q.getQuestionType() + "] (ID: " + q.getId() + "):");
            System.out.println("Question: " + q.getText());
            
            if (q.getQuestionType().equals("TF")) {
                System.out.println("Options:");
                System.out.println("  A. True");
                System.out.println("  B. False");
            } else {
                System.out.println("Options:");
                for (String opt : q.getOptions()) {
                    System.out.println("  " + opt);
                }
            }
            
            System.out.println("Correct Answer: " + q.getCorrectAnswer());
            System.out.println("Answer Range: " + q.getAnswerRange());
            System.out.println("-".repeat(40));
            questionNum++;
        }
    }
    
    // Add this NEW METHOD to get student statistics
    public void printStudentStatistics(String studentName) {
        try (Connection conn = dbManager.getConnection()) {
            
            // Get basic student info
            String sql1 = "SELECT " +
                         "COUNT(*) as totalAttempts, " +
                         "AVG(percentage) as avgPercentage, " +
                         "SUM(totalScore) as totalScore, " +
                         "SUM(totalQuestions) as totalQuestions " +
                         "FROM QuizScores " +
                         "WHERE studentName = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                pstmt.setString(1, studentName);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    int totalAttempts = rs.getInt("totalAttempts");
                    double avgPercentage = rs.getDouble("avgPercentage");
                    int totalScore = rs.getInt("totalScore");
                    int totalQuestions = rs.getInt("totalQuestions");
                    
                    if (totalAttempts > 0) {
                        System.out.println("\n" + "=".repeat(40));
                        System.out.println("STUDENT: " + studentName);
                        System.out.println("=".repeat(40));
                        System.out.println("Total Attempts: " + totalAttempts);
                        
                        if (totalQuestions > 0) {
                            double overallPercentage = (double) totalScore / totalQuestions * 100;
                            System.out.println("Overall Score: " + totalScore + "/" + totalQuestions);
                            System.out.println("Overall Percentage: " + String.format("%.1f%%", overallPercentage));
                        }
                        
                        if (!rs.wasNull()) {
                            System.out.println("Average Score: " + String.format("%.1f%%", avgPercentage));
                        }
                        
                        // Get last attempt date
                        String sql2 = "SELECT MAX(attemptDate) as lastAttempt FROM QuizScores WHERE studentName = ?";
                        try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                            pstmt2.setString(1, studentName);
                            ResultSet rs2 = pstmt2.executeQuery();
                            if (rs2.next() && rs2.getTimestamp("lastAttempt") != null) {
                                System.out.println("Last Attempt: " + rs2.getTimestamp("lastAttempt"));
                            }
                        }
                    } else {
                        System.out.println("No attempts found for " + studentName);
                    }
                }
            }
            
        } catch (SQLException e) { 
            System.err.println("✗ Error retrieving student statistics: " + e.getMessage());
        }
    }
    
    // Keep all existing methods below...
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
        try (Connection conn = dbManager.getConnection()) {
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
    
    public void saveQuestion(Question question) {
        String sql = "INSERT INTO Questions (text, optionA, optionB, optionC, optionD, optionE, correctAnswer, questionType, numberOfOptions) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, question.getText());
            
            if (question.getQuestionType().equals("TF")) {
                pstmt.setString(2, "True");
                pstmt.setString(3, "False");
                pstmt.setNull(4, Types.VARCHAR);
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setNull(6, Types.VARCHAR);
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
                        pstmt.setNull(i + 2, Types.VARCHAR);
                    }
                }
                pstmt.setInt(9, question.getNumberOfOptions());
            }
            
            pstmt.setString(7, String.valueOf(question.getCorrectAnswer()));
            pstmt.setString(8, question.getQuestionType());
            pstmt.executeUpdate();
            
            System.out.println("✓ Question saved to database!");
            
        } catch (SQLException e) { 
            System.err.println("✗ Error saving question: " + e.getMessage());
        }
    }
    
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM Questions ORDER BY id";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String questionType = rs.getString("questionType");
                char correct = rs.getString("correctAnswer").charAt(0);
                
                if (questionType.equals("TF")) {
                    questions.add(new Question(id, text, correct));
                } else {
                    int numberOfOptions = rs.getInt("numberOfOptions");
                    String[] opts = new String[numberOfOptions];
                    
                    String[] dbOpts = new String[5];
                    dbOpts[0] = rs.getString("optionA");
                    dbOpts[1] = rs.getString("optionB");
                    dbOpts[2] = rs.getString("optionC");
                    dbOpts[3] = rs.getString("optionD");
                    dbOpts[4] = rs.getString("optionE");
                    
                    char optionChar = 'A';
                    for (int i = 0; i < numberOfOptions; i++) {
                        if (dbOpts[i] != null) {
                            opts[i] = optionChar + ". " + dbOpts[i];
                        }
                        optionChar++;
                    }
                    
                    questions.add(new Question(id, text, opts, correct, numberOfOptions));
                }
            }
            
        } catch (SQLException e) { 
            System.err.println("✗ Error loading questions: " + e.getMessage());
        }
        
        return questions;
    }
    
    public void printStudentResults(String studentName) {
        try (Connection conn = dbManager.getConnection()) {
            
            System.out.println("\n=== QUIZ HISTORY ===");
            String sql1 = "SELECT * FROM QuizScores WHERE studentName = ? ORDER BY attemptDate DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                pstmt.setString(1, studentName);
                ResultSet rs = pstmt.executeQuery();
                
                int attemptCount = 0;
                while (rs.next()) {
                    attemptCount++;
                    System.out.println("\nAttempt #" + attemptCount + " (" + rs.getTimestamp("attemptDate") + ")");
                    System.out.println("Score: " + rs.getInt("totalScore") + 
                                     "/" + rs.getInt("totalQuestions") +
                                     " (" + String.format("%.1f", rs.getDouble("percentage")) + "%)");
                }
                
                if (attemptCount == 0) {
                    System.out.println("No quiz attempts found for " + studentName);
                    return;
                }
            }
            
            System.out.println("\n=== DETAILED RESULTS (Latest Attempt) ===");
            String sql2 = "SELECT q.text, q.correctAnswer, r.selectedAnswer, q.questionType, " +
                         "CASE WHEN r.selectedAnswer = q.correctAnswer THEN '✓' ELSE '✗' END as status " +
                         "FROM QuizResults r " +
                         "JOIN Questions q ON r.questionId = q.id " +
                         "WHERE r.studentName = ? " +
                         "AND r.attemptDate = (" +
                         "    SELECT MAX(attemptDate) " +
                         "    FROM QuizResults " +
                         "    WHERE studentName = ?" +
                         ") " +
                         "ORDER BY r.id";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                pstmt.setString(1, studentName);
                pstmt.setString(2, studentName);
                ResultSet rs = pstmt.executeQuery();
                
                int questionNum = 1;
                int correctCount = 0;
                int totalQuestions = 0;
                
                while (rs.next()) {
                    totalQuestions++;
                    String status = rs.getString("status");
                    if (status.equals("✓")) correctCount++;
                    
                    System.out.println("\nQ" + questionNum + " [" + rs.getString("questionType") + "]:");
                    System.out.println(rs.getString("text"));
                    
                    String selectedAnswer = rs.getString("selectedAnswer");
                    String correctAnswer = rs.getString("correctAnswer");
                    String questionType = rs.getString("questionType");
                    
                    if (questionType.equals("TF")) {
                        selectedAnswer = selectedAnswer.equals("A") ? "True" : "False";
                        correctAnswer = correctAnswer.equals("A") ? "True" : "False";
                    }
                    
                    System.out.println("Your Answer: " + selectedAnswer);
                    System.out.println("Correct Answer: " + correctAnswer);
                    System.out.println("Result: " + status);
                    questionNum++;
                }
                
                if (totalQuestions > 0) {
                    System.out.println("\n" + "=".repeat(40));
                    System.out.println("Latest Attempt Summary:");
                    System.out.println("Correct: " + correctCount + "/" + totalQuestions);
                    System.out.println("Score: " + String.format("%.1f%%", (double) correctCount / totalQuestions * 100));
                }
            }
            
            System.out.println("\n=== OVERALL STATISTICS ===");
            String sql3 = "SELECT " +
                         "COUNT(*) as totalAttempts, " +
                         "AVG(percentage) as avgPercentage " +
                         "FROM QuizScores " +
                         "WHERE studentName = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql3)) {
                pstmt.setString(1, studentName);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    int totalAttempts = rs.getInt("totalAttempts");
                    double avgPercentage = rs.getDouble("avgPercentage");
                    
                    System.out.println("Total Attempts: " + totalAttempts);
                    if (!rs.wasNull()) {
                        System.out.println("Average Score: " + String.format("%.1f%%", avgPercentage));
                    } else {
                        System.out.println("Average Score: N/A");
                    }
                }
            }
            
        } catch (SQLException e) { 
            System.err.println("✗ Error retrieving results: " + e.getMessage());
        }
    }
    
    public void printAllStudents() {
        try (Connection conn = dbManager.getConnection()) {
            System.out.println("\n=== ALL STUDENTS ===");
            String sql = "SELECT studentName, " +
                        "COUNT(*) as totalAttempts, " +
                        "AVG(percentage) as avgPercentage " +
                        "FROM QuizScores " +
                        "GROUP BY studentName " +
                        "ORDER BY studentName";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                int count = 0;
                System.out.printf("%-5s %-20s %-15s %-15s\n", "No.", "Student Name", "Attempts", "Avg Score");
                System.out.println("-".repeat(60));
                
                while (rs.next()) {
                    count++;
                    String studentName = rs.getString("studentName");
                    int attempts = rs.getInt("totalAttempts");
                    double avgScore = rs.getDouble("avgPercentage");
                    
                    System.out.printf("%-5d %-20s %-15d %-15s\n", 
                        count, studentName, attempts, 
                        String.format("%.1f%%", avgScore));
                }
                
                if (count == 0) {
                    System.out.println("No students found in database.");
                } else {
                    System.out.println("-".repeat(60));
                    System.out.println("Total Students: " + count);
                }
            }
            
        } catch (SQLException e) { 
            System.err.println("✗ Error retrieving students: " + e.getMessage());
        }
    }
}
