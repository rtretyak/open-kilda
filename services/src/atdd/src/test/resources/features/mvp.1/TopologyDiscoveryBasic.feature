@TOPO
Feature: Basic Topology Discovery

  Basic Topology Discovery involves the basics at small scale:
  - 100 switches or less
  - Simple configuration (links)
  - Simple discovery time (ie not too aggressive - X milliseconds per Switch?)

  @MVP1 @SMOKE
  Scenario Outline: Linear Network Discovery Smoke Test

  Verify topology discovery happens within acceptable time lengths.
  This is for a small numbe (3), useful in verifying that a really small network is discoverable.

    Given a clean controller
    And a random linear topology of <num> switches
    When the controller learns the topology
    Then the controller should converge within <discovery_time> milliseconds

    Examples:
      | num | discovery_time |
      |   3 |          60000 |


  @MVP1
  Scenario Outline: Linear Network Discovery Time

    Verify topology discovery happens within acceptable time lengths.
    Initial assumption is that discovery time is non-linear; e.g. logarithmic.

    Given a clean controller
    And a random linear topology of <num> switches
    When the controller learns the topology
    Then the controller should converge within <discovery_time> milliseconds

    Examples:
      | num | discovery_time |
      |  10 |          60000 |
      |  20 |          70000 |

  @MVP1
  Scenario Outline: Full-mesh Network Discovery Time

    Verify full mesh discovery time is acceptable.
    Depth of 3 / Fanout of 4 = 21, 40

    Given a clean controller
    And a random tree topology with depth of <depth> and fanout of <fanout>
    When the controller learns the topology
    Then the controller should converge within <discovery_time> milliseconds

    Examples:
      |  depth | fanout | discovery_time |
      |      3 |      4 |          80000 |

  @MVP1.2 @SCALE
  Scenario Outline: Full-mesh Network Discovery Time

    Verify full mesh discovery time is acceptable.
    Depth of 4 / Fanout of 5 = 156 switches, 310 links.


    Given a clean controller
    And a random tree topology with depth of <depth> and fanout of <fanout>
    When the controller learns the topology
    Then the controller should converge within <discovery_time> milliseconds

    Examples:
      |  depth | fanout | discovery_time  |
      |      4 |      5 |          360000 |

  @MVP1 @SEC
  Scenario: Ignore not signed LLDP packets

    Verify only LLDP packets with right sign approved

    Given a clean controller
    And a random linear topology of 2 switches
    And the controller learns the topology
    When send malformed lldp packet
    Then the topology is not changed

  @MPV1
  Scenario: Handle FL outage

    Verify correct work of topology discovery subsystem after loosing
    connection between event/wfm topology and FL

    It must not resend discovery events for ISL exist on "connection lost"
    time. It must send discovery events for new ISL - reported by FL after
    "connection" recovery.

    Given a clean controller
    And a random linear topology of 3 switches
    And the controller learns the topology

    Then make topology change - disable port de:ad:be:af:00:00:01-1
    When wait topology change - isl de:ad:be:af:00:00:01-1 ==> de:ad:be:af:00:00:02-1 is missing
    And wait topology change - isl de:ad:be:af:00:00:02-1 ==> de:ad:be:af:00:00:01-1 is missing
    And link between controller and kafka are lost
    And wait for FoodLight connection lost detected
    And 2 seconds passed

    Then record isl modify time de:ad:be:af:00:00:02-2 ==> de:ad:be:af:00:00:03-1
    And 2 seconds passed

    Then link between controller and kafka restored
    And 5 seconds passed

    Then recorded isl modify time de:ad:be:af:00:00:02-2 ==> de:ad:be:af:00:00:03-1 must match

    Then record isl modify time de:ad:be:af:00:00:02-2 ==> de:ad:be:af:00:00:03-1
    When make topology change - enable port de:ad:be:af:00:00:01-1
    Then wait topology change - isl de:ad:be:af:00:00:01-1 ==> de:ad:be:af:00:00:02-1 is available
    And wait topology change - isl de:ad:be:af:00:00:02-1 ==> de:ad:be:af:00:00:01-1 is available

    Then recorded isl modify time de:ad:be:af:00:00:02-2 ==> de:ad:be:af:00:00:03-1 must go forward
