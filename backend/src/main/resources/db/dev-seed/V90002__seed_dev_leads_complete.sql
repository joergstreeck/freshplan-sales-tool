-- ============================================================================
-- V90002: DEV-SEED Leads Complete
-- ============================================================================
-- Zweck: Realistische Lead-Daten für manuelle Frontend-Entwicklung
--
-- Inhalt:
--   - 10 Leads (IDs 90001-90010)
--   - 21 Lead Contacts (2-3 pro Lead, 1x is_primary per Lead)
--   - 21 Lead Activities (2 pro Lead, inkl. CREATED manuell)
--
-- Score-Verteilung:
--   🔥 HOT (55-60):        Lead 90003 (59), Lead 90007 (57)
--   📊 HIGH-PRIORITY (45-54): Lead 90004 (46), Lead 90008 (~55), Lead 90009 (~50)
--   ⚡ MEDIUM (35-44):     Lead 90002 (~40), Lead 90005 (37)
--   📉 LOW (20-34):        Lead 90001 (30), Lead 90006 (23), Lead 90010 (23)
--
-- Protection States:
--   - Fresh (5d):          Lead 90001
--   - Pre-Reminder (58d):  Lead 90002
--   - Grace Period (68d):  Lead 90005
--   - PreClaim (3d):       Lead 90006
--
-- Wichtig:
--   - RLS nicht nötig (freshplan User ist SUPERUSER)
--   - stage ist SMALLINT (0/1/2), nicht String!
--   - owner_user_id = 'admin' (konsistent)
--   - ON CONFLICT DO NOTHING (Idempotenz)
-- ============================================================================
-- BLOCK 1: LEADS (10 Leads)
-- ============================================================================

INSERT INTO leads (
  -- IDs
  id,

  -- Basic Info
  company_name,
  company_name_normalized,
  contact_person,
  email,
  email_normalized,
  phone,
  phone_e164,
  website,
  website_domain,

  -- Address
  street,
  postal_code,
  city,
  country_code,
  territory_id,

  -- Business Type & Size
  business_type,
  kitchen_size,
  employee_count,
  estimated_volume,
  branch_count,
  is_chain,

  -- Lead State
  status,
  stage,
  source,
  source_campaign,

  -- Ownership
  owner_user_id,
  collaborator_user_ids,

  -- Timestamps (Protection)
  created_at,
  registered_at,
  protection_start_at,
  first_contact_documented_at,
  last_activity_at,
  reminder_sent_at,
  grace_period_start_at,

  -- Protection Config
  protection_months,
  protection_days_60,
  protection_days_10,

  -- Pain Points (8 Boolean Flags)
  pain_staff_shortage,
  pain_high_costs,
  pain_food_waste,
  pain_quality_inconsistency,
  pain_time_pressure,
  pain_supplier_quality,
  pain_unreliable_delivery,
  pain_poor_service,
  pain_notes,

  -- Urgency
  urgency_level,
  multi_pain_bonus,

  -- Engagement
  relationship_status,
  decision_maker_access,
  internal_champion_name,
  competitor_in_use,

  -- Revenue Indicators
  budget_confirmed,
  deal_size,

  -- Scores
  pain_score,
  revenue_score,
  fit_score,
  engagement_score,
  lead_score,

  -- Audit
  created_by,
  updated_by,
  updated_at,
  version,

  -- Flags
  is_canonical,
  metadata
) VALUES

-- ============================================================================
-- LEAD 90001: Café Amadeus Berlin (Fresh Lead - 5 days old, Score 30)
-- ============================================================================
(
  90001,

  -- Basic
  'Café Amadeus Berlin',
  'cafe amadeus berlin',
  'Max Müller',
  'mueller@cafe-amadeus.example',
  'mueller@cafe-amadeus.example',
  '+49 30 12345601',
  '+493012345601',
  'https://cafe-amadeus.example',
  'cafe-amadeus.example',

  -- Address
  'Friedrichstraße 42',
  '10115',
  'Berlin',
  'DE',
  NULL,

  -- Business
  'RESTAURANT',
  'KLEIN',
  8,
  1500.00,
  1,
  FALSE,

  -- State
  'ACTIVE',
  1, -- REGISTRIERUNG
  'EMPFEHLUNG',
  'Partner-Netzwerk Berlin 2025',

  -- Ownership
  'admin',
  ARRAY[]::text[],

  -- Timestamps
  NOW() - INTERVAL '5 days',
  NOW() - INTERVAL '5 days',
  NOW() - INTERVAL '5 days',
  NOW() - INTERVAL '4 days',
  NOW() - INTERVAL '3 days',
  NULL,
  NULL,

  -- Protection
  6,
  60,
  10,

  -- Pain Points (3 aktiv)
  TRUE,  -- staff_shortage
  TRUE,  -- high_costs
  FALSE, -- food_waste
  FALSE, -- quality_inconsistency
  FALSE, -- time_pressure
  FALSE, -- supplier_quality
  TRUE,  -- unreliable_delivery
  FALSE, -- poor_service
  'Personalmangel kritisch, Koch verlässt uns nächsten Monat. Bisheriger Lieferant zu teuer und unzuverlässig.',

  -- Urgency
  'HIGH',
  0, -- Nur 3 Pains, kein Multi-Bonus

  -- Engagement
  'ENGAGED_POSITIVE',
  'IS_DECISION_MAKER',
  'Max Müller',
  'Metro',

  -- Revenue
  TRUE, -- budget_confirmed
  'SMALL',

  -- Scores
  25,  -- pain_score (10+7+8)
  40,  -- revenue_score
  45,  -- fit_score
  25,  -- engagement_score
  30,  -- lead_score AVG(10, 40, 45, 25) ≈ 30

  -- Audit
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '3 days',
  0,

  -- Flags
  TRUE,
  '{}'::jsonb
),

