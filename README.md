# LifeIndex

## Track anything. Find everything.

[![Android CI](https://github.com/Suhail-Allie/LifeIndex/actions/workflows/android.yml/badge.svg)](https://github.com/Suhail-Allie/LifeIndex/actions/workflows/android.yml)

LifeIndex is an Android personal life-administration application that brings important records, responsibilities, dates and supporting documents into one place. Rather than requiring a separate application for every part of life, it uses a reusable tracker system that can represent an item, a process or a recurring responsibility.

The project was developed for the **OPSC6312 – Open Source Coding Intermediate Portfolio of Evidence (Final POE)**.

---

## Project information

| Item | Details |
|---|---|
| Application | LifeIndex |
| Module | OPSC6312 – Open Source Coding Intermediate |
| Assessment | Portfolio of Evidence – Final POE (Part 3) |
| Author | Mogammad Suhail Allie |
| Student number | ST10455559 |
| Year | 2026 |
| Android package | `com.lifeindex.app` |
| Android minimum SDK | 26 (Android 8.0) |
| Compile / target SDK | 36 |
| Main branch for final POE work | `part3-final-poe` |
| Hosted API | `https://lifeindex-api.onrender.com` |

---

## Table of contents

- [Purpose and problem](#purpose-and-problem)
- [Main application features](#main-application-features)
- [Technology stack](#technology-stack)
- [System architecture](#system-architecture)
- [Data storage](#data-storage)
- [REST API](#rest-api)
- [Security and privacy](#security-and-privacy)
- [Offline-first tracker synchronisation](#offline-first-tracker-synchronisation)
- [Languages and accessibility](#languages-and-accessibility)
- [Build and run](#build-and-run)
- [Testing and continuous integration](#testing-and-continuous-integration)
- [Final POE release notes](#final-poe-release-notes)
- [Innovative features](#innovative-features)
- [Screenshots and demonstration video](#screenshots-and-demonstration-video)
- [Known implementation scope](#known-implementation-scope)
- [References](#references)

---

## Purpose and problem

Important personal information is often spread across notes, email, calendars, documents and separate reminder systems. This makes it difficult to see the full context of a record or find the supporting information when it is needed.

LifeIndex addresses this problem by providing one place to organise records and responsibilities. For example, a passport tracker can keep its title, dates, notes, custom information and supporting document together. The same tracker structure can be used for a vehicle, qualification, job application, warranty, travel record or another personal responsibility.

The application is intended for people who need a flexible way to organise personal information without having to build a separate system for every type of record.

## Main application features

### Account access

LifeIndex supports account registration and sign-in using an email address and password. Authenticated API requests use access tokens, while refresh sessions allow the application to renew an access session. Google Sign-In is also supported through Android Credential Manager and backend verification of the Google ID token.

### Tracker management

The application uses three tracker types:

- **Item** – a long-term record such as a passport, vehicle, warranty or qualification.
- **Process** – a record that moves through a sequence of stages, such as an application.
- **Recurring** – a record representing a responsibility that occurs repeatedly, such as a service or renewal.

Users can create, view, edit, archive, restore and delete trackers. A tracker can contain a title, type, status, priority, important date, notes and configured custom fields.

### Templates

Built-in templates provide a starting structure for common types of personal records. They reduce the amount of information a user has to configure when creating a tracker and support the reusable tracker design.

### Custom fields

Tracker field definitions are stored separately from the values entered by a user. This allows different trackers to capture different information without requiring a new PostgreSQL table or column for every tracker type. The current tracker-detail interface displays configured text, date and yes/no (boolean) fields.

### Home dashboard

The Home screen provides an overview of tracker information, including total trackers, due-today items, upcoming items, overdue items and recently updated records.

### Search and calendar

The application includes tracker search and a calendar view for navigating date-related tracker information. Search and date views help users locate records without having to open each tracker individually.

### Documents

Documents can be linked to a tracker and stored in a private Supabase Storage bucket. The supported file types are PDF, JPEG and PNG, with a maximum file size of 10 MB. The application can display document metadata, request a time-limited signed download URL and delete a document.

### Settings

The Settings screen provides controls for application preferences, including theme, notification preference, default reminder lead time and language selection.

### Notifications

The backend stores user notifications in Firebase Firestore and uses Firebase Cloud Messaging (FCM) to attempt push delivery to registered Android devices. Device registration tokens are associated with the authenticated LifeIndex account in PostgreSQL.

---

## Technology stack

| Area | Technology | Purpose |
|---|---|---|
| Android language | Kotlin | Application implementation |
| User interface | Jetpack Compose, Material 3 | Android screens and UI components |
| Navigation | Navigation Compose | Movement between application screens |
| Networking | Retrofit, OkHttp, Gson | REST requests and JSON conversion |
| Application state | ViewModel, Kotlin Coroutines and Flow | Screen state and asynchronous work |
| Local persistence | Room | Local tracker cache for offline use |
| Background work | WorkManager | Scheduling queued tracker synchronisation |
| Backend runtime | Node.js | API runtime |
| Backend framework | Express.js | REST API routes and controllers |
| Authentication | JWT, bcryptjs, Google Auth Library | Sessions, password hashing and Google ID token verification |
| Relational database | PostgreSQL on Supabase | User accounts, settings, trackers and related metadata |
| Object storage | Supabase Storage | Private document objects |
| NoSQL database | Firebase Firestore | User notification records |
| Push messaging | Firebase Cloud Messaging | Android push notification delivery |
| Backend hosting | Render | Hosted LifeIndex API |
| Version control | Git and GitHub | Source control and collaboration history |
| Continuous integration | GitHub Actions | Automated repository workflow |

---

## System architecture

LifeIndex separates the Android user interface from data access and hosted services. Compose screens collect user input and display state from ViewModels. Repositories coordinate local Room data and remote API requests. The Express API handles authenticated requests and communicates with PostgreSQL, Supabase Storage and Firebase services.

```text
┌──────────────────────────────────────────────────────────────┐
│                    Android application                       │
│                                                              │
│  Jetpack Compose screens                                     │
│          │                                                   │
│       ViewModels                                              │
│          │                                                   │
│     Repositories ─────────────── Retrofit / OkHttp            │
│          │                              │                    │
│     Room local cache              HTTPS REST requests        │
│          │                              │                    │
│     WorkManager                            │                  │
└────────────────────────────────────────────│─────────────────┘
                                             │
                                             ▼
┌──────────────────────────────────────────────────────────────┐
│                 Render – Express REST API                    │
│                                                              │
│  Authentication middleware • Controllers • Validation       │
│          │                    │                   │          │
│          ▼                    ▼                   ▼          │
│  Supabase PostgreSQL   Supabase Storage     Firebase services│
│  relational records   private file objects  Firestore + FCM  │
└──────────────────────────────────────────────────────────────┘
```

### Request and data flow

1. The user performs an action in an Android Compose screen.
2. The screen's ViewModel coordinates the operation through a repository.
3. For supported offline tracker operations, the repository writes the local tracker state to Room and queues the change.
4. For online operations, Retrofit sends an authenticated HTTPS request to the hosted Express API.
5. The API validates the request and account ownership before accessing the relevant service or database.
6. The API returns a response that the Android application uses to update its interface and, where applicable, its local cache.

---

## Data storage

LifeIndex uses different storage services for different types of information.

### PostgreSQL – Supabase

PostgreSQL stores structured application data, including:

- User account records and password hashes.
- User settings and preferences.
- Categories and tracker templates.
- Tracker records and their version numbers.
- Custom field definitions and values.
- Refresh-token session records.
- Attachment metadata and protected storage paths.
- Registered Android device tokens.

The `attachments` table stores metadata such as the tracker ID, uploader ID, original filename, storage path, bucket name, MIME type, file size and creation time. The actual file contents are stored in Supabase Storage rather than in the relational table.

### Supabase Storage – private documents

The application uses a private bucket named `lifeindex-attachments`. The backend accesses the bucket using a server-side Supabase secret key. The Android application does not receive this secret. After checking ownership, the API can create a temporary signed download URL. The current API issues signed URLs with a 10-minute expiry.

### Firebase Firestore – NoSQL notifications

Each user's notification records are stored in a Firestore subcollection:

```text
users/{userId}/notifications/{notificationId}
```

The notification record includes its title, body, type, related tracker ID where applicable, read state and timestamps. The notification API can create and retrieve these records.

### Room – local Android cache

Room stores tracker data on the device so that supported tracker information remains available without a network connection. Synchronisation metadata tracks whether a cached tracker is pending, synchronised or in conflict.

---

## REST API

The hosted backend is an Express REST API. The production base URL is:

```text
https://lifeindex-api.onrender.com/api/v1/
```

The main implemented route groups are summarised below. Authenticated routes require a bearer access token unless stated otherwise.

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/auth/register` | Register an account |
| `POST` | `/auth/login` | Sign in with email and password |
| `POST` | `/auth/google` | Verify Google Sign-In and create or link an account |
| `POST` | `/auth/refresh` | Refresh an access session |
| `POST` | `/auth/logout` | Revoke the refresh session |
| `GET`, `PATCH` | `/users/me/settings` | Read or update user settings |
| `GET` | `/categories` | Retrieve tracker categories |
| `GET` | `/templates` | Retrieve templates |
| `GET` | `/templates/:id/fields` | Retrieve a template's field definitions |
| `GET`, `POST` | `/trackers` | Retrieve or create trackers |
| `GET`, `PATCH`, `DELETE` | `/trackers/:id` | Retrieve, update or delete a tracker |
| `PATCH` | `/trackers/:id/archive` | Archive a tracker |
| `PATCH` | `/trackers/:id/restore` | Restore an archived tracker |
| `POST` | `/trackers/sync` | Submit queued, versioned tracker operations |
| `GET`, `PATCH` | `/trackers/:id/fields` | Retrieve or update tracker field values |
| `GET` | `/dashboard` | Retrieve dashboard tracker data |
| `GET` | `/search/trackers` | Search tracker records |
| `GET` | `/attachments` | List the signed-in user's documents |
| `POST` | `/trackers/:trackerId/attachments` | Upload a document to a tracker |
| `GET` | `/attachments/:id/download` | Request a temporary signed download URL |
| `DELETE` | `/attachments/:id` | Delete a document and its storage object |
| `POST` | `/devices/register` | Register an FCM device token |
| `POST`, `GET` | `/notifications` | Create or retrieve Firestore notifications |
| `PATCH` | `/notifications/:notificationId/read` | Mark a notification as read |
| `PATCH` | `/notifications/read-all` | Mark all notifications as read |
| `GET` | `/health` | Check API service health |

---

## Security and privacy

The backend applies authentication and ownership checks to protected records. The main security measures include:

- Passwords are hashed on the backend with `bcryptjs` before they are stored. Passwords are not stored as readable plaintext.
- Access and refresh tokens are used to manage authenticated sessions.
- Google ID tokens are verified by the backend before a Google sign-in is accepted.
- Tracker and attachment queries check the authenticated user's ownership.
- Document uploads accept PDF, JPEG and PNG MIME types, apply a 10 MB limit and check the file signature before storage.
- The Supabase Storage bucket is private. Document downloads use short-lived signed URLs.
- The Supabase secret key, JWT signing secrets, database connection string and Firebase Admin service-account credentials are configured as backend environment secrets. They must not be placed in the Android application or committed to GitHub.
- API traffic between the Android application and the hosted production API uses HTTPS.

The Firebase client configuration file in the Android project is not a substitute for the private Firebase Admin service-account credentials. Server-side credentials must remain in the hosting environment.

---

## Offline-first tracker synchronisation

The offline implementation uses Room as the local tracker cache and WorkManager to schedule synchronisation when network connectivity is available.

For supported tracker changes:

1. The repository updates the local Room record first.
2. The local record is marked `PENDING` and the change is queued.
3. WorkManager schedules the synchronisation worker.
4. When connectivity is available, the worker submits queued operations to `POST /api/v1/trackers/sync`.
5. The backend checks the tracker's version before applying an update.
6. Successful changes become `SYNCED`. Version mismatches are returned as `CONFLICT` so the user can choose the local or server version.

The tracker synchronisation implementation supports local tracker creation, tracker updates (including notes), archiving and deletion. The conflict-resolution flow preserves the local copy while presenting the server version for comparison.

**Scope note:** the current offline queue is for tracker records. Custom-field edits and reminder changes are not currently queued by this Room/WorkManager implementation. They should not be described as offline-synchronised features in a demonstration of this release.

---

## Languages and accessibility

LifeIndex includes string resources for:

- English (`values`)
- Afrikaans (`values-af`)
- isiXhosa (`values-xh`)

The language can be changed through the application settings. Android app-language configuration is included so the user can select a supported language. The interface uses Compose and Material 3 components and provides feedback for validation and API errors.

---

## Build and run

### Requirements

- Android Studio with a compatible Android Gradle Plugin 9.0.1 setup.
- JDK 17 or a compatible newer JDK.
- Android SDK Platform 36 and Android build tools.
- Android device or emulator running Android 8.0 (API 26) or newer.
- Node.js 24.x and npm for backend development.
- Access to the configured PostgreSQL, Supabase Storage and Firebase services for a full hosted-backend run.

### Clone the repository

```bash
git clone https://github.com/Suhail-Allie/LifeIndex.git
cd LifeIndex
git checkout part3-final-poe
```

### Build the Android application

On Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

On macOS or Linux:

```bash
./gradlew :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To install it on a connected Android device with ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Run the backend locally

```bash
cd backend
npm install
npm start
```

The backend requires environment configuration before it can connect to hosted services. Configure the values in a local, untracked environment file or through your hosting provider. Do not commit secret values.

The backend configuration uses these environment settings where applicable:

```text
DATABASE_URL
JWT_ACCESS_SECRET
JWT_REFRESH_SECRET
GOOGLE_WEB_CLIENT_ID
SUPABASE_URL
SUPABASE_SECRET_KEY
```

Firebase Admin also requires its service-account credentials to be supplied securely to the backend. Do not paste credentials into source files, README files, screenshots or public issue reports.

The Android client is configured for the hosted LifeIndex API by default. When developing against a local backend, configure the Android base URL to an address reachable from the selected device or emulator.

---

## Testing and continuous integration

### Local Android build

The debug application was built successfully using Gradle:

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

### Backend syntax checks

The attachment integration JavaScript files were checked with Node.js:

```powershell
node --check .\backend\controllers\attachmentController.js
node --check .\backend\routes\attachmentRoutes.js
node --check .\backend\config\supabaseStorage.js
```

### Hosted API health check

The hosted API health endpoint can be checked with:

```powershell
Invoke-RestMethod https://lifeindex-api.onrender.com/api/v1/health
```

The successful response identifies the service as `LifeIndex API` and returns status `ok`.

### Manual verification completed during development

The following integrations were exercised during development:

- Email account registration and login.
- Google Sign-In using Credential Manager and backend ID-token verification.
- FCM notification delivery to a registered test device during emulator testing.
- Switching between English, Afrikaans and isiXhosa during emulator testing.
- Offline tracker creation, persistence, editing, reconnection and version-conflict resolution during emulator testing.
- Supabase document upload and document management through the Android application.
- Deployment of the backend to Render and a successful production health check.
- Installation of the debug APK on a Redmi 10 2022 physical Android device (model `21121119SG`).

Physical-device installation was confirmed. Record the final on-device functional test results in the demonstration video and do not treat emulator-only checks as physical-device test evidence.

### GitHub Actions

The repository includes an Android CI workflow at `.github/workflows/android.yml`. Workflow executions and their build/check results are available in the repository's **Actions** tab. The badge at the top of this README links to that workflow.

---

## Final POE release notes

### Final POE – 5 November 2026

The Final POE extends the earlier prototype with the following changes and integrations:

1. **Google Single Sign-On:** added Google Credential Manager support in Android and Google ID-token verification in the backend.
2. **Offline-first tracker support:** added a Room tracker cache, pending-operation state, WorkManager scheduling and version-based conflict handling.
3. **Push notifications:** added Firebase device registration, Firestore notification storage/retrieval and FCM push delivery.
4. **Multilingual interface:** added English, Afrikaans and isiXhosa string resources and language selection.
5. **Private document storage:** integrated Supabase Storage with a private bucket, file validation, attachment metadata and temporary signed download links.
6. **Application updates:** expanded the Documents screen and connected it to the attachment API; updated application navigation, settings and translated screen resources.
7. **Hosted backend:** deployed the Express API through Render and connected it to the hosted database and Firebase services.
8. **Repository updates:** committed the final application and backend integration changes to the `part3-final-poe` branch.

---

## Innovative features

The main design features carried into the Final POE are:

- **Reusable tracker engine:** Item, Process and Recurring records use a common structure rather than separate applications for different personal needs.
- **Template-based setup:** ready-made structures give users a starting point for common records.
- **Flexible custom information:** field definitions are kept separate from tracker values so records can capture different kinds of information.
- **Connected record information:** tracker details bring core record information, notes, configured fields and linked documents into one context.
- **Dashboard date overview:** due-today, upcoming and overdue summaries help users identify records that need attention.
- **Offline tracker continuity:** supported tracker changes are stored locally and synchronised when connectivity returns, with a conflict state for version mismatches.
- **Private supporting documents:** files are associated with trackers and delivered through temporary signed links instead of public storage URLs.

---

## Screenshots and demonstration video

Add screenshots captured from the **physical Redmi 10 2022** to a folder such as `docs/screenshots/`. Recommended screenshots are:

- Home dashboard.
- Tracker list and tracker details.
- Template selection and custom fields.
- Calendar.
- Documents list and document metadata.
- Settings in English, Afrikaans and isiXhosa.
- Notification received on the physical device.
- Offline tracker status and successful synchronisation.

**Demonstration video:** add the final unlisted video URL here after recording and uploading the voice-over demonstration.

---

## Known implementation scope

The following scope notes keep the documentation aligned with the implementation:

- Offline synchronisation currently covers tracker records and notes. Custom-field changes and reminder changes are not currently part of the offline queue.
- The Settings screen includes a default reminder lead-time preference. This README does not claim a complete reminder scheduling and recurrence workflow.
- The Android debug APK has been built and installed on the Redmi 10 2022. Final functional checks on the physical device should be demonstrated separately from emulator tests.
- Google Play publication evidence, a signed release bundle and the final demonstration-video link must be added when those submission items are prepared.

---

## References

The following official documentation informed the implementation and development approach:

- [Android Developers – Guide to app architecture](https://developer.android.com/topic/architecture)
- [Android Developers – Jetpack Compose](https://developer.android.com/compose)
- [Android Developers – Room persistence library](https://developer.android.com/training/data-storage/room)
- [Android Developers – WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Android Developers – Credential Manager](https://developer.android.com/identity)
- [Android Developers – App languages](https://developer.android.com/guide/topics/resources/app-languages)
- [Firebase – Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Firebase – Cloud Firestore](https://firebase.google.com/docs/firestore)
- [Supabase – Storage](https://supabase.com/docs/guides/storage)
- [Express – Express application framework](https://expressjs.com/)
- [PostgreSQL – Documentation](https://www.postgresql.org/docs/)
- [Render – Web services](https://render.com/docs/web-services)
- [GitHub – GitHub Actions documentation](https://docs.github.com/actions)

---

**LifeIndex – Track anything. Find everything.**
