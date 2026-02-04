package util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import auth.*;
import educator.*;
import quiz.*;
import student.*;

public class DataStore {

    static final String url = "jdbc:sqlite:School.db";

    public void createTables() {
        Connection conn = connect();
        if (conn == null) {
            System.out.println("Error: Database connection is null. Cannot create tables.");
            return;
        }
        
        
        // Define all SQL strings
        String sqlUser = "CREATE TABLE IF NOT EXISTS user (" +
                        "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "age INTEGER, " +
                        "gender TEXT, " +
                        "birthDate TEXT, " +
                        "email TEXT UNIQUE NOT NULL, " +
                        "password_hash TEXT NOT NULL, " +
                        "password_salt TEXT NOT NULL, " +
                        "role TEXT NOT NULL)";

        String sqlCourses = "CREATE TABLE IF NOT EXISTS Courses (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "course_name TEXT NOT NULL, " +
                        "lesson_content TEXT, " +
                        "educator_id INTEGER, " +
                        "time_limit INTEGER DEFAULT 0, " +
                        "FOREIGN KEY(educator_id) REFERENCES user(user_id))"; 

        String sqlQuestions = "CREATE TABLE IF NOT EXISTS Questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "text TEXT NOT NULL, " +
                "options TEXT, " +            
                "correctAnswer TEXT, " +       
                "questionType TEXT, " +       
                "educator_id INTEGER, " +
                "course_id INTEGER, " +
                "FOREIGN KEY(educator_id) REFERENCES user(user_id), " +
                "FOREIGN KEY(course_id) REFERENCES Courses(id))"; 

        String sqlScores = "CREATE TABLE IF NOT EXISTS QuizScores (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "studentName TEXT, " +
                        "totalScore INTEGER, " +
                        "totalQuestions INTEGER, " +
                        "percentage REAL, " +
                        "quiz_type TEXT, " +
                        "course_id INTEGER, " +
                        "educator_id INTEGER, " +
                        "attemptDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(educator_id) REFERENCES user(user_id))";
        
        // Use an INTEGER primary key for student_id (was TEXT in older schema)
        String sqlStudent = "CREATE TABLE IF NOT EXISTS student (" +
                        "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER UNIQUE, " +
                        "gpa REAL, " +
                        "major TEXT, " +
                        "FOREIGN KEY(user_id) REFERENCES user(user_id))";
        
        String sqlEnrollments = "CREATE TABLE IF NOT EXISTS Enrollments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "student_id INTEGER, " +
                        "course_id INTEGER, " +
                        "enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(student_id) REFERENCES student(student_id), " +
                        "FOREIGN KEY(course_id) REFERENCES Courses(id))";
        
        String sqlTeacher = "CREATE TABLE IF NOT EXISTS teacher (" +
                        "teacher_id TEXT PRIMARY KEY, " +
                        "user_id INTEGER UNIQUE, " +
                        "name TEXT, " +
                        "gender TEXT, " +
                        "FOREIGN KEY(user_id) REFERENCES user(user_id))";

        String sqlAnnouncements = "CREATE TABLE IF NOT EXISTS Announcements (" +
                                  "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                  "title TEXT NOT NULL, " +
                                  "content TEXT NOT NULL, " +
                                  "educator_id INTEGER NOT NULL, " +
                                  "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                  "FOREIGN KEY(educator_id) REFERENCES user(user_id))";

        String sqlThreads = "CREATE TABLE IF NOT EXISTS Threads (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "title TEXT NOT NULL, " +
                            "creator_id INTEGER NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY(creator_id) REFERENCES user(user_id))";

