package quiz;

import java.util.*;
// import quiz.Quiz;
// import quiz.Question;
// import quiz.QuizService;

public class QuizAttempt {
    private String studentName;
    private Quiz quiz; 
    private Map<Integer, Character> studentAnswers = new HashMap<>();
    private Scanner scanner;
    private QuizService service;

    public QuizAttempt(String studentName, Quiz quiz, Scanner scanner, QuizService service) {
        this.studentName = studentName;
        this.quiz = quiz;
        this.scanner = scanner;
        this.service = service;
    }

    public void run() {
        System.out.println("\n--- Starting Quiz: " + quiz.getTitle() + " ---");

        for (Question q : quiz.getQuestions()) {
            System.out.println("\n" + q.getText());
            String[] options = q.getOptions();
            for (int i = 0; i < options.length; i++) {
                String opt = options[i];
                // If option already has letter prefix, use as-is; otherwise add it
                if (opt.matches("^[A-Z]\\..*")) {
                    System.out.println("  " + opt);
                } else {
                    char letter = (char) ('A' + i);
                    System.out.println("  " + letter + ". " + opt);
                }
            }
            
            System.out.print("Your Answer: ");
            String input = scanner.nextLine().trim().toUpperCase();
            
            if (!input.isEmpty()) {
                // Store answer using the Question ID as the key
                studentAnswers.put(q.getId(), input.charAt(0));
            }
        }

        // Invoke Auto-Grading
        int finalScore = service.processGrading(this);
        
        System.out.println("\n" + "=".repeat(30));
        System.out.println("QUIZ COMPLETE: " + studentName);
        System.out.println("Final Score: " + finalScore + "/" + quiz.getQuestions().size());
        System.out.println("=".repeat(30));
    }

    public void executeAttempt() {
        run();
    }

    public void setScore(int score) {
        // This is handled by QuizService
    }

    public int getScore() {
        return 0; // Will be set by QuizService after grading
    }

    public Map<Integer, Character> getAnswers() { return studentAnswers; }
    public Quiz getQuiz() { return quiz; }
    public String getStudentName() { return studentName; }
}