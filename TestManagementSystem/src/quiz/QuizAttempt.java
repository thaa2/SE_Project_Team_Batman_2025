package quiz;

import java.util.HashMap;
import java.util.Map;

public class QuizAttempt {

    private String studentName;
    private Quiz quiz;
    private Map<Integer, Character> answers = new HashMap<>();
    private int score;

    public QuizAttempt(String studentName, Quiz quiz) {
        this.studentName = studentName;
        this.quiz = quiz;
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


