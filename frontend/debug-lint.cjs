#!/usr/bin/env node

/**
 * Debug script for ESLint issues in CI
 * Provides detailed analysis of lint failures
 */

const { exec } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log('🔍 ESLint Debug Analysis');
console.log('========================');

// Check if we're in the right directory
if (!fs.existsSync('package.json')) {
    console.error('❌ Not in frontend directory');
    process.exit(1);
}

// Read package.json
const packageJson = JSON.parse(fs.readFileSync('package.json', 'utf8'));
console.log('📦 Project:', packageJson.name);
console.log('📦 Version:', packageJson.version);

// Check ESLint config
console.log('\n🔧 ESLint Configuration:');
if (fs.existsSync('eslint.config.js')) {
    console.log('✅ eslint.config.js exists');
    const config = fs.readFileSync('eslint.config.js', 'utf8');
    console.log('Config preview:', config.substring(0, 200) + '...');
} else {
    console.log('❌ No eslint.config.js found');
}

// Check node_modules
console.log('\n📁 Dependencies:');
const nodeModulesExists = fs.existsSync('node_modules');
console.log('node_modules exists:', nodeModulesExists ? '✅' : '❌');

if (nodeModulesExists) {
    const eslintExists = fs.existsSync('node_modules/.bin/eslint');
    console.log('ESLint binary exists:', eslintExists ? '✅' : '❌');
}

// Run ESLint with detailed output
console.log('\n🔍 Running ESLint with debug info...');

const eslintCommand = 'npx eslint . --ext ts,tsx --max-warnings 15 --format json';

exec(eslintCommand, (error, stdout, stderr) => {
    console.log('\n📊 ESLint Results:');
    
    if (stderr) {
        console.log('⚠️  Stderr:', stderr);
    }
    
    try {
        const results = JSON.parse(stdout);
        
        console.log('📁 Files processed:', results.length);
        
        let totalWarnings = 0;
        let totalErrors = 0;
        
        results.forEach(result => {
            if (result.messages.length > 0) {
                console.log(`\n📄 File: ${result.filePath}`);
                console.log(`   Warnings: ${result.warningCount}, Errors: ${result.errorCount}`);
                
                result.messages.forEach(msg => {
                    const type = msg.severity === 2 ? '❌ Error' : '⚠️  Warning';
                    console.log(`   ${type}: Line ${msg.line}:${msg.column} - ${msg.message}`);
                    console.log(`     Rule: ${msg.ruleId || 'unknown'}`);
                });
            }
            
            totalWarnings += result.warningCount;
            totalErrors += result.errorCount;
        });
        
        console.log('\n📈 Summary:');
        console.log(`Total warnings: ${totalWarnings}`);
        console.log(`Total errors: ${totalErrors}`);
        console.log(`Max warnings allowed: 15`);
        console.log(`Status: ${totalWarnings <= 15 && totalErrors === 0 ? '✅ PASS' : '❌ FAIL'}`);
        
        // Save detailed report
        const report = {
            timestamp: new Date().toISOString(),
            totalWarnings,
            totalErrors,
            maxWarnings: 15,
            status: totalWarnings <= 15 && totalErrors === 0 ? 'PASS' : 'FAIL',
            results: results.filter(r => r.messages.length > 0)
        };
        
        fs.writeFileSync('eslint-debug-report.json', JSON.stringify(report, null, 2));
        console.log('\n💾 Detailed report saved to: eslint-debug-report.json');
        
    } catch (parseError) {
        console.log('❌ Could not parse ESLint JSON output');
        console.log('Raw stdout:', stdout);
        console.log('Parse error:', parseError.message);
    }
    
    if (error) {
        console.log('\n❌ ESLint process error:', error.code);
        console.log('Error message:', error.message);
    }
});

// Also check TypeScript compilation
console.log('\n🔧 TypeScript Check:');
exec('npx tsc --noEmit', (error, stdout, stderr) => {
    if (error) {
        console.log('❌ TypeScript errors found:');
        console.log(stdout);
    } else {
        console.log('✅ TypeScript compilation OK');
    }
});