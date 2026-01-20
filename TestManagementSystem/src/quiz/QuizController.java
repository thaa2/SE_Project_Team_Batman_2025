package quiz;

import java.util.Scanner;
import auth.User;
import auth.Role;
import util.DataStore; // Import DataStore to show teacher list

public class QuizController {

    public static void runQuizModule(Scanner sc, User user) {
        try {
            // Note: If DatabaseManager.getInstance() is just for connection, 
            // ensure it doesn't conflict with DataStore.connect()
            QuizService service = new QuizService();
            QuizManager quizManager = new QuizManager(service);
            course.CourseManager courseManager = new course.CourseManager();
            DataStore ds = new DataStore(); // Added to access displayAvailableTeachers
            
            boolean exitModule = false;
            while (!exitModule) {
                System.out.println("\n=== SYSTEM (" + user.getRole() + ") ===");
                System.out.println("1. View My Results");
                System.out.println("2. Attempt Quiz");

                if (user.getRole() == Role.EDUCATOR) {
                    System.out.println("3. Create new Quiz");
                    System.out.println("4. View All Students Results");
                    System.out.println("5. Show All Questions in Bank");
                    System.out.println("6. Create a Course"); 
                    System.out.println("7. View My Courses");
                    System.out.println("8. Manage Courses (Edit/Delete)");
                }
                
                System.out.println("0. Logout/Back");
                System.out.print("Choose: ");
                
                String input = sc.nextLine();
                int choice = Integer.parseInt(input);
                
                switch (choice) {
                    case 1 -> quizManager.viewResults(sc, user.getName());
                    case 2 -> {
                        // NEW LOGIC: Ask for Teacher ID
                        ds.displayAvailableTeachers(); 
                        System.out.print("\nEnter the Teacher ID to take their quiz: ");
                        int teacherId = Integer.parseInt(sc.nextLine());
                        
                        // Pass the teacherId to your attemptQuiz method
                        quizManager.attemptQuizByTeacher(sc, teacherId, user.getName());
                    }
                    case 3 -> {
                        if (user.getRole() == Role.EDUCATOR) {
                             quizManager.addQuestions(sc, user.getUserId()); 
                        } else System.out.println("Access Denied.");
                    }
                    case 4 -> {
                        if (user.getRole() == Role.EDUCATOR) quizManager.viewAllStudentsResults(sc);
                        else System.out.println("Access Denied.");
                    }
                    case 6 -> {
                        if (user.getRole() == Role.EDUCATOR) 
                            courseManager.createCourse(sc, user.getUserId());
                        else System.out.println("Access Denied.");
                    }
                    case 7 -> {
                        if (user.getRole() == Role.EDUCATOR) 
                            courseManager.viewMyCourses(user.getUserId());
                        else System.out.println("Access Denied.");
                    }
                    case 8 -> {
                        if (user.getRole() == Role.EDUCATOR) 
                        courseManager.manageCourses(sc, user.getUserId());
                        else System.out.println("Access Denied.");
                }
                    case 0 -> exitModule = true;
                    default -> System.out.println("Invalid choice!");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}