package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import util.DataStore;
import student.Student;

public class StudentDashboard extends JFrame {
    private Student student;
    private DataStore dataStore;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private quiz.QuizService quizService;

    // Results table model & table for "My Results" panel
    private javax.swing.table.DefaultTableModel resultsTableModel;
    private javax.swing.JTable resultsTable;

    public StudentDashboard(Student student) {
        this.student = student;
        this.dataStore = new DataStore();
        this.quizService = new quiz.QuizService();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Student Dashboard - Test Management System");
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("logo.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Logo file not found, continue without icon
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        // Main container
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(240, 242, 245));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        container.add(headerPanel, BorderLayout.NORTH);

        // Sidebar and Content
        JPanel bodyPanel = new JPanel(new BorderLayout());
        JPanel sidebarPanel = createSidebarPanel();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(240, 242, 245));

        // Add different panels to CardLayout
        mainPanel.add(createDashboardPanel(), "dashboard");
        mainPanel.add(createResultsPanel(), "results");
        mainPanel.add(createAttemptPanel(), "attempt");
        mainPanel.add(createCoursesPanel(), "courses");
        mainPanel.add(createForumPanel(), "forum");
        mainPanel.add(createProfilePanel(), "profile");

        bodyPanel.add(sidebarPanel, BorderLayout.WEST);
        bodyPanel.add(mainPanel, BorderLayout.CENTER);

