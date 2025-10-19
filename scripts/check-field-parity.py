#!/usr/bin/env python3
"""
Backend/Frontend Field Parity Guard
====================================
Purpose: Ensure ALL fields in fieldCatalog.json exist in Backend entities
Usage: ./scripts/check-field-parity.py
Exit: 0 = OK, 1 = Ghost fields found
"""

import json
import re
import sys
from pathlib import Path

# Setup paths
SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent
CUSTOMER_ENTITY = PROJECT_ROOT / "backend/src/main/java/de/freshplan/domain/customer/entity/Customer.java"
LOCATION_ENTITY = PROJECT_ROOT / "backend/src/main/java/de/freshplan/domain/customer/entity/CustomerLocation.java"
FIELD_CATALOG = PROJECT_ROOT / "frontend/src/features/customers/data/fieldCatalog.json"

def extract_entity_fields(entity_path):
    """Extract field names from a Java entity"""
    if not entity_path.exists():
        return set()
    
    content = entity_path.read_text()
    
    # Multiple patterns to catch different field declaration styles
    patterns = [
        r'^\s*private\s+\w+(?:<[^>]+>)?\s+(\w+)\s*;',           # private Type field;
        r'^\s*private\s+\w+(?:<[^>]+>)?\s+(\w+)\s*=',           # private Type field = value;
        r'^\s*@Column.*?\n\s*private\s+\w+(?:<[^>]+>)?\s+(\w+)', # @Column\n private Type field
    ]
    
    fields = set()
    lines = content.split('\n')
    
    for i, line in enumerate(lines):
        for pattern in patterns:
            # For multi-line patterns, combine current + next line
            combined_line = line
            if i + 1 < len(lines):
                combined_line = line + '\n' + lines[i + 1]
            
            match = re.search(pattern, combined_line)
            if match:
                field_name = match.group(1)
                # Skip common non-field matches
                if field_name not in {'serialVersionUID', 'class', 'this', 'new'}:
                    fields.add(field_name)
    
    return fields

def extract_frontend_fields():
    """Extract field keys from fieldCatalog.json with entity type"""
    if not FIELD_CATALOG.exists():
        print(f"❌ ERROR: fieldCatalog.json not found at {FIELD_CATALOG}")
        sys.exit(1)
    
    with open(FIELD_CATALOG, 'r') as f:
        catalog = json.load(f)
    
    customer_fields = set()
    location_fields = set()
    
    # Extract from customer.base
    if 'customer' in catalog and 'base' in catalog['customer']:
        for field in catalog['customer']['base']:
            if 'key' in field:
                entity_type = field.get('entityType', 'customer')
                if entity_type == 'customer':
                    customer_fields.add(field['key'])
                elif entity_type == 'location':
                    location_fields.add(field['key'])
    
    # Extract from customer.industrySpecific
    if 'customer' in catalog and 'industrySpecific' in catalog['customer']:
        for industry_fields in catalog['customer']['industrySpecific'].values():
            for field in industry_fields:
                if 'key' in field:
                    entity_type = field.get('entityType', 'customer')
                    if entity_type == 'customer':
                        customer_fields.add(field['key'])
                    elif entity_type == 'location':
                        location_fields.add(field['key'])
    
    return customer_fields, location_fields

def main():
    print("🔍 Backend/Frontend Field Parity Check")
    print("━" * 50)
    
    # Extract backend fields
    print("📋 Extracting backend fields...")
    customer_backend = extract_entity_fields(CUSTOMER_ENTITY)
    location_backend = extract_entity_fields(LOCATION_ENTITY)
    print(f"✅ Customer.java: {len(customer_backend)} fields")
    print(f"✅ CustomerLocation.java: {len(location_backend)} fields")
    
    # Extract frontend fields
    print("📋 Extracting frontend fields from fieldCatalog.json...")
    customer_frontend, location_frontend = extract_frontend_fields()
    print(f"✅ Customer fields: {len(customer_frontend)} fields")
    print(f"✅ Location fields: {len(location_frontend)} fields")
    
    # Find ghost fields
    print()
    print("🔍 Checking for ghost fields (frontend-only)...")
    
    customer_ghosts = sorted(customer_frontend - customer_backend)
    location_ghosts = sorted(location_frontend - location_backend)
    
    total_ghosts = len(customer_ghosts) + len(location_ghosts)
    
    # Report results
    print()
    if total_ghosts == 0:
        print("✅ SUCCESS: All frontend fields exist in backend!")
        print(f"   Customer: {len(customer_frontend)} fields ✓")
        print(f"   Location: {len(location_frontend)} fields ✓")
        print(f"   Ghost: 0 fields")
        sys.exit(0)
    else:
        print(f"❌ FAILURE: Found {total_ghosts} ghost fields (frontend-only):")
        print()
        
        if customer_ghosts:
            print(f"  Customer entity ({len(customer_ghosts)} ghost fields):")
            for field in customer_ghosts:
                print(f"    - {field}")
            print()
        
        if location_ghosts:
            print(f"  Location entity ({len(location_ghosts)} ghost fields):")
            for field in location_ghosts:
                print(f"    - {field}")
            print()
        
        print("🚫 RULE VIOLATION: Backend/Frontend Parity (ZERO TOLERANCE)")
        print()
        print("📖 Fix by one of:")
        print("   1. Remove ghost fields from fieldCatalog.json")
        print("   2. Add missing fields to backend entities (+ migration)")
        print("   3. Fix 'entityType' in fieldCatalog.json (customer vs location)")
        print()
        print("   See CLAUDE.md → BACKEND/FRONTEND PARITY for details")
        sys.exit(1)

if __name__ == '__main__':
    main()
