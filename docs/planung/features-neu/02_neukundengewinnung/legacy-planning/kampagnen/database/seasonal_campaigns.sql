-- Seasonal Campaign Timing (Leads in Window)
CREATE OR REPLACE VIEW leads_in_season_window AS
SELECT c.id as customer_id, fv1.value->>'season_start' as season_start, fv1.value->>'season_end' as season_end
FROM field_values fv1
JOIN customers c ON c.id=fv1.customer_id
WHERE fv1.field_key='seasonal_menu_cycle'
  AND (fv1.value->>'season_start')::date <= CURRENT_DATE
  AND (fv1.value->>'season_end')::date >= CURRENT_DATE;
