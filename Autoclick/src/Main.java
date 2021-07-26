import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.dispatcher.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;

import net.miginfocom.swing.MigLayout;

public class Main extends JFrame {
	private static final long serialVersionUID = 1404408633029004775L;
	public static JTextField activationKey;
	public static boolean running = false;
	public static boolean activeKeyFocus = false;
	public static JFrame frame;
	public static JTable table;
	public static DefaultTableModel tableModel;
	public static GlobalKeyListener keylistener;
	public static JTextField DesiredRuns;
	public static JComboBox<String> commandsList;
	public static boolean dirCreated = false;
	public static String saveDirectory = "";
	public static String currentCommands = "index.txt";
	public static boolean doingInternalAction = false;
	public static JTable imageTable;
	public static DefaultTableModel imageTableModel;
	public static JTextField recordKeyField;
	public static boolean recordKeyFieldFocus = false;
	public static boolean advancedView = false;
	
	// Main table column identifier constants
	public static final int totalCols = 8;
	public static final int colNo = 0;
	public static final int colCommand = 1;
	public static final int colHold = 2;
	public static final int colRelease = 3;
	public static final int colXY = 4;
	public static final int colWaitTime = 5;
	public static final int colComment = 6;
	public static final int colTimeLeft = 7;
	
	// Image table column identifier constants
	public static final int totalImageCols = 4;
	public static final int imageColName = 0;
	public static final int imageColCoords = 1;
	public static final int imageColAction = 2;
	public static final int imageColPreview = 3;
	
	// UI Components need to be initialised in global space to be accessible for rearrangement method
	public static JPanel viewOptionsBox;
	public static JScrollPane scrollPane;
	public static JButton addAction;
	public static JButton deleteAction;
	public static JLabel activationKeyTxt;
	public static JButton startButton;
	public static JPanel runModeContainer;
	public static JButton newCommands;
	public static JButton save;
	public static JButton load;
	public static JPanel imageConditionContainer;
	public static JPanel recordPanel;
	public static JScrollPane savedProjectName;
	public static JLabel recordKeyTxt;
	public static JButton recordButton;
	
	public Main() {
		initUI();
	}
	
	public void initUI() {
		// Set the event dispatcher to a swing safe executor service.
		GlobalScreen.setEventDispatcher(new SwingDispatchService());
		
		// Set up the frame window and content pane
		frame = new JFrame("Auto Command");
		JPanel panel = new JPanel();
		frame.setContentPane(panel);

		// Get the screen size as a java dimension
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
		// Get 90% of the height, and 90% of the width of the screen
		int frameWidth = (int) (screenWidth * 0.9);
		int frameHeight = (int) (screenHeight * 0.9);
		// Set the frame height and width
		frame.setPreferredSize(new Dimension(frameWidth, frameHeight));

		// Create a basic black border line border that can be reused later on
		Border basicBlackBorder = BorderFactory.createLineBorder(Color.BLACK);
		
		// Create the space for displaying the project name once saved
		JLabel savedProjectNameLabel = new JLabel("Project: <Untitled>");
		savedProjectName = new JScrollPane(savedProjectNameLabel);
		savedProjectName.setBorder(basicBlackBorder);
		//savedProjectName.getHorizontalScrollBar().setPreferredSize(new Dimension(savedProjectName.getWidth(),8));
		
		// Column headers are set here
		String[] columnNames = 
			{"No.", "Command", "Hold", "Release", "X,Y", "Wait Time After Action (secs.)", "Comment", "Time Left"};
		// Also start application with a single row of empty data so a new user understands what to do more easily   
		Object[][] data = {{0, "", true, true, "", "", "", ""}};
		
		// Main table assigned here using the DefaultTableModel
		table = new JTable(new DefaultTableModel(data, columnNames)){
			private static final long serialVersionUID = -8930932398265858225L;
			// Allow all cells to be editable except the row ID column, time left countdown column and XY co-ordinate column
			@Override
			public boolean isCellEditable(int row,int column){
				if (column == colNo || column == colTimeLeft || column == colXY) return false;
				return true;
			}
			// Allow each column's renderer to render containing data according to it's type
			// E.g. integer data renders aligned to the right and boolean data renders as a check box (important for
			// hold/release columns.
			@Override
	        public Class<?> getColumnClass(int columnIndex) {
				return data[0][columnIndex].getClass();
	        }
		};
		// Keep the main table's model stored in a global variable to allow easy access for other classes 
		tableModel = (DefaultTableModel) table.getModel();
		
		
		//This block allows reordering of table rows (Taken from https://stackoverflow.com/a/42575276)
		TransferHandler handler = new TableRowTransferHandler();
		table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    table.setTransferHandler(handler);
	    table.setDropMode(DropMode.INSERT_ROWS);
	    table.setDragEnabled(true);
	    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	    //Disable row Cut, Copy, Paste
	    ActionMap map = table.getActionMap();
	    Action dummy = new AbstractAction() {
			private static final long serialVersionUID = -7395068805752131054L;
			@Override public void actionPerformed(ActionEvent e) { /* Dummy action */ }
	    };
	    map.put(TransferHandler.getCutAction().getValue(Action.NAME),   dummy);
	    map.put(TransferHandler.getCopyAction().getValue(Action.NAME),  dummy);
	    map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);
	    
	    // Make the scrollable area and put the main table in
		scrollPane = new JScrollPane(table);
		// Remove focus from anything else if panel or scrollpane background is clicked 
		panel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				deselectTables();
				panel.requestFocusInWindow();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		    public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		scrollPane.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				deselectTables();
				scrollPane.requestFocusInWindow();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		    public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		
		// For consistency, keep a constant row height throughout and 20px seems reasonable for this
		table.setRowHeight(20);
		// Set the column widths to reasonable values based on the data they hold
		TableColumn column = null;
		for (int i = 0; i < totalCols; i++) {
		    column = table.getColumnModel().getColumn(i);
		    if (i == colNo || i == colHold || i == colRelease) {
		        column.setPreferredWidth(50);
		    }
		    else if (i == colCommand || i == colXY){
		    	column.setPreferredWidth(150);
		    }
		    else if (i == colWaitTime){
		    	column.setPreferredWidth(200);
		    }
		    else if (i == colTimeLeft){
		    	column.setPreferredWidth(100);
		    }
		    else {
		        column.setPreferredWidth(850);
		    }
		}
		// Disable table column reordering to keep constant column numbers that can easily be referenced correctly
		table.getTableHeader().setReorderingAllowed(false);
		// Allow only 1 cell to be selected at a time so it's easier to tell which cell is currently selected
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Get the command column object and set its cell editor as a JComboBox that allows users to correctly select
		// actionable commands.
		TableColumn commandColumn = table.getColumnModel().getColumn(colCommand);
		JComboBox<String> comboBox = new JComboBox<String>();
		// Also allow the user to enter their own Strings by allowing editing so that it can be typed during a run 
		comboBox.setEditable(true);
		comboBox.addItem("{No Action}");
		comboBox.addItem("{m1}");
		comboBox.addItem("{Middle Mouse Btn}");
		comboBox.addItem("{m2}");
		comboBox.addItem("{Enter}");
		comboBox.addItem("{Backspace}");
		comboBox.addItem("{Shift}");
		comboBox.addItem("{Ctrl}");
		comboBox.addItem("{Alt}");
		comboBox.addItem("{Windows Key}");
		comboBox.addItem("{Tab}");
		comboBox.addItem("{Esc}");
		comboBox.addItem("{F1}");
		comboBox.addItem("{F2}");
		comboBox.addItem("{F3}");
		comboBox.addItem("{F4}");
		comboBox.addItem("{F5}");
		comboBox.addItem("{F6}");
		comboBox.addItem("{F7}");
		comboBox.addItem("{F8}");
		comboBox.addItem("{F9}");
		comboBox.addItem("{F10}");
		comboBox.addItem("{F11}");
		comboBox.addItem("{F12}");
		comboBox.addItem("{Caps Lock}");
		comboBox.addItem("{Num Lock}");
		comboBox.addItem("{Scroll Lock}");
		comboBox.addItem("{PrintScreen}");
		comboBox.addItem("{Insert}");
		comboBox.addItem("{Delete}");
		comboBox.addItem("{Page Up}");
		comboBox.addItem("{Page Down}");
		comboBox.addItem("{Home}");
		comboBox.addItem("{End}");
		comboBox.addItem("{Up}");
		comboBox.addItem("{Down}");
		comboBox.addItem("{Left}");
		comboBox.addItem("{Right}");
		comboBox.addItem("[End Program]");
		commandColumn.setCellEditor(new DefaultCellEditor(comboBox));
		
