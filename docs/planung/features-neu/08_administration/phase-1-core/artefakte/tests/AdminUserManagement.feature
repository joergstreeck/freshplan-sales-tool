Feature: Admin User Management
  As an admin I want to manage users and their territories to control access.

  Scenario: Create user and assign territory
    Given I am authenticated as "admin"
    When I create a user with email "rep@example.com" and roles "rep"
    And I replace claims with territory "DE" and org "ORG-1"
    Then the user can access only data in territory "DE"
