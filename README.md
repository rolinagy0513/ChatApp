# Chat Application

## Table of Contents
- [Description](#description)
- [Installation](#installation)
- [Requirements](#requirements)
- [Features](#features)
- [Notes](#notes)


## Description

**Chat Application** is a **Java Spring Boot backend** with a **React (JavaScript) frontend** for real-time one-on-one communication between users. It manages **messages, notifications, friend requests, and online status** with a **HttpOnly cookie-based authentication system**. 

**Project Goals and Motivation:**
  - My main goal was to strengthen my backend skills by building a project that communicates through **WebSockets in real time**.  
  - This was my first attempt at such a project. Since I was learning as I built it, the planning was not optimal, and the scope ended up focusing on **one-on-one chat functionality**.  

## Installation

- Clone the repository :
  ```bash
  git clone https://github.com/rolinagy0513/OnlineStore-API.git
- Rename the application.yml.example to application.yml and fill in the required values.
- Build the project:
  ```bash
  mvn clean install
- Run the project:
  ```bash
  mvn spring-boot:run
- The API will be available at: http://localhost:8080/ by default.


  ## Requirements

  ### Backend:
    - Java 21 or higher  
    - PostgreSQL database  
    - Maven
    - JavaScript and React with VITE 
    - IDE or code editor (IntelliJ, Eclipse, etc.)
   
  ### Frontend:
    - node 20.18.0
    - npm 10.8.2
    - stompJs 7.1.1
    - sockJs client 1.6.1
    - react 19.1.0
    - react-dom 19.1.0
    - vite 6.3.5

  
  ## Features

  - HttpOnly cookie-based authentication system with multiple roles: USER, ADMIN, MANAGER.
  - Basic user search system to find potential friends.
  - Real-time friend requests via WebSockets (handled by FriendService), delivered through a personal queue to which the frontend subscribes.
  - Once a friendship is established, users are subscribed to each other’s queues and can receive live updates such as online/offline status.
  - Presence management is handled by a singleton in-memory PresenceService, which stores user statuses to avoid database-related performance issues. (Planned future migration to Redis.)
  - Messaging system that allows friends to exchange real-time messages through WebSocket queues.
  - Contact list feature where a user’s friends are displayed. Optimized with caching and asynchronous operations, and fully reactive: updates instantly when a friend is added or removed.
  - User info panel displaying friend details (name, email, status, friendsSince, etc.) with a "Remove Friend" option. Removal triggers a WebSocket event to update both sides’ contact lists automatically.
  - Responsive frontend design with structured fetching logic, WebSocket connection utilities, multiple contexts, reusable components, and proper routing.
  - Unit tests for all services.
  - Comprehensive documentation for most files and methods.


  ## Notes

- The authentication system is based on open-source solutions with some custom modifications.
- The mobile view is not fully implemented yet.
- The project is still under active development. Planned improvements include:
  - Adding a Docker configuration for easier development and deployment.
  - Migrating the presence service from in-memory storage to Redis.
  - Improving frontend responsiveness and UI.
  - Expanding features and continuously improving code quality.
