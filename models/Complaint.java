package models;

public class Complaint {
    private int id;
    private String title;
    private String description;
    private String type;
    private String status;
    private String submitDate;
    private String updateMessage;
    private int studentRoll;
    private int technicianId; // 0 = unassigned

    public Complaint(int id, String title, String description, String type,
                     String status, String submitDate, int studentRoll) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.submitDate = submitDate;
        this.studentRoll = studentRoll;
        this.technicianId = 0;
        this.updateMessage = "";
    }

    // State Pattern: enforces legal transitions only
    public boolean canTransitionTo(String newStatus) {
        switch (this.status) {
            case "Submitted":     return newStatus.equals("Assigned");
            case "Assigned":      return newStatus.equals("Resolved");
            case "Resolved":      return newStatus.equals("Feedback_Given") || newStatus.equals("Closed");
            case "Feedback_Given":return newStatus.equals("Closed");
            default:              return false;
        }
    }

    public boolean updateStatus(String newStatus) {
        if (canTransitionTo(newStatus)) {
            this.status = newStatus;
            return true;
        }
        return false;
    }

    public void addIssueDetails(String details) {
        this.description = this.description + "\n[Details]: " + details;
    }

    // Getters
    public int getId()              { return id; }
    public String getTitle()        { return title; }
    public String getDescription()  { return description; }
    public String getType()         { return type; }
    public String getStatus()       { return status; }
    public String getSubmitDate()   { return submitDate; }
    public String getUpdateMessage(){ return updateMessage; }
    public int getStudentRoll()     { return studentRoll; }
    public int getTechnicianId()    { return technicianId; }

    // Setters
    public void setTechnicianId(int technicianId) { this.technicianId = technicianId; }
    public void setUpdateMessage(String msg)       { this.updateMessage = msg; }
    public void setStatus(String status)           { this.status = status; }

    @Override
    public String toString() { return "[#" + id + "] " + title + " — " + status; }
}