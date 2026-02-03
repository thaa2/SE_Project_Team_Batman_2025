package quiz;

import java.util.Scanner;
import auth.User;
import auth.Role;
import gui.StudentDashboard;
import student.Student;
import gui.EducatorDashboard;
import educator.Educator;

public class QuizController {

    public static void runQuizModule(Scanner sc, User user) {
        try {
            // Launch GUI Dashboard based on user role
            if (user.getRole() == Role.STUDENT) {
                Student student = (Student) user;
                new StudentDashboard(student);
            } else if (user.getRole() == Role.EDUCATOR) {
                Educator educator = (Educator) user;
                new EducatorDashboard(educator);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}