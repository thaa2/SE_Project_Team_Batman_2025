package quiz;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private String title;
    private List<Question> questions = new ArrayList<>();

    public Quiz(String title) { 
        this.title = title; 
    }
  
    public void addQuestion(Question question) { 
        questions.add(question); 
    }
    
    public List<Question> getQuestions() { 
        return questions; 
    }
    
    public String getTitle() { 
        return title; 
    }
    
    public int getQuestionCount() {
        return questions.size();
    }
}