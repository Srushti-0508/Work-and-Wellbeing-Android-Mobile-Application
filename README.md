# 📱 Productivity App (Android + Firebase)

A mobile productivity app built in **Java using Android Studio**, with full Firebase Firestore integration for authentication and real-time data management.

---

✨ Key Features

- 🔐 **User Authentication**
  - Sign up, login, and logout using Firebase Email & Password authentication
  - Send password reset links via email

- ✅ **Task Management**
  - Add, edit, and delete tasks
  - Mark tasks as completed — completed tasks move to a separate view
  - Track session counts per task (used for Pomodoro analytics)

- 🔁 **Habit Tracking**
  - Create daily habits with auto-reset every new day
  - Track daily completion
  - View habit completion progress on Home screen
  - Shows motivational message when all daily habits are completed

- ⏱️ **Pomodoro Timer**
  - Built-in 25-minute timer for focused work
  - Optional task selection during sessions
  - Session completion stored in SharedPreferences or linked to specific task
  - Sends a notification when session ends
  - Choose custom audio

- 📊 **Analytics**
  - Bar chart showing Pomodoro sessions per task
  - Pie chart comparing general sessions vs task-based sessions
  - Uses MPAndroidChart for visualization

---

## 🧩 App Architecture

The app is modularized into multiple fragments:

### 1. **HomeFragment**
- Displays daily summary of:
  - ✅ Completed tasks
  - 🔁 Completed habits

### 2. **TaskFragment**
- Shows active tasks
- Allows add/edit/delete
- Completed tasks are moved to CompletedTaskFragment

### 3. **CompletedTaskFragment**
- Displays tasks marked as completed
- Users can permanently delete them to declutter the task list

### 4. **HabitFragment**
- Similar structure to TaskFragment
- Habits reset to unchecked each new day
- Tracks daily completion and updates count in HomeFragment

### 5. **PomodoroFragment**
- Timer with 25-minute default
- Select audio
- Optional task selection
- Completion tracked

### 6. **AnalyticsFragment**
- Displays bar and pie charts using session data from Firestore and SharedPreferences

---

## 🔧 Built With

- **Java**
- **Android Studio**
- **Firebase Firestore** (for user data and documents)
- **Firebase Auth** (for authentication)
- **SharedPreferences** (for storing session data locally)
- **MPAndroidChart** (for graphs and charts)

---

## 🚀 Getting Started

1. Clone the repository  
   ```bash
   git clone https://github.com/your-username/your-repo-name.git
