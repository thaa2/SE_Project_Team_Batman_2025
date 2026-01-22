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

    public void saveQuestion(Question question, int educatorId) {
        String sql = "INSERT INTO Questions (text, correctAnswer, options, question_type, educator_id) VALUES (?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = util.DataStore.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, question.getText());
            pstmt.setString(2, String.valueOf(question.getCorrectAnswer()));
            
            // Store options as pipe-separated string
            String[] options = question.getOptions();
            String optionsStr = String.join("|", options);
            pstmt.setString(3, optionsStr);
            
            // Determine question type
            String type = options.length == 2 && options[0].contains("True") ? "TF" : "MCQ";
            pstmt.setString(4, type);
            pstmt.setInt(5, educatorId);
            pstmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.out.println("Error saving question: " + e.getMessage());
        }
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>();
    }

    public void printAllStudents() {
        // Implementation for printing all students
    }

    public void printStudentStatistics(String studentName) {
        // Implementation for printing student statistics
    }

    public void printStudentResults(String studentName) {
        // Call DataStore to display results from database
        util.DataStore dataStore = new util.DataStore();
        dataStore.displayStudentResults(studentName);
    }
}