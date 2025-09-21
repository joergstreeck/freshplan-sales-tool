import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

/**
 * k6 Performance Test Suite for Module 02 - Lead Management APIs
 *
 * Tests ABAC-secured Lead APIs against Foundation Standards performance requirements:
 * - P95 response time < 200ms
 * - Error rate < 0.5%
 * - Throughput > 100 RPS
 *
 * @see ../grundlagen/PERFORMANCE_STANDARDS.md
 * @see ../grundlagen/API_STANDARDS.md
 */

// Custom metrics
const leadCreationDuration = new Trend('lead_creation_duration', true);
const leadSearchDuration = new Trend('lead_search_duration', true);
const leadUpdateDuration = new Trend('lead_update_duration', true);
const errorRate = new Rate('errors');

// Test configuration
export const options = {
    stages: [
        { duration: '30s', target: 10 },   // Warm-up
        { duration: '1m', target: 50 },    // Ramp-up
        { duration: '3m', target: 100 },   // Stay at peak
        { duration: '1m', target: 50 },    // Ramp-down
        { duration: '30s', target: 0 },    // Cool-down
    ],
    thresholds: {
        // Foundation Standards P95 < 200ms requirement
        'http_req_duration{scenario:lead_creation}': ['p(95)<200'],
        'http_req_duration{scenario:lead_search}': ['p(95)<200'],
        'http_req_duration{scenario:lead_update}': ['p(95)<200'],

        // Overall API performance
        'http_req_duration': ['p(95)<200', 'p(99)<500'],

        // Error rate < 0.5%
        'errors': ['rate<0.005'],

        // Specific endpoint thresholds
        'lead_creation_duration': ['p(95)<150'],
        'lead_search_duration': ['p(95)<100'],
        'lead_update_duration': ['p(95)<150'],

        // Availability
        'http_req_failed': ['rate<0.01'],
    },
};

// Test environment configuration
const BASE_URL = __ENV.API_URL || 'http://localhost:8080';
const AUTH_TOKEN = __ENV.AUTH_TOKEN || generateTestToken();

// Test data generators
function generateTestToken() {
    // In real environment, this would be fetched from Keycloak
    return 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...test';
}

function generateLeadData() {
    const timestamp = Date.now();
    const random = Math.floor(Math.random() * 10000);

    return {
        companyName: `Test Restaurant ${timestamp}-${random}`,
        contactName: `Test Contact ${random}`,
        email: `test-${timestamp}-${random}@example.com`,
        phone: `+49 ${Math.floor(Math.random() * 900000000 + 100000000)}`,
        businessType: ['RESTAURANT', 'HOTEL', 'CAFE', 'CATERING'][Math.floor(Math.random() * 4)],
        territory: ['DE_NORTH', 'DE_SOUTH', 'DE_EAST', 'DE_WEST'][Math.floor(Math.random() * 4)],
        monthlyVolume: Math.floor(Math.random() * 50000 + 5000),
        source: 'WEBSITE',
        status: 'NEW',
        notes: `Performance test lead created at ${new Date().toISOString()}`,
        seasonalWindows: ['SUMMER', 'CHRISTMAS'],
        preferredProducts: ['FRESH_VEGETABLES', 'PREMIUM_MEAT'],
        leadScore: Math.floor(Math.random() * 100),
    };
}

// Common headers with ABAC security
function getHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': AUTH_TOKEN,
        'X-Territories': 'DE_NORTH,DE_SOUTH', // For dev/test
        'X-Chain-Id': 'EDEKA',
        'Accept': 'application/json',
    };
}

