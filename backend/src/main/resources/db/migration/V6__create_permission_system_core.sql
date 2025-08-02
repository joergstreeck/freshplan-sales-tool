-- V6: Core Permission System Tables
-- Creates minimal permission system without complex features
-- Based on FC-009 CLAUDE_TECH specification

-- Core permission definitions
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    permission_code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_permissions_code ON permissions(permission_code);
CREATE INDEX idx_permissions_resource_action ON permissions(resource, action);

-- Roles table (extending existing user_roles concept)
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Role to permission mapping
CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    granted BOOLEAN DEFAULT true,
    granted_by UUID REFERENCES app_user(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Direct user permissions (overrides role permissions)
CREATE TABLE user_permissions (
    user_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    granted BOOLEAN NOT NULL,
    granted_by UUID REFERENCES app_user(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    reason TEXT,
    PRIMARY KEY (user_id, permission_id),
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Indexes for user permissions
CREATE INDEX idx_user_permissions_user_id ON user_permissions(user_id);
CREATE INDEX idx_user_permissions_expires_at ON user_permissions(expires_at) WHERE expires_at IS NOT NULL;

-- Insert basic permissions
INSERT INTO permissions (permission_code, name, description, resource, action) VALUES
-- Customer permissions
('customers:read', 'Kunden anzeigen', 'Kann Kundenliste und Details anzeigen', 'customers', 'read'),
('customers:write', 'Kunden bearbeiten', 'Kann Kunden erstellen und bearbeiten', 'customers', 'write'),
('customers:delete', 'Kunden löschen', 'Kann Kunden löschen', 'customers', 'delete'),
-- Admin permissions
('admin:permissions', 'Berechtigungen verwalten', 'Kann Benutzerberechtigungen verwalten', 'admin', 'permissions'),
('admin:users', 'Benutzer verwalten', 'Kann Benutzer anlegen und verwalten', 'admin', 'users'),
-- System permissions
('*:*', 'Vollzugriff', 'Vollzugriff auf alle Funktionen', '*', '*');

-- Insert basic roles
INSERT INTO roles (name, description) VALUES
('admin', 'Administrator mit Vollzugriff'),
('manager', 'Manager mit erweiterten Rechten'),
('sales', 'Vertriebsmitarbeiter mit Basis-Rechten');

-- Assign permissions to roles
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT 
    r.id,
    p.id,
    NULL -- System-granted permissions have no granted_by user
FROM roles r, permissions p
WHERE 
    -- Admin gets all permissions
    (r.name = 'admin') OR
    -- Manager gets customer read/write
    (r.name = 'manager' AND p.permission_code IN ('customers:read', 'customers:write')) OR
    -- Sales gets customer read only
    (r.name = 'sales' AND p.permission_code = 'customers:read');

-- Add comments
COMMENT ON TABLE permissions IS 'System permissions in format resource:action';
COMMENT ON TABLE roles IS 'User roles with associated permissions';
COMMENT ON TABLE role_permissions IS 'Mapping between roles and permissions';
COMMENT ON TABLE user_permissions IS 'Direct user permission overrides';
