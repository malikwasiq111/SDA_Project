package store;

import models.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    private static DataStore instance;

    private List<Student>     students     = new ArrayList<>();
    private List<Admin>       admins       = new ArrayList<>();
    private List<Technician>  technicians  = new ArrayList<>();
    private List<Complaint>   complaints   = new ArrayList<>();
    private List<Feedback>    feedbacks    = new ArrayList<>();

    // studentRoll → list of notification messages
    private Map<Integer, List<String>> notifications = new HashMap<>();

    private int nextComplaintId = 5;
    private int nextFeedbackId  = 1;
    private int nextStudentId   = 3;
    private int nextTechId      = 3;

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    private void seedData() {
        // Students
        students.add(new Student(1, "Muhammad Wasiq", "wasiq@nu.edu",  "pass123"));
        students.add(new Student(2, "Adil Ghaffar",   "adil@nu.edu",   "pass123"));

        // Admins
        admins.add(new Admin(1, "Admin Sir", "admin@nu.edu", "admin123"));

        // Technicians
        technicians.add(new Technician(1, "Tech Ali",    "ali@tech.nu.edu",    "tech123"));
        technicians.add(new Technician(2, "Tech Hassan", "hassan@tech.nu.edu", "tech123"));

        // Pre-seeded complaints covering all statuses
        Complaint c1 = new Complaint(1, "PC not turning on",  "Desktop won't power on",        "Hardware", "Submitted",      "2025-04-01", 1);
        Complaint c2 = new Complaint(2, "WiFi not working",   "Cannot connect to campus WiFi", "Network",  "Assigned",       "2025-04-02", 1);
        Complaint c3 = new Complaint(3, "Software crash",     "IDE crashes on startup",        "Software", "Resolved",       "2025-04-03", 2);
        Complaint c4 = new Complaint(4, "Projector broken",   "Projector no display output",   "Hardware", "Feedback_Given", "2025-04-04", 2);

        c2.setTechnicianId(1);
        c3.setTechnicianId(2);
        c4.setTechnicianId(1);

        complaints.add(c1);
        complaints.add(c2);
        complaints.add(c3);
        complaints.add(c4);
    }

    // ── Complaint CRUD ─────────────────────────────────────────────────────────

    public void addComplaint(Complaint c) {
        complaints.add(c);
    }

    public List<Complaint> getAllComplaints() {
        return complaints;
    }

    public List<Complaint> getComplaintsByStudent(int studentRoll) {
        List<Complaint> result = new ArrayList<>();
        for (Complaint c : complaints)
            if (c.getStudentRoll() == studentRoll) result.add(c);
        return result;
    }

    public List<Complaint> getComplaintsByTechnician(int techId) {
        List<Complaint> result = new ArrayList<>();
        for (Complaint c : complaints)
            if (c.getTechnicianId() == techId) result.add(c);
        return result;
    }

    public List<Complaint> getComplaintsByStatus(String status) {
        List<Complaint> result = new ArrayList<>();
        for (Complaint c : complaints)
            if (c.getStatus().equals(status)) result.add(c);
        return result;
    }

    public Complaint getComplaintById(int id) {
        for (Complaint c : complaints)
            if (c.getId() == id) return c;
        return null;
    }

    public int getNextComplaintId() { return nextComplaintId++; }

    // ── Feedback CRUD ──────────────────────────────────────────────────────────

    public void addFeedback(Feedback f) { feedbacks.add(f); }

    public List<Feedback> getAllFeedbacks() { return feedbacks; }

    public Feedback getFeedbackByComplaint(int complaintId) {
        for (Feedback f : feedbacks)
            if (f.getComplaintId() == complaintId) return f;
        return null;
    }

    public int getNextFeedbackId() { return nextFeedbackId++; }

    // ── User Getters ───────────────────────────────────────────────────────────

    public List<Student>    getStudents()    { return students; }
    public List<Admin>      getAdmins()      { return admins; }
    public List<Technician> getTechnicians() { return technicians; }

    public Student findStudentByEmail(String email) {
        for (Student s : students)
            if (s.getEmail().equalsIgnoreCase(email)) return s;
        return null;
    }

    public Admin findAdminByEmail(String email) {
        for (Admin a : admins)
            if (a.getEmail().equalsIgnoreCase(email)) return a;
        return null;
    }

    public Technician findTechnicianByEmail(String email) {
        for (Technician t : technicians)
            if (t.getEmail().equalsIgnoreCase(email)) return t;
        return null;
    }

    public Technician getTechnicianById(int id) {
        for (Technician t : technicians)
            if (t.getID() == id) return t;
        return null;
    }

    public Student getStudentByRoll(int roll) {
        for (Student s : students)
            if (s.getRoll() == roll) return s;
        return null;
    }

    // ── Notifications ──────────────────────────────────────────────────────────

    public void addNotification(int studentRoll, String message) {
        notifications.computeIfAbsent(studentRoll, k -> new ArrayList<>()).add(message);
    }

    public List<String> getNotifications(int studentRoll) {
        return notifications.getOrDefault(studentRoll, new ArrayList<>());
    }

    public void clearNotifications(int studentRoll) {
        notifications.put(studentRoll, new ArrayList<>());
    }

    // ── User Management (Admin) ────────────────────────────────────────────────

    public boolean addStudent(String name, String email, String password) {
        if (findStudentByEmail(email) != null) return false; // duplicate
        students.add(new Student(nextStudentId++, name, email, password));
        return true;
    }

    public boolean addTechnician(String name, String email, String password) {
        if (findTechnicianByEmail(email) != null) return false;
        technicians.add(new Technician(nextTechId++, name, email, password));
        return true;
    }

    public boolean removeStudent(int roll) {
        return students.removeIf(s -> s.getRoll() == roll);
    }

    public boolean removeTechnician(int id) {
        return technicians.removeIf(t -> t.getID() == id);
    }

    public int getNextStudentId() { return nextStudentId; }
    public int getNextTechId()    { return nextTechId; }
}