		// Create the image action JComboBox object for later use
		JComboBox<String> imageActionBox = new JComboBox<String>();
		
		// If the user clicks a cell in the XY coordinate column in the main table, create a new maximised almost
		// invisible JFrame to cover the whole screen to get the coordinates of where the user wants to move the mouse.
		// This also serves as a way of showing the area that the user wants as a rectangle on the screen during selection. 
		table.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (table.isColumnSelected(colXY)){
					int XYRow = table.getSelectedRow();
					String XYRowValue = (String) table.getValueAt(XYRow, colXY);
					
					// This block changes the look and feel to the original so that the title bar can be properly hidden
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e1) {
						System.err.println("Code Main1 - Look and feel theme error.");
						e1.printStackTrace();
					}
					JFrame clickFrame = new JFrame();
					clickFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Take the full screen
					clickFrame.setUndecorated(true); // Remove all title bars
					// Remove all the background colour and set the alpha value to 1 so it's almost transparent
					// Setting it fully transparent would make it unclickable so this is necessary
					clickFrame.setBackground(new Color(0,0,0,1));
					// Use the ClickSelect object for the content pane
					clickFrame.getContentPane().add(new ClickSelect(clickFrame, XYRowValue, XYRow, true));
					clickFrame.setVisible(true);
					frame.setState(Frame.ICONIFIED);
					
				}
			}
			public void mouseEntered(MouseEvent e) {/*Unimplemented*/}
			public void mouseExited(MouseEvent e) {/*Unimplemented*/}
		    public void mousePressed(MouseEvent e) {/*Unimplemented*/}
			public void mouseReleased(MouseEvent e) {/*Unimplemented*/}
		});
		
		// Create the Add action button and let it create a new blank row at the end of the main table when clicked
		addAction = new JButton("Add action");
		addAction.addActionListener(new ActionListener() {
		   public void actionPerformed(ActionEvent event) {
			   int rowid = table.getRowCount();
			   tableModel.addRow(new Object[]{rowid, "", true, true, "", "", "", ""});
	       }
		});
		
		// Create the Delete selected row button and set its action as finding and deleting the selected row in main table
		deleteAction = new JButton("Delete selected row");
		deleteAction.addActionListener(new ActionListener() {
		   public void actionPerformed(ActionEvent event) {
			   int rowid = table.getSelectedRow();
			   if (rowid >= 0){ // If nothing is selected rowid would be -1, hence checking if it's >= 0
				   tableModel.removeRow(rowid);
				   // The row IDs would need to be recalculated to appear in order if a row somewhere in the middle is gone
				   for (int i=0;i<table.getRowCount();i++){
					   tableModel.setValueAt(i, i, 0);
				   }
			   }
	       }
		});
		
		// The activationKey is the button the user presses to start or stop the auto commands
		// This JLabel just indicates to the user what the JTextField next to it is for
		activationKeyTxt = new JLabel("Start/Stop key:");
		activationKey = new JTextField();
		// Set the text in the JTextField initially to the chosen key in GlobalKeyListener.startKey (F2 was most appropriate)
		activationKey.setText(NativeKeyEvent.getKeyText(GlobalKeyListener.startKey));
		// Set the alignment of the text in the JTextField to centre
		activationKey.setHorizontalAlignment(JTextField.CENTER);
		// Detect focus in the field so that the commands can't run when trying to change the start/stop key 
		activationKey.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				activeKeyFocus = true;
				deselectTables();
				// When user presses a key in the JTextField, the character is consumed so it doesn't display until
				// it has been set by GlobalKeyListener as what the key represents so that keys that cannot be typed
				// (such as function keys) can be shown as raw text in the field. 
				activationKey.addKeyListener(new KeyAdapter() {
				    public void keyPressed(KeyEvent e) {
				    	e.consume();
				    }
				});
			}
			@Override
			public void focusLost(FocusEvent e) {
				activeKeyFocus = false;
				panel.requestFocusInWindow();
			}
		});
		// The Start/Stop button has the same functionality as pressing the start/stop key
		startButton = new JButton("Start/Stop");
		startButton.addActionListener(new ActionListener() {
		   public void actionPerformed(ActionEvent event) {
			   // Only do commands start/stop if it's not recording actions
			   if (!Recorder.recording){
				   GlobalKeyListener.handleStartStop();
			   }
	       }
		});
		
		// Set up radio buttons for setting the number of times the command list runs for 
		JRadioButton runLengthContinuous = new JRadioButton("Continuous");
		// Start with a continuous run length selection 
		runLengthContinuous.setSelected(true);
		// Set the runContinuous variable to true if the option is selected 
		runLengthContinuous.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlobalKeyListener.runContinuous = true;
			}
		});
		// This is the text field for entering the number of runs the user wants to run for
		// if they want to specify a limited run length.
		DesiredRuns = new JTextField();
		// The limited run length option
		JRadioButton runLengthDefined = new JRadioButton("Enter number of runs:");
		// Set the runContinuous variable to false if this option is selected and put the text field in focus
		// to immediately allow the user to enter the number of runs they want.
		runLengthDefined.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DesiredRuns.requestFocus();
				GlobalKeyListener.runContinuous = false;
			}
		});
		// Also do the same if the user clicks the text field so it's as if the radio button is automatically selected 
		DesiredRuns.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent f) {
				runLengthDefined.setSelected(true);
				GlobalKeyListener.runContinuous = false;
			}
			@Override
			public void focusLost(FocusEvent f) {}
		});
		// Make the 2 radio button options in the same group
		ButtonGroup runLength = new ButtonGroup();
		runLength.add(runLengthContinuous);
		runLength.add(runLengthDefined);
		// This is the container for the run mode options so that the user knows they are part of the same set of options
		runModeContainer = new JPanel(new GridBagLayout());
		// The following sets up the layout for the run mode options container
		GridBagConstraints runModeC1 = new GridBagConstraints();
		runModeC1.fill = GridBagConstraints.HORIZONTAL;
		runModeC1.gridx = 0;
		runModeC1.gridy = 0;
		runModeC1.gridwidth = 2;
		runModeC1.weightx = 1.0;
		runModeC1.weighty = 0.5;
		runModeContainer.add(runLengthContinuous, runModeC1);
		GridBagConstraints runModeC2 = new GridBagConstraints();
		runModeC2.fill = GridBagConstraints.HORIZONTAL;
		runModeC2.gridx = 0;
		runModeC2.gridy = 1;
		runModeC2.weightx = 0.2;
		runModeC2.weighty = 0.5;
		runModeContainer.add(runLengthDefined, runModeC2);
		GridBagConstraints runModeC3 = new GridBagConstraints();
		runModeC3.fill = GridBagConstraints.HORIZONTAL;
		runModeC3.gridx = 1;
		runModeC3.gridy = 1;
		runModeC3.weightx = 1.0;
		runModeC3.weighty = 0.5;
		runModeContainer.add(DesiredRuns, runModeC3);
		// Give the container a border and a title so the user can see the special container area
		runModeContainer.setBorder(BorderFactory.createTitledBorder("Run Mode"));
		
		// Set up the commands list combo box with the ability to only show the tool tip (info when mouse hovered over)
		// when it's disabled so the user knows why it's disabled.
		String[] savedCommandsList = {"index"};
		commandsList = new JComboBox<String>(savedCommandsList){
			private static final long serialVersionUID = -7769735926047230143L;
			private String toolTip;
			@Override
			public void setToolTipText(String text){
				super.setToolTipText(text);
				if (text != null) toolTip = text;
			}
			@Override
			public void setEnabled(boolean b){
				super.setEnabled(b);
				// Once it's been set to enabled as true, the tool tip becomes null, otherwise it is set to toolTip
				super.setToolTipText(b ? null : toolTip);
			}
		};
		// This is where the tool tip is set
		commandsList.setToolTipText("Save or load an Auto Command file to access the multi command list feature.");
		// The same as above is done for the new commands button 
		newCommands = new JButton("New Commands"){
			private static final long serialVersionUID = -8807394692774064615L;
			private String toolTip;
			@Override
			public void setToolTipText(String text){
				super.setToolTipText(text);
				if (text != null) toolTip = text;
			}
			@Override
			public void setEnabled(boolean b){
				super.setEnabled(b);
				super.setToolTipText(b ? null : toolTip);
			}
		};
		newCommands.setToolTipText("Save or load an Auto Command file to access the multi command list feature.");
		// Set the action when the New Commands button is pressed
		newCommands.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get the user input for the new command list file name in a JOptionPane
				String newCommand = JOptionPane.showInputDialog("New command-set name:");
				File newCommandFile = null;
				// Make sure the user actually pressed the OK button to confirm they want the new command file
				if (newCommand != null){
					// Make sure there aren't any illegal characters in the file name as this would cause an error from the OS
					if(newCommand.contains("\\") || newCommand.contains("/") || newCommand.contains(":") ||
						newCommand.contains("*") || newCommand.contains("?") || newCommand.contains("\"") ||
						newCommand.contains("<") || newCommand.contains(">") || newCommand.contains("|")){
							// Notify the user that an invalid character was used and the file could not be created
							JOptionPane.showMessageDialog(panel,"Invalid character used for command name. It cannot contain any of the following:"
									+ "\n"+ "\\ / : * ? \" < > |\n Please choose a valid name for the command."
									,"Error", JOptionPane.ERROR_MESSAGE);
					}
					// If the user chooses this exact name, reject it because this name will be used by
					// the image condition options
					else if(newCommand.equals("ImageConditionOptions")){
						// Notify the user with an error message 
						JOptionPane.showMessageDialog(panel,"Invalid name chosen. This is a reserved command name"
									,"Error", JOptionPane.ERROR_MESSAGE);
					}
					else {
						// If the file name chosen is valid then set the new command file name ready to be created
						newCommandFile = new File(saveDirectory + "\\" + newCommand + ".txt");
					}
				}
				try {
					// Don't do anything if the user didn't press OK on the JOptionPane input dialog
					// or if the new command file name hasn't been set because an invalid character was used.
					if (newCommand == null || newCommandFile == null){}
					// We would know that the command already exists
					// if createNewFile() wasn't successful and returns false with the new command file name chosen.
					else if (!newCommandFile.createNewFile()){
						// Notify the user with a dialog box that the command file couldn't be created
					    JOptionPane.showMessageDialog(panel, "Command-set already exists!");
					}
					// Otherwise the command file was created successfully
					else {
						// It's necessary to let the program know that it isn't the user changing the combo box
						// otherwise it would fire the combo box changed method.
						doingInternalAction = true;
						// Add the new command file name to the commandsList combo box
						commandsList.addItem(newCommand);
						// Call the updateComboBox method to update all combo boxes in the main table with the new command file
						updatecomboBox(comboBox);
						// Also update all combo boxes in the image condition table with the new command file
						updateImagecomboBox(imageActionBox);
						// We are done changing the combo box so now we can listen for changes that the user makes
						doingInternalAction = false;
					}
				// This is used for debugging to identify what the problem might be and where it originated
				} catch (IOException e) {
					System.err.println("Code Main2 - Error creating new command file.");
					e.printStackTrace();
				}
			}
		});
		// Listen for changes to the commands list combo box that the user makes
		commandsList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Make sure that it's the user making changes and not an internal change within the program
				if (!doingInternalAction){
					// Get the commands the user selected
					String commandSet = (String)commandsList.getSelectedItem();
					// This is to decide whether there have been changes made to the currently loaded commands based on
					// what is currently saved on file. If there are any unsaved changes, the user gets a warning dialog.
					boolean showConfirmLoad = true;
					try {
						// Find what data is currently saved for this command list
						Object[][] loadedDataCurr = LoadFile(saveDirectory + "\\" + currentCommands);
						// If it's the same number of rows that is currently stored then we need to do more checking,
						// Otherwise it's definitely changed
						if (loadedDataCurr.length == table.getRowCount()){
							// Start off with assuming no changes
							boolean changed = false;
							// Go through the whole data that is saved and compare with what's in the main table now
							outerloop:
							for (int i=0;i<loadedDataCurr.length;i++){
								for (int j=0;j<loadedDataCurr[i].length;j++){
									// Convert both the loaded data and the data currently in the main table to String 
									String tblvalue = "";
									String loadvalue = "";
									// If any of the data cells are null, they will remain as empty strings,
									// which can also be compared easily.
									if (table.getValueAt(i, j) != null){
										tblvalue = String.valueOf(table.getValueAt(i, j));
									}
									if (loadedDataCurr[i][j] != null){
										loadvalue = String.valueOf(loadedDataCurr[i][j]);
									}
									// If any of the data is found to be different, changed is marked as true and the whole
									// loop is broken out of to try to optimise the application speed and not keep looping
									// unnecessarily if the data is already found to be changed.
									if (!loadvalue.equals(tblvalue)){
										changed = true;
										break outerloop;
									}
								}
							}
							// If the data is found not changed, then there would be no reason to show the confirm dialog
							if (!changed){
								showConfirmLoad = false;
							}
						}
					// This is used for debugging to identify what the problem might be and where it originated
					} catch (IOException e1) {
						System.err.println("Code Main3 - Error loading file.");
						e1.printStackTrace();
					}
					// Initially, we assume it's OK to move on with overwriting the main table
					int confirmLoad = JOptionPane.OK_OPTION;
					// Only show the warning dialog if it had been determined that the data had been changed
					if (showConfirmLoad){
						confirmLoad = JOptionPane.showConfirmDialog(panel, "Any unsaved changes will be lost.", "Warning", JOptionPane.OK_CANCEL_OPTION);
					}
					
					// If the user said it was OK to overwrite the data or if there were no changes,
					// load in the chosen command list to the main table.
					if (confirmLoad == JOptionPane.OK_OPTION){
						try {
							// Change the global variable to the correct command set name so it helps with other functions 
							currentCommands = commandSet + ".txt";
							// Load in the data to a variable using the known saved directory and the chosen command file
							Object[][] loadedData = LoadFile(saveDirectory + "\\" + currentCommands);
							// Make sure to change the String representation of the Hold and Release column values to boolean
							for (int i=0;i<loadedData.length;i++){
								for (int j=colHold;j<=colRelease;j++){
									loadedData[i][j] = Boolean.valueOf((String)loadedData[i][j]);
								}
							}
							// Clear the main table
							tableModel.setRowCount(0);
							// Add the loaded data to the main table
							for (int i=0;i<loadedData.length;i++){
								tableModel.insertRow(i, loadedData[i]);
							}
						// This is used for debugging to identify what the problem might be and where it originated
						} catch (IOException e) {
							System.err.println("Code Main4 - Error loading file.");
							e.printStackTrace();
						}
					}
					// If the user cancels and doesn't want to overwrite the table data,
					// the combo box gets set back to the value it was at before selecting the new command set option.
					else {
						// It's necessary to let the program know that it isn't the user changing the combo box
						// otherwise it would fire the combo box changed method.
						doingInternalAction = true;
						// Find the last dot in the current command set name so we know where to exclude the file extension
						int lastDot = currentCommands.lastIndexOf(".");
						// Remove what's behind the last dot to get the pure name of the file
						String commandSetName = currentCommands.substring(0, lastDot);
						// Set that command set as the selected one in combo box so the user knows which one they're using
						commandsList.setSelectedItem(commandSetName);
						// We are done changing the combo box so now we can listen for changes that the user makes
						doingInternalAction = false;
					}
				}
			}
		});
		// Initially, set the newCommands button and the commandsList combo box as disabled so user has to load or save first
		newCommands.setEnabled(false);
		commandsList.setEnabled(false);
		// Make sure the main table's combo boxes have all the available options in them initially
		updatecomboBox(comboBox);
		
		// Initialise the Save button
		save = new JButton("Save");
		// Make a file filter that only shows directories for when the user wants to save. This is to force save files
		// to be together in one directory where all command files and images are together.
		FileFilter saveFilter = new FileFilter() {
			@Override
			// Only accept the file in the JFileChooser if it's a directory
			public boolean accept(File file) {
				return file.isDirectory();
			}
			// Let the user know that it's filtered to directories only
			public String getDescription() {return "Directory";}
		};
		// Set up the JFileChooser to use the directory file filter and remove the default "All Files" filter
	    JFileChooser fcSave = new JFileChooser();
	    fcSave.setFileFilter(saveFilter);
	    fcSave.setAcceptAllFileFilterUsed(false);
	    // Through experimentation, only the FILES_AND_DIRECTORIES file selection mode worked correctly 
	    // which meant having to use a custom file filter above.
	    fcSave.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	    // Set initial save location to the same file location the application is stored and call the new folder New Commands
		fcSave.setSelectedFile(new File(fcSave.getCurrentDirectory(),"New Commands"));
		// Set the action for when save button is clicked
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Just to be sure, deselect all tables when the button is clicked
				deselectTables();
				// Make sure the main table is validated before saving so there aren't any problems with invalid characters 
				if (validateTable()){
					// If the user hasn't already saved or loaded yet, the save button would show up the JFileChooser
					// to allow the user to select a directory location and name for a new one
					if (!dirCreated){
						// Show the JFileChooser save dialog to choose a directory location and name
						int returnVal = fcSave.showSaveDialog(panel);
						// Proceed if the user approved of a location and name
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							// Get the string representation of the chosen directory path
							String fileLocation = fcSave.getSelectedFile().toString();
							// Get the directory name and use it as the project name
							savedProjectNameLabel.setText("Project: " + fcSave.getSelectedFile().getName());
							// Convert the string representation to a Path object for ease of use with other methods later
							Path saveDir = Paths.get(fileLocation);
							// Create the new directory in the chosen save Path
							try {
								Files.createDirectories(saveDir);
							// This is used for debugging to identify what the problem might be and where it originated
							} catch (IOException e) {
								e.printStackTrace();
								System.err.println("Code Main5 - Error saving to new directory.");
							}
							// Get the main table's size to create a new String 2D array to store the data so it can be
							// correctly saved as a tab-delimited text file
							int saveTableCols = tableModel.getColumnCount();
							int saveTableRows = tableModel.getRowCount();
							String[][] saveTableData = new String[saveTableRows][saveTableCols];
							// Put all the main table data into the 2D array
							for (int i=0;i<saveTableRows;i++){
								for (int j=0;j<saveTableCols;j++){
									// Make sure the data is converted to String form so it can be stored as text
									String value = String.valueOf(table.getValueAt(i, j));
									saveTableData[i][j] = value;
								}
							}
							// Save the data into a new file in the chosen directory called index.txt
							// This is the initial file that is always shown first when loading
							// SaveFile method handles converting the array to string and creating the actual file elsewhere
							try {
								SaveFile(fileLocation + "\\index.txt", saveTableData);
							// This is used for debugging to identify what the problem might be and where it originated
							} catch (IOException e) {
								e.printStackTrace();
								System.err.println("Code Main6 - Error saving file");
							}
							// Reassign the global variables to match the new state of directory created and the name of it
							saveDirectory = fileLocation;
							dirCreated = true;
							// The disabled buttons for creating and switching between command files can now be enabled
							newCommands.setEnabled(true);
							commandsList.setEnabled(true);
						}
					}
					// If the user has already saved or loaded, then the directory would have already been chosen
					// and so only an update to the saved table file in the directory is required
					else {
						// Get the main table's size to create a new String 2D array to store the data so it can be
						// correctly saved as a tab-delimited text file
						int saveTableCols = tableModel.getColumnCount();
						int saveTableRows = tableModel.getRowCount();
						String[][] saveTableData = new String[saveTableRows][saveTableCols];
						// Put all the main table data into the 2D array
						for (int i=0;i<saveTableRows;i++){
							for (int j=0;j<saveTableCols;j++){
								// Make sure the data is converted to String form so it can be stored as text
								String value = String.valueOf(table.getValueAt(i, j));
								// If the cell doesn't contain anything, it will be null, so it will need to be changed
								// into an empty string to more correctly represent its contents
								if (table.getValueAt(i, j) == null){
									saveTableData[i][j] = "";
								}
								// Otherwise, the value in the cell can remain as it is.
								else {
									saveTableData[i][j] = value;
								}
							}
						}
						// Save the data into a new file in the chosen directory called using the chosen name
						// SaveFile method handles converting the array to string and creating the actual file elsewhere
						try {
							SaveFile(saveDirectory + "\\" + currentCommands, saveTableData);
						// This is used for debugging to identify what the problem might be and where it originated
						} catch (IOException e) {
							e.printStackTrace();
							System.err.println("Code Main7 - Error saving file");
						}
					}
				}
			}
		});
		// Initialise the Save button
		load = new JButton("Load");
		// Create a new JFileChooser that allows the user to select which commands directory they would like to load,
		// while also setting it to show directories only so the user can only select command directories
		JFileChooser fcLoad = new JFileChooser();
	    fcLoad.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    // Set the action for when load button is clicked
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Just to be sure, deselect all tables when the button is clicked
				deselectTables();
				// Get the directory to load from the user
				int returnVal = fcLoad.showOpenDialog(panel);
				// If the user chose a directory and approved
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					// Get the String representation of the directory path the user chose
					String loadFileLocation = fcLoad.getSelectedFile().getAbsolutePath();
					// Get the directory name and use it as the project name
					savedProjectNameLabel.setText("Project: " + fcLoad.getSelectedFile().getName());
					// Always start from the index file after loading in a new directory
					String loadFileName = loadFileLocation + "\\index.txt";
					// Check if that index file actually exists
					File loadFile = new File(loadFileName);
					if (loadFile.exists()){
						try {
							// Load the file in to a 2D array so it could be used
							Object[][] loadedData = LoadFile(loadFileName);
							// Make sure to change the String representation of the Hold and Release column values to boolean
							for (int i=0;i<loadedData.length;i++){
								for (int j=colHold;j<=colRelease;j++){
									loadedData[i][j] = Boolean.valueOf((String)loadedData[i][j]);
								}
							}
							// Clear the current main table
							tableModel.setRowCount(0);
							// Add all the rows of data from the loaded data into the main table
							for (int i=0;i<loadedData.length;i++){
								tableModel.insertRow(i, loadedData[i]);
							}
							// Reassign the global variables to match the new state of directory created and the name of it
							saveDirectory = loadFileLocation;
							dirCreated = true;
							// Change the global variable to the correct command set name so it helps with other functions
							currentCommands = "index.txt";
							// The disabled buttons for creating and switching between command files can now be enabled
							newCommands.setEnabled(true);
							commandsList.setEnabled(true);
							
							// Check if there's an image condition options file and load that in too 
							if (new File(saveDirectory + "\\ImageConditionOptions.txt").exists()){
								// Load in the data to a 2D array
								Object[][] loadedImageData = LoadFile(saveDirectory + "\\ImageConditionOptions.txt");
								// Clear the image table
								imageTableModel.setRowCount(0);
								// Add the data to the image table
								for (int i=0;i<loadedImageData.length;i++){
									imageTableModel.insertRow(i, loadedImageData[i]);
									// If there exists an image in that row, give the option to preview it
									// (having coordinates means a screenshot had been taken)
									if (((String) loadedImageData[i][imageColCoords]).length() > 0){
										imageTable.setValueAt("View   >>", i, imageColPreview);
									}
								}
							}
							
							
							// Change this variable to true otherwise the combo box actions would fire
							doingInternalAction = true;
							// Clear the commands list combo box to add the commands for the new directory
							commandsList.removeAllItems();
							// Get all the command files within the new directory (disclude the image condition file)
							File cmdDir = new File(saveDirectory);
							File[] allCommandFiles = cmdDir.listFiles(new FilenameFilter() { 
								public boolean accept(File dir, String filename){
									if (filename.endsWith(".txt") && !filename.endsWith("ImageConditionOptions.txt")){
										return true;
									}
									return false; }
							});
							// Add the raw names of the command files without the extensions to the commandsList combo box
							for (File f : allCommandFiles){
								// Find where the last dot in the file name is to know where the extension begins
								int lastDot = f.getName().lastIndexOf(".");
								// Keep only the part before the dot and add it to the combo box
								String commandSetName = f.getName().substring(0, lastDot);
								commandsList.addItem(commandSetName);
							}
							// Set the index command file as the first one to be looked at 
							commandsList.setSelectedItem("index");
							// Update all main table combo boxes to have all [Goto ...] commands for all command sets
							updatecomboBox(comboBox);
							// Do the same for the image table
							updateImagecomboBox(imageActionBox);
							// Done changing the combo box
							doingInternalAction = false;
						// This is used for debugging to identify what the problem might be and where it originated
						} catch (IOException e) {
							e.printStackTrace();
							System.err.println("Code Main8 - Error loading file");
						}
					}
					// If the index command file doesn't exist, then the directory isn't compatible
					else {
						JOptionPane.showMessageDialog(panel, "Directory isn't a compatible Auto Command directory");
					}
				}
	       }
		});
		
		// The imageConditionContainer will use the GridBagLayout
		imageConditionContainer = new JPanel(new GridBagLayout());
		// Give the container a border and title
		imageConditionContainer.setBorder(BorderFactory.createTitledBorder("Image Condition Options"));
		
		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(null);
		imagePanel.setBorder(basicBlackBorder);
		
		String[] imageColumnNames = {"Name", "Co-ordinates", "Action When Seen", "Preview"};
		Object[][] imageData = {{"","","",""}};
		imageTable = new JTable(new DefaultTableModel(imageData, imageColumnNames)){
			private static final long serialVersionUID = -8973662294107192891L;

			public boolean isCellEditable(int row,int column){
				//if (column == imageColNo || column == imageColName || column == imageColCoords) return false;
				if (column == imageColAction) return true;
				return false;
			}
		};
		imageTableModel = (DefaultTableModel) imageTable.getModel();
		imageTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		JScrollPane imageScrollPane = new JScrollPane(imageTable);
		imageScrollPane.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				deselectTables();
				imageScrollPane.requestFocusInWindow();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		    public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		imageTable.setRowHeight(20);
		column = null;
		for (int i = 0; i < totalImageCols; i++) {
		    column = imageTable.getColumnModel().getColumn(i);
		    if (i == imageColName || i == imageColAction){
		    	column.setPreferredWidth(200);
		    }
		    else if (i == imageColCoords){
		    	column.setPreferredWidth(150);
		    }
		    else {
		        column.setPreferredWidth(100);
		    }
		}
		imageTable.getTableHeader().setReorderingAllowed(false);
		imageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		imageTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int SelectedRow = imageTable.getSelectedRow();
				if (imageTable.isColumnSelected(imageColName)){
					String NameRowValue = (String) imageTable.getValueAt(SelectedRow, imageColName);
					
					String NameRowInput = JOptionPane.showInputDialog(panel, "Image Condition Name:", NameRowValue);
					
					if (NameRowInput == null){}
					// Reject empty strings and already existing names
					else if (NameRowInput.isEmpty()){
						JOptionPane.showMessageDialog(panel, "Please choose a valid name for the image.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					else if(NameRowInput.contains("\\") || NameRowInput.contains("/") || NameRowInput.contains(":") || 
							NameRowInput.contains("*") || NameRowInput.contains("?") || NameRowInput.contains("\"") || 
							NameRowInput.contains("<") || NameRowInput.contains(">") || NameRowInput.contains("|")){
						JOptionPane.showMessageDialog(panel,
								"Invalid character used for image name. It cannot contain any of the following:\n"
								+ "\\ / : * ? \" < > |\n Please choose a valid name for the image.",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
					else if (new File(saveDirectory + "\\" + NameRowInput + ".png").exists()){
						JOptionPane.showMessageDialog(panel, "Image file already exists.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					// Otherwise rename the image file if all is OK.
					else {
						imageTable.setValueAt(NameRowInput, SelectedRow, imageColName);
						String XYRowValue = (String) imageTable.getValueAt(SelectedRow, imageColCoords);
						if (XYRowValue.length() > 0){
							File oldImage = new File(saveDirectory + "\\" + NameRowValue + ".png");
							File newImage = new File(saveDirectory + "\\" + NameRowInput + ".png");
							boolean successfulRenameImage = oldImage.renameTo(newImage);
							if (!successfulRenameImage) {
								JOptionPane.showMessageDialog(panel, "Error Renaming screenshot image.", "Error", JOptionPane.ERROR_MESSAGE);
							}
							else {
								updateImageConditionFile();
							}
						}
					}
					
				}
				else if (imageTable.isColumnSelected(imageColCoords)){
					if (dirCreated){
						String imageName = (String) imageTable.getValueAt(SelectedRow, imageColName);
						if (imageName.length() > 0){
							String XYRowValue = (String) imageTable.getValueAt(SelectedRow, imageColCoords);
							
							// This block changes the look and feel to the original so that the title bar can be properly hidden
							try {
								UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
							} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
									| UnsupportedLookAndFeelException e1) {
								System.err.println("Code Main9 - Look and feel theme error.");
								e1.printStackTrace();
							}
							JFrame clickFrame = new JFrame();
							clickFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
							clickFrame.setUndecorated(true);
							clickFrame.setBackground(new Color(0,0,0,1));
							clickFrame.getContentPane().add(new ClickSelect(clickFrame, XYRowValue, SelectedRow, false));
							clickFrame.setVisible(true);
							frame.setState(Frame.ICONIFIED);
						}
						else {
							JOptionPane.showMessageDialog(panel, "Please set an image name first.");
						}
					}
					else {
						JOptionPane.showMessageDialog(panel, "Please save before setting a new image condition.");
					}
				}
				else if (imageTable.isColumnSelected(imageColPreview)){
					String imageCoords = (String) imageTable.getValueAt(SelectedRow, imageColCoords);
					if (imageCoords.length() > 0){
						try {
							String imageName = (String) imageTable.getValueAt(SelectedRow, imageColName);
							BufferedImage readImage = ImageIO.read(new File(saveDirectory + "\\" + imageName + ".png"));
							
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
					        
					        double imagePanelWidth = imagePanel.getWidth();
					        double imagePanelHeight = imagePanel.getHeight();
					        int adjustedX = (int) Math.ceil((imagePanelWidth / screenWidth) * x);
					        int adjustedY = (int) Math.ceil((imagePanelHeight / screenHeight) * y);
					        int adjustedWidth = (int) Math.ceil((imagePanelWidth / screenWidth) * w);
					        int adjustedHeight = (int) Math.ceil((imagePanelHeight / screenHeight) * h);
					        
					        BufferedImage adjustedImage = resize(readImage, adjustedWidth, adjustedHeight);
							JLabel imageLabel = new JLabel(new ImageIcon(adjustedImage));
							imageLabel.setBounds(adjustedX, adjustedY, adjustedWidth, adjustedHeight);
							
							imagePanel.removeAll();
							imagePanel.add(imageLabel);
							panel.revalidate();
							panel.repaint();
						} catch (IOException e1) {
							System.err.println("Code Main10 - Error reading image to preview.");
							e1.printStackTrace();
						}
					}
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		    public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		TableColumn actionColumn = imageTable.getColumnModel().getColumn(imageColAction);
		imageActionBox.setEditable(false);
		imageActionBox.addItem("{End Program}");
		imageActionBox.addItem("{Restart}");
		doingInternalAction = true;
		updateImagecomboBox(imageActionBox);
		doingInternalAction = false;
		actionColumn.setCellEditor(new DefaultCellEditor(imageActionBox));
		
		imageTableModel.addTableModelListener(new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE){
		            int column = e.getColumn();
		            
		            if (column == imageColAction && dirCreated && (new File(saveDirectory + "\\ImageConditionOptions.txt").exists())){
		                updateImageConditionFile();
		            }
				}
			}
		});
		
		JButton addImage = new JButton("Add Image");
		addImage.addActionListener(new ActionListener() {
		   public void actionPerformed(ActionEvent event) {
			   imageTableModel.addRow(new Object[]{"","","",""});
	       }
		});
		JButton deleteImage = new JButton("Delete selected image row");
		deleteImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int rowid = imageTable.getSelectedRow();
				if (rowid >= 0){
					boolean imageExists = false;
					String imageName = "";
					if (!String.valueOf(imageTable.getValueAt(rowid, imageColCoords)).equals("")){
						imageExists = true;
						imageName = String.valueOf(imageTable.getValueAt(rowid, imageColName));
					}
					imageTableModel.removeRow(rowid);
					if (dirCreated){
						if (new File(saveDirectory + "\\ImageConditionOptions.txt").exists()){
							updateImageConditionFile();
						}
						if (imageExists){
							File imageFile = new File(saveDirectory + "\\" + imageName + ".png");
							imageFile.delete();
						}
					}
				}
			}
		});
		
		
		JButton hidePreview = new JButton("<<");
		hidePreview.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				imagePanel.removeAll();
				panel.revalidate();
				panel.repaint();
			}
		});
		
		GridBagConstraints imageCondC1 = new GridBagConstraints();
		imageCondC1.fill = GridBagConstraints.BOTH;
		imageCondC1.gridx = 0;
		imageCondC1.gridwidth = 2;
		imageCondC1.weightx = 0.2;
		imageCondC1.weighty = 0.98;
		imageConditionContainer.add(imageScrollPane, imageCondC1);
		GridBagConstraints imageCondC2 = new GridBagConstraints();
		imageCondC2.fill = GridBagConstraints.BOTH;
		imageCondC2.gridx = 0;
		imageCondC2.gridy = 1;
		imageCondC2.gridwidth = 1;
		imageCondC2.weightx = 0.07;
		imageCondC2.weighty = 0.02;
		imageConditionContainer.add(addImage, imageCondC2);
		GridBagConstraints imageCondC3 = new GridBagConstraints();
		imageCondC3.fill = GridBagConstraints.BOTH;
		imageCondC3.gridx = 1;
		imageCondC3.gridy = 1;
		imageCondC3.gridwidth = 1;
		imageCondC3.weightx = 0.03;
		imageCondC3.weighty = 0.02;
		imageConditionContainer.add(deleteImage, imageCondC3);
		GridBagConstraints imageCondC4 = new GridBagConstraints();
		imageCondC4.fill = GridBagConstraints.NONE;
		imageCondC4.gridx = 1;
		imageCondC4.gridy = 1;
		imageCondC4.gridwidth = 1;
		imageCondC4.weightx = 0.03;
		imageCondC4.weighty = 0.02;
		imageCondC4.gridx = 2;
		imageCondC4.gridy = 0;
		imageCondC4.gridheight = 2;
		imageCondC4.weightx = 0.01;
		imageCondC4.weighty = 1.00;
		imageConditionContainer.add(hidePreview, imageCondC4);
		GridBagConstraints imageCondC5 = new GridBagConstraints();
		imageCondC5.gridy = 1;
		imageCondC5.gridwidth = 1;
		imageCondC5.weighty = 0.02;
		imageCondC5.gridx = 2;
		imageCondC5.gridy = 0;
		imageCondC5.gridheight = 2;
		imageCondC5.weightx = 0.01;
		imageCondC5.weighty = 1.00;
		imageCondC5.fill = GridBagConstraints.BOTH;
		imageCondC5.gridx = 3;
		imageCondC5.weightx = 0.55;
		imageConditionContainer.add(imagePanel, imageCondC5);
		
		recordPanel = new JPanel(new GridBagLayout());
		recordPanel.setBorder(BorderFactory.createTitledBorder("Record actions for autofill"));
		//recordPanel.setBounds(1220, 550, 300, 100);
		recordKeyTxt = new JLabel("Start/Stop recording key:");
		recordKeyField = new JTextField();
		recordKeyField.setText(NativeKeyEvent.getKeyText(GlobalKeyListener.recordKey));
		recordKeyField.setHorizontalAlignment(JTextField.CENTER);
		recordKeyField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				recordKeyFieldFocus = true;
				deselectTables();
				recordKeyField.addKeyListener(new KeyAdapter() {
				    public void keyPressed(KeyEvent e) {
				    	e.consume();
				    }
				});
			}
			@Override
			public void focusLost(FocusEvent e) {
				recordKeyFieldFocus = false;
				panel.requestFocusInWindow();
			}
		});
		recordButton = new JButton("Start/Stop recording");
		recordButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!running){
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
			}
		});
		
		GridBagConstraints recordC1 = new GridBagConstraints();
		recordC1.gridx = 0;
		recordC1.gridy = 0;
		//recordC1.fill = GridBagConstraints.HORIZONTAL;
		recordC1.weightx = 0.5;
		recordC1.weighty = 0.5;
		recordC1.anchor = GridBagConstraints.LINE_END;
		recordC1.insets = new Insets(0,0,0,10);
		recordPanel.add(recordKeyTxt, recordC1);
		GridBagConstraints recordC2 = new GridBagConstraints();
		recordC2.gridx = 1;
		recordC2.gridy = 0;
		recordC2.fill = GridBagConstraints.BOTH;
		recordC2.weightx = 1.0;
		recordC2.weighty = 0.5;
		//recordC2.insets = new Insets(0,0,0,(int)(recordPanel.getWidth()*0.1));
		recordPanel.add(recordKeyField, recordC2);
		GridBagConstraints recordC3 = new GridBagConstraints();
		recordC3.gridx = 0;
		recordC3.gridy = 1;
		recordC3.gridwidth = 2;
		recordC3.fill = GridBagConstraints.BOTH;
		recordC3.weightx = 1.0;
		recordC3.weighty = 0.5;
		//recordC3.insets = new Insets(0,(int)(recordPanel.getWidth()*0.20),0,(int)(recordPanel.getWidth()*0.20));
		recordPanel.add(recordButton, recordC3);
		

		JLabel viewSelect = new JLabel("View: ");
		JRadioButton basicViewOption = new JRadioButton("Basic");
		basicViewOption.setSelected(true);
		basicViewOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				advancedView = false;
				updateView(panel);
			}
		});
		JRadioButton advancedViewOption = new JRadioButton("Advanced");
		advancedViewOption.setSelected(true);
		advancedViewOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				advancedView = true;
				updateView(panel);
			}
		});
		// Make the 2 radio button options in the same group
		ButtonGroup viewOptions = new ButtonGroup();
		viewOptions.add(basicViewOption);
		viewOptions.add(advancedViewOption);
		viewOptionsBox = new JPanel(new GridBagLayout());
		viewOptionsBox.setBorder(basicBlackBorder);
		GridBagConstraints viewOptionsC1 = new GridBagConstraints();
		viewOptionsC1.gridx = 0;
		viewOptionsC1.weightx = 0.3;
		viewOptionsBox.add(viewSelect, viewOptionsC1);
		GridBagConstraints viewOptionsC2 = new GridBagConstraints();
		viewOptionsC2.gridx = 1;
		viewOptionsC2.weightx = 0.35;
		viewOptionsBox.add(basicViewOption, viewOptionsC2);
		GridBagConstraints viewOptionsC3 = new GridBagConstraints();
		viewOptionsC3.gridx = 2;
		viewOptionsC3.weightx = 0.35;
		viewOptionsBox.add(advancedViewOption, viewOptionsC3);
		
		
		// Main panel layouts
		updateView(panel);
		
		// make sure panel is focusable to enable key listener 
		panel.setFocusable(true);
		panel.requestFocusInWindow();
		
		keylistener = new GlobalKeyListener();
		frame.addWindowListener(keylistener);
		
		frame.pack();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private static void updatecomboBox(JComboBox<String> comboBox){
		for (int i=0;i<commandsList.getItemCount();i++){
			String commandsListItem = (String) commandsList.getItemAt(i);
			boolean commandsListItemExists = false;
			for (int j=0;j<comboBox.getItemCount();j++){
				if (comboBox.getItemAt(j).equals("[GoTo " + commandsListItem + "]")){
					commandsListItemExists = true;
				}
			}
			if (!commandsListItemExists && !commandsListItem.equals(currentCommands.substring(0, currentCommands.lastIndexOf('.')))){
				comboBox.addItem("[GoTo " + commandsListItem + "]");
			}
		}
	}
	private static void updateImagecomboBox(JComboBox<String> comboBox){
		for (int i=0;i<commandsList.getItemCount();i++){
			String commandsListItem = (String) commandsList.getItemAt(i);
			boolean commandsListItemExists = false;
			for (int j=0;j<comboBox.getItemCount();j++){
				if (comboBox.getItemAt(j).equals("[GoTo " + commandsListItem + "]")){
					commandsListItemExists = true;
				}
			}
			if (!commandsListItemExists){
				comboBox.addItem("[GoTo " + commandsListItem + "]");
			}
		}
	}
	public static void SaveFile(String fileName, String[][] data) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		for (int i=0;i<data.length;i++){
			for (int j=0;j<data[i].length;j++){
				writer.write(data[i][j]);
				if (j == (data[i].length-1)){
					writer.write("\n");
				}
				else {
					writer.write("\t");
				}
			}
		}
		
		writer.close();
	}
	public static Object[][] LoadFile(String fileName) throws IOException {
		int loadTableCols = totalCols;
		int loadTableRows = 0;
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		while ((reader.readLine()) != null) loadTableRows++;
		reader.close();
		
		Object[][] loadTableData = new Object[loadTableRows][loadTableCols];
		reader = new BufferedReader(new FileReader(fileName));
		int rowNum = -1;
		String row;
		while ((row = reader.readLine()) != null){
			rowNum++;
			int colNum = 0;
			String cell = "";
			for (int i=0;i<row.length();i++){
				if (row.charAt(i) == '\t') {
					colNum++;
					cell = "";
				}
				else {
					cell += row.charAt(i);
					loadTableData[rowNum][colNum] = cell;
				}
			}
		}
		reader.close();
		return loadTableData;
	}
	
	public static void updateTableCoords(String value, int XYRow){
		table.setValueAt(value, XYRow, colXY);
		
		// Set the UI look and feel to the JTattoo customised theme
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			System.err.println("Code Main11 - Look and feel theme error.");
			e.printStackTrace();
		}
		frame.setState(NORMAL);
	}
	
	public static void updateImageCoords(String value, int XYRow){
		try {
			String pt1 = value.substring(0, value.indexOf('-'));
			String pt2 = value.substring(value.indexOf('-')+1);
			int valueX = Integer.valueOf(pt1.substring(0, pt1.indexOf(',')));
			int valueY = Integer.valueOf(pt1.substring(pt1.indexOf(',')+1));
			int valueX2 = Integer.valueOf(pt2.substring(0, pt2.indexOf(',')));
			int valueY2 = Integer.valueOf(pt2.substring(pt2.indexOf(',')+1));
			int x = Math.min(valueX,valueX2);
	        int y = Math.min(valueY,valueY2);
	        int w = Math.abs(valueX-valueX2);
	        int h = Math.abs(valueY-valueY2);
			BufferedImage image = new Robot().createScreenCapture(new Rectangle(x,y,w,h));
			String NameRowValue = (String) imageTable.getValueAt(XYRow, imageColName);
			ImageIO.write(image, "png", new File(saveDirectory + "\\" + NameRowValue + ".png"));
			imageTable.setValueAt(value, XYRow, imageColCoords);
			imageTable.setValueAt("View   >>", XYRow, imageColPreview);
			if (new File(saveDirectory + "\\ImageConditionOptions.txt").exists()){
				updateImageConditionFile();
			}
			else {
				String ActionRowValue = (String) imageTable.getValueAt(XYRow, imageColAction);
				String[][] imageData = {{NameRowValue, value, ActionRowValue}};
				SaveFile(saveDirectory + "\\ImageConditionOptions.txt", imageData);
			}
		} catch (IOException | HeadlessException | AWTException e) {
			System.err.println("Code Main12 - Error taking screenshot.");
			e.printStackTrace();
		}
		
		// Set the UI look and feel to the JTattoo customised theme
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			System.err.println("Code Main13 - Look and feel theme error.");
			e.printStackTrace();
		}
		frame.setState(NORMAL);
	}
	
	public static void updateImageConditionFile(){
		int saveTableCols = totalImageCols-1;
		int saveTableRows = imageTableModel.getRowCount();
		String[][] saveTableData = new String[saveTableRows][saveTableCols];
		for (int i=0;i<saveTableRows;i++){
			for (int j=0;j<saveTableCols;j++){
				String value = String.valueOf(imageTable.getValueAt(i, j));
				if (imageTable.getValueAt(i, j) == null){
					saveTableData[i][j] = "";
				}
				else {
					saveTableData[i][j] = value;
				}
			}
		}
		try {
			SaveFile(saveDirectory + "\\ImageConditionOptions.txt", saveTableData);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Code Main14 - Error saving file");
		}
	}
	
	// Resizing BufferedImage code by user Ocracoke, from:
	// https://stackoverflow.com/questions/9417356/bufferedimage-resize 
	public static BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		
		return dimg;
	}
	
	public static void deselectTables(){
		table.clearSelection();
		imageTable.clearSelection();
	}
	
	public static boolean validateTable(){
		for (int i=0;i<table.getRowCount();i++){
			String command = String.valueOf(table.getValueAt(i, colCommand));
			String waitTime = String.valueOf(table.getValueAt(i, colWaitTime));
			String comment = String.valueOf(table.getValueAt(i, colComment));
			if (command.contains("\\n") || command.contains("\\t") || command.contains("?") || command.contains("£")
					|| command.contains("%") || command.contains("|") || command.contains("¬")){
				table.setRowSelectionInterval(i,i);
				JOptionPane.showMessageDialog(frame, "Please remove the invalid characters (\\n, \\t, ?, £, %, | or ¬)"
						+ " in the Command column on the selected row.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if (waitTime != null && !waitTime.isEmpty()){
				if (!waitTime.matches("\\d+(\\.\\d+)?(\\-\\d+(\\.\\d+)?)?")){
					table.setRowSelectionInterval(i,i);
					JOptionPane.showMessageDialog(frame, "Please correct the Wait Time column in the selected row to:\n"
							+ "Either a one real number or a range of real numbers in the format 'X-X'.", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			if (comment.contains("\\n") || comment.contains("\\t")){
				table.setRowSelectionInterval(i,i);
				JOptionPane.showMessageDialog(frame, "Please remove the invalid characters (\\n or \\t)"
						+ " in the Comment column on the selected row.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}
	
	public static void updateView(JPanel panel){
		//First, remove all components in the panel
		//Get the components in the panel
		Component[] componentList = panel.getComponents();
		//Loop through the components
		for(Component c : componentList){
	        //Remove it
	        panel.remove(c);
		}
		
		if (advancedView){
			panel.setLayout(new MigLayout("fillx, gapx 5pt",
					"[120pt,grow,fill][60pt,grow,fill][60pt,grow,fill][50pt,grow][40pt,grow,fill][80pt,grow,fill][165pt,grow,fill][50pt,grow,fill][120pt,grow,fill][120pt,grow][60pt,grow,fill][60pt,grow,fill][120pt,grow,fill]",
					"[8%][47%][2.5%][2.5%][15%][25%]"));
			panel.add(savedProjectName, "cell 0 0 2 1,grow");
			panel.add(viewOptionsBox, "cell 11 0 2 1,grow");
			panel.add(scrollPane, "cell 0 1 13 1,grow");
			panel.add(addAction, "cell 0 2,grow");
			panel.add(deleteAction, "cell 1 2 2 1,grow");
			panel.add(activationKeyTxt, "cell 3 2,align right");
			panel.add(activationKey, "cell 4 2,grow");
			panel.add(startButton, "cell 5 2,grow");
			panel.add(runModeContainer, "cell 6 2 1 2,grow");
			panel.add(newCommands, "cell 8 2,grow");
			panel.add(commandsList, "cell 9 2,grow");
			panel.add(save, "cell 10 2 2 1,grow");
			panel.add(load, "cell 12 2,grow");
			panel.add(imageConditionContainer, "cell 0 4 9 2,grow");
			panel.add(recordPanel, "cell 10 4 3 1,grow");
			recordKeyTxt.setFont(recordKeyTxt.getFont().deriveFont(12.0f));
			recordKeyField.setFont(recordKeyField.getFont().deriveFont(12.0f));
			recordButton.setFont(recordButton.getFont().deriveFont(12.0f));
			activationKeyTxt.setFont(activationKeyTxt.getFont().deriveFont(12.0f));
			activationKey.setFont(activationKey.getFont().deriveFont(12.0f));
			startButton.setFont(startButton.getFont().deriveFont(12.0f));
			save.setFont(save.getFont().deriveFont(12.0f));
			load.setFont(load.getFont().deriveFont(12.0f));
		}
		else {
			panel.setLayout(new MigLayout("fillx, gapx 5pt",
					"[185pt]push[140pt][70pt,fill][70pt,fill][140pt,fill]push[185pt,fill]",
					"[4%]push[20%][10%][10%]push"));
			panel.add(savedProjectName, "cell 0 0,grow");
			panel.add(viewOptionsBox, "cell 5 0,grow");
			
			panel.add(recordPanel, "cell 1 1 4 1,grow");
			
			panel.add(activationKeyTxt, "cell 1 2,align right");
			panel.add(activationKey, "cell 2 2 2 1,grow");
			panel.add(startButton, "cell 4 2,grow");
			
			panel.add(save, "cell 1 3 2 1,grow");
			panel.add(load, "cell 3 3 2 1,grow");
			recordKeyTxt.setFont(recordKeyTxt.getFont().deriveFont(25.0f));
			recordKeyField.setFont(recordKeyField.getFont().deriveFont(25.0f));
			recordButton.setFont(recordButton.getFont().deriveFont(25.0f));
			activationKeyTxt.setFont(activationKeyTxt.getFont().deriveFont(25.0f));
			activationKey.setFont(activationKey.getFont().deriveFont(25.0f));
			startButton.setFont(startButton.getFont().deriveFont(25.0f));
			save.setFont(save.getFont().deriveFont(25.0f));
			load.setFont(load.getFont().deriveFont(25.0f));
		}
		panel.revalidate();
		panel.repaint();
	}
	
	public static void main(String[] args) {
		// Get the logger for "org.jnativehook" and set the level to warning.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.WARNING);
		// Don't forget to disable the parent handlers.
		logger.setUseParentHandlers(false);
		
		// Set the UI look and feel to the JTattoo customised theme
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			System.err.println("Code Main15 - Look and feel theme error.");
			e.printStackTrace();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Main();
			}
		});
		
	}
}
