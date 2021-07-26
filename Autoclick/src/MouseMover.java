import java.awt.MouseInfo;
import java.awt.Robot;

public class MouseMover extends WaitingObject {
	private int targetCoord;
	private boolean isX;
	private int time;
	private int maxTimes;
	private Robot screenWin;
	
	public MouseMover(int targetCoord, boolean isX, int time, int maxTimes, Robot screenWin){
		this.targetCoord = targetCoord;
		this.isX = isX;
		this.time = time;
		this.maxTimes = maxTimes;
		this.screenWin = screenWin;
		
	}
	
	@Override
	public void run(){
		long startTime = System.currentTimeMillis();
		int currentCoord = 0;
		if (isX)
			currentCoord = (int) MouseInfo.getPointerInfo().getLocation().getX();
		else
			currentCoord = (int) MouseInfo.getPointerInfo().getLocation().getY();
		int pixelDist = Math.abs(targetCoord - currentCoord);
		int timePerPixel = 0;
		if (time > 0)
			timePerPixel = (time*1000000) / pixelDist;
		while (System.currentTimeMillis()-startTime <= time && Main.running){
			if (isX)
				currentCoord = (int) MouseInfo.getPointerInfo().getLocation().getX();
			else
				currentCoord = (int) MouseInfo.getPointerInfo().getLocation().getY();
			pixelDist = Math.abs(targetCoord - currentCoord);
			boolean incrmnt = targetCoord > currentCoord;
			
			if (incrmnt){
				if (isX){
					moveMouse(currentCoord+1,(int) MouseInfo.getPointerInfo().getLocation().getY(),maxTimes,screenWin);
					System.out.println("X: " + (currentCoord+1) + "    Target: " + targetCoord + "    waitTime: " + time + "    timePerPixel: " + timePerPixel);
				}
				else {
					moveMouse((int) MouseInfo.getPointerInfo().getLocation().getX(),currentCoord+1,maxTimes,screenWin);
					System.out.println("Y: " + (currentCoord+1) + "    Target: " + targetCoord + "    waitTime: " + time + "    timePerPixel: " + timePerPixel);
				}
			}
			else if(pixelDist > 0) {
				if (isX){
					moveMouse(currentCoord-1,(int) MouseInfo.getPointerInfo().getLocation().getY(),maxTimes,screenWin);
					System.out.println("X: " + (currentCoord-1) + "    Target: " + targetCoord + "    waitTime: " + time + "    timePerPixel: " + timePerPixel);
				}
				else {
					moveMouse((int) MouseInfo.getPointerInfo().getLocation().getX(),currentCoord-1,maxTimes,screenWin);
					System.out.println("Y: " + (currentCoord-1) + "    Target: " + targetCoord + "    waitTime: " + time + "    timePerPixel: " + timePerPixel);
				}
			}
			
			Timer pixelMoveTimer = new Timer(timePerPixel, this);
			pixelMoveTimer.start();
			try {
				lock.lock();
				timing.await();
			} catch (InterruptedException e) {
				System.err.println("Code MseMvr1 - Error in wait time.");
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
	}
	
	public static void moveMouse(int x, int y, int maxTimes, Robot screenWin) {
	    for(int count = 0;(MouseInfo.getPointerInfo().getLocation().getX() != x || 
	            MouseInfo.getPointerInfo().getLocation().getY() != y) &&
	            count < maxTimes; count++) {
	        screenWin.mouseMove(x, y);
	    }
	}
}
