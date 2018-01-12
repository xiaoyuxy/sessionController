
public class RealTimer implements Timer {
	@Override
	public long getCurrentTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}
}
