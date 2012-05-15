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

import java.util.List;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class MultiselectJlist extends JList {
	private static final long serialVersionUID = -4099191706242329704L;

	public MultiselectJlist(List<?> items) {
		super(items.toArray());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setSelectionModel(new DefaultListSelectionModel() {
			private static final long serialVersionUID = 6067504797350429462L;

			@Override
			public void setSelectionInterval(int start, int end) {
				if(isSelectedIndex(start)) 
				{
				    removeSelectionInterval(start, end);
				}
				else 
				{
				    addSelectionInterval(start, end);
				}
			}
		});
	}
}
