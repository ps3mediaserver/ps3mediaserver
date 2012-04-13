package net.pms.medialibrary.gui.shared;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class TagNameComboBox extends JComboBox{
	private static final long serialVersionUID = -4167648170021609597L;
	private FileType fileType;

	public TagNameComboBox(FileType fileType) {
		setFileType(fileType);
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		if(this.fileType == null || this.fileType != fileType) {
			this.fileType = fileType;
			refreshItems();
		}
	}
	
	private void refreshItems() {
		List<String> tagNames = MediaLibraryStorage.getInstance().getExistingTags(fileType);
		setModel(new DefaultComboBoxModel(tagNames.toArray()));
	}
}
