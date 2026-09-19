# LifeIndex

### Track anything. Find everything.

LifeIndex is a personal life administration Android application designed to help users keep important information, responsibilities and records organised in one place.

Instead of creating separate systems for different parts of life, LifeIndex uses a reusable tracker system that allows users to create different types of trackers based on their needs.

---

## Project Overview

LifeIndex was developed as part of the OPSC6312 Open Source Coding Intermediate Portfolio of Evidence.

The application focuses on providing a simple way for users to store and manage important personal records and responsibilities.

Examples include:

- Passports
- Vehicles
- Qualifications
- Job applications
- Travel information
- Recurring services and renewals

The application uses a template-driven tracker system so that the same core functionality can be reused for many different types of information.

---

## Main Features

### User Authentication

Users can:

- Register a new account
- Log in
- Log out
- Maintain an authenticated session
- Use password-protected accounts

Passwords are hashed on the backend before being stored.

The application uses JWT access tokens and refresh tokens for authenticated API access.

---

### Tracker Management

Users can create and manage trackers using three tracker types:

- Item
- Process
- Recurring

Trackers can be:

- Created
- Viewed
- Edited
- Deleted
- Archived

Each tracker can contain information such as:

- Title
- Type
- Status
- Priority
- Important date
- Notes
- Custom fields

---

### Templates

LifeIndex includes reusable templates for common use cases.

Current templates include examples such as:

- Passport
- Vehicle
- Job Application
- Qualification
- Travel

Templates allow related information to be captured consistently without creating a completely different system for each use case.

---

### Custom Fields

Tracker information is stored using a flexible field system.

Supported field types include:

- Text
- Number
- Date
- Yes/No
- Choice

This allows different trackers to capture different information while still using the same tracker engine.

---

### Dashboard

The dashboard provides an overview of the user's trackers.

It includes:

- Total trackers
- Due today
- Upcoming trackers
- Overdue trackers
- Recently updated trackers

This gives the user a quick view of important information when opening the application.

---

### Search and Filtering

Users can search trackers by title and filter them by tracker type.

This helps users quickly find specific records as the number of trackers grows.

---

### Settings

Users can update application preferences including:

- Theme
- Reminder preferences
- Notification preferences

Settings are stored through the REST API and database.

---

## Deployment

The LifeIndex backend is deployed using Render.

The backend is built with Node.js and Express and connects to a PostgreSQL database hosted through Supabase.

### Hosted API

Base URL:

https://lifeindex-api.onrender.com/api/v1/

Health check:

https://lifeindex-api.onrender.com/api/v1/health

Database connection check:

https://lifeindex-api.onrender.com/api/v1/db-test

The Android application communicates with the hosted API over HTTPS.

Sensitive environment variables are stored securely in the hosting environment and are not committed to the repository.


## Testing

LifeIndex includes automated unit tests and GitHub Actions.

### Unit Tests

```bash
./gradlew testDebugUnitTest



The README requirement is explicit in the brief, including design considerations and GitHub/GitHub Actions usage. :contentReference[oaicite:2]{index=2}

---


```text
AI USE DECLARATION – LIFEINDEX PART 2

AI tools were used as development support during the implementation of LifeIndex. ChatGPT was used for code organisation, technical explanations, debugging, UI improvements, testing ideas and documentation.

AI assistance was used with Kotlin, Jetpack Compose, Retrofit, Node.js, Express, PostgreSQL, JWT authentication, GitHub Actions and deployment. It assisted with identifying and resolving compilation errors, API connection issues, authentication problems and deployment issues.

The application concept, feature selection, requirements, architecture and technical direction remained my responsibility. AI-generated suggestions were reviewed, adapted and tested within the LifeIndex project before being used.

AI was also used to help organise the implementation around the assessment requirements, including REST API integration, validation, automated testing, GitHub Actions, documentation and error handling.

The final implementation and testing were performed within the LifeIndex project, and generated suggestions were not accepted without checking that they worked with the existing application.

AI was therefore used as a development and learning assistant while responsibility for the final project, testing and submission remained with me.



## Technology Stack

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Android Studio
- Retrofit
- Gson
- ViewModel

### Backend

- Node.js
- Express.js
- REST API
- JSON Web Tokens
- bcryptjs

### Database

- PostgreSQL
- Supabase

### Hosting

- Render

### Version Control

- Git
- GitHub
- GitHub Actions

---

## System Architecture

```text
                         LifeIndex Android App
                                  |
                                  | HTTPS / REST API
                                  v
                         Render Web Service
                                  |
                                  | Node.js / Express
                                  v
                         Supabase PostgreSQL


