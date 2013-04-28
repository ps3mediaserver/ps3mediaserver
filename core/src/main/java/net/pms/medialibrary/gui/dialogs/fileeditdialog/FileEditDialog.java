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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.helpers.FileImportHelper;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.commons.interfaces.FileEditLinkedList;
import net.pms.medialibrary.gui.dialogs.FileUpdateWithPluginDialog;
import net.pms.medialibrary.storage.MediaLibraryStorage;
import net.pms.plugins.FileImportPlugin;

/**
 * Dialog used to visualize and edit file properties and tags
 * @author pw
 *
 */
public class FileEditDialog extends JDialog {
	private static final long serialVersionUID = 2921067184273978956L;
	private final int MIN_BUTTON_WIDTH = 60;

	private EditMode editMode;
	private FileEditLinkedList fileEditList;
	private DOFileInfo fileInfo;
	
	private JButton bPrevious;
	private JButton bNext;
	private JButton bOk;
	private JButton bCancel;
	private JButton bImportWithPlugin;
	private FileEditTabbedPane tpFileEdit;
	private List<DOFileInfo> files;
      
	private boolean requiresUpdate = false;
	
	public enum EditMode {
		Single,
		Multiple,
		Linked
	}
	
	/**
	 * Listener used to be notified of file info changes in order to only save an updated configuration to DB if something has changed
	 */
	private ActionListener fileInfoChangedListener = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			requiresUpdate = true;
		}
	};
	
	private FileEditDialog(EditMode editMode, String title) {
		((java.awt.Frame) getOwner()).setIconImage(new ImageIcon(getClass().getResource("/resources/images/icon-16.png")).getImage());
		setTitle(title);
		setMinimumSize(new Dimension(400, 300));
		this.editMode = editMode;
	}
	
	/**
	 * Constructor used to edit a single file
	 * @param fileInfo
	 */
	public FileEditDialog(DOFileInfo fileInfo) {
		this(EditMode.Single, fileInfo.getFilePath());		
		this.fileInfo = fileInfo;
		build();
	}

	/**
	 * Constructor used to edit a single file, where forward/back buttons are being shown
	 * @param fel a linked list with a selected item which will be opened for editing
	 */
	public FileEditDialog(FileEditLinkedList fel) {
		this(EditMode.Linked, fel.getSelected().getFilePath());
		fileEditList = fel;
		build();
	}
	
	/**
	 * Constructor used to edit multiple files
	 * @param files
	 */
	public FileEditDialog(List<DOFileInfo> files) {	
		this(EditMode.Multiple, String.format("%s items being edited", files.size()));
		this.files = files;
		build();
	}
	
	private void init() {
		//initialize tabbed pane
		DOFileInfo fileToShow = null;
		switch(editMode) {
		case Single:
			fileToShow = fileInfo;
			fileToShow.addPropertyChangeListener(fileInfoChangedListener);
			break;
		case Multiple:
			fileToShow = new DOVideoFileInfo();
			break;
		case Linked:			
			fileToShow = fileEditList.getSelected();
			fileToShow.addPropertyChangeListener(fileInfoChangedListener);
			break;
		}
		
		if(fileToShow == null) {
			return;
		}

		tpFileEdit = new FileEditTabbedPane(fileToShow, editMode == EditMode.Multiple);		
		
		//initialize previous and next buttons
		bPrevious = new JButton(new ImageIcon(getClass().getResource("/resources/images/previous-16.png")));
		bPrevious.setToolTipText(Messages.getString("ML.FileEditDialog.bPrevious"));
		bPrevious.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean success = saveUpdatedFileInfo();
				if(success) {
					fileEditList.getSelected().removePropertyChangeListener(fileInfoChangedListener);
					fileEditList.selectPreviousFile();
					fileEditList.getSelected().addPropertyChangeListener(fileInfoChangedListener);
					tpFileEdit.setContent(fileEditList.getSelected());
					setTitle(fileEditList.getSelected().getFilePath());
					refreshButtonStates();
				}
			}
		});
		
		bNext = new JButton(new ImageIcon(getClass().getResource("/resources/images/next-16.png")));
		bNext.setToolTipText(Messages.getString("ML.FileEditDialog.bNext"));
		bNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean success = saveUpdatedFileInfo();
				if(success) {
					fileEditList.getSelected().removePropertyChangeListener(fileInfoChangedListener);
					fileEditList.selectNextFile();
					fileEditList.getSelected().addPropertyChangeListener(fileInfoChangedListener);
					tpFileEdit.setContent(fileEditList.getSelected());
					setTitle(fileEditList.getSelected().getFilePath());
					refreshButtonStates();
				}
			}
		});
		
		//initialize ok and cancel buttons
		bOk = new JButton(Messages.getString("ML.FileEditDialog.bOk"));
		if (bOk.getPreferredSize().width < MIN_BUTTON_WIDTH) bOk.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bOk.getPreferredSize().height));
		bOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean success = saveUpdatedFileInfo();
				if(success) {
					tpFileEdit.dispose();
					dispose();
				}
			}
		});
		
		bCancel = new JButton(Messages.getString("ML.FileEditDialog.bCancel"));
		if (bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		bCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tpFileEdit.dispose();
				dispose();
			}
		});
		
		bImportWithPlugin = new JButton(Messages.getString("ML.FileEditDialog.bImportWithPlugin"));
		if (bImportWithPlugin.getPreferredSize().width < MIN_BUTTON_WIDTH) bImportWithPlugin.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bImportWithPlugin.getPreferredSize().height));
		bImportWithPlugin.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateWithPlugin();
			}
		});
	}
	
	private void updateWithPlugin() {
		FileUpdateWithPluginDialog dialog = new FileUpdateWithPluginDialog(editMode == EditMode.Linked ? fileEditList.getSelected() : fileInfo);
		dialog.pack();
		dialog.setMinimumSize(dialog.getSize());
		dialog.setLocation(GUIHelper.getCenterDialogOnParentLocation(dialog.getSize(), bImportWithPlugin));
		dialog.setModal(true);
		dialog.setVisible(true);
		
		if(dialog.isUpdate()) {
			FileImportPlugin plugin = dialog.getPlugin();
			
			//get the updated file info
			DOFileInfo ff = getEditingFileInfo();
			DOFileInfo displayedFileInfo = tpFileEdit.getDisplayedFileInfo();
			displayedFileInfo.mergePropertiesAndTags(ff);
			FileImportHelper.updateFileInfo(plugin, displayedFileInfo);

			tpFileEdit.setContent(displayedFileInfo);
		}
	}

	/**
	 * Initializes the UI components and builds the panel
	 */
	private void build() {
		init();
		
		//build the panel
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, d, 3px, d, fill:10:grow, d, 30px, d, 3px, d, 3px", // columns
		        "3px, fill:10:grow, 3px, p, 3px"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(tpFileEdit, cc.xyw(2, 2, 9));
		
		if(editMode == EditMode.Linked) {
			builder.add(bPrevious, cc.xy(2, 4));
			builder.add(bNext, cc.xy(4, 4));
			builder.add(bImportWithPlugin, cc.xy(6, 4));
		} else if(editMode == EditMode.Single){
			builder.add(bImportWithPlugin, cc.xy(6, 4));
		}
		
		builder.add(bOk, cc.xy(8, 4));
		builder.add(bCancel, cc.xy(10, 4));

		getContentPane().add(builder.getPanel());

		refreshButtonStates();
	}
	
	private DOFileInfo getEditingFileInfo() {
		switch (editMode) {
		case Linked:
			return fileEditList.getSelected();
		case Single:
			return fileInfo;
		default:
			return null;
		}
	}
	
	/**
	 * Disables or enables the back and forward buttons if there is no next item
	 */
	private void refreshButtonStates() {
		if(editMode == EditMode.Linked) {
			bPrevious.setEnabled(fileEditList.hasPreviousFile());
			bNext.setEnabled(fileEditList.hasNextFile());
		}
	}

	/**
	 * Saves the file info as it's being displayed to the database
	 * @return true if the file has been saved
	 */
	private boolean saveUpdatedFileInfo() {
		DOFileInfo ff = getEditingFileInfo();
		tpFileEdit.updateFileInfo(ff);
		
		switch(editMode) {
		case Linked:
		case Single:			
			if(requiresUpdate) {
				MediaLibraryStorage.getInstance().updateFileInfo(ff);
				requiresUpdate = false;
			}
			break;
		case Multiple:
			List<ConditionType> propertiesToUpdate = tpFileEdit.getPropertiesToUpdate();
			for (DOFileInfo fiUpdate : files) {
				FileImportHelper.updateFileInfo(ff, fiUpdate, propertiesToUpdate);
				MediaLibraryStorage.getInstance().updateFileInfo(fiUpdate);
			}
 			break;
		}
		
		return true;
	}
	
	@Override
	public void dispose() {
		if(fileInfo != null) {
			fileInfo.removePropertyChangeListener(fileInfoChangedListener);
		}
		if(fileEditList != null && fileEditList.getSelected() != null) {
			fileEditList.getSelected().removePropertyChangeListener(fileInfoChangedListener);
		}
		
		super.dispose();
	}
}
