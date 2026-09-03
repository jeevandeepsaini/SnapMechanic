<h1 align="center">SnapMechanic</h1>
<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?logo=android"/>
  <img src="https://img.shields.io/badge/Code-Kotlin-purple?logo=kotlin"/>
  <img src="https://img.shields.io/badge/Layout-XML-purple?logo=xml"/>
  <img src="https://img.shields.io/badge/Backend-Firebase-yellow?logo=firebase"/>
  <img src="https://img.shields.io/badge/API-mockapi.io-orange"/>
  <img src="https://img.shields.io/badge/Libraries-Retrofit | OkHttp | Gson | Glide-blue"/>
  <img src="https://img.shields.io/github/last-commit/jeevandeepsaini/SnapMechanic"/>
</p>

## 🚀 Overview
A production-ready Android app that lets users browse mechanics, view details, and request vehicle services.

## ✨ Key Features
- [x] **Home Screen:** Paginated list (5 per page), search filter, pull-to-refresh
- [x] **Garage Detail:** Full info, service chips, call button, working hours
- [x] **Request Service:** Date/time picker, multi-select services, working hours validation
- [x] **Booking Confirmation:** Custom dialog with full booking summary
- [x] **Profile Screen:** View/edit profile, bookings history, change password
- [x] **UI Polish:** Custom themed dialogs, car make/model dynamic dropdowns
- [x] **Firebase Auth:** Login, signup, forgot password, logout, delete account
- [x] **Firestore:** User profiles and bookings stored securely
- [x] **Error Handling:** Loading states, network errors, retry button
- [x] **Unit Tests:** TimeValidator logic tested

## 📸 Screenshots
<table>
<tr>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/splashScreen.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/loginScreen.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/signUpScreen.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/resetPasswordDialog.jpg" width="250" /></td>
</tr>
<tr>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/homeScreen.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/garageDetail.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/requestService1.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/requestService2.jpg" width="250" /></td>
</tr>
<tr>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/bookingConfirmedDialog.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/profileScreen.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/updateProfile.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/myBookings.jpg" width="250" /></td>
</tr>
<tr>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/changePassword.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/helpSupportScreen.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/deleteAccountDialog.jpg" width="250" /></td>
   <td><img src="https://github.com/jeevandeepsaini/SnapMechanic/blob/main/appScreenshots/logoutDialog.jpg" width="250" /></td>
</tr>
</table>

## 🔧 Tech Stack
| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | XML Layouts + ViewBinding |
| Navigation | Activity-based |
| Networking | Retrofit 2 + Gson |
| Auth | Firebase Authentication |
| Database | Firestore (Cloud NoSQL) |
| Image Loading | Glide |
| Pagination | Manual scroll listener |
| Design | Dark theme <img src="https://img.shields.io/badge/____-121212"/> <img src="https://img.shields.io/badge/____-FF6B35"/> |

## 🏗️  Project Architecture
SnapMechanic follows a **simple three-layer architecture**
```
UI (Activities)  →  Repository (Data Layer)  →  API / Firebase
```
### How it works

**UI Layer: Activities** own all screen logic. They call repositories to fetch or mutate data and update views directly. Navigation is activity-based, keeping things explicit and easy to trace.

**Repository Layer** is where all data operations live. Each repository is responsible for one domain:
- `GarageRepository`: fetches, searches, and paginates garages via Retrofit
- `AuthRepository`: handles login, signup, logout, and password changes via Firebase Auth
- `UserRepository`: reads and writes user profiles and booking history to Firestore

**`Result<T>` sealed class** is the single source of truth for state. Every repository call returns one of three states - `Loading`, `Success`, or `Error` - so activities always know exactly what to show.

**Data Sources** sit at the bottom and are never touched directly by the UI:
- **MockAPI via Retrofit + Gson**: serves the 20-garage dataset with pagination support
- **Firebase Authentication**: email/password sign-in, signup, logout, and delete account
- **Firestore**: NoSQL storage for user profiles and bookings, with per-user security rules

### Why this approach
The architecture was chosen to keep the codebase readable and navigable without framework overhead. Every data flow can be traced in a straight line: Activity calls Repository, Repository calls API or Firebase, result flows back as `Result<T>`.

## 📂 Project Structure
```
app/src/main/java/com/snapmechanic/app/
├── adapter/                      # RecyclerView adapters
│   ├── GarageAdapter.kt
│   └── BookingAdapter.kt
├── model/                        # Data classes
│   ├── Garage.kt
│   ├── User.kt
│   └── Booking.kt
├── network/                      # Retrofit API client
│   ├── ApiClient.kt
│   └── GarageApi.kt
├── repository/                   # Data layer (Retrofit + Firebase)
│   ├── GarageRepository.kt
│   ├── AuthRepository.kt
│   └── UserRepository.kt
├── ui/                           # All Activities
│   ├── SplashActivity.kt
│   ├── LoginActivity.kt
│   ├── SignupActivity.kt
│   ├── HomeActivity.kt
│   ├── GarageDetailActivity.kt
│   ├── RequestServiceActivity.kt
│   ├── ProfileActivity.kt
│   ├── UpdateProfileActivity.kt
│   ├── MyBookingsActivity.kt
│   ├── ChangePasswordActivity.kt
│   └── HelpSupportActivity.kt
└── utils/
    ├── Constants.kt
    ├── Extensions.kt
    ├── Result.kt
    └── TimeValidator.kt
```

## 🖥️ Set-Up Instructions
### Prerequisites
- Android Studio
- Android SDK
- Java 11+
- A Firebase project

### Step 1: Clone the project
```bash
git clone https://github.com/jeevandeepsaini/SnapMechanic.git
cd SnapMechanic
```

### Step 2: Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a project named `snapmechanic`
3. Add an Android app with package `com.snapmechanic.app`
4. Download `google-services.json` and place it in `app/`
5. Enable **Email/Password** in Authentication → Sign-in Methods
6. Enable **Firestore** → Start in test mode

### Step 3: Firestore Rules (for development)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    match /bookings/{bookingId} {
      allow read: if request.auth.uid == resource.data.userId;
      allow write: if request.auth != null;
    }
  }
}
```

### Step 4: Build & Run
```bash
./gradlew assembleDebug
```
Or press **Run ▶** in Android Studio.

## ⚙️ API Detail
> 🔗 **View Live API**: [Click here to browse the data](https://6a96f1f70e3240db906192f2.mockapi.io/garages/garages)
- **Base URL**: `https://6a96f1f70e3240db906192f2.mockapi.io/garages/`
- **Endpoint**: `GET /garages?page={page}&limit={limit}`
- **Single Garage**: `GET /garages/{id}`
- **Data**: 20 garage entries with name, rating, distance, services, working hours

## 🧪 Running Tests
```bash
./gradlew test
```
Tests are in `app/src/test/java/com/snapmechanic/app/utils/TimeValidatorTest.kt`

## 📜 License
**© 2026 Jeevandeep Saini. All rights reserved.** 

This project was developed and submitted as part of an internship assessment. AI-assisted development tools were used during the development process.

This repository is provided for the purpose of evaluating the submission. No license or permission is granted to copy, modify, distribute, reproduce, or reuse the code or substantial portions of this project without explicit written permission from the author, except as otherwise required by applicable law or the terms of the assessment.

## 👤 Author
**[Jeevandeep Saini](https://github.com/jeevandeepsaini)**
