# Human Resource Management System (HRMS)

## 📌 Overview
This is a Spring Boot backend application designed to manage employee data in an organization. It provides REST APIs for performing CRUD operations along with advanced backend features like pagination, filtering, validation, exception handling, and security.

---

## 🛠️ Tech Stack
- Java 17+
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL
- Spring Security
- Maven

---

## 🚀 Features

### Employee Management
- Create Employee
- Update Employee
- Delete Employee
- Get Employee by ID
- Get All Employees

### Advanced Backend Features
- Pagination support
- Sorting support
- Filtering based on fields (e.g., department, name, salary)
- Input validation using Bean Validation (@NotNull, @Email, @Size, etc.)
- Global exception handling using @ControllerAdvice

### Security (if implemented)
- Spring Security integration
- Authentication for protected APIs (if applicable)
- Role-based access control (if applicable)

---

## 📂 Project Architecture
The project follows a layered architecture:

Controller → Service → Repository → Database

---

## 🔗 API Endpoints

### Employee APIs
GET    /api/employees           → Get all employees (with pagination/filtering)
GET    /api/employees/{id}      → Get employee by ID
POST   /api/employees           → Create new employee
PUT    /api/employees/{id}      → Update employee
DELETE /api/employees/{id}      → Delete employee

---

### Pagination Example
GET /api/employees?page=0&size=5

---

### Filtering Example
GET /api/employees?department=IT

---

## ⚙️ Validation Rules
- Employee name must not be empty
- Email must be valid format
- Salary must be a positive value

---

## ❗ Exception Handling
- EmployeeNotFoundException for invalid employee IDs
- Global exception handler using @ControllerAdvice

---

## 🧪 Testing
- Tested using Postman
- Verified all CRUD operations
- Checked validation and error responses

---

## ▶️ How to Run the Project

### 1. Clone the repository
git clone <your-repo-url>

---

### 2. Configure database in application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/hrms
spring.datasource.username=root
spring.datasource.password=your_password

---

### 3. Run the application
mvn spring-boot:run

---

## 📌 Future Improvements
- JWT-based authentication
- Role-based authorization
- Docker containerization
- Deployment on cloud (AWS / Render / Railway)
- API documentation using Swagger

---

## 👨‍💻 Author
Gurushiddalingayya K Kalmath

---

## 📎 Note
This project follows a standard layered architecture used in real-world Spring Boot backend applications.
