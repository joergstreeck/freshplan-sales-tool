-- V9001: Add CASCADE delete for CI tests only
-- Purpose: Simplify test cleanup by enabling cascade deletes
-- Note: This is ONLY for CI/test environments, NOT for production!

-- customer_timeline_events -> customers
ALTER TABLE customer_timeline_events
  DROP CONSTRAINT IF EXISTS customer_timeline_events_customer_id_fkey;
ALTER TABLE customer_timeline_events
  ADD CONSTRAINT customer_timeline_events_customer_id_fkey
  FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE;

-- customer_contacts -> customers  
ALTER TABLE customer_contacts
  DROP CONSTRAINT IF EXISTS fk1r6og90xwheb8ordk28mnl7c8;
ALTER TABLE customer_contacts
  ADD CONSTRAINT fk1r6og90xwheb8ordk28mnl7c8
  FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE;

-- opportunities -> customers
ALTER TABLE opportunities
  DROP CONSTRAINT IF EXISTS opportunities_customer_id_fkey;
ALTER TABLE opportunities
  ADD CONSTRAINT opportunities_customer_id_fkey
  FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE;