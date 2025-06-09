-- Create user_roles table for @ElementCollection mapping
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role)
);

-- Add index for better performance
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);

-- Add default roles for existing users based on username
INSERT INTO user_roles (user_id, role)
SELECT id, 
    CASE 
        WHEN username = 'admin' THEN 'admin'
        WHEN username = 'manager' THEN 'manager'
        WHEN username = 'sales' THEN 'sales'
        ELSE 'viewer'  -- Default role for other users
    END
FROM app_user;

-- Add comment
COMMENT ON TABLE user_roles IS 'Roles assigned to users (admin, manager, sales, viewer)';