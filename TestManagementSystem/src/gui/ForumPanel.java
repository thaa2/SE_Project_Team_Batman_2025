package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import util.DataStore;
import auth.User;
import auth.Role;
import forum.Announcement;
import forum.ThreadModel;
import forum.Message;

public class ForumPanel extends JPanel {
    private User currentUser;
    private DataStore dataStore;

    // Announcements components
    private DefaultListModel<Announcement> annListModel;
    private JList<Announcement> annList;
    private JTextArea annContentArea;
    private JTextField annTitleField;
    private JButton addAnnButton;
    private JButton editAnnButton;
    private JButton deleteAnnButton;

    // Discussions components
    private DefaultListModel<ThreadModel> threadListModel;
    private JList<ThreadModel> threadList;
    private JTextArea messagesArea;
    private JTextField newThreadField;
    private JTextField newMessageField;
    private JButton createThreadButton;
    private JButton postMessageButton;

    public ForumPanel(User user) {
        this.currentUser = user;
        this.dataStore = new DataStore();
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Announcements", createAnnouncementsPanel());
        tabs.addTab("Discussions", createDiscussionsPanel());
        add(tabs, BorderLayout.CENTER);
        loadAnnouncements();
        loadThreads();

        // Periodically refresh so other users' posts/announcements appear
        javax.swing.Timer refresher = new javax.swing.Timer(10000, e -> {
            int selectedThreadId = -1;
            ThreadModel sel = threadList.getSelectedValue();
            if (sel != null) selectedThreadId = sel.getId();
            loadAnnouncements();
            loadThreads();
            if (selectedThreadId != -1) {
                // try to re-select the same thread
                for (int i = 0; i < threadListModel.getSize(); i++) {
                    ThreadModel t = threadListModel.getElementAt(i);
                    if (t.getId() == selectedThreadId) {
                        threadList.setSelectedIndex(i);
                        threadList.ensureIndexIsVisible(i);
                        showThreadMessages();
                        break;
                    }
                }
            }
        });
        refresher.setRepeats(true);
        refresher.start();
    }

