package net.pms.medialibrary.commons.events;

import java.util.EventObject;

import javax.swing.tree.DefaultMutableTreeNode;

public class NodeMovedActionEvent extends EventObject{
    private static final long serialVersionUID = 469170664615867584L;
	private DefaultMutableTreeNode[] nodesToRefresh;
	private DefaultMutableTreeNode moveNode;

	public NodeMovedActionEvent(Object sender, DefaultMutableTreeNode moveNode, DefaultMutableTreeNode[] nodesToRefresh) {
		super(sender);
		this.nodesToRefresh = nodesToRefresh;
		this.moveNode = moveNode;
	}

	public DefaultMutableTreeNode[] getNodesToRefresh() {
		return nodesToRefresh;
	}

	public DefaultMutableTreeNode getMoveNode() {
		return moveNode;
	}
}
