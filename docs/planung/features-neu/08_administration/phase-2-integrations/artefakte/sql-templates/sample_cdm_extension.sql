-- Migration: $(./scripts/get-next-migration.sh)__sample_cdm_extension.sql
-- SampleBox/TestPhase/TestResult/ProductFeedback for FreshPlan

CREATE TABLE IF NOT EXISTS sample_box (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id     uuid NOT NULL,
  lead_id       uuid NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
  account_id    uuid NOT NULL,
  configured_by uuid NOT NULL,
  configured_at timestamptz NOT NULL DEFAULT now(),
  notes         text
);
CREATE TABLE IF NOT EXISTS sample_box_item (
  box_id        uuid NOT NULL REFERENCES sample_box(id) ON DELETE CASCADE,
  sku           text NOT NULL,
  qty           integer NOT NULL CHECK (qty > 0),
  PRIMARY KEY (box_id, sku)
);
CREATE TABLE IF NOT EXISTS test_phase (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id     uuid NOT NULL,
  lead_id       uuid NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
  account_id    uuid NOT NULL,
  start_date    date NOT NULL,
  end_date      date,
  stage         text NOT NULL CHECK (stage IN ('SAMPLE','TRIAL','PRODUCTION_CANDIDATE')),
  external_feedback_token text UNIQUE,
  created_by    uuid NOT NULL,
  created_at    timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS test_result (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  phase_id      uuid NOT NULL REFERENCES test_phase(id) ON DELETE CASCADE,
  sku           text NOT NULL,
  outcome       text NOT NULL CHECK (outcome IN ('SUCCESS','MIXED','FAIL')),
  sell_through  numeric(6,2),
  waste_delta   numeric(6,2),
  notes         text,
  recorded_at   timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS product_feedback (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  phase_id      uuid NOT NULL REFERENCES test_phase(id) ON DELETE CASCADE,
  contact_id    uuid,
  sku           text,
  rating        integer CHECK (rating BETWEEN 1 AND 5),
  comment       text,
  source        text NOT NULL CHECK (source IN ('EMAIL','FORM','PHONE','VISIT')),
  received_at   timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE sample_box ENABLE ROW LEVEL SECURITY;
ALTER TABLE sample_box_item ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_phase ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_result ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_feedback ENABLE ROW LEVEL SECURITY;

CREATE POLICY rls_sample_box_rw ON sample_box
  USING (EXISTS (SELECT 1 FROM leads l WHERE l.id = sample_box.lead_id))
  WITH CHECK (EXISTS (SELECT 1 FROM leads l WHERE l.id = sample_box.lead_id));
CREATE POLICY rls_sample_box_item_rw ON sample_box_item
  USING (EXISTS (SELECT 1 FROM sample_box b JOIN leads l ON l.id=b.lead_id WHERE b.box_id=sample_box_item.box_id))
  WITH CHECK (EXISTS (SELECT 1 FROM sample_box b JOIN leads l ON l.id=b.lead_id WHERE b.box_id=sample_box_item.box_id));
CREATE POLICY rls_test_phase_rw ON test_phase
  USING (EXISTS (SELECT 1 FROM leads l WHERE l.id = test_phase.lead_id))
  WITH CHECK (EXISTS (SELECT 1 FROM leads l WHERE l.id = test_phase.lead_id));
CREATE POLICY rls_test_result_rw ON test_result
  USING (EXISTS (SELECT 1 FROM test_phase p JOIN leads l ON l.id=p.lead_id WHERE p.id=test_result.phase_id))
  WITH CHECK (EXISTS (SELECT 1 FROM test_phase p JOIN leads l ON l.id=p.lead_id WHERE p.id=test_result.phase_id));
CREATE POLICY rls_product_feedback_rw ON product_feedback
  USING (EXISTS (SELECT 1 FROM test_phase p JOIN leads l ON l.id=p.lead_id WHERE p.id=product_feedback.phase_id))
  WITH CHECK (EXISTS (SELECT 1 FROM test_phase p JOIN leads l ON l.id=p.lead_id WHERE p.id=product_feedback.phase_id));
