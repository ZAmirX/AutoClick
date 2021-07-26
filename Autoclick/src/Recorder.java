import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;

import org.jnativehook.keyboard.NativeKeyEvent;

public class Recorder {
	public static boolean recording = false;
	public static int currentX, currentY;
	public static long lastCommandEndTime;
	public static int sameCommandReleaseTimeout = 400;
	public static int typingTimeout = 400;
	public static int sameMouseMoveCommandPeriod = 800;
	public static long lastMouseMoveCommandTime;
	public static boolean commandStarted = false;
	public static boolean didSpecAction = false;
	public static boolean inTypedCommand;
	public static long lastTypedEndTime;
	public Recorder(){
		
	}
	
	public static void keyPressed(int keyCode){
		if (!getKeyText(keyCode).isEmpty()){
			int currentRow = Main.table.getRowCount()-1;
			boolean validKey = true;
			if (Main.table.getRowCount() > 0){
				for (int i=Main.table.getRowCount()-1;i>=0;i--){
					Object command = Main.table.getValueAt(i, Main.colCommand);
					boolean hold = (boolean) Main.table.getValueAt(i, Main.colHold);
					boolean release = (boolean) Main.table.getValueAt(i, Main.colRelease);
					if(command != null && command.equals(getKeyText(keyCode)) && release == true){
						break;
					}
					else if (command != null && command.equals(getKeyText(keyCode)) && hold == true && release == false){
						validKey = false;
						break;
					}
				}
			}
			Object currentCommand = null;
			boolean currentCommandReleased = false;
			if (currentRow >= 0){
				currentCommand = Main.table.getValueAt(currentRow, Main.colCommand);
				currentCommandReleased = (boolean) Main.table.getValueAt(currentRow, Main.colRelease);
			}
			if (!(getKeyText(keyCode).equals((String)currentCommand) && !currentCommandReleased)){
				if (inTypedCommand && isTypedCode(keyCode) && (System.currentTimeMillis() - lastTypedEndTime) <= typingTimeout){
					Main.table.setValueAt(true, currentRow, Main.colRelease);
					if (keyCode == NativeKeyEvent.VC_BACKSPACE){
						String newValue = ((String)currentCommand).substring(0,((String)currentCommand).length()-1);
						Main.table.setValueAt(newValue, currentRow, Main.colCommand);
					}
					else {
						Main.table.setValueAt((String)currentCommand + getKeyText(keyCode), currentRow, Main.colCommand);
					}
					lastTypedEndTime = System.currentTimeMillis();
					lastCommandEndTime = lastTypedEndTime;
				}
				else if (validKey){
					startCommand();
					currentRow = Main.table.getRowCount()-1;
					//System.out.println("Code: " + keyCode);
					//System.out.println("Text: " + getKeyText(keyCode));
					Main.table.setValueAt(getKeyText(keyCode), currentRow, Main.colCommand);
					Main.table.setValueAt(true, currentRow, Main.colHold);
					Main.table.setValueAt(false, currentRow, Main.colRelease);
					if (isTypedCode(keyCode) && keyCode != NativeKeyEvent.VC_BACKSPACE){
						inTypedCommand = true;
						lastTypedEndTime = System.currentTimeMillis();
					}
					else {
						inTypedCommand = false;
					}
					addCurrentXYToRow(currentRow);
					lastMouseMoveCommandTime = System.currentTimeMillis();
					lastCommandEndTime = System.currentTimeMillis();
					//Main.table.setValueAt(currentX + "," + currentY, currentRow, Main.colXY);
				}
			}
		}
	}
	public static void keyReleased(int keyCode){
		if (!getKeyText(keyCode).isEmpty() && Main.table.getRowCount() > 0){
			int currentRow = Main.table.getRowCount()-1;
			Object currentCommand = Main.table.getValueAt(currentRow, Main.colCommand);
			boolean validKey = false;
			for (int i=Main.table.getRowCount()-1;i>=0;i--){
				Object command = Main.table.getValueAt(i, Main.colCommand);
				boolean hold = (boolean) Main.table.getValueAt(i, Main.colHold);
				boolean release = (boolean) Main.table.getValueAt(i, Main.colRelease);
				if (command != null && command.equals(getKeyText(keyCode)) && release == true){
					break;
				}
				else if(command != null && command.equals(getKeyText(keyCode)) && hold == true && release == false){
					validKey = true;
					break;
				}
			}
			if (validKey){
				long timeLapsed = System.currentTimeMillis() - lastCommandEndTime;
				//System.out.println(getKeyText(keyCode));
				//System.out.println("Time lapsed: " + timeLapsed);
				if (currentCommand != null && ((String)currentCommand).equals(getKeyText(keyCode)) && timeLapsed <= sameCommandReleaseTimeout){
					Main.table.setValueAt(true, currentRow, Main.colRelease);
				}
				else {
					startCommand();
					currentRow = Main.table.getRowCount()-1;
					Main.table.setValueAt(getKeyText(keyCode), currentRow, Main.colCommand);
					Main.table.setValueAt(false, currentRow, Main.colHold);
					Main.table.setValueAt(true, currentRow, Main.colRelease);
					addCurrentXYToRow(currentRow);
					lastMouseMoveCommandTime = System.currentTimeMillis();
				}
				lastCommandEndTime = System.currentTimeMillis();
				//Main.table.setValueAt(currentX + "," + currentY, currentRow, Main.colXY);
				endCommand();
			}
		}
	}
	
