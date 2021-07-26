import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class Countdown {
	static int interval;
	static Timer timer;
	public Countdown(int rowid, int waitTime){
		timer = new Timer();
		interval = waitTime;
		int delay = 0;
		int period = 10;
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				double newInterval = setInterval()/1000.0;
				DecimalFormat df = new DecimalFormat("#.##");
				df.setRoundingMode(RoundingMode.CEILING);
				Main.tableModel.setValueAt(df.format(newInterval), rowid, Main.colTimeLeft);
			}
		}, delay, period);
	}
	private static final int setInterval() {
	    if (interval <= 10){
	        timer.cancel();
	    }
	    return interval -= 10;
	}
}
