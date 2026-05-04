package observer;

public interface ComplaintObserver {
    void onStatusChanged(int complaintId, String newStatus, String message);
}