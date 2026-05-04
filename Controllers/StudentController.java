package controllers;

import models.*;
import observer.NotificationService;
import store.DataStore;

import java.time.LocalDate;
import java.util.List;

public class StudentController {

    private static final DataStore db = DataStore.getInstance();

    public static Complaint submitComplaint(String title, String description,
                                            String type, int studentRoll) {
        int id = db.getNextComplaintId();
        String date = LocalDate.now().toString();
        Complaint c = new Complaint(id, title, description, type, "Submitted", date, studentRoll);
        db.addComplaint(c);
        NotificationService.notifyObservers(id, "Submitted",
                "Complaint #" + id + " \"" + title + "\" submitted successfully.");
        return c;
    }

    public static List<Complaint> getMyComplaints(int studentRoll) {
        return db.getComplaintsByStudent(studentRoll);
    }

    public static List<Complaint> getResolvedComplaints(int studentRoll) {
        List<Complaint> mine = db.getComplaintsByStudent(studentRoll);
        mine.removeIf(c -> !c.getStatus().equals("Resolved"));
        return mine;
    }

    public static boolean submitFeedback(int complaintId, int studentRoll, String message) {
        Complaint c = db.getComplaintById(complaintId);
        if (c == null || !c.getStatus().equals("Resolved")) return false;

        Feedback f = new Feedback(db.getNextFeedbackId(), complaintId, studentRoll, message);
        db.addFeedback(f);
        c.updateStatus("Feedback_Given");
        NotificationService.notifyObservers(complaintId, "Feedback_Given",
                "Feedback submitted for complaint #" + complaintId + ".");
        return true;
    }
}