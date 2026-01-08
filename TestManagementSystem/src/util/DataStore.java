package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataStore {

    static final String url = "jdbc:sqlite:School.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC Driver loaded");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver NOT found");
        }
    }

    public static Connection connect() {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void InsertUser(String name, int age, String gender, String birthDate,
                       String email, String password, String role) {

    String sql = "INSERT INTO user (name, age, gender, birthDate, email, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = connect()) {
        // Add this check!
        if (conn == null) {
            System.out.println("Database connection failed. User not saved.");
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            // ... (the rest of your setString calls)
            pstmt.executeUpdate();
            System.out.println("User inserted successfully!");
        }

    } catch (SQLException e) {
        System.out.println("Error inserting user: " + e.getMessage());
    }
}

public String login(String email, String password) {
    String sql = "SELECT role FROM user WHERE email = ? AND password = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, email);
        pstmt.setString(2, password);
        var rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getString("role"); // Returns "STUDENT" or "EDUCATOR"
        }
    } catch (SQLException e) {
        System.out.println("Login error: " + e.getMessage());
    }
    return null;
}
}
