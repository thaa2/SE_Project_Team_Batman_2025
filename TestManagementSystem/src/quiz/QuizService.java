package quiz;

import java.io.*;
import java.util.*;
import quiz.QuizAttempt;
import quiz.Question;
import quiz.Quiz;

public class QuizService {

    public int processGrading(QuizAttempt attempt) {
        int score = 0;
        List<Question> questions = attempt.getQuiz().getQuestions();
        Map<Integer, Character> studentAnswers = attempt.getAnswers();

        // Auto-Grading Logic
        for (Question q : questions) {
            Character studentChoice = studentAnswers.get(q.getId());
            
            // Compare student input to the Question's answer key
            if (studentChoice != null && studentChoice == q.getCorrectAnswer()) {
                score++;
            }
        }
        
        // Save to both file and database
        saveToFile(attempt.getStudentName(), score, questions.size());
        saveToDatabase(attempt.getStudentName(), score, questions.size());
        return score;
    }

    private void saveToFile(String name, int score, int total) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("quiz_results.txt", true)))) {
            double percent = ((double) score / total) * 100;
            out.printf("Student: %s | Score: %d/%d (%.2f%%) | Date: %s%n", 
                        name, score, total, percent, new java.util.Date());
        } catch (IOException e) {
            System.err.println("Error saving results: " + e.getMessage());
        }
    }

    private void saveToDatabase(String studentName, int score, int totalQuestions) {
        String sql = "INSERT INTO QuizScores (studentName, totalScore, totalQuestions, percentage) VALUES (?, ?, ?, ?)";
        try (java.sql.Connection conn = util.DataStore.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            double percentage = ((double) score / totalQuestions) * 100;
            pstmt.setString(1, studentName);
            pstmt.setInt(2, score);
            pstmt.setInt(3, totalQuestions);
            pstmt.setDouble(4, percentage);
            pstmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.out.println("Error saving score to database: " + e.getMessage());
        }
    }

    public List<Question> getQuestionsByTeacher(int teacherId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, text, correctAnswer, options, question_type FROM Questions WHERE educator_id = ?";
        try (java.sql.Connection conn = util.DataStore.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                char correctAnswer = rs.getString("correctAnswer").charAt(0);
                String type = rs.getString("question_type");
                
                if ("MCQ".equals(type)) {
                    String optionsStr = rs.getString("options");
                    String[] options = optionsStr != null ? optionsStr.split("\\|") : new String[]{"A. True", "B. False"};
                    questions.add(new Question(id, text, options, correctAnswer));
                } else {
                    questions.add(new Question(id, text, correctAnswer));
                }
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error fetching questions: " + e.getMessage());
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
                String sql1 = "INSERT INTO QuizScores (studentName, questionId, selectedAnswer) VALUES (?, ?, ?)";
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
        // Implementation for printing student statistics
    }

    public void printStudentResults(String studentName) {
        // Call DataStore to display results from database
        util.DataStore dataStore = new util.DataStore();
        dataStore.displayStudentResults(studentName);
    }
}