================================================================================
OpenAPI Event Repository Mock - Implementation Summary
================================================================================

OVERVIEW
--------
An OpenAPI-based mock Event Repository (ER) service for eiffel-intelligence,
enabling integration testing of ERQueryService against a real HTTP endpoint
instead of mocked responses.

================================================================================

MODULE STRUCTURE
----------------
openapi/
├── pom.xml
├── src/main/java/com/ericsson/ei/erservice/
│   ├── OpenApiErApplication.java      - Spring Boot entry point
│   ├── ErApiDelegateImpl.java         - Implements EventsApiDelegate & SearchApiDelegate
│   ├── ErSecurityConfig.java          - Permits all requests (disables auth)
│   └── Jackson2WebMvcConfig.java      - Registers Jackson 2.x message converter
├── src/main/resources/
│   └── openapi-spec.yaml              - OpenAPI 3.0.1 spec for ER API
└── src/test/resources/eventrepository/events/
    ├── ArtifactCreated-1.json
    ├── ArtifactCreated-2.json
    ├── CompositionDefined-1.json
    ├── CompositionDefined-2.json
    └── SourceChangeCreated-1.json

================================================================================

TECH STACK
------------------
- Spring Boot 4.0.4 (same as eiffel-intelligence)
- openapi-generator-maven-plugin 7.12.0 (Jakarta EE compatible)
- Jackson 2.x for HTTP message conversion (excluded Jackson 3.x from starter)
- useTags=true with separate tags ("Events", "Search") to generate
  EventsApiDelegate and SearchApiDelegate
- delegatePattern=true for clean separation of generated and hand-written code

================================================================================

OPENAPI SPEC ENDPOINTS
-----------------------
1. GET /events          - List/filter events by query parameters
2. GET /events/{id}     - Get single event by ID
3. POST /search/{id}    - Upstream/downstream link traversal
   Query params: limit, levels, tree, shallow
   Body: {"dlt": ["ALL"], "ult": ["ALL"]}
   Response: {"upstreamLinkObjects": [...], "downstreamLinkObjects": [...]}

================================================================================

IMPLEMENTATION DETAILS
----------------------
ErApiDelegateImpl implements both generated delegate interfaces:

EventsApiDelegate:
  - getEventUsingGET(id): Looks up event by meta.id using JsonPath
  - getEventsUsingGET(pageSize, params): Filters events by arbitrary key-value
    params using JsonPath traversal

SearchApiDelegate:
  - searchUsingPOST(id, limit, levels, tree, shallow, searchParameters):
    * Parses dlt/ult link type arrays from request body
    * Performs recursive graph traversal on loaded events
    * Upstream: follows event's own links to targets
    * Downstream: finds events whose links point to this event
    * Tree mode: returns nested arrays [event, [subtree], ...]
    * Flat mode: returns flat list of events
    * Respects maxLevels and maxEvents limits
    * Cycle detection via visited set

================================================================================

TEST CLASS
----------
File: src/test/java/com/ericsson/ei/erqueryservice/test/ERQueryServiceWithOpenAPITest.java

Based on existing tests:
  - ERQueryServiceTest (mocks HttpExecutor)
  - UpStreamEventHandlerTest (mocks ERQueryService)

Approach: Boots the OpenAPI ER service on port 8764 via @BeforeClass,
configures ERQueryService with ReflectionTestUtils, makes real HTTP calls.
No MongoDB or RabbitMQ required.

TEST CASES (5 total, all passing):
----------------------------------
1. testUpstreamSearchReturnsTree
   - Event: CompositionDefined-1 (has ELEMENT links to 2 ArtifactCreated events)
   - Verifies: 200 response, upstreamLinkObjects is array with size > 1

2. testDownstreamSearchReturnsTree
   - Event: ArtifactCreated-1 (referenced by CompositionDefined-1)
   - Verifies: 200 response, downstreamLinkObjects is present and is array

3. testUpAndDownStreamSearch
   - Event: CompositionDefined-1
   - Verifies: 200 response, both upstreamLinkObjects and downstreamLinkObjects present

4. testSearchWithLimitedLevels
   - Event: CompositionDefined-1, levels=1
   - Verifies: 200 response, upstreamLinkObjects present (depth limited)

5. testSearchForNonExistentEventReturns404
   - Event: "non-existent-id"
   - Verifies: 404 response

================================================================================

FILES MODIFIED IN MAIN PROJECT
------------------------------
- pom.xml: Added eiffel-intelligence-openapi-er as test-scoped dependency
- src/test/resources/eventrepository/events/: 5 event JSON files (same as openapi module)

================================================================================

BUILD & RUN
-----------
# Build openapi module first:
cd openapi && mvn clean install -DskipTests -s ../.mvn/project-settings.xml

# Run the test:
cd .. && mvn test -Dtest=ERQueryServiceWithOpenAPITest -s .mvn/project-settings.xml

================================================================================
