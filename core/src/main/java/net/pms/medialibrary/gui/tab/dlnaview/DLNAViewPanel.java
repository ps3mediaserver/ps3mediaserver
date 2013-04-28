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

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.events.LibraryShowListener;
import net.pms.medialibrary.storage.MediaLibraryStorage;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DLNAViewPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JCheckBox cbDisplayItems;
	private DLNAViewTree tree;
	private LibraryShowListener libraryShowListener;
	
	/**  
	*   DLNAViewPanel constructor
	*   @param mediaLibraryStorage storage that will be used for saving the folder structure as well as displaying folders and items 
	*/  
	public DLNAViewPanel(){
		super(new GridLayout());
		
		MediaLibraryStorage mediaLibraryStorage = MediaLibraryStorage.getInstance();

		// create the tree structure from stored folders
		DOMediaLibraryFolder rootFolder = mediaLibraryStorage.getMediaLibraryFolder(MediaLibraryStorage.ROOT_FOLDER_ID, MediaLibraryStorage.ALL_CHILDREN);
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootFolder);
		tree = new DLNAViewTree(rootNode, mediaLibraryStorage);
		tree.setEditable(true);
		tree.setLibraryShowListener(new LibraryShowListener() {
			
			@Override
			public void show(DOFilter filter, FileType fileType) {
				if(libraryShowListener != null){
					libraryShowListener.show(filter, fileType);
				}
			}
		});

		cbDisplayItems = new JCheckBox(Messages.getString("ML.DLNAViewPanel.cbDisplayItems"));
		cbDisplayItems.setSelected(false);
		cbDisplayItems.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateDisplayItems();
			}
		});
		
		
		
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("fill:10:grow", // columns
			"p, 2dlu, fill:10:grow"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		builder.add(cbDisplayItems, cc.xy(1, 1));

        JScrollPane treeViewScrollPane = new JScrollPane(tree); 
		builder.add(treeViewScrollPane, cc.xy(1, 3));
		
		add(builder.getPanel());
	}
	
	public void setLibraryShowListener(LibraryShowListener libraryShowListener){
		this.libraryShowListener = libraryShowListener;
	}
	
	private void updateDisplayItems(){
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		tree.setDisplayItems(cbDisplayItems.isSelected());
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
}
