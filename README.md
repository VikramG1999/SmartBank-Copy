# SmartBank – Banking Management System

SmartBank is a full-stack banking application built to demonstrate banking operations using Java, Spring Boot, React.js, and MySQL.

## Technologies Used

- **Backend:** Java, Spring Boot, Spring Data JPA, Spring Security
- **Frontend:** React.js
- **Database:** MySQL
- **Authentication:** JWT
- **Email:** Gmail SMTP for password-reset emails
- **Build Tool:** Maven

## Features

- User registration and login
- Secure authentication using JWT
- Bank account management
- Money transfers
- Beneficiary management
- Fixed deposit management
- Transaction management
- Forgot-password and password-reset functionality
- Admin functionality

## Backend Setup

1. Clone this repository.
2. Open the project in Eclipse or another Java IDE.
3. Create a MySQL database named `smartbank`.
4. Copy `secrets.properties.example` to `secrets.properties`.
5. Replace the placeholder values with your local database and email credentials.
6. Run the Spring Boot application.

## Configuration

Keep real passwords, JWT secrets, and other credentials in `secrets.properties`. Never commit this file to GitHub.

## Project Status

The backend source code is available. Frontend and backend deployment will be configured separately.

## Author

Vikram Gorad
