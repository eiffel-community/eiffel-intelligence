@ThreadAndWaitlistRepeat
Feature: Test Threading and Waitlist Repeat

  @ThreadingAndWaitlistRepeatScenario
  Scenario: Test multithreaded events processing, waitlist resend and waitlist TTL.
  	Given that eiffel events are sent  	
	Then waitlist should not be empty
	And no event is aggregated
	And the waitlist will try to resend the events at given time interval
	And correct amount of threads should be spawned
	And after the time to live has ended, the waitlist should be empty
