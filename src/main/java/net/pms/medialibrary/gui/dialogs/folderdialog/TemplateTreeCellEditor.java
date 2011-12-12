package net.pms.medialibrary.gui.dialogs.folderdialog;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.pms.medialibrary.commons.dataobjects.DOFileEntryBase;

public class TemplateTreeCellEditor extends DefaultTreeCellEditor {
	DefaultMutableTreeNode editingNode;

	public TemplateTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
		super(tree, renderer);
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		if (value instanceof DefaultMutableTreeNode) {
			editingNode = (DefaultMutableTreeNode) value;
		}

		Component c = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);

		editingIcon = ((TemplateTreeCellRenderer) renderer).getIconToDisplay(value);
		return c;
	}

	@Override
	public Object getCellEditorValue() {
		DOFileEntryBase file = null;
		if (editingNode != null && realEditor != null) {
			if (editingNode.getUserObject() instanceof DOFileEntryBase) {
				file = (DOFileEntryBase) editingNode.getUserObject();
				file.setDisplayNameMask(realEditor.getCellEditorValue().toString());
			}
		}
		editingNode = null;
		return file;
	}
}
