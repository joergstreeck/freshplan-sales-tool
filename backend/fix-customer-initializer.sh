#!/bin/bash

# Fix CustomerDataInitializer by adding Sprint-2 fields with defaults
# This script adds the missing location fields to all customer creations

echo "Fixing CustomerDataInitializer for Sprint-2 fields..."

# Find all lines with setCreatedBy and add Sprint-2 fields before them
sed -i '' 's/customer.setCreatedBy("\(.*\)");/customer.setLocationsGermany(0);\
    customer.setLocationsAustria(0);\
    customer.setLocationsRestEu(0);\
    customer.setLocationsSwitzerland(0);\
    customer.setTotalLocationsEu(0);\
    customer.setPainPoints("[]");\
    customer.setPrimaryFinancing("EIGENKAPITAL");\
    customer.setCreatedBy("\1");/g' src/main/java/de/freshplan/api/CustomerDataInitializer.java

echo "✅ CustomerDataInitializer fixed with Sprint-2 default values"