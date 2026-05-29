# EduTrack Teacher Module - Technical Logic & Features

This file documents the features and logic implemented in the **Teacher Module**. It is updated in real-time as we code.

---

## 🤒 1. Attendance & Leave System
**Feature:** Instant Leave Application.

### Core Logic:
*   **Availability Sync:** When a teacher marks themselves "Absent" or takes leave, the system queries the global `timetable` for that specific teacher.
*   **Automatic Invalidation:** Every student belonging to the sections taught by that teacher sees an automated **"CLASS CANCELLED"** status in their timetable for the leave duration.

---

## ➕ 2. Makeup Class Engine
**Feature:** Cross-Section Makeup Scheduling.

### Core Logic:
*   **Timetable Overlap Check:** Before a teacher can book a makeup slot, the engine fetches the target section's regular timetable to ensure students don't already have a class.
*   **Combined Sections:** Logic allows a teacher to select multiple sections (e.g., K23BD + K23AI) to hold a joint makeup class in one room.

---

## 📚 3. Notes & Resource Center
**Feature:** Secure Content Upload.

### Core Logic:
*   **Metadata Association:** Uploaded files are tagged with `subjectId` and `sectionId`.
*   **Cloud Storage:** Uses Firebase Storage for file persistence and Firestore for metadata (file size, type, upload date).

---
**Last Updated:** Phase 2 Foundation
