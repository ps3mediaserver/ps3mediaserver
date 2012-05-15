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
package net.pms.medialibrary.gui.tab.dlnaview;

import javax.swing.JMenuItem;

import net.pms.plugins.DlnaTreeFolderPlugin;

public class SpecialFolderMenuItem extends JMenuItem {
    private static final long serialVersionUID = -2269678999237368235L;
    private DlnaTreeFolderPlugin specialFolder;

    public SpecialFolderMenuItem(DlnaTreeFolderPlugin f){
    	super(f.getName());
    	setSpecialFolder(f);
    }

	public void setSpecialFolder(DlnaTreeFolderPlugin specialFolder) {
	    this.specialFolder = specialFolder;
    }

	public DlnaTreeFolderPlugin getSpecialFolder() {
	    return specialFolder;
    }
}
