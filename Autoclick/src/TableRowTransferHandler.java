import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

//Demo - BasicDnD (Drag and Drop and Data Transfer) https://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
//Demo - DropDemo (Drag and Drop and Data Transfer) https://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
//@see https://docs.oracle.com/javase/tutorial/uiswing/examples/dnd/DropDemoProject/src/dnd/ListTransferHandler.java
//@see https://github.com/aterai/java-swing-tips/blob/master/DnDReorderTable/src/java/example/TableRowsDnDTest.java
class TableRowTransferHandler extends TransferHandler {
	private final DataFlavor localObjectFlavor;
	private int[] indices;
	private int addIndex = -1; //Location where items were added
	private int addCount; //Number of items added.

	protected TableRowTransferHandler() {
		super();
		localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
	}
	@Override protected Transferable createTransferable(JComponent c) {
		JTable table = (JTable) c;
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		List<Object> list = new ArrayList<>();
		indices = table.getSelectedRows();
		for (int i : indices) {
			list.add(model.getDataVector().get(i));
		}
		Object[] transferedObjects = list.toArray();
		return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
	}
	@Override public boolean canImport(TransferHandler.TransferSupport info) {
		JTable table = (JTable) info.getComponent();
		boolean isDropable = info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
		table.setCursor(isDropable ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
		return isDropable;
	}
	@Override public int getSourceActions(JComponent c) {
		return TransferHandler.MOVE;
	}
	@Override public boolean importData(TransferHandler.TransferSupport info) {
		if (!canImport(info)) {
			return false;
		}
		TransferHandler.DropLocation tdl = info.getDropLocation();
		if (!(tdl instanceof JTable.DropLocation)) {
			return false;
		}
		JTable.DropLocation dl = (JTable.DropLocation) tdl;
		JTable target = (JTable) info.getComponent();
		DefaultTableModel model = (DefaultTableModel) target.getModel();
		int index = dl.getRow();
		int max = model.getRowCount();
		if (index < 0 || index > max) {
			index = max;
		}
		addIndex = index;
		target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		try {
			Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
			addCount = values.length;
			for (int i = 0; i < values.length; i++) {
				int idx = index++;
				model.insertRow(idx, (Vector) values[i]);
				target.getSelectionModel().addSelectionInterval(idx, idx);
			}
			return true;
		} catch (UnsupportedFlavorException | IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	@Override protected void exportDone(JComponent c, Transferable data, int action) {
		cleanup(c, action == TransferHandler.MOVE);
	}

	//If the remove argument is true, the drop has been
	//successful and it's time to remove the selected items
	//from the list. If the remove argument is false, it
	//was a Copy operation and the original list is left
	//intact.
	protected void cleanup(JComponent c, boolean remove) {
		if (remove && indices != null) {
			c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			DefaultTableModel model = (DefaultTableModel)((JTable) c).getModel();
			//If we are moving items around in the same list, we
			//need to adjust the indices accordingly, since those
			//after the insertion point have moved.
			if (addCount > 0) {
				for (int i = 0; i < indices.length; i++) {
					if (indices[i] >= addIndex) {
						indices[i] += addCount;
					}
				}
			}
			for (int i = indices.length - 1; i >= 0; i--) {
				model.removeRow(indices[i]);
			}
		}
		for (int i=0;i<Main.table.getRowCount();i++){
			Main.tableModel.setValueAt(i, i, 0);
		}
		indices  = null;
		addCount = 0;
		addIndex = -1;
	}
}