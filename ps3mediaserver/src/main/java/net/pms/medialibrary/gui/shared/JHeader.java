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
package net.pms.medialibrary.gui.shared;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.ConditionType;

public class JHeader extends JComponent {
	private static final long serialVersionUID = 4201268643022810386L;
	
	private JCheckBox cbTitle;
	private JLabel lTitle;

	public JHeader(ConditionType ct) {
		this(ct, false);
	}

	public JHeader(ConditionType ct, boolean isConfirmEdit) {
		this(Messages.getString("ML.Condition.Header.Type." + ct) + ":", isConfirmEdit);
	}

	public JHeader(String text) {
		this(text, false);
	}
	
	public JHeader(String text, boolean isConfirmEdit) {
		this(text, isConfirmEdit, false);
	}
	
	public JHeader(String text, boolean isConfirmEdit, boolean setSelected) {
		setLayout(new BorderLayout());
		JComponent cp;
		
		cbTitle = new JCheckBox(text);
		cbTitle.setSelected(setSelected);
		lTitle = new JLabel(text);
		
		if(isConfirmEdit) {
			cp = cbTitle;
		} else {
			cp = lTitle;
		}
		cp.setFont(cp.getFont().deriveFont(Font.BOLD));
		add(cp, BorderLayout.LINE_START);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		cbTitle.setEnabled(enabled);
		lTitle.setEnabled(enabled);
	}
	
	public void setSelected(boolean selected) {
		if(cbTitle != null) {
			cbTitle.setSelected(selected);
		}
	}

	public boolean isSelected() {
		if(cbTitle != null) {
			return cbTitle.isSelected();
		}
		return true;
	}

	public void addChangeListener(ChangeListener l) {
		cbTitle.addChangeListener(l);
	}
}
