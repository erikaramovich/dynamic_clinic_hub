-- 1. Drop the old single-column unique constraints
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_name_key;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;

-- 2. Add the new composite unique constraints
-- This allows multiple users with the same name/email AS LONG AS their roles are different
ALTER TABLE users ADD CONSTRAINT uk_name_role UNIQUE (name, role);
ALTER TABLE users ADD CONSTRAINT uk_email_role UNIQUE (email, role);