// Main test scenario
export default function () {
    const headers = getHeaders();
    let createdLeadId = null;

    group('Lead Management API Tests', () => {

        // Test 1: Create Lead
        group('Create Lead', () => {
            const leadData = generateLeadData();
            const createStart = Date.now();

            const createResponse = http.post(
                `${BASE_URL}/api/v1/leads`,
                JSON.stringify(leadData),
                {
                    headers,
                    tags: { scenario: 'lead_creation' }
                }
            );

            const createDuration = Date.now() - createStart;
            leadCreationDuration.add(createDuration);

            const createSuccess = check(createResponse, {
                'Lead created successfully': (r) => r.status === 201,
                'Response has lead ID': (r) => {
                    if (r.status === 201) {
                        const body = JSON.parse(r.body);
                        createdLeadId = body.id;
                        return body.id !== undefined;
                    }
                    return false;
                },
                'Response time < 200ms': (r) => createDuration < 200,
                'Has location header': (r) => r.headers['Location'] !== undefined,
                'Content-Type is JSON': (r) => r.headers['Content-Type']?.includes('application/json'),
            });

            errorRate.add(!createSuccess);
        });

        sleep(0.5); // Small delay between operations

        // Test 2: Search Leads
        group('Search Leads', () => {
            const searchQueries = [
                '',                          // All leads
                'Restaurant',                // By name
                'status=NEW',               // By status
                'territory=DE_NORTH',       // By territory
                'limit=20&cursor=',         // Pagination
            ];

            const query = searchQueries[Math.floor(Math.random() * searchQueries.length)];
            const searchStart = Date.now();

            const searchResponse = http.get(
                `${BASE_URL}/api/v1/leads?${query}`,
                {
                    headers,
                    tags: { scenario: 'lead_search' }
                }
            );

            const searchDuration = Date.now() - searchStart;
            leadSearchDuration.add(searchDuration);

            const searchSuccess = check(searchResponse, {
                'Search successful': (r) => r.status === 200,
                'Response is array': (r) => {
                    if (r.status === 200) {
                        const body = JSON.parse(r.body);
                        return Array.isArray(body.items || body);
                    }
                    return false;
                },
                'Response time < 100ms': (r) => searchDuration < 100,
                'Has pagination info': (r) => {
                    if (r.status === 200) {
                        const body = JSON.parse(r.body);
                        return body.hasNext !== undefined || body.cursor !== undefined;
                    }
                    return true; // Optional
                },
            });

            errorRate.add(!searchSuccess);
        });

        sleep(0.5);

        // Test 3: Get Lead by ID
        if (createdLeadId) {
            group('Get Lead by ID', () => {
                const getResponse = http.get(
                    `${BASE_URL}/api/v1/leads/${createdLeadId}`,
                    {
                        headers,
                        tags: { scenario: 'lead_get' }
                    }
                );

                const getSuccess = check(getResponse, {
                    'Get lead successful': (r) => r.status === 200,
                    'Response has correct ID': (r) => {
                        if (r.status === 200) {
                            const body = JSON.parse(r.body);
                            return body.id === createdLeadId;
                        }
                        return false;
                    },
                });

                errorRate.add(!getSuccess);
            });
        }

        sleep(0.5);

        // Test 4: Update Lead
        if (createdLeadId) {
            group('Update Lead', () => {
                const updateData = {
                    status: 'QUALIFIED',
                    leadScore: 85,
                    notes: 'Updated during performance test',
                };

                const updateStart = Date.now();

                const updateResponse = http.patch(
                    `${BASE_URL}/api/v1/leads/${createdLeadId}`,
                    JSON.stringify(updateData),
                    {
                        headers,
                        tags: { scenario: 'lead_update' }
                    }
                );

                const updateDuration = Date.now() - updateStart;
                leadUpdateDuration.add(updateDuration);

                const updateSuccess = check(updateResponse, {
                    'Update successful': (r) => r.status === 200,
                    'Status updated': (r) => {
                        if (r.status === 200) {
                            const body = JSON.parse(r.body);
                            return body.status === 'QUALIFIED';
                        }
                        return false;
                    },
                    'Response time < 150ms': (r) => updateDuration < 150,
                });

                errorRate.add(!updateSuccess);
            });
        }

        sleep(0.5);

        // Test 5: Bulk Operations
        group('Bulk Lead Operations', () => {
            const bulkData = Array.from({ length: 10 }, () => generateLeadData());

            const bulkResponse = http.post(
                `${BASE_URL}/api/v1/leads/bulk`,
                JSON.stringify({ leads: bulkData }),
                {
                    headers,
                    tags: { scenario: 'lead_bulk' }
                }
            );

            const bulkSuccess = check(bulkResponse, {
                'Bulk create successful': (r) => r.status === 201 || r.status === 207,
                'All leads processed': (r) => {
                    if (r.status === 201 || r.status === 207) {
                        const body = JSON.parse(r.body);
                        return body.processed === 10;
                    }
                    return false;
                },
            });

            errorRate.add(!bulkSuccess);
        });

        // Test 6: Territory-based Access Control
        group('ABAC Territory Validation', () => {
            // Try to access lead from unauthorized territory
            const unauthorizedHeaders = {
                ...headers,
                'X-Territories': 'DE_WEST', // Different territory
            };

            const abacResponse = http.get(
                `${BASE_URL}/api/v1/leads?territory=DE_NORTH`,
                { headers: unauthorizedHeaders }
            );

            check(abacResponse, {
                'ABAC restricts access correctly': (r) => {
                    if (r.status === 200) {
                        const body = JSON.parse(r.body);
                        // Should return empty or filtered results
                        return body.items?.length === 0 || body.length === 0;
                    }
                    return r.status === 403; // Or forbidden
                },
            });
        });

        // Test 7: Campaign Assignment
        if (createdLeadId) {
            group('Campaign Assignment', () => {
                const campaignData = {
                    leadIds: [createdLeadId],
                    campaignId: 'SUMMER_2025',
                };

                const campaignResponse = http.post(
                    `${BASE_URL}/api/v1/campaigns/assign`,
                    JSON.stringify(campaignData),
                    { headers }
                );

                check(campaignResponse, {
                    'Campaign assignment successful': (r) =>
                        r.status === 200 || r.status === 202,
                });
            });
        }

        // Test 8: Email Integration
        group('Email Lead Creation', () => {
            const emailData = {
                from: 'inquiry@restaurant.com',
                subject: 'Interesse an Cook&Fresh Produkten',
                body: 'Wir möchten mehr über Ihre Produkte erfahren...',
                receivedAt: new Date().toISOString(),
            };

            const emailResponse = http.post(
                `${BASE_URL}/api/v1/leads/from-email`,
                JSON.stringify(emailData),
                { headers }
            );

            check(emailResponse, {
                'Email lead processed': (r) =>
                    r.status === 201 || r.status === 202,
            });
        });
    });

    // Random sleep between iterations (0.5 - 2 seconds)
    sleep(Math.random() * 1.5 + 0.5);
}

// Teardown function
export function teardown(data) {
    console.log('Performance Test Summary:');
    console.log('==========================');
    console.log(`Total Requests: ${__ENV.total_requests || 'N/A'}`);
    console.log(`Error Rate: ${errorRate.rate * 100}%`);
    console.log('==========================');
}