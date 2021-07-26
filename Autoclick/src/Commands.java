import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class Commands extends WaitingObject {
	public String[][] tableData;
	public String[][] imageTableData;
	public static int clickLengthMin = 20;
	public static int clickLengthMax = 200;
	private long runCounter = 0;
	public Integer runLength;
	public boolean newThreadStarted = false;
	public boolean doingRestart = false; 
	
	public Commands(String[][] tblData, String[][] imageTblData, Integer runlength, boolean restart) {
		tableData = tblData;
		imageTableData = imageTblData;
		runLength = runlength;
		doingRestart = restart;
	}
	
	@Override
	public void run(){
		while (Main.running && !newThreadStarted){
			ImageChecker imageChecker = new ImageChecker(this);
			imageChecker.start();
			for (int i=0;i<tableData.length;i++){
				
				boolean isSpecialCommand = false;
				String commandCell = tableData[i][Main.colCommand];
				if (commandCell != null && !commandCell.isEmpty()){
					String lastItem = commandCell.substring(commandCell.length()-1);
					if (commandCell.length() > 5){
						if (commandCell.substring(0,6).equals("[GoTo ") && lastItem.equals("]")){
							isSpecialCommand = true;
						}
					}
				}
				
				if (!isSpecialCommand){
					Main.table.setRowSelectionInterval(i,i);
				}
				if (Main.running && !newThreadStarted){
					try {
						Robot robot = new Robot();
						Timer timer;
						double time;
						int clickLength = ThreadLocalRandom.current().nextInt(clickLengthMin, clickLengthMax+1);
						if (tableData[i][Main.colWaitTime] != null && !tableData[i][Main.colWaitTime].isEmpty()){
							if (tableData[i][Main.colWaitTime].contains("-")){
								double time1 = Double.valueOf(tableData[i][Main.colWaitTime].substring(0, tableData[i][Main.colWaitTime].indexOf('-')));
								double time2 = Double.valueOf(tableData[i][Main.colWaitTime].substring(tableData[i][Main.colWaitTime].indexOf('-')+1));
								
								time = ThreadLocalRandom.current().nextDouble(Math.min(time1, time2), Math.max(time2, time1));
							}
							else {
								time = Double.valueOf(tableData[i][Main.colWaitTime]);
							}
						}
						else {
							time = 0;
						}
						int waitTime = (int) (time * 1000);
						
						if (tableData[i][Main.colXY] != null && !tableData[i][Main.colXY].isEmpty() &&
								(i == 0 && runCounter == 0)){
							int[] XY = getMouseCoords(i);
							MouseMover.moveMouse(XY[0], XY[1], 10, robot);
						}
						
						
						if (Main.running && !newThreadStarted){
							switch (tableData[i][Main.colCommand]) {
							case ("{No Action}"):
							case (""):
								break;
							case ("{m1}"):
								MousePressAction(robot, InputEvent.BUTTON1_DOWN_MASK, i, clickLength);
								break;
							case ("{Middle Mouse Btn}"):
								MousePressAction(robot, InputEvent.BUTTON2_DOWN_MASK, i, clickLength);
								break;
							case ("{m2}"):
								MousePressAction(robot, InputEvent.BUTTON3_DOWN_MASK, i, clickLength);
								break;
							case ("{Enter}"):
								KeyPressAction(robot, KeyEvent.VK_ENTER, i, clickLength);
								break;
							case ("{Backspace}"):
								KeyPressAction(robot, KeyEvent.VK_BACK_SPACE, i, clickLength);
								break;
							case ("{Shift}"):
								KeyPressAction(robot, KeyEvent.VK_SHIFT, i, clickLength);
								break;
							case ("{Ctrl}"):
								KeyPressAction(robot, KeyEvent.VK_CONTROL, i, clickLength);
								break;
							case ("{Alt}"):
								KeyPressAction(robot, KeyEvent.VK_ALT, i, clickLength);
								break;
							case ("{Windows Key}"):
								KeyPressAction(robot, KeyEvent.VK_WINDOWS, i, clickLength);
								break;
							case ("{Tab}"):
								KeyPressAction(robot, KeyEvent.VK_TAB, i, clickLength);
								break;
							case ("{Esc}"):
								KeyPressAction(robot, KeyEvent.VK_ESCAPE, i, clickLength);
								break;
							case ("{F1}"):
								KeyPressAction(robot, KeyEvent.VK_F1, i, clickLength);
								break;
							case ("{F2}"):
								KeyPressAction(robot, KeyEvent.VK_F2, i, clickLength);
								break;
							case ("{F3}"):
								KeyPressAction(robot, KeyEvent.VK_F3, i, clickLength);
								break;
							case ("{F4}"):
								KeyPressAction(robot, KeyEvent.VK_F4, i, clickLength);
								break;
							case ("{F5}"):
								KeyPressAction(robot, KeyEvent.VK_F5, i, clickLength);
								break;
							case ("{F6}"):
								KeyPressAction(robot, KeyEvent.VK_F6, i, clickLength);
								break;
							case ("{F7}"):
								KeyPressAction(robot, KeyEvent.VK_F7, i, clickLength);
								break;
							case ("{F8}"):
								KeyPressAction(robot, KeyEvent.VK_F8, i, clickLength);
								break;
							case ("{F9}"):
								KeyPressAction(robot, KeyEvent.VK_F9, i, clickLength);
								break;
							case ("{F10}"):
								KeyPressAction(robot, KeyEvent.VK_F10, i, clickLength);
								break;
							case ("{F11}"):
								KeyPressAction(robot, KeyEvent.VK_F11, i, clickLength);
								break;
							case ("{F12}"):
								KeyPressAction(robot, KeyEvent.VK_F12, i, clickLength);
								break;
							case ("{Caps Lock}"):
								KeyPressAction(robot, KeyEvent.VK_CAPS_LOCK, i, clickLength);
								break;
							case ("{Num Lock}"):
								KeyPressAction(robot, KeyEvent.VK_NUM_LOCK, i, clickLength);
								break;
							case ("{Scroll Lock}"):
								KeyPressAction(robot, KeyEvent.VK_SCROLL_LOCK, i, clickLength);
								break;
							case ("{PrintScreen}"):
								KeyPressAction(robot, KeyEvent.VK_PRINTSCREEN, i, clickLength);
								break;
							case ("{Insert}"):
								KeyPressAction(robot, KeyEvent.VK_INSERT, i, clickLength);
								break;
							case ("{Delete}"):
								KeyPressAction(robot, KeyEvent.VK_DELETE, i, clickLength);
								break;
							case ("{Page Up}"):
								KeyPressAction(robot, KeyEvent.VK_PAGE_UP, i, clickLength);
								break;
							case ("{Page Down}"):
								KeyPressAction(robot, KeyEvent.VK_PAGE_DOWN, i, clickLength);
								break;
							case ("{Home}"):
								KeyPressAction(robot, KeyEvent.VK_HOME, i, clickLength);
								break;
							case ("{End}"):
								KeyPressAction(robot, KeyEvent.VK_END, i, clickLength);
								break;
							case ("{Up}"):
								KeyPressAction(robot, KeyEvent.VK_UP, i, clickLength);
								break;
							case ("{Down}"):
								KeyPressAction(robot, KeyEvent.VK_DOWN, i, clickLength);
								break;
							case ("{Left}"):
								KeyPressAction(robot, KeyEvent.VK_LEFT, i, clickLength);
								break;
							case ("{Right}"):
								KeyPressAction(robot, KeyEvent.VK_RIGHT, i, clickLength);
								break;
							case ("[End Program]"):
								Main.running = false;
								Main.frame.setState(Frame.NORMAL);
								break;
							default: 
								if (isSpecialCommand){
									String runCommandSet = commandCell.substring(6, commandCell.length()-1);
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
										Commands subCommandExecute = new Commands(stringData, imageTableData, runLength, false);
										subCommandExecute.start();
										newThreadStarted = true;
									} catch (IOException e) {
										System.err.println("Code Cmd1 - Error loading sub-commands.");
										e.printStackTrace();
									}
								}
								else {
									if (tableData[i][Main.colCommand].length() == 1){
										KeyPressAction(robot, getTypedKeyCode(tableData[i][Main.colCommand].charAt(0)), i, clickLength);
									}
									else {
										for (int j=0;j<tableData[i][Main.colCommand].length();j++){
											if (Main.running && !newThreadStarted){
												robot.keyPress(getTypedKeyCode(tableData[i][Main.colCommand].charAt(j)));
												robot.keyRelease(getTypedKeyCode(tableData[i][Main.colCommand].charAt(j)));
											}
											else {
												break;
											}
										}
									}
								}
								break;
							}
						}
						else {
							break;
						}

						if (newThreadStarted){
							break;
						}
						if (Main.running && !newThreadStarted){
							if (waitTime <= 0){
								if (i == tableData.length-1 && tableData[0][Main.colXY] != null && !tableData[0][Main.colXY].isEmpty()){
									int[] XY = getMouseCoords(0);
									MouseMover.moveMouse(XY[0], XY[1], 10, robot);
								}
								else if (tableData[i+1][Main.colXY] != null && !tableData[i+1][Main.colXY].isEmpty()) {
									int[] XY = getMouseCoords(i+1);
									MouseMover.moveMouse(XY[0], XY[1], 10, robot);
								}
							}
							else if (tableData[i][Main.colXY] != null && !tableData[i][Main.colXY].isEmpty()){
								int[] XY = {0,0};
								boolean cont = false;
								if (i == tableData.length-1 && (tableData[0][Main.colXY] != null && !tableData[0][Main.colXY].isEmpty())){
									XY = getMouseCoords(0);
									cont = true;
								}
								else if (i != tableData.length-1 && (tableData[i+1][Main.colXY] != null && !tableData[i+1][Main.colXY].isEmpty())) {
									XY = getMouseCoords(i+1);
									cont = true;
								}
								if (cont){
									MouseMover.moveMouse(XY[0], XY[1], 10, robot);
									/*MouseMover MoveX = new MouseMover(XY[0],true,waitTime,10,robot);
									MoveX.start();
									MouseMover MoveY = new MouseMover(XY[1],false,waitTime,10,robot);
									MoveY.start();*/
								}
							}
							if (waitTime > 0){
								new Countdown(i, waitTime);
								timer = new Timer(waitTime*1000000, this);
								timer.start();
								try {
									lock.lock();
									timing.await();
								} catch (InterruptedException e) {
									e.printStackTrace();
									System.err.println("Code Cmd2 - Error in wait time.");
								} finally {
									lock.unlock();
									if (!newThreadStarted){
										Countdown.timer.cancel();
										Main.tableModel.setValueAt("", i, Main.colTimeLeft);
									}
								}
							}
						}
						else {
							break;
						}
					} catch (AWTException e1) {
						e1.printStackTrace();
						System.err.println("Code Cmd3 - Unable to follow commands.");
					}
				}
				else {
					break;
				}
			}
			if (doingRestart){
				doingRestart = false;
			}
			runCounter += 1;
			if (runLength != null && runCounter >= runLength){
				Main.running = false;
				Main.frame.setState(Frame.NORMAL);
			}
		}
	}
	
	private int[] getMouseCoords(int tableRow){
		int X, Y;
		if (tableData[tableRow][Main.colXY].contains("-")){
			String pt1 = tableData[tableRow][Main.colXY].substring(0, tableData[tableRow][Main.colXY].indexOf('-'));
			String pt2 = tableData[tableRow][Main.colXY].substring(tableData[tableRow][Main.colXY].indexOf('-')+1);
			int pt1x = Integer.valueOf(pt1.substring(0, pt1.indexOf(',')));
			int pt1y = Integer.valueOf(pt1.substring(pt1.indexOf(',')+1));
			int pt2x = Integer.valueOf(pt2.substring(0, pt2.indexOf(',')));
			int pt2y = Integer.valueOf(pt2.substring(pt2.indexOf(',')+1));
			
			X = ThreadLocalRandom.current().nextInt(Math.min(pt1x, pt2x), Math.max(pt2x, pt1x) + 1);
			Y = ThreadLocalRandom.current().nextInt(Math.min(pt1y, pt2y), Math.max(pt2y, pt1y) + 1);
		}
		else {
			X = Integer.valueOf(tableData[tableRow][Main.colXY].substring(0, tableData[tableRow][Main.colXY].indexOf(',')));
			Y = Integer.valueOf(tableData[tableRow][Main.colXY].substring(tableData[tableRow][Main.colXY].indexOf(',')+1));
		}
		int[] XY = {X,Y};
		return XY;
	}
	
	private void MousePressAction(Robot robot, int actionid, int row, int clickLength){
		boolean hold = Boolean.valueOf(tableData[row][Main.colHold]);
		boolean release = Boolean.valueOf(tableData[row][Main.colRelease]);
		if (hold){
			robot.mousePress(actionid);
		}
		if (hold && release){
			Timer timer = new Timer(clickLength*1000000, this);
			timer.start();
			try {
				lock.lock();
				timing.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.err.println("Code Cmd4 - Error in wait time.");
			} finally {
				lock.unlock();
			}
		}
		if (release){
			robot.mouseRelease(actionid);
		}
	}
	
	private void KeyPressAction(Robot robot, int actionid, int row, int clickLength){
		boolean hold = Boolean.valueOf(tableData[row][Main.colHold]);
		boolean release = Boolean.valueOf(tableData[row][Main.colRelease]);
		if (hold){
			robot.keyPress(actionid);
		}
		if (hold && release){
			Timer timer = new Timer(clickLength*1000000, this);
			timer.start();
			try {
				lock.lock();
				timing.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.err.println("Code Cmd5 - Error in wait time.");
			} finally {
				lock.unlock();
			}
		}
		if (release){
			robot.keyRelease(actionid);
		}
	}
	
	private int getTypedKeyCode(char key){
		key = Character.toLowerCase(key);
		switch (key){
		case ('a'): return KeyEvent.VK_A;
		case ('b'): return KeyEvent.VK_B;
		case ('c'): return KeyEvent.VK_C;
		case ('d'): return KeyEvent.VK_D;
		case ('e'): return KeyEvent.VK_E;
		case ('f'): return KeyEvent.VK_F;
		case ('g'): return KeyEvent.VK_G;
		case ('h'): return KeyEvent.VK_H;
		case ('i'): return KeyEvent.VK_I;
		case ('j'): return KeyEvent.VK_J;
		case ('k'): return KeyEvent.VK_K;
		case ('l'): return KeyEvent.VK_L;
		case ('m'): return KeyEvent.VK_M;
		case ('n'): return KeyEvent.VK_N;
		case ('o'): return KeyEvent.VK_O;
		case ('p'): return KeyEvent.VK_P;
		case ('q'): return KeyEvent.VK_Q;
		case ('r'): return KeyEvent.VK_R;
		case ('s'): return KeyEvent.VK_S;
		case ('t'): return KeyEvent.VK_T;
		case ('u'): return KeyEvent.VK_U;
		case ('v'): return KeyEvent.VK_V;
		case ('w'): return KeyEvent.VK_W;
		case ('x'): return KeyEvent.VK_X;
		case ('y'): return KeyEvent.VK_Y;
		case ('z'): return KeyEvent.VK_Z;
		
		case ('0'): return KeyEvent.VK_0;
		case ('1'): return KeyEvent.VK_1;
		case ('2'): return KeyEvent.VK_2;
		case ('3'): return KeyEvent.VK_3;
		case ('4'): return KeyEvent.VK_4;
		case ('5'): return KeyEvent.VK_5;
		case ('6'): return KeyEvent.VK_6;
		case ('7'): return KeyEvent.VK_7;
		case ('8'): return KeyEvent.VK_8;
		case ('9'): return KeyEvent.VK_9;
		
		case (' '): return KeyEvent.VK_SPACE;
		case ('\\'): return KeyEvent.VK_BACK_SLASH;
		case ('<'): return KeyEvent.VK_LESS;
		case ('>'): return KeyEvent.VK_GREATER;
		case ('/'): return KeyEvent.VK_SLASH;
		case ('@'): return KeyEvent.VK_AT;
		case ('`'): return KeyEvent.VK_BACK_QUOTE;
		case ('{'): return KeyEvent.VK_BRACELEFT;
		case ('}'): return KeyEvent.VK_BRACERIGHT;
		case ('['): return KeyEvent.VK_OPEN_BRACKET;
		case (']'): return KeyEvent.VK_CLOSE_BRACKET;
		case (':'): return KeyEvent.VK_COLON;
		case (';'): return KeyEvent.VK_SEMICOLON;
		case (','): return KeyEvent.VK_COMMA;
		case ('.'): return KeyEvent.VK_PERIOD;
		case ('\''): return KeyEvent.VK_QUOTE;
		case ('#'): return KeyEvent.VK_NUMBER_SIGN;
		case ('~'): return KeyEvent.VK_DEAD_TILDE;
		case ('!'): return KeyEvent.VK_EXCLAMATION_MARK;
		case ('"'): return KeyEvent.VK_QUOTEDBL;
		case ('$'): return KeyEvent.VK_DOLLAR;
		case ('^'): return KeyEvent.VK_CIRCUMFLEX;
		case ('&'): return KeyEvent.VK_AMPERSAND;
		case ('*'): return KeyEvent.VK_ASTERISK;
		case ('('): return KeyEvent.VK_BRACELEFT;
		case (')'): return KeyEvent.VK_BRACERIGHT;
		case ('-'): return KeyEvent.VK_MINUS;
		case ('_'): return KeyEvent.VK_UNDERSCORE;
		case ('='): return KeyEvent.VK_EQUALS;
		case ('+'): return KeyEvent.VK_PLUS;
		}
		return KeyEvent.VK_UNDEFINED;
	}
	
}
