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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import net.pms.encoders.Player;

public class TreeNodeSettings extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -337606760204027449L;
	private Player p;
	private JComponent otherConfigPanel;
	private boolean enable = true;
	
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;

	}

	public Player getPlayer() {
		return p;
	}

	public TreeNodeSettings(String name, Player p, JComponent otherConfigPanel) {
		super(name);
		this.p = p;
		this.otherConfigPanel = otherConfigPanel;
		
	}
	
	public String id() {
		if (p != null)
			return p.id();
		else if (otherConfigPanel != null)
			return "" + otherConfigPanel.hashCode();
		else
			return null;
			
	}
	
	public JComponent getConfigPanel() {
		if (p != null) {
			return p.config();
		} else if (otherConfigPanel != null)
			return otherConfigPanel;
		else
			return new JPanel();
	}

}
