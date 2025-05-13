# Project Expense Tracker - Admin App (Android Native)

## Overview
This repository contains the Admin App component (Native Android) of a comprehensive Project Expense Tracker system designed for modern businesses and project management. This application works in conjunction with a separate User App built with MAUI.

This project is being developed as part of the COMP1661 Mobile Application Design and Development module at the University of Greenwich.

## Related Repository
- **User App (MAUI)**: [Project Expense Tracker User App](https://github.com/Hakeem-404/.MAUI-Project-Expense-Tracker-App)

## Application Purpose
Project expense tracker apps help businesses track spending to prevent budget overruns by monitoring all project costs. This Admin App allows project managers to:
- Create and manage project details
- Record and track expenses tied to specific projects 
- Search and filter project information
- Synchronize data between local storage and cloud databases

## Features
- **Project Management**
  - Create, edit, view, and delete project details
  - Track project status, budget, timelines, and requirements
  
- **Expense Management**
  - Add/edit/delete expenses for specific projects
  - Categorize expenses by type (Travel, Equipment, Materials, etc.)
  - Track payment methods and status
  
- **Data Storage & Synchronization**
  - Local SQLite database for offline functionality
  - Cloud synchronization capabilities
  
- **Search Functionality**
  - Search projects by name, description, date, status, or owner
  - Advanced filtering options

## Technical Details
- **Language**: Java
- **Platform**: Native Android
- **Minimum SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 34 (Android 14)
- **Database**: SQLite (local), Firebase (cloud)
- **Architecture Pattern**: MVVM (Model-View-ViewModel)

## Installation Instructions
1. Clone this repository:
git clone https://github.com/Hakeem-404/Project-Expense-Tracker-Android-App.git
2. Open the project in Android Studio
3. Sync Gradle and resolve any dependencies
4. Build and run on an emulator or physical device

## Project Structure
- **Models/**: Data models for projects and expenses
- **Views/**: UI implementation files
- **ViewModels/**: Business logic and data binding
- **Database/**: Database helper classes and data access objects
- **Utils/**: Utility classes and helper functions
- **Services/**: Cloud connectivity and synchronization services

## Screenshots
1. HomePage<img width="143" alt="Homepage" src="https://github.com/user-attachments/assets/eda75d2a-5ff0-4f28-9da0-40c9a1424abf" />
<img width="144" alt="Menu & Features" src="https://github.com/user-attachments/assets/7cf926fb-2458-4417-8295-bd32909a67e9" />
<img width="143" alt="Restore From Cloud Feature" src="https://github.com/user-attachments/assets/5f6ec8ed-daca-4b72-b169-75e062faf8aa" />
<img width="151" alt="Restoring loading indicator" src="https://github.com/user-attachments/assets/080a1fd7-5be8-4402-af66-187a449a03f4" />
<img width="151" alt="Restored available projects from cloid" src="https://github.com/user-attachments/assets/1a0972bb-0a3d-4636-af8d-f59a22b7593a" />
<img width="150" alt="Search Functionality" src="https://github.com/user-attachments/assets/abca95d4-f88d-4ed8-b42a-92711ad37042" />
<img width="169" alt="Advanced Search & Filter Functionality" src="https://github.com/user-attachments/assets/7d938424-b9c4-417b-9d15-c8bae5d502f3" />
<img width="169" alt="Filter result" src="https://github.com/user-attachments/assets/5e44f75c-2eda-48a8-b898-3f79e3d13029" />
<img width="169" alt="Project Details" src="https://github.com/user-attachments/assets/5196fd04-7f6f-4e15-98d7-7f71320254d0" />
<img width="133" alt="Project Expense Details" src="https://github.com/user-attachments/assets/0f04d267-dcbd-4005-be7d-53daf90e327d" />
<img width="165" alt="Edit Expense Details" src="https://github.com/user-attachments/assets/7b922040-b553-4349-aa1d-159eb4de6e0b" />
<img width="165" alt="Delete Expense" src="https://github.com/user-attachments/assets/f7d74a85-420a-459d-bfdf-8dbcdbfb0c5d" />
<img width="149" alt="Add New Project" src="https://github.com/user-attachments/assets/d7348d61-d8a4-4ed6-a4cf-e873c8eb37bb" />
<img width="153" alt="Add New Project 2" src="https://github.com/user-attachments/assets/6eaeb85c-6f82-4689-9ce7-b083eb799248" />
<img width="135" alt="Add New Expense" src="https://github.com/user-attachments/assets/88a520c1-04b4-4d3a-8130-82d6c2f36498" />

## License
This project is developed for educational purposes as part of COMP1661 at the University of Greenwich.
