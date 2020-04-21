@ThreadAndWaitlistRepeat
Feature: Test Threading and Waitlist Repeat

  @ThreadingAndWaitlistRepeatScenario
  Scenario: Test multithreaded events processing, waitlist resend and waitlist TTL.
  	Given that eiffel events are sent
	Then waitlist should not be empty
	And no event is aggregated
	And event-to-object-map is manipulated to include the sent events
	And when waitlist has resent events they should have been deleted        
	And after the time to live has ended, the waitlist should be empty
