package net.pms.medialibrary.gui.dialogs.folderdialog;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.pms.medialibrary.commons.dataobjects.DOFileEntryFile;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFolder;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryPlugin;

public class TemplateTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = -6938006991556561827L;
	private ImageIcon         folderIcon;
	private ImageIcon         folderEntryIcon;
	private ImageIcon         fileFolderFileSingleIcon;
	private ImageIcon         fileFolderFileMultipleIcon;

	public TemplateTreeCellRenderer() {
		String iconsFolder = "/resources/images/";
		this.folderIcon = new ImageIcon(getClass().getResource(iconsFolder + "filefolder-16.png"));
		this.folderEntryIcon = new ImageIcon(getClass().getResource(iconsFolder + "filefolderentry-16.png"));
		this.fileFolderFileSingleIcon = new ImageIcon(getClass().getResource(iconsFolder + "filefolderfile_single-16.png"));
		this.fileFolderFileMultipleIcon = new ImageIcon(getClass().getResource(iconsFolder + "filefolderfile_multiple-16.png"));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		setIcon(getIconToDisplay(value));
		return this;
	}

	public Icon getIconToDisplay(Object value) {
		Icon icon = null;
		if (value instanceof DefaultMutableTreeNode) {
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			if (userObject instanceof DOFileEntryFolder) {
				icon = this.folderIcon;
			} else if (userObject instanceof DOFileEntryInfo) {
				icon = this.folderEntryIcon;
			} else if (userObject instanceof DOFileEntryFile) {
				DOFileEntryFile fef =(DOFileEntryFile)userObject;
				switch(fef.getFileDisplayMode()){
					case MULTIPLE:
						icon = this.fileFolderFileMultipleIcon;
					break;
						default:
							icon = this.fileFolderFileSingleIcon;
					break;
				}
			} else if (userObject instanceof DOFileEntryPlugin){
				DOFileEntryPlugin entry = (DOFileEntryPlugin)userObject;
				icon = entry.getPlugin().getTreeIcon();
			}
		}
		if(icon == null){
			icon = getIcon();
		}
	    return icon;
    }
}
