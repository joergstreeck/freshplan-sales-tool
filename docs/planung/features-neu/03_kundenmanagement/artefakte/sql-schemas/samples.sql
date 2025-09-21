-- samples.sql (Module 03) – Cook&Fresh® Sample-Box Tracking
CREATE TYPE sample_status AS ENUM ('REQUESTED','PACKED','IN_TRANSIT','DELIVERED','FEEDBACK_SUCCESS','FEEDBACK_NEUTRAL','FEEDBACK_FAIL','CANCELED');

CREATE TABLE IF NOT EXISTS sample_request (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id uuid NOT NULL,
  territory text NOT NULL,
  status sample_status NOT NULL DEFAULT 'REQUESTED',
  delivery_date date,
  delivery_address text,
  notes text,
  created_by uuid,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sample_item (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  sample_id uuid NOT NULL REFERENCES sample_request(id) ON DELETE CASCADE,
  sku text NOT NULL,
  quantity int NOT NULL CHECK (quantity > 0)
);

-- Indexing
CREATE INDEX IF NOT EXISTS ix_sample_request_customer ON sample_request(customer_id);
CREATE INDEX IF NOT EXISTS ix_sample_request_status ON sample_request(status);
CREATE INDEX IF NOT EXISTS ix_sample_request_territory ON sample_request(territory);

-- RLS by territory
ALTER TABLE sample_request ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_sample_read ON sample_request USING (territory = current_setting('app.territory', true));
CREATE POLICY rls_sample_write ON sample_request FOR UPDATE USING (territory = current_setting('app.territory', true)) WITH CHECK (territory = current_setting('app.territory', true));
