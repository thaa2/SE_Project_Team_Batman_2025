package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
// import auth.*;
public class DataStore {
    static final String url = "jdbc:sqlite:School.db";
    
    public static Connection connect(){
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }

    public void InsertUser(String name, int age, String gender, String birthDate, String email, String password, String role) throws SQLException {
        String sql = "INSERT INTO user (name, age, gender, birthDate, email, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {

            
            Connection connection = DriverManager.getConnection(url);
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            pstmt.setString(4, birthDate);
            pstmt.setString(5, email);
            pstmt.setString(6, password);
            pstmt.setString(7, role);
            pstmt.executeUpdate();
            System.out.println("User inserted successfully!");
            connection.close();
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
                System.out.println("Login successful.");
                // 2. Returns the role found in the database (e.g., "ADMIN")
                return rs.getString("role"); 
            } else {
                System.out.println("Invalid email or password.");
                return null; // Return null or an empty string for "Failed"
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return null; // 3. IMPORTANT: You must return something here too!
        }
    }
}
