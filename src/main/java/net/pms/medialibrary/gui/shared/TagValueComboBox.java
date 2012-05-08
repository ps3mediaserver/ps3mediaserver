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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import net.pms.medialibrary.storage.MediaLibraryStorage;

public class TagValueComboBox extends JComboBox {
	private static final long serialVersionUID = 8015993256438845569L;
	private String tagName;

	public TagValueComboBox(String tagName) {
		setTagName(tagName);
	}

	public void setTagName(String tagName) {
		if(this.tagName == null || !this.tagName.equals(tagName)) {
			this.tagName = tagName;
			refreshItems();
		}
	}
	
	private void refreshItems() {
		List<String> tagValues = MediaLibraryStorage.getInstance().getTagValues(tagName, true, 1);
		setModel(new DefaultComboBoxModel(tagValues.toArray()));
	}
}
