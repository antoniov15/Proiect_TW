CREATE SCHEMA IF NOT EXISTS account_schema;

-- PROCEDURI STOCATE (Pentru Account Service)

SET search_path TO account_schema;

-- 1. Statistici pentru Admin: Numărul de utilizatori noi într-o perioadă
CREATE OR REPLACE FUNCTION count_new_users(
    p_start_date DATE,
    p_end_date DATE
)

RETURNS INTEGER AS $$
DECLARE
user_count INTEGER;
BEGIN
SELECT COUNT(*)
INTO user_count
FROM account_schema.accounts
WHERE created_at BETWEEN p_start_date AND p_end_date;

RETURN user_count;
END;
$$ LANGUAGE plpgsql;

-- 2. GDPR / Confidențialitate: Anonimizarea datelor unui utilizator
-- Această procedură va înlocui numele și email-ul cu date generice pentru un ID dat.

CREATE OR REPLACE FUNCTION anonymize_user_data(
    p_user_id BIGINT
)
RETURNS VARCHAR AS $$
DECLARE
v_rows_affected INTEGER;
BEGIN
UPDATE account_schema.accounts
SET user_name = 'DeletedUser_' || id,
    email = 'deleted_' || id || '@financeapp.local',
    password = 'deleted',
    role = 'INACTIVE'
WHERE id = p_user_id;

GET DIAGNOSTICS v_rows_affected = ROW_COUNT;

IF v_rows_affected > 0 THEN
        RETURN 'SUCCESS';
ELSE
        RETURN 'USER_NOT_FOUND';
END IF;
END;
$$ LANGUAGE plpgsql;

-- 3. Validare avansată la înregistrare (Check Uniqueness)
-- verifica deodata si username-ul si email-ul

CREATE OR REPLACE FUNCTION check_account_availability(
    p_email VARCHAR,
    p_username VARCHAR
)
RETURNS VARCHAR AS $$
DECLARE
v_email_exists BOOLEAN;
    v_user_exists BOOLEAN;
BEGIN
SELECT EXISTS(SELECT 1 FROM account_schema.accounts WHERE email = p_email) INTO v_email_exists;
SELECT EXISTS(SELECT 1 FROM account_schema.accounts WHERE user_name = p_username) INTO v_user_exists;

IF v_email_exists THEN
        RETURN 'EMAIL_TAKEN';
    ELSIF v_user_exists THEN
        RETURN 'USERNAME_TAKEN';
ELSE
        RETURN 'AVAILABLE';
END IF;
END;
$$ LANGUAGE plpgsql;