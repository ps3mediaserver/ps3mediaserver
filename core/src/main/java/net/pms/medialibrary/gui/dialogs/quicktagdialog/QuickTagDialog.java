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
package net.pms.medialibrary.gui.dialogs.quicktagdialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOQuickTagEntry;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class QuickTagDialog extends JDialog {
	private static final long serialVersionUID = 1623280241555707047L;

	private final int MIN_BUTTON_WIDTH = 60;
	
	private JPanel pButtons;
	private JPanel pTitle;
	private QuickTagEntriesPanel pQuickTagEntries;
	
	private FileType fileType;

	public QuickTagDialog(FileType fileType) {
		((java.awt.Frame)getOwner()).setIconImage(new ImageIcon(getClass().getResource("/resources/images/icon-16.png")).getImage());
		setTitle(Messages.getString("ML.QuickTagDialog.Title"));
		
		List<DOQuickTagEntry> quickTagEntries = MediaLibraryStorage.getInstance().getQuickTagEntries();
		this.fileType = fileType;
		
		init(quickTagEntries);
		build();
	}

	private void init(List<DOQuickTagEntry> quickTagEntries) {
		//title
		pTitle = new JPanel(new BorderLayout());
		JLabel lTitle = new JLabel(Messages.getString("ML.QuickTagDialog.Header"));
		lTitle.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		lTitle.setFont(lTitle.getFont().deriveFont((float)lTitle.getFont().getSize() + 4));
		lTitle.setFont(lTitle.getFont().deriveFont(Font.BOLD));
		pTitle.add(lTitle, BorderLayout.LINE_START);
		
		JButton bAddQuickTag = new JButton();
		bAddQuickTag.setIcon(new ImageIcon(getClass().getResource("/resources/images/tp_add.png")));
		bAddQuickTag.setToolTipText(Messages.getString("ML.QuickTagDialog.bAddToolTip"));
		bAddQuickTag.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pQuickTagEntries.addQuickTagEntry(new DOQuickTagEntry());
			}
		});
		pTitle.add(bAddQuickTag, BorderLayout.LINE_END);
		pTitle.add(new JSeparator(), BorderLayout.SOUTH);
		
		//quick tags
		pQuickTagEntries = new QuickTagEntriesPanel(quickTagEntries, fileType);
		
		//buttons
		JButton bSave = new JButton(Messages.getString("ML.QuickTagDialog.bOk"));
		if(bSave.getPreferredSize().width < MIN_BUTTON_WIDTH) bSave.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bSave.getPreferredSize().height));
		bSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				close(true);
			}
		});

		JButton bApply = new JButton(Messages.getString("ML.QuickTagDialog.bSave"));
		if(bApply.getPreferredSize().width < MIN_BUTTON_WIDTH) bApply.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bApply.getPreferredSize().height));
		bApply.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		JButton bCancel = new JButton(Messages.getString("ML.QuickTagDialog.bCancel"));
		if(bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		bCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				close(false);
			}
		});

		pButtons = new JPanel();
		pButtons.add(bSave);
		pButtons.add(bApply);
		pButtons.add(bCancel);		
	}
	
	private void build() {
		setLayout(new BorderLayout());
		add(pTitle, BorderLayout.NORTH);
		add(pQuickTagEntries, BorderLayout.CENTER);
		add(pButtons, BorderLayout.SOUTH);
	}
	
	private void close(boolean doSave) {
		boolean doClose = true;
		if(doSave) {
			doClose = save();
		}
		
		if(doClose) {
			dispose();
		}
	}
	
	private boolean save() {
		List<DOQuickTagEntry> quickTagEntries = pQuickTagEntries.getQuickTagEntries();
		for(DOQuickTagEntry entry : quickTagEntries) {
			if(entry.getName().equals("")) {
				JOptionPane.showMessageDialog(this, Messages.getString("ML.QuickTagDialog.Message.NameEmpty"));
				return false;
			}
		}
		
		MediaLibraryStorage.getInstance().setQuickTagEntries(quickTagEntries);
		return true;
	}
}
