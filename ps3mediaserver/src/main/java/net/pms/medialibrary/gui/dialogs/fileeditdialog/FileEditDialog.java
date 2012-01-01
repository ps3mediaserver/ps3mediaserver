package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import javax.swing.JDialog;

import net.pms.medialibrary.commons.dataobjects.DOFileInfo;

import com.jgoodies.binding.list.SelectionInList;

public class FileEditDialog extends JDialog {
	private static final long serialVersionUID = 2921067184273978956L;
	private DOFileInfo fileInfo;
	private SelectionInList<DOFileInfo> selectionInList;

	public FileEditDialog(DOFileInfo fileInfo, SelectionInList<DOFileInfo> selectionInList) {
		setTitle(fileInfo.getFilePath());
		
		setFileInfo(fileInfo);
		setSelectionInList(selectionInList);
	}

	public DOFileInfo getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(DOFileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public SelectionInList<DOFileInfo> getSelectionInList() {
		return selectionInList;
	}

	public void setSelectionInList(SelectionInList<DOFileInfo> selectionInList) {
		this.selectionInList = selectionInList;
	}
}
