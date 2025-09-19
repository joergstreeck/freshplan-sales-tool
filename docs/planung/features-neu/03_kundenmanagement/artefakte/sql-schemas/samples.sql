-- Samples (PostgreSQL)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sample_status') THEN
    CREATE TYPE sample_status AS ENUM ('REQUESTED','PACKED','SHIPPED','DELIVERED','FEEDBACK_SUCCESS','FEEDBACK_NEUTRAL','FEEDBACK_FAIL','CANCELED');
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS sample_request (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   uuid NOT NULL,
  status        sample_status NOT NULL DEFAULT 'REQUESTED',
  delivery_date date,
  delivery_address text,
  contact_email text,
  notes         text,
  created_by    uuid,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sample_item (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  sample_id   uuid NOT NULL REFERENCES sample_request(id) ON DELETE CASCADE,
  sku         text NOT NULL,
  quantity    int  NOT NULL CHECK (quantity > 0)
);

CREATE OR REPLACE FUNCTION trg_recompute_hot_on_sample() RETURNS trigger AS $$
DECLARE v_customer uuid;
BEGIN
  IF (TG_OP = 'INSERT') THEN v_customer := NEW.customer_id;
  ELSE v_customer := COALESCE(NEW.customer_id, OLD.customer_id);
  END IF;
  PERFORM recompute_customer_hot(v_customer);
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS t_sample_recompute ON sample_request;
CREATE TRIGGER t_sample_recompute
AFTER INSERT OR UPDATE ON sample_request
FOR EACH ROW EXECUTE FUNCTION trg_recompute_hot_on_sample();
