package net.pms.medialibrary.gui.tab.libraryview;

import javax.swing.table.DefaultTableCellRenderer;

public class FileDisplayTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -7619527933444988562L;

	public FileDisplayTableCellRenderer() {
		//align all cell content to the left
		setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
	}
}
