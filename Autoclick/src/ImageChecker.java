import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageChecker extends Thread{
	private Commands commands;
	private int checkInterval = 300;
	
	public ImageChecker(Commands commands){
		this.commands = commands;
	}
	
	public void run(){
		while (commands.imageTableData != null && Main.running && !commands.newThreadStarted){
			for (int i=0;i<commands.imageTableData.length;i++){
				String CurrcommandSet = (String) Main.commandsList.getSelectedItem();
				String imageAction = commands.imageTableData[i][Main.imageColAction];
				if (!CurrcommandSet.equals(imageAction.substring(6,imageAction.length()-1))){
					try {
						String imageName = commands.imageTableData[i][Main.imageColName];
						String imageCoords = commands.imageTableData[i][Main.imageColCoords];
						BufferedImage readImage = ImageIO.read(new File(Main.saveDirectory + "\\" + imageName + ".png"));
						
						String pt1 = imageCoords.substring(0, imageCoords.indexOf('-'));
						String pt2 = imageCoords.substring(imageCoords.indexOf('-')+1);
						int imageCoordsX = Integer.valueOf(pt1.substring(0, pt1.indexOf(',')));
						int imageCoordsY = Integer.valueOf(pt1.substring(pt1.indexOf(',')+1));
						int imageCoordsX2 = Integer.valueOf(pt2.substring(0, pt2.indexOf(',')));
						int imageCoordsY2 = Integer.valueOf(pt2.substring(pt2.indexOf(',')+1));
						int x = Math.min(imageCoordsX,imageCoordsX2);
				        int y = Math.min(imageCoordsY,imageCoordsY2);
				        int w = Math.abs(imageCoordsX-imageCoordsX2);
				        int h = Math.abs(imageCoordsY-imageCoordsY2);
				        
				        BufferedImage prescreenImage = new Robot().createScreenCapture(new Rectangle(x,y,w,h));
				        ByteArrayOutputStream screenImageOut = new ByteArrayOutputStream();
				        ImageIO.write(prescreenImage, "png", screenImageOut);
				        byte[] data = screenImageOut.toByteArray();
				        ByteArrayInputStream screenImageIn = new ByteArrayInputStream(data);
						BufferedImage screenImage = ImageIO.read(screenImageIn);
				        boolean imagesSame = compareImage(readImage, screenImage);
				        if (imagesSame){
				        	boolean isSpecialCommand = false;
				        	if (imageAction.length() > 5){
				        		String lastItem = imageAction.substring(imageAction.length()-1);
								if (imageAction.substring(0,6).equals("[GoTo ") && lastItem.equals("]")){
									isSpecialCommand = true;
								}
							}
				        	if (isSpecialCommand){
				        		String runCommandSet = imageAction.substring(6, imageAction.length()-1);
								try {
									Object[][] loadedData = Main.LoadFile(Main.saveDirectory + "\\" + runCommandSet + ".txt");
									for (int i1=0;i1<loadedData.length;i1++){
										for (int j1=Main.colHold;j1<=Main.colRelease;j1++){
											loadedData[i1][j1] = Boolean.valueOf((String)loadedData[i1][j1]);
										}
									}
									Main.tableModel.setRowCount(0);
									for (int l=0;l<loadedData.length;l++){
										Main.tableModel.insertRow(l, loadedData[l]);
									}
									Main.currentCommands = runCommandSet + ".txt";
									Main.doingInternalAction = true;
									Main.commandsList.setSelectedItem(runCommandSet);
									Main.doingInternalAction = false;
									String[][] stringData = new String[loadedData.length][loadedData[0].length];
									for (int d1=0;d1<loadedData.length;d1++){
										for (int d2=0;d2<loadedData[d1].length;d2++){
											if (loadedData[d1][d2] == null){
												stringData[d1][d2] = "";
											}
											else {
												stringData[d1][d2] = String.valueOf(loadedData[d1][d2]);
											}
										}
									}
									Commands subCommandExecute = new Commands(stringData, commands.imageTableData, commands.runLength, false);
									commands.newThreadStarted = true;
									try {
										commands.lock.lock();
										commands.timing.signal();
									} finally{
										commands.lock.unlock();
										if (Countdown.timer != null){
											Countdown.timer.cancel();
										}
									}
									subCommandExecute.start();
								} catch (IOException e) {
									System.err.println("Code ImgChk1 - Error loading sub-commands.");
									e.printStackTrace();
								}
				        	}
				        	else {
				        		if (imageAction.equals("{End Program}")){
				        			Main.running = false;
				        			try {
				        				Commands.staticLock.lock();
				        				Commands.staticTiming.signalAll();
				        			} finally{
				        				Commands.staticLock.unlock();
				        				if (Countdown.timer != null){
				        					Countdown.timer.cancel();
				        				}
				        			}
				        			Main.frame.setState(Frame.NORMAL);
				        		}
				        		else if (imageAction.equals("{Restart}") && !commands.doingRestart){
				        			Commands subCommandExecute = new Commands(commands.tableData, commands.imageTableData, commands.runLength, true);
				        			if (Countdown.timer != null){
				    					Countdown.timer.cancel();
				    				}
				        			subCommandExecute.start();
									commands.newThreadStarted = true;
				        		}
				        	}
				        }
					} catch (IOException | AWTException e) {
						System.err.println("Code ImgChk2 - Error comparing screen image to file image.");
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				System.err.println("Code ImgChk3 - Error in image check interval.");
				e.printStackTrace();
			}
		}
	}
	
	// Taken from user Sandip Ganguli at https://stackoverflow.com/questions/8567905/how-to-compare-images-for-similarity-using-java
	// This API will compare two image file //
	// return true if both image files are equal else return false//
	public static boolean compareImage(BufferedImage imageA,BufferedImage  imageB) {        
		// take buffer data from botm image files //
		DataBuffer dbA = imageA.getData().getDataBuffer();
		int sizeA = dbA.getSize();                     
		DataBuffer dbB = imageB.getData().getDataBuffer();
		int sizeB = dbB.getSize();
		// compare data-buffer objects //
		if(sizeA == sizeB) {
			for(int i=0; i<sizeA; i++) { 
				if(dbA.getElem(i) != dbB.getElem(i)) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}
}
