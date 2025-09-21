Feature: DSGVO Compliance
  Scenario: Queue export and delete requests
    Given a subject with id "00000000-0000-0000-0000-000000000001"
    When I queue an export request
    And I queue a delete request
    Then both requests appear in the DSAR queue
