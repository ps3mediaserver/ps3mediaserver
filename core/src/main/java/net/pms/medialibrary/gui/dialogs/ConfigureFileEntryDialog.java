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
package net.pms.medialibrary.gui.dialogs;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryBase;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFile;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFolder;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryInfo;
import net.pms.medialibrary.commons.enumarations.DialogActionType;
import net.pms.medialibrary.commons.enumarations.FileDisplayType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.events.FilterFileDialogDialogEventArgs;
import net.pms.medialibrary.commons.events.ConfigureFileDialogListener;
import net.pms.medialibrary.gui.dialogs.folderdialog.FolderDialog;
import net.pms.medialibrary.gui.shared.FileDisplayPanel;

public class ConfigureFileEntryDialog extends JDialog {
	private static final Logger log = LoggerFactory.getLogger(ConfigureFileEntryDialog.class);
	private static final long                 serialVersionUID          = 5418437600836118875L;
	private final int MIN_BUTTON_WIDTH = 60;
	private FileDisplayPanel                  pFilterFile;
	private JPanel                            pButtons;
	private List<ConfigureFileDialogListener> filterFileDialogListeners = new ArrayList<ConfigureFileDialogListener>();
	private FileDisplayType                   fileEntryType;
	private DOFileEntryFolder                 parent;

	public ConfigureFileEntryDialog(DOFileEntryBase fileEntry, DOFileEntryFolder parent, FileType fileType) {
		((java.awt.Frame) this.getOwner()).setIconImage(new ImageIcon(FolderDialog.class.getResource("/resources/images/icon-16.png")).getImage());
		setMinimumSize(new Dimension(600, 0));
		pFilterFile = new FileDisplayPanel(fileEntry, fileType);
		fileEntryType = fileEntry.getFileEntryType();
		this.parent = parent;
		switch (fileEntryType) {
			case INFO:
				pFilterFile.setFileDisplayModeVisible(false);
				break;
			case FILE:
				// do nothing
				break;
			case FOLDER:
				pFilterFile.setFileDisplayModeVisible(false);
				pFilterFile.setThumbnailPrioVisible(false);
				break;
			case UNKNOWN:
				// do nothing
				break;
		default:
			log.warn(String.format("Unhandled file entry type received (%s). This should never happen!", fileEntryType));
			break;
		}
		setTitle(Messages.getString("ML.ConfigureFileEntryDialog.Title." + fileEntryType));

		JButton bOk = new JButton(Messages.getString("ML.ConfigureFileEntryDialog.bOk"));
		if(bOk.getPreferredSize().width < MIN_BUTTON_WIDTH) bOk.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bOk.getPreferredSize().height));
		bOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				fireFilterFileDialogEvent(DialogActionType.OK);
			}
		});
		JButton bCancel = new JButton(Messages.getString("ML.ConfigureFileEntryDialog.bCancel"));
		if(bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		bCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				fireFilterFileDialogEvent(DialogActionType.CANCEL);
			}
		});
		pFilterFile.setThumbnailPriorities(fileEntry.getThumbnailPriorities());

		pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pButtons.add(bOk);
		pButtons.add(bCancel);

		refreshDialog();
	}

	public void addConfigureFileDialogDialogListener(ConfigureFileDialogListener l) {
		filterFileDialogListeners.add(l);
	}

	private void fireFilterFileDialogEvent(DialogActionType actionType) {
		DOFileEntryBase fileEntry = null;
		switch (fileEntryType) {
			case FILE:
				fileEntry = new DOFileEntryFile(pFilterFile.getFileDisplayMode(), -1, parent, 0, pFilterFile.getDisplaynameMask(), pFilterFile.getThumbnailPriorities(),
				        pFilterFile.getMaxLineLength());
				break;
			case INFO:
				fileEntry = new DOFileEntryInfo(-1, parent, 0, pFilterFile.getDisplaynameMask(), pFilterFile.getThumbnailPriorities(), pFilterFile.getMaxLineLength());
				break;
			case FOLDER:
				fileEntry = new DOFileEntryFolder(new ArrayList<DOFileEntryBase>(), -1, parent, 0, pFilterFile.getDisplaynameMask(),
				        pFilterFile.getThumbnailPriorities(), pFilterFile.getMaxLineLength());
				break;
			default:
				log.warn(String.format("Unhandled file entry type received (%s). This should never happen!", fileEntryType));
				break;
		}

		for (ConfigureFileDialogListener l : filterFileDialogListeners) {
			l.configureFileDialogAction(new FilterFileDialogDialogEventArgs(this, actionType, fileEntry));
		}
	}

	private void refreshDialog() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, fill:300:grow, 3px", // columns
		        "3px, fill:p:grow, p, p"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(pFilterFile, cc.xy(2, 2));
		builder.addSeparator("", cc.xy(2, 3));
		builder.add(pButtons, cc.xy(2, 4, CellConstraints.FILL, CellConstraints.CENTER));

		add(builder.getPanel());
	}
	
	@Override
	public void pack(){
		super.pack();
		setMinimumSize(getSize());
	}
}
