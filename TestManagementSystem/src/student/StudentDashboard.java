package student;

import java.util.Scanner;
import util.DataStore;

public class StudentDashboard {
    private Student student;
    private DataStore dataStore;

    public StudentDashboard(Student student) {
        this.student = student;
        this.dataStore = new DataStore();
    }

    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("\n=== SYSTEM (STUDENT) ===");
            System.out.println("1. View My Results");
            System.out.println("2. Attempt Quiz");
            System.out.println("0. Logout/Back");
            System.out.print("Choose: ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input.");
                sc.next();
                continue;
            }

            choice = sc.nextInt();
            sc.nextLine(); // Clear buffer

            switch (choice) {
                case 1:
                    viewResults();
                    break;
                case 2:
                    System.out.println("Attempting quiz...");
                    break;
                case 0:
                    System.out.println("Logged out.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private void viewResults() {
        dataStore.displayStudentResults(student.getName());
    }
}
