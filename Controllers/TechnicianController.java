package controllers;

import models.Complaint;
import observer.NotificationService;
import store.DataStore;

import java.util.List;

public class TechnicianController {

    private static final DataStore db = DataStore.getInstance();

    public static List<Complaint> getAssignedComplaints(int techId) {
        return db.getComplaintsByTechnician(techId);
    }

    public static boolean resolveComplaint(int complaintId) {
        Complaint c = db.getComplaintById(complaintId);
        if (c == null || !c.getStatus().equals("Assigned")) return false;

        c.updateStatus("Resolved");
        NotificationService.notifyObservers(complaintId, "Resolved",
                "Complaint #" + complaintId + " \"" + c.getTitle() + "\" has been resolved.");
        return true;
    }

    public static boolean returnComplaint(int complaintId, String reason) {
        Complaint c = db.getComplaintById(complaintId);
        if (c == null) return false;

        c.setStatus("Submitted");
        c.setTechnicianId(0);
        c.setUpdateMessage("Cannot resolve: " + reason);
        NotificationService.notifyObservers(complaintId, "Submitted",
                "Complaint #" + complaintId + " returned: " + reason);
        return true;
    }

    public static void sendUpdate(int complaintId, String message) {
        Complaint c = db.getComplaintById(complaintId);
        if (c != null) {
            c.setUpdateMessage(message);
            NotificationService.notifyObservers(complaintId, c.getStatus(),
                    "Update on complaint #" + complaintId + ": " + message);
        }
    }
}