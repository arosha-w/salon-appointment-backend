# 🛏️ Smart Salon Appointment & Capacity Prediction System - Backend

**Version:** 0.0.1-SNAPSHOT  
**Built with:** Spring Boot 4.0.2 | Java 21 | PostgreSQL | JWT Authentication | Maven

A comprehensive Spring Boot REST API backend for managing salon appointments with intelligent capacity prediction, role-based access control, and advanced analytics.

---

## 📌 Project Overview

The Smart Salon Appointment Backend is a enterprise-grade REST API built with Spring Boot 4.0.2 and Java 21. It provides:

- ✅ **Smart Appointment Scheduling** - Book, reschedule, and manage appointments with real-time availability
- ✅ **Capacity Predictions** - Predict salon capacity and recommend optimal booking times
- ✅ **Role-Based Access Control** - Separate dashboards for Admin, Stylist, and Client roles
- ✅ **JWT Authentication** - Secure token-based authentication
- ✅ **Real-time Analytics** - Track peak hours, booking trends, and revenue metrics
- ✅ **PostgreSQL Database** - Reliable relational data persistence
- ✅ **RESTful APIs** - 50+ endpoints with full CORS support

---

## ✨ Key Features

### 📅 Appointment Management
- Create appointments with automatic service pricing and duration calculation
- Reschedule appointments with availability validation
- Cancel appointments with status tracking
- View appointment history and upcoming bookings
- Smart slot locking to prevent double bookings

### 🎯 Capacity Prediction System
- Real-time available slot generation for stylists
- Peak hour analysis (identifies busiest times per day)
- Booking trend analysis (7, 14, 30-day periods)
- Stylist workload tracking and performance metrics

### 👥 Multi-Role Dashboards
- **Admin:** Manage stylists, services, clients, view all appointments, analytics
- **Stylist:** View assigned appointments, manage client relationships, track performance
- **Client:** Browse stylists, book appointments, view history, manage bookings

### 🔐 Security Features
- JWT token-based authentication
- Password encryption with Bcrypt
- Role-based authorization (ADMIN, STYLIST, CLIENT)
- CORS configuration for frontend integration
- Protected API endpoints

### 📊 Advanced Analytics
- Peak hour detection
- Booking trend analysis
- Revenue tracking
- Performance metrics
- Capacity utilization monitoring

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 4.0.2 | REST API Framework |
| Java | 21+ | Programming Language (LTS) |
| PostgreSQL | 12+ | Relational Database |
| Spring Security | 6.x | Authentication & Authorization |
| Spring Data JPA | Latest | ORM with Hibernate |
| JWT (JJWT) | 0.11.5 | Token Generation & Validation |
| Maven | 3.9.0 | Build & Dependency Management |
| Lombok | Latest | Reduce Boilerplate Code |

---

## 📦 Prerequisites

- **Java 21 or higher** - Spring Boot 4.x requires Java 21+
  - [Download Java 21](https://www.oracle.com/java/technologies/downloads/)
  - Verify: `java -version`

- **Maven 3.9.0+** - Build automation tool
  - [Download Maven](https://maven.apache.org/download.cgi)
  - Verify: `mvn -version`

- **PostgreSQL 12+** - Database server
  - [Download PostgreSQL](https://www.postgresql.org/)
  - Verify: `psql --version`

- **Git** - Version control
  - [Download Git](https://git-scm.com/)

---

 Clone the Repository

```bash
git clone https://github.com/arosha-w/salon-appointment-backend.git
cd salon-appointment-backend
