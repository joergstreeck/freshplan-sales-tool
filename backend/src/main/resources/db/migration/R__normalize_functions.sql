-- R__normalize_functions.sql
-- Repeatable Migration: Normalization Functions
-- These functions can be updated and will be re-applied by Flyway
-- Author: team/leads-backend
-- Updated: 2025-09-28

-- =====================================================
-- EMAIL NORMALIZATION
-- =====================================================
CREATE OR REPLACE FUNCTION normalize_email(email_input TEXT)
RETURNS VARCHAR AS $$
BEGIN
  IF email_input IS NULL OR TRIM(email_input) = '' THEN
    RETURN NULL;
  END IF;
  -- Simple normalization: lowercase, trim
  RETURN LOWER(TRIM(email_input));
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION normalize_email IS
  'Normalizes email addresses for deduplication (lowercase, trimmed)';

-- =====================================================
-- COMPANY NAME NORMALIZATION
-- =====================================================
CREATE OR REPLACE FUNCTION normalize_company_name(company_input TEXT)
RETURNS TEXT AS $$
BEGIN
  IF company_input IS NULL OR TRIM(company_input) = '' THEN
    RETURN NULL;
  END IF;
  -- Remove diacritics, lowercase, normalize whitespace
  RETURN regexp_replace(
    LOWER(unaccent(TRIM(company_input))),
    '\s+', ' ', 'g'
  );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION normalize_company_name IS
  'Normalizes company names (lowercase, no diacritics, single spaces)';

-- =====================================================
-- PHONE NORMALIZATION (Simple E.164-like)
-- =====================================================
CREATE OR REPLACE FUNCTION normalize_phone(phone_input TEXT, default_country_code TEXT DEFAULT '+49')
RETURNS TEXT AS $$
DECLARE
  cleaned TEXT;
BEGIN
  IF phone_input IS NULL OR TRIM(phone_input) = '' THEN
    RETURN NULL;
  END IF;

  -- Remove all non-digit characters except leading +
  cleaned := regexp_replace(phone_input, '[^0-9+]', '', 'g');

  -- Handle different formats
  IF cleaned ~ '^\+' THEN
    -- Already has country code
    RETURN cleaned;
  ELSIF cleaned ~ '^00' THEN
    -- International format with 00
    RETURN '+' || substring(cleaned from 3);
  ELSIF cleaned ~ '^0' THEN
    -- National format, add default country code
    RETURN default_country_code || substring(cleaned from 2);
  ELSE
    -- Assume it needs country code
    RETURN default_country_code || cleaned;
  END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION normalize_phone IS
  'Simple phone normalization to E.164-like format (without full validation)';

-- =====================================================
-- EXTRACT DOMAIN FROM EMAIL/WEBSITE
-- =====================================================
CREATE OR REPLACE FUNCTION extract_domain(input_text TEXT)
RETURNS TEXT AS $$
DECLARE
  domain TEXT;
BEGIN
  IF input_text IS NULL OR TRIM(input_text) = '' THEN
    RETURN NULL;
  END IF;

  -- If it looks like email, extract domain part
  IF input_text LIKE '%@%' THEN
    domain := LOWER(TRIM(split_part(input_text, '@', 2)));
  -- If it's a URL, extract domain
  ELSIF input_text LIKE 'http%://%' THEN
    domain := LOWER(TRIM(split_part(split_part(input_text, '://', 2), '/', 1)));
  ELSE
    -- Assume it's already a domain
    domain := LOWER(TRIM(input_text));
  END IF;

  -- Remove www. prefix if present
  IF domain LIKE 'www.%' THEN
    domain := substring(domain from 5);
  END IF;

  RETURN domain;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION extract_domain IS
  'Extracts domain from email address or website URL';

-- =====================================================
-- LEAD NORMALIZATION TRIGGER
-- =====================================================
CREATE OR REPLACE FUNCTION leads_normalize_trigger()
RETURNS TRIGGER AS $$
BEGIN
  -- Normalize email if present
  IF NEW.email IS NOT NULL THEN
    NEW.email_normalized := normalize_email(NEW.email);
  END IF;

  -- Normalize company name if present
  IF NEW.company_name IS NOT NULL THEN
    NEW.company_name_normalized := normalize_company_name(NEW.company_name);
  END IF;

  -- Extract domain from website or email
  IF NEW.website IS NOT NULL THEN
    NEW.website_domain := extract_domain(NEW.website);
  ELSIF NEW.email IS NOT NULL THEN
    NEW.website_domain := extract_domain(NEW.email);
  END IF;

  -- Note: phone_e164 should be set by application with proper library

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create or replace trigger
DROP TRIGGER IF EXISTS leads_normalize_before_insert_update ON leads;
CREATE TRIGGER leads_normalize_before_insert_update
  BEFORE INSERT OR UPDATE ON leads
  FOR EACH ROW
  EXECUTE FUNCTION leads_normalize_trigger();