    // ---------------- Announcements Tab ----------------
    private JPanel createAnnouncementsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new BorderLayout());
        annListModel = new DefaultListModel<>();
        annList = new JList<>(annListModel);
        annList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel("[" + value.getEducatorName() + "] " + value.getTitle() + " (" + value.getCreatedAt() + ")");
            lbl.setOpaque(true);
            if (isSelected) {
                lbl.setBackground(new Color(220, 230, 250));
            } else {
                lbl.setBackground(Color.WHITE);
            }
            lbl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            return lbl;
        });
        annList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        annList.addListSelectionListener(e -> showSelectedAnnouncement());
        left.add(new JScrollPane(annList), BorderLayout.CENTER);
        panel.add(left, BorderLayout.WEST);

        // Right side: content + actions
        JPanel right = new JPanel(new BorderLayout());
        annContentArea = new JTextArea();
        annContentArea.setEditable(currentUser.getRole() == Role.EDUCATOR); // Educators can compose; students only view
        annContentArea.setLineWrap(true);
        annContentArea.setWrapStyleWord(true);
        right.add(new JScrollPane(annContentArea), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        annTitleField = new JTextField();
        annTitleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        annContentArea.setPreferredSize(new Dimension(0, 200));

        addAnnButton = new JButton("Add Announcement");
        editAnnButton = new JButton("Edit Announcement");
        deleteAnnButton = new JButton("Delete Announcement");

        addAnnButton.addActionListener(e -> handleAddAnnouncement());
        editAnnButton.addActionListener(e -> handleEditAnnouncement());
        deleteAnnButton.addActionListener(e -> handleDeleteAnnouncement());

        // Only educators have controls
        boolean isEducator = (currentUser.getRole() == Role.EDUCATOR);
        annTitleField.setVisible(isEducator);
        addAnnButton.setVisible(isEducator);
        editAnnButton.setVisible(isEducator);
        deleteAnnButton.setVisible(isEducator);

        if (isEducator) {
            actionPanel.add(new JLabel("Title:"));
            actionPanel.add(annTitleField);
            actionPanel.add(Box.createVerticalStrut(10));
            actionPanel.add(addAnnButton);
            actionPanel.add(Box.createVerticalStrut(5));
            actionPanel.add(editAnnButton);
            actionPanel.add(Box.createVerticalStrut(5));
            actionPanel.add(deleteAnnButton);
        }

        right.add(actionPanel, BorderLayout.SOUTH);

        panel.add(right, BorderLayout.CENTER);
        return panel;
    }

    private void loadAnnouncements() {
        annListModel.clear();
        List<Announcement> list = dataStore.getAnnouncements();
        for (Announcement a : list) annListModel.addElement(a);
    }

    private void showSelectedAnnouncement() {
        Announcement sel = annList.getSelectedValue();
        if (sel != null) {
            annContentArea.setText(sel.getContent());
            annTitleField.setText(sel.getTitle());
        } else {
            annContentArea.setText("");
            annTitleField.setText("");
        }
    }

    private void handleAddAnnouncement() {
        String title = annTitleField.getText().trim();
        String content = annContentArea.getText().trim();
        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both title and content", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int id = dataStore.createAnnouncement(title, content, currentUser.getUserId());
        if (id > 0) {
            JOptionPane.showMessageDialog(this, "Announcement posted", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAnnouncements();
            annTitleField.setText("");
            annContentArea.setText("");
        }
    }

    private void handleEditAnnouncement() {
        Announcement sel = annList.getSelectedValue();
        if (sel == null) return;
        if (sel.getEducatorId() != currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "You can only edit your own announcements", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String title = annTitleField.getText().trim();
        String content = annContentArea.getText().trim();
        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both title and content", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dataStore.editAnnouncement(sel.getId(), title, content, currentUser.getUserId());
        JOptionPane.showMessageDialog(this, "Announcement updated", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadAnnouncements();
    }

    private void handleDeleteAnnouncement() {
        Announcement sel = annList.getSelectedValue();
        if (sel == null) return;
        if (sel.getEducatorId() != currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "You can only delete your own announcements", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int res = JOptionPane.showConfirmDialog(this, "Delete this announcement?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            dataStore.deleteAnnouncement(sel.getId(), currentUser.getUserId());
            loadAnnouncements();
        }
    }

    // ---------------- Discussions Tab ----------------
    private JPanel createDiscussionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Left side: threads
        JPanel left = new JPanel(new BorderLayout());
        left.setBorder(BorderFactory.createTitledBorder("Threads"));
        left.setPreferredSize(new Dimension(320, 0));
        threadListModel = new DefaultListModel<>();
        threadList = new JList<>(threadListModel);
        threadList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        threadList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getTitle() + " - " + dataStore.getUserNameById(value.getCreatorId()) + " (" + value.getCreatedAt() + ")");
            lbl.setOpaque(true);
            if (isSelected) lbl.setBackground(new Color(220, 230, 250)); else lbl.setBackground(Color.WHITE);
            lbl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            return lbl;
        });
        threadList.addListSelectionListener(e -> showThreadMessages());
        left.add(new JScrollPane(threadList), BorderLayout.CENTER);

        JPanel createThreadPanel = new JPanel(new BorderLayout());
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.X_AXIS));
        newThreadField = new JTextField();
        newThreadField.setColumns(18);
        newThreadField.setToolTipText("Enter thread title and press Enter or 'Create Thread'");
        newThreadField.addActionListener(e -> handleCreateThread());
        inner.add(newThreadField);
        createThreadButton = new JButton("Create Thread");
        createThreadButton.addActionListener(e -> handleCreateThread());
        inner.add(Box.createHorizontalStrut(8));
        inner.add(createThreadButton);
        createThreadPanel.add(inner, BorderLayout.CENTER);
        left.add(createThreadPanel, BorderLayout.SOUTH);

        panel.add(left, BorderLayout.WEST);

        // Right side: messages and posting
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("Messages"));
        messagesArea = new JTextArea();
        messagesArea.setEditable(false);
        messagesArea.setLineWrap(true);
        messagesArea.setWrapStyleWord(true);
        right.add(new JScrollPane(messagesArea), BorderLayout.CENTER);

        JPanel postPanel = new JPanel(new BorderLayout());
        newMessageField = new JTextField();
        newMessageField.setToolTipText("Type a message and press Enter or 'Post'");
        newMessageField.addActionListener(e -> handlePostMessage());
        postMessageButton = new JButton("Post");
        postMessageButton.addActionListener(e -> handlePostMessage());
        postPanel.add(newMessageField, BorderLayout.CENTER);
        postPanel.add(postMessageButton, BorderLayout.EAST);
        right.add(postPanel, BorderLayout.SOUTH);

        panel.add(right, BorderLayout.CENTER);

        return panel;
    }

    private void loadThreads() {
        int prevSelectedId = -1;
        ThreadModel prev = threadList.getSelectedValue();
        if (prev != null) prevSelectedId = prev.getId();

        threadListModel.clear();
        List<ThreadModel> list = dataStore.getThreads();
        for (ThreadModel t : list) threadListModel.addElement(t);

        // Auto-select first if none selected, otherwise try to reselect previous
        if (threadListModel.getSize() > 0) {
            if (prevSelectedId != -1) {
                for (int i = 0; i < threadListModel.getSize(); i++) {
                    if (threadListModel.getElementAt(i).getId() == prevSelectedId) {
                        threadList.setSelectedIndex(i);
                        threadList.ensureIndexIsVisible(i);
                        showThreadMessages();
                        return;
                    }
                }
            }
            if (threadList.getSelectedIndex() == -1) {
                threadList.setSelectedIndex(0);
                threadList.ensureIndexIsVisible(0);
                showThreadMessages();
            }
        } else {
            messagesArea.setText("");
        }
    }

    private void showThreadMessages() {
        ThreadModel sel = threadList.getSelectedValue();
        messagesArea.setText("");
        if (sel == null) return;
        List<Message> msgs = dataStore.getMessagesByThread(sel.getId());
        StringBuilder sb = new StringBuilder();
        if (msgs.isEmpty()) {
            sb.append("No messages yet. Be the first to post!\n");
        } else {
            for (Message m : msgs) {
                sb.append(m.getAuthorName()).append(" (" + m.getCreatedAt() + "):\n");
                sb.append(m.getContent()).append("\n\n");
            }
        }
        messagesArea.setText(sb.toString());
        messagesArea.setCaretPosition(0);
    }

    private void handleCreateThread() {
        String title = newThreadField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a thread title", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (currentUser.getUserId() <= 0) {
            JOptionPane.showMessageDialog(this, "User identity not set. Please re-login.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int id = dataStore.createThread(title, currentUser.getUserId());
        if (id > 0) {
            newThreadField.setText("");
            loadThreads();
            // Select the newly created thread by id
            for (int i = 0; i < threadListModel.getSize(); i++) {
                ThreadModel t = threadListModel.getElementAt(i);
                if (t.getId() == id) {
                    threadList.setSelectedIndex(i);
                    threadList.ensureIndexIsVisible(i);
                    break;
                }
            }
            newMessageField.requestFocusInWindow();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create thread. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePostMessage() {
        ThreadModel sel = threadList.getSelectedValue();
        // If no thread selected but thread title entered, create it automatically
        if (sel == null) {
            String pendingTitle = newThreadField.getText().trim();
            if (!pendingTitle.isEmpty()) {
                if (currentUser.getUserId() <= 0) {
                    JOptionPane.showMessageDialog(this, "User identity not set. Please re-login.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int newId = dataStore.createThread(pendingTitle, currentUser.getUserId());
                if (newId > 0) {
                    newThreadField.setText("");
                    loadThreads();
                    for (int i = 0; i < threadListModel.getSize(); i++) {
                        ThreadModel t = threadListModel.getElementAt(i);
                        if (t.getId() == newId) {
                            threadList.setSelectedIndex(i);
                            threadList.ensureIndexIsVisible(i);
                            sel = t;
                            break;
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create thread. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a thread first or create one", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String msg = newMessageField.getText().trim();
        if (msg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a message to post", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int msgId = dataStore.addMessage(sel.getId(), currentUser.getUserId(), msg);
        if (msgId > 0) {
            newMessageField.setText("");
            showThreadMessages();
            // scroll messages area to bottom
            messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
        } else {
            JOptionPane.showMessageDialog(this, "Failed to post message", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
