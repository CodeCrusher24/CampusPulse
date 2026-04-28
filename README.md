# CampusPulse 🎓

**A Role-Based Event Management System for SMVDU**

CampusPulse is a full-stack web application that manages the complete lifecycle of campus events at Shri Mata Vaishno Devi University (SMVDU) — from creation and dual-authority approval through to publishing and student discovery.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Data Model](#data-model)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Default Seed Data](#default-seed-data)
- [User Roles & Permissions](#user-roles--permissions)
- [Event Lifecycle](#event-lifecycle)
- [Frontend](#frontend)
- [Project Structure](#project-structure)

---

## Overview

CampusPulse enforces a structured, role-based workflow where no event goes public without passing through a **dual approval gate** — one from a venue authority and one from a faculty/HOD authority. Students only ever see events that have been fully vetted and published.

There is **no self-registration**. All user accounts are created and managed by a System Admin, ensuring that only verified university members access the system.

---

## Features

- **Role-Based Access Control** — four distinct roles with separate views and permissions
- **Dual Approval Workflow** — venue authority + faculty/HOD must both approve before an event goes live
- **Admin Shortcuts** — events created by a System Admin bypass all approvals and are immediately published
- **Open-Space Auto-Approval** — events using open-space venues skip the venue-approval gate
- **Bulk User Import** — admins can upload a CSV to create many student accounts at once
- **Mandatory First-Login Password Change** — new accounts are locked until the user sets their own password
- **Soft Delete** — users are deactivated, not permanently deleted
- **Paginated APIs** — all list endpoints support pagination and sorting
- **Swagger UI** — interactive API documentation available at `/swagger-ui/index.html`
- **Academic Session Awareness** — student year-of-study is computed from their admission year
- **Alumni Tracking** — students are automatically flagged as alumni when their program completes
- **Integrated/Early-Exit Programs** — supports students who opt out of integrated programs early

---

## Architecture

```
┌──────────────────────────────────────────┐
│            Frontend (Vanilla JS)         │
│  index.html · script.js · style.css      │
│  Role-aware SPA served from the browser  │
└─────────────────────┬────────────────────┘
                      │ HTTP (REST/JSON)
                      ▼
┌──────────────────────────────────────────┐
│         Spring Boot REST API             │
│  ┌──────────┐  ┌──────────┐  ┌────────┐  │
│  │ Auth     │  │ Events   │  │ Admin  │  │
│  │ Controller│ │ Controller│ │Controller │
│  └──────────┘  └──────────┘  └────────┘  │
│         JWT (stateless, JJWT 0.11.5)     │
│         Spring Security filter chain     │
│  ┌──────────────────────────────────┐    │
│  │ Service layer (business logic)   │    │
│  └──────────────────────────────────┘    │
│  ┌──────────────────────────────────┐    │
│  │ JPA Repositories (Spring Data)   │    │
│  └──────────────────────────────────┘    │
└─────────────────────┬────────────────────┘
                      │ JDBC
                      ▼
            ┌─────────────────┐
            │   PostgreSQL    │
            │ (campus_pulse)  │
            └─────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Build | Maven |
| Utilities | Lombok, BCrypt |
| Frontend | Vanilla HTML/CSS/JavaScript (no framework) |

---

## Data Model

### Core Entities

**User** — university members; stores role, entry number, admission year, program, school, alumni status, and a `mustChangePassword` flag.

**Event** — the central entity; holds title, description, date/time, venue, capacity, registration counts, up to three categories, tags, image URL, approval state, and a lifecycle status.

**Venue** — a named location on campus. Type is either `MANAGED` (requires venue-approval) or `OPEN_SPACE` (auto-approved on event submission).

**EventCategory** — a named category with a color code and icon (e.g., Technical, Cultural, Sports).

**School** — a university school/department (e.g., School of CSE, School of Physics).

**Program** — an academic program (e.g., BTech CSE) linked to a School; holds duration, program type (UG/PG/INTEGRATED), and optional early-exit configuration.

### Enums

| Enum | Values |
|---|---|
| `UserRole` | `STUDENT`, `EVENT_COORDINATOR`, `FACULTY_AUTHORITY`, `SYSTEM_ADMIN` |
| `EventStatus` | `DRAFT`, `PENDING`, `APPROVED`, `PUBLISHED`, `REJECTED`, `CANCELLED`, `COMPLETED` |
| `VenueType` | `MANAGED`, `OPEN_SPACE` |
| `VenueApprovalAuthority` | (authority type for venue approvals) |

---

## API Reference

All endpoints are prefixed with `/api`. A running Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

### Authentication — `/api/auth`

| Method | Path | Description | Auth Required |
|---|---|---|---|
| `POST` | `/login` | Returns a JWT on valid credentials. Login is **blocked** until the default password has been changed. | No |
| `POST` | `/change-password` | Change password using old/default password. Unlocks login for new accounts. | No |

> **Default password:** when an admin creates your account, your initial password is the portion of your email before the `@` symbol (e.g., `23bcs060` for `23bcs060@smvdu.ac.in`).

### Events — `/api/events`

| Method | Path | Description | Roles |
|---|---|---|---|
| `POST` | `/` | Create a new event (DRAFT, or PUBLISHED if SYSTEM_ADMIN) | Coordinator, Faculty, Admin |
| `PUT` | `/{eventId}` | Update a DRAFT or REJECTED event | Creator, Admin |
| `POST` | `/{eventId}/submit` | Submit event for approval (DRAFT → PENDING) | Creator |
| `POST` | `/{eventId}/approve/venue` | Venue authority approves or rejects the venue booking | Faculty, Admin |
| `POST` | `/{eventId}/approve/event` | Faculty/HOD approves or rejects the event | Faculty, Admin |
| `POST` | `/{eventId}/publish` | Publish an APPROVED event (APPROVED → PUBLISHED) | Creator, Admin |
| `DELETE` | `/{eventId}` | Delete a DRAFT or REJECTED event | Creator, Admin |
| `GET` | `/` | All events paginated (admin/faculty view) | Authenticated |
| `GET` | `/published` | Published events only (student feed) | Authenticated |
| `GET` | `/my-events` | Events created by the current user | Authenticated |
| `GET` | `/pending-approvals` | Events awaiting approval | Authenticated |
| `GET` | `/{eventId}` | Single event by ID | Authenticated |

### Admin — `/api/admin`

| Method | Path | Description |
|---|---|---|
| `POST` | `/users` | Create a single user account |
| `POST` | `/users/admin` | Create a System Admin account |
| `POST` | `/users/bulk` | Bulk import users from a CSV file |
| `GET` | `/users` | List all users (paginated) |
| `GET` | `/users/{id}` | Get a user by ID |
| `PUT` | `/users/{id}/role` | Promote or demote a user's role |
| `POST` | `/users/{id}/reset-password` | Reset a user's password to default |
| `PUT` | `/users/{id}/deactivate` | Soft-delete / deactivate a user |
| `PUT` | `/users/{id}/reactivate` | Reactivate a deactivated user |

### Other Endpoints

| Prefix | Description |
|---|---|
| `/api/venues` | List and manage campus venues |
| `/api/events/categories` | List and manage event categories |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### 1. Clone the repository

```bash
git clone https://github.com/CodeCrusher24/CampusPulse.git
cd CampusPulse
```

### 2. Set up the database

```sql
CREATE DATABASE campus_pulse;
```

### 3. Configure the application

```bash
cd backend/src/main/resources
cp application-example.properties application.properties
```

Edit `application.properties` with your database credentials (see [Configuration](#configuration)).

### 4. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`. On first run, the `DataLoader` automatically seeds schools, programs, venues, event categories, and the first admin account.

### 5. Open the frontend

Open `frontend/index.html` directly in your browser or serve it with any static file server:

```bash
cd frontend
npx serve .
```

The frontend connects to `http://localhost:8080` by default.

---

## Configuration

Copy `application-example.properties` to `application.properties` and update the following:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/campus_pulse
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# Academic session — set to the ending year of the current session
# e.g., for 2025–2026, use 2026
app.session.current-year=2026
```

`spring.jpa.hibernate.ddl-auto=update` means Hibernate will create and update tables automatically — no manual migrations needed for development.

---

## Default Seed Data

On first startup, `DataLoader` seeds the following:

- **First Admin** — `admin@smvdu.ac.in` / password: `admin` (must change on first login)
- **Schools** — CSE, ECE, Physics, Business, and more SMVDU schools
- **Programs** — BTech, MTech, BCA, MCA, Integrated programs with correct durations and early-exit configs
- **Venues** — Lecture theatres, seminar halls, open grounds, and auditoriums
- **Event Categories** — Technical, Cultural, Sports, Workshop, etc. with color codes and icons

---

## User Roles & Permissions

| Role | Can Do |
|---|---|
| **STUDENT** | View and browse published events |
| **EVENT_COORDINATOR** | Create, edit, and submit events for approval; view their own events |
| **FACULTY_AUTHORITY** | Approve or reject venue bookings and events; view all events and pending approvals |
| **SYSTEM_ADMIN** | Full access; create/manage user accounts; create events that bypass approval and publish immediately |

---

## Event Lifecycle

```
                  ┌─────────┐
         create   │  DRAFT  │  edit / delete allowed
  ─────────────►  └────┬────┘
                       │ submit
                       ▼
                  ┌─────────┐
                  │ PENDING │  awaiting dual approval
                  └────┬────┘
          ┌────────────┴────────────┐
          │ venue gate              │ event gate
          │ (FACULTY_AUTHORITY)     │ (FACULTY_AUTHORITY)
          ▼                         ▼
    venueApproved=true        eventApproved=true
          └────────────┬────────────┘
                       │ both gates pass
                       ▼
                  ┌──────────┐
                  │ APPROVED │
                  └────┬─────┘
                       │ publish (by creator or admin)
                       ▼
                  ┌───────────┐
                  │ PUBLISHED │  visible to students
                  └───────────┘

  At any point:
  • Either authority rejects → REJECTED  (can edit and resubmit)
  • Creator/admin cancels  → CANCELLED
  • Event date passes      → COMPLETED
```

**Special cases:**
- Events created by **SYSTEM_ADMIN** skip all gates and go straight to **PUBLISHED**.
- Events using an **OPEN_SPACE** venue have the venue gate pre-approved on submission.
- Events with a **custom location** (no registered venue) also have the venue gate pre-approved.

---

## Frontend

The frontend is a role-aware single-page application built with plain HTML, CSS, and JavaScript — no framework required.

**Auth flow:**
1. User visits the app and sees the Sign In screen.
2. First-time users click "Set your password" to complete the mandatory password change.
3. After login, a JWT is stored in `localStorage` and the app screen loads.

**Navigation is built dynamically based on role:**

| Role | Sidebar items |
|---|---|
| Student | Events |
| Event Coordinator | Events, My Events |
| Faculty Authority | Events, Pending Approvals, All Events |
| System Admin | Events, All Events, Pending Approvals, Users |

**Fonts:** Syne (headings) + DM Sans (body) via Google Fonts.

---

## Project Structure

```
CampusPulse/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/parag/campuspulse/
│       ├── CampuspulseApplication.java
│       ├── config/
│       │   ├── DataLoader.java          # Seeds DB on first run
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── SecurityConfig.java
│       │   └── SwaggerConfig.java
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── AdminController.java
│       │   ├── EventController.java
│       │   ├── EventCategoryController.java
│       │   └── VenueController.java
│       ├── dto/
│       │   ├── AdminDTOs.java
│       │   ├── EventDTOs.java
│       │   └── JwtResponse.java
│       ├── model/
│       │   ├── User.java
│       │   ├── UserRole.java
│       │   ├── Event.java
│       │   ├── EventStatus.java
│       │   ├── EventCategory.java
│       │   ├── Venue.java
│       │   ├── VenueType.java
│       │   ├── VenueApprovalAuthority.java
│       │   ├── School.java
│       │   └── Program.java
│       ├── repository/
│       │   ├── UserRepository.java
│       │   ├── EventRepository.java
│       │   ├── EventCategoryRepository.java
│       │   ├── VenueRepository.java
│       │   ├── SchoolRepository.java
│       │   └── ProgramRepository.java
│       ├── service/
│       │   ├── UserService.java
│       │   ├── AdminService.java
│       │   ├── EventService.java
│       │   ├── VenueService.java
│       │   ├── CsvImportService.java
│       │   └── StudentParserService.java
│       └── util/
│           ├── JwtUtil.java
│           ├── EmailValidator.java
│           └── PasswordValidator.java
└── frontend/
    ├── index.html
    ├── script.js
    └── style.css
```

---

## Password Policy

Passwords must be **8–20 characters** and contain at least one uppercase letter, one lowercase letter, one digit, and one special character.

---

## License

This project is currently unlicensed. All rights reserved by the author.
