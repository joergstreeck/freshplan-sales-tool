#!/bin/bash

# Fix CustomerDataInitializer by adding Sprint-2 fields with defaults
# This script adds the missing location fields to all customer creations
# Works cross-platform (macOS and Linux)

echo "Fixing CustomerDataInitializer for Sprint-2 fields..."

# Use perl for better cross-platform compatibility
# (Alternative to sed which has different syntax on BSD vs GNU)
perl -pi -e 's/customer\.setCreatedBy\("(.*?)"\);/customer.setLocationsGermany(0);
    customer.setLocationsAustria(0);
    customer.setLocationsRestEu(0);
    customer.setLocationsSwitzerland(0);
    customer.setTotalLocationsEu(0);
    customer.setPainPoints("[]");
    customer.setPrimaryFinancing("EIGENKAPITAL");
    customer.setCreatedBy("$1");/g' src/main/java/de/freshplan/api/CustomerDataInitializer.java

echo "✅ CustomerDataInitializer fixed with Sprint-2 default values"