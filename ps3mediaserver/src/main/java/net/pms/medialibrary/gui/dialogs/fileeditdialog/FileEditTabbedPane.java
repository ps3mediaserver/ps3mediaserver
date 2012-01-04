package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOAudioFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOImageFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.interfaces.IBuildable;

public class FileEditTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = -4181083393313495546L;
	private DOFileInfo fileInfo;

	private JPanel infoPanel;
	private JPanel propertiesPanel;
	private JPanel tagsPanel;

	public FileEditTabbedPane(DOFileInfo fileInfo) {
		setContent(fileInfo);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent paramComponentEvent) {
				((IBuildable) propertiesPanel).build();
			}
		});
	}

	public DOFileInfo getFileInfo() {
		return fileInfo;
	}

	public void setContent(DOFileInfo fileInfo) {
		this.fileInfo = fileInfo;
		
		infoPanel = new JPanel();
		propertiesPanel = new JPanel();
		tagsPanel = new JPanel();
		if(fileInfo instanceof DOVideoFileInfo) {
			infoPanel = new VideoFileInfoPanel((DOVideoFileInfo) fileInfo);
			propertiesPanel = new VideoFilePropertiesPanel((DOVideoFileInfo) fileInfo);
		} else if(fileInfo instanceof DOAudioFileInfo) {
			//TODO: implement
		} else if(fileInfo instanceof DOImageFileInfo) {
			//TODO: implement
		}
		
		//store the selected tab before removing all the tabs to re-add them
		int selectedIndex = getSelectedIndex();
		
		removeAll();
		addTab(Messages.getString("ML.FileEditTabbedPane.tInfo"), infoPanel);
		addTab(Messages.getString("ML.FileEditTabbedPane.tProperties"), propertiesPanel);
		addTab(Messages.getString("ML.FileEditTabbedPane.tTags"), tagsPanel);
		
		if(selectedIndex > 0) {
			setSelectedIndex(selectedIndex);
		}
	}
}