	public static void mousePressed(int mouseCode){
		inTypedCommand = false;
		int currentRow = Main.table.getRowCount()-1;
		Object currentValue = null;
		boolean release = false;
		if (currentRow >= 0){
			currentValue = Main.table.getValueAt(currentRow, Main.colCommand);
			release = (boolean) Main.table.getValueAt(currentRow, Main.colRelease);
		}
		if (!(currentValue != null && currentValue.equals(getMouseText(mouseCode)) && !release)){
			startCommand();
			currentRow = Main.table.getRowCount()-1;
			//System.out.println("Code: " + keyCode);
			//System.out.println("Text: " + getKeyText(keyCode));
			Main.table.setValueAt(getMouseText(mouseCode), currentRow, Main.colCommand);
			Main.table.setValueAt(true, currentRow, Main.colHold);
			Main.table.setValueAt(false, currentRow, Main.colRelease);
			Main.table.setValueAt(currentX + "," + currentY, currentRow, Main.colXY);
			lastMouseMoveCommandTime = System.currentTimeMillis();
			lastCommandEndTime = lastMouseMoveCommandTime;
			//Main.table.setValueAt(currentX + "," + currentY, currentRow, Main.colXY);
		}
	}
	public static void mouseReleased(int mouseCode){
		inTypedCommand = false;
		if (!getMouseText(mouseCode).isEmpty() && Main.table.getRowCount() > 0){
			int currentRow = Main.table.getRowCount()-1;
			Object currentCommand = Main.table.getValueAt(currentRow, Main.colCommand);
			boolean validCode = false;
			for (int i=Main.table.getRowCount()-1;i>=0;i--){
				Object command = Main.table.getValueAt(i, Main.colCommand);
				boolean hold = (boolean) Main.table.getValueAt(i, Main.colHold);
				boolean release = (boolean) Main.table.getValueAt(i, Main.colRelease);
				if (command != null && command.equals(getMouseText(mouseCode)) && release == true){
					break;
				}
				else if(command != null && command.equals(getMouseText(mouseCode)) && hold == true && release == false){
					validCode = true;
					break;
				}
			}
			if (validCode){
				long timeLapsed = System.currentTimeMillis() - lastCommandEndTime;
				//System.out.println(getKeyText(keyCode));
				//System.out.println("Time lapsed: " + timeLapsed);
				if (currentCommand != null && ((String)currentCommand).equals(getMouseText(mouseCode)) && timeLapsed <= sameCommandReleaseTimeout){
					Main.table.setValueAt(true, currentRow, Main.colRelease);
				}
				else {
					startCommand();
					currentRow = Main.table.getRowCount()-1;
					Main.table.setValueAt(getMouseText(mouseCode), currentRow, Main.colCommand);
					Main.table.setValueAt(false, currentRow, Main.colHold);
					Main.table.setValueAt(true, currentRow, Main.colRelease);
					Point mousePoint = MouseInfo.getPointerInfo().getLocation();
					currentX = mousePoint.x;
					currentY = mousePoint.y;
					Main.table.setValueAt(currentX + "," + currentY, currentRow, Main.colXY);
					lastMouseMoveCommandTime = System.currentTimeMillis();
				}
				lastCommandEndTime = System.currentTimeMillis();
				//Main.table.setValueAt(currentX + "," + currentY, currentRow, Main.colXY);
				endCommand();
			}
		}
	}
	public static void mouseMoved(int X, int Y){
		//Mouse pressed but not released = startCommand();
		//long currentTime = System.currentTimeMillis();
		long timeDiff = System.currentTimeMillis() - lastMouseMoveCommandTime;
		if (timeDiff > sameMouseMoveCommandPeriod){
			startCommand();
			int currentRow = Main.table.getRowCount()-1;
			Main.table.setValueAt("{No Action}", currentRow, Main.colCommand);
			Main.table.setValueAt(currentX + "," + currentY, currentRow, Main.colXY);
			lastCommandEndTime = System.currentTimeMillis();
		}
		currentX = X;
		currentY = Y;
		lastMouseMoveCommandTime = System.currentTimeMillis();
	}
	
