# Hotel Management System

A Java 17 hotel management project with a Spring Boot web backend, MySQL database, JDBC-based data access, and a browser dashboard. The original console application is also preserved for OOP demonstration.

## Project Overview

This project manages:
- Rooms
- Customers
- Reservations
- Check-in and check-out
- Payments
- Simple reports

The web backend uses Spring Boot and Spring JDBC. The original plain-Java HTTP server remains available as a legacy/demo option.

## Features

- Add, view, search, update, and delete rooms
- Add, view, search, update, and delete customers
- Make reservations and cancel them
- Validate room availability and date ranges
- Check in and check out reservations
- Generate hotel bills
- Record and view payments
- View hotel summary reports
- SQL script with sample data

## Technologies Used

- Java 17+
- Spring Boot 3.3
- Maven
- MySQL
- JDBC
- HTML, CSS, and JavaScript dashboard
- OOP structure with DAO and Service layers

## Requirements

- Java 17 or later
- MySQL Server
- MySQL Connector/J jar
- VS Code (recommended)

## Install Java

For Windows, install the Microsoft Build of OpenJDK 17:

1. Download from https://aka.ms/download-jdk
2. Install it
3. Confirm Java is installed:

```powershell
java -version
javac -version
```

## Install MySQL

1. Download MySQL Community Server from https://dev.mysql.com/downloads/mysql/
2. Install it and set a root password
3. Start the MySQL service
4. Use the MySQL command line or Workbench to run SQL scripts

## Create the Database

After starting MySQL, run:

```sql
CREATE DATABASE hotel_management;
```

## Run the SQL Script

From the project root:

```powershell
mysql -u root -p < .\database\hotel_management.sql
```

If prompted, enter your MySQL root password.

## Configure MySQL Username and Password

Open the file:

```text
src/util/DatabaseConnection.java
```

Update these values:

```java
private static final String USERNAME = System.getenv().getOrDefault("DB_USERNAME", "root");
private static final String PASSWORD = System.getenv().containsKey("DB_PASSWORD")
        ? System.getenv("DB_PASSWORD")
        : "";
```

If your MySQL root account has no password, leave the password blank.

You can either:
- edit the file directly, or
- set environment variables before running the app:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = ""
```

## Add MySQL Connector/J

The project already includes:

```text
lib/mysql-connector-j.jar
```

If it is missing, download it from Maven Central and place it in the lib folder:

```powershell
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar" -OutFile ".\lib\mysql-connector-j.jar"
```

## Compile the Project

From the project root:

```powershell
$env:PATH = "C:\Program Files\Microsoft\jdk-17\bin;$env:PATH"

