package quiz;

import java.util.List;
import java.util.Scanner;

public class QuizController {

    // ===== EDUCATOR ONLY =====
    public static void addQuestions(Scanner sc) {

        Question.initDB();

        System.out.print("Do you want to add a new question? (y/n): ");
        String add = sc.nextLine();

        while (add.equalsIgnoreCase("y")) {

            System.out.print("Enter question text: ");
            String text = sc.nextLine();

            String[] options = new String[5];
            char optionChar = 'A';
            for (int i = 0; i < 5; i++) {
                System.out.print("Option " + optionChar + ": ");
                options[i] = sc.nextLine();
                optionChar++;
            }

            System.out.print("Correct answer (A-E): ");
            char correct = sc.nextLine().toUpperCase().charAt(0);

            new Question(text, options, correct).saveToDB();

            System.out.print("Add another question? (y/n): ");
            add = sc.nextLine();
        }
    }

    // ===== STUDENT ONLY =====
    public static void takeQuiz(Scanner sc) {

        QuizService service = new QuizService();
        List<Question> questions = Question.getAllQuestions();

        if (questions.isEmpty()) {
            System.out.println("No questions available!");
            return;
        }

        System.out.print("Enter student name: ");
        String studentName = sc.nextLine();

        Quiz quiz = new Quiz("Quiz from DB");
        for (Question q : questions) quiz.addQuestion(q);

        QuizAttempt attempt = new QuizAttempt(studentName, quiz);

        for (Question q : quiz.getQuestions()) {
            System.out.println("\n" + q.getText());
            for (String opt : q.getOptions()) System.out.println(opt);

            char ans;
            do {
                System.out.print("Your answer (A-E): ");
                ans = sc.nextLine().toUpperCase().charAt(0);
            } while (ans < 'A' || ans > 'E');

            attempt.answerQuestion(q.getId(), ans);
        }

        int score = service.gradeQuiz(attempt);
        System.out.println("Score: " + score + "/" + quiz.getQuestions().size());
    }
}

