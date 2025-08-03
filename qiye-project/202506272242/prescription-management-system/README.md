# Prescription Management System

## Overview

The Prescription Management System is designed to provide a comprehensive platform for managing prescriptions and drug information within healthcare institutions. This system facilitates the entire lifecycle of prescription management, ensuring efficient, secure, and stable operations.

## Features

- **Standardized Management**: Unified data standards for prescriptions and drug information.
- **Role-Based Access Control**: Different access levels for doctors, pharmacists, and administrators.
- **Visual Representation**: Intuitive display of prescription compositions and drug details through images and structured text.
- **Efficient Interaction**: User-friendly interface for seamless front-end and back-end interactions.

## Project Structure

The project is divided into two main parts: **Backend** and **Frontend**.

### Backend

- **Java 11**: The backend is built using Java 11 and Spring Boot 2.7.
- **MyBatis-Plus**: For database operations with SQLite.
- **Spring Security**: For JWT authentication and role-based access control.
- **Database**: SQLite is used for data storage, with a master-slave configuration for read and write operations.

### Frontend

- **Vue 3**: The frontend is developed using Vue 3 with Vite as the build tool.
- **Element Plus**: UI components for a responsive and modern interface.
- **Vuex**: State management for handling application data.
- **Axios**: For making HTTP requests to the backend API.

## Getting Started

### Prerequisites

- Java 11
- Node.js 14+
- Maven

### Installation

1. **Clone the repository**:
   ```
   git clone <repository-url>
   cd prescription-management-system
   ```

2. **Backend Setup**:
   - Navigate to the `backend` directory.
   - Run the following command to install dependencies:
     ```
     mvn install
     ```
   - Start the backend application:
     ```
     mvn spring-boot:run
     ```

3. **Frontend Setup**:
   - Navigate to the `frontend` directory.
   - Install dependencies:
     ```
     npm install
     ```
   - Start the frontend application:
     ```
     npm run dev
     ```

## API Documentation

Refer to the backend `README.md` for detailed API endpoints and usage.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.