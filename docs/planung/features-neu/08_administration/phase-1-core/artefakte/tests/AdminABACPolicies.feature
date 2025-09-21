Feature: ABAC Policies
  Scenario Outline: Access is enforced by territories and org
    Given a user with territories <territories> and org <org>
    When the user requests resource in territory <reqTerritory> and org <reqOrg>
    Then the access is <result>

    Examples:
      | territories | org    | reqTerritory | reqOrg  | result  |
      | DE,AT       | ORG-1  | DE           | ORG-1   | allowed |
      | DE,AT       | ORG-1  | CH           | ORG-1   | denied  |
      | DE          | ORG-2  | DE           | ORG-1   | denied  |
