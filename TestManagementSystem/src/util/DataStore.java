package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataStore {

    static final String url = "jdbc:sqlite:C:/ITC/School.db";

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

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            pstmt.setString(4, birthDate);
            pstmt.setString(5, email);
            pstmt.setString(6, password);
            pstmt.setString(7, role);

            pstmt.executeUpdate();
            System.out.println("User inserted successfully!");

        } catch (SQLException e) {
            System.out.println("Error inserting user: " + e.getMessage());
        }
    }

    public String login(String email, String password) {
        String sql = "SELECT password, role FROM user WHERE email = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            var rs = pstmt.executeQuery();

            if (rs.next()) {
                if (rs.getString("password").equals(password)) {
                    return rs.getString("role");
                }
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return null;
    }
}
