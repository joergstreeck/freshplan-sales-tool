Feature: Risk-Tiered Approvals
  Scenario: Time-delayed Tier1 with emergency override
    Given a policy change "truststore.update" requires Tier1
    When an admin submits with emergency override and justification "SMTP down"
    Then the approval status is "OVERRIDDEN"
