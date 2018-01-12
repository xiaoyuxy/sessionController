
public class FakeTimer implements Timer{
	private int timeStamp = 0;
	@Override
	public long getCurrentTimeStamp() {
		return timeStamp;
	}

	public void setCurrentTimeStamp(int timeStamp) {
		assert(timeStamp >= this.timeStamp);
		this.timeStamp = timeStamp;
	}
}
