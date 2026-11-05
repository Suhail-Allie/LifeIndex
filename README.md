# LifeIndex
## Track anything. Find everything.

![Android CI](https://github.com/Suhail-Allie/LifeIndex/actions/workflows/android.yml/badge.svg)

LifeIndex is an Android personal life-administration application that helps users organise important personal records, responsibilities, dates and supporting documents in one place.

Instead of using different applications to manage different aspects of life, LifeIndex uses a flexible tracker system that can represent personal items, ongoing processes and recurring responsibilities.

This project was developed as part of the **OPSC6312 – Open Source Coding Intermediate Portfolio of Evidence (Final POE – Part 3)**.

---

## Project Information

| Item | Details |
|---|---|
| Application | LifeIndex |
| Module | OPSC6312 – Open Source Coding Intermediate |
| Assessment | Final Portfolio of Evidence – Part 3 |
| Developer | Mogammad Suhail Allie |
| Student Number | ST10455559 |
| Year | 2026 |
| Package Name | `com.lifeindex.app` |
| Minimum Android Version | Android 8.0 (API 26) |
| Compile / Target SDK | 36 |
| Final Development Branch | `part3-final-poe` |
| Hosted API | https://lifeindex-api.onrender.com |

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Main Application Features](#main-application-features)
3. [What Was Added in Part 3](#what-was-added-in-part-3)
4. [Technology Stack](#technology-stack)
5. [System Architecture](#system-architecture)
6. [Database and Storage](#database-and-storage)
7. [REST API](#rest-api)
8. [Authentication and Security](#authentication-and-security)
9. [Offline Mode and Synchronisation](#offline-mode-and-synchronisation)
10. [Notifications](#notifications)
11. [Multi-language Support](#multi-language-support)
12. [Build and Installation](#build-and-installation)
13. [Testing and GitHub Actions](#testing-and-github-actions)
14. [Final POE Release Notes](#final-poe-release-notes)
15. [Innovative Features](#innovative-features)
16. [Application Screenshots](#application-screenshots)
17. [Play Store Preparation](#play-store-preparation)
18. [Demonstration Video](#demonstration-video)
19. [Implementation Scope](#implementation-scope)
20. [AI Usage](#ai-usage)
21. [References](#references)

---

## 1. Project Overview

### Purpose of the Application

Important personal information is often spread across notes, emails, calendars, documents and different reminder applications. This can make it difficult to keep track of important dates and find the information or documents associated with a particular responsibility.

LifeIndex was developed to make managing these responsibilities easier by providing one central place to organise personal information.

For example, a user can create a passport tracker containing its expiry date, notes, additional information and supporting documents. The same approach can be used for vehicle information, qualifications, warranties, job applications and other personal responsibilities.

The main idea behind LifeIndex is flexibility. Users are not restricted to managing one specific category of information because the application uses a reusable tracker system.

### Intended Users

LifeIndex is intended for individuals who want a more organised way of keeping track of personal records, responsibilities and important dates.

---

## 2. Main Application Features

### User Registration and Login

Users can create an account using an email address and password and subsequently log in to access their information.

The backend hashes passwords before storing them and uses authentication tokens to manage user sessions.

Google Sign-In is also supported through Android Credential Manager and backend verification.

### Tracker Management

Trackers are the main functionality of LifeIndex. The application supports three tracker types:

- **Item:** Used for long-term personal records such as passports, vehicles, qualifications and warranties.
- **Process:** Used for activities that move through different stages, such as job applications.
- **Recurring:** Used for responsibilities that happen repeatedly, such as renewals or vehicle services.

Users can create, view, edit, archive, restore and delete trackers.

Each tracker can contain information such as its title, type, status, priority, important dates, notes and custom fields.

### Templates

Built-in templates provide users with a starting structure when creating trackers. This makes it easier to capture relevant information without having to configure every field manually.

### Custom Fields

Custom fields allow different trackers to store information relevant to their specific purpose.

Field definitions are stored separately from their values, which makes the tracker system more flexible. The current tracker-detail interface supports text, date and yes/no fields.

### Home Dashboard

The Home screen provides an overview of the user's information, including:

- Total trackers.
- Items due today.
- Upcoming items.
- Overdue items.
- Recently updated records.

This allows users to identify important responsibilities without opening every tracker individually.

### Search and Calendar

Search allows users to locate trackers by their information.

The calendar view provides another way to navigate date-related tracker information and identify important upcoming dates.

### Document Management

Users can attach supporting documents to trackers.

Supported file types include PDF, JPEG and PNG, with a maximum file size of 10 MB.

Documents are stored in a private Supabase Storage bucket. The application retrieves document information through the backend and uses temporary signed URLs when a document needs to be downloaded.

### Application Settings

The Settings screen allows users to manage application preferences, including:

- Application theme.
- Notification preference.
- Default reminder lead time.
- Display language.

### Tracker Archiving

Users can archive trackers that they no longer need to see in their active tracker list. Archived trackers can be restored when needed.

---

## 3. What Was Added in Part 3

Part 3 focused on improving the original prototype and introducing additional functionality required for the final POE.

The following additions were made:

**Google Single Sign-On**

Google Sign-In was integrated using Android Credential Manager. The backend verifies the Google ID token before allowing the user to access the application.

**Offline Tracker Functionality**

Room was introduced to maintain a local tracker cache. WorkManager schedules synchronisation when a network connection becomes available.

**Push Notifications**

Firebase Cloud Messaging was integrated for push notifications, while Firebase Firestore stores notification records.

**Multi-language Support**

English, Afrikaans and isiXhosa language resources were added, allowing users to change the application language through Settings.

**Document Storage**

Supabase Storage was integrated to allow users to upload, retrieve and delete supporting documents associated with trackers.

**Backend Improvements**

The backend was extended to support the additional functionality and was deployed using Render.

These changes expanded LifeIndex beyond its original prototype and improved how the application manages personal information.

---

## 4. Technology Stack

| Area | Technology | Purpose |
|---|---|---|
| Programming Language | Kotlin | Android application development |
| UI Framework | Jetpack Compose | User interface |
| Design Components | Material 3 | Consistent Android design |
| Navigation | Navigation Compose | Screen navigation |
| Networking | Retrofit and OkHttp | API communication |
| JSON Processing | Gson | JSON conversion |
| State Management | ViewModel, Flow and Coroutines | Application state and asynchronous operations |
| Local Database | Room | Offline tracker storage |
| Background Processing | WorkManager | Tracker synchronisation |
| Backend | Node.js | Server runtime |
| Backend Framework | Express.js | REST API |
| Authentication | JWT and bcryptjs | Authentication and password hashing |
| Google Authentication | Google Auth Library | Google ID-token verification |
| Relational Database | PostgreSQL | Structured application data |
| Backend Database Hosting | Supabase | PostgreSQL hosting |
| File Storage | Supabase Storage | Private document storage |
| NoSQL Database | Firebase Firestore | Notification records |
| Push Notifications | Firebase Cloud Messaging | Notification delivery |
| Backend Hosting | Render | Hosting the Express API |
| Version Control | Git and GitHub | Source code management |
| Automated Workflow | GitHub Actions | Automated testing and builds |

---

## 5. System Architecture

LifeIndex uses a client-server architecture. The Android application communicates with a hosted Node.js and Express backend through a REST API.

The Android side uses Jetpack Compose for the interface, ViewModels to manage screen state and repositories to coordinate data operations.

Room is used for local tracker storage, while the hosted backend manages the main application data and communicates with the external storage and Firebase services.

### Architecture Diagram

```text
                 ANDROID APPLICATION
                          |
                 Jetpack Compose
                          |
                     ViewModels
                          |
                    Repositories
                     /       \
                    /         \
              Room DB       Retrofit
                |               |
                |          HTTPS REST API
                |               |
                |               |
                +--------+------+
                         |
                         v
                 RENDER HOSTING
                         |
                   Express API
                         |
          +--------------+--------------+
          |              |              |
          v              v              v
      PostgreSQL    Supabase        Firebase
      Database      Storage         Services
          |              |           /    \
          |              |     Firestore  FCM
          |              |         |       |
      Structured     Documents  Notification Push
        Data           Files       Records  Delivery
```

### Application Data Flow

1. The user interacts with a screen in the Android application.
2. The relevant ViewModel handles the action and communicates with a repository.
3. For supported offline tracker operations, the repository updates the local Room database.
4. When online, Retrofit sends requests to the hosted Express API.
5. The backend validates the request and checks that the authenticated user owns the relevant information.
6. The backend communicates with PostgreSQL, Supabase Storage or Firebase, depending on the operation.
7. The response is returned to the Android application and displayed to the user.

---

## 6. Database and Storage

LifeIndex uses multiple storage technologies because different types of information require different storage approaches.

### PostgreSQL – Structured Data

PostgreSQL, hosted through Supabase, stores the main application records.

This includes:

- User account information and password hashes.
- User settings and preferences.
- Tracker categories and templates.
- Tracker records and version numbers.
- Custom field definitions and values.
- Refresh-token session records.
- Document metadata and storage paths.
- Registered Android device tokens.

The relational database is used because these records have relationships with users and trackers and need to be managed consistently.

### Supabase Storage – Document Storage

Supabase Storage is used to store the actual files uploaded by users.

LifeIndex uses a private storage bucket called:

`lifeindex-attachments`

The database stores the document metadata, while Supabase Storage stores the actual file contents.

The backend checks ownership before allowing access to a document. When a user requests a document, the API can generate a temporary signed download URL that expires after 10 minutes.

The Supabase secret key remains on the backend and is not included in the Android application.

### Firebase Firestore – NoSQL Database

Firebase Firestore is used to store notification records.

The notification data is organised using the following structure:

```text
users/{userId}/notifications/{notificationId}
```

Each notification record can contain:

- Notification title.
- Notification message.
- Notification type.
- Related tracker ID, where applicable.
- Read status.
- Creation and update timestamps.

The backend can create and retrieve these notification records.

### Room – Local Database

Room is used to store tracker information locally on the Android device.

This allows supported tracker information to remain available when an internet connection is unavailable.

Room also stores synchronisation information to identify whether a tracker is pending synchronisation, already synchronised or in conflict.

---

## 7. REST API

LifeIndex uses a custom REST API developed with Node.js and Express.

The API is hosted on Render.

**Production API URL:**

https://lifeindex-api.onrender.com

**API Base Path:**

`https://lifeindex-api.onrender.com/api/v1/`

Retrofit is used by the Android application to communicate with the backend.

### Main API Endpoints

The following table summarises the main implemented API routes.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/auth/register` | Register a new account |
| POST | `/auth/login` | Log in using email and password |
| POST | `/auth/google` | Verify Google Sign-In |
| POST | `/auth/refresh` | Refresh an access session |
| POST | `/auth/logout` | Revoke a refresh session |
| GET / PATCH | `/users/me/settings` | Retrieve or update user settings |
| GET | `/categories` | Retrieve tracker categories |
| GET | `/templates` | Retrieve available templates |
| GET | `/templates/:id/fields` | Retrieve template field definitions |
| GET / POST | `/trackers` | Retrieve or create trackers |
| GET / PATCH / DELETE | `/trackers/:id` | Retrieve, update or delete a tracker |
| PATCH | `/trackers/:id/archive` | Archive a tracker |
| PATCH | `/trackers/:id/restore` | Restore an archived tracker |
| POST | `/trackers/sync` | Synchronise queued tracker operations |
| GET / PATCH | `/trackers/:id/fields` | Retrieve or update tracker field values |
| GET | `/dashboard` | Retrieve dashboard information |
| GET | `/search/trackers` | Search tracker records |
| GET | `/attachments` | Retrieve the user's documents |
| POST | `/trackers/:trackerId/attachments` | Upload a document |
| GET | `/attachments/:id/download` | Generate a temporary download URL |
| DELETE | `/attachments/:id` | Delete a document |
| POST | `/devices/register` | Register an FCM device token |
| POST / GET | `/notifications` | Create or retrieve notifications |
| PATCH | `/notifications/:notificationId/read` | Mark a notification as read |
| PATCH | `/notifications/read-all` | Mark all notifications as read |
| GET | `/health` | Check API availability |

Authenticated endpoints require a valid bearer access token unless they are specifically designed for unauthenticated access, such as registration and login.

---

## 8. Authentication and Security

Security was considered throughout the development of LifeIndex because the application stores personal information and supporting documents.

The main security measures include:

- Passwords are hashed on the backend using `bcryptjs` before they are stored in PostgreSQL.
- JWT access tokens are used to authenticate requests.
- Refresh-token sessions allow users to renew their access sessions.
- Google ID tokens are verified by the backend before Google authentication is accepted.
- Protected API operations check that the authenticated user owns the requested tracker or document.
- Document uploads are restricted to PDF, JPEG and PNG files.
- The document upload process applies a 10 MB size limit and checks the file signature.
- The Supabase Storage bucket is private.
- Document downloads use temporary signed URLs.
- Backend credentials and signing secrets are stored as environment secrets rather than being placed in public source code.
- Communication with the hosted API uses HTTPS.

The application does not store user passwords as readable plaintext.

---

## 9. Offline Mode and Synchronisation

Offline functionality was introduced in Part 3 using Room and WorkManager.

Room maintains a local tracker cache, while WorkManager schedules synchronisation when network connectivity becomes available.

### How Synchronisation Works

1. A user creates or changes a supported tracker.
2. The application saves the change to the local Room database.
3. The tracker is marked as `PENDING`.
4. A synchronisation operation is queued.
5. WorkManager schedules the synchronisation process.
6. Once connectivity is available, the application submits the queued operations to the backend through:

   `POST /api/v1/trackers/sync`

7. The backend checks the tracker version before applying an update.
8. A successful operation is marked as `SYNCED`.
9. If the server version has changed, the operation is marked as `CONFLICT` so the user can compare the local and server versions.

### Supported Offline Operations

The current offline synchronisation implementation supports:

- Tracker creation.
- Tracker updates, including notes.
- Tracker archiving.
- Tracker deletion.

The conflict-resolution process preserves the local copy while presenting the server version for comparison.

**Current limitation:** Custom-field changes and reminder changes are not included in the offline synchronisation queue. Offline support should therefore be understood as applying to the supported tracker operations rather than every feature in the application.

---

## 10. Notifications

Firebase Cloud Messaging (FCM) is used to support push notifications in LifeIndex.

The backend stores notification records in Firebase Firestore and uses Firebase Cloud Messaging to send push notifications to registered Android devices.

The process works as follows:

1. The Android device obtains an FCM registration token.
2. The application sends the token to the backend.
3. The backend associates the token with the authenticated LifeIndex user.
4. Notification information is stored in Firestore.
5. The backend uses Firebase Cloud Messaging to send the notification.
6. The Android application receives the push notification.

Users can retrieve their notification records and mark notifications as read.

---

## 11. Multi-language Support

LifeIndex includes support for three languages:

| Language | Android Resource Directory |
|---|---|
| English | `values` |
| Afrikaans | `values-af` |
| isiXhosa | `values-xh` |

The language can be changed through the application settings.

Android string resources are used to manage the translated interface text. This makes it easier to maintain and expand the language support without having to rewrite the screen layouts.

---

## 12. Build and Installation

### Development Requirements

To build the Android application, the following tools and configurations are required:

- Android Studio.
- Compatible Android Gradle Plugin 9.0.1 setup.
- JDK 17 or a compatible newer JDK.
- Android SDK Platform 36.
- Android device or emulator running Android 8.0 (API 26) or newer.

For backend development:

- Node.js 24.x.
- npm.
- Access to the configured PostgreSQL, Supabase Storage and Firebase services.

### Clone the Repository

```bash
git clone https://github.com/Suhail-Allie/LifeIndex.git
cd LifeIndex
git checkout part3-final-poe
```

### Build the Android Application

For Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

For macOS or Linux:

```bash
./gradlew :app:assembleDebug
```

The generated debug APK is located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Install the Application Using ADB

With an Android device connected and USB debugging enabled:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Run the Backend Locally

```bash
cd backend
npm install
npm start
```

The backend requires environment configuration before it can connect to the hosted services.

The following environment variables are used where applicable:

```text
DATABASE_URL
JWT_ACCESS_SECRET
JWT_REFRESH_SECRET
GOOGLE_WEB_CLIENT_ID
SUPABASE_URL
SUPABASE_SECRET_KEY
```

Firebase Admin also requires its service-account credentials to be configured securely on the backend.

**Important:** Secret values must never be committed to GitHub or included in public documentation.

---

## 13. Testing and GitHub Actions

Testing was carried out during development to check the application, backend integrations and the functionality introduced in Part 3.

### Android Build Testing

The Android application was built using Gradle.

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

### Backend Syntax Testing

Node.js syntax checks were performed on the attachment integration files.

```powershell
node --check .\backend\controllers\attachmentController.js
node --check .\backend\routes\attachmentRoutes.js
node --check .\backend\config\supabaseStorage.js
```

### API Health Check

The hosted API can be checked using:

```powershell
Invoke-RestMethod https://lifeindex-api.onrender.com/api/v1/health
```

The successful health check identifies the service as `LifeIndex API` and returns a status of `ok`.

### Manual Testing

The following functionality was exercised during development:

- Email registration and login.
- Google Sign-In and backend ID-token verification.
- Changing the application language.
- Receiving FCM notifications on a registered test device during emulator testing.
- Offline tracker creation and local persistence.
- Editing trackers and synchronising after reconnecting.
- Version-conflict handling.
- Supabase document upload and document management.
- Backend deployment and communication with the hosted API.

The debug APK was also successfully installed on a physical Redmi 10 2022 Android device.

The installation itself is separate from a full functional test of every feature on the physical device. The final demonstration video should provide the evidence for the on-device functionality being presented.

### GitHub Actions

GitHub Actions is used to automate Android checks and build processes.

The workflow is located at:

```text
.github/workflows/android.yml
```

The workflow helps check the project through automated Gradle testing and building when the relevant repository events trigger it.

The GitHub Actions badge at the top of this README links to the workflow.

The workflow results can also be viewed through the repository's **Actions** tab.

### Version Control

Git and GitHub were used throughout development to maintain the project source code and record changes.

The final development branch is:

```text
part3-final-poe
```

The final POE submission should also be tagged using the required tag:

```bash
git tag -a "Final POE" -m "Final POE submission"
git push origin "Final POE"
```

---

## 14. Final POE Release Notes

### Final POE – 5 November 2026

The final version builds on the original prototype by introducing additional functionality and improving the application's overall design and capabilities.

| Area | Changes made |
|---|---|
| Authentication | Added Google Single Sign-On using Credential Manager and backend ID-token verification. |
| Offline functionality | Added Room caching, queued tracker operations and WorkManager synchronisation. |
| Conflict handling | Added version-based conflict detection and a comparison process for local and server data. |
| Notifications | Integrated Firebase Cloud Messaging and Firestore notification storage. |
| Language support | Added English, Afrikaans and isiXhosa resources and language selection. |
| Document management | Integrated Supabase Storage for private document uploads, retrieval and deletion. |
| Security | Added file validation, ownership checks and temporary signed document download links. |
| Backend | Expanded the Express API to support the additional functionality and deployed it through Render. |
| Application interface | Updated navigation, settings and translated screen resources. |
| Repository | Updated the source code and backend integration for the final POE. |

The most important changes in this release were the introduction of offline tracker synchronisation, Google Sign-In, push notifications and multi-language support.

These features expanded the original prototype into a more complete personal life-administration application.

---

## 15. Innovative Features

LifeIndex includes several features that make the application flexible and practical for managing different types of personal information.

### Reusable Tracker System

The Item, Process and Recurring tracker types allow users to manage different responsibilities through one common system.

This avoids the need to create separate modules for every possible type of personal record.

### Template-Based Tracker Creation

Templates provide users with predefined structures for common records, reducing the time needed to create a tracker.

### Flexible Custom Fields

Custom fields allow different trackers to capture information specific to their purpose. This makes the application adaptable to different personal requirements.

### Connected Personal Records

Users can keep important information, notes, custom fields and supporting documents together within a tracker.

### Offline Tracker Management

The ability to create and update supported trackers without an internet connection makes the application more practical when connectivity is unavailable.

### Dashboard Overview

The dashboard brings important dates and tracker information together, helping users identify responsibilities that need attention.

---


| Offline synchronisation | `offline-sync.png` |


## 18. Demonstration Video

The final demonstration video presents LifeIndex running on a physical Android device.

The video should include a voice-over explaining the functionality being demonstrated.

The demonstration should cover:

- User registration and login.
- Google Single Sign-On.
- Tracker creation and management.
- Application settings.
- Offline tracker functionality and synchronisation.
- Push notifications.
- Multi-language support.
- Document management.
- The hosted backend and relevant stored data.

**Final demonstration video:**

(https://youtu.be/zObcPhJk3X4)

---

## 19. Implementation Scope

A few implementation details are important when describing the final version:

- Offline synchronisation currently covers supported tracker operations and notes. Custom-field edits and reminder changes are not included in the offline queue.
- The Settings screen includes a default reminder lead-time preference. This documentation does not claim a complete reminder scheduling and recurrence workflow.
- The debug APK was installed on a physical Redmi 10 2022 device. Emulator testing and physical-device functional testing should be distinguished when presenting evidence.
- Google Play publication evidence and the signed release build should be included with the final submission.

---

## 20. AI Usage

ChatGPT was used as an AI-assisted development and writing tool during the development of LifeIndex.

AI assistance was used to understand technical requirements, explore implementation approaches, troubleshoot errors, review code and prepare technical documentation.



---

## 21. References

The following official documentation was used as a reference during development:

- [Android Developers – Guide to App Architecture](https://developer.android.com/topic/architecture)
- [Android Developers – Jetpack Compose](https://developer.android.com/compose)
- [Android Developers – Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Android Developers – WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Android Developers – Credential Manager](https://developer.android.com/identity)
- [Android Developers – App Languages](https://developer.android.com/guide/topics/resources/app-languages)
- [Firebase – Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Firebase – Cloud Firestore](https://firebase.google.com/docs/firestore)
- [Supabase – Storage](https://supabase.com/docs/guides/storage)
- [Express – Express Application Framework](https://expressjs.com/)
- [PostgreSQL – Documentation](https://www.postgresql.org/docs/)
- [Render – Web Services](https://render.com/docs/web-services)
- [GitHub – GitHub Actions](https://docs.github.com/actions)

---

**LifeIndex – Track anything. Find everything.**
