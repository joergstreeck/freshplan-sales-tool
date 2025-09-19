import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';
import { randomItem, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

/**
 * k6 Performance Test - Communication Module with Real-World Data
 * Simulates B2B Food Sales Communication Patterns
 * Target: 10,000+ active threads, 100,000+ messages
 */

// Custom metrics
const errorRate = new Rate('errors');
const threadListDuration = new Trend('thread_list_duration');
const threadDetailDuration = new Trend('thread_detail_duration');
const messageSendDuration = new Trend('message_send_duration');
const searchDuration = new Trend('search_duration');
const slaProcessingDuration = new Trend('sla_processing_duration');
const concurrentUpdateErrors = new Rate('concurrent_update_errors');

// Real-world test data
const territories = new SharedArray('territories', function () {
  // German sales territories
  return [
    'BER', 'MUC', 'HAM', 'FRA', 'CGN', 'STG', 'DUS', 'LEJ', 'DRS', 'HAN',
    'NUE', 'DUI', 'BRE', 'ESS', 'DOC', 'KIE', 'MAG', 'FRE', 'BRA', 'POT'
  ];
});

const customerTypes = new SharedArray('customerTypes', function () {
  return [
    { type: 'restaurant', avgThreads: 15, avgMessages: 50 },
    { type: 'hotel', avgThreads: 25, avgMessages: 100 },
    { type: 'betriebsgastronomie', avgThreads: 10, avgMessages: 30 },
    { type: 'vending', avgThreads: 5, avgMessages: 15 },
    { type: 'chain', avgThreads: 50, avgMessages: 200 }
  ];
});

const messageTemplates = new SharedArray('messageTemplates', function () {
  return [
    // Sample follow-ups (most common)
    { subject: 'Sample Follow-up T+3', weight: 30 },
    { subject: 'Sample Follow-up T+7', weight: 25 },
    { subject: 'Cook&Fresh Produkttest - Wie war Ihre Erfahrung?', weight: 20 },
    // Regular communication
    { subject: 'Neue Produkte im Cook&Fresh Sortiment', weight: 10 },
    { subject: 'Sonderaktion für Ihre Region', weight: 5 },
    { subject: 'Terminvereinbarung für Produktvorstellung', weight: 5 },
    { subject: 'Bestellbestätigung Cook&Fresh', weight: 5 }
  ];
});

// Load test scenarios matching real usage patterns
export const options = {
  scenarios: {
    // Morning peak (8-10 AM) - Sales reps checking overnight emails
    morning_peak: {
      executor: 'ramping-vus',
      startVUs: 50,
      stages: [
        { duration: '2m', target: 200 },  // Ramp up
        { duration: '5m', target: 200 },  // Stay at peak
        { duration: '2m', target: 50 },   // Ramp down
      ],
      startTime: '0s',
      tags: { scenario: 'morning_peak' }
    },

    // Business hours steady load
    business_hours: {
      executor: 'constant-vus',
      vus: 100,
      duration: '15m',
      startTime: '10m',
      tags: { scenario: 'business_hours' }
    },

    // Sample follow-up burst (T+3/T+7 automated sends)
    followup_burst: {
      executor: 'shared-iterations',
      vus: 50,
      iterations: 1000,
      maxDuration: '5m',
      startTime: '25m',
      tags: { scenario: 'followup_burst' }
    },

    // Concurrent updates (multiple sales reps updating same thread)
    concurrent_updates: {
      executor: 'per-vu-iterations',
      vus: 20,
      iterations: 10,
      maxDuration: '3m',
      startTime: '30m',
      tags: { scenario: 'concurrent_updates' }
    },

    // Search and analytics queries
    search_analytics: {
      executor: 'constant-arrival-rate',
      rate: 10,
      timeUnit: '1s',
      duration: '5m',
      preAllocatedVUs: 20,
      startTime: '33m',
      tags: { scenario: 'search_analytics' }
    }
  },

  thresholds: {
    // API Performance SLAs
    'http_req_duration{url:~.*threads.*}': ['p(95)<200'],  // Thread list < 200ms
    'http_req_duration{url:~.*messages.*}': ['p(95)<150'], // Messages < 150ms
    'http_req_duration{url:~.*search.*}': ['p(95)<500'],   // Search < 500ms
    'http_req_duration{url:~.*reply.*}': ['p(95)<300'],    // Reply < 300ms

    // Business metrics
    'thread_list_duration': ['p(95)<200'],
    'thread_detail_duration': ['p(95)<250'],
    'message_send_duration': ['p(95)<300'],
    'search_duration': ['p(95)<500'],
    'sla_processing_duration': ['p(95)<100'],

    // Error rates
    'errors': ['rate<0.01'],  // Less than 1% error rate
    'concurrent_update_errors': ['rate<0.05'], // Less than 5% concurrent update conflicts
    'http_req_failed': ['rate<0.01'],
  },
};

// Setup: Create test data
export function setup() {
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

  // Authenticate and get tokens for different user types
  const tokens = {
    salesRep: authenticateUser(baseUrl, 'sales@freshfoodz.de', 'password'),
    manager: authenticateUser(baseUrl, 'manager@freshfoodz.de', 'password'),
    readonly: authenticateUser(baseUrl, 'readonly@freshfoodz.de', 'password'),
  };

  // Generate realistic customer IDs (UUIDs)
  const customerIds = generateCustomerIds(1000);

  // Generate thread IDs for concurrent update tests
  const sharedThreadIds = generateThreadIds(baseUrl, tokens.salesRep, 20);

  return {
    baseUrl,
    tokens,
    customerIds,
    sharedThreadIds
  };
}

// Main test scenarios
export default function (data) {
  const scenario = __ENV.SCENARIO || exec.test.options.scenarios[exec.scenario.name];

  switch (exec.scenario.name) {
    case 'morning_peak':
      morningPeakScenario(data);
      break;
    case 'business_hours':
      businessHoursScenario(data);
      break;
    case 'followup_burst':
      followUpBurstScenario(data);
      break;
    case 'concurrent_updates':
      concurrentUpdateScenario(data);
      break;
    case 'search_analytics':
      searchAnalyticsScenario(data);
      break;
    default:
      businessHoursScenario(data);
  }
}

// Scenario: Morning Peak - Sales reps checking threads
function morningPeakScenario(data) {
  const territory = randomItem(territories);
  const headers = {
    'Authorization': `Bearer ${data.tokens.salesRep}`,
    'X-Territories': territory,
    'Content-Type': 'application/json',
  };

  group('Morning Dashboard Load', () => {
    // 1. Load thread list with realistic pagination
    const threadsStart = Date.now();
    const threadsRes = http.get(
      `${data.baseUrl}/api/comm/threads?territory=${territory}&limit=50&status=OPEN,IN_PROGRESS`,
      { headers, tags: { name: 'GetThreadList' } }
    );
    threadListDuration.add(Date.now() - threadsStart);

    check(threadsRes, {
      'thread list loaded': (r) => r.status === 200,
      'thread list < 200ms': (r) => r.timings.duration < 200,
      'threads returned': (r) => JSON.parse(r.body).items.length > 0,
    }) || errorRate.add(1);

    if (threadsRes.status === 200) {
      const threads = JSON.parse(threadsRes.body).items;

      // 2. Load details for unread threads (typical morning behavior)
      const unreadThreads = threads.filter(t => t.unreadCount > 0).slice(0, 5);

      unreadThreads.forEach(thread => {
        sleep(randomIntBetween(1, 3)); // Simulate reading time

        const detailStart = Date.now();
        const detailRes = http.get(
          `${data.baseUrl}/api/comm/threads/${thread.id}`,
          { headers, tags: { name: 'GetThreadDetail' } }
        );
        threadDetailDuration.add(Date.now() - detailStart);

        check(detailRes, {
          'thread detail loaded': (r) => r.status === 200,
          'thread detail < 250ms': (r) => r.timings.duration < 250,
        });

        // 3. Load messages for the thread
        const messagesRes = http.get(
          `${data.baseUrl}/api/comm/threads/${thread.id}/messages?limit=20`,
          { headers, tags: { name: 'GetMessages' } }
        );

        check(messagesRes, {
          'messages loaded': (r) => r.status === 200,
          'messages < 150ms': (r) => r.timings.duration < 150,
        });

        // 4. Mark as read
        if (thread.unreadCount > 0) {
          http.post(
            `${data.baseUrl}/api/comm/threads/${thread.id}/mark-read`,
            null,
            { headers, tags: { name: 'MarkAsRead' } }
          );
        }
      });
    }
  });

  sleep(randomIntBetween(5, 10));
}

// Scenario: Business Hours - Regular communication
function businessHoursScenario(data) {
  const territory = randomItem(territories);
  const customerId = randomItem(data.customerIds);
  const headers = {
    'Authorization': `Bearer ${data.tokens.salesRep}`,
    'X-Territories': territory,
    'Content-Type': 'application/json',
  };

  group('Regular Business Communication', () => {
    // Random action distribution based on real usage
    const action = Math.random();

    if (action < 0.4) {
      // 40% - Reply to existing thread
      replyToThread(data, headers, territory);
    } else if (action < 0.7) {
      // 30% - Create new thread
      createNewThread(data, headers, customerId, territory);
    } else if (action < 0.85) {
      // 15% - Search threads
      searchThreads(data, headers, territory);
    } else if (action < 0.95) {
      // 10% - Log phone call
      logPhoneCall(data, headers, customerId, territory);
    } else {
      // 5% - Schedule meeting
      scheduleMeeting(data, headers, customerId, territory);
    }
  });

  sleep(randomIntBetween(3, 8));
}

// Scenario: Follow-up Burst - Automated sample follow-ups
function followUpBurstScenario(data) {
  const headers = {
    'Authorization': `Bearer ${data.tokens.salesRep}`,
    'Content-Type': 'application/json',
  };

  group('Sample Follow-up Automation', () => {
    const customerId = randomItem(data.customerIds);
    const territory = randomItem(territories);
    const followUpType = Math.random() < 0.6 ? 'T+3' : 'T+7';

    // Create follow-up thread
    const slaStart = Date.now();
    const payload = {
      customerId: customerId,
      territory: territory,
      channel: 'EMAIL',
      subject: `Sample Follow-up ${followUpType}`,
      participants: [
        { email: 'kuechenchef@restaurant.de', role: 'Küchenchef' },
        { email: 'einkauf@restaurant.de', role: 'Einkauf' }
      ],
      body: generateFollowUpMessage(followUpType),
      tags: ['sample_delivered', `followup_${followUpType.toLowerCase()}`],
      priority: followUpType === 'T+3' ? 'HIGH' : 'MEDIUM'
    };

    headers['X-Territories'] = territory;
    const res = http.post(
      `${data.baseUrl}/api/comm/threads`,
      JSON.stringify(payload),
      { headers, tags: { name: 'CreateFollowUp' } }
    );
    slaProcessingDuration.add(Date.now() - slaStart);

    check(res, {
      'follow-up created': (r) => r.status === 201,
      'SLA processing < 100ms': (r) => r.timings.duration < 100,
    }) || errorRate.add(1);
  });

  sleep(0.5); // High throughput for burst
}

// Scenario: Concurrent Updates - ETag conflict resolution
function concurrentUpdateScenario(data) {
  const threadId = randomItem(data.sharedThreadIds);
  const headers = {
    'Authorization': `Bearer ${data.tokens.salesRep}`,
    'Content-Type': 'application/json',
  };

  group('Concurrent Thread Updates', () => {
    // Get current thread version
    const getRes = http.get(
      `${data.baseUrl}/api/comm/threads/${threadId}`,
      { headers, tags: { name: 'GetThreadForUpdate' } }
    );

    if (getRes.status === 200) {
      const thread = JSON.parse(getRes.body);
      const etag = `"v${thread.version}"`;

      // Attempt concurrent update
      headers['If-Match'] = etag;
      const updatePayload = {
        body: `Update at ${new Date().toISOString()} by VU ${__VU}`,
      };

      const updateRes = http.post(
        `${data.baseUrl}/api/comm/threads/${threadId}/reply`,
        JSON.stringify(updatePayload),
        { headers, tags: { name: 'ConcurrentReply' } }
      );

      if (updateRes.status === 412) {
        // Expected ETag conflict
        concurrentUpdateErrors.add(1);

        // Retry with updated ETag
        const retryRes = http.get(
          `${data.baseUrl}/api/comm/threads/${threadId}`,
          { headers: { 'Authorization': headers['Authorization'] } }
        );

        if (retryRes.status === 200) {
          const updatedThread = JSON.parse(retryRes.body);
          headers['If-Match'] = `"v${updatedThread.version}"`;

          http.post(
            `${data.baseUrl}/api/comm/threads/${threadId}/reply`,
            JSON.stringify(updatePayload),
            { headers, tags: { name: 'RetryAfterConflict' } }
          );
        }
      } else {
        check(updateRes, {
          'concurrent update succeeded': (r) => r.status === 201,
        });
      }
    }
  });

  sleep(randomIntBetween(1, 2));
}

// Scenario: Search and Analytics
function searchAnalyticsScenario(data) {
  const headers = {
    'Authorization': `Bearer ${data.tokens.salesRep}`,
    'Content-Type': 'application/json',
  };

  group('Search and Analytics Queries', () => {
    const searchType = Math.random();

    if (searchType < 0.3) {
      // Full-text search
      const searchTerms = ['Cook&Fresh', 'Gulasch', 'Sample', 'Follow-up', 'Lieferung'];
      const term = randomItem(searchTerms);

      const searchStart = Date.now();
      const res = http.get(
        `${data.baseUrl}/api/comm/search?q=${encodeURIComponent(term)}&limit=20`,
        { headers, tags: { name: 'FullTextSearch' } }
      );
      searchDuration.add(Date.now() - searchStart);

      check(res, {
        'search completed': (r) => r.status === 200,
        'search < 500ms': (r) => r.timings.duration < 500,
        'search has results': (r) => JSON.parse(r.body).items.length > 0,
      });

    } else if (searchType < 0.6) {
      // Analytics: Thread count by status
      const res = http.get(
        `${data.baseUrl}/api/comm/analytics/thread-status-distribution`,
        { headers, tags: { name: 'AnalyticsStatusDist' } }
      );

      check(res, {
        'analytics loaded': (r) => r.status === 200,
        'analytics < 300ms': (r) => r.timings.duration < 300,
      });

    } else if (searchType < 0.8) {
      // SLA compliance report
      const res = http.get(
        `${data.baseUrl}/api/comm/analytics/sla-compliance?days=7`,
        { headers, tags: { name: 'SLACompliance' } }
      );

      check(res, {
        'SLA report loaded': (r) => r.status === 200,
        'SLA report < 400ms': (r) => r.timings.duration < 400,
      });

    } else {
      // Message volume trends
      const res = http.get(
        `${data.baseUrl}/api/comm/analytics/message-volume?period=monthly`,
        { headers, tags: { name: 'MessageVolume' } }
      );

      check(res, {
        'volume trends loaded': (r) => r.status === 200,
        'trends < 350ms': (r) => r.timings.duration < 350,
      });
    }
  });

  sleep(randomIntBetween(2, 5));
}

// Helper Functions

function authenticateUser(baseUrl, username, password) {
  const res = http.post(
    `${baseUrl}/auth/login`,
    JSON.stringify({ username, password }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  return res.status === 200 ? JSON.parse(res.body).access_token : null;
}

function generateCustomerIds(count) {
  const ids = [];
  for (let i = 0; i < count; i++) {
    ids.push(`${Math.random().toString(36).substr(2, 8)}-customer-${i}`);
  }
  return ids;
}

function generateThreadIds(baseUrl, token, count) {
  // Create some threads to use for concurrent updates
  const ids = [];
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
    'X-Territories': 'BER',
  };

  for (let i = 0; i < count; i++) {
    const res = http.post(
      `${baseUrl}/api/comm/threads`,
      JSON.stringify({
        customerId: `test-customer-${i}`,
        territory: 'BER',
        channel: 'EMAIL',
        subject: `Test Thread ${i}`,
        body: 'Test content for concurrent updates',
      }),
      { headers }
    );

    if (res.status === 201) {
      ids.push(JSON.parse(res.body).id);
    }
  }

  return ids;
}

function replyToThread(data, headers, territory) {
  // Get an open thread
  headers['X-Territories'] = territory;
  const threadsRes = http.get(
    `${data.baseUrl}/api/comm/threads?territory=${territory}&status=OPEN&limit=10`,
    { headers }
  );

  if (threadsRes.status === 200) {
    const threads = JSON.parse(threadsRes.body).items;
    if (threads.length > 0) {
      const thread = randomItem(threads);

      const sendStart = Date.now();
      headers['If-Match'] = `"v${thread.version}"`;
      const res = http.post(
        `${data.baseUrl}/api/comm/threads/${thread.id}/reply`,
        JSON.stringify({
          body: generateBusinessReply(),
        }),
        { headers, tags: { name: 'ReplyToThread' } }
      );
      messageSendDuration.add(Date.now() - sendStart);

      check(res, {
        'reply sent': (r) => r.status === 201 || r.status === 412,
        'reply < 300ms': (r) => r.timings.duration < 300,
      });
    }
  }
}

function createNewThread(data, headers, customerId, territory) {
  const template = randomItem(messageTemplates);

  headers['X-Territories'] = territory;
  const sendStart = Date.now();
  const res = http.post(
    `${data.baseUrl}/api/comm/threads`,
    JSON.stringify({
      customerId: customerId,
      territory: territory,
      channel: 'EMAIL',
      subject: template.subject,
      body: generateEmailBody(template.subject),
      participants: generateParticipants(),
    }),
    { headers, tags: { name: 'CreateThread' } }
  );
  messageSendDuration.add(Date.now() - sendStart);

  check(res, {
    'thread created': (r) => r.status === 201,
    'create < 300ms': (r) => r.timings.duration < 300,
  });
}

function searchThreads(data, headers, territory) {
  const searchTerms = [
    'Cook&Fresh',
    'Sample',
    'Gulasch',
    'Bolognese',
    'Follow-up',
    'T+3',
    'T+7',
    territory,
  ];

  const term = randomItem(searchTerms);
  const searchStart = Date.now();
  const res = http.get(
    `${data.baseUrl}/api/comm/threads?q=${encodeURIComponent(term)}&territory=${territory}`,
    { headers, tags: { name: 'SearchThreads' } }
  );
  searchDuration.add(Date.now() - searchStart);

  check(res, {
    'search completed': (r) => r.status === 200,
    'search < 500ms': (r) => r.timings.duration < 500,
  });
}

function logPhoneCall(data, headers, customerId, territory) {
  headers['X-Territories'] = territory;
  const res = http.post(
    `${data.baseUrl}/api/comm/activities`,
    JSON.stringify({
      customerId: customerId,
      territory: territory,
      channel: 'PHONE',
      phoneNumber: `+49 30 ${randomIntBetween(1000000, 9999999)}`,
      duration: `${randomIntBetween(1, 15)}m ${randomIntBetween(0, 59)}s`,
      notes: 'Discussed new Cook&Fresh products and pricing',
      contactPerson: 'Küchenchef',
    }),
    { headers, tags: { name: 'LogPhoneCall' } }
  );

  check(res, {
    'phone call logged': (r) => r.status === 201,
    'log < 200ms': (r) => r.timings.duration < 200,
  });
}

function scheduleMeeting(data, headers, customerId, territory) {
  headers['X-Territories'] = territory;
  const futureDate = new Date();
  futureDate.setDate(futureDate.getDate() + randomIntBetween(1, 14));

  const res = http.post(
    `${data.baseUrl}/api/comm/activities`,
    JSON.stringify({
      customerId: customerId,
      territory: territory,
      channel: 'MEETING',
      scheduledAt: futureDate.toISOString(),
      location: 'Restaurant vor Ort',
      agenda: 'Cook&Fresh Produktvorstellung und Verkostung',
      attendees: ['kuechenchef@restaurant.de', 'einkauf@restaurant.de'],
    }),
    { headers, tags: { name: 'ScheduleMeeting' } }
  );

  check(res, {
    'meeting scheduled': (r) => r.status === 201,
    'schedule < 250ms': (r) => r.timings.duration < 250,
  });
}

function generateFollowUpMessage(type) {
  const messages = {
    'T+3': 'Guten Tag, vor 3 Tagen haben Sie unsere Cook&Fresh Produkte erhalten. Wie war Ihr erster Eindruck? Gerne stehe ich für Fragen zur Verfügung.',
    'T+7': 'Hallo, eine Woche ist vergangen seit Sie unsere Cook&Fresh Produkte testen. Wie sind die Erfahrungen Ihres Küchenteams? Können wir über eine Bestellung sprechen?'
  };
  return messages[type] || messages['T+3'];
}

function generateBusinessReply() {
  const replies = [
    'Vielen Dank für Ihre Nachricht. Wir werden das intern besprechen.',
    'Die Produkte haben uns überzeugt. Bitte senden Sie uns ein Angebot.',
    'Wir benötigen noch weitere Informationen zu den Lieferbedingungen.',
    'Können wir einen Termin für eine ausführliche Besprechung vereinbaren?',
    'Die Qualität ist gut, aber wir müssen noch über den Preis verhandeln.',
  ];
  return randomItem(replies);
}

function generateEmailBody(subject) {
  const baseBody = 'Sehr geehrte Damen und Herren,\n\n';
  const endings = '\n\nMit freundlichen Grüßen\nIhr FreshFoodz Team';

  const bodies = {
    'Sample Follow-up': 'wir hoffen, Sie hatten Gelegenheit, unsere Cook&Fresh Produkte zu testen.',
    'Neue Produkte': 'wir freuen uns, Ihnen unser erweitertes Cook&Fresh Sortiment vorstellen zu dürfen.',
    'Sonderaktion': 'profitieren Sie von unserer aktuellen Sonderaktion für Ihre Region.',
    'Terminvereinbarung': 'gerne würden wir Ihnen unsere Produkte persönlich vorstellen.',
    'Bestellbestätigung': 'vielen Dank für Ihre Bestellung. Die Lieferung erfolgt wie besprochen.',
  };

  for (const [key, body] of Object.entries(bodies)) {
    if (subject.includes(key)) {
      return baseBody + body + endings;
    }
  }

  return baseBody + 'wir möchten Ihnen unsere Cook&Fresh Produkte vorstellen.' + endings;
}

function generateParticipants() {
  const allParticipants = [
    { email: 'kuechenchef@restaurant.de', role: 'Küchenchef' },
    { email: 'einkauf@restaurant.de', role: 'Einkauf' },
    { email: 'geschaeftsfuehrer@restaurant.de', role: 'Geschäftsführer' },
    { email: 'f&b@hotel.de', role: 'F&B Manager' },
  ];

  const count = randomIntBetween(1, 3);
  const selected = [];
  for (let i = 0; i < count; i++) {
    selected.push(allParticipants[i]);
  }
  return selected;
}

// Teardown: Clean up test data
export function teardown(data) {
  // Clean up test threads created during setup
  if (data.sharedThreadIds && data.sharedThreadIds.length > 0) {
    const headers = {
      'Authorization': `Bearer ${data.tokens.salesRep}`,
    };

    data.sharedThreadIds.forEach(threadId => {
      http.del(`${data.baseUrl}/api/comm/threads/${threadId}`, null, { headers });
    });
  }
}

// Handle test summary
export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: '→ ', enableColors: true }),
    './reports/communication-load-test.json': JSON.stringify(data),
    './reports/communication-load-test.html': htmlReport(data),
  };
}