CREATE DATABASE IF NOT EXISTS hotel_management;
USE hotel_management;

DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS staff;

CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    government_id VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE rooms (
    room_number INT PRIMARY KEY,
    room_type ENUM('SINGLE', 'DOUBLE', 'DELUXE', 'SUITE') NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    availability_status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE reservations (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    room_number INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_guests INT NOT NULL,
    reservation_status ENUM('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (room_number) REFERENCES rooms(room_number)
);

CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    reservation_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'UPI') NOT NULL,
    payment_status ENUM('PENDING', 'PAID', 'FAILED') NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

CREATE TABLE staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

INSERT INTO customers (full_name, phone_number, email, address, government_id) VALUES
('Aarav Sharma', '+91 9876543210', 'aarav.sharma@example.com', 'Bengaluru, Karnataka', 'ID101'),
('Priya Nair', '+91 8765432109', 'priya.nair@example.com', 'Kochi, Kerala', 'ID102'),
('Rohit Verma', '+91 7654321098', 'rohit.verma@example.com', 'Hyderabad, Telangana', 'ID103'),
('Sneha Patel', '+91 6543210987', 'sneha.patel@example.com', 'Ahmedabad, Gujarat', 'ID104'),
('Kabir Singh', '+91 5432109876', 'kabir.singh@example.com', 'Delhi, Delhi', 'ID105');

INSERT INTO rooms (room_number, room_type, price_per_night, availability_status) VALUES
(101, 'SINGLE', 2500.00, 'AVAILABLE'),
(102, 'DOUBLE', 4200.00, 'AVAILABLE'),
(103, 'DELUXE', 5800.00, 'OCCUPIED'),
(104, 'SUITE', 9000.00, 'AVAILABLE'),
(201, 'SINGLE', 2600.00, 'AVAILABLE'),
(202, 'DOUBLE', 4300.00, 'AVAILABLE'),
(203, 'DELUXE', 6100.00, 'MAINTENANCE'),
(204, 'SUITE', 9800.00, 'AVAILABLE');

INSERT INTO staff (full_name, phone_number, email, address, role, username, password_hash) VALUES
('Meera Iyer', '+91 9988776655', 'meera.iyer@example.com', 'Chennai, Tamil Nadu', 'Manager', 'meera_manager', 'hashed_password_1'),
('Aditya Rao', '+91 9911223344', 'aditya.rao@example.com', 'Pune, Maharashtra', 'Front Desk', 'aditya_frontdesk', 'hashed_password_2'),
('Nisha Khan', '+91 9822334455', 'nisha.khan@example.com', 'Jaipur, Rajasthan', 'Housekeeping', 'nisha_housekeeping', 'hashed_password_3');

INSERT INTO reservations (customer_id, room_number, check_in_date, check_out_date, number_of_guests, reservation_status, total_amount) VALUES
(1, 101, '2026-08-10', '2026-08-14', 2, 'CHECKED_OUT', 10000.00),
(2, 103, '2026-08-15', '2026-08-18', 3, 'CHECKED_IN', 17400.00),
(3, 104, '2026-08-20', '2026-08-25', 2, 'PENDING', 45000.00);

INSERT INTO payments (reservation_id, amount, payment_date, payment_method, payment_status) VALUES
(1, 10000.00, '2026-08-10', 'CARD', 'PAID'),
(2, 17400.00, '2026-08-15', 'UPI', 'PAID');
