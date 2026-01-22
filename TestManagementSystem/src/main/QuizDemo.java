package main;

import java.util.*;
import quiz.*;
import auth.*;

public class QuizDemo {
    
    public static void main(String[] args) {
        System.out.println("\n=== QUIZ MODULE DEMONSTRATION ===\n");
        
        // Create sample questions
        Quiz sampleQuiz = new Quiz("Math Quiz");
        
        Question q1 = new Question(1, "What is 2 + 2?", new String[]{"A. 3", "B. 4", "C. 5", "D. 6"}, 'B');
        Question q2 = new Question(2, "What is the capital of France?", new String[]{"A. London", "B. Paris", "C. Berlin", "D. Madrid"}, 'B');
        Question q3 = new Question(3, "Is the Earth round?", 'A'); // True/False question
        
        sampleQuiz.addQuestion(q1);
        sampleQuiz.addQuestion(q2);
        sampleQuiz.addQuestion(q3);
        
        System.out.println("Quiz: " + sampleQuiz.getTitle());
        System.out.println("Total Questions: " + sampleQuiz.getQuestions().size());
        System.out.println("\nQuestions in the quiz:");
        
        for (Question q : sampleQuiz.getQuestions()) {
            System.out.println("\n" + q.getId() + ". " + q.getText());
            for (String opt : q.getOptions()) {
                System.out.println("   " + opt);
            }
            System.out.println("   Correct Answer: " + q.getCorrectAnswer());
        }
        
        System.out.println("\n✓ Quiz is loaded successfully and ready for students to attempt!");
        System.out.println("\nTo run the actual quiz:");
        System.out.println("1. Register as a Student");
        System.out.println("2. Choose 'Attempt Quiz' option");
        System.out.println("3. Enter your answers for each question");
        System.out.println("4. Your score will be calculated automatically");
    }
}
