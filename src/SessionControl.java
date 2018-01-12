import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class Session{
	private final String sessionId;
	private final long sessionStart;
	private long sessionEnd;
	
	public Session(String id, long sessionStart, long sessionEnd){
		this.sessionId = id;
		this.sessionStart = sessionStart;
		this.sessionEnd = sessionEnd;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public long getSessionEnd() {
		return sessionEnd;
	}
	public long getSessionStart() {
		return sessionStart;
	}
	
	public void updateSessionEnd(long newSessionEnd) {
		sessionEnd = Math.max(sessionEnd, newSessionEnd);
	}
}


class Event {
	private EventName name;
	private long timeout;
	public Event(EventName name, long timeout){
		this.name = name;
		this.timeout = timeout;
	}
	public long getTimeout() {
		return timeout;
	}
	
	public EventName getName() {
		return name;
	}
}

enum EventName {
	CHECK_OPEN,
	CHECK_CLOSE,
	SWIPE,
	TOUCH
}

// @Singleton
public class SessionControl {
	
	private SessionControl(Timer timer) {
		this.timer = timer;
	}
	
	private static SessionControl controller;
	
	public static SessionControl provideController(Timer timer) {
		if (controller == null) {
			controller = new SessionControl(timer);
		}
		return controller;
	}
	
	// For testing only
	public static void resetSingleton() {
		controller = null;
	}
	
	private Session session = null;
	private final List<Session> listOfSession = new ArrayList<>();
	private boolean checkOpen = false;
	private final Timer timer;
	
	public boolean addEvent(Event event) {
		long timeStamp = timer.getCurrentTimeStamp();
		session = getCurrentSession();
		if (session == null || (session.getSessionEnd() < timeStamp && session.getSessionEnd() != -1)) {
			session = createSessionFromEvent(event, timeStamp);
			listOfSession.add(session);
			return true;
		} else {
			extentSessionWithEvent(event, timeStamp);
			return false;
		}
	}
	
	private void extentSessionWithEvent(Event event, long startTime) {
		EventName eventName = event.getName();
		if (eventName == EventName.CHECK_OPEN) {
			this.checkOpen = true;
		} else if (eventName == EventName.CHECK_CLOSE) {
			if (this.checkOpen == false) {
				System.out.println("CHECK_CLOSE when check open is disabled"); // replace this with log or exception depending on expected behavior
			}
			this.checkOpen = false;
			session.updateSessionEnd(startTime);
		} else if (eventName == EventName.SWIPE || eventName == EventName.TOUCH){
			session.updateSessionEnd(event.getTimeout() + startTime);
		} else {
			throw new RuntimeException("unknow event name");
		}
	}

	private Session createSessionFromEvent(Event event, long startTime) {
		String id = UUID.randomUUID().toString();
		long endTime = -1;
		EventName eventName = event.getName();
		if (eventName == EventName.CHECK_OPEN) {
			this.checkOpen = true;
		} else if (eventName == EventName.CHECK_CLOSE) {
		    throw new RuntimeException("Should not create new event of check close");
		} else if (eventName == EventName.SWIPE || eventName == EventName.TOUCH){
			endTime = startTime + event.getTimeout();
		} else {
			throw new RuntimeException("Unknow event name");
		}
		return new Session(id, startTime, endTime);
	}
	
	public Session getCurrentSession() {
		long currentTime = timer.getCurrentTimeStamp();
		if (session == null || (session.getSessionEnd() < currentTime && !this.checkOpen)) {
			return null;
		}
		return session;
	}
	
	public List<Session> getSessions() {
		return listOfSession;
	}
	
}
