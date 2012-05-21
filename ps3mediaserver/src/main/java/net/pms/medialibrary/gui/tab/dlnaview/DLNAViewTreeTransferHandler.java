//code taken from http://www.coderanch.com/t/346509/Swing-AWT-SWT-JFace/java/JTree-drag-drop-inside-one
package net.pms.medialibrary.gui.tab.dlnaview;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.medialibrary.commons.dataobjects.DOFolder;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.events.NodeMovedActionEvent;
import net.pms.medialibrary.commons.events.NodeMovedActionListener;

public class DLNAViewTreeTransferHandler extends TransferHandler {
	private static final long               serialVersionUID  = 1L;
	private static final Logger log = LoggerFactory.getLogger(DLNAViewTreeTransferHandler.class);
	private DataFlavor                      nodesFlavor;
	private DataFlavor[]                    flavors           = new DataFlavor[1];
	private DefaultMutableTreeNode   nodeToRemove;
	private List<NodeMovedActionListener>   nodeMoveListeners = new ArrayList<NodeMovedActionListener>();
	private DefaultMutableTreeNode[] nodesToRefresh;
	private DefaultMutableTreeNode   moveNode;

	public DLNAViewTreeTransferHandler() {
		String mimeType = "";
		try {
			mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + javax.swing.tree.DefaultMutableTreeNode[].class.getName() + "\"";
			nodesFlavor = new DataFlavor(mimeType);
			flavors[0] = nodesFlavor;
		} catch (ClassNotFoundException e) {
			log.error("MimeType not found: " + mimeType, e);
		}
	}

	public void addNodeMovedActionListener(NodeMovedActionListener listener) {
		if (!this.nodeMoveListeners.contains(listener)) {
			this.nodeMoveListeners.add(listener);
		}
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDrop()) { 
			return false; 
		}

		support.setShowDropLocation(true);

		if (!support.isDataFlavorSupported(nodesFlavor)) { return false; }

		JTree tree = (JTree) support.getComponent();
		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();

		// Only allow drops onto media library folders
		if (!(dl.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode)
				) { 
			return false; 
		}

		DefaultMutableTreeNode nodeToMove = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		DefaultMutableTreeNode nodeToDropOnto = (DefaultMutableTreeNode) dl.getPath().getLastPathComponent();

		// don't allow drop on itself or own child folder
		DefaultMutableTreeNode parent = nodeToDropOnto;
		while (parent != null) {
			if (parent.equals(nodeToMove)) { 
				return false; 
			}
			parent = (DefaultMutableTreeNode) parent.getParent();
		}

		// Do not allow a drop underneath another node than a DOFolder
		if (dl.getChildIndex() > 0 
				&& !(((DefaultMutableTreeNode) tree.getModel().getChild(nodeToDropOnto, dl.getChildIndex() - 1)).getUserObject() instanceof DOFolder)) {
			return false; 
		}

