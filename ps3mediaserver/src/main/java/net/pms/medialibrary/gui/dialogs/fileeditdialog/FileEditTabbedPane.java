package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOAudioFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOImageFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.exceptions.ConditionTypeException;
import net.pms.medialibrary.commons.interfaces.IFilePropertiesEditor;

/**
 * Groups the info, properties and tags panels in a tabbed pane.
 * Different panels will be created depending on the file type of the file info
 * @author pw
 *
 */
public class FileEditTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = -4181083393313495546L;

	private JPanel infoPanel;
	private JPanel propertiesPanel;
	private FileTagsPanel tagsPanel;
	
	private DOFileInfo fileInfo;
	private boolean isMultiEdit;
	
	/**
	 * Constructor
	 * @param fileInfo the file info to edit
	 * @param b 
	 */
	public FileEditTabbedPane(DOFileInfo fileInfo, boolean isMultiEdit) {
		this.isMultiEdit = isMultiEdit;
		setContent(fileInfo);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent paramComponentEvent) {
				((IFilePropertiesEditor) propertiesPanel).build();
			}
		});
	}

	/**
	 * Updates the GUI to show the file info
	 * @param fileInfo the file info to edit
	 */
	public void setContent(DOFileInfo fileInfo) {	
		this.fileInfo = fileInfo;
		setContent();
	}

	/**
	 * Initializes the components and builds the panel
	 */
	private void setContent() {		
		infoPanel = new JPanel();
		propertiesPanel = new JPanel();
		tagsPanel = new FileTagsPanel(isMultiEdit ? new HashMap<String, List<String>>() : fileInfo.getTags(), true);
		
		if(fileInfo instanceof DOVideoFileInfo) {
			infoPanel = new VideoFileInfoPanel((DOVideoFileInfo) fileInfo);
			propertiesPanel = new VideoFilePropertiesPanel(isMultiEdit ? new DOVideoFileInfo() : (DOVideoFileInfo) fileInfo, isMultiEdit);
		} else if(fileInfo instanceof DOAudioFileInfo) {
			//TODO: implement
		} else if(fileInfo instanceof DOImageFileInfo) {
			//TODO: implement
		}
		
		//store the selected tab before removing all the tabs to re-add them
		int selectedIndex = getSelectedIndex();
		
		//remove all tab before restoring them
		removeAll();

		if(isMultiEdit) {
			addTab(Messages.getString("ML.FileEditTabbedPane.tProperties"), propertiesPanel);
			addTab(Messages.getString("ML.FileEditTabbedPane.tTags"), tagsPanel);
		} else {
			addTab(Messages.getString("ML.FileEditTabbedPane.tInfo"), infoPanel);
			addTab(Messages.getString("ML.FileEditTabbedPane.tProperties"), propertiesPanel);
			addTab(Messages.getString("ML.FileEditTabbedPane.tTags"), tagsPanel);			
		}
		
		//restore the selected tab
		if(selectedIndex > 0) {
			setSelectedIndex(selectedIndex);
		}
	}

	/**
	 * Updates all parameters and tags of the file info as they are configured in the GUI
	 * @return
	 * @throws ConditionTypeException
	 */
	public DOFileInfo getUpdatedFileInfo() throws ConditionTypeException {
		//update the file properties
		((IFilePropertiesEditor) propertiesPanel).updateFileInfo(fileInfo);
		fileInfo.setTags(tagsPanel.getTags());
		
		return fileInfo;
	}
	
	public List<ConditionType> getPropertiesToUpdate() {
		return ((IFilePropertiesEditor) propertiesPanel).getPropertiesToUpdate();	
	}
}