	public static void startRecording(){
		Main.tableModel.setRowCount(0);
		recording = true;
		Main.frame.setState(Frame.ICONIFIED);
		Point mousePoint = MouseInfo.getPointerInfo().getLocation();
		currentX = mousePoint.x;
		currentY = mousePoint.y;
		lastCommandEndTime = 0;
		lastMouseMoveCommandTime = System.currentTimeMillis();
	}
	
	public static void startCommand(){
		commandStarted = true;
		addWaitTimeToCmd();
		int rowid = Main.table.getRowCount();
		Main.tableModel.addRow(new Object[]{rowid, "", true, true, "", "", "", ""});
	}
	
	// Clean up and move on to the next command only when:
	//-Time out interval has passed since the beginning of the command
	//-Mouse released event
	//-Key released event
	public static void endCommand(){
		commandStarted = false;
	}
	
	public static void endRecording(){
		recording = false;
		for (int i=Main.table.getRowCount()-1;i>=0;i--){
			Object lastXY = Main.table.getValueAt(i, Main.colXY);
			if (lastXY != null && !((String)lastXY).isEmpty()){
				int lastX = Integer.valueOf(((String)lastXY).substring(0, ((String)lastXY).indexOf(',')));
				int lastY = Integer.valueOf(((String)lastXY).substring(((String)lastXY).indexOf(',')+1));
				if (lastX != currentX || lastY != currentY){
					startCommand();
					int currentRow = Main.table.getRowCount()-1;
					Main.table.setValueAt("{No Action}", currentRow, Main.colCommand);
					Main.table.setValueAt(currentX + "," + currentY, currentRow, Main.colXY);
					lastCommandEndTime = System.currentTimeMillis();
				}
				break;
			}
		}
		addWaitTimeToCmd();
		Main.frame.setState(Frame.NORMAL);
	}
	
	public static void addWaitTimeToCmd(){
		int currentRow = Main.table.getRowCount()-1;
		if (lastCommandEndTime != 0){
			long currentTime = System.currentTimeMillis();
			double timeSinceLastCommand = (currentTime-lastCommandEndTime)/1000.0;
			Main.table.setValueAt(timeSinceLastCommand, currentRow, Main.colWaitTime);
		}
	}
	
	public static void addCurrentXYToRow(int row){
		for (int i=Main.table.getRowCount()-1;i>=0;i--){
			Object lastXY = Main.table.getValueAt(i, Main.colXY);
			if (lastXY != null && !((String)lastXY).isEmpty()){
				int lastX = Integer.valueOf(((String)lastXY).substring(0, ((String)lastXY).indexOf(',')));
				int lastY = Integer.valueOf(((String)lastXY).substring(((String)lastXY).indexOf(',')+1));
				if (lastX != currentX || lastY != currentY){
					Main.table.setValueAt(currentX + "," + currentY, row, Main.colXY);
				}
				break;
			}
		}
	}
	
