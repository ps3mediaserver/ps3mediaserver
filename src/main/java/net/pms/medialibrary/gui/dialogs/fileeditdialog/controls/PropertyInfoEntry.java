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
package net.pms.medialibrary.gui.dialogs.fileeditdialog.controls;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.gui.shared.JHeader;

public class PropertyInfoEntry extends JPanel {
	private static final long serialVersionUID = 6146935074550677958L;

	private JHeader hTitle;
	private JTextField tfValue;
	private JComboBox cbValue;
	
	public PropertyInfoEntry(String value, ConditionType ct, boolean isConfirmEdit) {
		setLayout(new BorderLayout(0, 1));

		hTitle = new JHeader(ct, isConfirmEdit);
		tfValue = new JTextField(value);
		tfValue.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				hTitle.setSelected(true);
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				hTitle.setSelected(true);
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				hTitle.setSelected(true);
			}
		});
		build();
	}
	
	public String getText() {
		String res;
		if(cbValue != null) {
			res = cbValue.getSelectedItem().toString();
		} else {
			res = tfValue.getText();
		}
		return res;
	}
	
	public boolean isSelected() {
		return hTitle.isSelected();
	}
	
	public void addValue(String value) {
		if(cbValue == null) {
			cbValue = new JComboBox();
			String val = tfValue.getText();
			if(!val.equals("")) {
				cbValue.addItem(val);
				cbValue.setSelectedIndex(0);
			}
			build();
		}
	}
	
	private void build() {
		removeAll();
		
		JPanel pTitle = new JPanel(new GridLayout());
		pTitle.setAlignmentY(LEFT_ALIGNMENT);
		pTitle.add(hTitle);
		add(pTitle, BorderLayout.NORTH);
		
		add(tfValue, BorderLayout.CENTER);
	}
}
