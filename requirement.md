# EduTrack ERP + AI University Management System - Requirement & Logic Blueprint

This document serves as the master requirement specification for the **EduTrack** system. It outlines the architecture, database schema, and complex business logic for all roles (Admin, Teacher, Student).

---

## 🏗️ 1. Project Architecture & Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Modern Material 3)
*   **Architecture Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture
*   **Backend:** Firebase (Authentication, Firestore, Storage)
*   **Dependency Injection:** Hilt
*   **Persistence:** DataStore (Preferences)

### Folder Structure (`com.example.edutrack`)
*   `data/`: Repositories, Models, Firebase service classes.
*   `ui/`: Organized by feature area (`auth`, `admin`, `teacher`, `student`, `shared`).
*   `utils/`: Helper functions, constants, formatting tools.
*   `navigation/`: NavHost and Screen definitions.

---

## 🔐 2. Authentication & Role System

### Login Gateway
The login screen features a 3-dot menu in the top-right corner to switch roles:
1.  **Student Login** (Default)
2.  **Teacher Login**
3.  **Admin Login**

### Admin Authentication (Special Case)
*   **Single User:** Only one admin account exists.
*   **Credentials:** email `niteshdwivedi942@gmail.com` OR mobile `8707726234`.
*   **Security:** OTP-based login for the admin.

### Teacher Authentication
*   **Credentials:** Teacher ID + Password.
*   **Default Password:** `Teacher@54`.
*   **Password Reset:** Requires Date of Birth (DOB) verification.

### Student Authentication
*   **Credentials:** Registration Number + Password.
*   **Feature:** Standard Forgot/Reset password flow.

---

## 🏛️ 3. Admin Dashboard & Management

### Dashboard Layout
*   Rounded quick-access icons.
*   Grid: 5 icons per row.
*   Modules: Students, Teachers, Sections, Timetable, Results, Attendance, Subjects, Exams, Notifications, Holidays, Makeup Classes, Fees, Placements, Analytics, Settings, Rooms, Notes, Assignments, Teacher Leaves, AI Analytics.

### Teacher Management
*   **Fields:** teacherId, name, DOB (Mandatory), phone, email, department, assigned subjects/sections, status (Active/Inactive).
*   **History:** List of all created teachers with audit trail.

### Section Management
*   **Fields:** sectionId (e.g., K23BD), department, semester, strength, mentorTeacherId.

### Room Management
*   **Fields:** roomId, type (Lecture/Lab/Seminar), capacity, floor, status.

---

## 📅 4. Timetable Engine (Core Logic)

### Weekly Repeating Logic
*   The timetable follows a weekly cycle (Monday to Sunday).
*   Classes created for "Monday" appear every Monday until the `effectiveTo` date.

### Conflict Detection (Smart Validation)
Before saving any class, the engine checks:
1.  **Teacher Conflict:** A teacher cannot be assigned to two different classes at the same time.
2.  **Room Conflict:** One room cannot host two different classes at the same time.
3.  **Section Conflict:** One section cannot have two different subjects at the same time.
*   *UI:* Error messages must explicitly state which conflict occurred.

---

## 📈 5. Result & GPA Engine

### Grading Scale (Modified)
*   **90-100:** O (10) - Outstanding
*   **80-89:** A+ (9) - Excellent
*   **70-79:** A (8) - Very Good
*   **60-69:** B+ (7) - Good
*   **50-59:** B (6) - Above Average
*   **41-50:** C (5) - Average
*   **40:** Pass (Exactly 40 marks)
*   **35-39:** D (Grace Pass)
*   **Below 35:** R (Reappear) - Up to 5 attempts.
*   **F:** Backlog (After 5 failed R attempts).

### Grace Marks Logic
*   If a student scores **35-39**, the system adds **grace marks** to make it exactly **40**.
*   **Restriction:** Maximum of **2 subjects** per semester can receive grace marks.

### Calculation Formulas
*   **TGPA:** `Σ(Subject_Credits * Grade_Points) / Σ(Semester_Credits)`
*   **CGPA:** `Σ(All_Semester_Total_Credit_Points) / Σ(Total_Credits_Overall)`

---

## 👨‍🏫 6. Teacher Features

### Makeup Class System
*   Teachers can schedule extra classes for one or more sections.
*   **Validation:** System checks Teacher/Room/Section availability for the requested slot.
*   **Free Slot View:** Teachers can see available time slots before booking.

### Leave Management
*   Teachers apply for leave (Date, Start Time, End Time, Reason).
*   **Automation:** When a leave is approved, the system automatically marks all corresponding student classes for that teacher as **"CLASS CANCELLED"**.

### Content Upload
*   Upload PDFs, DOCs, PPTs, and Images.
*   Organized by **Section** and **Subject**.

---

## ☁️ 7. Firestore Database Schema

1.  `users`: Global role mapping (uid -> role).
2.  `students`: Student profiles, section, contact.
3.  `teachers`: Teacher profiles, assignments.
4.  `admins`: Admin details.
5.  `sections`: Section metadata.
6.  `subjects`: Subject details and credits.
7.  `timetables`: Weekly repeating schedule data.
8.  `attendance`: Daily attendance logs.
9.  `results`: Subject-wise marks and grades.
10. `notes`: File metadata and storage links.
11. `assignments`: Task details and student submissions.
12. `exams`: Schedules and venues.
13. `rooms`: Infrastructure availability.
14. `notifications`: System-wide alerts.
15. `teacher_leaves`: Leave requests and status.
16. `makeup_classes`: Extra class schedules.
17. `cgpa_records`: Semester-wise GPA summaries.

---

## 🚀 8. Implementation Steps

1.  **Foundation:** Setup project structure, Navigation Compose, Hilt DI, and Auth repositories.
2.  **Auth Flow:** Implement Login (3-role switcher) and Admin OTP logic.
3.  **Core Dashboards:** Build Admin and Teacher dashboard grid layouts with icons.
4.  **Admin Management:** Build CRUD for Teachers, Sections, and Rooms.
5.  **Timetable Foundation:** Implement the conflict check logic and schedule creation.
6.  **Teacher Core:** Makeup class scheduling and Leave system (with auto-cancellation logic).
7.  **Result Engine:** Build the search-by-regNum profile and Semester-card grading system.
8.  **Student UI:** Update existing Student screens to match the new ERP architecture.
9.  **AI & Analytics:** Integrate AI analytics and advanced report generation.

---
**Status:** Requirement Approved
**Next Step:** Implementation of Authentication Flow & Dashboard Structure.
