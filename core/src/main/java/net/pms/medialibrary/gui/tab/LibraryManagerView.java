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
package net.pms.medialibrary.gui.tab;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LibraryManagerView extends JPanel {
    private static final long serialVersionUID = -1315789010762303892L;

	public LibraryManagerView(){
		setLayout(new GridLayout());
		add(build());
	}
	
	private Component build(){
		FormLayout layout = new FormLayout("fill:10:grow", "fill:10:grow");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		CellConstraints cc = new CellConstraints();

		builder.add(new JLabel("****** Needs to be done ******"), cc.xy(1, 1, CellConstraints.CENTER, CellConstraints.CENTER));
		
		return builder.getPanel();		
	}
}