	public static String getKeyText(int keyCode){
		switch (keyCode){
		case NativeKeyEvent.VC_ENTER: return "{Enter}";
		case NativeKeyEvent.VC_BACKSPACE: return "{Backspace}";
		case NativeKeyEvent.VC_SHIFT: return "{Shift}";
		case NativeKeyEvent.VC_CONTROL: return "{Ctrl}";
		case NativeKeyEvent.VC_ALT: return "{Alt}";
		case NativeKeyEvent.VC_META: return "{Windows Key}";
		case NativeKeyEvent.VC_TAB: return "{Tab}";
		case NativeKeyEvent.VC_ESCAPE: return "{Esc}";
		case NativeKeyEvent.VC_F1: return "{F1}";
		case NativeKeyEvent.VC_F2: return "{F2}";
		case NativeKeyEvent.VC_F3: return "{F3}";
		case NativeKeyEvent.VC_F4: return "{F4}";
		case NativeKeyEvent.VC_F5: return "{F5}";
		case NativeKeyEvent.VC_F6: return "{F6}";
		case NativeKeyEvent.VC_F7: return "{F7}";
		case NativeKeyEvent.VC_F8: return "{F8}";
		case NativeKeyEvent.VC_F9: return "{F9}";
		case NativeKeyEvent.VC_F10: return "{F10}";
		case NativeKeyEvent.VC_F11: return "{F11}";
		case NativeKeyEvent.VC_F12: return "{F12}";
		case NativeKeyEvent.VC_CAPS_LOCK: return "{Caps Lock}";
		case NativeKeyEvent.VC_NUM_LOCK: return "{Num Lock}";
		case NativeKeyEvent.VC_SCROLL_LOCK: return "{Scroll Lock}";
		case NativeKeyEvent.VC_PRINTSCREEN: return "{PrintScreen}";
		case NativeKeyEvent.VC_INSERT: return "{Insert}";
		case NativeKeyEvent.VC_DELETE: return "{Delete}";
		case NativeKeyEvent.VC_PAGE_UP: return "{Page Up}";
		case NativeKeyEvent.VC_PAGE_DOWN: return "{Page Down}";
		case NativeKeyEvent.VC_HOME: return "{Home}";
		case NativeKeyEvent.VC_END: return "{End}";
		case NativeKeyEvent.VC_UP: return "{Up}";
		case NativeKeyEvent.VC_DOWN: return "{Down}";
		case NativeKeyEvent.VC_LEFT: return "{Left}";
		case NativeKeyEvent.VC_RIGHT: return "{Right}";
		case NativeKeyEvent.VC_0: return "0";
		case NativeKeyEvent.VC_1: return "1";
		case NativeKeyEvent.VC_2: return "2";
		case NativeKeyEvent.VC_3: return "3";
		case NativeKeyEvent.VC_4: return "4";
		case NativeKeyEvent.VC_5: return "5";
		case NativeKeyEvent.VC_6: return "6";
		case NativeKeyEvent.VC_7: return "7";
		case NativeKeyEvent.VC_8: return "8";
		case NativeKeyEvent.VC_9: return "9";
		case NativeKeyEvent.VC_MINUS: return "-";
		case NativeKeyEvent.VC_EQUALS: return "=";
		case NativeKeyEvent.VC_A: return "a";
		case NativeKeyEvent.VC_B: return "b";
		case NativeKeyEvent.VC_C: return "c";
		case NativeKeyEvent.VC_D: return "d";
		case NativeKeyEvent.VC_E: return "e";
		case NativeKeyEvent.VC_F: return "f";
		case NativeKeyEvent.VC_G: return "g";
		case NativeKeyEvent.VC_H: return "h";
		case NativeKeyEvent.VC_I: return "i";
		case NativeKeyEvent.VC_J: return "j";
		case NativeKeyEvent.VC_K: return "k";
		case NativeKeyEvent.VC_L: return "l";
		case NativeKeyEvent.VC_M: return "m";
		case NativeKeyEvent.VC_N: return "n";
		case NativeKeyEvent.VC_O: return "o";
		case NativeKeyEvent.VC_P: return "p";
		case NativeKeyEvent.VC_Q: return "q";
		case NativeKeyEvent.VC_R: return "r";
		case NativeKeyEvent.VC_S: return "s";
		case NativeKeyEvent.VC_T: return "t";
		case NativeKeyEvent.VC_U: return "u";
		case NativeKeyEvent.VC_V: return "v";
		case NativeKeyEvent.VC_W: return "w";
		case NativeKeyEvent.VC_X: return "x";
		case NativeKeyEvent.VC_Y: return "y";
		case NativeKeyEvent.VC_Z: return "z";
		case NativeKeyEvent.VC_OPEN_BRACKET: return "[";
		case NativeKeyEvent.VC_CLOSE_BRACKET: return "]";
		case NativeKeyEvent.VC_BACK_SLASH: return "\\";
		case NativeKeyEvent.VC_SEMICOLON: return ";";
		case NativeKeyEvent.VC_BACKQUOTE: return "'";
		case NativeKeyEvent.VC_QUOTE: return "\"";
		case NativeKeyEvent.VC_COMMA: return ",";
		case NativeKeyEvent.VC_PERIOD: return ".";
		case NativeKeyEvent.VC_SLASH: return "/";
		case NativeKeyEvent.VC_SPACE: return " ";
		case NativeKeyEvent.VC_YEN: return "`";
		default: return "";
		}
	}
	
