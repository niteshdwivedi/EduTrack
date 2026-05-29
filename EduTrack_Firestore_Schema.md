# EduTrack - Firestore Collection & Field Guide

This document lists every collection and field name used in the **EduTrack** Firestore database. Use this guide to generate or import your data exactly as the app expects.

---

### **1. Collection: `students`**
*   **Purpose**: Stores student profiles and login credentials.
*   **Fields**:
    *   `registrationNumber` (String/Number): The primary ID for login (e.g., `12317648`).
    *   `password` (String/Number): Student password for login.
    *   `name` (String): Full name (e.g., `Nitesh Dwivedi`).
    *   `email` (String): Official university email.
    *   `rollNumber` (String): Unique roll number.
    *   `section` (String): The student's class section (e.g., `A`, `B`, `223BD`).
    *   `course` (String): Degree name (e.g., `B.Tech CSE`).
    *   `semester` (Number): Current semester (e.g., `6`).
    *   `phone` (String/Number): Contact mobile number.
    *   `university` (String): University name.
    *   `profilePictureUrl` (String): URL of the uploaded profile image.

---

### **2. Collection: `attendance`**
*   **Purpose**: Stores individual attendance records for calculating percentages.
*   **Fields**:
    *   `studentReg` (String/Number): Must match a `registrationNumber` in the `students` collection.
    *   `subject` (String): Name of the class (e.g., `Operating Systems`).
    *   `faculty` (String): Name of the teacher.
    *   `status` (String): Value must be either **`Present`** or **`Absent`**.
    *   `date` (String): Date of the class in **`dd-MM-yyyy`** format.
    *   `time` (String): Time of the class (e.g., `09:00 AM`).
    *   `type` (String): `Lecture` or `Practical`.
    *   `room` (String): Classroom ID.

---

### **3. Collection: `timetable`**
*   **Purpose**: Defines the weekly class schedule.
*   **Fields**:
    *   `classId` (String): Unique ID for the class slot.
    *   `subjectName` (String): Full name of the subject.
    *   `subjectCode` (String): University code (e.g., `CSE335`).
    *   `faculty` (String): Teacher's name.
    *   `day` (String): Value must be **`Monday`**, **`Tuesday`**, etc.
    *   `startTime` (String): Start time (e.g., `09:00 AM`).
    *   `endTime` (String): End time (e.g., `10:30 AM`).
    *   `room` (String): Classroom (e.g., `38-910`).
    *   `section` (String): (Optional) Only shows if student matches this section.

---

### **4. Collection: `exams`**
*   **Purpose**: Stores exam schedules and results.
*   **Fields**:
    *   `examId` (String): Unique ID for the exam.
    *   `subject` (String): Subject name.
    *   `subjectCode` (String): Subject code.
    *   `date` (String): Date of exam in **`dd-MM-yyyy`** format.
    *   `time` (String): Exam start time (e.g., `10:00 AM`).
    *   `venue` (String): Hall/Room number.
    *   `section` (String): (Optional) Section-specific exam.
    *   `completed` (Boolean): `true` moves the card to History; `false` keeps it in Upcoming.
    *   `resultStatus` (String): `Published`, `Pending`, or `Not Available`.
    *   `marks` (String): Score achieved (e.g., `85/100`).
    *   `grade` (String): Grade letter (e.g., `A+`).
    *   `attendance` (String): `Present` or `Absent`.

---

### **5. Collection: `notes`**
*   **Purpose**: Stores study materials and PDFs.
*   **Fields**:
    *   `noteId` (String): Unique ID.
    *   `title` (String): Name of the notes (e.g., `Unit 2 - OS Notes`).
    *   `subject` (String): Related subject.
    *   `type` (String): `PDF`, `Slides`, or `Practical`.
    *   `fileSize` (String): Size in MB (e.g., `15 MB`).
    *   `fileUrl` (String): Public link to the file.
    *   `date` (String): Upload date.

---

### **6. Collection: `teachers`**
*   **Purpose**: Tracks teacher attendance for automated class cancellations.
*   **Fields**:
    *   `teacherName` (String): Name exactly as it appears in the `timetable`.
    *   `status` (String): Set to **`Absent`** to trigger a "Cancelled" alert on the Dashboard.
    *   `subject` (String): The subject they were supposed to teach.
    *   `date` (String): Date of absence in **`dd-MM-yyyy`** format.
    *   `period` (String): (Optional) specific time slot.

---

### **7. Collection: `makeup_classes`**
*   **Purpose**: Stores special extra classes.
*   **Fields**:
    *   `subject` (String): Subject name.
    *   `teacher` (String): Teacher's name.
    *   `date` (String): Date in **`dd-MM-yyyy`** format.
    *   `time` (String): Time range (e.g., `02:00 PM - 03:30 PM`).
    *   `venue` (String): Room number.

---

**Crucial Format Rules**:
1. **Dates**: Always use `dd-MM-yyyy` (e.g., `25-05-2026`).
2. **Times**: Always include AM/PM (e.g., `10:00 AM`).
3. **Booleans**: Use true/false for `completed`.
