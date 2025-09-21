-- testing/pgbench/lead_check.sql
-- Simple function call workload for assert_edit_and_audit(p_lead,...)
-- Use: pgbench -n -f testing/pgbench/lead_check.sql -c 50 -T 60
SELECT assert_edit_and_audit(:lead_id, 'EDIT'::lead_action, 'perf-check');
