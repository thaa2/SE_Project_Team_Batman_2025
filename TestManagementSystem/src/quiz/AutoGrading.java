package quiz;

import java.util.*;

import util.DataStore;

import java.io.*;

public class AutoGrading {

    public static void main(String[] args) {
        // 1. Define the Answer Key
        Map<String, String> answerKey = new HashMap<>();
        answerKey.put("Q1", "Paris");
        answerKey.put("Q2", "4");
        answerKey.put("Q3", "Java");
        answerKey.put("Q4", "Mars");

        Scanner scanner = new Scanner(System.in);
        
        // 2. Collect Student Info
        System.out.print("Enter student name: ");
        String studentName = scanner.nextLine();

        Map<String, String> studentAnswers = new HashMap<>();
        System.out.println("\n--- Start Quiz ---");

        // 3. Collect Answers
        for (String question : answerKey.keySet()) {
            System.out.print(question + ": ");
            String response = scanner.nextLine();
            studentAnswers.put(question, response);
        }

        // 4. Calculate Score
        int score = calculateScore(studentAnswers, answerKey);
        int totalQuestions = answerKey.size();

        // 5. Display and Save Results
        System.out.println("\nQuiz Complete! " + studentName + " scored " + score + "/" + totalQuestions);
        saveResult(studentName, score, totalQuestions);
        
        scanner.close();
    }

    
    // Compares answers and returns the count of correct ones.
    public static int calculateScore(Map<String, String> studentAnswers, Map<String, String> answerKey) {
        int score = 0;
        for (String question : answerKey.keySet()) {
            String studentAns = studentAnswers.get(question).trim();
            String correctAns = answerKey.get(question);

            if (studentAns.equalsIgnoreCase(correctAns)) {
                score++;
            }
        }
        return score;
    }

    
    // Saves the result to both file and database
    public static void saveResult(String name, int score, int total) {
        double percentage = ((double) score / total) * 100;
        
        // Save to text file
        try (FileWriter fw = new FileWriter("quiz_results.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            out.printf("Student: %s | Score: %d/%d (%.2f%%)%n", name, score, total, percentage);
            System.out.println("Results saved to quiz_results.txt");
            
        } catch (IOException e) {
            System.err.println("Error saving to file: " + e.getMessage());
        }
        
        // Save to database
        DataStore ds = new DataStore();
        ds.saveQuizResult(name, score, total);
    }
}