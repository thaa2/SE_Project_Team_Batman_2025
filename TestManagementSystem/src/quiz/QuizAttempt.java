package quiz;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import util.*;

public class QuizAttempt {
    private String studentName;
    private Quiz quiz;
    private Map<Integer, Character> answers = new HashMap<>();
    private int score;
    private Scanner scanner;
    private QuizService quizService;
    
    public QuizAttempt(String studentName, Quiz quiz, Scanner scanner, QuizService quizService) {
        this.studentName = studentName;
        this.quiz = quiz;
        this.scanner = scanner;
        this.quizService = quizService;
    }
    
    public void executeAttempt() {
        displayQuizHeader();
        askQuestions();
        gradeAndDisplayResults();
    }
    
    private void displayQuizHeader() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("QUIZ: " + quiz.getTitle());
        System.out.println("Student: " + studentName);
        System.out.println("Number of questions: " + quiz.getQuestions().size());
        
        int mcqCount = 0;
        int tfCount = 0;
        for (Question q : quiz.getQuestions()) {
            if (q.getQuestionType().equals("MCQ")) {
                mcqCount++;
            } else {
                tfCount++;
            }
        }
        System.out.println("MCQ Questions: " + mcqCount);
        System.out.println("True/False Questions: " + tfCount);
        System.out.println("=".repeat(50) + "\n");
    }
    
    private void askQuestions() {
        int questionNum = 1;
        for (Question q : quiz.getQuestions()) {
            displayQuestion(questionNum, q);
            char answer = getValidAnswer(q);
            answerQuestion(q.getId(), answer);
            System.out.println("-".repeat(40));
            questionNum++;
        }
    }
    
    private void displayQuestion(int questionNum, Question q) {
        System.out.println("Question " + questionNum + " [" + q.getQuestionType() + "]:");
        System.out.println(q.getText());
        
        if (q.getQuestionType().equals("TF")) {
            System.out.println("  A. True");
            System.out.println("  B. False");
        } else {
            for (String opt : q.getOptions()) {
                System.out.println("  " + opt);
            }
        }
    }
    
    private char getValidAnswer(Question q) {
        while (true) {
            if (q.getQuestionType().equals("TF")) {
                System.out.print("Your answer (A for True, B for False): ");
            } else {
                System.out.print("Your answer (" + q.getAnswerRange() + "): ");
            }
            
            String input = scanner.nextLine().toUpperCase();
            
            if (input.length() > 0) {
                char answer = input.charAt(0);
                
                if (q.getQuestionType().equals("TF")) {
                    if (answer == 'A' || answer == 'B') {
                        return answer;
                    }
                } else {
                    char maxOption;
                    switch (q.getNumberOfOptions()) {
                        case 3: maxOption = 'C'; break;
                        case 4: maxOption = 'D'; break;
                        case 5: maxOption = 'E'; break;
                        default: maxOption = 'E'; break;
                    }
                    
                    if (answer >= 'A' && answer <= maxOption) {
                        return answer;
                    }
                }
            }
            
            if (q.getQuestionType().equals("TF")) {
                System.out.println("Invalid input! Please enter A (True) or B (False).");
            } else {
                System.out.println("Invalid input! Please enter " + q.getAnswerRange());
            }
        }
    }
    
    private void gradeAndDisplayResults() {
        this.score = quizService.gradeQuiz(this);
        displayResults();
    }
    
    private void displayResults() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("QUIZ COMPLETED!");
        System.out.println("Student: " + studentName);
        System.out.println("Score: " + score + "/" + quiz.getQuestions().size());
        
        double percentage = (double) score / quiz.getQuestions().size() * 100;
        System.out.println("Percentage: " + String.format("%.1f%%", percentage));
        System.out.println("Grade: " + getGrade(percentage));
        System.out.println("=".repeat(50));
        DataStore ds = new DataStore();
        ds.saveQuizResult(studentName,score,quiz.getQuestions().size());
    }
    
    private String getGrade(double percentage) {
        if (percentage >= 90) return "A (Excellent)";
        else if (percentage >= 80) return "B (Very Good)";
        else if (percentage >= 70) return "C (Good)";
        else if (percentage >= 60) return "D (Satisfactory)";
        else return "F (Fail)";
    }
    
    public void answerQuestion(int questionId, char answer) {
        answers.put(questionId, Character.toUpperCase(answer));
    }
    
    public Map<Integer, Character> getAnswers() { return answers; }
    public Quiz getQuiz() { return quiz; }
    public String getStudentName() { return studentName; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
