/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.medialibrary.gui.shared;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.border.Border;

//source: http://codeidol.com/java/swing/Lists-and-Combos/Reorder-a-JList-with-Drag-and-Drop/#part-2
public class ReorderableJList extends JList implements DragSourceListener,
		DropTargetListener, DragGestureListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6724612074505958204L;
	static DataFlavor localObjectFlavor;
	static {
		try {
			localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	static DataFlavor[] supportedFlavors = { localObjectFlavor };
	DragSource dragSource;
	DropTarget dropTarget;
	Object dropTargetCell;
	int draggedIndex = -1;
	
	private static ReorderableJList startDragInstance;

	public ReorderableJList() {
		super();
		setCellRenderer(new ReorderableListCellRenderer());
		setModel(new DefaultListModel());
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
		dropTarget = new DropTarget(this, this);
	}

	// DragGestureListener
	public void dragGestureRecognized(DragGestureEvent dge) {
		if(!isEnabled()) {
			return;
		}
		
		// find object at this x,y
		Point clickPoint = dge.getDragOrigin();
		int index = locationToIndex(clickPoint);
		
		if (index == -1) {
			return;
		}
		
		Object target = getModel().getElementAt(index);
		Transferable trans = new RJLTransferable(target);
		draggedIndex = index;
		dragSource.startDrag(dge, Cursor.getDefaultCursor(), trans, this);
		startDragInstance = this;
	}

	// DragSourceListener events
	public void dragDropEnd(DragSourceDropEvent dsde) {
		if(!isEnabled()) {
			return;
		}
		
		dropTargetCell = null;
		draggedIndex = -1;
		repaint();
	}

	public void dragEnter(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent dse) {
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	// DropTargetListener events
	public void dragEnter(DropTargetDragEvent dtde) {
		if(!isEnabled()) {
			return;
		}
		
		if (dtde.getSource() != dropTarget || !((DropTarget)dtde.getSource()).getComponent().equals(this)) {
			dtde.rejectDrag();
		}
		else {
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		}
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
		if(!isEnabled()) {
			return;
		}
		
		// figure out which cell it's over, no drag to self
		if (dtde.getSource() != dropTarget) {
			dtde.rejectDrag();
		}
		Point dragPoint = dtde.getLocation();
		int index = locationToIndex(dragPoint);
		if (index == -1) {
			dropTargetCell = null;
		}
		else {
			dropTargetCell = getModel().getElementAt(index);
		}
		repaint();
	}

	public void drop(DropTargetDropEvent dtde) {
		if(!isEnabled()) {
			return;
		}
		
		if (draggedIndex < 0 || dtde.getSource() != dropTarget) {
			dtde.rejectDrop();
			return;
		}
		
		Point dropPoint = dtde.getLocation();
		int index = locationToIndex(dropPoint);
		boolean dropped = false;
		try {
			if ((index == -1) || (index == draggedIndex)) {
				dtde.rejectDrop();
				return;
			}
			dtde.acceptDrop(DnDConstants.ACTION_MOVE);
			Object dragged = dtde.getTransferable().getTransferData(localObjectFlavor);
			// move items - note that indicies for insert will
			// change if [removed] source was before target
			boolean sourceBeforeTarget = (draggedIndex < index);
			DefaultListModel mod = (DefaultListModel) getModel();
			mod.remove(draggedIndex);
			mod.add((sourceBeforeTarget ? index - 1 : index), dragged);
			dropped = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		dtde.dropComplete(dropped);
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	class RJLTransferable implements Transferable {
		Object object;

		public RJLTransferable(Object o) {
			object = o;
		}

		public Object getTransferData(DataFlavor df)
				throws UnsupportedFlavorException, IOException {
			if (isDataFlavorSupported(df))
				return object;
			else
				throw new UnsupportedFlavorException(df);
		}

		public boolean isDataFlavorSupported(DataFlavor df) {
			return (df.equals(localObjectFlavor));
		}

		public DataFlavor[] getTransferDataFlavors() {
			return supportedFlavors;
		}
	}

	class ReorderableListCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 6997796439196530912L;
		boolean isTargetCell;
		boolean isLastItem;

		public ReorderableListCellRenderer() {
			super();
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
			isTargetCell = (value == dropTargetCell && startDragInstance.equals(list));
			isLastItem = (index == list.getModel().getSize() - 1);
			boolean showSelected = false;
			return super.getListCellRendererComponent(list, value, index, showSelected, hasFocus);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (isTargetCell) {
				g.setColor(Color.black);
				g.drawLine(0, 0, getSize().width, 0);
			}
		}
		
		@Override
		public Border getBorder() {
			return BorderFactory.createEmptyBorder();
		}
	}
}
