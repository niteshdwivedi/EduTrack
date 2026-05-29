# EduTrack - Advanced Project Technical Logic & Feature Report

This document provides a deep dive into the technical implementation and logic behind every feature in the **EduTrack** application. Use this for highly detailed slides in your Project Presentation (PPT).

---

## 1. Professional Branding & System UI
*   **Premium Launcher Icon**:
    *   **Logic**: Replaced default Android assets with a custom vector (`ic_launcher_foreground` and `ic_launcher_background`). 
    *   **Design**: Uses a "Deep Charcoal" (#1A1A1A) hex code for a premium dark feel and a "Minimalist Graduation Cap" to represent academia.
*   **Animated Splash Gateway**:
    *   **Logic**: Implemented a `SplashViewModel` that interacts with **Jetpack DataStore**.
    *   **Automation**: The app checks the local preference key `registration_number`. If it exists and is not empty, it assumes an active session and triggers an immediate navigation to the Dashboard, bypassing the login screen for a 1-second "Instant-In" experience.
*   **Adaptive Status Bar (SideEffect)**:
    *   **Logic**: Uses Compose `SideEffect` to monitor the `MaterialTheme` color scheme.
    *   **System Call**: It programmatically tells the Android Window Manager to set `isAppearanceLightStatusBars = !darkTheme`. This ensures that if the app has a white background, the system icons (Time, Wi-Fi, Battery) turn dark so they don't "disappear."

## 2. Smart Search Engine (Fuzzy Logic)
*   **Feature**: Finding screens like "Attendance" even with typos (e.g., "Atend").
*   **The Algorithm**: Implemented a **Character-Sequence Similarity Algorithm**.
    *   **Step 1**: Converts both the search query and target name to lowercase.
    *   **Step 2**: Iterates through the target string and counts how many characters from the query appear in the correct relative order.
    *   **Step 3**: Calculates a score: `Matches / QueryLength`.
    *   **Threshold**: Only results with a score above **0.3 (30% similarity)** are displayed, ensuring the list stays relevant.

## 3. Intelligent Dashboard & Alerts
*   **User Personalization**:
    *   **Logic**: Upon login, the app saves the Registration Number. The `DashboardViewModel` then performs a **Multi-Cased Firestore Query**. It searches for the ID as a `String` and as a `Number` (Long) to ensure compatibility with different data upload formats.
*   **Urgent Alerts (Cross-Collection Logic)**:
    *   **Logic**: The app performs a real-time fetch from `teachers` (filtered by `status == 'Absent'`) and `makeup_classes`. 
    *   **Notification Badge**: The count is stored in a `StateFlow` and passed to a `Badge` component on the Dashboard icons, giving the student a "Red Dot" alert for urgent changes.
*   **Exam Countdown Timer**:
    *   **Logic**: Uses `SimpleDateFormat` to parse the `ExamDate` (dd-MM-yyyy). It runs a coroutine loop that updates every 60 seconds, calculating: `TargetMillis - System.currentTimeMillis()`. It then converts this into "Days" and "Hours" strings dynamically.

## 4. Smart Timetable & Attendance
*   **Live Class Monitoring**:
    *   **Logic**: While displaying the timetable, the app cross-references the list of Absent Teachers. If a match is found (Teacher Name + Date), the specific timetable card's `isTeacherAbsent` flag is set to `true`.
    *   **UI Response**: The card color changes to **Soft Red**, the icon changes to a "Cancel" sign, and a "Cancelled" label is appended.
*   **Attendance Percentage Calculation**:
    *   **Formula**: `(Sum of all 'Present' records / Total records found for Student ID) * 100`.
    *   **Database Logic**: Records are fetched and then grouped by the `subject` field using Kotlin's `.groupBy {}` function to show individual subject progress.

## 5. Exam Alarm & Reminder System
*   **Logic**: Uses the Android **`AlarmManager`** service.
*   **Precision**: We use `setExactAndAllowWhileIdle` to ensure the alarm rings even if the phone is in "Doze Mode" (battery saving).
*   **Trigger**: A `PendingIntent` is sent to an `ExamAlarmReceiver` (BroadcastReceiver), which then triggers a high-priority Notification and starts the custom ringtone.
*   **Persistence**: If the phone is switched off and on, the app uses a `BOOT_COMPLETED` receiver to automatically re-schedule any upcoming exam alarms.

## 6. Placement Portal & Job Intelligence
*   **Package Handling**:
    *   **Logic**: Since salaries can be stored as "32", "32 LPA", or 3200000, we created a helper property `packageOffered`. 
    *   **Normalization**: It uses `.toString()` and appends "LPA" (Lakhs Per Annum) only if missing, ensuring a clean and consistent UI regardless of data input format.
*   **Direct Apply Logic**: Each job card stores a URL. The app uses an `Intent(Intent.ACTION_VIEW)` to open the university's registration link in the phone's browser directly.

## 7. Results & GPA Calculation Algorithm
*   **Logic**: A strict `when` expression maps Grades (A+, A, B, etc.) to a `Double` point value (4.0, 3.7, etc.).
*   **The Math**:
    1.  `TotalPoints = sum(CourseCredits * GradePoints)`
    2.  `TotalCredits = sum(CourseCredits)`
    3.  `GPA = TotalPoints / TotalCredits`
*   **StateFlow**: This calculation happens "On-the-Fly." As soon as a user changes a grade in the calculator, the total GPA on the screen updates instantly without refreshing the page.

## 8. Profile & Image Processing
*   **Coil Image Engine**:
    *   **Logic**: Integrated the **Coil library** for asynchronous image loading. It handles image caching so that once your profile picture is loaded, it doesn't need to be downloaded again, saving mobile data.
*   **Image Picking**:
    *   **Logic**: Uses `rememberLauncherForActivityResult` with `GetContent()`. This opens the Android System File Picker safely and returns a `Uri`. The app then applies a `CircleShape` clip to the image for a professional look.

---
**Project Status**: All Logic Verified & Deployed
**Prepared By**: EduTrack Development Team