-- ============================================================================
-- LEAD 90002: Gasthaus zur Linde Stuttgart (Pre-Reminder - 58 days, Score ~40)
-- ============================================================================
(
  90002,

  -- Basic
  'Gasthaus zur Linde Stuttgart',
  'gasthaus zur linde stuttgart',
  'Thomas Weber',
  'weber@gasthaus-linde.example',
  'weber@gasthaus-linde.example',
  '+49 711 2345601',
  '+497112345601',
  'https://gasthaus-linde.example',
  'gasthaus-linde.example',

  -- Address
  'Hauptstätter Straße 88',
  '70173',
  'Stuttgart',
  'DE',
  NULL,

  -- Business
  'RESTAURANT',
  'MITTEL',
  22,
  6000.00,
  1,
  FALSE,

  -- State
  'ACTIVE',
  1, -- REGISTRIERUNG
  'MESSE',
  'INTERGASTRA 2025',

  -- Ownership
  'admin',
  ARRAY[]::text[],

  -- Timestamps (58 days old - kurz vor Reminder!)
  NOW() - INTERVAL '58 days',
  NOW() - INTERVAL '58 days',
  NOW() - INTERVAL '58 days',
  NOW() - INTERVAL '58 days', -- MESSE erfordert first_contact
  NOW() - INTERVAL '30 days', -- Lange keine Aktivität!
  NULL,
  NULL,

  -- Protection
  6,
  60,
  10,

  -- Pain Points (2 aktiv)
  FALSE, -- staff_shortage
  FALSE, -- high_costs
  TRUE,  -- food_waste
  TRUE,  -- quality_inconsistency
  FALSE, -- time_pressure
  FALSE, -- supplier_quality
  FALSE, -- unreliable_delivery
  FALSE, -- poor_service
  'Hohe Lebensmittelverschwendung durch ungenaue Planung. Qualität schwankt.',

  -- Urgency
  'NORMAL',
  0,

  -- Engagement
  'CONTACTED',
  'INDIRECT',
  NULL,
  'Transgourmet',

  -- Revenue
  FALSE,
  'MEDIUM',

  -- Scores
  13,  -- pain_score (7+6)
  45,  -- revenue_score
  65,  -- fit_score
  8,   -- engagement_score
  33,  -- lead_score AVG(8, 45, 65, 8) ≈ 32

  -- Audit
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '30 days',
  0,

  -- Flags
  TRUE,
  '{}'::jsonb
),

