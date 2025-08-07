-- Create profiles table
CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL UNIQUE,
    company_info TEXT,
    contact_info TEXT,
    financial_info TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Indexes for performance
CREATE INDEX idx_profiles_customer_id ON profiles(customer_id);
CREATE INDEX idx_profiles_created_at ON profiles(created_at);
CREATE INDEX idx_profiles_updated_at ON profiles(updated_at);

-- Comments for documentation
COMMENT ON TABLE profiles IS 'Stores customer profile information';
COMMENT ON COLUMN profiles.id IS 'Unique identifier for the profile';
COMMENT ON COLUMN profiles.customer_id IS 'Reference to the customer, must be unique';
COMMENT ON COLUMN profiles.company_info IS 'JSON data containing company information';
COMMENT ON COLUMN profiles.contact_info IS 'JSON data containing contact information';
COMMENT ON COLUMN profiles.financial_info IS 'JSON data containing financial information';
COMMENT ON COLUMN profiles.notes IS 'Free text notes about the customer';
COMMENT ON COLUMN profiles.created_at IS 'Timestamp when the profile was created';
COMMENT ON COLUMN profiles.updated_at IS 'Timestamp when the profile was last updated';
COMMENT ON COLUMN profiles.created_by IS 'User who created the profile';
COMMENT ON COLUMN profiles.updated_by IS 'User who last updated the profile';
COMMENT ON COLUMN profiles.version IS 'Version number for optimistic locking';