        String sqlMessages = "CREATE TABLE IF NOT EXISTS Messages (" +
                             "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                             "thread_id INTEGER NOT NULL, " +
                             "author_id INTEGER NOT NULL, " +
                             "content TEXT NOT NULL, " +
                             "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                             "FOREIGN KEY(thread_id) REFERENCES Threads(id), " +
                             "FOREIGN KEY(author_id) REFERENCES user(user_id))";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUser);
            stmt.execute(sqlCourses);
            stmt.execute(sqlQuestions);
            stmt.execute(sqlScores);
            stmt.execute(sqlAnnouncements);
            stmt.execute(sqlThreads);
            stmt.execute(sqlMessages);

            // Create AttemptDetails table to store per-attempt, per-question student answers
            String sqlAttemptDetails = "CREATE TABLE IF NOT EXISTS AttemptDetails (" +
                                       "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                       "attempt_id INTEGER, " +
                                       "question_id INTEGER, " +
                                       "selectedAnswer TEXT, " +
                                       "FOREIGN KEY(attempt_id) REFERENCES QuizScores(id), " +
                                       "FOREIGN KEY(question_id) REFERENCES Questions(id))";
            stmt.execute(sqlAttemptDetails);

            stmt.execute(sqlStudent);
            stmt.execute(sqlEnrollments);

            // If an older student table used TEXT student_id (e.g. "S12"), migrate to
            // an integer AUTOINCREMENT primary key to match Enrollments.student_id (INTEGER).
            try (ResultSet pico = stmt.executeQuery("PRAGMA table_info('student')")) {
                boolean needsMigration = false;
                while (pico.next()) {
                    String cname = pico.getString("name");
                    String ctype = pico.getString("type");
                    if ("student_id".equalsIgnoreCase(cname) && (ctype == null || !ctype.equalsIgnoreCase("INTEGER"))) {
                        needsMigration = true; break;
                    }
                }
                if (needsMigration) {
                    System.out.println("Migrating 'student' table to use INTEGER PK (student_id)...");
                    stmt.execute("PRAGMA foreign_keys = OFF");
                    stmt.execute("BEGIN TRANSACTION");

                    stmt.execute("CREATE TABLE IF NOT EXISTS student_new (student_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER UNIQUE, gpa REAL, major TEXT, FOREIGN KEY(user_id) REFERENCES user(user_id))");
                    stmt.execute("INSERT INTO student_new (user_id, gpa, major) SELECT user_id, gpa, major FROM student");

                    // Rebuild enrollments table and remap student ids via user_id
                    stmt.execute("CREATE TABLE IF NOT EXISTS Enrollments_new (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER, course_id INTEGER, enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(student_id) REFERENCES student(student_id), FOREIGN KEY(course_id) REFERENCES Courses(id))");
                    stmt.execute("INSERT INTO Enrollments_new (id, student_id, course_id, enrollment_date) SELECT e.id, sn.student_id, e.course_id, e.enrollment_date FROM Enrollments e JOIN student s ON e.student_id = s.student_id JOIN student_new sn ON s.user_id = sn.user_id");

                    stmt.execute("DROP TABLE Enrollments");
                    stmt.execute("DROP TABLE student");
                    stmt.execute("ALTER TABLE student_new RENAME TO student");
                    stmt.execute("ALTER TABLE Enrollments_new RENAME TO Enrollments");

                    stmt.execute("COMMIT");
                    stmt.execute("PRAGMA foreign_keys = ON");
                    System.out.println("Migration complete.");
                }
            } catch (SQLException e) {
                System.out.println("Student table migration failed: " + e.getMessage());
                e.printStackTrace();
            }

            stmt.execute(sqlTeacher);
            stmt.execute(sqlAnnouncements);
            stmt.execute(sqlThreads);
            stmt.execute(sqlMessages);
            
            // Migrate existing plaintext passwords into salted hashes if needed
            try (ResultSet cols = stmt.executeQuery("PRAGMA table_info('user')")) {
                boolean hasHash = false;
                boolean hasPlain = false;
                while (cols.next()) {
                    String colName = cols.getString("name");
                    if ("password_hash".equalsIgnoreCase(colName)) hasHash = true;
                    if ("password".equalsIgnoreCase(colName)) hasPlain = true;
                }
                if (!hasHash && hasPlain) {
                    System.out.println("Migrating plaintext passwords to hashed passwords...");
                    stmt.execute("ALTER TABLE user ADD COLUMN password_hash TEXT");
                    stmt.execute("ALTER TABLE user ADD COLUMN password_salt TEXT");
                    try (PreparedStatement select = conn.prepareStatement("SELECT user_id, password FROM user");
                         ResultSet rs2 = select.executeQuery()) {
                        while (rs2.next()) {
                            int uid = rs2.getInt("user_id");
                            String plain = rs2.getString("password");
                            if (plain != null && !plain.isEmpty()) {
                                String salt = hashingpassword.generateSalt();
                                String hash = hashingpassword.hashPassword(plain, salt);
                                try (PreparedStatement up = conn.prepareStatement("UPDATE user SET password_hash = ?, password_salt = ? WHERE user_id = ?")) {
                                    up.setString(1, hash);
                                    up.setString(2, salt);
                                    up.setInt(3, uid);
                                    up.executeUpdate();
                                }
                            }
                        }
                    }
                    // Overwrite old password column for safety
                    stmt.execute("UPDATE user SET password = NULL");
                    System.out.println("Migration completed.");
                }

                // Ensure QuizScores has educator_id column (used by various queries). Add if missing.
                try (ResultSet qsCols = stmt.executeQuery("PRAGMA table_info('QuizScores')")) {
                    boolean hasEduId = false;
                    while (qsCols.next()) {
                        String colName = qsCols.getString("name");
                        if ("educator_id".equalsIgnoreCase(colName)) { hasEduId = true; break; }
                    }
                    if (!hasEduId) {
                        System.out.println("Adding educator_id column to QuizScores table...");
                        stmt.execute("ALTER TABLE QuizScores ADD COLUMN educator_id INTEGER");
                        // No FK can be added with ALTER TABLE in SQLite easily; foreign key enforcement if present will still work for values.
                        System.out.println("Added educator_id column to QuizScores.");
                    }
                } catch (SQLException e) {
                    System.out.println("Failed to ensure educator_id column: " + e.getMessage());
                }
            } catch (SQLException e) {
                System.out.println("Password migration failed: " + e.getMessage());
            }
            
            System.out.println("All database tables checked/created successfully.");
            
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void displayAvailableTeachers() {
        String sql = "SELECT user_id, name FROM user WHERE role = 'EDUCATOR' ORDER BY user_id ASC";
        try (Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n--- Available Teachers ---");
            System.out.printf("%-5s | %-20s\n", "ID", "Name");
            while (rs.next()) {
                System.out.printf("%-5d | %-20s\n", rs.getInt("user_id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Error displaying teachers: " + e.getMessage());
            e.printStackTrace();
        }
    }
        
    public static Connection connect() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
            // Ensure foreign key enforcement is on
            try (Statement pragma = connection.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON;");
            } catch (SQLException pe) {
                System.out.println("Warning: unable to enable foreign_keys PRAGMA: " + pe.getMessage());
            }
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite Driver not found: " + e.getMessage());
            System.out.println("Make sure sqlite-jdbc.jar is in your classpath!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
            System.out.println("Check database path: " + url);
            e.printStackTrace();
        }
        return connection;
    }

    public List<Question> getQuestionsByEducator(int teacherId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM Questions WHERE educator_id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
while (rs.next()) {
    int id = rs.getInt("id");
    String text = rs.getString("text");
    String correct = rs.getString("correctAnswer"); // Get as String, not char
    String type = rs.getString("questionType");
    
    // This now matches the updated Question constructor
    questions.add(new Question(id, text, correct, type)); 
}
        } catch (SQLException e) {
            System.out.println("Error fetching questions: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    public void role(String role, String name, String gender, int id) throws SQLException {
        if (role.equalsIgnoreCase("STUDENT")) {
            // Insert a student row; student_id will be generated as an INTEGER PK
            String sql = "INSERT INTO student (user_id, gpa, major) VALUES (?, ?, ?)";
            try (Connection connection = connect();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, id);         // user_id (INTEGER FK)
                pstmt.setDouble(2, 0.0);
                pstmt.setString(3, "Undeclared");
                pstmt.executeUpdate();
                System.out.println("✓ Student saved!");
            } catch (SQLException e) {
                System.out.println("Error saving student: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } else if (role.equalsIgnoreCase("EDUCATOR")) {
            String sql = "INSERT INTO teacher (teacher_id, user_id, name, gender) VALUES (?, ?, ?, ?)";
            try (Connection connection = connect();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
                String t_id = "T" + id;
                
                pstmt.setString(1, t_id);
                pstmt.setInt(2, id);
                pstmt.setString(3, name);
                pstmt.setString(4, gender);
                pstmt.executeUpdate();
                System.out.println("✓ Educator saved!");
            } catch (SQLException e) {
                System.out.println("Error saving educator: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
    }

    public int InsertUser(String name, int age, String gender, String birthDate, String email, String password, String role) {
        String sql = "INSERT INTO user (name, age, gender, birthDate, email, password_hash, password_salt, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = connect();
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Generate salt and hash for the incoming plaintext password
            String salt = hashingpassword.generateSalt();
            String hash = hashingpassword.hashPassword(password, salt);

            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            pstmt.setString(4, birthDate);
            pstmt.setString(5, email);
            pstmt.setString(6, hash);
            pstmt.setString(7, salt);
            pstmt.setString(8, role);
            
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                System.out.println("Error: no rows inserted for user.");
                return -1;
            }
            System.out.println("✓ User inserted successfully!");

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1); 
                    System.out.println("✓ Generated User ID: " + newId);
                    
                    try {
                        role(role, name, gender, newId);
                    } catch (SQLException re) {
                        System.out.println("[ERROR] Role insertion failed for user id " + newId + ": " + re.getMessage());
                        // Attempt to remove the inserted user to keep DB consistent
                        try (PreparedStatement del = connection.prepareStatement("DELETE FROM user WHERE user_id = ?")) {
                            del.setInt(1, newId);
                            del.executeUpdate();
                            System.out.println("[INFO] Rolled back inserted user id " + newId);
                        } catch (SQLException de) {
                            System.out.println("[ERROR] Failed to delete user after role insert failure: " + de.getMessage());
                            de.printStackTrace();
                        }
                        // Rethrow so outer catch handles it (gives -1 or -2 depending)
                        throw re;
                    }
                    return newId;
                }
            }
        } catch (SQLException e) {
            String msg = e.getMessage();
            System.out.println("Error inserting user: " + msg);
            if (msg != null && msg.contains("UNIQUE constraint failed")) {
                System.out.println("[DEBUG] Detected UNIQUE constraint during InsertUser. Returning -2.");
                e.printStackTrace();
                return -2; // indicate duplicate at DB level
            }
            e.printStackTrace();
        }
        return -1;
    }

    public User getAuthenticatedUser(String email, String password) {
        String sql = "SELECT * FROM user WHERE email = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("user_id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String gender = rs.getString("gender");
                String birthDate = rs.getString("birthDate");
                String roleStr = rs.getString("role");
                String storedHash = null;
                String storedSalt = null;
                try {
                    storedHash = rs.getString("password_hash");
                    storedSalt = rs.getString("password_salt");
                } catch (SQLException e) {
                    // older DB may have only 'password' column
                    storedHash = rs.getString("password");
                    storedSalt = null;
                }

                // Verify password
                boolean verified = false;
                if (storedSalt != null && storedHash != null) {
                    verified = hashingpassword.verifyPassword(password, storedSalt, storedHash);
                } else if (storedHash != null) {
                    // If only stored hash present without salt (unlikely), compare directly
                    verified = storedHash.equals(password);
                }

                if (!verified) {
                    System.out.println("Invalid credentials for email: " + email);
                    return null;
                }

                // Accept common variants for educator role (EDUCATOR, Teacher, etc.)
                if (roleStr != null && (roleStr.equalsIgnoreCase("EDUCATOR") || roleStr.equalsIgnoreCase("TEACHER") || roleStr.toUpperCase().contains("EDU"))) {
                    return new Educator(id, name, age, gender, birthDate, email, storedHash != null ? storedHash : "");
                } else if (roleStr != null && roleStr.equalsIgnoreCase("STUDENT")) {
                    return new Student(id, name, age, gender, birthDate, email, storedHash != null ? storedHash : "");
                } else {
                    // Unknown role stored in DB — log and return null so login fails with clear console info
                    System.out.println("Unknown role stored for user " + email + ": '" + roleStr + "'");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Login database error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void printEducatorList() {
        String sql = "SELECT user_id, name FROM user WHERE role = 'EDUCATOR'";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n--- Available Educators ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("user_id") + " | Name: " + rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Error printing educators: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Check if an email is already registered (case-insensitive)
    public boolean isEmailTaken(String email) {
        String sql = "SELECT COUNT(*) AS cnt FROM user WHERE lower(email) = lower(?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int cnt = rs.getInt("cnt");
                System.out.println("[DEBUG] isEmailTaken('" + email + "') -> " + cnt);
                return cnt > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking email existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

public void insertQuestion(Question question, int educatorId, int courseId, String type) {
    String sql = "INSERT INTO Questions (text, options, correctAnswer, questionType, educator_id, course_id) VALUES (?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, question.getText());
        
        // If it's a short answer, options are null. 
        // For MCQ, you'd join your options array into a single string here.
        pstmt.setString(2, (question.getOptions() != null) ? String.join("|", question.getOptions()) : null);
        
        pstmt.setString(3, question.getCorrectAnswerString()); // Ensure Question class has this
        pstmt.setString(4, type); 
        pstmt.setInt(5, educatorId);
        pstmt.setInt(6, courseId);
        
        pstmt.executeUpdate();
        System.out.println("✓ " + type + " Question saved!");
    } catch (SQLException e) {
        System.out.println("Error saving question: " + e.getMessage());
    }
}

    public void saveQuizResult(String studentName, int totalScore, int totalQuestions) {
        String sql = "INSERT INTO QuizScores (studentName, totalScore, totalQuestions, percentage) VALUES (?, ?, ?, ?)";
        
        double percentage = (totalQuestions > 0) ? ((double) totalScore / totalQuestions) * 100 : 0;
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentName);
            pstmt.setInt(2, totalScore);
            pstmt.setInt(3, totalQuestions);
            pstmt.setDouble(4, percentage);
            
            pstmt.executeUpdate();
            System.out.println("✓ Quiz result saved to database!");
            
        } catch (SQLException e) {
            System.out.println("Error saving quiz result: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void displayStudentResults(String studentName) {
        String sql = "SELECT id, studentName, totalScore, totalQuestions, percentage, attemptDate FROM QuizScores WHERE studentName = ? ORDER BY attemptDate DESC";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentName);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("\n========== Quiz Results for " + studentName + " ==========");
            System.out.printf("%-5s | %-12s | %-10s | %-8s | %-20s\n", 
                              "ID", "Score", "Total Qs", "%", "Attempt Date");
            System.out.println("--------------------------------------------------------------");
            
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                int id = rs.getInt("id");
                int score = rs.getInt("totalScore");
                int total = rs.getInt("totalQuestions");
                double percentage = rs.getDouble("percentage");
                String date = rs.getString("attemptDate");
                
                System.out.printf("%-5d | %-12s | %-10d | %6.2f%% | %-20s\n", 
                                  id, score + "/" + total, total, percentage, date);
            }
            
            if (!hasResults) {
                System.out.println("No results found for this student.");
            }
            System.out.println("=========================================================");
            
        } catch (SQLException e) {
            System.out.println("Error retrieving results: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ----------------- Forum related database methods -----------------

    public int createAnnouncement(String title, String content, int educatorId) {
        String sql = "INSERT INTO Announcements (title, content, educator_id) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setInt(3, educatorId);
            System.out.println("[DEBUG] createAnnouncement: title='" + title + "' educatorId=" + educatorId);
            int affected = pstmt.executeUpdate();
            System.out.println("[DEBUG] createAnnouncement: rowsAffected=" + affected);
            if (affected == 0) {
                System.out.println("Error creating announcement: no rows affected.");
                return -1;
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("[DEBUG] createAnnouncement: generatedKey=" + id);
                    return id;
                }
            }
            try (Statement stmt2 = conn.createStatement();
                 ResultSet rs2 = stmt2.executeQuery("SELECT last_insert_rowid()")) {
                if (rs2.next()) {
                    int id2 = rs2.getInt(1);
                    System.out.println("[DEBUG] createAnnouncement: fallback last_insert_rowid=" + id2);
                    return id2;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating announcement: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
    public void editAnnouncement(int id, String title, String content, int educatorId) {
        String sql = "UPDATE Announcements SET title = ?, content = ? WHERE id = ? AND educator_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setInt(3, id);
            pstmt.setInt(4, educatorId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error editing announcement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void deleteAnnouncement(int id, int educatorId) {
        String sql = "DELETE FROM Announcements WHERE id = ? AND educator_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, educatorId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting announcement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public java.util.List<forum.Announcement> getAnnouncements() {
        java.util.List<forum.Announcement> list = new java.util.ArrayList<>();
        String sql = "SELECT A.id, A.title, A.content, A.educator_id, A.created_at, U.name as educator_name FROM Announcements A JOIN user U ON A.educator_id = U.user_id ORDER BY A.created_at DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new forum.Announcement(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getInt("educator_id"),
                    rs.getString("created_at"),
                    rs.getString("educator_name")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching announcements: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public int createThread(String title, int creatorId) {
        String sql = "INSERT INTO Threads (title, creator_id) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setInt(2, creatorId);
            System.out.println("[DEBUG] createThread: title='" + title + "' creatorId=" + creatorId);
            int affected = pstmt.executeUpdate();
            System.out.println("[DEBUG] createThread: rowsAffected=" + affected);
            if (affected == 0) {
                System.out.println("Error creating thread: no rows affected.");
                return -1;
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("[DEBUG] createThread: generatedKey=" + id);
                    return id;
                }
            }
            // Fallback for sqlite JDBC drivers that do not return generated keys
            try (Statement stmt2 = conn.createStatement();
                 ResultSet rs2 = stmt2.executeQuery("SELECT last_insert_rowid()")) {
                if (rs2.next()) {
                    int id2 = rs2.getInt(1);
                    System.out.println("[DEBUG] createThread: fallback last_insert_rowid=" + id2);
                    return id2;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating thread: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public java.util.List<forum.ThreadModel> getThreads() {
        java.util.List<forum.ThreadModel> list = new java.util.ArrayList<>();
        String sql = "SELECT id, title, creator_id, created_at FROM Threads ORDER BY created_at DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new forum.ThreadModel(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("creator_id"),
                    rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching threads: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public int addMessage(int threadId, int authorId, String content) {
        String sql = "INSERT INTO Messages (thread_id, author_id, content) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, threadId);
            pstmt.setInt(2, authorId);
            pstmt.setString(3, content);
            System.out.println("[DEBUG] addMessage: threadId=" + threadId + " authorId=" + authorId + " content='" + (content.length()>50?content.substring(0,50)+"...":content) + "'");
            int affected = pstmt.executeUpdate();
            System.out.println("[DEBUG] addMessage: rowsAffected=" + affected);
            if (affected == 0) {
                System.out.println("Error adding message: no rows affected.");
                return -1;
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("[DEBUG] addMessage: generatedKey=" + id);
                    return id;
                }
            }
            try (Statement stmt2 = conn.createStatement();
                 ResultSet rs2 = stmt2.executeQuery("SELECT last_insert_rowid()")) {
                if (rs2.next()) {
                    int id2 = rs2.getInt(1);
                    System.out.println("[DEBUG] addMessage: fallback last_insert_rowid=" + id2);
                    return id2;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error adding message: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public java.util.List<forum.Message> getMessagesByThread(int threadId) {
        java.util.List<forum.Message> list = new java.util.ArrayList<>();
        String sql = "SELECT M.id, M.thread_id, M.author_id, M.content, M.created_at, U.name as author_name FROM Messages M JOIN user U ON M.author_id = U.user_id WHERE M.thread_id = ? ORDER BY M.created_at ASC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, threadId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new forum.Message(
                    rs.getInt("id"),
                    rs.getInt("thread_id"),
                    rs.getInt("author_id"),
                    rs.getString("content"),
                    rs.getString("created_at"),
                    rs.getString("author_name")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching messages: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public String getUserNameById(int userId) {
        String sql = "SELECT name FROM user WHERE user_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("name");
        } catch (SQLException e) {
            System.out.println("Error fetching user name: " + e.getMessage());
            e.printStackTrace();
        }
        return "Unknown";
    }

    /**
     * Returns the numeric student_id (student.student_id) for the given user_id,
     * or -1 if no student record exists.
     */
    public int getStudentDbIdByUserId(int userId) {
        String sql = "SELECT student_id FROM student WHERE user_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("student_id");
        } catch (SQLException e) {
            System.out.println("Error fetching student id: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Returns a display identifier for the student (e.g., "S12"). If no
     * student record exists, falls back to "S" + userId to provide a stable label.
     */
    public String getStudentIdentifierByUserId(int userId) {
        int sid = getStudentDbIdByUserId(userId);
        if (sid > 0) return "S" + sid;
        return "S" + userId;
    }

    /**
     * Returns the teacher identifier stored in `teacher.teacher_id` (e.g., "T3").
     * If no teacher row exists, falls back to "T" + userId.
     */
    public String getTeacherIdentifierByUserId(int userId) {
        String sql = "SELECT teacher_id FROM teacher WHERE user_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String t = rs.getString("teacher_id");
                if (t != null && !t.trim().isEmpty()) return t;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching teacher id: " + e.getMessage());
            e.printStackTrace();
        }
        return "T" + userId;
    }
}