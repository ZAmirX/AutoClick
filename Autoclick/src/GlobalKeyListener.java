import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JOptionPane;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class GlobalKeyListener implements NativeKeyListener, NativeMouseInputListener, WindowListener {
	//public static Commands commandExecute;
	public static int startKey = NativeKeyEvent.VC_F2;
	public static int recordKey = NativeKeyEvent.VC_F3;
	public static boolean runContinuous = true;
	public static int runLengthDesired = 0;
	public static boolean RShiftPressed = false;
	public GlobalKeyListener() {
		
	}
	public static void handleStartStop(){
		if (!Main.running){
			if (Main.validateTable()){
				int tableCols = Main.tableModel.getColumnCount();
				int tableRows = Main.tableModel.getRowCount();
				String[][] tableData = new String[tableRows][tableCols];
				for (int i=0;i<tableRows;i++){
					for (int j=0;j<tableCols;j++){
						String value = String.valueOf(Main.table.getValueAt(i, j));
						if (Main.table.getValueAt(i, j) == null){
							tableData[i][j] = "";
						}
						else {
							tableData[i][j] = value;
						}
					}
				}
				String[][] imageTableData = null;
				if (Main.dirCreated){
					if (new File(Main.saveDirectory + "\\ImageConditionOptions.txt").exists()){
						int imageTableCols = Main.imageTableModel.getColumnCount()-1;
						int imageTableRows = Main.imageTableModel.getRowCount();
						imageTableData = new String[imageTableRows][imageTableCols];
						for (int i=0;i<imageTableRows;i++){
							for (int j=0;j<imageTableCols;j++){
								String value = String.valueOf(Main.imageTable.getValueAt(i, j));
								if (Main.imageTable.getValueAt(i, j) == null){
									imageTableData[i][j] = "";
								}
								else {
									imageTableData[i][j] = value;
								}
							}
						}
					}
				}
				
				if (runContinuous){
					Main.running = true;
					Main.frame.setState(Frame.ICONIFIED);
					Commands commandExecute = new Commands(tableData, imageTableData, null, false);
					commandExecute.start();
				}
				else {
					try {
						int DesiredRunsInput = Math.abs(Integer.parseInt(Main.DesiredRuns.getText()));
						Main.running = true;
						Main.frame.setState(Frame.ICONIFIED);
						Commands commandExecute = new Commands(tableData, imageTableData, DesiredRunsInput, false);
						commandExecute.start();
					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(Main.frame, "Please enter a valid integer as the number of runs.");
					}
				}
			}
		}
		else {
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
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (Recorder.recording && e.getKeyCode() != recordKey){
			// This checks if the bit mask for the Right-Shift key is pressed.
			// It is needed as it is possible that it could have been pressed while another modifier key was already pressed.
			// E.g. 0110000 would mean both Right-Ctrl and Right-Shift are pressed,  so we would need to check if the 2^4 bit is active.
			// This is only necessary because the external library used does not recognise the Right-Shift as a known key.
			if ((e.getModifiers() & (1L << (long)(Math.log(NativeInputEvent.SHIFT_R_MASK)/Math.log(2)))) != 0 && !RShiftPressed){
				Recorder.keyPressed(NativeKeyEvent.VC_SHIFT);
				RShiftPressed = true;
			}
			else {
				Recorder.keyPressed(e.getKeyCode());
			}
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		
		if (Main.activeKeyFocus && e.getKeyCode() != recordKey){
			if (!NativeKeyEvent.getKeyText(e.getKeyCode()).equals("Unknown keyCode: 0xe36")){
				startKey = e.getKeyCode();
				Main.activationKey.setText(NativeKeyEvent.getKeyText(e.getKeyCode()));
			}
		}
		
		if (Main.recordKeyFieldFocus && e.getKeyCode() != startKey){
			if (!NativeKeyEvent.getKeyText(e.getKeyCode()).equals("Unknown keyCode: 0xe36")){
				recordKey = e.getKeyCode();
				Main.recordKeyField.setText(NativeKeyEvent.getKeyText(e.getKeyCode()));
			}
		}
		
		if (e.getKeyCode() == startKey && !Main.activeKeyFocus && !Main.recordKeyFieldFocus && !Recorder.recording) {
			handleStartStop();
		}
		
		if (e.getKeyCode() == recordKey && !Main.recordKeyFieldFocus && !Main.activeKeyFocus && !Main.running){
			boolean confirmed = false;
			if (Main.tableModel.getRowCount() > 1 && !Recorder.recording){
				int userConfirm = JOptionPane.showConfirmDialog(Main.frame, "All rows will be deleted and any unsaved changes will be lost.",
						"Warning", JOptionPane.OK_CANCEL_OPTION);
				confirmed = userConfirm == JOptionPane.OK_OPTION ? true : false;
			}
			else {
				confirmed = true;
			}
			if (Recorder.recording){
				Recorder.endRecording();
			}
			else if (confirmed) {
				Recorder.startRecording();
			}
		}
		else if (Recorder.recording){
			// Same as checking the bit mask for the Right-Shift in nativeKeyPressed but making sure it's not active with !.
			if (!((e.getModifiers() & (1L << (long)(Math.log(NativeInputEvent.SHIFT_R_MASK)/Math.log(2)))) != 0) && RShiftPressed){
				Recorder.keyReleased(NativeKeyEvent.VC_SHIFT);
				RShiftPressed = false;
			}
			else {
				Recorder.keyReleased(e.getKeyCode());
			}
		}
	}
	
	public void nativeKeyTyped(NativeKeyEvent e) {}
	
	
	public void nativeMousePressed(NativeMouseEvent e) {
		//e.getButton(): m1 = 1, m2 = 2, MMB = 3
		if (Recorder.recording){
			Recorder.mousePressed(e.getButton());
		}
	}
	@Override
	public void nativeMouseReleased(NativeMouseEvent e) {
		if (Recorder.recording){
			Recorder.mouseReleased(e.getButton());
		}
	}
	@Override
	public void nativeMouseMoved(NativeMouseEvent e) {
		if (Recorder.recording){
			Recorder.mouseMoved(e.getX(), e.getY());
		}
	}
	public void nativeMouseClicked(NativeMouseEvent e) {}
	public void nativeMouseDragged(NativeMouseEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {
		// Initialise native hook.
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("Code GKL1 - There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
			ex.printStackTrace();

			System.exit(1);
		}

		// Add the appropriate listeners.
		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
		GlobalScreen.addNativeMouseMotionListener(this);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		//Clean up the native hook.
		try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e1) {
			System.err.println("Code GKL2 - There was a problem unregistering the native hook.");
			System.err.println(e1.getMessage());
			e1.printStackTrace();
		}
		System.runFinalization();
		System.exit(0);
	}

	public void windowClosing(WindowEvent e) { /* Unimplemented */ }
	public void windowIconified(WindowEvent e) { /* Unimplemented */ }
	public void windowDeiconified(WindowEvent e) { /* Unimplemented */ }
	public void windowActivated(WindowEvent e) { /* Unimplemented */ }
	public void windowDeactivated(WindowEvent e) { /* Unimplemented */ }
	
}