        container.add(bodyPanel, BorderLayout.CENTER);
        setContentPane(container);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                Color color1 = new Color(67, 97, 238);
                Color color2 = new Color(103, 58, 183);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JLabel titleLabel = new JLabel("Student Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // JLabel userLabel = new JLabel("Welcome, " + student.getName());
        // userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // userLabel.setForeground(new Color(255, 255, 255, 200));
        // headerPanel.add(userLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        String[] menuItems = {"Dashboard", "My Results", "Attempt Quiz", "Courses", "Forum", "Profile"};
        String[] panelNames = {"dashboard", "results", "attempt", "courses", "forum", "profile"};

        for (int i = 0; i < menuItems.length; i++) {
            JButton button = createSidebarButton(menuItems[i], panelNames[i]);
            sidebarPanel.add(button);
            if (i < menuItems.length - 1) {
                sidebarPanel.add(Box.createVerticalStrut(10));
            }
        }

        sidebarPanel.add(Box.createVerticalGlue());

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        logoutButton.setOpaque(true);
        logoutButton.setContentAreaFilled(true);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                new LoginGUI().setVisible(true);
            }
        });
        sidebarPanel.add(logoutButton);

        return sidebarPanel;
    }

    private JButton createSidebarButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBackground(new Color(240, 242, 245));
        button.setForeground(new Color(60, 60, 60));
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(220, 230, 250));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(240, 242, 245));
            }
        });
        button.addActionListener(e -> {
            // Ensure "My Results" is refreshed each time the panel is opened
            if ("results".equals(panelName) && resultsTableModel != null) {
                loadStudentResults();
            }
            cardLayout.show(mainPanel, panelName);
        });
        return button;
    }

    private JComponent createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel welcomeLabel = new JLabel("Welcome to Your Dashboard, " + student.getName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(60, 60, 60));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(welcomeLabel);

        panel.add(Box.createVerticalStrut(20));

        // Statistics cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(new Color(240, 242, 245));
        statsPanel.add(createStatCard("Quizzes Attempted", "0", new Color(67, 97, 238)));
        statsPanel.add(createStatCard("Average Score", "0%", new Color(103, 58, 183)));
        statsPanel.add(createStatCard("Courses Enrolled", "0", new Color(76, 175, 80)));
        panel.add(statsPanel);

        panel.add(Box.createVerticalStrut(30));

        // Quick actions
        JLabel actionsLabel = new JLabel("Quick Actions");
        actionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        actionsLabel.setForeground(new Color(60, 60, 60));
        actionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(actionsLabel);

        panel.add(Box.createVerticalStrut(15));

        JPanel actionsWrapperPanel = new JPanel();
        actionsWrapperPanel.setBackground(new Color(240, 242, 245));
        actionsWrapperPanel.setLayout(new BoxLayout(actionsWrapperPanel, BoxLayout.X_AXIS));
        
        JPanel actionsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        actionsPanel.setBackground(new Color(240, 242, 245));
        actionsPanel.setMaximumSize(new Dimension(900, 200));
        actionsPanel.add(createActionCard("Attempt Quiz", "Take a new quiz", e -> cardLayout.show(mainPanel, "attempt")));
        actionsPanel.add(createActionCard("View Results", "Check your scores", e -> { loadStudentResults(); cardLayout.show(mainPanel, "results"); }));
        actionsPanel.add(createActionCard("Browse Courses", "Enroll in courses", e -> cardLayout.show(mainPanel, "courses")));
        
        actionsWrapperPanel.add(Box.createHorizontalGlue());
        actionsWrapperPanel.add(actionsPanel);
        actionsWrapperPanel.add(Box.createHorizontalGlue());
        
        panel.add(actionsWrapperPanel);

        panel.add(Box.createVerticalGlue());

        return new JScrollPane(panel);
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(150, 150, 150));
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(10));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);
        card.add(valueLabel);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        return card;
    }

    private JPanel createActionCard(String title, String description, ActionListener action) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(60, 60, 60));
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(150, 150, 150));
        card.add(descLabel);

        card.add(Box.createVerticalGlue());

        JButton button = new JButton("Go");
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(67, 97, 238));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addActionListener(action);
        card.add(button);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        return card;
    }

    private JComponent createResultsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Your Quiz Results");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        // Results table (first hidden column stores attempt id)
        String[] columnNames = {"ID", "Quiz", "Score", "Total", "Percentage", "Date"};

        // Backing model so we can refresh table contents dynamically
        resultsTableModel = new javax.swing.table.DefaultTableModel(columnNames, 0);
        resultsTable = new JTable(resultsTableModel);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultsTable.setRowHeight(30);
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultsTable.getTableHeader().setBackground(new Color(67, 97, 238));
        resultsTable.getTableHeader().setForeground(Color.black);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // hide id column
        resultsTable.getColumnModel().getColumn(0).setMinWidth(0);
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        resultsTable.getColumnModel().getColumn(0).setWidth(0);
        resultsTable.getColumnModel().getColumn(1).setMinWidth(100);
        resultsTable.getColumnModel().getColumn(2).setMinWidth(70);
        resultsTable.getColumnModel().getColumn(3).setMinWidth(70);
        resultsTable.getColumnModel().getColumn(4).setMinWidth(90);
        resultsTable.getColumnModel().getColumn(5).setMinWidth(100);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(0, 260));
        panel.add(scrollPane);

        // View Details button
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewDetailsBtn = new JButton("View Details");
        viewDetailsBtn.addActionListener(e -> {
            int row = resultsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a result to view details.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Integer attemptId = (Integer) resultsTableModel.getValueAt(row, 0);
            if (attemptId != null) openAttemptDetails(attemptId);
        });
        actionsPanel.add(viewDetailsBtn);
        panel.add(actionsPanel);

        panel.add(Box.createVerticalGlue());

        // Preload results once (also refreshed whenever the results panel is opened)
        loadStudentResults();

        return panel;
    }

    private void loadStudentResults() {
        if (resultsTableModel == null) return;
        resultsTableModel.setRowCount(0);

        String sql = "SELECT q.id, q.quiz_type, q.totalScore, q.totalQuestions, q.percentage, q.attemptDate, q.course_id, c.course_name " +
                     "FROM QuizScores q LEFT JOIN Courses c ON q.course_id = c.id WHERE q.studentName = ? ORDER BY q.attemptDate DESC";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getName());
            ResultSet rs = pstmt.executeQuery();

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                int id = rs.getInt("id");
                String quizType = rs.getString("quiz_type");
                String courseName = rs.getString("course_name");
                int courseId = rs.getInt("course_id");

                String quizLabel;
                if (quizType != null && quizType.equalsIgnoreCase("GENERAL")) {
                    quizLabel = "General";
                } else if (courseName != null && !courseName.isEmpty()) {
                    quizLabel = courseName;
                } else if (courseId > 0) {
                    quizLabel = "Course " + courseId;
                } else {
                    quizLabel = "N/A";
                }

                int score = rs.getInt("totalScore");
                int total = rs.getInt("totalQuestions");
                double perc = rs.getDouble("percentage");
                String date = rs.getString("attemptDate");

                resultsTableModel.addRow(new Object[]{id, quizLabel, score, total, String.format("%.2f%%", perc), date});
            }

            if (!hasResults) {
                System.out.println("No quiz results found for student: " + student.getName());
            }

        } catch (SQLException e) {
            System.out.println("Error loading student results: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openAttemptDetails(int attemptId) {
        String sql = "SELECT ad.question_id, ad.selectedAnswer, q.text, q.options, q.correctAnswer, q.questionType " +
                     "FROM AttemptDetails ad JOIN Questions q ON q.id = ad.question_id WHERE ad.attempt_id = ? ORDER BY ad.id ASC";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, attemptId);
            ResultSet rs = pstmt.executeQuery();

            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(new String[]{"#","Question","Your Answer","Correct","Result"}, 0);
            int i = 1;
            boolean any = false;
            while (rs.next()) {
                any = true;
                int qid = rs.getInt("question_id");
                String sel = rs.getString("selectedAnswer");
                String qtext = rs.getString("text");
                String options = rs.getString("options");
                String correct = rs.getString("correctAnswer");
                String type = rs.getString("questionType");

                String selDisplay = formatAnswerForDisplay(sel, options, type);
                String correctDisplay = formatAnswerForDisplay(correct, options, type);
                String result = (sel != null && !sel.isEmpty() && sel.equals(correct)) ? "Correct" : "Incorrect";
                if (sel == null || sel.isEmpty()) result = "No Answer";

                model.addRow(new Object[]{i++, qtext, selDisplay, correctDisplay, result});
            }

            if (!any) {
                JOptionPane.showMessageDialog(this, "No detailed answers found for this attempt.", "No Details", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JTable table = new JTable(model);
            table.setRowHeight(28);
            table.getColumnModel().getColumn(0).setMaxWidth(50);

            JScrollPane sp = new JScrollPane(table);
            sp.setPreferredSize(new Dimension(800, 400));

            JOptionPane.showMessageDialog(this, sp, "Attempt Details (ID=" + attemptId + ")", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException e) {
            System.out.println("Error loading attempt details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatAnswerForDisplay(String letter, String optionsStr, String type) {
        if (letter == null || letter.isEmpty()) return "-";
        letter = letter.trim();
        if ("MCQ".equalsIgnoreCase(type) && optionsStr != null) {
            String[] opts = optionsStr.split("\\|");
            switch (letter) {
                case "A": return (opts.length > 0 ? opts[0] : "A");
                case "B": return (opts.length > 1 ? opts[1] : "B");
                case "C": return (opts.length > 2 ? opts[2] : "C");
                case "D": return (opts.length > 3 ? opts[3] : "D");
                default: return letter;
            }
        } else if ("TF".equalsIgnoreCase(type)) {
            if (letter.equals("A")) return "True";
            if (letter.equals("B")) return "False";
            return letter;
        } else {
            return letter;
        }
    }
    private JComponent createAttemptPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Attempt a Quiz");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JLabel infoLabel = new JLabel("Select a teacher or course to begin:");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(infoLabel);

        panel.add(Box.createVerticalStrut(15));

        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        listsPanel.setBackground(new Color(240, 242, 245));

        // Left: Teachers -> General Quiz
        JPanel teachersCard = new JPanel();
        teachersCard.setLayout(new BoxLayout(teachersCard, BoxLayout.Y_AXIS));
        teachersCard.setBorder(BorderFactory.createTitledBorder("Teacher Quizzes"));
        teachersCard.setBackground(Color.WHITE);
        teachersCard.setPreferredSize(new Dimension(350, 300));

        // populate teachers from DB
        try (java.sql.Connection conn = util.DataStore.connect();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT user_id, name FROM user WHERE role = 'EDUCATOR' ORDER BY name ASC")) {
            while (rs.next()) {
                int tid = rs.getInt("user_id");
                String tname = rs.getString("name");
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(Color.WHITE);
                JLabel name = new JLabel(tname + " (ID: " + tid + ")");
                JButton take = new JButton("Start Teacher Quiz");
                take.addActionListener(e -> {
                    // Only load GENERAL questions (not attached to a course)
                    java.util.List<quiz.Question> qs = quizService.getGeneralQuestionsByTeacher(tid);
                    if (qs.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No general questions available for " + tname);
                        return;
                    }
                    // Pass the teacher id so the attempt can be associated with this educator
                    QuizAttemptDialog dlg = new QuizAttemptDialog(this, student.getName(), qs, "Teacher: " + tname, quizService, 0, 0, "GENERAL", tid);
                    dlg.setVisible(true);
                });
                row.add(name, BorderLayout.WEST);
                row.add(take, BorderLayout.EAST);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                teachersCard.add(row);
                teachersCard.add(Box.createVerticalStrut(6));
            }
        } catch (Exception ex) {
            System.out.println("Error loading teachers: " + ex.getMessage());
        }

        // Right: Courses -> Course Quiz
        JPanel coursesCard = new JPanel();
        coursesCard.setLayout(new BoxLayout(coursesCard, BoxLayout.Y_AXIS));
        coursesCard.setBorder(BorderFactory.createTitledBorder("Course Quizzes"));
        coursesCard.setBackground(Color.WHITE);
        coursesCard.setPreferredSize(new Dimension(350, 300));

        String sql = "SELECT c.id, c.course_name, c.time_limit, u.user_id, u.name FROM Courses c JOIN user u ON c.educator_id = u.user_id ORDER BY c.course_name ASC";
        try (java.sql.Connection conn = util.DataStore.connect();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int cid = rs.getInt("id");
                String cname = rs.getString("course_name");
                int timeLimit = rs.getInt("time_limit");
                int tid = rs.getInt("user_id");
                String tname = rs.getString("name");

                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setBackground(Color.WHITE);
                JLabel lbl = new JLabel(cname + " (Teacher: " + tname + ")");
                if (timeLimit > 0) lbl.setText(lbl.getText() + " — " + timeLimit + " min");
                JButton start = new JButton("Start Course Quiz");
                JButton enroll = new JButton("Enroll");

                start.addActionListener(e -> {
                    java.util.List<quiz.Question> qs = quizService.getQuestionsByTeacherAndCourse(tid, cid);
                    if (qs.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No questions found for " + cname);
                        return;
                    }
                    // Also pass the teacher id so saved attempts have educator context
                    QuizAttemptDialog dlg = new QuizAttemptDialog(this, student.getName(), qs, cname, quizService, timeLimit, cid, "COURSE", tid);
                    dlg.setVisible(true);
                });

                enroll.addActionListener(e -> {
                    enrollInCourse(cid);
                });

                row.add(lbl);
                row.add(start);
                row.add(enroll);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                coursesCard.add(row);
                coursesCard.add(Box.createVerticalStrut(6));
            }
        } catch (Exception ex) {
            System.out.println("Error loading courses: " + ex.getMessage());
        }

        listsPanel.add(new JScrollPane(teachersCard));
        listsPanel.add(new JScrollPane(coursesCard));

        panel.add(listsPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JComponent createForumPanel() {
        // ForumPanel handles both announcements and discussions; pass current student (a User)
        return new ForumPanel(student);
    }

    private JComponent createCoursesPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Available Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JPanel coursesPanel = new JPanel();
        coursesPanel.setLayout(new BoxLayout(coursesPanel, BoxLayout.Y_AXIS));
        coursesPanel.setBackground(new Color(240, 242, 245));

        String sql = "SELECT c.id, c.course_name, c.lesson_content, u.user_id, u.name FROM Courses c JOIN user u ON c.educator_id = u.user_id ORDER BY c.course_name ASC";
        try (java.sql.Connection conn = util.DataStore.connect();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int cid = rs.getInt("id");
                String cname = rs.getString("course_name");
                String content = rs.getString("lesson_content");
                int tid = rs.getInt("user_id");
                String tname = rs.getString("name");

                JPanel card = new JPanel(new BorderLayout());
                card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
                card.setBackground(Color.WHITE);

                JLabel lbl = new JLabel("" + cname + " (Teacher: " + tname + ")");
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

                JTextArea contentArea = new JTextArea(content);
                contentArea.setLineWrap(true);
                contentArea.setWrapStyleWord(true);
                contentArea.setEditable(false);
                contentArea.setBackground(Color.WHITE);
                contentArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                actions.setBackground(Color.WHITE);
                JButton enroll = new JButton("Enroll");
                JButton view = new JButton("View Content");

                enroll.addActionListener(e -> enrollInCourse(cid));
                view.addActionListener(e -> {
                    JOptionPane.showMessageDialog(this, content, cname + " - Lesson Content", JOptionPane.INFORMATION_MESSAGE);
                });

                actions.add(view);
                actions.add(enroll);

                card.add(lbl, BorderLayout.NORTH);
                card.add(contentArea, BorderLayout.CENTER);
                card.add(actions, BorderLayout.SOUTH);

                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
                coursesPanel.add(card);
                coursesPanel.add(Box.createVerticalStrut(10));
            }
        } catch (Exception e) {
            System.out.println("Error loading courses: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(coursesPanel);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        panel.add(scrollPane);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JComponent createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Your Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JPanel profileCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        profileCard.setBackground(Color.WHITE);

        String[] labels = {"Name:", "Email:", "Age:", "Gender:", "Birth Date:", "Student ID:"};
        String[] values = {student.getName(), student.getEmail(), String.valueOf(student.getAge()), student.getGender(), student.getBirthDate(), dataStore.getStudentIdentifierByUserId(student.getUserId())};

        for (int i = 0; i < labels.length; i++) {
            JPanel fieldPanel = new JPanel(new BorderLayout(10, 0));
            fieldPanel.setBackground(Color.WHITE);
            fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            JLabel labelLabel = new JLabel(labels[i]);
            labelLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            labelLabel.setForeground(new Color(60, 60, 60));
            labelLabel.setPreferredSize(new Dimension(100, 0));

            JLabel valueLabel = new JLabel(values[i]);
            valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            valueLabel.setForeground(new Color(100, 100, 100));

            fieldPanel.add(labelLabel, BorderLayout.WEST);
            fieldPanel.add(valueLabel, BorderLayout.CENTER);
            profileCard.add(fieldPanel);

            if (i < labels.length - 1) {
                profileCard.add(Box.createVerticalStrut(15));
            }
        }

        panel.add(profileCard);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    public void showMenu() {
        // For backward compatibility with console-based code
        cardLayout.show(mainPanel, "dashboard");
    }

    private void viewResults() {
        dataStore.displayStudentResults(student.getName());
    }

    private void enrollInCourse(int courseId) {
        try (java.sql.Connection conn = util.DataStore.connect()) {
            conn.setAutoCommit(false);
            int studentDbId = -1;

            // 1) Find or create a student entry (student.student_id) using user_id
            String findSql = "SELECT student_id FROM student WHERE user_id = ?";
            try (java.sql.PreparedStatement findStmt = conn.prepareStatement(findSql)) {
                findStmt.setInt(1, student.getUserId());
                java.sql.ResultSet rs = findStmt.executeQuery();
                if (rs.next()) studentDbId = rs.getInt("student_id");
            }

            if (studentDbId == -1) {
                String insertSql = "INSERT INTO student (user_id, gpa, major) VALUES (?, ?, ?)";
                try (java.sql.PreparedStatement insertStmt = conn.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setInt(1, student.getUserId());
                    insertStmt.setDouble(2, 0.0);
                    insertStmt.setString(3, "Undeclared");
                    insertStmt.executeUpdate();
                    try (java.sql.ResultSet gk = insertStmt.getGeneratedKeys()) {
                        if (gk.next()) studentDbId = gk.getInt(1);
                    }
                }
            }

            if (studentDbId == -1) throw new Exception("Could not determine student id");

            // 2) Enroll (avoid duplicates)
            String enrollSql = "INSERT OR IGNORE INTO Enrollments (student_id, course_id) VALUES (?, ?)";
            try (java.sql.PreparedStatement enrollStmt = conn.prepareStatement(enrollSql)) {
                enrollStmt.setInt(1, studentDbId);
                enrollStmt.setInt(2, courseId);
                int rows = enrollStmt.executeUpdate();
                conn.commit();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Enrolled successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "You are already enrolled in this course.");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enrollment error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