		return true;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		JTree tree = (JTree) c;
		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null && paths.length > 0 
				&& paths[0].getLastPathComponent() instanceof DefaultMutableTreeNode 
				&& ((DefaultMutableTreeNode)paths[0].getLastPathComponent()).getUserObject() instanceof DOFolder) {
			this.nodeToRemove = (DefaultMutableTreeNode) paths[0].getLastPathComponent();

			return new NodesTransferable(this.nodeToRemove);
		}
		return null;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		if ((action & MOVE) == MOVE) {
			if (this.nodesToRefresh != null && this.moveNode != null) {
				for (NodeMovedActionListener l : this.nodeMoveListeners) {
					l.nodeMovedReceived(new NodeMovedActionEvent(this, this.moveNode, this.nodesToRefresh));
				}
				this.moveNode = null;
				this.nodesToRefresh = null;
			}
		}
	}

	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) { return false; }
		// Extract transfer data.
		DefaultMutableTreeNode node = null;
		try {
			Transferable t = support.getTransferable();
			node = (DefaultMutableTreeNode) t.getTransferData(nodesFlavor);
		} catch (UnsupportedFlavorException ufe) {
			log.error("Unsupported Flavor", ufe);
			return false;
		} catch (java.io.IOException ioe) {
			log.error("I/O error", ioe);
			return false;
		}

		// Get drop location info.
		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
		int childIndex = dl.getChildIndex();
		TreePath dest = dl.getPath();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
		JTree tree = (JTree) support.getComponent();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		// Configure for drop mode.
		if (childIndex == -1 || parent.getChildCount() < childIndex) {
			childIndex = getInsertIndex(parent);
		}

		if (this.nodeToRemove.getParent() == parent && this.nodeToRemove.getParent().getIndex(this.nodeToRemove) < childIndex) {
			childIndex--;
		}

		DefaultMutableTreeNode oldParentNode = (DefaultMutableTreeNode)this.nodeToRemove.getParent();

		List<String> expandedPaths = new ArrayList<String>();
		if (tree.isExpanded(new TreePath(this.nodeToRemove.getPath()))) {
			expandedPaths.add(this.nodeToRemove.getPath().toString());
		}
		expandedPaths = getExpandedChildPaths(tree, node, new TreePath(node.getPath()), expandedPaths);

		// Remove node from old position
		model.removeNodeFromParent(this.nodeToRemove);

		// Add node to new position
		model.insertNodeInto(node, parent, childIndex);

		moveNode = node;
		moveNode.setParent(parent);
		
		if(moveNode instanceof DefaultMutableTreeNode
				&& ((DefaultMutableTreeNode)moveNode).getUserObject() instanceof DOFolder){
			((DOFolder)((DefaultMutableTreeNode)moveNode).getUserObject()).setParentFolder((DOMediaLibraryFolder) parent.getUserObject());
		}

		// Keep paths expanded as they were before
		refreshExpandedTreePaths(tree, expandedPaths, node);

		tree.setSelectionPath(new TreePath(node.getPath()));

		ArrayList<DefaultMutableTreeNode> nodesToRefresh = new ArrayList<DefaultMutableTreeNode>();
		nodesToRefresh.add(oldParentNode);
		if (!nodesToRefresh.contains(parent)) {
			nodesToRefresh.add(parent);
		}
		this.nodesToRefresh = nodesToRefresh.toArray(new DefaultMutableTreeNode[nodesToRefresh.size()]);

		return true;
	}

	private void refreshExpandedTreePaths(JTree tree, List<String> expandedPaths, DefaultMutableTreeNode node) {
		TreePath pathToExpand = new TreePath(node.getPath());
		if (expandedPaths.contains(pathToExpand.toString())) {
			tree.expandPath(new TreePath(node.getPath()));
		}

		DefaultMutableTreeNode currentNode;
		for (int i = 0; i < node.getChildCount(); i++) {
			currentNode = (DefaultMutableTreeNode) node.getChildAt(i);

			// recursive call to update children
			refreshExpandedTreePaths(tree, expandedPaths, currentNode);
		}
	}

	private List<String> getExpandedChildPaths(JTree tree, DefaultMutableTreeNode node, TreePath currentPath, List<String> expandedPaths) {
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);

			TreePath p = new TreePath(child.getPath());

			// add to the list if expanded
			if (tree.isExpanded(p)) {
				expandedPaths.add(p.toString());
			}

			// recursive call
			getExpandedChildPaths(tree, child, currentPath, expandedPaths);
		}
		return expandedPaths;
	}

	private int getInsertIndex(DefaultMutableTreeNode parent) {
		int insertIndex = -1;
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (!(((DefaultMutableTreeNode)parent.getChildAt(i)).getUserObject() instanceof DOFolder)) {
				insertIndex = i;
				break;
			}
		}

		if (insertIndex == -1) {
			insertIndex = parent.getChildCount();
		}
		return insertIndex;
	}

	@Override
	public String toString() {
		return getClass().getName();
	}

	public class NodesTransferable implements Transferable {
		DefaultMutableTreeNode node;

		public NodesTransferable(DefaultMutableTreeNode node) {
			this.node = node;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
			return node;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return nodesFlavor.equals(flavor);
		}
	}
}