	public static boolean isTypedCode(int code){
		switch (code){
		case NativeKeyEvent.VC_BACKSPACE:;
		case NativeKeyEvent.VC_0:
		case NativeKeyEvent.VC_1:
		case NativeKeyEvent.VC_2:
		case NativeKeyEvent.VC_3:
		case NativeKeyEvent.VC_4:
		case NativeKeyEvent.VC_5:
		case NativeKeyEvent.VC_6:
		case NativeKeyEvent.VC_7:
		case NativeKeyEvent.VC_8:
		case NativeKeyEvent.VC_9:
		case NativeKeyEvent.VC_MINUS:
		case NativeKeyEvent.VC_EQUALS:
		case NativeKeyEvent.VC_A:
		case NativeKeyEvent.VC_B:
		case NativeKeyEvent.VC_C:
		case NativeKeyEvent.VC_D:
		case NativeKeyEvent.VC_E:
		case NativeKeyEvent.VC_F:
		case NativeKeyEvent.VC_G:
		case NativeKeyEvent.VC_H:
		case NativeKeyEvent.VC_I:
		case NativeKeyEvent.VC_J:
		case NativeKeyEvent.VC_K:
		case NativeKeyEvent.VC_L:
		case NativeKeyEvent.VC_M:
		case NativeKeyEvent.VC_N:
		case NativeKeyEvent.VC_O:
		case NativeKeyEvent.VC_P:
		case NativeKeyEvent.VC_Q:
		case NativeKeyEvent.VC_R:
		case NativeKeyEvent.VC_S:
		case NativeKeyEvent.VC_T:
		case NativeKeyEvent.VC_U:
		case NativeKeyEvent.VC_V:
		case NativeKeyEvent.VC_W:
		case NativeKeyEvent.VC_X:
		case NativeKeyEvent.VC_Y:
		case NativeKeyEvent.VC_Z:
		case NativeKeyEvent.VC_OPEN_BRACKET:
		case NativeKeyEvent.VC_CLOSE_BRACKET:
		case NativeKeyEvent.VC_BACK_SLASH:
		case NativeKeyEvent.VC_SEMICOLON:
		case NativeKeyEvent.VC_BACKQUOTE:
		case NativeKeyEvent.VC_QUOTE:
		case NativeKeyEvent.VC_COMMA:
		case NativeKeyEvent.VC_PERIOD:
		case NativeKeyEvent.VC_SLASH:
		case NativeKeyEvent.VC_SPACE:
		case NativeKeyEvent.VC_YEN:
			return true;
		default:
			return false;
		}
	}
	
	public static String getMouseText(int mouseCode){
		switch (mouseCode){
		case 1: return "{m1}";
		case 2: return "{m2}";
		case 3: return "{Middle Mouse Btn}";
		default: return "";
		}
	}
}
