# 📅 Schedulist

> A smart, student-focused Android app for tasks, events, and schedules — all in one place.  
Schedulist reduces the cognitive load of using multiple apps by integrating with Google Calendar and Google Tasks, Firebase Cloud Firestore for data storage and Google Identity Services for secure login.

---

## Features

- 📝 **Task Management**  
  - Create, edit, and delete tasks with categories and lists.  
  - Integrated with **Google Tasks API**.  
  - Multiple task views: Daily, Weekly, Monthly, Simple List.  

- 📆 **Event Management**  
  - Sync with **Google Calendar API**.  
  - Unified view of academic and personal events.  

- ⚙️ **Customisable Settings**  
  - Dark / Light Mode support.  
  - Multi-language support (English & Afrikaans).  
  - Account management settings.  

- 🔐 Secure Authentication
  - Schedulist prioritizes security-first authentication:
  - Credential Manager – Provides a modern, unified way to request credentials on Android. This ensures a consistent and secure sign-in across devices.
  - Google Identity Services (GIS) – Handles OAuth2-based identity verification with Google accounts.
  - Firebase Authentication – Validates and persists sessions securely on the backend.
  - Firestore Integration – Stores user metadata (not raw credentials) for role management and personalization.

- 🔑 **How It Works**
  - When a user taps "Sign in with Google", the app uses CredentialManager to build a GetCredentialRequest with the server-side client ID.
  - Google Identity Services returns a Google ID Token credential.
  - The token is exchanged for a Firebase Auth Credential via GoogleAuthProvider.getCredential().
  - Firebase signs in the user and creates a session tied to their UID.

   - **Sign-Out**
   - On sign-out, the app clears all credential states with credentialManager.clearCredentialState() and calls firebaseAuth.signOut() to prevent lingering sessions.

- ☁️ **Cloud Integration**  
  - **Firebase Cloud Firestore** for storing tasks, events, and user data.  
  - Synchronised experience across devices.  

---

## 🖼️ App Previews

<img width="276" height="490" alt="image" src="https://github.com/user-attachments/assets/3aef2c77-9ca3-4773-935c-662560a82e7c" />
<img width="277" height="491" alt="image" src="https://github.com/user-attachments/assets/03c8962f-2a6a-4d2f-bb35-f0c9ca3d09c1" />
<img width="274" height="489" alt="image" src="https://github.com/user-attachments/assets/26d56163-43a9-466d-a11e-14ce77ccfc4d" />
<img width="275" height="489" alt="image" src="https://github.com/user-attachments/assets/64546082-812c-4a5d-928e-6479a84d532a" />
<img width="275" height="487" alt="image" src="https://github.com/user-attachments/assets/509be3e7-03d5-428c-b5ef-7669e5181723" />
<img width="277" height="489" alt="image" src="https://github.com/user-attachments/assets/671ee091-783c-4bc3-aa04-37c564ea888e" />
<img width="276" height="489" alt="image" src="https://github.com/user-attachments/assets/7d867af3-80d9-4917-851c-797272472c57" />
<img width="275" height="489" alt="image" src="https://github.com/user-attachments/assets/b46cced0-2d87-4b13-b1ad-63719c4a99e5" />
<img width="275" height="490" alt="image" src="https://github.com/user-attachments/assets/5ad8d23f-d6ca-4181-b715-fa032ffdea16"/>
  

📖 How to Use Schedulist
1. Login & Authentication

  - On launch, you’ll see the Login Screen.
  - You can sign in using your email & password, or quickly with Google Sign-In.
  - If you don’t have an account, tap “Don’t have an account?” to create one.

2. Dashboard Overview

  - After signing in, you’ll land on the Dashboard, which includes:
  - Tasks – Create, edit, and track personal tasks.
  - Calendar – View your synced events from Google Calendar.
  - Events – Add new events with title, description, location, and attachments.
  - Simple List – Create quick to-do lists without extra details.
  - Account – Manage your profile and authentication.
  - Settings – Configure app preferences.

3. Adding a New Event

  - Tap “Add New Event”.
  - Enter details:
  - Title
  - Description
  - Location
  - (Optional) Attach a Google Document
  - Set the date & time.
  - Tap Save Event to add it to your schedule.

4. Adding a New Task

  - Tap “Add New Task”.
  - Enter:
  - Title
  - Description
  - Priority/Energy level (Low, Medium, High)
  - Date & Time
  - Tap Save Task.
  - Tasks will appear in your Task List, where you can view or edit them.

5. Notifications

  - The Notifications Panel helps you stay on track.
  - Types of alerts include:
  - Task Reminders – Upcoming tasks.
  - Event Reminders – Calendar events.
  - Productivity Alerts – General productivity nudges.
  - Toggle notifications on/off in this panel.

6. Managing Tasks & Events

  - Tasks and events are displayed as cards with details like title, description, date, and location.
  - Tap View to open full details.
  - Use filters to organize tasks into lists for easier management.



### 📊 Dashboard
Quick-access cards: **Tasks, Calendar, Events, Simple List, Account, Settings**.

### ✅ Tasks
Organise with filters and lists. Create new tasks via a clean pop-up form.

### ⚙️ Settings
Options for:  
- Language preferences  
- Dark/Light theme  
- Notification toggles  
- Account settings  

---

## 🛠️ Tech Stack

- **Android (Kotlin/Java)**  
- **Firebase Cloud Firestore** – app data storage  
- **Google Identity Services** – secure user login  
- **Credential Manager API** – authentication handling  
- **Google Calendar API (Client Library)** – event management  
- **Google Tasks API (Client Library)** – task management  
- **Google Cloud Console** – scope configuration for Calendar & Tasks APIs  
- **Material Design Components** – UI/UX  

---

## 📋 Requirements

- Android 8.0 (API level 26) or higher  
- Google account for authentication & API access  
- Enabled APIs in **Google Cloud Console**:  
  - Google Calendar API  
  - Google Tasks API  

---

## ⚙️ Setup

1. Clone the repo:
     - git clone https://github.com/Deanjamesg/Repo-ScheduList.git
     - cd Repo-ScheduList

3. Launch Android Studio.

  - Choose “Open an existing project” and select the cloned Repo-ScheduList directory.
  - Wait for Gradle sync and dependency resolution.

4. Run the App

  - Connect an Android device or start an emulator (minimum API level 26+).
  - Build & run the app module.
  - On first launch, you’ll be prompted to log in (Google sign-in or email/password), after which you’ll be taken to the dashboard.

