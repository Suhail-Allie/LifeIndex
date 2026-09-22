# LifeIndex

## Track anything. Find everything.

[![Android CI](https://github.com/Suhail-Allie/LifeIndex/actions/workflows/android.yml/badge.svg)](https://github.com/Suhail-Allie/LifeIndex/actions/workflows/android.yml)

LifeIndex is a personal life administration Android application designed to help users keep important information, responsibilities and records organised in one place.

Instead of creating separate systems for different parts of life, LifeIndex uses a reusable tracker system that allows users to create different types of trackers based on their needs.

---

# Project Information

| Information | Details |
|---|---|
| **Project** | LifeIndex |
| **Module** | OPSC6312 – Open Source Coding Intermediate |
| **Assessment** | Portfolio of Evidence – Part 2 |
| **Author** | Mogammad Suhail Allie |
| **Year** | 2026 |

---

# Project Overview

LifeIndex was developed as part of the OPSC6312 Open Source Coding Intermediate Portfolio of Evidence.

The purpose of the application is to provide users with one place to manage important personal records, responsibilities and information.

Examples of information that can be tracked include:

- Passports
- Vehicles
- Qualifications
- Job applications
- Travel information
- Recurring services
- Renewals
- Other important personal records

The application uses a reusable tracker engine so that users do not need a completely separate system for every area of their life.

The main concept behind LifeIndex is to make personal information easier to organise by allowing users to create trackers that suit different situations while keeping the same core application structure.

---

# Problem Being Addressed

Important personal information is often spread across notes applications, calendars, emails, documents and different reminder systems.

LifeIndex addresses this by providing a central personal tracking system where related information can be grouped together.

For example, a user can create a passport tracker containing:

- Passport number
- Expiry information
- Notes
- Custom fields
- Related information

The same system can then be used for a vehicle, qualification, job application, travel record or another personal responsibility.

---

# Main Features

## User Authentication

LifeIndex provides account-based authentication.

Users can:

- Register an account
- Log in
- Log out
- Maintain an authenticated session
- Access protected application data

Passwords are securely **hashed** on the backend before being stored.

The authentication system uses:

- JWT access tokens
- JWT refresh tokens
- Protected API endpoints
- User ownership checks
- Refresh-token session handling

---

## Tracker Management

LifeIndex provides three main tracker types:

- **Item**
- **Process**
- **Recurring**

Users can:

- Create trackers
- View trackers
- Edit trackers
- Delete trackers
- Archive trackers
- View tracker details

Trackers can contain information such as:

- Title
- Tracker type
- Status
- Priority
- Important date
- Notes
- Custom fields

### Item Trackers

Item trackers are designed for long-term records such as:

- Passports
- Vehicles
- Warranties
- Important documents

### Process Trackers

Process trackers are designed for activities that move through stages, such as:

- Job applications
- Applications
- Other multi-step processes

### Recurring Trackers

Recurring trackers are designed for responsibilities that repeat, such as:

- Renewals
- Services
- Regular maintenance
- Recurring responsibilities

---

## Templates

LifeIndex includes reusable templates for common use cases.

Current templates include:

- Passport
- Vehicle
- Job Application
- Qualification
- Travel

Templates allow users to start with a predefined structure instead of building every tracker from scratch.

This supports the reusable tracker engine that was planned during the design stage of the project.

---

## Custom Fields

LifeIndex uses a flexible field system so that different trackers can store different types of information.

Supported field types include:

- Text
- Number
- Date
- Yes/No
- Choice

This allows different trackers to capture different information while still using the same reusable tracker engine.

The application uses separate field definitions and values so that tracker information can remain flexible without requiring a separate database structure for every possible tracker.

---

## Dashboard

The Home dashboard provides an overview of the user's tracking information.

It includes:

- Total trackers
- Due today
- Upcoming trackers
- Overdue trackers
- Recently updated trackers

The dashboard provides a quick summary when the user opens the application.

---

## Search and Filtering

Users can search trackers by title and filter trackers by tracker type.

This allows specific records to be located quickly as the number of trackers increases.

---

## Settings

Users can change application preferences including:

- Theme
- Notification preference
- Default reminder time

Settings are sent through the REST API and stored for the user's account.

---

## Input Validation and Error Handling

LifeIndex validates important user input before requests are submitted.

Validation includes:

- Invalid email addresses
- Missing display names
- Short registration passwords
- Empty tracker titles
- Invalid reminder values

Validation and API errors are displayed to the user instead of allowing invalid input to cause the application to crash.

The application was also tested with incorrect login credentials and invalid form values.

---

# User Interface

LifeIndex uses a modern, minimal and user-friendly interface.

The interface was designed around:

- Simple navigation
- Clear visual hierarchy
- Consistent spacing
- Rounded components
- Clear typography
- Minimal visual clutter
- Easy access to important information

## Main Navigation

The main application navigation contains:

- Home
- Trackers
- Calendar
- Documents
- Settings

The Android application is built using Jetpack Compose and Material 3.

---

# Technology Stack

## Android

- Kotlin
- Jetpack Compose
- Material 3
- Android Studio
- Android SDK
- Retrofit
- Gson
- ViewModel
- Gradle

## Backend

- Node.js
- Express.js
- REST API
- JSON Web Tokens
- bcryptjs
- PostgreSQL client (`pg`)

## Database

- PostgreSQL
- Supabase

## Hosting

- Render

## Version Control and Continuous Integration

- Git
- GitHub
- GitHub Actions

---

# System Architecture

```text
                    ┌─────────────────────────┐
                    │    LifeIndex Android    │
                    │     Kotlin / Compose    │
                    └────────────┬────────────┘
                                 │
                                 │ HTTPS / REST API
                                 ▼
                    ┌─────────────────────────┐
                    │      Render Web         │
                    │   Node.js / Express     │
                    └────────────┬────────────┘
                                 │
                                 │ PostgreSQL
                                 ▼
                    ┌─────────────────────────┐
                    │   Supabase PostgreSQL   │
                    └─────────────────────────┘
```

The Android application communicates with the hosted backend through HTTPS.

The backend provides the REST API and communicates with the PostgreSQL database hosted through Supabase.

---

# REST API

The LifeIndex API is versioned under:

`/api/v1`

## Main API Areas

- `/auth`
- `/users`
- `/trackers`
- `/categories`
- `/templates`
- `/dashboard`
- `/search`

## Authentication Endpoints

```text
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
```

## User Settings Endpoints

```text
GET    /api/v1/users/me/settings
PATCH  /api/v1/users/me/settings
```

## Tracker Endpoints

```text
GET    /api/v1/trackers
POST   /api/v1/trackers
GET    /api/v1/trackers/{id}
PATCH  /api/v1/trackers/{id}
DELETE /api/v1/trackers/{id}
PATCH  /api/v1/trackers/{id}/archive
```

## Template Endpoints

```text
GET    /api/v1/templates
GET    /api/v1/templates/{id}/fields
```

## Tracker Field Endpoints

```text
GET    /api/v1/trackers/{id}/fields
PATCH  /api/v1/trackers/{id}/fields
```

## Dashboard and Search

```text
GET    /api/v1/dashboard
GET    /api/v1/search/trackers
```

## Health and Database Testing

```text
GET    /api/v1/health
GET    /api/v1/db-test
```

---

# Deployment

The LifeIndex backend is deployed using **Render**.

The backend is built with Node.js and Express and connects to a PostgreSQL database hosted through **Supabase**.

The Android application uses the hosted API over HTTPS.

## Hosted API

### Base API

[Open LifeIndex Hosted API](https://lifeindex-api.onrender.com/api/v1/)

### Health Check

[Open LifeIndex API Health Check](https://lifeindex-api.onrender.com/api/v1/health)

### Database Connection Check

[Open LifeIndex Database Connection Check](https://lifeindex-api.onrender.com/api/v1/db-test)

The database connection check confirms that the deployed API can communicate with the hosted PostgreSQL database.

Sensitive environment variables are stored securely in the hosting environment and are not committed to the repository.

---

# Database

LifeIndex uses PostgreSQL through Supabase.

The database contains tables supporting areas such as:

- Users
- User settings
- Refresh sessions
- Categories
- Templates
- Template fields
- Trackers
- Field definitions
- Field values
- Reminders
- Attachments
- History events
- Notifications
- Devices

The tracker system separates field definitions from field values so that different trackers can store different types of information while using the same tracker engine.

---

# Security

The application includes:

- Password hashing using bcryptjs
- JWT access tokens
- JWT refresh tokens
- Protected API routes
- User ownership checks
- Refresh-token session handling
- Environment variables for sensitive configuration
- HTTPS communication with the hosted API

The real `.env` file is excluded from the Git repository.

A safe `.env.example` file is included for configuration reference without exposing credentials.

---

# Local Development

## Android Application

Open the project in Android Studio.

The Android application can be built using:

```bash
./gradlew assembleDebug
```

Unit tests can be run using:

```bash
./gradlew testDebugUnitTest
```

## Backend

Navigate to the backend folder:

```bash
cd backend
```

Install dependencies:

```bash
npm install
```

Start the backend:

```bash
npm start
```

The backend uses environment variables for its database connection and JWT configuration.

Do not commit the real `.env` file.

---

# Testing

LifeIndex includes automated unit tests focused on validation functionality.

Tests cover areas including:

- Email validation
- Display name validation
- Password validation
- Tracker title validation
- Reminder validation

## Run Unit Tests

```bash
./gradlew testDebugUnitTest
```

## Build the Debug Application

```bash
./gradlew assembleDebug
```

The application was also manually tested through:

- Registration
- Login
- Incorrect login credentials
- Settings updates
- Tracker creation
- Tracker editing
- Tracker deletion
- Tracker archiving
- Template selection
- Custom fields
- Search
- Filtering
- Hosted API requests
- Hosted database connection

---

# GitHub Actions

GitHub Actions is used to automatically test and build the Android application.

The workflow is located at:

`.github/workflows/android.yml`

## Workflow Steps

1. Repository checkout
2. JDK setup
3. Gradle setup
4. Unit tests
5. Debug APK build

The workflow runs when changes are pushed to the `main` branch or when a pull request is opened against `main`.

A successful GitHub Actions run confirms that the project can be tested and built in a clean cloud environment.

## GitHub Links

[Open the LifeIndex GitHub Repository](https://github.com/Suhail-Allie/LifeIndex)

[View GitHub Actions](https://github.com/Suhail-Allie/LifeIndex/actions)

[View Android CI Workflow](https://github.com/Suhail-Allie/LifeIndex/actions/workflows/android.yml)

---

# Repository Structure

```text
LifeIndex/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/com/lifeindex/app/
│   │   │       ├── data/
│   │   │       │   ├── local/
│   │   │       │   ├── model/
│   │   │       │   ├── remote/
│   │   │       │   └── repository/
│   │   │       ├── navigation/
│   │   │       ├── ui/
│   │   │       │   ├── screens/
│   │   │       │   ├── theme/
│   │   │       │   └── viewmodel/
│   │   │       ├── util/
│   │   │       └── MainActivity.kt
│   │   │
│   │   └── test/
│   │       └── java/com/lifeindex/app/
│   │
│   └── build.gradle.kts
│
├── backend/
│   ├── controllers/
│   ├── middleware/
│   ├── routes/
│   ├── .env.example
│   ├── db.js
│   ├── jwt.js
│   ├── server.js
│   ├── tokenUtils.js
│   └── package.json
│
├── .github/
│   └── workflows/
│       └── android.yml
│
├── docs/
│   └── images/
│
├── .gitignore
├── README.md
├── gradle/
├── gradlew
├── gradlew.bat
├── build.gradle.kts
└── settings.gradle.kts
```

---

# Screenshots

The completed LifeIndex application should be documented with screenshots of the main interface.

Store the screenshots in:

`docs/images/`

Recommended files:

```text
docs/images/login.png
docs/images/dashboard.png
docs/images/trackers.png
docs/images/tracker-detail.png
docs/images/settings.png
```

## Login

![LifeIndex Login](docs/images/login.png)

## Dashboard

![LifeIndex Dashboard](docs/images/dashboard.png)

## Trackers

![LifeIndex Trackers](docs/images/trackers.png)

## Tracker Detail

![LifeIndex Tracker Detail](docs/images/tracker-detail.png)

## Settings

![LifeIndex Settings](docs/images/settings.png)

---

# Demonstration Video

The demonstration video presents the completed LifeIndex Part 2 prototype.

The video demonstrates:

- Account registration
- Login
- Authentication
- Tracker creation
- Tracker editing
- Tracker management
- Templates
- Custom fields
- Search and filtering
- Settings
- Invalid-input handling
- REST API integration
- Hosted backend and database integration

## Video Presentation

**[Watch the LifeIndex Part 2 Demonstration Video](https://youtu.be/zObcPhJk3X4)**

Direct video link:

https://youtu.be/zObcPhJk3X4

---

# Assessment Requirements Covered

The Part 2 prototype includes the required non-POE functionality:

- User registration
- User login
- Password security
- Application settings
- REST API integration
- Database integration
- Hosted backend
- Part 1 application features
- Invalid-input handling
- Automated testing
- GitHub Actions
- GitHub repository
- README documentation
- Comments
- Logging

Additional user-defined functionality includes:

- Tracker management
- Item, Process and Recurring trackers
- Reusable templates
- Custom fields
- Dashboard
- Search
- Tracker filtering
- Tracker editing
- Tracker archiving

---

# Comments and Logging

Comments have been added to important areas of the application to explain key implementation decisions and logic.

Logging is used to assist with:

- Application startup
- API troubleshooting
- Database troubleshooting
- Authentication troubleshooting
- Backend operations

Logging and comments are focused on important application behaviour rather than unnecessary comments throughout the code.

---

# AI Use Declaration

AI tools were used as development support during the implementation of LifeIndex.

ChatGPT was used for code organisation, technical explanations, debugging, UI improvements, testing ideas, deployment support and documentation.

AI assistance was used with technologies including Kotlin, Jetpack Compose, Retrofit, Node.js, Express, PostgreSQL, JWT authentication, GitHub Actions and deployment.

The application concept, feature selection, requirements, architecture and technical direction remained my responsibility. AI-generated suggestions were reviewed, adapted and tested within the LifeIndex project before being used.

AI was also used to help organise the implementation around the assessment requirements, including REST API integration, validation, automated testing, GitHub Actions, documentation and error handling.

The final implementation and testing were performed within the LifeIndex project. Generated suggestions were not accepted without checking that they worked with the existing application.

AI was therefore used as a development and learning assistant while responsibility for the final project, testing and submission remained with me.

---

# Future Development

The following features are planned for the Final POE stage:

- Google SSO
- Offline functionality and synchronisation
- Push notifications
- Multi-language support including South African languages

These features are outside the current Part 2 prototype scope.

---

# Project Links

## Source Code

[LifeIndex GitHub Repository](https://github.com/Suhail-Allie/LifeIndex)

## GitHub Actions

[LifeIndex GitHub Actions](https://github.com/Suhail-Allie/LifeIndex/actions)

## Android CI Workflow

[LifeIndex Android CI Workflow](https://github.com/Suhail-Allie/LifeIndex/actions/workflows/android.yml)

## Hosted REST API

[LifeIndex Hosted API](https://lifeindex-api.onrender.com/api/v1/)

## API Health Check

[LifeIndex API Health Check](https://lifeindex-api.onrender.com/api/v1/health)

## Database Connection Check

[LifeIndex Database Connection Check](https://lifeindex-api.onrender.com/api/v1/db-test)

## Demonstration Video

[LifeIndex Part 2 Demonstration Video](https://youtu.be/zObcPhJk3X4)

---

# Author

**Mogammad Suhail Allie**

**OPSC6312 – Open Source Coding Intermediate**

**LifeIndex**

**2026**
