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


-- =============================================================================
-- PROCEDURI STOCATE (Pentru AI/Chat Service)
-- =============================================================================

SET search_path TO ai_schema;

-- 1. Statistici: Numărul total de conversații (chat-uri) într-o perioadă
CREATE OR REPLACE FUNCTION count_chats_in_period(
    p_start_date DATE,
    p_end_date DATE
)
RETURNS INTEGER AS $$
DECLARE
    chat_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO chat_count
    FROM ai_schema.chat
    WHERE created_at BETWEEN p_start_date AND (p_end_date + INTERVAL '1 day');

    RETURN chat_count;
END;
$$ LANGUAGE plpgsql;


-- 2. Statistici: Numărul total de mesaje pe rol (USER, ASSISTANT, CONTEXT)
CREATE OR REPLACE FUNCTION count_messages_by_role(
    p_role VARCHAR
)
RETURNS INTEGER AS $$
DECLARE
    message_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO message_count
    FROM ai_schema.message
    WHERE role = p_role;

    RETURN message_count;
END;
$$ LANGUAGE plpgsql;


-- 3. Obține istoricul complet al unui chat (pentru context AI)
-- Returnează: Tabel cu mesajele din chat, ordonate cronologic
CREATE OR REPLACE FUNCTION get_chat_history(
    p_chat_id BIGINT
)
RETURNS TABLE(
    message_id BIGINT,
    role VARCHAR,
    content TEXT,
    created_at TIMESTAMP WITH TIME ZONE
) AS $$
BEGIN
    RETURN QUERY
    SELECT m.message_id, m.role, m.content, m.created_at
    FROM ai_schema.message m
    WHERE m.chat_id = p_chat_id
    ORDER BY m.created_at ASC;
END;
$$ LANGUAGE plpgsql;


-- 4. GDPR / Confidențialitate: Anonimizarea conversațiilor
-- Șterge conținutul mesajelor dar păstrează structura pentru analiză statistică
CREATE OR REPLACE FUNCTION anonymize_chat_data(
    p_chat_id BIGINT
)
RETURNS VARCHAR AS $$
DECLARE
    v_rows_affected INTEGER;
BEGIN
    UPDATE ai_schema.message
    SET content = '[REDACTED]'
    WHERE chat_id = p_chat_id;

    GET DIAGNOSTICS v_rows_affected = ROW_COUNT;

    IF v_rows_affected > 0 THEN
        UPDATE ai_schema.chat
        SET title = 'Anonymized Chat #' || p_chat_id
        WHERE chat_id = p_chat_id;

        RETURN 'SUCCESS';
    ELSE
        RETURN 'CHAT_NOT_FOUND';
    END IF;
END;
$$ LANGUAGE plpgsql;


-- 5. Curățare: Ștergerea chat-urilor vechi (data retention policy)
CREATE OR REPLACE FUNCTION delete_old_chats(
    p_cutoff_date DATE
)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM ai_schema.message
    WHERE chat_id IN (
        SELECT chat_id FROM ai_schema.chat
        WHERE created_at < p_cutoff_date
    );

    WITH deleted_rows AS (
        DELETE FROM ai_schema.chat
        WHERE created_at < p_cutoff_date
        RETURNING chat_id
    )
    SELECT COUNT(*) INTO deleted_count FROM deleted_rows;

    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;


-- 6. Statistici avansate: Media mesajelor per conversație
CREATE OR REPLACE FUNCTION get_average_messages_per_chat()
RETURNS DECIMAL AS $$
DECLARE
    avg_messages DECIMAL(10, 2);
BEGIN
    SELECT COALESCE(AVG(message_count), 0)
    INTO avg_messages
    FROM (
        SELECT chat_id, COUNT(*) as message_count
        FROM ai_schema.message
        GROUP BY chat_id
    ) AS chat_stats;

    RETURN avg_messages;
END;
$$ LANGUAGE plpgsql;


-- 7. Căutare în conversații: Găsește chat-uri care conțin un anumit text
CREATE OR REPLACE FUNCTION search_chats_by_content(
    p_search_text VARCHAR
)
RETURNS TABLE(
    chat_id BIGINT,
    chat_title VARCHAR,
    matching_messages INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.chat_id,
        c.title,
        COUNT(m.message_id)::INTEGER as matching_messages
    FROM ai_schema.chat c
    INNER JOIN ai_schema.message m ON c.chat_id = m.chat_id
    WHERE m.content ILIKE '%' || p_search_text || '%'
    GROUP BY c.chat_id, c.title
    ORDER BY matching_messages DESC;
END;
$$ LANGUAGE plpgsql;


-- 8. Obține ultimele N conversații (pentru dashboard)
CREATE OR REPLACE FUNCTION get_recent_chats(
    p_limit INTEGER DEFAULT 10
)
RETURNS TABLE(
    chat_id BIGINT,
    title VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE,
    message_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.chat_id,
        c.title,
        c.created_at,
        COUNT(m.message_id) as message_count
    FROM ai_schema.chat c
    LEFT JOIN ai_schema.message m ON c.chat_id = m.chat_id
    GROUP BY c.chat_id, c.title, c.created_at
    ORDER BY c.created_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;


-- 9. Statistici pe perioadă: Activitatea zilnică (mesaje per zi)
CREATE OR REPLACE FUNCTION get_daily_message_stats(
    p_start_date DATE,
    p_end_date DATE
)
RETURNS TABLE(
    day DATE,
    user_messages BIGINT,
    assistant_messages BIGINT,
    total_messages BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        DATE(m.created_at) as day,
        COUNT(*) FILTER (WHERE m.role = 'USER') as user_messages,
        COUNT(*) FILTER (WHERE m.role = 'ASSISTANT') as assistant_messages,
        COUNT(*) as total_messages
    FROM ai_schema.message m
    WHERE DATE(m.created_at) BETWEEN p_start_date AND p_end_date
    GROUP BY DATE(m.created_at)
    ORDER BY day;
END;
$$ LANGUAGE plpgsql;


-- =============================================================================
-- PROCEDURI STOCATE (Pentru Account Service)
-- =============================================================================

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