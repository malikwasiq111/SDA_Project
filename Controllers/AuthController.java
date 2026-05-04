package controllers;

import models.*;
import store.DataStore;

public class AuthController {

    private static final DataStore db = DataStore.getInstance();

    /**
     * @param role "Student" | "Admin" | "Technician"
     * @return matched object or null
     */
    public static Object login(String email, String password, String role) {
        email = email.trim();
        switch (role) {
            case "Student": {
                Student s = db.findStudentByEmail(email);
                if (s != null && s.login(email, password)) return s;
                break;
            }
            case "Admin": {
                Admin a = db.findAdminByEmail(email);
                if (a != null && a.login(email, password)) return a;
                break;
            }
            case "Technician": {
                Technician t = db.findTechnicianByEmail(email);
                if (t != null && t.login(email, password)) return t;
                break;
            }
        }
        return null;
    }
}