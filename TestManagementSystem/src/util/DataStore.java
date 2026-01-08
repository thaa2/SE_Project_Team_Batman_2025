package util;

// import java.beans.Statement;
// import java.lang.Thread.State;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
// import java.sql.Statement;
// import auth.*;
public class DataStore {
    static final String url = "jdbc:sqlite:C:/ITC/School.db";

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
            // Connection conn = connect();
            // Statement stmt = conn.createStatement();
            
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
    String sql = "SELECT password, role FROM user WHERE email = ?";

    try (Connection connection = DriverManager.getConnection(url);
         PreparedStatement pstmt = connection.prepareStatement(sql)) {

        pstmt.setString(1, email);
        var rs = pstmt.executeQuery();

        if (rs.next()) {
            if (rs.getString("password").equals(password)) {
                return rs.getString("role"); // SUCCESS
            }
        }

    } catch (SQLException e) {
        System.out.println("Login error: " + e.getMessage());
    }
    return null; // FAILED
}

}
