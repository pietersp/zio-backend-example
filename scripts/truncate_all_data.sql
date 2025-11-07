-- scripts/truncate_all_data.sql
-- Copy-paste-ready TRUNCATE statements for this project's tables.
-- WARNING: Destructive. Backup your data and ensure you're connected to the correct database/schema before running.

-- Safe, per-table truncates (truncate child/join tables first)
TRUNCATE TABLE employee_phone RESTART IDENTITY CASCADE;
TRUNCATE TABLE phone RESTART IDENTITY CASCADE;
TRUNCATE TABLE employee RESTART IDENTITY CASCADE;
TRUNCATE TABLE department RESTART IDENTITY CASCADE;

-- Single-line combined option (same effect)
-- TRUNCATE TABLE employee_phone, phone, employee, department RESTART IDENTITY CASCADE;
