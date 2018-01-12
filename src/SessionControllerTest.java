
public class SessionControllerTest {
	private FakeTimer timer = new FakeTimer();
	
	// should be replaced by test framework's assertion method
	static void assertThat(boolean condition, String msg) {
		if (!condition) {
			System.out.println("fail: " + msg);
		}
	}
	//Test Case 1: Test Multiple Touches event
	void testMultipleTouches() {
		int trialNum = 4;
		SessionControl.resetSingleton();
		int currentTime = 0;
		for (int i = 0; i < trialNum; i++) {
			
			SessionControl controller = SessionControl.provideController(timer);
			currentTime += 100;
			timer.setCurrentTimeStamp(currentTime);
			// Submit a touch event
			Event touchEvent = new Event(EventName.TOUCH, 200);
			boolean state = controller.addEvent(touchEvent);
			assertThat(state == true, "First use addevent method should return true when there no on-going session");
			// Assert a session is in progress
			Session session = controller.getCurrentSession();
			assertThat(session != null, "a session should be in progress");
			
			// Allow the touch to timeout
			currentTime += 201;
			timer.setCurrentTimeStamp(currentTime);
			session = controller.getCurrentSession();
			assertThat(session == null, "last session should timeout");
			assertThat(controller.getSessions().size() == i + 1, "session size should increase in each loop");
		}
		
		System.out.println("testMultipleTouches passes");
	}
	// Test case 2: Overlapping touches
	void testOverLappingTouches() {
		SessionControl.resetSingleton();
		SessionControl controller = SessionControl.provideController(timer);
		timer.setCurrentTimeStamp(100);
	    // submit first touch event
		Event touchEvent1 = new Event(EventName.TOUCH, 200);
		boolean state1 = controller.addEvent(touchEvent1);
		assertThat(state1 == true, "Should return true when create new session");
		timer.setCurrentTimeStamp(200);
		Event touchEvent2 = new Event(EventName.TOUCH, 200);
		boolean state2 = controller.addEvent(touchEvent2);
		assertThat(state2 == false, "a session should be in progress");
		Session session = controller.getCurrentSession();
		assertThat((session.getSessionStart() == 100 && session.getSessionEnd() == 400), "Session should start at 100, end at 400");
		timer.setCurrentTimeStamp(500);
		assertThat(controller.getSessions() == null, "Session should have expired");
		System.out.println("OverLapping touches test passes");
	}
	//Test case 3: Check Do not Expire
	void testCheckDoNotExpire() {
		SessionControl.resetSingleton();
		SessionControl controller = SessionControl.provideController(timer);
		//add checkopen event at time 100
		timer.setCurrentTimeStamp(100);
		Event checkOpenEvent = new Event(EventName.CHECK_OPEN, -1);
		boolean state = controller.addEvent(checkOpenEvent);
		assertThat(state == true, "First use addevent method should return true when there no on-going session");
		Session session = controller.getCurrentSession();
		
		//wait for a long time from 100 - 1000
		timer.setCurrentTimeStamp(1000);
		assertThat(session.getSessionStart() == 100 && session.getSessionEnd() == -1, "The session at this point matches the first session check");
		
		//add checkclose event at time 2000
		timer.setCurrentTimeStamp(2000);
		Event checkCloseEvent = new Event(EventName.CHECK_CLOSE, -1);
		controller.addEvent(checkCloseEvent);
		assertThat(session.getSessionStart() == 100 && session.getSessionEnd() == 2000, "session from 100 to 2000");
		
		System.out.println("Check session case passes");
	}
	// Test 4: Checkopen -> swipe -> Checkclose -> swipe end
	void testSwipeWithCheck(){
		SessionControl.resetSingleton();
		SessionControl controller = SessionControl.provideController(timer);
		
		//add checkopen event at time 100
		timer.setCurrentTimeStamp(100);
		Event checkOpenEvent = new Event(EventName.CHECK_OPEN, -1);
		boolean state1 = controller.addEvent(checkOpenEvent);
		assertThat(state1 == true, "First use addevent method should return true when there no on-going session");
		Session session = controller.getCurrentSession();
		
		// add swipeevent at time 200
		timer.setCurrentTimeStamp(200);
		Event swipeEvent = new Event(EventName.SWIPE, 150);
		boolean start2 = controller.addEvent(swipeEvent);
		assertThat(start2 == false && session.getSessionEnd() == 350, "extend the Sessionend time");
		
		//add checkclose event at time 300
		timer.setCurrentTimeStamp(300);
		Event checkCloseEvent = new Event(EventName.CHECK_CLOSE, -1); // add checkclose event at tme 200
		boolean start3 = controller.addEvent(checkCloseEvent);
		assertThat(start3 == false && session.getSessionEnd() == 350, "checkclose event will not effect the on-going event");
		
		System.out.println("Swipe session With Check event passes");
		
	}
	
	
	public static void main(String[] args) {
		SessionControllerTest test = new SessionControllerTest();
		test.testMultipleTouches();
		test.testOverLappingTouches();
		test.testCheckDoNotExpire();
		test.testSwipeWithCheck();
	}
}
