# EduTrack - Master Technical Viva Master Guide (CSE225)

This document is designed to help you ace your technical viva for the **EduTrack** project. It aligns implemented features with the **CSE225: Developing Android Apps** syllabus.

---

## 🎓 Syllabus Alignment (Unit-wise)

### Unit I: Views & Components in Jetpack Compose
**Q: How did you implement the Splash Screen?**
*   **Answer:** I used a `LaunchedEffect` in the `SplashScreen` composable. It handles the 1-second delay and the scale animation (0 to 1) for the graduation cap logo. It also triggers the logic to check if a user session exists in DataStore.

**Q: Where did you use Lists and Grids?**
*   **Answer:** I used `LazyColumn` for the Timetable and Attendance records to handle large datasets efficiently. I used `LazyRow` for the Quick Action icons on the Dashboard.

---

### Unit II: Managing Application Communication and Scheduling
**Q: What is the difference between Intent and PendingIntent in your project?**
*   **Answer:** 
    *   **Intent:** Used for immediate action, like navigating from the Dashboard to the Profile screen (`navController.navigate`).
    *   **PendingIntent:** Used for the **Exam Alarm system**. It’s a "wrapper" around an intent that I give to the Android `AlarmManager`. It allows the system to trigger the alarm even if the app is killed or the phone is in sleep mode.

**Q: Which Scheduler did you use for background tasks?**
*   **Answer:** I used **AlarmManager** for the Exam Alarms because they need to be triggered at an exact time. I also implemented **WorkManager** for the Assignment Reminder Worker, which is better for periodic, non-exact background tasks.

---

### Unit III: Notifications and User Interaction
**Q: How many Notification Channels did you use?**
*   **Answer:** I implemented **one primary notification channel** (ID: `edu_track_notifications`, Name: "EduTrack Reminders").
*   **Technical Detail:** I initialized this in `NotificationHelper.kt`. Since Android 8.0 (API 26), channels are mandatory to show notifications.

**Q: Why are Notification Channels important?**
*   **Answer:** They give the user control. If I had multiple channels (e.g., "Exam Alerts" and "News Updates"), the user could go to Android Settings and turn off news but keep the exam alerts active.

---

### Unit IV: Custom UI Components
**Q: How did you create the "Teacher Absent" Red Cards?**
*   **Answer:** I created a custom `ClassItem` composable. It uses a `Modifier.background` with a conditional color logic. If `isTeacherAbsent` is true, it applies `Color(0xFFFFEBEE)` and adds a "Cancelled" label. This is a fully declarative UI approach.

---

### Unit V: Modern Data Storage
**Q: What is Jetpack DataStore and why did you use it over SharedPreferences?**
*   **Answer:** I used **Preferences DataStore** to store the user's Registration Number (`12317648`) for persistent login.
*   **Why:** DataStore is built on **Kotlin Coroutines and Flow**. It handles data updates asynchronously and safely on a background thread, preventing UI jank or crashes that SharedPreferences might cause.

**Q: How do you connect the Auth system?**
*   **Answer:** I used **Firebase Firestore** as the backend. When a user enters their ID and password, the app performs a query on the `students` collection. I implemented **Multi-type matching** (searching for the ID as both a String and a Number) to ensure compatibility with different database formats.

---

### Unit VI: Advanced Navigation & Paging
**Q: Where did you use HorizontalPager?**
*   **Answer:** I used `HorizontalPager` for the **University News & Placement banner** and the **Exam Countdown banner** on the Dashboard. It allows users to swipe through different recruitment drives.

**Q: Explain your Navigation Drawer implementation.**
*   **Answer:** I used `ModalNavigationDrawer` with a `ModalDrawerSheet`. It contains the user's profile summary (fetched from Firestore) and links to all major modules like GPA Calc, Jobs, and Resources.

---

## ⚡ Core Technical Concepts (General Viva)

### Q: What are Coroutines and where are they used in EduTrack?
**Answer:** Coroutines are "lightweight threads" for asynchronous work. In this project, they are used in:
1.  **Firebase Requests:** Using `await()` to fetch data without blocking the main UI thread.
2.  **Search Algorithm:** Running the fuzzy-matching similarity logic in the background.
3.  **Timers:** Using a `while(true)` loop with `delay(1000)` to update the exam countdown every second.

