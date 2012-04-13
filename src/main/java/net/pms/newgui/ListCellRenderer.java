/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
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
package net.pms.newgui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class ListCellRenderer implements javax.swing.ListCellRenderer {
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus) {
		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
			isSelected, cellHasFocus);

		if (index == 0) {
			renderer.setFont(renderer.getFont().deriveFont(Font.BOLD));
		}
		return renderer;
	}
}
