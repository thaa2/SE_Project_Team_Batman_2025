package quiz;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import util.*;


public class QuizService {

    public int processGrading(QuizAttempt attempt) {
        int score = 0;
        List<Question> questions = attempt.getQuiz().getQuestions();
        Map<Integer, Character> studentAnswers = attempt.getAnswers();

        // Auto-Grading Logic
        for (Question q : questions) {
            Character studentChoice = studentAnswers.get(q.getId());
            String correctAnswer = q.getCorrectAnswer();
            
            // Compare student input to the Question's answer key
            if (studentChoice != null && studentChoice.toString().equals(correctAnswer)) {
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
        String sql = "SELECT id, text, correctAnswer, options, questionType FROM Questions WHERE educator_id = ? ORDER BY id ASC";
        try (java.sql.Connection conn = util.DataStore.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String correctAnswer = rs.getString("correctAnswer");
                String type = rs.getString("questionType");
                String optionsStr = rs.getString("options");
                
                if ("MCQ".equals(type)) {
                    String[] options = optionsStr != null ? optionsStr.split("\\|") : new String[]{"A", "B", "C"};
                    questions.add(new Question(id, text, options, correctAnswer, type));
                } else if ("TF".equals(type)) {
                    String[] tfOptions = {"True", "False"};
                    questions.add(new Question(id, text, tfOptions, correctAnswer, type));
                } else {
                    // SHORT answer or other types
                    questions.add(new Question(id, text, correctAnswer, type));
                }
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error fetching questions: " + e.getMessage());
        }
        return questions;
    }

    public List<Question> getQuestionsByTeacherAndCourse(int teacherId, int courseId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, text, correctAnswer, options, questionType FROM Questions WHERE educator_id = ? AND course_id = ? ORDER BY id ASC";
        try (java.sql.Connection conn = util.DataStore.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            pstmt.setInt(2, courseId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String correctAnswer = rs.getString("correctAnswer");
                String type = rs.getString("questionType");
                String optionsStr = rs.getString("options");
                
                if ("MCQ".equals(type)) {
                    String[] options = optionsStr != null ? optionsStr.split("\\|") : new String[]{"A", "B", "C"};
                    questions.add(new Question(id, text, options, correctAnswer, type));
                } else if ("TF".equals(type)) {
                    String[] tfOptions = {"True", "False"};
                    questions.add(new Question(id, text, tfOptions, correctAnswer, type));
                } else {
                    // SHORT answer or other types
                    questions.add(new Question(id, text, correctAnswer, type));
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
            String correctAnswer = q.getCorrectAnswer();
            if (studentAnswer != null && studentAnswer.toString().equals(correctAnswer)) {
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

    public void printAllStudents() {
        throw new UnsupportedOperationException("Unimplemented method 'printAllStudents'");
    }

    public void saveQuestion(Question question, int educatorId, int courseId) {
        String type = question.getQuestionType();
        String sql = "INSERT INTO Questions (text, options, correctAnswer, questionType, educator_id, course_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, question.getText());
            
            // Handle options based on question type
            String[] options = question.getOptions();
            String optionsStr = null;
            
            if ("MCQ".equals(type) && options != null) {
                optionsStr = String.join("|", options);
            } else if ("TF".equals(type)) {
                optionsStr = "True|False";
            }
            // For SHORT answer type, options remain null
            
            if (optionsStr != null) {
                pstmt.setString(2, optionsStr);
            } else {
                pstmt.setNull(2, java.sql.Types.VARCHAR);
            }
            
            pstmt.setString(3, question.getCorrectAnswer());
            pstmt.setString(4, type);
            pstmt.setInt(5, educatorId);
            if (courseId > 0) {
                pstmt.setInt(6, courseId);
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            pstmt.executeUpdate();
            if (courseId > 0) {
                System.out.println("✓ " + type + " question added to course successfully!");
            } else {
                System.out.println("✓ " + type + " question saved successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Error saving question: " + e.getMessage());
        }
    }

    public void saveQuestion(Question question, int educatorId) {
        saveQuestion(question, educatorId, 0);
    }

    public Object getAllQuestions() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getAllQuestions'");
    }
}