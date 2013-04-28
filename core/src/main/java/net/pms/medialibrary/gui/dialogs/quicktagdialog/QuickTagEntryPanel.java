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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOQuickTagEntry;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.KeyCombinationCBItem;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.KeyCombination;
import net.pms.medialibrary.gui.shared.TagNameComboBox;
import net.pms.medialibrary.gui.shared.TagValueComboBox;

public class QuickTagEntryPanel {
	
	private static KeyCombinationCBItem[] keyCombinations;
	
	private JTextField tfName;
	private TagNameComboBox cbTagName;
	private TagValueComboBox cbTagValue;
	private JComboBox cbKeyCombination;
	private JComboBox cbVirtualKey;
	private JButton bDelete;
	private List<ActionListener> deleteListseners = new ArrayList<ActionListener>();
	
	public QuickTagEntryPanel(DOQuickTagEntry entry, FileType fileType) {		
		//lazy-initialize key combinations
		if (keyCombinations == null) {
			keyCombinations = new KeyCombinationCBItem[] {
					new KeyCombinationCBItem(KeyCombination.Ctrl),
					new KeyCombinationCBItem(KeyCombination.Shift),
					new KeyCombinationCBItem(KeyCombination.Alt),
					new KeyCombinationCBItem(KeyCombination.CtrlShift),
					new KeyCombinationCBItem(KeyCombination.CtrlAlt),
					new KeyCombinationCBItem(KeyCombination.ShiftAlt),
					new KeyCombinationCBItem(KeyCombination.CtrlShiftAlt) };
		}
		
		init(entry, fileType);
	}
	
	public void addDeleteListener(ActionListener deleteListener) {
		deleteListseners.add(deleteListener);
	}

	private void init(DOQuickTagEntry entry, FileType fileType) {
		tfName = new JTextField(entry.getName());
		tfName.setEditable(true);
		
		cbTagValue = new TagValueComboBox(entry.getTagName());
		cbTagValue.setEditable(true);
		
		cbTagName = new TagNameComboBox(fileType);
		cbTagName.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cbTagName.getSelectedItem() != null) {
					cbTagValue.setTagName(cbTagName.getSelectedItem().toString());
				}
			}
		});
		cbTagName.setEditable(true);
		
		cbTagName.setSelectedItem(entry.getTagName());
		cbTagValue.setSelectedItem(entry.getTagValue());
		
		cbKeyCombination = new JComboBox(keyCombinations);
		cbKeyCombination.setSelectedItem(new KeyCombinationCBItem(entry.getKeyCombination()));
		
		List<String> virtualKeys = new ArrayList<String>();
		//add numbers
		for(int i = 0; i < 10; i++) {
			virtualKeys.add(String.valueOf(i));
		}
		//add chars
		for(int i = (int)'A'; i <= (int)'Z'; i++) {
			virtualKeys.add(String.valueOf((char)i));			
		}
		cbVirtualKey = new JComboBox(virtualKeys.toArray());
		cbVirtualKey.setSelectedItem(String.valueOf((char)entry.getKeyCode()));
		
		bDelete = new JButton();
		bDelete.setIcon(new ImageIcon(getClass().getResource("/resources/images/tp_remove.png")));
		bDelete.setToolTipText(Messages.getString("ML.QuickTagDialog.bRemoveToolTip"));
		bDelete.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				fireDelete();
			}
		});
	}

	public JTextField getTfName() {
		return tfName;
	}

	public JComboBox getCbTagName() {
		return cbTagName;
	}

	public JComboBox getCbTagValue() {
		return cbTagValue;
	}

	public JComboBox getCbKeyCombination() {
		return cbKeyCombination;
	}

	public JComboBox getCbVirtualKey() {
		return cbVirtualKey;
	}

	public JButton getbDelete() {
		return bDelete;
	}
	
	public DOQuickTagEntry getQuickTagEntry() {		
		DOQuickTagEntry res = new DOQuickTagEntry();
		res.setName(tfName.getText());
		if(cbTagName.getSelectedItem() != null) res.setTagName(cbTagName.getSelectedItem().toString());
		if(cbTagValue.getSelectedItem() != null) res.setTagValue(cbTagValue.getSelectedItem().toString());
		if(cbVirtualKey.getSelectedItem() != null) res.setKeyCode((int)cbVirtualKey.getSelectedItem().toString().charAt(0));
		if(cbKeyCombination.getSelectedItem() != null) res.setKeyCombination(((KeyCombinationCBItem)cbKeyCombination.getSelectedItem()).getKeyCombination());
		
		return res;
	}

	private void fireDelete() {
		for(ActionListener l : deleteListseners) {
			l.actionPerformed(new ActionEvent(this, 0, "delete"));
		}
	}
}
