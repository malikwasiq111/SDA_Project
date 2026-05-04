package gui;

import controllers.AdminController;
import models.*;
import observer.ComplaintObserver;
import observer.NotificationService;
import store.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame implements ComplaintObserver {

    private final Admin admin;

    private DefaultTableModel allComplaintsModel;
    private JTable            allComplaintsTable;
    private JComboBox<String> statusFilter;

    private DefaultTableModel assignModel;
    private JTable            assignTable;
    private JComboBox<String> techCombo;

    private JLabel totalLbl, submittedLbl, assignedLbl, resolvedLbl, closedLbl;

    private DefaultTableModel accountsModel;

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        NotificationService.register(this);

        setTitle("Admin Dashboard — " + admin.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("📋 All Complaints",  buildAllComplaintsTab());
        tabs.addTab("🔗 Assign",          buildAssignTab());
        tabs.addTab("📊 Reports",         buildReportsTab());
        tabs.addTab("👥 Manage Accounts", buildAccountsTab());

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(20, 80, 50));
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel name = new JLabel("Admin Panel — " + admin.getName());
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { NotificationService.unregister(this); dispose(); new LoginFrame().setVisible(true); });
        p.add(name, BorderLayout.WEST);
        p.add(logout, BorderLayout.EAST);
        return p;
    }

    // ── Tab 1: All Complaints ─────────────────────────────────────────────────

    private JPanel buildAllComplaintsTab() {
        String[] cols = {"ID","Student","Title","Type","Status","Technician"};
        allComplaintsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        allComplaintsTable = new JTable(allComplaintsModel);
        styleTable(allComplaintsTable);

        statusFilter = new JComboBox<>(new String[]{"All","Submitted","Assigned","Resolved","Feedback_Given","Closed"});
        statusFilter.addActionListener(e -> refreshAllComplaints());

        JButton closeBtn = new JButton("Close Complaint");
        closeBtn.setBackground(new Color(180, 30, 30));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> {
            int row = allComplaintsTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a complaint first.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            int id = (int) allComplaintsModel.getValueAt(row, 0);
            String status = (String) allComplaintsModel.getValueAt(row, 4);
            if (status.equals("Closed")) { JOptionPane.showMessageDialog(this, "This complaint is already closed.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
            boolean ok = AdminController.closeComplaint(id);
            if (ok) { JOptionPane.showMessageDialog(this, "Complaint #" + id + " closed.", "Success", JOptionPane.INFORMATION_MESSAGE); }
            else    { JOptionPane.showMessageDialog(this, "Cannot close complaint in current state.", "Error", JOptionPane.ERROR_MESSAGE); }
        });

        JButton refresh = new JButton("🔄 Refresh");
        refresh.addActionListener(e -> refreshAllComplaints());

        refreshAllComplaints();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Filter by Status:"));
        top.add(statusFilter);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btn.add(refresh); btn.add(closeBtn);

        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(allComplaintsTable), BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    private void refreshAllComplaints() {
        allComplaintsModel.setRowCount(0);
        String filter = (String) statusFilter.getSelectedItem();
        List<Complaint> list = AdminController.getComplaintsByStatus(filter);
        for (Complaint c : list) {
            String student  = resolveStudent(c.getStudentRoll());
            String tech     = c.getTechnicianId() == 0 ? "Unassigned" : resolveTech(c.getTechnicianId());
            allComplaintsModel.addRow(new Object[]{c.getId(), student, c.getTitle(), c.getType(), c.getStatus(), tech});
        }
    }

    // ── Tab 2: Assign ─────────────────────────────────────────────────────────

    private JPanel buildAssignTab() {
        String[] cols = {"ID","Student","Title","Type","Date"};
        assignModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        assignTable = new JTable(assignModel);
        styleTable(assignTable);

        techCombo = new JComboBox<>();
        refreshTechCombo();

        JButton assignBtn = primaryButton("Assign to Technician");
        assignBtn.addActionListener(e -> {
            int row = assignTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a complaint first.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (techCombo.getItemCount() == 0) { JOptionPane.showMessageDialog(this, "Please select a technician to assign.", "Error", JOptionPane.ERROR_MESSAGE); return; }

            int complaintId = (int) assignModel.getValueAt(row, 0);
            String techEntry = (String) techCombo.getSelectedItem();
            int techId = Integer.parseInt(techEntry.split("ID:")[1].split("\\)")[0].trim());

            boolean ok = AdminController.assignComplaint(complaintId, techId);
            if (ok) { JOptionPane.showMessageDialog(this, "Complaint assigned successfully.", "Success", JOptionPane.INFORMATION_MESSAGE); refreshAssignTable(); refreshAllComplaints(); }
            else    { JOptionPane.showMessageDialog(this, "Assignment failed. Complaint may not be in Submitted state.", "Error", JOptionPane.ERROR_MESSAGE); }
        });

        refreshAssignTable();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Assign To:"));
        top.add(techCombo);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btn.add(assignBtn);

        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(assignTable), BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    private void refreshAssignTable() {
        assignModel.setRowCount(0);
        List<Complaint> submitted = AdminController.getComplaintsByStatus("Submitted");
        for (Complaint c : submitted)
            assignModel.addRow(new Object[]{c.getId(), resolveStudent(c.getStudentRoll()), c.getTitle(), c.getType(), c.getSubmitDate()});
    }

    private void refreshTechCombo() {
        techCombo.removeAllItems();
        for (Technician t : AdminController.getAllTechnicians())
            techCombo.addItem(t.getName() + " (ID:" + t.getID() + ")");
    }

    // ── Tab 3: Reports ────────────────────────────────────────────────────────

    private JPanel buildReportsTab() {
        totalLbl     = statLabel("—");
        submittedLbl = statLabel("—");
        assignedLbl  = statLabel("—");
        resolvedLbl  = statLabel("—");
        closedLbl    = statLabel("—");

        JPanel grid = new JPanel(new GridLayout(5, 2, 10, 12));
        grid.setBorder(new EmptyBorder(20, 60, 20, 60));
        grid.add(boldLabel("Total Complaints:"));  grid.add(totalLbl);
        grid.add(boldLabel("Submitted:"));         grid.add(submittedLbl);
        grid.add(boldLabel("Assigned:"));          grid.add(assignedLbl);
        grid.add(boldLabel("Resolved:"));          grid.add(resolvedLbl);
        grid.add(boldLabel("Closed:"));            grid.add(closedLbl);

        JButton gen = primaryButton("Generate Report");
        gen.addActionListener(e -> {
            Report r = AdminController.generateReport();
            totalLbl.setText(String.valueOf(r.getTotalComplaints()));
            submittedLbl.setText(String.valueOf(r.getSubmittedComplaints()));
            assignedLbl.setText(String.valueOf(r.getAssignedComplaints()));
            resolvedLbl.setText(String.valueOf(r.getResolvedComplaints()));
            closedLbl.setText(String.valueOf(r.getClosedComplaints()));
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.add(grid, BorderLayout.CENTER);
        JPanel btn = new JPanel(); btn.add(gen);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    // ── Tab 4: Manage Accounts ────────────────────────────────────────────────

    private JPanel buildAccountsTab() {
        String[] cols = {"ID", "Name", "Email", "Role"};
        accountsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(accountsModel);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionBackground(new Color(180, 210, 255));

        refreshAccountsTable(accountsModel);

        // ── Add User ──────────────────────────────────────────────────────────
        JButton addBtn = new JButton("➕ Add User");
        addBtn.setBackground(new Color(20, 80, 50));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> {
            JTextField nameF  = new JTextField();
            JTextField emailF = new JTextField();
            JPasswordField passF = new JPasswordField();
            JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Technician"});

            JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
            form.add(new JLabel("Name:"));    form.add(nameF);
            form.add(new JLabel("Email:"));   form.add(emailF);
            form.add(new JLabel("Password:"));form.add(passF);
            form.add(new JLabel("Role:"));    form.add(roleBox);

            int result = JOptionPane.showConfirmDialog(this, form,
                "Add New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String name  = nameF.getText().trim();
                String email = emailF.getText().trim();
                String pass  = new String(passF.getPassword()).trim();
                String role  = (String) roleBox.getSelectedItem();

                if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean ok = role.equals("Student")
                    ? DataStore.getInstance().addStudent(name, email, pass)
                    : DataStore.getInstance().addTechnician(name, email, pass);

                if (ok) {
                    JOptionPane.showMessageDialog(this, role + " added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAccountsTable(accountsModel);
                    refreshTechCombo(); // update assign tab dropdown
                } else {
                    JOptionPane.showMessageDialog(this, "Email already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ── Remove User ───────────────────────────────────────────────────────
        JButton removeBtn = new JButton("🗑 Remove User");
        removeBtn.setBackground(new Color(180, 30, 30));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setFocusPainted(false);
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int    id   = (int)    accountsModel.getValueAt(row, 0);
            String name = (String) accountsModel.getValueAt(row, 1);
            String role = (String) accountsModel.getValueAt(row, 3);

            int confirm = JOptionPane.showConfirmDialog(this,
                "Remove " + role + " \"" + name + "\"?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = role.equals("Student")
                    ? DataStore.getInstance().removeStudent(id)
                    : DataStore.getInstance().removeTechnician(id);

                if (ok) {
                    JOptionPane.showMessageDialog(this, role + " removed.", "Done", JOptionPane.INFORMATION_MESSAGE);
                    refreshAccountsTable(accountsModel);
                    refreshTechCombo();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not remove user.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(addBtn); btnRow.add(removeBtn);

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnRow, BorderLayout.SOUTH);
        return p;
    }

    private void refreshAccountsTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Student s : AdminController.getAllStudents())
            model.addRow(new Object[]{s.getRoll(), s.getName(), s.getEmail(), "Student"});
        for (Technician t : AdminController.getAllTechnicians())
            model.addRow(new Object[]{t.getID(), t.getName(), t.getEmail(), "Technician"});
    }

    // ── Observer ──────────────────────────────────────────────────────────────

    @Override
    public void onStatusChanged(int complaintId, String newStatus, String message) {
        SwingUtilities.invokeLater(() -> {
            refreshAllComplaints();
            refreshAssignTable();
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String resolveStudent(int roll) {
        for (Student s : AdminController.getAllStudents())
            if (s.getRoll() == roll) return s.getName();
        return "Roll#" + roll;
    }

    private String resolveTech(int id) {
        for (Technician t : AdminController.getAllTechnicians())
            if (t.getID() == id) return t.getName();
        return "Tech#" + id;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(26);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setSelectionBackground(new Color(180, 210, 255));
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(20, 80, 50));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        return b;
    }

    private JLabel statLabel(String val) {
        JLabel l = new JLabel(val);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(new Color(30, 60, 114));
        return l;
    }

    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return l;
    }
}