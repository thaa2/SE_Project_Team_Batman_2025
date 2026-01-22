package quiz;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private String title;
    private List<Question> questions;

    public Quiz(String title) {
        this.title = title;
        this.questions = new ArrayList<>();
    }

    public void addQuestion(Question q) {
        questions.add(q);
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public String getTitle() {
        return title;
    }
}