-- ============================================================================
-- LEAD 90003: Hotel Seeblick Konstanz (HOT LEAD #1 - Score 59) 🔥
-- ============================================================================
(
  90003,

  -- Basic
  'Hotel Seeblick Konstanz',
  'hotel seeblick konstanz',
  'Julia Becker',
  'becker@hotel-seeblick.example',
  'becker@hotel-seeblick.example',
  '+49 7531 345601',
  '+497531345601',
  'https://hotel-seeblick.example',
  'hotel-seeblick.example',

  -- Address
  'Seestraße 15',
  '78462',
  'Konstanz',
  'DE',
  NULL,

  -- Business
  'HOTEL',
  'GROSS',
  85,
  28000.00,
  1,
  FALSE,

  -- State
  'QUALIFIED',
  2, -- QUALIFIZIERT
  'WEB_FORMULAR',
  'Google Ads Premium Hotels Q1 2025',

  -- Ownership
  'admin',
  ARRAY[]::text[],

  -- Timestamps (30 days old)
  NOW() - INTERVAL '30 days',
  NOW() - INTERVAL '30 days',
  NOW() - INTERVAL '30 days',
  NOW() - INTERVAL '28 days',
  NOW() - INTERVAL '3 days', -- Recent activity!
  NULL,
  NULL,

  -- Protection
  6,
  60,
  10,

  -- Pain Points (7 aktiv - MAXIMUM!)
  TRUE,  -- staff_shortage
  TRUE,  -- high_costs
  TRUE,  -- food_waste (NEW!)
  TRUE,  -- quality_inconsistency
  TRUE,  -- time_pressure (NEW!)
  TRUE,  -- supplier_quality
  TRUE,  -- unreliable_delivery
  FALSE, -- poor_service
  'KRITISCHE LAGE: Saison startet in 2 Wochen! Bisheriger Lieferant (Selgros) fiel KOMPLETT aus - keine Lieferungen mehr. Koch hat gekündigt wegen Stress. Lebensmittelverschwendung durch schlechte Planung. Qualität schwankt extrem. Zeitdruck enorm - müssen SOFORT Lösung haben!',

  -- Urgency
  'EMERGENCY',
  10, -- 7 Pains ≥ 4 → Multi-Bonus!

  -- Engagement
  'ADVOCATE', -- Höchste Stufe!
  'IS_DECISION_MAKER',
  'Julia Becker (F&B Manager)',
  'Selgros (fiel aus!)',

  -- Revenue
  TRUE,
  'ENTERPRISE',

  -- Scores
  59,  -- pain_score (10+7+7+6+5+10+8 = 53, -4 Cap, +10 Bonus = 59)
  100, -- revenue_score (Enterprise + Budget)
  85,  -- fit_score (GROSS + HOTEL)
  25,  -- engagement_score (CAPPED bei 25!)
  59,  -- lead_score AVG(24, 100, 85, 25) ≈ 59 🔥

  -- Audit
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '3 days',
  0,

  -- Flags
  TRUE,
  '{}'::jsonb
),

-- ============================================================================
-- LEAD 90004: Kantine TechCorp München (LOST - Competitor Won, Score 46)
-- ============================================================================
(
  90004,

  -- Basic
  'Kantine TechCorp München',
  'kantine techcorp munchen',
  'Peter Fischer',
  'fischer@techcorp-kantine.example',
  'fischer@techcorp-kantine.example',
  '+49 89 4567801',
  '+49894567801',
  'https://techcorp-kantine.example',
  'techcorp-kantine.example',

  -- Address
  'Kaufingerstraße 10',
  '80331',
  'München',
  'DE',
  NULL,

  -- Business
  'KANTINE',
  'GROSS',
  120,
  42000.00,
  1,
  FALSE,

  -- State
  'LOST',
  2, -- War QUALIFIZIERT
  'PARTNER',
  'IT-Catering-Partner Q4 2024',

  -- Ownership
  'admin',
  ARRAY[]::text[],

  -- Timestamps (90 days old)
  NOW() - INTERVAL '90 days',
  NOW() - INTERVAL '90 days',
  NOW() - INTERVAL '90 days',
  NOW() - INTERVAL '88 days',
  NOW() - INTERVAL '7 days', -- Lost notification
  NULL,
  NULL,

  -- Protection
  6,
  60,
  10,

  -- Pain Points (nur 1)
  FALSE, -- staff_shortage
  TRUE,  -- high_costs
  FALSE, -- food_waste
  FALSE, -- quality_inconsistency
  FALSE, -- time_pressure
  FALSE, -- supplier_quality
  FALSE, -- unreliable_delivery
  FALSE, -- poor_service
  NULL,

  -- Urgency
  'NORMAL',
  0,

  -- Engagement
  'ENGAGED_SKEPTICAL',
  'BLOCKED', -- Procurement blockte!
  NULL,
  'Compass Group (GEWONNEN!)',

  -- Revenue
  TRUE,
  'ENTERPRISE',

  -- Scores
  7,   -- pain_score
  90,  -- revenue_score
  80,  -- fit_score
  3,   -- engagement_score (BLOCKED = -3, aber Min ist 0)
  45,  -- lead_score AVG(4, 90, 80, 3) ≈ 44

  -- Audit
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '7 days',
  0,

  -- Flags
  TRUE,
  '{}'::jsonb
),

-- ============================================================================
-- LEAD 90005: Seniorenheim Alpenblick (GRACE PERIOD - KRITISCH, Score 37)
-- ============================================================================
(
  90005,

  -- Basic
  'Seniorenheim Alpenblick Bad Tölz',
  'seniorenheim alpenblick bad tolz',
  'Monika Wagner',
  'wagner@alpenblick.example',
  'wagner@alpenblick.example',
  '+49 8041 567801',
  '+498041567801',
  'https://alpenblick.example',
  'alpenblick.example',

  -- Address
  'Ludwigstraße 22',
  '83646',
  'Bad Tölz',
  'DE',
  NULL,

  -- Business
  'GESUNDHEIT',
  'MITTEL',
  45,
  12000.00,
  1,
  FALSE,

  -- State
  'GRACE_PERIOD', -- KRITISCH!
  1, -- REGISTRIERUNG
  'TELEFON',
  NULL,

  -- Ownership
  'admin',
  ARRAY[]::text[],

  -- Timestamps (68 days old - IN GRACE PERIOD!)
  NOW() - INTERVAL '68 days',
  NOW() - INTERVAL '68 days',
  NOW() - INTERVAL '68 days',
  NOW() - INTERVAL '68 days', -- TELEFON erfordert first_contact
  NOW() - INTERVAL '68 days', -- KEINE Aktivität seit 68 Tagen!
  NOW() - INTERVAL '8 days',  -- Reminder sent
  NOW() - INTERVAL '8 days',  -- Grace period started

  -- Protection
  6,
  60,
  10,

  -- Pain Points (1)
  TRUE,  -- staff_shortage
  FALSE, -- high_costs
  FALSE, -- food_waste
  FALSE, -- quality_inconsistency
  FALSE, -- time_pressure
  FALSE, -- supplier_quality
  FALSE, -- unreliable_delivery
  FALSE, -- poor_service
  NULL,

  -- Urgency
  'NORMAL',
  0,

  -- Engagement
  'COLD',
  'UNKNOWN',
  NULL,
  NULL,

  -- Revenue
  FALSE,
  'LARGE',

  -- Scores
  10,  -- pain_score
  65,  -- revenue_score
  70,  -- fit_score
  0,   -- engagement_score (COLD + UNKNOWN + recency malus)
  36,  -- lead_score AVG(6, 65, 70, 0) ≈ 35

  -- Audit
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '68 days',
  0,

  -- Flags
  TRUE,
  '{}'::jsonb
),

-- ============================================================================
-- LEAD 90006: Bäckerei Schmidt Nürnberg (PreClaim - VORMERKUNG, Score 23)
-- ============================================================================
(
  90006,

  -- Basic
  'Bäckerei Schmidt Nürnberg',
  'backerei schmidt nurnberg',
  NULL, -- PreClaim hat KEINE Kontaktperson!
  NULL,
  NULL,
  NULL,
  NULL,
  NULL,
  NULL,

  -- Address
  'Breite Gasse 5',
  '90402',
  'Nürnberg',
  'DE',
  NULL,

  -- Business
  'LEH',
  'KLEIN',
  12,
  2400.00,
  1,
  FALSE,

  -- State
  'REGISTERED',
  0, -- VORMERKUNG (PreClaim!)
  'WEB_FORMULAR',
  'Google Ads Bäckereien Bayern',

  -- Ownership
  'admin',
  ARRAY[]::text[],

  -- Timestamps (3 days old - PreClaim window!)
  NOW() - INTERVAL '3 days',
  NOW() - INTERVAL '3 days',
  NOW() - INTERVAL '3 days',
  NULL, -- PreClaim: Kein first_contact!
  NULL,
  NULL,
  NULL,

  -- Protection
  6,
  60,
  10,

  -- Pain Points (2)
  FALSE, -- staff_shortage
  TRUE,  -- high_costs
  FALSE, -- food_waste
  FALSE, -- quality_inconsistency
  TRUE,  -- time_pressure
  FALSE, -- supplier_quality
  FALSE, -- unreliable_delivery
  FALSE, -- poor_service
  NULL,

  -- Urgency
  'MEDIUM',
  0,

  -- Engagement
  'COLD',
  'UNKNOWN',
  NULL,
  NULL,

  -- Revenue
  FALSE,
  'SMALL',

  -- Scores
  12,  -- pain_score (7+5)
  30,  -- revenue_score
  40,  -- fit_score
  0,   -- engagement_score
  21,  -- lead_score AVG(9, 30, 40, 0) ≈ 20

  -- Audit
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '3 days',
  0,

  -- Flags
  TRUE,
  '{}'::jsonb
),

-- ============================================================================
-- LEAD 90007: Universitätsmensa Heidelberg (HOT LEAD #2 - Score 57) 🔥
-- ============================================================================
(
  90007,

  -- Basic
  'Universitätsmensa Heidelberg',
  'universitatsmensa heidelberg',
  'Dr. Anna Lehmann',
  'lehmann@uni-heidelberg.example',
  'lehmann@uni-heidelberg.example',
  '+49 6221 678902',
  '+496221678902',
  'https://mensa-heidelberg.example',
  'mensa-heidelberg.example',

  -- Address
  'Universitätsplatz 1',
  '69117',
  'Heidelberg',
  'DE',
  NULL,

  -- Business
  'BILDUNG',
  'SEHR_GROSS', -- UPGRADE!
  180,
  85000.00,
  1,
  FALSE,

  -- State
  'ACTIVE',
  1, -- REGISTRIERUNG
  'MESSE',
  'Bildungsmesse Karlsruhe 2025',

  -- Ownership
  'admin',
  ARRAY[]::text[],

  -- Timestamps (25 days old)
  NOW() - INTERVAL '25 days',
  NOW() - INTERVAL '25 days',
  NOW() - INTERVAL '25 days',
  NOW() - INTERVAL '25 days',
  NOW() - INTERVAL '10 days',
  NULL,
  NULL,

  -- Protection
  6,
  60,
  10,

  -- Pain Points (4 aktiv - Multi-Bonus!)
  TRUE,  -- staff_shortage
  FALSE, -- high_costs
  TRUE,  -- food_waste
  TRUE,  -- quality_inconsistency
  TRUE,  -- time_pressure
  FALSE, -- supplier_quality
  FALSE, -- unreliable_delivery
  FALSE, -- poor_service
  'Personalmangel kritisch. Hohe Lebensmittelverschwendung. Qualität schwankt. Zeitdruck: Semester-Start April!',

  -- Urgency
  'EMERGENCY', -- UPGRADE! (Aramark fiel aus)
  10, -- 4 Pains → Multi-Bonus

  -- Engagement
  'ENGAGED_POSITIVE',
  'DIRECT', -- UPGRADE! (Champion brachte uns zur Chefin)
  'Robert Kraus (Mensaleiter)',
  'Aramark (Vertrag nicht verlängert!)',

  -- Revenue
  FALSE, -- Öffentliche Ausschreibung
  'ENTERPRISE',

  -- Scores
  34,  -- pain_score (10+7+6+5 = 28, -4 Cap, +10 Bonus = 34)
  95,  -- revenue_score (Enterprise, aber Budget not confirmed)
  95,  -- fit_score (SEHR_GROSS + BILDUNG = Perfect Match!)
  20,  -- engagement_score
  57,  -- lead_score AVG(18, 95, 95, 20) ≈ 57 🔥

  -- Audit
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '10 days',
  0,

  -- Flags
  TRUE,
  '{}'::jsonb
),

-- ============================================================================
-- LEAD 90008: Catering Eventgarten Frankfurt (High-Priority, Score ~55)
-- ============================================================================
(
  90008,

  -- Basic
  'Catering Eventgarten Frankfurt',
  'catering eventgarten frankfurt',
  'Claudia Richter',
  'richter@eventgarten.example',
  'richter@eventgarten.example',
  '+49 69 7890101',
  '+49697890101',
  'https://eventgarten.example',
  'eventgarten.example',

  -- Address
  'Zeil 88',
  '60311',
  'Frankfurt am Main',
  'DE',
  NULL,

  -- Business
  'CATERING',
  'GROSS',
  65,
  32000.00,
  2,
  TRUE, -- Chain!

  -- State
  'QUALIFIED',
  2, -- QUALIFIZIERT
  'EMPFEHLUNG',
  'Event-Netzwerk Hessen',

  -- Ownership
  'admin',
  ARRAY[]::text[],

  -- Timestamps (15 days old)
  NOW() - INTERVAL '15 days',
  NOW() - INTERVAL '15 days',
  NOW() - INTERVAL '15 days',
  NOW() - INTERVAL '14 days',
  NOW() - INTERVAL '2 days', -- Recent!
  NULL,
  NULL,

  -- Protection
  6,
  60,
  10,

  -- Pain Points (3)
  FALSE, -- staff_shortage
  FALSE, -- high_costs
  FALSE, -- food_waste
  FALSE, -- quality_inconsistency
  TRUE,  -- time_pressure
  FALSE, -- supplier_quality
  TRUE,  -- unreliable_delivery
  TRUE,  -- poor_service
  'Bisheriger Lieferant liefert oft zu spät + schlechter Service. Event-Saison startet!',

  -- Urgency
  'EMERGENCY',
  0, -- Nur 3 Pains

  -- Engagement
  'TRUSTED',
  'IS_DECISION_MAKER',
  'Claudia Richter (Geschäftsführerin)',
  'Lokaler Großhändler',

  -- Revenue
  TRUE,
  'ENTERPRISE',

  -- Scores
  16,  -- pain_score (8+3+5)
  100, -- revenue_score
  85,  -- fit_score
  24,  -- engagement_score
  56,  -- lead_score AVG(14, 100, 85, 24) ≈ 56

  -- Audit
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '2 days',
  0,

  -- Flags
  TRUE,
  '{}'::jsonb
),

-- ============================================================================
-- LEAD 90009: Klinikum Charité Berlin (High-Priority, Score ~50)
-- ============================================================================
(
  90009,

  -- Basic
  'Klinikum Charité Berlin',
  'klinikum charite berlin',
  'Dr. Markus Stein',
  'stein@charite.example',
  'stein@charite.example',
  '+49 30 9876501',
  '+49309876501',
  'https://charite.example',
  'charite.example',

  -- Address
  'Charitéplatz 1',
  '10117',
  'Berlin',
  'DE',
  NULL,

  -- Business
  'GESUNDHEIT',
  'SEHR_GROSS',
  350,
  125000.00,
  1,
  FALSE,

  -- State
  'ACTIVE',
  1, -- REGISTRIERUNG
  'PARTNER',
  'Healthcare Partner Network Q1 2025',

  -- Ownership
  'admin',
  ARRAY[]::text[],

  -- Timestamps (45 days old)
  NOW() - INTERVAL '45 days',
  NOW() - INTERVAL '45 days',
  NOW() - INTERVAL '45 days',
  NOW() - INTERVAL '42 days',
  NOW() - INTERVAL '15 days',
  NULL,
  NULL,

  -- Protection
  6,
  60,
  10,

  -- Pain Points (2)
  FALSE, -- staff_shortage
  TRUE,  -- high_costs
  FALSE, -- food_waste
  FALSE, -- quality_inconsistency
  FALSE, -- time_pressure
  TRUE,  -- supplier_quality
  FALSE, -- unreliable_delivery
  FALSE, -- poor_service
  NULL,

  -- Urgency
  'MEDIUM',
  0,

  -- Engagement
  'ENGAGED_SKEPTICAL',
  'BLOCKED', -- Procurement blockt
  NULL,
  'Apetito',

  -- Revenue
  FALSE,
  'ENTERPRISE',

  -- Scores
  17,  -- pain_score (10+7)
  85,  -- revenue_score
  95,  -- fit_score (SEHR_GROSS + GESUNDHEIT)
  4,   -- engagement_score (BLOCKED!)
  50,  -- lead_score AVG(11, 85, 95, 4) ≈ 49

  -- Audit
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '15 days',
  0,

  -- Flags
  TRUE,
  '{}'::jsonb
),

-- ============================================================================
-- LEAD 90010: Restaurant Dolce Vita Hamburg (Low-Priority, Score 23)
-- ============================================================================
(
  90010,

  -- Basic
  'Restaurant Dolce Vita Hamburg',
  'restaurant dolce vita hamburg',
  'Giuseppe Rossi',
  'rossi@dolcevita-hamburg.example',
  'rossi@dolcevita-hamburg.example',
  '+49 40 3456701',
  '+49403456701',
  'https://dolcevita-hamburg.example',
  'dolcevita-hamburg.example',

  -- Address
  'Mönckebergstraße 33',
  '20095',
  'Hamburg',
  'DE',
  NULL,

  -- Business
  'RESTAURANT',
  'KLEIN',
  10,
  1800.00,
  1,
  FALSE,

  -- State
  'ACTIVE',
  1, -- REGISTRIERUNG
  'TELEFON',
  NULL,

  -- Ownership
  'admin',
  ARRAY[]::text[],

  -- Timestamps (12 days old)
  NOW() - INTERVAL '12 days',
  NOW() - INTERVAL '12 days',
  NOW() - INTERVAL '12 days',
  NOW() - INTERVAL '12 days',
  NOW() - INTERVAL '8 days',
  NULL,
  NULL,

  -- Protection
  6,
  60,
  10,

  -- Pain Points (1)
  FALSE, -- staff_shortage
  FALSE, -- high_costs
  FALSE, -- food_waste
  FALSE, -- quality_inconsistency
  FALSE, -- time_pressure
  FALSE, -- supplier_quality
  FALSE, -- unreliable_delivery
  TRUE,  -- poor_service
  NULL,

  -- Urgency
  'NORMAL',
  0,

  -- Engagement
  'CONTACTED',
  'IS_DECISION_MAKER',
  NULL,
  'Metro',

  -- Revenue
  FALSE,
  'SMALL',

  -- Scores
  3,   -- pain_score
  25,  -- revenue_score
  45,  -- fit_score
  17,  -- engagement_score
  23,  -- lead_score AVG(3, 25, 45, 17) ≈ 23

  -- Audit
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '8 days',
  0,

  -- Flags
  TRUE,
  '{}'::jsonb
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- BLOCK 2: LEAD CONTACTS (21 Contacts - 2-3 per Lead, except PreClaim)
-- ============================================================================

INSERT INTO lead_contacts (
  id,
  lead_id,
  first_name,
  last_name,
  position,
  email,
  phone,
  mobile,
  is_primary,
  is_decision_maker,
  decision_level,
  warmth_score,
  personal_notes,
  created_at,
  created_by,
  updated_by,
  updated_at
) VALUES

-- ============================================================================
-- LEAD 90001: Café Amadeus Berlin (2 Contacts)
-- ============================================================================
(
  gen_random_uuid(),
  90001,
  'Max',
  'Müller',
  'Geschäftsführer & Inhaber',
  'mueller@cafe-amadeus.example',
  '+49 30 12345601',
  '+49 160 1234501',
  TRUE, -- is_primary (sync to leads!)
  TRUE, -- is_decision_maker
  'EXECUTIVE',
  75,
  'Sehr offen für Gespräch. Dringend auf der Suche nach zuverlässigem Partner. Koch kündigt nächsten Monat.',
  NOW() - INTERVAL '5 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '3 days'
),
(
  gen_random_uuid(),
  90001,
  'Lisa',
  'Schäfer',
  'Küchenleiterin',
  'schaefer@cafe-amadeus.example',
  NULL,
  '+49 170 9876501',
  FALSE,
  FALSE,
  'INFLUENCER',
  60,
  'Pragmatisch, möchte schnelle Lösung. Beklagt Qualität von Metro-Produkten.',
  NOW() - INTERVAL '4 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '4 days'
),

-- ============================================================================
-- LEAD 90002: Gasthaus zur Linde Stuttgart (2 Contacts)
-- ============================================================================
(
  gen_random_uuid(),
  90002,
  'Thomas',
  'Weber',
  'Geschäftsführer',
  'weber@gasthaus-linde.example',
  '+49 711 2345601',
  '+49 171 8765401',
  TRUE, -- is_primary
  TRUE,
  'EXECUTIVE',
  45,
  'War enthusiastisch nach Messe, aber schwer erreichbar. Antwortet selten auf E-Mails.',
  NOW() - INTERVAL '58 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '30 days'
),
(
  gen_random_uuid(),
  90002,
  'Sabine',
  'Klein',
  'Serviceleiterin',
  'klein@gasthaus-linde.example',
  NULL,
  '+49 172 3456701',
  FALSE,
  FALSE,
  'Informant',
  30,
  'Freundlich, aber wenig Einfluss. Weist auf Chef hin.',
  NOW() - INTERVAL '30 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '30 days'
),

-- ============================================================================
-- LEAD 90003: Hotel Seeblick Konstanz (3 Contacts - HOT LEAD!)
-- ============================================================================
(
  gen_random_uuid(),
  90003,
  'Julia',
  'Becker',
  'F&B Managerin',
  'becker@hotel-seeblick.example',
  '+49 7531 345601',
  '+49 160 7890101',
  TRUE, -- is_primary
  TRUE,
  'EXECUTIVE',
  95, -- ADVOCATE warmth!
  'CHAMPION! Kämpft intern für uns. Extrem dringlich - Saison startet in 2 Wochen. Selgros fiel komplett aus!',
  NOW() - INTERVAL '30 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '3 days'
),
(
  gen_random_uuid(),
  90003,
  'Stefan',
  'Huber',
  'Küchenchef',
  'huber@hotel-seeblick.example',
  NULL,
  '+49 175 2345601',
  FALSE,
  FALSE,
  'INFLUENCER',
  70,
  'Hochmotiviert, aber gestresst (hat gekündigt wegen Überlastung). Braucht SOFORT Lösung.',
  NOW() - INTERVAL '28 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '5 days'
),
(
  gen_random_uuid(),
  90003,
  'Michael',
  'Zimmermann',
  'Hoteldirektor',
  'zimmermann@hotel-seeblick.example',
  '+49 7531 345600',
  '+49 160 4567801',
  FALSE,
  TRUE,
  'EXECUTIVE',
  80,
  'Unterstützt Julia aktiv. Budget freigegeben. Will schnelle Entscheidung.',
  NOW() - INTERVAL '20 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '7 days'
),

-- ============================================================================
-- LEAD 90004: Kantine TechCorp München (2 Contacts - LOST)
-- ============================================================================
(
  gen_random_uuid(),
  90004,
  'Peter',
  'Fischer',
  'Kantinenleiter',
  'fischer@techcorp-kantine.example',
  '+49 89 4567801',
  '+49 160 6789001',
  TRUE, -- is_primary
  FALSE,
  'INFLUENCER',
  50,
  'War interessiert, aber Procurement blockte. Entschied sich für Compass Group (größerer Name).',
  NOW() - INTERVAL '90 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '7 days'
),
(
  gen_random_uuid(),
  90004,
  'Andrea',
  'Neumann',
  'Procurement Manager',
  'neumann@techcorp.example',
  '+49 89 4567800',
  NULL,
  FALSE,
  TRUE,
  'EXECUTIVE',
  20,
  'Blockierte Gespräch. Wollte nur etablierte Player (Compass, Aramark). Zu risikoavers für Startup.',
  NOW() - INTERVAL '80 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '80 days'
),

-- ============================================================================
-- LEAD 90005: Seniorenheim Alpenblick (2 Contacts - GRACE PERIOD)
-- ============================================================================
(
  gen_random_uuid(),
  90005,
  'Monika',
  'Wagner',
  'Heimleiterin',
  'wagner@alpenblick.example',
  '+49 8041 567801',
  '+49 170 1234501',
  TRUE, -- is_primary
  TRUE,
  'EXECUTIVE',
  25, -- COLD warmth!
  'Schwer erreichbar. Hat nach erstem Gespräch nie wieder geantwortet. GRACE PERIOD läuft!',
  NOW() - INTERVAL '68 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '68 days'
),
(
  gen_random_uuid(),
  90005,
  'Klaus',
  'Berger',
  'Küchenleiter',
  'berger@alpenblick.example',
  NULL,
  '+49 175 8901201',
  FALSE,
  FALSE,
  'Informant',
  30,
  'Nett am Telefon, aber hat keinen Entscheidungsspielraum. Verweist auf Frau Wagner.',
  NOW() - INTERVAL '68 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '68 days'
),

-- ============================================================================
-- LEAD 90006: Bäckerei Schmidt Nürnberg (PreClaim - KEINE CONTACTS!)
-- ============================================================================
-- PreClaim Stage = VORMERKUNG → 0 Contacts (nur Firma registriert)

-- ============================================================================
-- LEAD 90007: Universitätsmensa Heidelberg (3 Contacts - HOT LEAD #2)
-- ============================================================================
(
  gen_random_uuid(),
  90007,
  'Anna',
  'Lehmann',
  'Mensachefin',
  'lehmann@uni-heidelberg.example',
  '+49 6221 678902',
  '+49 160 2345601',
  TRUE, -- is_primary
  TRUE,
  'EXECUTIVE',
  80,
  'Sehr offen! Aramark-Vertrag nicht verlängert. Sucht dringend Alternative. Semester-Start April!',
  NOW() - INTERVAL '25 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '10 days'
),
(
  gen_random_uuid(),
  90007,
  'Robert',
  'Kraus',
  'Mensaleiter (Operativ)',
  'kraus@uni-heidelberg.example',
  NULL,
  '+49 171 4567801',
  FALSE,
  FALSE,
  'INFLUENCER',
  75,
  'CHAMPION! Brachte uns direkt zur Chefin. Kämpft intern für uns. Pragmatisch, will schnelle Lösung.',
  NOW() - INTERVAL '25 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '12 days'
),
(
  gen_random_uuid(),
  90007,
  'Prof. Dr. Martin',
  'Schneider',
  'Studierendenwerk Vorstand',
  'schneider@studierendenwerk-heidelberg.example',
  '+49 6221 678900',
  NULL,
  FALSE,
  TRUE,
  'EXECUTIVE',
  60,
  'Finale Genehmigung nötig. Neutral, aber vertraut Anna Lehmann. Budget ist da.',
  NOW() - INTERVAL '18 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '18 days'
),

-- ============================================================================
-- LEAD 90008: Catering Eventgarten Frankfurt (3 Contacts - QUALIFIED)
-- ============================================================================
(
  gen_random_uuid(),
  90008,
  'Claudia',
  'Richter',
  'Geschäftsführerin',
  'richter@eventgarten.example',
  '+49 69 7890101',
  '+49 160 8901201',
  TRUE, -- is_primary
  TRUE,
  'EXECUTIVE',
  90, -- TRUSTED warmth!
  'Exzellente Beziehung! Teilt interne Zahlen. Vertraut uns. Event-Saison startet - will SOFORT wechseln!',
  NOW() - INTERVAL '15 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '2 days'
),
(
  gen_random_uuid(),
  90008,
  'Daniel',
  'Hoffmann',
  'Operations Manager',
  'hoffmann@eventgarten.example',
  NULL,
  '+49 175 6789001',
  FALSE,
  FALSE,
  'INFLUENCER',
  75,
  'Pragmatisch, will sofortige Lösung. Beklagt Lieferanten-Service massiv.',
  NOW() - INTERVAL '14 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '5 days'
),
(
  gen_random_uuid(),
  90008,
  'Sandra',
  'Meier',
  'Einkaufsleiterin Filiale 2',
  'meier@eventgarten.example',
  NULL,
  '+49 170 3456701',
  FALSE,
  FALSE,
  'Informant',
  60,
  'Freundlich, aber wenig Einfluss. Bestätigt Probleme mit aktuellem Lieferanten.',
  NOW() - INTERVAL '10 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '10 days'
),

-- ============================================================================
-- LEAD 90009: Klinikum Charité Berlin (2 Contacts - BLOCKED)
-- ============================================================================
(
  gen_random_uuid(),
  90009,
  'Markus',
  'Stein',
  'Küchenleiter',
  'stein@charite.example',
  '+49 30 9876501',
  '+49 171 2345601',
  TRUE, -- is_primary
  FALSE,
  'INFLUENCER',
  55,
  'Interessiert, aber kein Entscheider. Procurement blockt - will nur etablierte Player (Apetito).',
  NOW() - INTERVAL '45 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '15 days'
),
(
  gen_random_uuid(),
  90009,
  'Frau Dr. Martina',
  'Koch',
  'Procurement Director',
  'koch@charite.example',
  '+49 30 9876500',
  NULL,
  FALSE,
  TRUE,
  'Blocker',
  15, -- BLOCKED!
  'Blockiert aktiv! Will nur Apetito (etablierter Partner). Risikoavers. Schwer zu erreichen.',
  NOW() - INTERVAL '40 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '40 days'
),

-- ============================================================================
-- LEAD 90010: Restaurant Dolce Vita Hamburg (2 Contacts)
-- ============================================================================
(
  gen_random_uuid(),
  90010,
  'Giuseppe',
  'Rossi',
  'Inhaber',
  'rossi@dolcevita-hamburg.example',
  '+49 40 3456701',
  '+49 160 5678901',
  TRUE, -- is_primary
  TRUE,
  'EXECUTIVE',
  60,
  'Freundlich, aber nicht dringlich. Beschwert sich über Metro-Service, aber nicht handlungsbereit.',
  NOW() - INTERVAL '12 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '8 days'
),
(
  gen_random_uuid(),
  90010,
  'Maria',
  'Lombardi',
  'Serviceleiterin',
  'lombardi@dolcevita-hamburg.example',
  NULL,
  '+49 175 1234501',
  FALSE,
  FALSE,
  'Informant',
  50,
  'Nett, aber wenig Einfluss. Bestätigt Service-Probleme mit Metro.',
  NOW() - INTERVAL '10 days',
  'DEV-SEED',
  'DEV-SEED',
  NOW() - INTERVAL '10 days'
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- STATUS CHECK: CONTACTS
-- ============================================================================
DO $$
DECLARE
  contact_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO contact_count FROM lead_contacts WHERE lead_id BETWEEN 90001 AND 90010;
  RAISE NOTICE 'DEV-SEED Lead Contacts inserted: % of 21', contact_count;
END $$;

-- ============================================================================
-- BLOCK 3: LEAD ACTIVITIES (21 Activities - 2 per Lead, inkl. CREATED)
-- ============================================================================

INSERT INTO lead_activities (
  lead_id,
  user_id,
  activity_type,
  activity_date,
  description,
  is_meaningful_contact,
  counts_as_progress
) VALUES

-- ============================================================================
-- LEAD 90001: Café Amadeus Berlin (Fresh Lead - 5 days)
-- ============================================================================
(
  90001,
  'admin',
  'CREATED',
  NOW() - INTERVAL '5 days',
  'Lead erfasst via Empfehlung (Partner-Netzwerk Berlin 2025)',
  FALSE,
  FALSE
),
(
  90001,
  'admin',
  'CALL',
  NOW() - INTERVAL '4 days',
  'Erstkontakt per Telefon mit Max Müller. Sehr interessiert! Koch kündigt nächsten Monat, sucht dringend neuen Lieferanten. Metro zu teuer und unzuverlässig.',
  TRUE, -- is_meaningful_contact!
  FALSE
),

-- ============================================================================
-- LEAD 90002: Gasthaus zur Linde Stuttgart (Pre-Reminder - 58 days)
-- ============================================================================
(
  90002,
  'admin',
  'CREATED',
  NOW() - INTERVAL '58 days',
  'Lead erfasst via Messe (INTERGASTRA 2025)',
  FALSE,
  FALSE
),
(
  90002,
  'admin',
  'MEETING',
  NOW() - INTERVAL '58 days',
  'Gespräch auf INTERGASTRA-Stand mit Thomas Weber. Enthusiastisch, bat um Follow-up. Qualitätsprobleme mit Transgourmet.',
  TRUE,
  TRUE -- counts_as_progress!
),

-- ============================================================================
-- LEAD 90003: Hotel Seeblick Konstanz (HOT LEAD #1 - Score 59)
-- ============================================================================
(
  90003,
  'admin',
  'CREATED',
  NOW() - INTERVAL '30 days',
  'Lead erfasst via Web-Formular (Google Ads Premium Hotels Q1 2025)',
  FALSE,
  FALSE
),
(
  90003,
  'admin',
  'QUALIFIED_CALL',
  NOW() - INTERVAL '28 days',
  'Qualifizierungs-Call mit Julia Becker (F&B Manager). KRITISCHE LAGE! Selgros fiel KOMPLETT aus - keine Lieferungen mehr. Koch hat gekündigt. Saison startet in 2 Wochen! Budget freigegeben. SOFORT-Bedarf!',
  TRUE,
  TRUE -- counts_as_progress!
),
(
  90003,
  'admin',
  'ROI_PRESENTATION',
  NOW() - INTERVAL '3 days',
  'ROI-Präsentation für Hoteldirektor Michael Zimmermann. Sehr positiv! Budget genehmigt. Nächster Schritt: Vertragsverhandlung. Julia kämpft intern für uns - echte CHAMPION!',
  TRUE,
  TRUE -- counts_as_progress!
),

-- ============================================================================
-- LEAD 90004: Kantine TechCorp München (LOST - Competitor Won)
-- ============================================================================
(
  90004,
  'admin',
  'CREATED',
  NOW() - INTERVAL '90 days',
  'Lead erfasst via Partner (IT-Catering-Partner Q4 2024)',
  FALSE,
  FALSE
),
(
  90004,
  'admin',
  'NOTE',
  NOW() - INTERVAL '7 days',
  'LOST: Entscheidung für Compass Group. Procurement (Andrea Neumann) blockierte - wollte nur etablierte Player. Zu risikoavers für Startup.',
  FALSE,
  FALSE
),

-- ============================================================================
-- LEAD 90005: Seniorenheim Alpenblick (GRACE PERIOD - KRITISCH!)
-- ============================================================================
(
  90005,
  'admin',
  'CREATED',
  NOW() - INTERVAL '68 days',
  'Lead erfasst via Telefon (Kaltakquise)',
  FALSE,
  FALSE
),
(
  90005,
  'admin',
  'CALL',
  NOW() - INTERVAL '68 days',
  'Erstkontakt mit Monika Wagner (Heimleiterin). Nett, aber schwer erreichbar. Hat um schriftliches Angebot gebeten. Seither keine Reaktion!',
  TRUE,
  FALSE
),

-- ============================================================================
-- LEAD 90006: Bäckerei Schmidt Nürnberg (PreClaim - VORMERKUNG)
-- ============================================================================
(
  90006,
  'admin',
  'CREATED',
  NOW() - INTERVAL '3 days',
  'Lead erfasst via Web-Formular (Google Ads Bäckereien Bayern). VORMERKUNG - noch kein Erstkontakt dokumentiert!',
  FALSE,
  FALSE
),
(
  90006,
  'admin',
  'NOTE',
  NOW() - INTERVAL '2 days',
  'Web-Formular ausgefüllt, aber bisher KEINE Antwort auf Follow-up E-Mails. PreClaim-Window läuft noch 4 Tage!',
  FALSE,
  FALSE
),

-- ============================================================================
-- LEAD 90007: Universitätsmensa Heidelberg (HOT LEAD #2 - Score 57)
-- ============================================================================
(
  90007,
  'admin',
  'CREATED',
  NOW() - INTERVAL '25 days',
  'Lead erfasst via Messe (Bildungsmesse Karlsruhe 2025)',
  FALSE,
  FALSE
),
(
  90007,
  'admin',
  'MEETING',
  NOW() - INTERVAL '25 days',
  'Gespräch mit Robert Kraus (Mensaleiter). CHAMPION! Aramark-Vertrag NICHT verlängert. Brachte uns direkt zu Anna Lehmann (Mensachefin). Semester-Start April - dringlicher Bedarf!',
  TRUE,
  TRUE -- counts_as_progress!
),

-- ============================================================================
-- LEAD 90008: Catering Eventgarten Frankfurt (High-Priority, Score 56)
-- ============================================================================
(
  90008,
  'admin',
  'CREATED',
  NOW() - INTERVAL '15 days',
  'Lead erfasst via Empfehlung (Event-Netzwerk Hessen)',
  FALSE,
  FALSE
),
(
  90008,
  'admin',
  'QUALIFIED_CALL',
  NOW() - INTERVAL '14 days',
  'Qualifizierungs-Call mit Claudia Richter (GF). Exzellente Beziehung! Teilt interne Zahlen. Event-Saison startet - will SOFORT wechseln. Bisheriger Lieferant liefert zu spät + schlechter Service.',
  TRUE,
  TRUE -- counts_as_progress!
),

-- ============================================================================
-- LEAD 90009: Klinikum Charité Berlin (High-Priority, Score 50)
-- ============================================================================
(
  90009,
  'admin',
  'CREATED',
  NOW() - INTERVAL '45 days',
  'Lead erfasst via Partner (Healthcare Partner Network Q1 2025)',
  FALSE,
  FALSE
),
(
  90009,
  'admin',
  'EMAIL',
  NOW() - INTERVAL '15 days',
  'E-Mail-Austausch mit Markus Stein (Küchenleiter). Interessiert, aber Procurement (Dr. Martina Koch) blockt - will nur Apetito. Sehr frustrierend!',
  TRUE,
  FALSE
),

-- ============================================================================
-- LEAD 90010: Restaurant Dolce Vita Hamburg (Low-Priority, Score 23)
-- ============================================================================
(
  90010,
  'admin',
  'CREATED',
  NOW() - INTERVAL '12 days',
  'Lead erfasst via Telefon (Kaltakquise)',
  FALSE,
  FALSE
),
(
  90010,
  'admin',
  'CALL',
  NOW() - INTERVAL '12 days',
  'Erstkontakt mit Giuseppe Rossi (Inhaber). Freundlich, aber nicht dringlich. Beschwert sich über Metro-Service, aber scheint nicht handlungsbereit.',
  TRUE,
  FALSE
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- STATUS CHECK: ACTIVITIES
-- ============================================================================
DO $$
DECLARE
  activity_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO activity_count FROM lead_activities WHERE lead_id BETWEEN 90001 AND 90010;
  RAISE NOTICE 'DEV-SEED Lead Activities inserted: % of 21', activity_count;
END $$;

-- ============================================================================
-- STATUS CHECK: CONTACTS
-- ============================================================================
DO $$
DECLARE
  contact_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO contact_count FROM lead_contacts WHERE lead_id BETWEEN 90001 AND 90010;
  RAISE NOTICE 'DEV-SEED Lead Contacts inserted: % of 21', contact_count;
END $$;

-- ============================================================================
-- STATUS CHECK: LEADS
-- ============================================================================
DO $$
DECLARE
  lead_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO lead_count FROM leads WHERE id BETWEEN 90001 AND 90010;
  RAISE NOTICE 'DEV-SEED Leads inserted: % of 10', lead_count;
END $$;
