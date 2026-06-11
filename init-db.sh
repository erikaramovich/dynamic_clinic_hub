#!/bin/bash
set -e

# Use PGPASSWORD so psql doesn't prompt for a password
export PGPASSWORD="$POSTGRES_PASSWORD"

# Create the appointment database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT 'CREATE DATABASE "$POSTGRES_APPOINTMENT_DB"'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$POSTGRES_APPOINTMENT_DB')\gexec
EOSQL
