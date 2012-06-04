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
package net.pms.plugin.dlnatreefolder.fsfp.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.pms.plugin.dlnatreefolder.FileSystemFolderPlugin;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A FolderEntryPanel represents one shared folder showing up
 * with a text field and a button to browse for a folder
 * and one to delete the shared folder
 */
public class FolderEntryPanel extends JPanel {
	private static final long    serialVersionUID = 436739054166843859L;
	private JTextField           tfFolderPath;
	private JButton              bBrowse;
	private JButton              bRemove;
	private List<ActionListener> removeListeners  = new ArrayList<ActionListener>();

	/**
	 * Instantiates a new folder entry panel with an empty folder path
	 */
	public FolderEntryPanel() {
		this("");
	}

	/**
	 * Instantiates a new folder entry panel with the given folder path
	 *
	 * @param folderPath the folder path
	 */
	public FolderEntryPanel(String folderPath) {
		setLayout(new GridLayout());

		init();
		rebuildPanel();

		setFolderPath(folderPath);
	}

	/**
	 * Gets the folder path.
	 *
	 * @return the folder path
	 */
	public String getFolderPath() {
		return tfFolderPath.getText();
	}

	/**
	 * Sets the folder path.
	 *
	 * @param path the new folder path
	 */
	public void setFolderPath(String path) {
		tfFolderPath.setText(path);
	}

	/**
	 * Initializes the graphical components
	 */
	private void init() {
		tfFolderPath = new JTextField();

		bBrowse = new JButton(FileSystemFolderPlugin.messages.getString("FolderEntry.1"));
		bBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				if (fc.showOpenDialog(getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
					tfFolderPath.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});

		bRemove = new JButton(new ImageIcon(getClass().getResource("/resources/images/tp_remove.png")));
		bRemove.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireRemoveListener();
			}
		});
	}

	/**
	 * Adds the action listener which will be notified
	 * when a shared folder requests deletion
	 *
	 * @param l the listener
	 */
	public void addRemoveListener(ActionListener l) {
		removeListeners.add(l);
	}

	/**
	 * Notifies all remove listeners of a deletion request
	 */
	private void fireRemoveListener() {
		for (ActionListener l : removeListeners) {
			l.actionPerformed(new ActionEvent(this, 0, ""));
		}
	}

	/**
	 * Rebuild the panel.
	 */
	private void rebuildPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("p, 3px, fill:10:grow, 3px, p, 3px, p", // columns
		        "p"); // raws
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.addLabel(FileSystemFolderPlugin.messages.getString("FolderEntry.2"), cc.xy(1, 1));
		builder.add(tfFolderPath, cc.xy(3, 1));
		builder.add(bBrowse, cc.xy(5, 1));
		builder.add(bRemove, cc.xy(7, 1));

		removeAll();
		add(builder.getPanel());
	}
}
