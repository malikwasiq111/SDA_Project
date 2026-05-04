package controllers;

import models.*;
import observer.NotificationService;
import store.DataStore;

import java.util.List;

public class AdminController {

    private static final DataStore db = DataStore.getInstance();

    public static List<Complaint> getAllComplaints() {
        return db.getAllComplaints();
    }

    public static List<Complaint> getComplaintsByStatus(String status) {
        if (status == null || status.equals("All")) return db.getAllComplaints();
        return db.getComplaintsByStatus(status);
    }

    public static boolean assignComplaint(int complaintId, int technicianId) {
        Complaint c = db.getComplaintById(complaintId);
        Technician t = db.getTechnicianById(technicianId);
        if (c == null || t == null) return false;
        if (!c.getStatus().equals("Submitted")) return false;

        c.setTechnicianId(technicianId);
        c.updateStatus("Assigned");
        NotificationService.notifyObservers(complaintId, "Assigned",
                "Complaint #" + complaintId + " assigned to " + t.getName() + ".");
        return true;
    }

    public static boolean closeComplaint(int complaintId) {
        Complaint c = db.getComplaintById(complaintId);
        if (c == null) return false;
        if (!c.canTransitionTo("Closed")) return false;

        c.updateStatus("Closed");
        NotificationService.notifyObservers(complaintId, "Closed",
                "Complaint #" + complaintId + " has been closed by Admin.");
        return true;
    }

    public static Report generateReport() {
        Report r = new Report(1);
        r.generateReport();
        return r;
    }

    public static List<Feedback> getAllFeedbacks() {
        return db.getAllFeedbacks();
    }

    public static List<Technician> getAllTechnicians() {
        return db.getTechnicians();
    }

    public static List<Student> getAllStudents() {
        return db.getStudents();
    }
}