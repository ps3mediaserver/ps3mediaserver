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
package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.medialibrary.commons.dataobjects.DOAudioFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOImageFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.interfaces.IFilePropertiesEditor;
import net.pms.medialibrary.gui.dialogs.fileeditdialog.panels.FileCoverPanel;
import net.pms.medialibrary.gui.dialogs.fileeditdialog.panels.FileTagsPanel;
import net.pms.medialibrary.gui.dialogs.fileeditdialog.panels.VideoFileInfoPanel;
import net.pms.medialibrary.gui.dialogs.fileeditdialog.panels.VideoFilePropertiesPanel;
import net.pms.medialibrary.gui.dialogs.fileeditdialog.transferhandlers.FileCoverTransferHandler;

/**
 * Groups the info, properties and tags panels in a tabbed pane. Different
 * panels will be created depending on the file type of the file info
 * 
 * @author pw
 * 
 */
public class FileEditTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = -4181083393313495546L;
	private static final Logger log = LoggerFactory
			.getLogger(FileEditTabbedPane.class);

	private VideoFileInfoPanel pInfo;
	private JPanel pProperties;
	private FileTagsPanel tagsPanel;
	private FileCoverPanel coverPanel;

	private DOFileInfo fileInfo;
	private boolean isMultiEdit;

	/**
	 * Constructor
	 * 
	 * @param fileInfo the file info to edit
	 * @param isMultiEdit
	 */
	public FileEditTabbedPane(DOFileInfo fileInfo, boolean isMultiEdit) {
		this.isMultiEdit = isMultiEdit;
		setContent(fileInfo);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent paramComponentEvent) {
				((IFilePropertiesEditor) pProperties).build();
			}
		});
	}

	/**
	 * Updates the GUI to show the file info
	 * 
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
		pInfo = new VideoFileInfoPanel();
		pProperties = new JPanel();
		tagsPanel = new FileTagsPanel(isMultiEdit ? new HashMap<String, List<String>>() : fileInfo.getTags(), true);
		try {
			coverPanel = new FileCoverPanel(PMS.getConfiguration().getTempFolder().getAbsolutePath() + File.separator + FileCoverTransferHandler.TMP_FILENAME);
		} catch (IOException e) {
			log.error("Failed to get temp folder location", e);
		}

		if (fileInfo instanceof DOVideoFileInfo) {
			pInfo = new VideoFileInfoPanel((DOVideoFileInfo) fileInfo);
			pProperties = new VideoFilePropertiesPanel(isMultiEdit ? new DOVideoFileInfo() : (DOVideoFileInfo) fileInfo, isMultiEdit);
		} else if (fileInfo instanceof DOAudioFileInfo) {
			// TODO: implement
		} else if (fileInfo instanceof DOImageFileInfo) {
			// TODO: implement
		}

		// store the selected tab before removing all the tabs to re-add them
		int selectedIndex = getSelectedIndex();

		// remove all tab before restoring them
		removeAll();

		if (isMultiEdit) {
			addTab(Messages.getString("ML.FileEditTabbedPane.tProperties"), pProperties);
			addTab(Messages.getString("ML.FileEditTabbedPane.tTags"), tagsPanel);
			addTab(Messages.getString("ML.FileEditTabbedPane.tCover"), coverPanel);
		} else {
			addTab(Messages.getString("ML.FileEditTabbedPane.tInfo"), pInfo);
			addTab(Messages.getString("ML.FileEditTabbedPane.tProperties"), pProperties);
			addTab(Messages.getString("ML.FileEditTabbedPane.tTags"), tagsPanel);
		}

		// restore the selected tab
		if (selectedIndex > 0) {
			setSelectedIndex(selectedIndex);
		}
	}

	public DOFileInfo getDisplayedFileInfo() {
		DOFileInfo newFileInfo = null;
		if (fileInfo instanceof DOVideoFileInfo) {
			newFileInfo = new DOVideoFileInfo();
		} else if (fileInfo instanceof DOAudioFileInfo) {
			newFileInfo = new DOAudioFileInfo();
		} else if (fileInfo instanceof DOImageFileInfo) {
			newFileInfo = new DOImageFileInfo();
		} else if (fileInfo instanceof DOFileInfo) {
			newFileInfo = new DOFileInfo();
		}
		updateFileInfo(newFileInfo);
		return newFileInfo;
	}

	public void updateFileInfo(DOFileInfo fileInfo) {
		// update the file properties
		((IFilePropertiesEditor) pProperties).updateFileInfo(fileInfo);
		((IFilePropertiesEditor) pInfo).updateFileInfo(fileInfo);
		if (isMultiEdit) {
			coverPanel.updateFileInfo(fileInfo);
		}
		fileInfo.setTags(tagsPanel.getTags());
	}

	public List<ConditionType> getPropertiesToUpdate() {
		List<ConditionType> res = ((IFilePropertiesEditor) pProperties).getPropertiesToUpdate();
		if (isMultiEdit) {
			res.addAll(coverPanel.getPropertiesToUpdate());
		}

		return res;
	}

	public void dispose() {
		pInfo.dispose();
	}
}
