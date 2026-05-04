package gui;

import controllers.AuthController;
import models.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField     emailField;
    private JPasswordField passwordField;
    private JRadioButton   rbStudent, rbAdmin, rbTechnician;

    public LoginFrame() {
        setTitle("IT Complaint Management System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));

        // ── Header ───────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(new Color(30, 60, 114));
        header.setBorder(new EmptyBorder(18, 20, 18, 20));
        JLabel title = new JLabel("IT Complaint Management System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel sub = new JLabel("NUCES FAST — BCS-4C");
        sub.setForeground(new Color(180, 200, 240));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel hText = new JPanel(new GridLayout(2, 1));
        hText.setOpaque(false);
        hText.add(title); hText.add(sub);
        header.add(hText);
        root.add(header, BorderLayout.NORTH);

        // ── Form ─────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        // Role selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel roleLabel = new JLabel("Login As:");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(roleLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        rbStudent    = new JRadioButton("Student");
        rbAdmin      = new JRadioButton("Admin");
        rbTechnician = new JRadioButton("Technician");
        rbStudent.setSelected(true);
        rbStudent.setOpaque(false);
        rbAdmin.setOpaque(false);
        rbTechnician.setOpaque(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbStudent); bg.add(rbAdmin); bg.add(rbTechnician);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolePanel.setOpaque(false);
        rolePanel.add(rbStudent); rolePanel.add(rbAdmin); rolePanel.add(rbTechnician);
        gbc.gridx = 0; gbc.gridwidth = 2;
        form.add(rolePanel, gbc);

        // Email
        gbc.gridy = 2; gbc.gridwidth = 2;
        form.add(makeLabel("Email Address:"), gbc);
        gbc.gridy = 3;
        emailField = new JTextField();
        styleField(emailField);
        form.add(emailField, gbc);

        // Password
        gbc.gridy = 4;
        form.add(makeLabel("Password:"), gbc);
        gbc.gridy = 5;
        passwordField = new JPasswordField();
        styleField(passwordField);
        form.add(passwordField, gbc);

        root.add(form, BorderLayout.CENTER);

        // ── Button ───────────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(0, 40, 20, 40));
        JButton loginBtn = new JButton("Login");
        loginBtn.setPreferredSize(new Dimension(340, 38));
        loginBtn.setBackground(new Color(30, 60, 114));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> doLogin());
        btnPanel.add(loginBtn);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void doLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role     = rbStudent.isSelected() ? "Student"
                        : rbAdmin.isSelected()   ? "Admin"
                        : "Technician";

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object user = AuthController.login(email, password, role);

        if (user == null) {
            JOptionPane.showMessageDialog(this,
                "Invalid credentials. Only registered users can access.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setVisible(false);

        if (user instanceof Student) {
            new StudentDashboard((Student) user).setVisible(true);
        } else if (user instanceof Admin) {
            new AdminDashboard((Admin) user).setVisible(true);
        } else if (user instanceof Technician) {
            new TechnicianDashboard((Technician) user).setVisible(true);
        }
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 210)),
            new EmptyBorder(6, 8, 6, 8)));
        f.setPreferredSize(new Dimension(340, 36));
    }
}