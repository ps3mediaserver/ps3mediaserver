package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOAudioFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOImageFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;

public class FileEditTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = -4181083393313495546L;
	private DOFileInfo fileInfo;

	public FileEditTabbedPane(DOFileInfo fileInfo) {
		setFileInfo(fileInfo);
		
		JPanel infoPanel = new JPanel();
		JPanel propertiesPanel = new JPanel();
		JPanel tagsPanel = new JPanel();
		if(fileInfo instanceof DOVideoFileInfo) {
			infoPanel = new VideoFileInfoPanel((DOVideoFileInfo) fileInfo);
		} else if(fileInfo instanceof DOAudioFileInfo) {
			//TODO: implement
		} else if(fileInfo instanceof DOImageFileInfo) {
			//TODO: implement
		}
		
		addTab(Messages.getString("ML.FileEditTabbedPane.tInfo"), infoPanel);
		addTab(Messages.getString("ML.FileEditTabbedPane.tProperties"), propertiesPanel);
		addTab(Messages.getString("ML.FileEditTabbedPane.tTags"), tagsPanel);
	}

	public DOFileInfo getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(DOFileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}
}
