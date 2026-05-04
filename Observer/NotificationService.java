package observer;

import store.DataStore;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private static final List<ComplaintObserver> observers = new ArrayList<>();

    public static void register(ComplaintObserver o) {
        if (!observers.contains(o)) observers.add(o);
    }

    public static void unregister(ComplaintObserver o) {
        observers.remove(o);
    }

    public static void notifyObservers(int complaintId, String newStatus, String message) {
        // Persist to DataStore so student sees it even after re-login
        var complaint = DataStore.getInstance().getComplaintById(complaintId);
        if (complaint != null)
            DataStore.getInstance().addNotification(complaint.getStudentRoll(), message);

        // Fire live observers if dashboard is currently open
        for (ComplaintObserver o : observers)
            o.onStatusChanged(complaintId, newStatus, message);
    }
}