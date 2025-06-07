#!/usr/bin/env node

// Backend Integration Test Script
// Run with: node test-backend-integration.js

const API_URL = 'http://localhost:8080';

async function testBackendConnection() {
  console.log('🧪 Testing Backend Connection...\n');

  // Test 1: Ping endpoint
  console.log('1️⃣ Testing /api/ping');
  try {
    const response = await fetch(`${API_URL}/api/ping`);
    if (response.ok) {
      const data = await response.json();
      console.log('✅ Backend is running!');
      console.log('Response:', JSON.stringify(data, null, 2));
    } else {
      console.log('❌ Backend returned error:', response.status, response.statusText);
      return false;
    }
  } catch (error) {
    console.log('❌ Backend is not reachable on port 8080');
    console.log('Error:', error.message);
    return false;
  }
  
  return true;
}

async function testCalculatorAPI() {
  console.log('\n\n🧪 Testing Calculator API...\n');

  // Test input
  const testInput = {
    orderValue: 25000,
    leadTime: 14,
    pickup: false,
    chain: false,
  };

  // Test 1: Calculate Discount
  console.log('1️⃣ Testing POST /api/calculator/calculate');
  console.log('Input:', JSON.stringify(testInput, null, 2));
  try {
    const response = await fetch(`${API_URL}/api/calculator/calculate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(testInput),
    });

    if (response.ok) {
      const result = await response.json();
      console.log('✅ Calculate endpoint works!');
      console.log('Response:', JSON.stringify(result, null, 2));
      
      // Validate response structure
      if (result.totalDiscount && result.finalPrice && result.discounts) {
        console.log('\n📊 Calculation Summary:');
        console.log(`  Order Value: ${testInput.orderValue}€`);
        console.log(`  Final Price: ${result.finalPrice}€`);
        console.log(`  Total Discount: ${result.totalDiscount}%`);
        console.log(`  Savings: ${testInput.orderValue - result.finalPrice}€`);
      }
    } else {
      console.log('❌ Calculate endpoint failed:', response.status, response.statusText);
      const errorText = await response.text();
      if (errorText) console.log('Error response:', errorText);
    }
  } catch (error) {
    console.log('❌ Calculate endpoint error:', error.message);
  }

  console.log('\n---\n');

  // Test 2: Get Scenarios
  console.log('2️⃣ Testing GET /api/calculator/scenarios');
  try {
    const response = await fetch(`${API_URL}/api/calculator/scenarios`);
    
    if (response.ok) {
      const scenarios = await response.json();
      console.log('✅ Scenarios endpoint works!');
      console.log(`Found ${scenarios.length} scenarios:`);
      scenarios.forEach((s, i) => {
        console.log(`  ${i + 1}. ${s.name} - ${s.description}`);
      });
    } else {
      console.log('❌ Scenarios endpoint failed:', response.status, response.statusText);
    }
  } catch (error) {
    console.log('❌ Scenarios endpoint error:', error.message);
  }

  console.log('\n---\n');

  // Test 3: Get Specific Scenario
  console.log('3️⃣ Testing GET /api/calculator/scenarios/optimal');
  try {
    const response = await fetch(`${API_URL}/api/calculator/scenarios/optimal`);
    
    if (response.ok) {
      const scenario = await response.json();
      console.log('✅ Scenario detail endpoint works!');
      console.log('Optimal scenario:', JSON.stringify(scenario, null, 2));
    } else {
      console.log('❌ Scenario detail endpoint failed:', response.status, response.statusText);
    }
  } catch (error) {
    console.log('❌ Scenario detail endpoint error:', error.message);
  }

  console.log('\n---\n');

  // Test 4: Get Rules
  console.log('4️⃣ Testing GET /api/calculator/rules');
  try {
    const response = await fetch(`${API_URL}/api/calculator/rules`);
    
    if (response.ok) {
      const rules = await response.json();
      console.log('✅ Rules endpoint works!');
      console.log('Discount Rules:', JSON.stringify(rules, null, 2));
    } else {
      console.log('❌ Rules endpoint failed:', response.status, response.statusText);
    }
  } catch (error) {
    console.log('❌ Rules endpoint error:', error.message);
  }
}

// Main execution
async function main() {
  console.log('🚀 FreshPlan Backend Integration Test\n');
  console.log('Target URL:', API_URL);
  console.log('Time:', new Date().toISOString());
  console.log('=====================================\n');

  const isBackendRunning = await testBackendConnection();
  
  if (isBackendRunning) {
    await testCalculatorAPI();
    console.log('\n✅ Integration test complete!');
  } else {
    console.log('\n❌ Backend is not running!');
    console.log('\n💡 Start the backend with:');
    console.log('   cd /Users/joergstreeck/freshplan-sales-tool/backend');
    console.log('   ./mvnw quarkus:dev\n');
    console.log('Or use Docker:');
    console.log('   cd /Users/joergstreeck/freshplan-sales-tool/infrastructure');
    console.log('   ./start-local-env.sh\n');
  }
}

// Run the test
main().catch(console.error);