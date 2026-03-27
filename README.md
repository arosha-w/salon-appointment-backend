# Salon Appointment Backend

## Project Overview
The Salon Appointment Backend is a robust and flexible API server designed to manage appointments for salons and spas. It offers a complete solution for scheduling, managing, and tracking salon appointments, allowing for seamless interaction between clients and salon staff.

## Features
- User Authentication and Authorization
- Appointment Scheduling and Management
- Notification System for Appointment Reminders
- Salon and Service Management
- Analytics Dashboard for Appointment Statistics
- Support for Multiple Service Providers

## Tech Stack
- **Backend:** Node.js, Express.js
- **Database:** MongoDB
- **Authentication:** JWT (JSON Web Token)
- **Containerization:** Docker
- **Testing Framework:** Jest

## Quick Start Guide
1. **Clone the repository:**
   ```bash
   git clone https://github.com/arosha-w/salon-appointment-backend.git
   cd salon-appointment-backend
   ```
2. **Install dependencies:**
   ```bash
   npm install
   ```
3. **Environment Setup:**
   Create a `.env` file in the root of the project and configure your environment variables.
4. **Run the application:**
   ```bash
   npm start
   ```
5. **Access the API:**
   The API will be available at `http://localhost:3000`.

## API Endpoints
- **GET /api/appointments** - Retrieve all appointments
- **POST /api/appointments** - Create a new appointment
- **PUT /api/appointments/:id** - Update an existing appointment
- **DELETE /api/appointments/:id** - Delete an appointment

## Docker Deployment
1. **Build the Docker image:**
   ```bash
   docker build -t salon-appointment-backend .
   ```
2. **Run the Docker container:**
   ```bash
   docker run -p 3000:3000 salon-appointment-backend
   ```
3. **Access the API:**
   The API will be running inside the Docker container at `http://localhost:3000`.

## Troubleshooting Guide
- **Common Issues:**
  - Ensure that your MongoDB service is running.
  - Double-check the environment variables in the `.env` file.
  - Check for network issues if accessing the API from a different host.
- **Debugging:**
  - Use console logs to track the flow of data in the application.
  - Utilize tools like Postman for API testing and validation.
  - Refer to logs for any error messages during runtime.

---

For more detailed documentation, please refer to the [project wiki](https://github.com/arosha-w/salon-appointment-backend/wiki).