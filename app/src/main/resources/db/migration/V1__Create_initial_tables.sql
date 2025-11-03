-- V1__Create_initial_tables.sql
-- Initial database schema creation for employee management system
-- Replaces manual table creation in Main.scala with Flyway migration

-- Create department table
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Create employee table
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    department_id INT NOT NULL,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
);

-- Create phone table
CREATE TABLE phone (
    id SERIAL PRIMARY KEY,
    number VARCHAR(15) NOT NULL
);

-- Create employee_phone junction table
CREATE TABLE employee_phone (
    employee_id INT NOT NULL,
    phone_id INT NOT NULL,
    PRIMARY KEY (employee_id, phone_id),
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    FOREIGN KEY (phone_id) REFERENCES phone(id) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_employee_department_id ON employee(department_id);
CREATE INDEX idx_phone_number ON phone(number);