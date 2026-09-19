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