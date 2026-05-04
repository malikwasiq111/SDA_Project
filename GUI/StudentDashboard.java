package gui;

import controllers.StudentController;
import models.*;
import observer.ComplaintObserver;
import observer.NotificationService;
import store.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class StudentDashboard extends JFrame implements ComplaintObserver {

    private final Student student;

    // Tab 2
    private JTable      complaintsTable;
    private DefaultTableModel complaintsModel;

    // Tab 3
    private JComboBox<String> feedbackCombo;
    private JTextArea         feedbackArea;

    // Tab 4
    private JTextArea notificationsArea;

    public StudentDashboard(Student student) {
        this.student = student;
        NotificationService.register(this);

        setTitle("Student Dashboard — " + student.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 560);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("📝 Submit Complaint",  buildSubmitTab());
        tabs.addTab("📋 My Complaints",     buildMyComplaintsTab());
        tabs.addTab("💬 Give Feedback",     buildFeedbackTab());
        tabs.addTab("🔔 Notifications",     buildNotificationsTab());

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(30, 60, 114));
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel name = new JLabel("Welcome, " + student.getName());
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel role = new JLabel("Roll No: " + student.getRoll() + "  |  Student Portal");
        role.setForeground(new Color(180, 210, 255));
        role.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton logout = new JButton("Logout");
        logout.setFocusPainted(false);
        logout.addActionListener(e -> { NotificationService.unregister(this); dispose(); new LoginFrame().setVisible(true); });
        p.add(name, BorderLayout.WEST);
        p.add(role, BorderLayout.CENTER);
        p.add(logout, BorderLayout.EAST);
        return p;
    }

    // ── Tab 1: Submit ─────────────────────────────────────────────────────────

    private JPanel buildSubmitTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;

        JTextField titleField = new JTextField();
        JTextArea  descArea   = new JTextArea(4, 30);
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Hardware","Software","Network","Other"});

        addRow(p, g, "Issue Title:", titleField);
        g.gridy++;
        addRow(p, g, "Description:", new JScrollPane(descArea));
        g.gridy++;
        addRow(p, g, "Issue Type:", typeBox);
        g.gridy++;

        JButton submit = primaryButton("Submit Complaint");
        submit.addActionListener(e -> {
            String title = titleField.getText().trim();
            String desc  = descArea.getText().trim();
            String type  = (String) typeBox.getSelectedItem();
            if (title.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            StudentController.submitComplaint(title, desc, type, student.getRoll());
            JOptionPane.showMessageDialog(this, "Complaint submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            titleField.setText(""); descArea.setText("");
            refreshComplaintsTable();
        });
        p.add(submit, g);
        return p;
    }

    // ── Tab 2: My Complaints ──────────────────────────────────────────────────

    private JPanel buildMyComplaintsTab() {
        String[] cols = {"ID","Title","Type","Status","Date"};
        complaintsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        complaintsTable = new JTable(complaintsModel);
        complaintsTable.setRowHeight(26);
        complaintsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        complaintsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Color rows by status
        complaintsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String status = (String) t.getModel().getValueAt(row, 3);
                if (!sel) {
                    switch (status) {
                        case "Submitted":      setBackground(new Color(210, 230, 255)); break;
                        case "Assigned":       setBackground(new Color(255, 235, 180)); break;
                        case "Resolved":       setBackground(new Color(200, 240, 200)); break;
                        case "Feedback_Given": setBackground(new Color(220, 255, 220)); break;
                        case "Closed":         setBackground(new Color(220, 220, 220)); break;
                        default:               setBackground(Color.WHITE);
                    }
                }
                return this;
            }
        });

        refreshComplaintsTable();

        JButton refresh = new JButton("🔄 Refresh");
        refresh.addActionListener(e -> refreshComplaintsTable());

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.add(new JScrollPane(complaintsTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(refresh);
        p.add(btnRow, BorderLayout.SOUTH);
        return p;
    }

    private void refreshComplaintsTable() {
        complaintsModel.setRowCount(0);
        List<Complaint> list = StudentController.getMyComplaints(student.getRoll());
        for (Complaint c : list) {
            complaintsModel.addRow(new Object[]{
                c.getId(), c.getTitle(), c.getType(), c.getStatus(), c.getSubmitDate()
            });
        }
    }

    // ── Tab 3: Feedback ───────────────────────────────────────────────────────

    private JPanel buildFeedbackTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;

        feedbackCombo = new JComboBox<>();
        refreshFeedbackCombo();

        feedbackArea = new JTextArea(4, 30);
        feedbackArea.setLineWrap(true); feedbackArea.setWrapStyleWord(true);

        addRow(p, g, "Select Resolved Complaint:", feedbackCombo);
        g.gridy++;
        addRow(p, g, "Your Feedback:", new JScrollPane(feedbackArea));
        g.gridy++;

        JButton submit = primaryButton("Submit Feedback");
        submit.addActionListener(e -> {
            String msg = feedbackArea.getText().trim();
            if (feedbackCombo.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No resolved complaints available.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (msg.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String selected = (String) feedbackCombo.getSelectedItem();
            int id = Integer.parseInt(selected.split("\\[#")[1].split("]")[0]);
            boolean ok = StudentController.submitFeedback(id, student.getRoll(), msg);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Feedback submitted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                feedbackArea.setText("");
                refreshFeedbackCombo();
                refreshComplaintsTable();
            } else {
                JOptionPane.showMessageDialog(this, "Feedback only allowed on Resolved complaints.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        p.add(submit, g);
        return p;
    }

    private void refreshFeedbackCombo() {
        if (feedbackCombo == null) return;
        feedbackCombo.removeAllItems();
        List<Complaint> resolved = StudentController.getResolvedComplaints(student.getRoll());
        for (Complaint c : resolved) feedbackCombo.addItem(c.toString());
    }

    // ── Tab 4: Notifications ──────────────────────────────────────────────────

    private JPanel buildNotificationsTab() {
        notificationsArea = new JTextArea();
        notificationsArea.setEditable(false);
        notificationsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notificationsArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Load persisted notifications from DataStore on open
        List<String> saved = DataStore.getInstance().getNotifications(student.getRoll());
        if (saved.isEmpty()) {
            notificationsArea.setText("📬 No notifications yet.\n");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String msg : saved) sb.append("🔔 ").append(msg).append("\n");
            notificationsArea.setText(sb.toString());
        }

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.add(new JScrollPane(notificationsArea), BorderLayout.CENTER);

        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            DataStore.getInstance().clearNotifications(student.getRoll());
            notificationsArea.setText("");
        });
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btn.add(clear);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    // ── Observer ──────────────────────────────────────────────────────────────

    @Override
    public void onStatusChanged(int complaintId, String newStatus, String message) {
        // Only show notifications for this student's complaints
        Complaint c = DataStore.getInstance().getComplaintById(complaintId);
        if (c != null && c.getStudentRoll() == student.getRoll()) {
            SwingUtilities.invokeLater(() -> {
                notificationsArea.append("🔔 " + message + "\n");
                refreshComplaintsTable();
                refreshFeedbackCombo();
            });
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addRow(JPanel p, GridBagConstraints g, String label, Component field) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l, g);
        g.gridy++;
        p.add(field, g);
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(30, 60, 114));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(240, 36));
        return b;
    }
}