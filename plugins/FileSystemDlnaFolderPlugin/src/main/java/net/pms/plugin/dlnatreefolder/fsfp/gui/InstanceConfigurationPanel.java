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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.pms.plugin.dlnatreefolder.FileSystemFolderPlugin;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The configuration panel for the {@link FileSystemFolderPlugin}.
 */
public class InstanceConfigurationPanel extends JPanel {
	private static final long serialVersionUID = -2950360909291954353L;
	private List<FolderEntryPanel> sharedFolders = new ArrayList<FolderEntryPanel>();
	
	private JButton bAddFolder;
	private JPanel pNoSharedFoldersSet;

	/**
	 * Instantiates a new configuration panel without any shared folders
	 */
	public InstanceConfigurationPanel() {
		this(new ArrayList<String>());
	}

	/**
	 * Instantiates a new configuration panel with shared folders
	 *
	 * @param folderPaths the folder paths
	 */
	public InstanceConfigurationPanel(List<String> folderPaths) {
		setLayout(new GridLayout());
		init();
		rebuildPanel();
	}

	/**
	 * Gets the shared folders.
	 *
	 * @return the folders
	 */
	public List<String> getFolders() {
		List<String> folders = new ArrayList<String>();
		for (FolderEntryPanel fe : sharedFolders) {
			if (!folders.contains(fe.getFolderPath())) {
				folders.add(fe.getFolderPath());
			}
		}
		return folders;
	}

	/**
	 * Sets the shared folders.
	 *
	 * @param folderPaths the new folders
	 */
	public void setFolders(List<String> folderPaths) {
		sharedFolders.clear();
		rebuildPanel();
		for (String folderPath : folderPaths) {
			addFolderEntry(folderPath);
		}
	}

	/**
	 * Initializes the graphical components
	 */
	private void init() {
		pNoSharedFoldersSet = new JPanel();
		pNoSharedFoldersSet.setLayout(new GridLayout());
		pNoSharedFoldersSet.add(new JLabel(FileSystemFolderPlugin.messages.getString("InstanceConfigurationPanel.1")));

		bAddFolder = new JButton(FileSystemFolderPlugin.messages.getString("InstanceConfigurationPanel.2"));
		rebuildPanel();
		bAddFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addFolderEntry("");
			}
		});
	}

	/**
	 * Adds a shared folder. A maximum of 20 are allowed
	 *
	 * @param folderPath the folder path
	 */
	private void addFolderEntry(String folderPath) {
		if (sharedFolders.size() >= 20) {
			JOptionPane.showMessageDialog(this, FileSystemFolderPlugin.messages.getString("InstanceConfigurationPanel.3"));
			return;
		}

		FolderEntryPanel fe = new FolderEntryPanel(folderPath);
		fe.addRemoveListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sharedFolders.remove(e.getSource());
				rebuildPanel();
			}
		});
		sharedFolders.add(fe);
		rebuildPanel();
	}

	/**
	 * Rebuilds the panel.
	 */
	private void rebuildPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		PanelBuilder conBuilder;
		FormLayout conLayout = new FormLayout("fill:10:grow", // columns
		        "p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, fill:10:grow"); // rows
		conBuilder = new PanelBuilder(conLayout);
		conBuilder.setOpaque(true);

		// Conditions
		if (sharedFolders.size() > 0) {
			// Add conditions if we've got any
			for (int i = 0; i < this.sharedFolders.size(); i++) {
				conBuilder.add(this.sharedFolders.get(i), cc.xy(1, i + 1));
			}
		} else {
			// Show the 'no shared folders set' label if there are none
			conBuilder.add(pNoSharedFoldersSet, cc.xy(1, 21, CellConstraints.CENTER, CellConstraints.CENTER));
		}

		FormLayout layout = new FormLayout("fill:10:grow", // columns
		        "fill:10:grow, 3px, p"); // raws
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		JScrollPane spConditions = new JScrollPane(conBuilder.getPanel());
		spConditions.setBorder(null);
		builder.add(spConditions, cc.xy(1, 1));

		JPanel pButton = new JPanel();
		pButton.add(bAddFolder);
		builder.add(pButton, cc.xy(1, 3));

		removeAll();
		add(builder.getPanel());
		validate();
	}
}
