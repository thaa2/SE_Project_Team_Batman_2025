package educator;

import java.util.Scanner;

public class EducatorDashboard {

    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("\n=== Educator Dashboard ===");
            System.out.println("1. Create Course");
            System.out.println("2. View My Courses");
            System.out.println("3. Create Quiz");
            System.out.println("4. View Student Results");
            System.out.println("0. Logout");
            System.out.print("Choose: ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input.");
                sc.next();
                continue;
            }

            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("Create course (educator only)");
                    break;
                case 2:
                    System.out.println("View courses");
                    break;
                case 3:
                    System.out.println("Create quiz");
                    break;
                case 4:
                    System.out.println("View results");
                    break;
                case 0:
                    System.out.println("Logged out.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }
}
