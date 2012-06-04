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
package net.pms.medialibrary.gui.tab.dlnaview;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import net.pms.medialibrary.commons.dataobjects.DOFolder;

public class DLNAViewTreeCellEditor extends DefaultTreeCellEditor {
	private DefaultMutableTreeNode editingNode;

	public DLNAViewTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
		super(tree, renderer);
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		if (value instanceof DefaultMutableTreeNode
				&& ((DefaultMutableTreeNode)value).getUserObject() instanceof DOFolder) {
			editingNode = (DefaultMutableTreeNode) value;
		}

		Component c = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);

		editingIcon = ((DLNAViewTreeCellRenderer) renderer).getIconToDisplay(value);
		return c;
	}

	@Override
	public Object getCellEditorValue() {
		Object res = null;
		
    	if(editingNode != null && editingNode.getUserObject() instanceof DOFolder) {
    		DOFolder f = (DOFolder)editingNode.getUserObject();
    		f.setName(realEditor.getCellEditorValue().toString());
    		res = f;
    	}
    	
    	DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    	for(TreeModelListener l : model.getListeners(TreeModelListener.class)){
    		Object[] children = new Object[1];
    		children[0] = editingNode;
    		l.treeNodesChanged(new TreeModelEvent(this, editingNode.getPath(), new int[1], children));
    	}

		editingNode = null;
		return res;
	}
	
	@Override
	public boolean isCellEditable(EventObject event) {
		return event == null || 
        		((((DLNAViewTree)event.getSource()).getSelectedNode() != null 
        				&& ((DLNAViewTree)event.getSource()).getSelectedNode().getUserObject() instanceof DOFolder));
	}
}
