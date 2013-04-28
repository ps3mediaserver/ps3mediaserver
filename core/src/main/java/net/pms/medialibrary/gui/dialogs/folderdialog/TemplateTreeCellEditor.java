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
