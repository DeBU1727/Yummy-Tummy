# 🍔 Yummy-Tummy Backend Architecture

This repository contains the backend source code and server infrastructure for **Yummy-Tummy**, a sit-dine and food delivery web application. The backend is built using a dual-stack approach, utilizing both **Node.js** and **Java** to handle routing, secure authentication, database management, and business logic.

## ✨ Core Backend Features

* **Secure OTP Authentication:** Handles the generation and verification of secure, passwordless One-Time Passwords (OTPs) via email.
* **Reliable Email Service:** Integrates the **Brevo API** for safe, non-spam delivery of OTPs *(Users are advised to check spam folders if emails are delayed)*.
* **Database Management:** Connects to **MySQL** to manage the curated menu, user sessions, and cart states.
* **Simulated Checkout Logic:** Processes backend validation for the simulated payment gateway, designed strictly for educational purposes.
* **RESTful Endpoints:** Provides reliable APIs for the React frontend to fetch data and process orders.

## 🛠️ Tech Stack

* **Node.js:** Server environment, API routing, and microservices integration.
* **Java:** Core business logic and backend processing.
* **Database:** MySQL
* **External Services:** Brevo (Transactional Email API for OTPs)

## 🚀 Getting Started

Follow these instructions to set up and run the dual-stack backend services locally.

### Prerequisites

* [Node.js](https://nodejs.org/) installed
* [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) installed
* [MySQL Server](https://www.mysql.com/) installed and running locally
* A [Brevo](https://www.brevo.com/) account and API Key

### 1. Database Setup

1. Open your MySQL client.
2. Create the database: `CREATE DATABASE yummy_tummy;`
3. Execute the provided `.sql` schema file (if applicable) to generate the tables for the menu and users.

### 2. Environment Variables (.env)

Create a `.env` file in your root backend directory and configure the following parameters:

```env
# Database Configuration
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_mysql_password
DB_NAME=yummy_tummy

# Brevo API Configuration
BREVO_API_KEY=your_brevo_api_key_here
BREVO_SENDER_EMAIL=your_verified_email@domain.com

# Ports
NODE_PORT=5000
JAVA_PORT=8080
