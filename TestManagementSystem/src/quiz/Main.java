package quiz;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // ===== Initialize DB for questions =====
        Question.initDB();
        QuizService service = new QuizService();

        // ===== Educator adds questions =====
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

        // ===== Student takes quiz =====
        List<Question> questions = Question.getAllQuestions();
        if (questions.isEmpty()) {
            System.out.println("No questions available!");
            return;
        }

        System.out.print("\nEnter student name: ");
        String studentName = sc.nextLine();

        Quiz quiz = new Quiz("Quiz from DB");
        for (Question q : questions) quiz.addQuestion(q);

        QuizAttempt attempt = new QuizAttempt(studentName, quiz);

        System.out.println("\n--- " + quiz.getTitle() + " ---");

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

        // ===== Grade & save attempt =====
        int score = service.gradeQuiz(attempt);

        System.out.println("\nStudent: " + studentName);
        System.out.println("Score: " + score + "/" + quiz.getQuestions().size());

        // ===== Optional: display student's results from DB =====
        System.out.print("\nDo you want to see the detailed results? (y/n): ");
        String view = sc.nextLine();
        if (view.equalsIgnoreCase("y")) {
            service.printStudentResults(studentName);
        }

        sc.close();
    }
}

