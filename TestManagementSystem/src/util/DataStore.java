package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
//import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
// import auth.*;
public class DataStore {
    static final String url = "jdbc:sqlite:C:\\ITC\\DATABASE\\School.db";
    
    public static Connection connect(){
        Connection connection = null;
        try {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException cnfe) {
                System.out.println("SQLite JDBC driver not found. Add sqlite-jdbc jar to classpath (e.g., lib/sqlite-jdbc.jar)");
                return null;
            }
            connection = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }

    public void InsertUser(String name, int age, String gender, String birthDate, String email, String password, String role) {
        String sql = "INSERT INTO user (name, age, gender, birthDate, email, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection connection = connect();
        if (connection == null) {
            System.out.println("Cannot insert user: no DB connection (SQLite driver missing or connection failed).");
            return;
        }
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
    public String login(String email, String password) {
        String sql = "SELECT role FROM user WHERE email = ? AND password = ?";
        Connection conn = connect();
        if (conn == null) {
            System.out.println("Cannot perform login: no DB connection (SQLite driver missing or connection failed).");
            return null;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            var rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Login successful.");
                return rs.getString("role");
            } else {
                System.out.println("Invalid email or password.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return null;
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public String getNameByEmail(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNameByEmail'");
    }
}
