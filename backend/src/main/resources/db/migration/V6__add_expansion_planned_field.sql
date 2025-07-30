-- Migration V6: Add expansion_planned field to customer table
-- Sprint 2: Erweiterung der Filialstruktur um Expansionspläne

-- Add expansion_planned column as simple ja/nein string
ALTER TABLE customers 
ADD COLUMN expansion_planned VARCHAR(10);

-- Add comment for documentation
COMMENT ON COLUMN customers.expansion_planned IS 'Expansion planned: ja, nein';

-- Create index for sales queries (expansion = high relevance)
CREATE INDEX idx_customer_expansion_sales 
ON customers (expansion_planned) 
WHERE expansion_planned = 'ja';