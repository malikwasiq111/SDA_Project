To RUN 
open in terminal and execute the foloowing command.
java -cp bin Main

# 🖥️ IT Complaint Management System

> A Java Swing desktop application for managing IT-related complaints in a university environment.
> Built as part of **Software Design & Analysis — Assignment 3**

---

## 🔐 Test Credentials

> Copy-paste these directly into the login screen to test each role.

| Role | Email | Password |
|------|-------|----------|
| 👨‍🎓 Student | `wasiq@nu.edu` | `pass123` |
| 👨‍🎓 Student | `adil@nu.edu` | `pass123` |
| 🔧 Technician | `ali@tech.nu.edu` | `tech123` |
| 🔧 Technician | `hassan@tech.nu.edu` | `tech123` |
| 🛡️ Admin | `admin@nu.edu` | `admin123` |

---

## 📌 Project Info

| Field | Details |
|-------|---------|
| **Course** | Software Design & Analysis (SDA) |
| **Institution** | NUCES — FAST Peshawar |
| **Section** | BCS-4C |
| **Authors** | Muhammad Wasiq (24P-0679), Adil Ghaffar (24P-0647) |
| **Submitted To** | Sir Umer Haroon |
| **Language** | Java (JDK 11+) |
| **GUI Framework** | Java Swing |
| **Architecture** | MVC — Layered Monolith |
| **Storage** | In-Memory (no database required) |

---

## 🚀 How to Run

### Option 1 — VS Code (Recommended)

1. Install **JDK 21** → [Download here](https://www.oracle.com/java/technologies/downloads/#jdk21-windows)
2. Install **Extension Pack for Java** in VS Code (`Ctrl+Shift+X` → search it)
3. Open VS Code → `File` → `Open Folder` → select the `CMS` folder
4. In the Explorer panel, open `src/Main.java`
5. Click the **▶ Run** button that appears above the `main` method

### Option 2 — IntelliJ IDEA

1. Open IntelliJ → `File` → `Open` → select the `CMS` folder
2. Right-click the `src/` folder → `Mark Directory As` → `Sources Root`
3. Open `src/Main.java` → click the green **▶ Play** button at the top

### Option 3 — Command Line

```bash
# From inside the CMS/ folder
find src -name "*.java" > sources.txt
mkdir out
javac -sourcepath src -d out @sources.txt
java -cp out Main
```

---

## 📁 Project Structure

```
CMS/
├── src/
│   ├── Main.java                          ← Entry point
│   │
│   ├── models/                            ← Data classes (from Class Diagram)
│   │   ├── Student.java
│   │   ├── Admin.java
│   │   ├── Technician.java
│   │   ├── Complaint.java                 ← Includes State pattern logic
│   │   ├── Feedback.java
│   │   └── Report.java
│   │
│   ├── store/
│   │   └── DataStore.java                 ← Singleton — central data store
│   │
│   ├── observer/
│   │   ├── ComplaintObserver.java         ← Observer interface
│   │   └── NotificationService.java      ← Fires & persists notifications
│   │
│   ├── controllers/                       ← Business logic layer
│   │   ├── AuthController.java
│   │   ├── StudentController.java
│   │   ├── AdminController.java
│   │   └── TechnicianController.java
│   │
│   └── gui/                               ← Swing GUI layer
│       ├── LoginFrame.java
│       ├── StudentDashboard.java          ← 4 tabs
│       ├── AdminDashboard.java            ← 4 tabs
│       └── TechnicianDashboard.java       ← 2 tabs
│
└── README.md
```

---

## ✨ Features

### 👨‍🎓 Student
- Login with university email and password
- Submit a complaint with title, description, and issue type
- View all personal complaints with color-coded status indicators
- Provide feedback on resolved complaints
- Receive persistent notifications for every status change

### 🛡️ Admin
- View all complaints across the system with status filtering
- Assign complaints to available technicians
- Close resolved complaints
- Generate summary reports (total, submitted, assigned, resolved, closed)
- Manage user accounts — add and remove students and technicians

### 🔧 Technician
- View complaints assigned specifically to them
- Mark complaints as resolved
- Return complaints that cannot be resolved (with a reason)
- Send update messages to students regarding their complaint

---

## 🔄 Complaint Lifecycle

```
[Submitted] ──(Admin assigns)──► [Assigned] ──(Tech resolves)──► [Resolved]
                                                                       │
                                                            (Student gives feedback)
                                                                       │
                                                                       ▼
                                                             [Feedback_Given]
                                                                       │
                                                            (Admin closes)
                                                                       │
                                                                       ▼
                                                                  [Closed]
```

Status is color-coded in the Student dashboard:

| Status | Color |
|--------|-------|
| Submitted | 🔵 Blue |
| Assigned | 🟠 Orange |
| Resolved | 🟢 Green |
| Feedback Given | 🟢 Light Green |
| Closed | ⚪ Grey |

---

## 🏗️ Design Patterns Used

### 1. Singleton — `DataStore.java`
Ensures a single shared data source across all dashboards and controllers.
Without it, each screen would hold separate data that never syncs.

```java
public static DataStore getInstance() {
    if (instance == null) instance = new DataStore();
    return instance;
}
```

### 2. Observer — `NotificationService.java` + `ComplaintObserver.java`
Automatically notifies the student whenever their complaint status changes.
Notifications are persisted in `DataStore` so they survive between login sessions.

```java
// StudentDashboard implements ComplaintObserver
void onStatusChanged(int complaintId, String newStatus, String message);
```

### 3. State — `Complaint.java`
Enforces legal complaint status transitions only.
Prevents invalid moves like jumping from Submitted directly to Closed.

```java
public boolean canTransitionTo(String newStatus) {
    switch (this.status) {
        case "Submitted":      return newStatus.equals("Assigned");
        case "Assigned":       return newStatus.equals("Resolved");
        case "Resolved":       return newStatus.equals("Feedback_Given") || newStatus.equals("Closed");
        case "Feedback_Given": return newStatus.equals("Closed");
        default:               return false;
    }
}
```

---

## 🗂️ Architecture Overview

```
┌──────────────────────────────┐
│        GUI Layer             │  LoginFrame, StudentDashboard,
│     (Java Swing)             │  AdminDashboard, TechnicianDashboard
└──────────────┬───────────────┘
               │ calls
┌──────────────▼───────────────┐
│     Controller Layer         │  AuthController, StudentController,
│   (Business Logic)           │  AdminController, TechnicianController
└──────────────┬───────────────┘
               │ reads / writes
┌──────────────▼───────────────┐
│    Model + Data Layer        │  Complaint, Student, Admin, Technician,
│  (DataStore Singleton)       │  Feedback, Report + in-memory storage
└──────────────────────────────┘
```

---

## ⚠️ Error Handling

The application handles the following errors with user-friendly dialog boxes:

| Scenario | Message Shown |
|----------|--------------|
| Wrong login credentials | "Invalid credentials. Only registered users can access." |
| Empty form fields | "Please fill all required fields." |
| Assigning without selecting technician | "Please select a technician to assign." |
| Feedback on non-resolved complaint | "Feedback only allowed on Resolved complaints." |
| No row selected in table | "Please select a complaint first." |
| Closing already closed complaint | "This complaint is already closed." |
| Adding duplicate email | "Email already exists." |

---

## 📋 Pre-Loaded Sample Data

The system starts with 4 sample complaints covering all statuses so you can test every feature immediately without setting anything up.

| ID | Title | Status | Student | Technician |
|----|-------|--------|---------|------------|
| 1 | PC not turning on | Submitted | Wasiq | — |
| 2 | WiFi not working | Assigned | Wasiq | Tech Ali |
| 3 | Software crash | Resolved | Adil | Tech Hassan |
| 4 | Projector broken | Feedback Given | Adil | Tech Ali |

---

## 📽️ Demo Video

> *[Link to demo video — add after recording]*

---

## 📄 License

This project was developed for academic purposes at NUCES FAST Peshawar.
Not intended for production use.
