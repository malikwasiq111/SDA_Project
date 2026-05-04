package gui;

import controllers.TechnicianController;
import models.*;
import observer.ComplaintObserver;
import observer.NotificationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TechnicianDashboard extends JFrame implements ComplaintObserver {

    private final Technician technician;

    private DefaultTableModel assignedModel;
    private JTable            assignedTable;
    private JComboBox<String> updateCombo;

    public TechnicianDashboard(Technician technician) {
        this.technician = technician;
        NotificationService.register(this);

        setTitle("Technician Dashboard — " + technician.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 520);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("🛠 Assigned Complaints", buildAssignedTab());
        tabs.addTab("📨 Send Update",         buildSendUpdateTab());

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(90, 40, 10));
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel name = new JLabel("Technician — " + technician.getName());
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { NotificationService.unregister(this); dispose(); new LoginFrame().setVisible(true); });
        p.add(name, BorderLayout.WEST);
        p.add(logout, BorderLayout.EAST);
        return p;
    }

    // ── Tab 1: Assigned Complaints ────────────────────────────────────────────

    private JPanel buildAssignedTab() {
        String[] cols = {"ID","Student Roll","Title","Type","Status","Date"};
        assignedModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        assignedTable = new JTable(assignedModel);
        assignedTable.setRowHeight(26);
        assignedTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        assignedTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignedTable.setSelectionBackground(new Color(255, 220, 180));

        refreshAssignedTable();

        JButton resolveBtn = new JButton("✔ Mark Resolved");
        resolveBtn.setBackground(new Color(20, 130, 60));
        resolveBtn.setForeground(Color.WHITE);
        resolveBtn.setFocusPainted(false);
        resolveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resolveBtn.addActionListener(e -> {
            int row = assignedTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a complaint first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int id = (int) assignedModel.getValueAt(row, 0);
            boolean ok = TechnicianController.resolveComplaint(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Complaint #" + id + " marked as Resolved.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAssignedTable();
                refreshUpdateCombo();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot resolve. Complaint must be in Assigned state.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton returnBtn = new JButton("✘ Cannot Resolve");
        returnBtn.setBackground(new Color(180, 30, 30));
        returnBtn.setForeground(Color.WHITE);
        returnBtn.setFocusPainted(false);
        returnBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        returnBtn.addActionListener(e -> {
            int row = assignedTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a complaint first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int id = (int) assignedModel.getValueAt(row, 0);
            String reason = JOptionPane.showInputDialog(this, "Reason for returning:", "Cannot Resolve", JOptionPane.QUESTION_MESSAGE);
            if (reason != null && !reason.trim().isEmpty()) {
                TechnicianController.returnComplaint(id, reason.trim());
                JOptionPane.showMessageDialog(this, "Complaint returned to Admin.", "Done", JOptionPane.INFORMATION_MESSAGE);
                refreshAssignedTable();
            }
        });

        JButton refresh = new JButton("🔄 Refresh");
        refresh.addActionListener(e -> refreshAssignedTable());

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btn.add(refresh); btn.add(returnBtn); btn.add(resolveBtn);

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(assignedTable), BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    private void refreshAssignedTable() {
        assignedModel.setRowCount(0);
        List<Complaint> list = TechnicianController.getAssignedComplaints(technician.getID());
        for (Complaint c : list)
            assignedModel.addRow(new Object[]{c.getId(), c.getStudentRoll(), c.getTitle(), c.getType(), c.getStatus(), c.getSubmitDate()});
    }

    // ── Tab 2: Send Update ────────────────────────────────────────────────────

    private JPanel buildSendUpdateTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 0);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;

        updateCombo = new JComboBox<>();
        refreshUpdateCombo();

        JTextArea msgArea = new JTextArea(4, 30);
        msgArea.setLineWrap(true); msgArea.setWrapStyleWord(true);

        JLabel l1 = new JLabel("Select Complaint:");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l1, g); g.gridy++;
        p.add(updateCombo, g); g.gridy++;

        JLabel l2 = new JLabel("Update Message:");
        l2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l2, g); g.gridy++;
        p.add(new JScrollPane(msgArea), g); g.gridy++;

        JButton send = new JButton("Send Update");
        send.setBackground(new Color(90, 40, 10));
        send.setForeground(Color.WHITE);
        send.setFont(new Font("Segoe UI", Font.BOLD, 13));
        send.setFocusPainted(false);
        send.addActionListener(e -> {
            if (updateCombo.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No complaints available.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String msg = msgArea.getText().trim();
            if (msg.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String selected = (String) updateCombo.getSelectedItem();
            int id = Integer.parseInt(selected.split("#")[1].split("]")[0]);
            TechnicianController.sendUpdate(id, msg);
            JOptionPane.showMessageDialog(this, "Update sent to student.", "Success", JOptionPane.INFORMATION_MESSAGE);
            msgArea.setText("");
        });
        p.add(send, g);
        return p;
    }

    private void refreshUpdateCombo() {
        if (updateCombo == null) return;
        updateCombo.removeAllItems();
        List<Complaint> list = TechnicianController.getAssignedComplaints(technician.getID());
        for (Complaint c : list) updateCombo.addItem(c.toString());
    }

    // ── Observer ──────────────────────────────────────────────────────────────

    @Override
    public void onStatusChanged(int complaintId, String newStatus, String message) {
        SwingUtilities.invokeLater(() -> {
            refreshAssignedTable();
            refreshUpdateCombo();
        });
    }
}