package net.pms.medialibrary.gui.tab.dlnaview;

import javax.swing.JMenuItem;

import net.pms.medialibrary.external.DlnaTreeFolderPlugin;

public class SpecialFolderMenuItem extends JMenuItem {
    private static final long serialVersionUID = -2269678999237368235L;
    private DlnaTreeFolderPlugin specialFolder;

    public SpecialFolderMenuItem(DlnaTreeFolderPlugin f){
    	super(f.getName());
    	setSpecialFolder(f);
    }

	public void setSpecialFolder(DlnaTreeFolderPlugin specialFolder) {
	    this.specialFolder = specialFolder;
    }

	public DlnaTreeFolderPlugin getSpecialFolder() {
	    return specialFolder;
    }
}
