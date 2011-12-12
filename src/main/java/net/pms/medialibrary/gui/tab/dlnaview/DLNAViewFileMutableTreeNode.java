package net.pms.medialibrary.gui.tab.dlnaview;

import javax.swing.tree.DefaultMutableTreeNode;

import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.FileDisplayProperties;

public class DLNAViewFileMutableTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	private DOFileInfo fileInfo;
	private FileDisplayProperties displayProperties;
	
	public DLNAViewFileMutableTreeNode(){
		this(new DOFileInfo(), new FileDisplayProperties());
	}
	
	public DLNAViewFileMutableTreeNode(DOFileInfo fileInfo, FileDisplayProperties displayProperties){
		super(fileInfo.getDisplayString(displayProperties.getDisplayNameMask()), true);
		setFileInfo(fileInfo);
		setDisplayProperties(displayProperties);
	}

	public void setFileInfo(DOFileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public DOFileInfo getFileInfo() {
		return fileInfo;
	}

	public void setDisplayProperties(FileDisplayProperties displayProperties) {
		this.displayProperties = displayProperties;
	}

	public FileDisplayProperties getDisplayProperties() {
		return displayProperties;
	}
}
