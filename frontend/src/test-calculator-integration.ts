// Test script for Calculator API integration

async function testCalculatorAPI() {
  const API_URL = 'http://localhost:8080';

  console.log('🧪 Testing Calculator API Integration...\n');

  // Test 1: Calculate Discount
  console.log('1️⃣ Testing POST /api/calculator/calculate');
  try {
    const response = await fetch(`${API_URL}/api/calculator/calculate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        orderValue: 25000,
        leadTime: 14,
        pickup: false,
        chain: false,
      }),
    });

    if (response.ok) {
      const result = await response.json();
      console.log('✅ Calculate endpoint works!');
      console.log('Response:', JSON.stringify(result, null, 2));
    } else {
      console.log('❌ Calculate endpoint failed:', response.status, response.statusText);
    }
  } catch (error) {
    console.log('❌ Calculate endpoint error:', error);
  }

  console.log('\n---\n');

  // Test 2: Get Scenarios
  console.log('2️⃣ Testing GET /api/calculator/scenarios');
  try {
    const response = await fetch(`${API_URL}/api/calculator/scenarios`);

    if (response.ok) {
      const scenarios = await response.json();
      console.log('✅ Scenarios endpoint works!');
      console.log(`Found ${scenarios.length} scenarios:`, scenarios.map(s => s.name).join(', '));
    } else {
      console.log('❌ Scenarios endpoint failed:', response.status, response.statusText);
    }
  } catch (error) {
    console.log('❌ Scenarios endpoint error:', error);
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
    console.log('❌ Scenario detail endpoint error:', error);
  }

  console.log('\n---\n');

  // Test 4: Get Rules
  console.log('4️⃣ Testing GET /api/calculator/rules');
  try {
    const response = await fetch(`${API_URL}/api/calculator/rules`);

    if (response.ok) {
      const rules = await response.json();
      console.log('✅ Rules endpoint works!');
      console.log('Rules:', JSON.stringify(rules, null, 2));
    } else {
      console.log('❌ Rules endpoint failed:', response.status, response.statusText);
    }
  } catch (error) {
    console.log('❌ Rules endpoint error:', error);
  }

  console.log('\n🏁 Integration test complete!');
}

// Run the test
testCalculatorAPI();