$srcFiles = Get-ChildItem -Recurse -Filter *.java -Path .\src | ForEach-Object { $_.FullName }
javac -cp ".\lib\mysql-connector-j.jar" -d .\out $srcFiles
```

## Run the Program in VS Code

From the terminal in VS Code:

```powershell
java -cp ".\out;.\lib\mysql-connector-j.jar" Main
```

Or run it from the Java debugger using the VS Code Java extension.

## Run the HTML Web Version

The main web version is now served by Spring Boot.

### Spring Boot web version

Install Maven, then from the project root run:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = ""
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

The Spring Boot entry point is `src/main/java/com/hotel/HotelApplication.java`.
The REST API is in `src/main/java/com/hotel/HotelController.java`.

For a one-click presentation startup, double-click `start-spring.bat`. Keep the terminal window open while demonstrating.
The launcher preserves `DB_USERNAME` and `DB_PASSWORD`. If Maven is unavailable, it starts the existing JAR in `target` instead.
After changing Java source code, install Maven and run `mvn package` once to rebuild that JAR before presenting.

### One-click startup

The existing `start-web.bat` still starts the original plain-Java web server. Use the Maven command above when demonstrating the Spring Boot version.

```powershell
.\start-web.bat
```

Then open:

```text
http://localhost:8080
```

## Example Usage

1. Start the MySQL server
2. Run the SQL file
3. Update the database credentials
4. Compile the code
5. Run `Main`
6. Use menu options for rooms, customers, reservations, and payments

## OOP Concepts Demonstrated

- Encapsulation: private fields with getter/setter methods
- Abstraction: abstract `Person` class and `Printable` interface
- Inheritance: `Customer` and `Staff` extend `Person`
- Polymorphism: method overriding with `displayInfo()`
- Constructors: defined in all model classes
- Method overloading: multiple overloaded validation methods
- Method overriding: `Customer.displayInfo()` and `Staff.displayInfo()`
- Interfaces: `Printable` interface used by `Person`
- Exceptions: custom exception classes for invalid data and missing records
- Collections: `List` used for DAO results and menu display

## Database Structure

Database name:

```text
hotel_management
```

Tables:

- `customers`
- `rooms`
- `reservations`
- `payments`
- `staff`

Relationships:

- `customers` -> `reservations`
- `rooms` -> `reservations`
- `reservations` -> `payments`

## Common Errors and Solutions

### 1. Communications link failure
Cause: MySQL service is not running or credentials are wrong.
Solution: start MySQL and check `DatabaseConnection.java`.

### 2. Class not found: `com.mysql.cj.jdbc.Driver`
Cause: MySQL Connector/J jar is missing.
Solution: add `lib/mysql-connector-j.jar`.

### 3. `java` command not recognized
Cause: Java is not installed or PATH is not set.
Solution: install Java 17 and add it to PATH.

### 4. `Unknown database 'hotel_management'`
Cause: SQL script was not run successfully.
Solution: run `database/hotel_management.sql` in MySQL.

### 5. Build fails with Java compile errors
Cause: package/import mismatch or syntax issue.
Solution: ensure files are saved and compile from the project root using the commands above.

## Project Structure

```text
HotelManagementSystem/
├── src/
│   ├── Main.java
│   ├── dao/
│   │   ├── CustomerDAO.java
│   │   ├── PaymentDAO.java
│   │   ├── ReservationDAO.java
│   │   └── RoomDAO.java
│   ├── model/
│   │   ├── Customer.java
│   │   ├── Payment.java
│   │   ├── Person.java
│   │   ├── Printable.java
│   │   ├── Reservation.java
│   │   ├── Room.java
│   │   └── Staff.java
│   ├── service/
│   │   ├── CustomerService.java
│   │   ├── PaymentService.java
│   │   ├── ReportService.java
│   │   ├── ReservationService.java
│   │   └── RoomService.java
│   └── util/
│       ├── CustomExceptions.java
│       ├── DatabaseConnection.java
│       └── InputValidator.java
├── database/
│   └── hotel_management.sql
├── lib/
│   └── mysql-connector-j.jar
├── .gitignore
├── README.md
└── out/
```

## Viva Preparation Script

You can explain the project in simple words:

- `Main.java` is the user interface.
- `model` classes represent data objects such as Room, Customer, Reservation, and Payment.
- `DAO` classes connect to the MySQL database and perform CRUD operations.
- `service` classes contain business logic such as room checks, reservation validation, and billing.
- `util` classes help with database configuration, validation, and exceptions.
- JDBC is used to connect Java to MySQL.
- The program follows OOP by separating responsibilities into classes.

This project is a good example of a college-level OOP and database application.

## Spring Boot backend

The original console application and plain JDBC sources under `src/` are preserved. A Spring Boot REST backend is also provided under `src/main/java/com/hotel/`, using the same MySQL schema and serving the dashboard from `src/main/resources/static`.

Set database credentials (and optionally `DB_URL`), then build and run from the project root:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-password"
mvn spring-boot:run
```

Alternatively build an executable jar with `mvn clean package` and run `java -jar target\hotel-management-1.0.0.jar`. Open `http://localhost:8080`; REST endpoints are available under `/api/rooms`, `/api/customers`, `/api/reservations`, `/api/checkin`, `/api/checkout`, `/api/payments`, `/api/bill`, and `/api/reports`.
