package course;

import java.sql.*;
import java.util.Scanner;
import util.DataStore;

public class CourseManager {

    // --- CREATE ---
    public void createCourse(Scanner sc, int educatorId) {
        System.out.println("\n=== CREATE NEW COURSE ===");
        System.out.print("Enter Course Name: ");
        String courseName = sc.nextLine().trim();
        
        if (courseName.isEmpty()) {
            System.out.println("Error: Course name cannot be empty!");
            return;
        }

        System.out.println("Enter the lesson content (Type 'END' on a new line when finished):");
        StringBuilder contentBuilder = new StringBuilder();
        String line;
        while (!(line = sc.nextLine()).equalsIgnoreCase("END")) {
            contentBuilder.append(line).append("\n");
        }
        String lessonContent = contentBuilder.toString().trim();

        String sql = "INSERT INTO Courses(course_name, lesson_content, educator_id) VALUES(?, ?, ?)";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseName);
            pstmt.setString(2, lessonContent);
            pstmt.setInt(3, educatorId);
            pstmt.executeUpdate();
            System.out.println("✓ Course and Lesson created successfully!");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // --- READ (List) ---
    public void viewMyCourses(int educatorId) {
        String sql = "SELECT id, course_name FROM Courses WHERE educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educatorId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n--- YOUR REGISTERED COURSES ---");
            System.out.printf("%-5s | %-20s\n", "ID", "Course Name");
            System.out.println("---------------------------------");

            boolean found = false;
            while (rs.next()) {
                System.out.printf("%-5d | %-20s\n", rs.getInt("id"), rs.getString("course_name"));
                found = true;
            }
            if (!found) System.out.println("You have not created any courses yet.");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // --- MANAGE (The logic for Menu Choice 8) ---
    public void manageCourses(Scanner sc, int educatorId) {
        viewMyCourses(educatorId); 
        System.out.print("\nEnter Course ID to manage (or 0 to go back): ");
        
        int courseId;
        try {
            courseId = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        if (courseId == 0) return;

        System.out.println("\n--- COURSE MANAGEMENT (ID: " + courseId + ") ---");
        System.out.println("1. Read Full Lesson Content");
        System.out.println("2. Edit Name and Lesson Text");
        System.out.println("3. Delete this Course");
        System.out.println("0. Back");
        System.out.print("Choose action: ");
        
        String choice = sc.nextLine();

        switch (choice) {
            case "1" -> readLesson(courseId);
            case "2" -> editCourse(sc, courseId, educatorId); // sc passed here
            case "3" -> deleteCourse(sc, courseId, educatorId); // sc passed here
            case "0" -> { return; }
            default -> System.out.println("Invalid choice.");
        }
    }

    // --- READ (Detailed) ---
    private void readLesson(int id) {
        String sql = "SELECT course_name, lesson_content FROM Courses WHERE id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n========================================");
                System.out.println("COURSE: " + rs.getString("course_name").toUpperCase());
                System.out.println("----------------------------------------");
                System.out.println(rs.getString("lesson_content"));
                System.out.println("========================================\n");
            } else {
                System.out.println("Course ID not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error reading lesson: " + e.getMessage());
        }
    }

    // --- UPDATE ---
    private void editCourse(Scanner sc, int courseId, int educatorId) {
        System.out.print("Enter New Course Name: ");
        String newName = sc.nextLine().trim();
        
        System.out.println("Enter New Lesson Content (Type 'END' to finish):");
        StringBuilder sb = new StringBuilder();
        String line;
        while (!(line = sc.nextLine()).equalsIgnoreCase("END")) {
            sb.append(line).append("\n");
        }

        String sql = "UPDATE Courses SET course_name = ?, lesson_content = ? WHERE id = ? AND educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, sb.toString().trim());
            pstmt.setInt(3, courseId);
            pstmt.setInt(4, educatorId);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("✓ Course updated successfully!");
            else System.out.println("Error: Update failed. You may not own this course.");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // --- DELETE ---
    private void deleteCourse(Scanner sc, int courseId, int educatorId) {
        System.out.print("Are you sure you want to delete this course? (yes/no): ");
        String confirm = sc.nextLine();
        
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        String sql = "DELETE FROM Courses WHERE id = ? AND educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, educatorId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) System.out.println("🗑 Course deleted successfully!");
            else System.out.println("Error: Could not delete.");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }
public void viewEnrolledCourses(int studentId) {
    String sql = "SELECT c.course_name, c.lesson_content FROM Courses c " +
                 "JOIN Enrollments e ON c.id = e.course_id " +
                 "WHERE e.student_id = ?";
    try (Connection conn = DataStore.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, studentId);
        ResultSet rs = pstmt.executeQuery();
        
        System.out.println("\n--- Your Course Materials ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("Course: " + rs.getString("course_name"));
            System.out.println("Content: " + rs.getString("lesson_content"));
            System.out.println("-----------------------------");
        }
        if (!found) System.out.println("You are not enrolled in any courses yet.");
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
}
public void viewAllAvailableCourses() {
    // FIX: Change 'u.user_id' to 'u.uers_id' to match your DataStore table definition
    String sql = "SELECT c.id, c.course_name, u.name FROM Courses c JOIN user u ON c.educator_id = u.uers_id"; 
    try (Connection conn = util.DataStore.connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        System.out.println("\n--- Available Courses ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println(rs.getInt("id") + ". " + rs.getString("course_name") + " (Teacher: " + rs.getString("name") + ")");
        }
        if (!found) System.out.println("No courses available currently.");
        
    } catch (SQLException e) {
        System.out.println("Error loading courses: " + e.getMessage());
    }
}

public void enrollInCourse(Scanner sc, int studentId) {
    System.out.print("Enter Course ID to enroll: ");
    int courseId = Integer.parseInt(sc.nextLine());
    String sql = "INSERT INTO Enrollments (student_id, course_id) VALUES (?, ?)";
    try (Connection conn = util.DataStore.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, studentId);
        pstmt.setInt(2, courseId);
        pstmt.executeUpdate();
        System.out.println("✓ Successfully enrolled!");
    } catch (SQLException e) {
        System.out.println("Enrollment error: " + e.getMessage());
    }
}
}