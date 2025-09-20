Feature: Admin Audit System
  Scenario: Log and search audit events
    Given an admin action "outbox.pause" is performed
    Then an audit event is recorded with risk tier "TIER3"
    And I can search the audit log for "outbox.pause"
