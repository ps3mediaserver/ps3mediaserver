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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryPlugin;
import net.pms.medialibrary.commons.enumarations.DialogActionType;
import net.pms.medialibrary.commons.events.FileEntryPluginDialogActionEvent;
import net.pms.medialibrary.commons.events.FileEntryPluginDialogActionListener;
import net.pms.medialibrary.commons.events.SpecialFolderDialogActionListener;
import net.pms.medialibrary.gui.dialogs.folderdialog.FolderDialog;

public class FileEntryPluginDialog extends JDialog {
    private static final long serialVersionUID = -8915850418829416485L;
	private final int MIN_BUTTON_WIDTH = 60;
	private DOFileEntryPlugin                       pluginEntry;
	private JPanel                                  pButtons;
	private JTextField                              tfName;
	private boolean isNew;

	private List<FileEntryPluginDialogActionListener> dialogListeners  = new ArrayList<FileEntryPluginDialogActionListener>();

	public FileEntryPluginDialog(DOFileEntryPlugin pluginEntry, boolean isNew) {
		((java.awt.Frame)this.getOwner()).setIconImage(new ImageIcon(FolderDialog.class.getResource("/resources/images/icon-16.png")).getImage());
		init();

		setPluginEntry(pluginEntry);
		setIsNew(isNew);
	}

	public void addFileEntryPluginDialogActionListener(FileEntryPluginDialogActionListener l) {
		if (!dialogListeners.contains(l)) {
			dialogListeners.add(l);
		}
	}

	public void removeSpecialFolderDialogActionListener(SpecialFolderDialogActionListener l) {
		if (dialogListeners.contains(l)) {
			dialogListeners.remove(l);
		}
	}

	public void setPluginEntry(DOFileEntryPlugin plugin) {
		this.pluginEntry = plugin;
		tfName.setText(plugin.getDisplayNameMask());
		setTitle(plugin.getPlugin().getName());
		refreshDialog();
	}

	public DOFileEntryPlugin getPluginEntry() {
		return pluginEntry;
	}

	public void setIsNew(boolean isNew) {
	    this.isNew = isNew;
    }

	public boolean isNew() {
	    return isNew;
    }

	private void init() {
		tfName = new JTextField();

		JButton bSave = new JButton(Messages.getString("ML.SpecialFolderDialog.bSave"));
		if(bSave.getPreferredSize().width < MIN_BUTTON_WIDTH) bSave.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bSave.getPreferredSize().height));
		bSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireSpecialFolderDialogAction(DialogActionType.OK);
			}
		});

		JButton bApply = new JButton(Messages.getString("ML.SpecialFolderDialog.bApply"));
		if(bApply.getPreferredSize().width < MIN_BUTTON_WIDTH) bApply.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bApply.getPreferredSize().height));
		bApply.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireSpecialFolderDialogAction(DialogActionType.APPLY);
			}
		});

		JButton bCancel = new JButton(Messages.getString("ML.SpecialFolderDialog.bCancel"));
		if(bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		bCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireSpecialFolderDialogAction(DialogActionType.CANCEL);
			}
		});

		pButtons = new JPanel();
		pButtons.add(bSave);
		pButtons.add(bApply);
		pButtons.add(bCancel);
	}

	private void fireSpecialFolderDialogAction(DialogActionType actionType) {
		getPluginEntry().setDisplayNameMask(tfName.getText());
		FileEntryPluginDialogActionEvent e = new FileEntryPluginDialogActionEvent(this, getPluginEntry(), actionType, isNew());
		for (FileEntryPluginDialogActionListener l : dialogListeners) {
			l.fileEntryPluginDialogActionReceived(e);
		}
	}

	private void refreshDialog() {
		FormLayout layout = new FormLayout("3px, p, 3px, p:grow , 3px", 
				"3px, p, 3px, p, 3px, fill:p:grow, 3px, p, 3px, p, 3px");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();

		builder.addLabel(Messages.getString("ML.SpecialFolderDialog.lName"), cc.xy(2, 2));
		builder.add(tfName, cc.xy(4, 2));
		builder.addSeparator("", cc.xyw(2, 4, 3));

		builder.add(getPluginEntry().getPlugin().getConfigurationPanel(), cc.xyw(2, 6, 3));
		builder.addSeparator("", cc.xyw(2, 8, 3));
		builder.add(pButtons, cc.xyw(2, 10, 3));

		getContentPane().removeAll();
		getContentPane().add(builder.getPanel());
		validate();
	}
}
