package student;

import java.util.Scanner;

public class StudentDashboard {

    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("\n=== Student Dashboard ===");
            System.out.println("1. View Courses");
            System.out.println("2. Enroll Course");
            System.out.println("3. Take Quiz");
            System.out.println("4. View Results");
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
                    System.out.println("Viewing courses...");
                    break;
                case 2:
                    System.out.println("Enroll course...");
                    break;
                case 3:
                    System.out.println("Taking quiz...");
                    break;
                case 4:
                    System.out.println("Viewing results...");
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
