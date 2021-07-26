
public class Timer extends Thread {
	private long duration;
	private WaitingObject waitingObject;
	public Timer(long dur, WaitingObject waitingObject){
		duration = dur;
		this.waitingObject = waitingObject;
	}
	public void run(){
		try {
			long millis = 0;
			int nanos = (int) duration;
			if (duration >= 1000000){
				millis = Math.floorDiv(duration, 1000000);
				nanos = (int) (duration % 1000000);
			}
			Thread.sleep(millis, nanos);
			waitingObject.lock.lock();
			waitingObject.timing.signal();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println("Code Timer1 - Error in wait time.");
		} finally {
			waitingObject.lock.unlock();
		}
	}
}
