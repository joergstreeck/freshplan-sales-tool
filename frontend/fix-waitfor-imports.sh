#!/bin/bash

# Fix missing waitFor imports in test files

echo "🔧 Fixing missing waitFor imports in test files..."

# Find all test files with waitFor usage but missing import
for file in $(grep -r "waitFor" --include="*.test.tsx" --include="*.test.ts" src/ | cut -d: -f1 | sort -u); do
  # Check if waitFor is already imported
  if ! grep -q "import.*waitFor.*from.*@testing-library" "$file"; then
    echo "Fixing: $file"
    
    # Check if there's already an import from @testing-library/react
    if grep -q "from '@testing-library/react'" "$file"; then
      # Add waitFor to existing import
      sed -i '' "s/import { \(.*\) } from '@testing-library\/react'/import { \1, waitFor } from '@testing-library\/react'/" "$file"
      
      # Remove duplicate waitFor if it exists
      sed -i '' 's/, waitFor, waitFor/, waitFor/g' "$file"
    else
      # Add new import line after first import statement
      sed -i '' "1,/^import/s/^import/import { waitFor } from '@testing-library\/react';\nimport/" "$file"
    fi
  fi
done

# Fix missing act imports
for file in $(grep -r "act(" --include="*.test.tsx" --include="*.test.ts" src/ | cut -d: -f1 | sort -u); do
  # Check if act is already imported
  if ! grep -q "import.*act.*from" "$file"; then
    echo "Fixing act in: $file"
    
    # Check if there's already an import from react
    if grep -q "from 'react'" "$file"; then
      # Add act to existing React import
      sed -i '' "s/import React from 'react'/import React, { act } from 'react'/" "$file"
      sed -i '' "s/import { \(.*\) } from 'react'/import { \1, act } from 'react'/" "$file"
      
      # Remove duplicate act if it exists
      sed -i '' 's/, act, act/, act/g' "$file"
    else
      # Add new import line after first import statement
      sed -i '' "1,/^import/s/^import/import { act } from 'react';\nimport/" "$file"
    fi
  fi
done

# Fix missing fireEvent imports
for file in $(grep -r "fireEvent" --include="*.test.tsx" --include="*.test.ts" src/ | cut -d: -f1 | sort -u); do
  # Check if fireEvent is already imported
  if ! grep -q "import.*fireEvent.*from.*@testing-library" "$file"; then
    echo "Fixing fireEvent in: $file"
    
    # Check if there's already an import from @testing-library/react
    if grep -q "from '@testing-library/react'" "$file"; then
      # Add fireEvent to existing import
      sed -i '' "s/import { \(.*\) } from '@testing-library\/react'/import { \1, fireEvent } from '@testing-library\/react'/" "$file"
      
      # Remove duplicate fireEvent if it exists
      sed -i '' 's/, fireEvent, fireEvent/, fireEvent/g' "$file"
    else
      # Add new import line after first import statement  
      sed -i '' "1,/^import/s/^import/import { fireEvent } from '@testing-library\/react';\nimport/" "$file"
    fi
  fi
done

echo "✅ Import fixes completed!"