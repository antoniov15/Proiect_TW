CREATE SCHEMA IF NOT EXISTS account_schema;
CREATE SCHEMA IF NOT EXISTS transaction_schema;
CREATE SCHEMA IF NOT EXISTS ai_schema;


-- ALTER DATABASE finance_app_db SET search_path TO public, account_schema, transaction_schema, ai_schema;


-- PROCEDURI STOCATE (Pentru Transaction Service)

SET search_path TO transaction_schema;

-- 1. Funcție pentru calculul totalului cheltuielilor pe o lună specifică
-- Returnează: Suma totală (DECIMAL)
CREATE OR REPLACE FUNCTION calculate_monthly_expense(
    p_user_id BIGINT,
    p_month INT,
    p_year INT
)
RETURNS DECIMAL AS $$
DECLARE
total_amount DECIMAL(19, 2);
BEGIN
SELECT COALESCE(SUM(amount), 0)
INTO total_amount
FROM transaction_schema.transactions
WHERE user_id = p_user_id
  AND type = 'EXPENSE'
  AND EXTRACT(MONTH FROM date) = p_month
  AND EXTRACT(YEAR FROM date) = p_year;

RETURN total_amount;
END;
$$ LANGUAGE plpgsql;


-- 2. Procedură pentru verificarea statusului bugetului
-- Returnează: Un text ('WITHIN BUDGET', 'WARNING', 'EXCEEDED')
CREATE OR REPLACE FUNCTION check_budget_status(
    p_user_id BIGINT,
    p_budget_limit DECIMAL
)
RETURNS VARCHAR AS $$
DECLARE
current_spending DECIMAL(19, 2);
BEGIN
    -- Calculăm cheltuielile pe luna curentă
SELECT COALESCE(SUM(amount), 0)
INTO current_spending
FROM transaction_schema.transactions
WHERE user_id = p_user_id
  AND type = 'EXPENSE'
  AND date >= DATE_TRUNC('month', CURRENT_DATE);

-- Logică de business
IF current_spending > p_budget_limit THEN
        RETURN 'EXCEEDED';
    ELSIF current_spending > (p_budget_limit * 0.9) THEN
        RETURN 'WARNING'; -- Dacă a depășit 90%
ELSE
        RETURN 'WITHIN BUDGET';
END IF;
END;
$$ LANGUAGE plpgsql;


-- 3. Procedură pentru "Arhivarea" tranzacțiilor vechi
-- (Simulare: de fapt le șterge și returnează numărul lor, pentru test)
CREATE OR REPLACE FUNCTION archive_old_transactions(
    p_cutoff_date DATE
)
RETURNS INTEGER AS $$
DECLARE
deleted_count INTEGER;
BEGIN
WITH deleted_rows AS (
DELETE FROM transaction_schema.transactions
WHERE date < p_cutoff_date
        RETURNING id
    )
SELECT COUNT(*) INTO deleted_count FROM deleted_rows;

RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;