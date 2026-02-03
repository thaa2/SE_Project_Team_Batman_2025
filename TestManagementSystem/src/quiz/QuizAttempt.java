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
    private int score = 0; // store computed score

    // metadata to persist
    private int courseId = 0; // 0 = general / none
    private String quizType = "GENERAL"; // or "COURSE"
    private int educatorId = 0; // optional: which educator provided the quiz (for GENERAL quizzes)

    // Backwards-compatible constructor with educatorId
    public QuizAttempt(String studentName, Quiz quiz, QuizService service, int courseId, String quizType, int educatorId) {
        this(studentName, quiz, service, courseId, quizType);
        this.educatorId = educatorId;
    }

    public int getEducatorId() { return educatorId; }
    public void setEducatorId(int id) { this.educatorId = id; }
    public QuizAttempt(String studentName, Quiz quiz, Scanner scanner, QuizService service) {
        this.studentName = studentName;
        this.quiz = quiz;
        this.scanner = scanner;
        this.service = service;
    }

    // Overloaded constructor for GUI usage (no Scanner required)
    public QuizAttempt(String studentName, Quiz quiz, QuizService service) {
        this(studentName, quiz, null, service);
    }

    // Overloaded constructor with metadata
    public QuizAttempt(String studentName, Quiz quiz, QuizService service, int courseId, String quizType) {
        this(studentName, quiz, null, service);
        this.courseId = courseId;
        this.quizType = quizType;
    }

    public int getCourseId() { return courseId; }
    public String getQuizType() { return quizType; }
    public void setCourseId(int id) { this.courseId = id; }
    public void setQuizType(String t) { this.quizType = t; }

    public void run() {
        if (scanner == null) {
            // No console mode - GUI should use getters/setters and call grading directly
            return;
        }

        System.out.println("\n--- Starting Quiz: " + quiz.getTitle() + " ---");

        for (Question q : quiz.getQuestions()) {
            System.out.println("\n" + q.getText());
            String[] options = q.getOptions();
            
            // Handle different question types
            if (options != null && options.length > 0) {
                // MCQ or True/False questions with options
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
            }
            // For SHORT answer questions, just prompt for input without options
            
            System.out.print("Your Answer: ");
            String input = scanner.nextLine().trim();
            
            if (!input.isEmpty()) {
                // Store answer using the Question ID as the key
                // For MCQ/TF, convert to uppercase; for SHORT, keep as-is for comparison
                if (q.getQuestionType().equals("SHORT")) {
                    studentAnswers.put(q.getId(), input.charAt(0));
                } else {
                    studentAnswers.put(q.getId(), input.toUpperCase().charAt(0));
                }
            }
        }

        // Invoke Auto-Grading
        int finalScore = service.processGrading(this);
        this.setScore(finalScore);
        
        System.out.println("\n" + "=".repeat(30));
        System.out.println("QUIZ COMPLETE: " + studentName);
        System.out.println("Final Score: " + finalScore + "/" + quiz.getQuestions().size());
        System.out.println("=".repeat(30));
    }

    public void executeAttempt() {
        run();
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return this.score;
    }

    public Map<Integer, Character> getAnswers() { return studentAnswers; }
    public Quiz getQuiz() { return quiz; }
    public String getStudentName() { return studentName; }
}