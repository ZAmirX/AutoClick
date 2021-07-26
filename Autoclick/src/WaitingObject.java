import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WaitingObject extends Thread {

	final Lock lock = new ReentrantLock();
	final Condition timing = lock.newCondition();
	static final Lock staticLock = new ReentrantLock();
	static final Condition staticTiming = staticLock.newCondition();
	
}
