# EduTrack Admin Module - Technical Logic & Features

This file documents the features and logic implemented in the **Admin Module**. It is updated in real-time as we code.

---

## 📅 1. Smart Timetable Engine
**Feature:** Section-wise Hourly Scheduler (7 AM - 7 PM).

### Core Logic:
*   **Discovery Layer:** Before seeing slots, Admin sees a list of **Sections with Specializations**.
*   **Hourly Slot Generation:** The UI generates a list of 12 hourly cards. If data exists in Firestore for that hour, the card shows class details. If not, it shows "Empty Slot".
*   **Selective Persistence:** Empty slots are never pushed to Firestore. Only slots with valid data (Subject, Teacher, Room) are saved or updated.
*   **Intelligent Room Conflict Logic:** 
    *   The system checks the entire `timetable` collection for any other section having a class in the **same room** at the **same time**.
    *   If found, it triggers a blocking alert: *"Room [Number] is already occupied by another section!"*
*   **Bulk Deletion:** A "Delete Semester Schedule" button allows clearing the entire timetable for a specific section in one click.

---

## 🏫 2. Section & Specialization Management
**Feature:** Dynamic Section Creation and Multi-Select Control.

### Core Logic:
*   **Schema:** Sections are stored with fields: `sectionId`, `specialization`, and `semester`.
*   **Gesture Control:** Implemented **Long-Press** to activate multi-select mode.
*   **Bulk Operations:** Supports "Select All" and "Batch Delete" to remove multiple sections and their associated data from Firestore simultaneously.

---

## 👨‍🏫 3. Teacher Management
**Feature:** CRUD Operations with Persistence.

### Core Logic:
*   **Hilt-Powered Repository:** Uses `FirestoreRepository` to push `Teacher` objects to the backend.
*   **Automatic ID Mapping:** Generates a unique document ID based on the `teacherId` provided.
*   **Data Integrity:** Validates that mandatory fields (DOB, Department) are present before saving.

---
**Last Updated:** Phase 2 Foundation
