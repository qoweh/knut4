-- Initial database setup
-- This script runs when PostgreSQL container starts for the first time

-- Create application user and database (already done via environment variables)
-- Additional initialization can be added here

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create any initial tables or data if needed
-- (Production tables should be created via Flyway migrations)

\echo 'Database initialization completed successfully!'