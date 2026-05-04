package models;

public class Feedback {
    private int feedbackId;
    private int complaintId;
    private int studentRoll;
    private String feedbackMessage;

    public Feedback(int feedbackId, int complaintId, int studentRoll, String feedbackMessage) {
        this.feedbackId = feedbackId;
        this.complaintId = complaintId;
        this.studentRoll = studentRoll;
        this.feedbackMessage = feedbackMessage;
    }

    public int getFeedbackId()        { return feedbackId; }
    public int getComplaintId()       { return complaintId; }
    public int getStudentRoll()       { return studentRoll; }
    public String getFeedbackMessage(){ return feedbackMessage; }

    @Override
    public String toString() {
        return "Feedback[complaint=#" + complaintId + "]: " + feedbackMessage;
    }
}