### Q: Explain the Fuzzy Search logic.
**Answer:** I implemented a **Character-Sequence Similarity Algorithm**. It calculates the ratio of matching characters between the user's query and the screen name. If the match is > 30%, the result is shown. This handles spelling mistakes (e.g., "Atend" for "Attendance").

---

## 🚀 Beyond the Syllabus (Pro-Level Questions)

These questions cover modern industry standards that demonstrate you didn't just build an app, but you followed **Professional Android Architecture**.

### Q: What Architecture Pattern did you use in this project?
**Answer:** I used the **MVVM (Model-View-ViewModel)** pattern.
*   **Model:** Data classes in `Models.kt`.
*   **View:** Jetpack Compose screens.
*   **ViewModel:** Logic handlers (e.g., `DashboardViewModel`) that survive configuration changes (like screen rotation) and provide data to the UI using **StateFlow**.

### Q: What is Dependency Injection (DI) and why did you use Hilt?
**Answer:** DI is a technique where one object provides the dependencies of another object. I used **Dagger Hilt** to manage this.
*   **Why:** Instead of creating a new Firestore instance in every file, Hilt creates it once in `AppModule.kt` and "injects" it where needed. This makes the code modular, easier to test, and memory-efficient.

### Q: What is StateFlow and why not just use regular variables?
**Answer:** `StateFlow` is a state-holder observable flow. Regular variables don't tell the UI when to update. When a value in `StateFlow` changes (like the Exam Countdown), it automatically triggers a **Recomposition** in Compose, updating the screen instantly.

### Q: How did you handle Image Loading in the Profile section?
**Answer:** I used the **Coil (Coroutines Image Loader)** library. It is optimized for Kotlin Coroutines and handles image caching automatically, so the app doesn't have to re-download the profile picture every time the screen opens.

### Q: Explain the logic of your "Dark Mode" implementation.
**Answer:** I implemented it using **Material 3 Theming**. I defined a `DarkColorScheme` and `LightColorScheme` in `Theme.kt`. The app uses `isSystemInDarkTheme()` to detect the phone's setting and applies the correct colors globally. I also used a `SideEffect` to ensure the status bar icons change color to remain visible.

### Q: Why did you use Firebase Firestore instead of a local SQLite database?
**Answer:** Firestore is a **NoSQL Cloud Database**. It allows real-time data syncing across different devices. Since EduTrack needs to show the latest university news and teacher absences, a cloud database is better because I can update the data once in the Firebase Console, and it reflects for all students instantly.

---

## 🛠️ Behind the Scenes: APIs & Dependencies

These questions focus on the **tools** and **libraries** you integrated into the app.

### Q: Which external APIs did you use in this project?
**Answer:** I used two primary cloud APIs:
1.  **Firebase Firestore API:** For real-time NoSQL data storage (attendance, timetable, exams).
2.  **Google Gemini AI API:** Powering the "AI Assistant" feature to answer student academic queries.
3.  **Firebase Auth API:** For the secure user registration and login system.

### Q: Can you explain the major dependencies in your `build.gradle`?
**Answer:**
*   **Hilt (Dagger Hilt):** Used for **Dependency Injection**. It provides instances of Firestore/Auth throughout the app automatically.
*   **Coil (Compose Image Loader):** Used for fetching and displaying the user's **Profile Picture** from a URL or device gallery.
*   **Navigation Compose:** Used for managing **Single-Activity Architecture** and switching between screens.
*   **DataStore Preferences:** Used for **Persistent Storage** of the user's session (Registration Number).
*   **WorkManager:** Used for **Background Tasks** like checking for upcoming assignments even when the app is closed.
*   **Kotlinx Coroutines:** Used for **Asynchronous tasks** to prevent the UI from lagging during network calls.

### Q: Why did you use `libs.versions.toml` (Version Catalog)?
**Answer:** It is the modern Android standard for **Dependency Management**. Instead of hardcoding versions in every module, I defined them in a central catalog. This ensures version consistency across the entire project and makes updating libraries much easier.

---
**Project Status:** Syllabus CO1 - CO6 Fully Implemented
**Prepared by:** Nitesh Dwivedi & Team
