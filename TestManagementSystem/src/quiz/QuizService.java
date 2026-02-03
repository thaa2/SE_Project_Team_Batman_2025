package quiz;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        
        // Save to both file and database. Include metadata (course, quiz type and educator) when available.
        saveToFile(attempt.getStudentName(), score, questions.size());
        saveToDatabase(attempt.getStudentName(), score, questions.size(), attempt.getCourseId(), attempt.getQuizType(), attempt.getEducatorId());
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

    private void saveToDatabase(String studentName, int score, int totalQuestions, Integer courseId, String quizType, Integer educatorId) {
        String sql = "INSERT INTO QuizScores (studentName, totalScore, totalQuestions, percentage, course_id, quiz_type, attemptDate, educator_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = util.DataStore.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            double percentage = (totalQuestions > 0) ? ((double) score / totalQuestions) * 100 : 0.0;
            String attemptDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            pstmt.setString(1, studentName);
            pstmt.setInt(2, score);
            pstmt.setInt(3, totalQuestions);
            pstmt.setDouble(4, percentage);
            if (courseId != null && courseId > 0) pstmt.setInt(5, courseId); else pstmt.setNull(5, java.sql.Types.INTEGER);
            pstmt.setString(6, quizType != null ? quizType : "GENERAL");
            pstmt.setString(7, attemptDate);
            if (educatorId != null && educatorId > 0) pstmt.setInt(8, educatorId); else pstmt.setNull(8, java.sql.Types.INTEGER);
            pstmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.out.println("Error saving score to database: " + e.getMessage());
        }
    }

    public List<Question> getQuestionsByTeacher(int teacherId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, text, correctAnswer, options, questionType, course_id FROM Questions WHERE educator_id = ? ORDER BY id ASC";
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
                int courseId = rs.getInt("course_id");
                
                if ("MCQ".equals(type)) {
                    String[] options = optionsStr != null ? optionsStr.split("\\|") : new String[]{"A", "B", "C"};
                    questions.add(new Question(id, text, options, correctAnswer, type, courseId));
                } else if ("TF".equals(type)) {
                    String[] tfOptions = {"True", "False"};
                    questions.add(new Question(id, text, tfOptions, correctAnswer, type, courseId));
                } else {
                    // SHORT answer or other types
                    questions.add(new Question(id, text, correctAnswer, type, courseId));
                }
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error fetching questions: " + e.getMessage());
        }
        return questions;
    }

    public List<Question> getQuestionsByTeacherAndCourse(int teacherId, int courseId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, text, correctAnswer, options, questionType, course_id FROM Questions WHERE educator_id = ? AND course_id = ? ORDER BY id ASC";
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
                int courseIdRes = rs.getInt("course_id");
                
                if ("MCQ".equals(type)) {
                    String[] options = optionsStr != null ? optionsStr.split("\\|") : new String[]{"A", "B", "C"};
                    questions.add(new Question(id, text, options, correctAnswer, type, courseIdRes));
                } else if ("TF".equals(type)) {
                    String[] tfOptions = {"True", "False"};
                    questions.add(new Question(id, text, tfOptions, correctAnswer, type, courseIdRes));
                } else {
                    // SHORT answer or other types
                    questions.add(new Question(id, text, correctAnswer, type, courseIdRes));
                }
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error fetching questions: " + e.getMessage());
        }
        return questions;
    }

    // Return only general questions (not linked to any course)
    public List<Question> getGeneralQuestionsByTeacher(int teacherId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, text, correctAnswer, options, questionType, course_id FROM Questions WHERE educator_id = ? AND (course_id IS NULL OR course_id = 0) ORDER BY id ASC";
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
                int courseId = rs.getInt("course_id");

                if ("MCQ".equals(type)) {
                    String[] options = optionsStr != null ? optionsStr.split("\\|") : new String[]{"A", "B", "C"};
                    questions.add(new Question(id, text, options, correctAnswer, type, courseId));
                } else if ("TF".equals(type)) {
                    String[] tfOptions = {"True", "False"};
                    questions.add(new Question(id, text, tfOptions, correctAnswer, type, courseId));
                } else {
                    questions.add(new Question(id, text, correctAnswer, type, courseId));
                }
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error fetching general questions: " + e.getMessage());
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
        String sql = "INSERT INTO QuizScores (studentName, totalScore, totalQuestions, percentage, course_id, quiz_type, attemptDate, educator_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DataStore.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int totalQuestions = attempt.getQuiz().getQuestions() != null ? attempt.getQuiz().getQuestions().size() : 0;
                double percentage = (totalQuestions > 0) ? ((double) attempt.getScore() / totalQuestions) * 100 : 0.0;
                String attemptDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                pstmt.setString(1, attempt.getStudentName());
                pstmt.setInt(2, attempt.getScore());
                pstmt.setInt(3, totalQuestions);
                pstmt.setDouble(4, percentage);
                if (attempt.getCourseId() > 0) pstmt.setInt(5, attempt.getCourseId()); else pstmt.setNull(5, java.sql.Types.INTEGER);
                pstmt.setString(6, attempt.getQuizType() != null ? attempt.getQuizType() : "GENERAL");
                pstmt.setString(7, attemptDate);
                if (attempt.getEducatorId() > 0) pstmt.setInt(8, attempt.getEducatorId()); else pstmt.setNull(8, java.sql.Types.INTEGER);

                pstmt.executeUpdate();

                long attemptId = -1;
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) attemptId = rs.getLong(1);
                }

                if (attemptId > 0) {
                    String sqlDetail = "INSERT INTO AttemptDetails (attempt_id, question_id, selectedAnswer) VALUES (?, ?, ?)";
                    try (PreparedStatement pdet = conn.prepareStatement(sqlDetail)) {
                        for (Question q : attempt.getQuiz().getQuestions()) {
                            Character ans = attempt.getAnswers().get(q.getId());
                            String sel = ans != null ? ans.toString() : "";
                            pdet.setLong(1, attemptId);
                            pdet.setInt(2, q.getId());
                            pdet.setString(3, sel);
                            pdet.addBatch();
                        }
                        pdet.executeBatch();
                    }
                }

                conn.commit();
                System.out.println("✓ Saved quiz attempt for " + attempt.getStudentName() + " : " + attempt.getScore() + "/" + totalQuestions + " (id=" + attemptId + ")");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving attempt: " + e.getMessage());
            e.printStackTrace();
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

    // Update an existing question. Returns true on success.
    public boolean updateQuestion(Question q, int educatorId) {
        String sql = "UPDATE Questions SET text = ?, options = ?, correctAnswer = ?, questionType = ?, course_id = ? WHERE id = ? AND educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, q.getText());
            if (q.getOptions() != null) pstmt.setString(2, String.join("|", q.getOptions())); else pstmt.setNull(2, java.sql.Types.VARCHAR);
            pstmt.setString(3, q.getCorrectAnswer());
            pstmt.setString(4, q.getQuestionType());
            if (q.getCourseId() > 0) pstmt.setInt(5, q.getCourseId()); else pstmt.setNull(5, java.sql.Types.INTEGER);
            pstmt.setInt(6, q.getId());
            pstmt.setInt(7, educatorId);

            int updated = pstmt.executeUpdate();
            if (updated > 0) System.out.println("✓ Question updated (id=" + q.getId() + ")");
            return updated > 0;
        } catch (SQLException e) {
            System.out.println("Error updating question: " + e.getMessage());
            return false;
        }
    }

    // Delete a question that belongs to the educator
    public boolean deleteQuestion(int questionId, int educatorId) {
        String sql = "DELETE FROM Questions WHERE id = ? AND educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            pstmt.setInt(2, educatorId);
            int deleted = pstmt.executeUpdate();
            if (deleted > 0) System.out.println("✓ Question deleted (id=" + questionId + ")");
            return deleted > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting question: " + e.getMessage());
            return false;
        }
    }
}