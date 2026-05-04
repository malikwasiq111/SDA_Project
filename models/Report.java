package models;

import store.DataStore;
import java.util.List;

public class Report {
    private int reportId;
    private int totalComplaints;
    private int submittedComplaints;
    private int assignedComplaints;
    private int resolvedComplaints;
    private int closedComplaints;

    public Report(int reportId) {
        this.reportId = reportId;
    }

    public void generateReport() {
        List<Complaint> all = DataStore.getInstance().getAllComplaints();
        totalComplaints = all.size();
        submittedComplaints = 0;
        assignedComplaints  = 0;
        resolvedComplaints  = 0;
        closedComplaints    = 0;

        for (Complaint c : all) {
            switch (c.getStatus()) {
                case "Submitted":      submittedComplaints++; break;
                case "Assigned":       assignedComplaints++;  break;
                case "Resolved":
                case "Feedback_Given": resolvedComplaints++;  break;
                case "Closed":         closedComplaints++;    break;
            }
        }
    }

    public String displayReport() {
        return String.format(
            "Total: %d | Submitted: %d | Assigned: %d | Resolved: %d | Closed: %d",
            totalComplaints, submittedComplaints, assignedComplaints,
            resolvedComplaints, closedComplaints
        );
    }

    public int getReportId()           { return reportId; }
    public int getTotalComplaints()    { return totalComplaints; }
    public int getSubmittedComplaints(){ return submittedComplaints; }
    public int getAssignedComplaints() { return assignedComplaints; }
    public int getResolvedComplaints() { return resolvedComplaints; }
    public int getClosedComplaints()   { return closedComplaints; }
}