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
# 📦 **Release Notes**
## 🧪 **Prototype – Part 2**

The early prototype established the core foundation of Schedulist, introducing the initial UI and basic functionality needed for testing and validation.

## ✔️ **Key Highlights**

Initial UI completed and fully functional for core navigation.

Event Management working, allowing users to create and view events.

Task Management functional with basic task creation and editing.

Seeded data used to simulate real user interactions.

Connected to Firebase for storing early test data and validating backend connectivity

## ⚙️ 🚀 **Official Release – Part 3**

The full release delivers complete functionality, enhanced UI/UX, real API integration, and improved reliability. This version is designed for real-world daily productivity across devices.

## ✨ New & Improved Features

## 🔐 Biometric Authentication
Added fingerprint/biometric login for seamless and secure access.

##🌐 Google API Integration

Google Calendar API for event syncing.

Google Tasks API for task syncing.
Provides real-time connectivity with the user's Google ecosystem that they have set up.

## 🎨 Preferences & Personalisation

Dark/Light mode

Multi-language support (English & Afrikaans)

Configurable notification and display settings

## 📴 Offline Functionality
App now works offline with automatic resync when connectivity returns.

## 🔔 Real-Time Notifications
Smart reminders for tasks and events.

## 🖥️ UI & UX Enhancements
Polished layout, updated components, smoother transitions, and improved card-based design for easier navigation.

Changed the colour scheme of the applicaiton to be something more friendly on the eyes.
<div align="center"> <img width="260" src="https://github.com/user-attachments/assets/0873c944-f1a8-4749-b6d1-e62f5f798d5b" /> <img width="260" src="https://github.com/user-attachments/assets/3d2097d6-6848-4bf5-a5ad-01386c910aa0" /> <img width="260" src="https://github.com/user-attachments/assets/037ab150-393d-42c2-acff-1308ff0ae796" /> <br/> <img width="260" src="https://github.com/user-attachments/assets/deeeb03e-d74d-45e7-abd4-db2435ceb4bd" /> <img width="260" src="https://github.com/user-attachments/assets/f688faf1-858c-4233-9ccf-e25a7a47584c" /> <img width="260" src="https://github.com/user-attachments/assets/a12b3a7d-c00c-40b7-ab7c-b9116c760790" /> <br/> <img width="260" src="https://github.com/user-attachments/assets/7fe9fe8f-8893-493f-b79c-ecda846d0bee" /> <img width="260" src="https://github.com/user-attachments/assets/1cd64163-a882-49e2-8856-1753c09f9e92" /> </div>





# AI Tool Usage and Contribution Report: ScheduList

1. Introduction and Context

During the development of the ScheduList mobile application, specific generative AI tools were utilized to enhance debugging efficiency, confirm architectural best practices, and automate repetitive code structures. These tools were primarily employed as sophisticated research assistants, accelerating complex problem-solving without replacing original coding effort. All outputs were subject to rigorous validation by the developer before integration.

2. Specific Scenarios and Contribution

The following key areas benefited from AI assistance, focusing on reducing debugging time and ensuring adherence to professional Android architectural standards (MVVM—Model-View-ViewModel):

A. Multi-Language Support (Localization)

The project required multi-language support (English and Afrikaans). While the developer wrote all the UI logic, AI was used to confirm that the resource keys were applied consistently across multiple files and to perform initial translation drafts.

Prompt Example: "Please cross-check this list of hardcoded strings against my strings.xml and suggest the equivalent Afrikaans translations for 'Travel Time Alerts', 'Task Reminders', and 'Productivity Alerts' for the Notification screen."

Contribution: AI provided validated string resource keys (@string/notification_travel_time, @string/notification_productivity_alerts) and supplied the initial Afrikaans translation drafts, which were then reviewed and finalized by a human collaborator.

B. Architectural Boilerplate (MVVM)

Implementing the MVVM pattern requires creating boilerplate classes (Repository, ViewModel, Factory, and Adapter) for every feature (Tasks, Events, Calendar). AI was instrumental in scaffolding these classes quickly.

Prompt Example: "Write the full Kotlin class for EventsViewModelFactory that accepts an EventsRepository in its constructor, following the ViewModelProvider.Factory pattern."

Contribution: AI generated the necessary template code for the Factory classes, ensuring dependency injection was set up correctly. This saved several hours that would have been spent writing repetitive constructor and type-checking logic, allowing the focus to shift immediately to the complex database queries.

C. Complex Logic Integration (The SimpleList Feature)

A core challenge was the SimpleListRepository.kt, which needed to combine asynchronous data from two separate repositories (TasksRepository and EventsRepository) into a single, chronologically sorted list.

Prompt Example: "In SimpleListRepository.kt, how can I safely combine the results of two different suspend functions (getTasks() and getEvents()) and map the resulting objects (Task and Event) into one common SimpleListItem format, ensuring the final list is sorted by date?"

Contribution: AI provided the model for safely handling and mapping disparate data types, offering solutions like the whenAllSuccess pattern (or equivalent structured concurrency) and the correct use of Kotlin's map and sortedBy functions, which were then adapted to the project's specific data models.

All generative AI assistance used in this project was provided by the Google Gemini model.

Reference:

Google. (2025). Gemini (Version 2.5) [Large Language Model]. Retrieved from [The specific URL where you accessed the Gemini interface].


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

---

### Video Link

[https://youtu.be/5Kmdqez9nB8](https://youtu.be/sG0ouu82lYI)

