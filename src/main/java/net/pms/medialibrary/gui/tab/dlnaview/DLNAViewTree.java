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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.medialibrary.commons.MediaLibraryConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryBase;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFolder;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryPlugin;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFolder;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.dataobjects.DOSpecialFolder;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.dataobjects.FileDisplayProperties;
import net.pms.medialibrary.commons.enumarations.AutoFolderProperty;
import net.pms.medialibrary.commons.enumarations.AutoFolderType;
import net.pms.medialibrary.commons.enumarations.CopyCutAction;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.MediaLibraryConstants;
import net.pms.medialibrary.commons.events.AutoFolderDialogActionEvent;
import net.pms.medialibrary.commons.events.AutoFolderDialogActionListener;
import net.pms.medialibrary.commons.events.FolderDialogFolderUpdateEvent;
import net.pms.medialibrary.commons.events.FolderDialogActionListener;
import net.pms.medialibrary.commons.events.NodeMovedActionEvent;
import net.pms.medialibrary.commons.events.NodeMovedActionListener;
import net.pms.medialibrary.commons.events.LibraryShowListener;
import net.pms.medialibrary.commons.events.SpecialFolderDialogActionEvent;
import net.pms.medialibrary.commons.events.SpecialFolderDialogActionListener;
import net.pms.medialibrary.commons.helpers.AutoFolderCreator;
import net.pms.medialibrary.commons.helpers.FileHelper;
import net.pms.medialibrary.commons.helpers.FolderComparator;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.gui.dialogs.AddAutoFolderDialog;
import net.pms.medialibrary.gui.dialogs.PluginFolderDialog;
import net.pms.medialibrary.gui.dialogs.folderdialog.FolderDialog;
import net.pms.medialibrary.storage.MediaLibraryStorage;
import net.pms.notifications.NotificationCenter;
import net.pms.notifications.NotificationSubscriber;
import net.pms.notifications.types.PluginEvent;
import net.pms.plugins.DlnaTreeFolderPlugin;
import net.pms.plugins.PluginsFactory;

public class DLNAViewTree extends JTree {
	private static final Logger log = LoggerFactory.getLogger(DLNAViewTree.class);
	private static final long      serialVersionUID           = -8908138387113406521L;
	private boolean                displayItems               = false;
	private IMediaLibraryStorage   mediaLibraryStorage;
	private JPopupMenu             contextMenu;
	private DefaultMutableTreeNode copyNode;
	private CopyCutAction          currentCutPasteOperation   = CopyCutAction.NONE;

	private JMenuItem              refreshItem;
	private JMenuItem              editItem;
	private JMenuItem              deleteItem;
	private JMenuItem              copyItem;
	private JMenuItem              cutItem;
	private JMenuItem              pasteMenuItem;
	private JMenuItem 			   showInLibraryItem;

	private Comparator<DOFolder>   positionInParentComparator = new FolderComparator();
	private JMenuItem              addAutoFolderItem;
	private JMenuItem              addFolderItem;
	private JMenu addMenu;
	private LibraryShowListener libraryShowListener;
	private JMenuItem setAsRootItem;

	public DLNAViewTree(DefaultMutableTreeNode rootNode, IMediaLibraryStorage storage) {
		super(rootNode);

		this.mediaLibraryStorage = storage;
		initContextMenu();
		initTreeView();
		initPluginChangeListener();
	}

	public void setDisplayItems(boolean value) {
		if (displayItems != value) {
			displayItems = value;
			refreshNode((DefaultMutableTreeNode) treeModel.getRoot());
		}
	}

	public boolean isDisplayItems() {
		return displayItems;
	}
	
	public void setLibraryShowListener(LibraryShowListener libraryShowListener){
		this.libraryShowListener = libraryShowListener;
	}

	private void initPluginChangeListener() {
		NotificationCenter.getInstance(PluginEvent.class).subscribe(new NotificationSubscriber<PluginEvent>() {			
			@Override
			public void onMessage(PluginEvent obj) {
				refreshAddMenu(true);
			}
		});
	}

	/**
	 * Initialize the tree view
	 */
	private void initTreeView() {
		addTreeExpansionListener(new TreeExpansionListener() {
			
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				setSelectionPath(event.getPath());
			}
			
			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				setSelectionPath(event.getPath());
			}
		});
		
		treeModel.addTreeModelListener(new TreeModelListener() {

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				if (e.getChildren() != null && e.getChildren().length > 0) {
					for (Object child : e.getChildren()) {
						if (child instanceof DefaultMutableTreeNode 
								&& ((DefaultMutableTreeNode)child).getUserObject() instanceof DOMediaLibraryFolder) {
							DOMediaLibraryFolder f = (DOMediaLibraryFolder) ((DefaultMutableTreeNode)child).getUserObject();
							f.setName(((DefaultMutableTreeNode)child).getUserObject().toString());
							mediaLibraryStorage.updateFolderDisplayName(f.getId(), f.getName());
						} else if (child instanceof DefaultMutableTreeNode) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) child;
							if (node.getUserObject() instanceof DOSpecialFolder) {
								DOSpecialFolder sf = (DOSpecialFolder) node.getUserObject();
								mediaLibraryStorage.updateFolderDisplayName(sf.getId(), sf.getName());
							}
						}
					}
				}
			}

			public void treeStructureChanged(TreeModelEvent e) {
			}

			public void treeNodesRemoved(TreeModelEvent e) {
			}

			public void treeNodesInserted(TreeModelEvent e) {
			}
		});
		if (treeModel.getRoot() != null 
				&& treeModel.getRoot() instanceof DefaultMutableTreeNode
				&& ((DefaultMutableTreeNode)treeModel.getRoot()).getUserObject() instanceof DOMediaLibraryFolder) {
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
			addChildrenToNode(rootNode, (DOMediaLibraryFolder)rootNode.getUserObject());
			expandPath(new TreePath(rootNode.getPath()));
		}

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setShowsRootHandles(true);

		// for drag & drop
		DLNAViewTreeTransferHandler transferHandler = new DLNAViewTreeTransferHandler();
		transferHandler.addNodeMovedActionListener(new NodeMovedActionListener() {

			@Override
			public void nodeMovedReceived(NodeMovedActionEvent e) {
				updateNodeIndexes(e.getNodesToRefresh());

				for (int i = 0; i < e.getNodesToRefresh().length; i++) {
					refreshNode(e.getNodesToRefresh()[i]);
				}

				refreshNode((DefaultMutableTreeNode) e.getMoveNode());
			}
		});
		setTransferHandler(transferHandler);
		setDragEnabled(true);
		setDropMode(DropMode.ON_OR_INSERT);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (getSelectionPath() != null && getSelectionPath().getLastPathComponent() != null
				        && getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode
				        && ((DefaultMutableTreeNode)getSelectionPath().getLastPathComponent()).getUserObject() instanceof DOFolder) {
					if (e.isControlDown()) {
						if (e.getKeyCode() == KeyEvent.VK_R) {
							refreshSelectedNode();
						} else if (e.getKeyCode() == KeyEvent.VK_C) {
							copySelectedNode();
						} else if (e.getKeyCode() == KeyEvent.VK_X) {
							cutSelectedNode();
						} else if (e.getKeyCode() == KeyEvent.VK_V) {
							pasteNode();
						} else if (e.getKeyCode() == KeyEvent.VK_N) {
							addNewFolder();
						} else if (e.getKeyCode() == KeyEvent.VK_E) {
							editSelectedNode();
						}
					} else {
						if (e.getKeyCode() == KeyEvent.VK_DELETE) {
							deleteSelectedNode();
						} else if (e.getKeyCode() == KeyEvent.VK_F5) {
							refreshSelectedNode();
						}
					}
				}
			}
		});

		// Initialize the renderer that will change the folder icons
		DLNAViewTreeCellRenderer renderer = new DLNAViewTreeCellRenderer();
		setCellRenderer(renderer);
		setCellEditor(new DLNAViewTreeCellEditor(this, renderer));

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				// Select the node for every mouse button
				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				setSelectionPath(selPath);
				DefaultMutableTreeNode selNode;

				// Get the selected node
				Object o = getLastSelectedPathComponent();
				if (o != null && (o instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) o).getUserObject() instanceof DOFolder)) {
					selNode = (DefaultMutableTreeNode) o;
				} else {
					selNode = null;
				}

				if (selNode != null) {
					if (e.getClickCount() == 1) {
						switch (e.getButton()) {
							case MouseEvent.BUTTON3:
								contextMenu.removeAll();

								if(selNode.getUserObject() instanceof DOSpecialFolder){
									contextMenu.add(editItem);
									contextMenu.add(deleteItem);
									contextMenu.addSeparator();

									contextMenu.add(copyItem);
									contextMenu.add(cutItem);									
								} else if(selNode.getUserObject() instanceof DOMediaLibraryFolder){
									if(MediaLibraryConfiguration.getInstance().isMediaLibraryEnabled()){    								
    									contextMenu.add(refreshItem);
    									contextMenu.addSeparator();
    									addAutoFolderItem.setVisible(true);							
    									contextMenu.add(showInLibraryItem);
    									contextMenu.add(setAsRootItem);
    									contextMenu.addSeparator();
    								} else {
    									addAutoFolderItem.setVisible(false);    									
    								}
    									
									contextMenu.add(addMenu);

									contextMenu.add(editItem);
									contextMenu.add(deleteItem);
									contextMenu.addSeparator();

									contextMenu.add(copyItem);
									contextMenu.add(cutItem);

    								if(selNode.getUserObject() instanceof DOMediaLibraryFolder){
    									contextMenu.add(pasteMenuItem);    									
    								}

									pasteMenuItem.setEnabled(copyNode != null);
    								
    								editItem.setEnabled(true);
    								if(selNode.getUserObject() instanceof DOSpecialFolder){
    									if(((DOSpecialFolder)selNode.getUserObject()).getSpecialFolderImplementation().getInstanceConfigurationPanel() == null){
    										editItem.setEnabled(false);
    									}
    								}
								} else {
									contextMenu.add(addMenu);
									contextMenu.add(editItem);
									contextMenu.add(deleteItem);
									contextMenu.addSeparator();

									contextMenu.add(copyItem);
									contextMenu.add(cutItem);
									contextMenu.add(pasteMenuItem);
								}
								
								contextMenu.show((JTree) e.getSource(), e.getX(), e.getY());
								break;
						}
					}
				}
			}
		});
	}

	private void refreshSelectedNode() {
		DefaultMutableTreeNode selNode = getSelectionPath() == null ? null : (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
		if (selNode != null) {
			refreshNode(selNode);
		}
	}

	private void showInLibraryRequested() {
		if(getSelectedNode() != null 
				&& getSelectedNode().getUserObject() instanceof DOMediaLibraryFolder){
			DOMediaLibraryFolder folder = ((DOMediaLibraryFolder)getSelectedNode().getUserObject());
			if(libraryShowListener != null){
				libraryShowListener.show(folder.getInheritedFilter(), folder.getFileType());
			}			
		}
	}

	private void setAsRootrequested() {
		if(getSelectedNode() != null 
				&& getSelectedNode().getUserObject() instanceof DOMediaLibraryFolder){
			DOMediaLibraryFolder folder = ((DOMediaLibraryFolder)getSelectedNode().getUserObject());
			mediaLibraryStorage.setMetaDataValue(MediaLibraryConstants.MetaDataKeys.ROOT_FOLDER_ID.toString(), String.valueOf(folder.getId()));
		}
	}

	private void addAutoFolderRequested() {
		List<AutoFolderType> folderTypes = new ArrayList<AutoFolderType>();
		folderTypes.add(AutoFolderType.TYPE_NAME);
		folderTypes.add(AutoFolderType.A_TO_Z);
		folderTypes.add(AutoFolderType.FILE_SYSTEM);
		folderTypes.add(AutoFolderType.TAG);
		AddAutoFolderDialog d = new AddAutoFolderDialog(folderTypes);
		d.addAutoFolderDialogActionListener(new AutoFolderDialogActionListener() {

			@Override
			public void autoFolderDialogActionReceived(AutoFolderDialogActionEvent event) {
				handleAutoFolderDialogActionReceived(event);
			}
		});
		d.setLocation(centerDialogOnScreen(d.getPreferredSize()));
		d.setResizable(false);
		d.setModal(true);
		d.pack();
		d.setVisible(true);
	}

	private void addSpecialFolderRequested(DlnaTreeFolderPlugin f) {
		String configDir = PMS.getConfiguration().getProfileDirectory() + File.separatorChar + "mlx_folder_plugin_configs" + File.separatorChar;		
		File cfgDir = new File(configDir);
		if(!cfgDir.isDirectory()){
			cfgDir.mkdirs();
		}
		
		File configFile;
		int i = 1;
		do {
			configFile = new File(configDir + f.getClass().getSimpleName() + "_" + i++ + ".cfg");
		} while (configFile.exists());

		DefaultMutableTreeNode parentFolder = null;
		DefaultMutableTreeNode selNode = getSelectionPath() == null ? null : (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
		if (selNode.getUserObject() instanceof DOMediaLibraryFolder) {
			parentFolder = selNode;
		} else {
			log.warn("Special folders can only be added to a DLNAViewFolderMutableTreeNode");
			return;
		}

		DOSpecialFolder sf = new DOSpecialFolder(configFile.getAbsolutePath(), f, "", -1, ((DOMediaLibraryFolder)parentFolder.getUserObject()).getId(), getNewFolderInsertPosition(parentFolder));
		if(sf.getSpecialFolderImplementation() != null){
			sf.setName(sf.getSpecialFolderImplementation().getName());
    		if(sf.getSpecialFolderImplementation().getInstanceConfigurationPanel() != null){
    			showSpecialFolderDialog(sf);
    		} else {
    			mediaLibraryStorage.insertFolder(sf);
    			if(selNode.getUserObject() instanceof DOMediaLibraryFolder){
    				refreshNode(selNode);
    				setExpandedState(new TreePath(selNode.getPath()), true);
    				for(int j = 0; j < ((DefaultMutableTreeNode) selNode).getChildCount(); j++) {
						DefaultMutableTreeNode n = (DefaultMutableTreeNode)selNode.getChildAt(j);
						if(n.getUserObject().equals(sf)){
							setNodeSelected(n);
							break;
						}
    				}
    			}
    		}
		}
	}

	private void showSpecialFolderDialog(DOSpecialFolder sf) {
		if(sf == null || sf.getSpecialFolderImplementation() == null || sf.getSpecialFolderImplementation().getInstanceConfigurationPanel() == null){
			return;
		}
		
		PluginFolderDialog d = new PluginFolderDialog(sf);
		d.addSpecialFolderDialogActionListener(new SpecialFolderDialogActionListener() {

			@Override
			public void specialFolderDialogActionReceived(SpecialFolderDialogActionEvent e) {
				handleSpecialFolderDialogActionReceived(e);
			}
		});
		d.setMinimumSize(new Dimension(600, 300));
		d.setModal(true);
		d.setLocation(centerDialogOnScreen(d.getSize()));
		d.setVisible(true);
	}

	private void addNewFolder() {
		DefaultMutableTreeNode selNode = getSelectionPath() == null ? null : (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
		if (selNode != null && selNode.getUserObject() instanceof DOMediaLibraryFolder) {
			DOMediaLibraryFolder parentFolder = (DOMediaLibraryFolder)selNode.getUserObject();
			DOMediaLibraryFolder mediaLibraryFolder = new DOMediaLibraryFolder(-1, parentFolder);
			mediaLibraryFolder.setName(Messages.getString("ML.DLNAViewTree.NewFolderTitle"));
			if(MediaLibraryConfiguration.getInstance().isMediaLibraryEnabled()){
    			mediaLibraryFolder.setFileType(parentFolder.getFileType());
    			mediaLibraryFolder.setMaxFiles(parentFolder.getMaxFiles());
    			showFolderDialog(mediaLibraryFolder, true);
			} else {
    			mediaLibraryFolder.setFileType(FileType.FILE);
				mediaLibraryFolder.setPositionInParent(getNewFolderInsertPosition((DefaultMutableTreeNode) selNode));
				mediaLibraryStorage.insertFolder(mediaLibraryFolder);
				refreshNode(selNode);
				
				DefaultMutableTreeNode newNode = null;
				for(int i = 0; i < ((DefaultMutableTreeNode) selNode).getChildCount(); i++){
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) selNode).getChildAt(i);
					if(child.getUserObject() instanceof DOMediaLibraryFolder){
						newNode = child;
					}
				}
				setNodeSelected(newNode);
				startEditingAtPath(getSelectionPath());
			}
		}
	}

	private void editSelectedNode() {
		Object selNode = getSelectionPath() == null ? null : getSelectionPath().getLastPathComponent();
		if (selNode != null && selNode instanceof DefaultMutableTreeNode) {
    			if (((DefaultMutableTreeNode) selNode).getUserObject() instanceof DOMediaLibraryFolder) {
    				if(MediaLibraryConfiguration.getInstance().isMediaLibraryEnabled()){
        				DOMediaLibraryFolder mlf = (DOMediaLibraryFolder) ((DefaultMutableTreeNode) selNode).getUserObject();
        				showFolderDialog(mlf, false);
    				} else {
    					startEditingAtPath(getSelectionPath());
    				}
    			} else if (((DefaultMutableTreeNode) selNode).getUserObject() instanceof DOSpecialFolder) {
    				DOSpecialFolder sf = (DOSpecialFolder) ((DefaultMutableTreeNode) selNode).getUserObject();
    				if(sf.getSpecialFolderImplementation() != null && sf.getSpecialFolderImplementation().getInstanceConfigurationPanel() != null){
        				showSpecialFolderDialog(sf);
    				} else {
    					startEditingAtPath(new TreePath(((DefaultMutableTreeNode) selNode).getPath()));
    				}
    			}
		}
	}

	private void deleteSelectedNode() {
		Object selNode = getSelectionPath() == null ? null : getSelectionPath().getLastPathComponent();
		if (selNode != null && selNode instanceof DefaultMutableTreeNode) {
			deleteNode((DefaultMutableTreeNode) selNode, true);
		}
	}

	private void copySelectedNode() {
		Object selNode = getSelectionPath() == null ? null : getSelectionPath().getLastPathComponent();
		if (selNode != null && selNode instanceof DefaultMutableTreeNode) {
			copyNode = (DefaultMutableTreeNode) selNode;
			currentCutPasteOperation = CopyCutAction.COPY;
		}
	}

	private void cutSelectedNode() {
		Object selNode = getSelectionPath() == null ? null : getSelectionPath().getLastPathComponent();
		if (selNode != null && selNode instanceof DefaultMutableTreeNode) {
			copyNode = (DefaultMutableTreeNode) selNode;
			currentCutPasteOperation = CopyCutAction.CUT;
		}
	}

	/**
	 * Initializes the context menu The menu item event listeners also live here
	 */
	private void initContextMenu() {
		String iconsFolder = "/resources/images/";
		contextMenu = new JPopupMenu();

		addMenu = new JMenu(Messages.getString("ML.ContextMenu.ADD"));
		addMenu.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "add-16.png")));

		refreshItem = new JMenuItem(Messages.getString("ML.ContextMenu.REFRESH"));
		refreshItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "refresh-16.png")));
		refreshItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshSelectedNode();
			}
		});
		
		setAsRootItem = new JMenuItem(Messages.getString("ML.ContextMenu.SET_AS_ROOT"));
		setAsRootItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "root_folder-16.png")));
		setAsRootItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setAsRootrequested();
			}
		});
		
		showInLibraryItem = new JMenuItem(Messages.getString("ML.ContextMenu.SHOW_IN_LIBRARY"));
		showInLibraryItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "library-16.png")));
		showInLibraryItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showInLibraryRequested();
			}
		});
		
		addAutoFolderItem = new JMenuItem(Messages.getString("ML.ContextMenu.ADD_AUTO_FOLDER"));
		addAutoFolderItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "auto_folder-16.png")));
		addAutoFolderItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addAutoFolderRequested();
			}
		});
		addFolderItem = new JMenuItem(Messages.getString("ML.ContextMenu.ADD_FOLDER"));
		addFolderItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "nofilefilter_folder-16.png")));
		addFolderItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addNewFolder();
			}
		});

		refreshAddMenu(false);

		editItem = new JMenuItem(Messages.getString("ML.ContextMenu.EDIT"));
		editItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "edit-16.png")));
		editItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editSelectedNode();
			}
		});
		deleteItem = new JMenuItem(Messages.getString("ML.ContextMenu.DELETE"));
		deleteItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "delete-16.png")));
		deleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedNode();
			}
		});
		copyItem = new JMenuItem(Messages.getString("ML.ContextMenu.COPY"));
		copyItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "copy-16.png")));
		copyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copySelectedNode();
			}
		});
		cutItem = new JMenuItem(Messages.getString("ML.ContextMenu.CUT"));
		cutItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "cut-16.png")));
		cutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cutSelectedNode();
			}
		});
		pasteMenuItem = new JMenuItem(Messages.getString("ML.ContextMenu.PASTE"));
		pasteMenuItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "paste-16.png")));
		pasteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteNode();
			}
		});
	}

	private void refreshAddMenu(boolean refreshPlugins) {
		addMenu.removeAll();
		
		addMenu.add(addFolderItem);
		addMenu.add(addAutoFolderItem);
		
		if(refreshPlugins) {
			List<DlnaTreeFolderPlugin> dlnaTreeFolders = PluginsFactory.getDlnaTreeFolderPlugins();
			Collections.sort(dlnaTreeFolders, new Comparator<DlnaTreeFolderPlugin>() {
				@Override
	            public int compare(DlnaTreeFolderPlugin o1, DlnaTreeFolderPlugin o2) {
		            return o1.getName().compareToIgnoreCase(o2.getName());
	            }
			});
	
			if(dlnaTreeFolders.size() > 0) {
	    		addMenu.addSeparator();
	    		for (DlnaTreeFolderPlugin f : dlnaTreeFolders) {
	    			SpecialFolderMenuItem miSpecialFolder = new SpecialFolderMenuItem(f);
	    			miSpecialFolder.setIcon(f.getTreeNodeIcon());
	    			miSpecialFolder.addActionListener(new ActionListener() {
	    
	    				@Override
	    				public void actionPerformed(ActionEvent e) {
	    					addSpecialFolderRequested(((SpecialFolderMenuItem) e.getSource()).getSpecialFolder());
	    				}
	    			});
	    			addMenu.add(miSpecialFolder);
	    		}
			}
		}
	}

	private Point centerDialogOnScreen(Dimension dialogSize) {
		return GUIHelper.getCenterDialogOnParentLocation(dialogSize, this);
	}

	/**
	 * Add files to node according to the set filter
	 * 
	 * @param currentTreeNode
	 *            node to which items will be added
	 * @param currentFolder
	 *            folder from which items will be selected
	 */
	private void addFilesToNode(DefaultMutableTreeNode currentTreeNode, DOMediaLibraryFolder folder) {
		assert (currentTreeNode != null);
		assert (folder != null);

		if (folder.isDisplayItems()) {
			switch (folder.getFileType()) {
				case AUDIO:
					break;
				case VIDEO:
					FileDisplayProperties fdp = folder.getDisplayProperties();
					List<DOVideoFileInfo> videos = mediaLibraryStorage.getVideoFileInfo(folder.getInheritedFilter(), fdp.isSortAscending(), fdp.getSortType(), folder.getMaxFiles(), fdp.getSortOption(), true);

					int insertPos;
					int endPos;
					if (videos.size() == 0) {
						// Remove all videos if the received list is empty
						insertPos = getNewFolderInsertPosition(currentTreeNode);
						endPos = currentTreeNode.getChildCount();
					} else {
						insertPos = getNewFolderInsertPosition(currentTreeNode);
						endPos = currentTreeNode.getChildCount();
						// check, modify or add videos
						for (DOVideoFileInfo video : videos) {
							switch (folder.getDisplayProperties().getFileDisplayType()) {
								case FILE:
									((DefaultTreeModel) getModel()).insertNodeInto(new DLNAViewFileMutableTreeNode(video, folder.getDisplayProperties()),
									        currentTreeNode, insertPos);
									break;
								case FOLDER:
									insertFileFolder(currentTreeNode, video, mediaLibraryStorage.getFileFolder(folder.getDisplayProperties().getTemplate().getId()),
									        insertPos);
									break;
							}

							insertPos++;
						}
					}

					// remove invalid nodes
					if (insertPos < endPos) {
						for (int i = insertPos; i < endPos; i++) {
							((DefaultTreeModel) getModel()).removeNodeFromParent((DefaultMutableTreeNode) currentTreeNode.getChildAt(insertPos));
						}
					}
					break;
				case PICTURES:
					break;
			}
		} else {
			int firstFileIndex = getNewFolderInsertPosition(currentTreeNode);
			int nbItems = currentTreeNode.getChildCount();
			for (int i = firstFileIndex; i < nbItems; i++) {
				((DefaultTreeModel) getModel()).removeNodeFromParent((DefaultMutableTreeNode) currentTreeNode.getChildAt(firstFileIndex));
			}
		}
	}

	private void insertFileFolder(DefaultMutableTreeNode parentNode, DOFileInfo fileInfo, DOFileEntryFolder fileFolder, int positionInParent) {
		assert (parentNode != null);
		assert (positionInParent >= 0);

		String convertedMask = fileInfo.getDisplayString(fileFolder.getDisplayNameMask());
		fileFolder.setDisplayNameMask(convertedMask);

		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(fileFolder);
		((DefaultTreeModel) treeModel).insertNodeInto(newNode, parentNode, positionInParent);
		populateFileFolderNodes(newNode, fileInfo);
	}

	private void populateFileFolderNodes(DefaultMutableTreeNode folderNode, DOFileInfo fileInfo) {
		assert (folderNode != null);

		//only add entries to folders
		if (folderNode.getUserObject() instanceof DOFileEntryFolder) {
			DOFileEntryFolder fileFolder = (DOFileEntryFolder) folderNode.getUserObject();
			int posInParent = 0;
			
			//add all children
			for (int i = 0; i < fileFolder.getChildren().size(); i++) {
				DOFileEntryBase currFile = fileFolder.getChildren().get(i);
				
				//convert the display name and set it
				String convertedMask = fileInfo.getDisplayString(currFile.getDisplayNameMask());
				currFile.setDisplayNameMask(convertedMask);
				
				//add the node
				//split into multiple nodes if a max text length has been set
				if (currFile.getMaxLineLength() > 0 && convertedMask.length() > currFile.getMaxLineLength()) {
					int cutPos;
					do {
						cutPos = getCutOffPosition(convertedMask, currFile.getMaxLineLength());
						String text;
						if (cutPos > 0) {
							text = convertedMask.substring(0, cutPos).trim();
							convertedMask = convertedMask.substring(cutPos).trim();
						} else {
							text = convertedMask.trim();
							convertedMask = "";
						}

						DOFileEntryBase splitFile = currFile.clone();
						splitFile.setDisplayNameMask(text);
						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(splitFile);
						((DefaultTreeModel) treeModel).insertNodeInto(newNode, folderNode, posInParent++);

						if (currFile instanceof DOFileEntryFolder) {
							populateFileFolderNodes(newNode, fileInfo);
						} else if (currFile instanceof DOFileEntryPlugin 
								&& ((DOFileEntryPlugin) currFile).getPlugin() != null 
								&& ((DOFileEntryPlugin) currFile).getPlugin().isAvailable()
								&& fileInfo instanceof DOVideoFileInfo) {
							addPluginChildNodes((DOFileEntryPlugin) currFile, (DOVideoFileInfo) fileInfo, newNode);
						}
					} while (cutPos > 0);
				} else {
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(currFile);
					((DefaultTreeModel) treeModel).insertNodeInto(newNode, folderNode, posInParent++);

					if (currFile instanceof DOFileEntryFolder) {
						populateFileFolderNodes(newNode, fileInfo);
					} else if (currFile instanceof DOFileEntryPlugin 
							&& ((DOFileEntryPlugin) currFile).getPlugin() != null 
							&& fileInfo instanceof DOVideoFileInfo) {
						addPluginChildNodes((DOFileEntryPlugin) currFile, (DOVideoFileInfo) fileInfo, newNode);
					}
				}
			}
		}
	}
	
	private void addPluginChildNodes(DOFileEntryPlugin fileEntryPlugin, DOVideoFileInfo videoFileInfo, MutableTreeNode parentNode){
			try {
				fileEntryPlugin.getPlugin().loadConfiguration(fileEntryPlugin.getPluginConfigFilePath());
			} catch (IOException e) {
				log.error(String.format("Failed to load configuration '%s' for plugin '%s'", fileEntryPlugin.getPluginConfigFilePath(), fileEntryPlugin.getPlugin().getName()));
			}
			fileEntryPlugin.getPlugin().setVideo(videoFileInfo);
			if (fileEntryPlugin.getPlugin().getTreeNode() != null) {
				for (int i = 0; i < fileEntryPlugin.getPlugin().getTreeNode().getChildCount(); i++) {
					if (fileEntryPlugin.getPlugin().getTreeNode().getChildAt(i) instanceof MutableTreeNode) {
						MutableTreeNode pluginChild = (MutableTreeNode) fileEntryPlugin.getPlugin().getTreeNode().getChildAt(i);
						((DefaultTreeModel) treeModel).insertNodeInto(pluginChild, parentNode, i);
					}
				}
			}
	}

	private int getCutOffPosition(String convertedMask, int maxLineLength) {
		int cutOffPos = -1;
		if (maxLineLength > 0 && convertedMask.length() > maxLineLength) {
			cutOffPos = maxLineLength;
			while (cutOffPos > 0 && convertedMask.charAt(cutOffPos) != ' ') {
				cutOffPos--;
			}
			if (cutOffPos == 0) {
				cutOffPos = maxLineLength;
			}
		}
		return cutOffPos;
	}

	private int getNewFolderInsertPosition(DefaultMutableTreeNode selectedNode) {
		int index = -1;

		// Put the index at the position of the first file
		for (int i = 0; i < selectedNode.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) selectedNode.getChildAt(i);
			if (child instanceof DLNAViewFileMutableTreeNode) {
				index = i;
				break;
			}
		}

		// Add it to the end if no files are found
		if (index == -1) {
			index = selectedNode.getChildCount();
		}
		return index;
	}

	/**
	 * Add all child nodes to the folder recursively
	 * 
	 * @param parentNode
	 *            node to which child folders will be added
	 * @param currentFolder
	 *            folder from which child folders will be selected
	 */
	private void addChildrenToNode(DefaultMutableTreeNode parentNode, DOMediaLibraryFolder currentFolder) {
		assert (parentNode != null);
		assert (currentFolder != null);

		// Sort the list by positionInParent
		Collections.sort(currentFolder.getChildFolders(), positionInParentComparator);

		for (DOFolder f : currentFolder.getChildFolders()) {
			DOFolder parentFolder = (DOFolder)((DefaultMutableTreeNode)parentNode).getUserObject();
			f.setParentId(parentFolder.getId());
			if (f instanceof DOMediaLibraryFolder) {
				if(log.isDebugEnabled()) log.debug("Add media library folder " + f.getName() + " to " + parentFolder.getName());
				DOMediaLibraryFolder mediaLibraryFolder = (DOMediaLibraryFolder) f;
				DefaultMutableTreeNode newNode = addMediaLibraryFolder(mediaLibraryFolder, parentNode);
				
				if (displayItems 
						&& mediaLibraryFolder.isDisplayItems()
						&& MediaLibraryConfiguration.getInstance().isMediaLibraryEnabled()) {
					addFilesToNode(newNode, mediaLibraryFolder);
				}
				
				addChildrenToNode(newNode, mediaLibraryFolder);
			} else if (f instanceof DOSpecialFolder) {
				if(log.isDebugEnabled()) log.debug("Add special folder " + f.getName() + " to " + parentFolder.getName());
				addSpecialFolder((DOSpecialFolder) f, parentNode);
			}
		}
	}

	private DefaultMutableTreeNode addSpecialFolder(DOSpecialFolder f, DefaultMutableTreeNode parentNode) {
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(f);

		int insertPos = 0;
		if (f.getPositionInParent() > parentNode.getChildCount()) {
			insertPos = getNewFolderInsertPosition(parentNode);
		} else {
			insertPos = f.getPositionInParent();
		}
		
		if(f.getSpecialFolderImplementation().getTreeNode() != null && isDisplayItems()){
			for(int i = 0; i < f.getSpecialFolderImplementation().getTreeNode().getChildCount(); i++){
				MutableTreeNode n = (MutableTreeNode) f.getSpecialFolderImplementation().getTreeNode().getChildAt(i);
				newNode.add(n);
			}
		}

		((DefaultTreeModel) getModel()).insertNodeInto(newNode, parentNode, insertPos);

		return newNode;
	}

	private DefaultMutableTreeNode addMediaLibraryFolder(DOMediaLibraryFolder f, DefaultMutableTreeNode parentNode) {
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(f);

		int insertPos = 0;
		if (f.getPositionInParent() > parentNode.getChildCount()) {
			insertPos = getNewFolderInsertPosition(parentNode);
		} else {
			insertPos = f.getPositionInParent();
		}

		((DefaultTreeModel) getModel()).insertNodeInto(newNode, parentNode, insertPos);

		return newNode;
	}

	private void pasteNode() {
		if (currentCutPasteOperation == CopyCutAction.COPY || currentCutPasteOperation == CopyCutAction.CUT) {
			if (!(((DefaultMutableTreeNode)getSelectionPath().getLastPathComponent()).getUserObject() instanceof DOMediaLibraryFolder)) {
				log.warn("A node can only be pasted onto a DLNAViewFolderMutableTreeNode");
				return;
			}
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) getSelectionPath().getLastPathComponent();
			DOMediaLibraryFolder parentFolder = (DOMediaLibraryFolder)((DefaultMutableTreeNode)getSelectionPath().getLastPathComponent()).getUserObject();

			DOFolder pasteFolder;
			if (copyNode.getUserObject() instanceof DOFolder) {
				pasteFolder = ((DOFolder) copyNode.getUserObject()).clone();
			} else {
				log.warn("The node to paste didn't contain a UserObject of type DOFolder");
				return;
			}
			DefaultMutableTreeNode pasteNode = new DefaultMutableTreeNode(pasteFolder);
			
			//check that if we've got a cut/paste the destination folder isn't a child of the source folder
			if(currentCutPasteOperation == CopyCutAction.CUT){
				DefaultMutableTreeNode selNode = getSelectionPath() == null  || !(getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) ? null : (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
				DOFolder f = selNode.getUserObject() instanceof DOFolder ? (DOFolder)selNode.getUserObject() : null;
				while(f != null){
					if(f.equals(pasteFolder)){
						JOptionPane.showMessageDialog(getTopLevelAncestor(), Messages.getString("ML.DLNAViewTree.DontCutPasteFolderMsg"));
						return;
					}
					f = f.getParentFolder();
				}
				
				deleteNode(copyNode, false);
			}
			
			pasteFolder.setId(-1);
			pasteFolder.setParentFolder(parentFolder);
			if(pasteFolder instanceof DOMediaLibraryFolder){
				for(DOFolder f : ((DOMediaLibraryFolder) pasteFolder).getChildFolders()){
					f.setParentFolder((DOMediaLibraryFolder) pasteFolder);
				}				
			}
			
			if(pasteFolder instanceof DOSpecialFolder){
				DOSpecialFolder sf = (DOSpecialFolder)pasteFolder;
				
				File cfgFile = new File(sf.getConfigFilePath());
				if(cfgFile.exists()){
					String baseName = sf.getConfigFilePath().substring(0, sf.getConfigFilePath().lastIndexOf("_") + 1);
					File newCfgFile;
					int i = 1;
					do {
						newCfgFile = new File(baseName +  i++ + ".cfg");
					} while (newCfgFile.exists());
					
					FileHelper.copyFile(cfgFile.getAbsolutePath(), newCfgFile.getAbsolutePath());
					sf.setConfigFilePath(newCfgFile.getAbsolutePath());
				}
			}

			int insertPos = getNewFolderInsertPosition(selectedNode);
			pasteFolder.setPositionInParent(insertPos);

			if (pasteFolder instanceof DOMediaLibraryFolder) {
				mediaLibraryStorage.insertFolder(pasteFolder);
			} else if (pasteFolder instanceof DOSpecialFolder) {
				mediaLibraryStorage.insertFolder(pasteFolder);
				addSpecialFolder((DOSpecialFolder) pasteFolder, selectedNode);
			}

			refreshNode(selectedNode);

			pasteNode = (DefaultMutableTreeNode) treeModel.getChild(selectedNode, insertPos);
			setNodeSelected(pasteNode);
		}

		currentCutPasteOperation = CopyCutAction.COPY;
	}

	/**
	 * Deletes a node (as well as all of its child folders) in the DB and the
	 * tree view
	 * 
	 * @param selNode2
	 *            the node to delete
	 */
	private void deleteNode(DefaultMutableTreeNode selNode2, boolean confirmDelete) {
		assert (selNode2 != null);

		DOFolder fDel = (DOFolder) selNode2.getUserObject();
		if (selNode2.getUserObject() instanceof DOFolder) {
			fDel = (DOFolder) selNode2.getUserObject();
		}

		boolean doDelete = false;

		if (!confirmDelete) {
			doDelete = true;
		} else {
			if (fDel.getId() == MediaLibraryStorage.ROOT_FOLDER_ID || fDel.getId() == MediaLibraryStorage.getInstance().getRootFolderId()) {
				JOptionPane.showMessageDialog(getTopLevelAncestor(), String.format(Messages.getString("ML.DLNAViewTree.DeleteFolderForbiddenMsg"), fDel.getName()));
			} else {
				String question = String.format(Messages.getString("ML.DLNAViewTree.ConfirmDeleteFolderMsg"), fDel.getName());

				int resp = JOptionPane.showConfirmDialog(null, question);
				switch (resp) {
					case JOptionPane.CANCEL_OPTION:
						break;
					case JOptionPane.YES_OPTION:
						doDelete = true;
						break;
					case JOptionPane.NO_OPTION:
						break;
				}

				requestFocus();
			}
		}

		if (doDelete) {
			DefaultMutableTreeNode refreshParent = (DefaultMutableTreeNode) selNode2.getParent();
			((DOMediaLibraryFolder)refreshParent.getUserObject()).getChildFolders().remove(fDel);
			((DefaultTreeModel) getModel()).removeNodeFromParent(selNode2);
			DefaultMutableTreeNode[] fs = new DefaultMutableTreeNode[1];
			fs[0] = refreshParent;
			updateNodeIndexes(fs);
			mediaLibraryStorage.deleteFolder(fDel.getId());
		}
	}

	/**
	 * Refreshes the folders and items in a node from DB
	 * 
	 * @param node
	 *            the node to update
	 */
	private void refreshNode(DefaultMutableTreeNode node) {
		assert (node != null);
		
		if(!(node.getUserObject() instanceof DOMediaLibraryFolder)){
			return;
		}
		DOMediaLibraryFolder oldFolder = (DOMediaLibraryFolder)node.getUserObject();

		//update the folders from the DB
		DOMediaLibraryFolder newRoot = mediaLibraryStorage.getMediaLibraryFolder(oldFolder.getId(), MediaLibraryStorage.ALL_CHILDREN);
		newRoot.setParentFolder(oldFolder.getParentFolder());

		//save the expanded paths in order to restore them later
		List<String> expandedPaths = new ArrayList<String>();
		TreePath currentPath = new TreePath(node.getPath());
		if (isExpanded(currentPath)) {
			expandedPaths.add(currentPath.toString());
		}
		expandedPaths = getExpandedChildPaths(node, currentPath, expandedPaths);
		
		//get selected node
		DefaultMutableTreeNode selNode = getSelectionPath() == null ? null : (DefaultMutableTreeNode) getSelectionPath().getLastPathComponent();
		
		//remove all children from the node
		while(node.getChildCount() > 0) {
			((DefaultTreeModel)getModel()).removeNodeFromParent((MutableTreeNode) node.getChildAt(0));
		}
		
		//add updated folder nodes
		addChildrenToNode(node, newRoot);

		//add file nodes if required
		if (displayItems 
				&& newRoot.isDisplayItems()
				&& MediaLibraryConfiguration.getInstance().isMediaLibraryEnabled()) {
			addFilesToNode(node, newRoot);
		}
		
		//expand the paths as they were before
		refreshExpandedTreePaths(expandedPaths, node);
		
		//select the previously selected node
		if(selNode != null){
			DefaultMutableTreeNode newSelNode = getEquivalenNodeInTree((DefaultMutableTreeNode) node.getRoot(), selNode);
			setSelectionPath(newSelNode == null ? null : new TreePath(newSelNode.getPath()));
		}
	}

	private void refreshExpandedTreePaths(List<String> expandedPaths, DefaultMutableTreeNode node) {
		TreePath pathToExpand = new TreePath(node.getPath());
		if (expandedPaths.contains(pathToExpand.toString())) {
			expandPath(new TreePath(node.getPath()));

			DefaultMutableTreeNode currentNode;
			for (int i = 0; i < node.getChildCount(); i++) {
				currentNode = (DefaultMutableTreeNode) node.getChildAt(i);

				// recursive call to update all children
				refreshExpandedTreePaths(expandedPaths, currentNode);
			}
		}
	}

	private List<String> getExpandedChildPaths(DefaultMutableTreeNode node, TreePath currentPath, List<String> expandedPaths) {
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);

			TreePath p = new TreePath(child.getPath());

			// add to the list if expanded
			if (isExpanded(p)) {
				expandedPaths.add(p.toString());
			}

			// recursive call
			getExpandedChildPaths(child, currentPath, expandedPaths);
		}
		return expandedPaths;
	}

	/**
	 * Displays a folder dialog
	 * 
	 * @param folder
	 *            initial folder values
	 * @param isNewFolder
	 *            is a new folder being added?
	 */
	private void showFolderDialog(DOMediaLibraryFolder folder, boolean isNewFolder) {
		DOMediaLibraryFolder mlf;
		if (folder == null) {
			mlf = new DOMediaLibraryFolder();
		} else {
			mlf = folder;
		}
		FolderDialog fd = new FolderDialog(mlf, isNewFolder, MediaLibraryConfiguration.getInstance().isMediaLibraryEnabled());

		fd.addFolderDialogActionListener(new FolderDialogActionListener() {

			@Override
			public void folderDialogActionReceived(FolderDialogFolderUpdateEvent event) {
				handleFolderDialogActionReceived(event);
			}

			@Override
			public void templateUpdatePerformed() {
				handleTemplateUpdate();
			}
		});
		fd.setModal(true);
		fd.pack();

		fd.setLocation(GUIHelper.getCenterDialogOnParentLocation(fd.getSize(), this));
		fd.setVisible(true);
	}
	
	private void handleTemplateUpdate(){
		DefaultMutableTreeNode selNode = getSelectionPath() == null  || !(getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) ? null : (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
		DefaultMutableTreeNode sn = selNode;
		refreshNode((DefaultMutableTreeNode)selNode.getRoot());
		setNodeSelected(getEquivalenNodeInTree((DefaultMutableTreeNode) getModel().getRoot(), sn));
	}
	
	private DefaultMutableTreeNode getEquivalenNodeInTree(DefaultMutableTreeNode sourceNode, DefaultMutableTreeNode compareNode){
		if(sourceNode.getUserObject().equals(compareNode.getUserObject())){
			return sourceNode;
		} else {
			for(int i = 0; i < sourceNode.getChildCount(); i++){
				DefaultMutableTreeNode res = getEquivalenNodeInTree((DefaultMutableTreeNode) sourceNode.getChildAt(i), compareNode);
				if(res != null){
					return res;
				}
			}
		}
		return null;
		
	}

	/**
	 * Update folder in DB and tree view
	 * 
	 * @param mediaLibraryFolder
	 *            the folder containing all the information about what the node
	 *            has to display
	 * @param node
	 *            the node to whom the folder belongs to
	 */
	private void updateFolder(DOMediaLibraryFolder mediaLibraryFolder, DefaultMutableTreeNode node) {
		assert (mediaLibraryFolder != null);
		assert (node != null);

		mediaLibraryStorage.updateFolder(mediaLibraryFolder);
		node.setUserObject(mediaLibraryFolder);
		((DefaultTreeModel) getModel()).nodeChanged(node);
	}

	/**
	 * Sets all needed settings to select a node
	 * 
	 * @param node
	 *            the node to select
	 */
	private void setNodeSelected(DefaultMutableTreeNode node) {
		assert (node != null);

		TreePath selPath = new TreePath(node.getPath());
		setSelectionPath(selPath);
		scrollPathToVisible(selPath);
	}

	private void handleFolderDialogActionReceived(FolderDialogFolderUpdateEvent event) {
		FolderDialog fd = (FolderDialog) event.getSource();
		boolean doInsert = false;
		switch (event.getActionType()) {
			case OK:
				doInsert = true;
				fd.dispose();
				break;
			case APPLY:
				doInsert = true;
				break;
			case CANCEL:
				fd.dispose();
				break;
		}

		if (doInsert) {
			DefaultMutableTreeNode selNode = getSelectionPath() == null  || !(getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) ? null : (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
			DefaultMutableTreeNode nodeToSelect = null;
			if (selNode == null || !(selNode.getUserObject() instanceof DOMediaLibraryFolder)) {
				log.warn("Folders can only be inserted into DLNAViewFolderMutableTreeNode");
				return;
			}

			if (event.isNewFolder()) {
				DOMediaLibraryFolder newFolder = event.getMediaLibraryFolder();
				newFolder.setPositionInParent(getNewFolderInsertPosition(selNode));
				newFolder.setParentFolder((DOMediaLibraryFolder) selNode.getUserObject());
				mediaLibraryStorage.insertFolder(newFolder);
				refreshNode(selNode);
				nodeToSelect = (DefaultMutableTreeNode) treeModel.getChild(selNode, newFolder.getPositionInParent());
			} else {
				updateFolder(event.getMediaLibraryFolder(), selNode);
				refreshNode(selNode);
				nodeToSelect = selNode;
			}
			setNodeSelected(nodeToSelect);
		}
	}

	private void handleSpecialFolderDialogActionReceived(SpecialFolderDialogActionEvent e) {
		PluginFolderDialog d = (PluginFolderDialog) e.getSource();
		boolean doInsert = false;
		switch (e.getActionType()) {
			case OK:
				doInsert = true;
				d.dispose();
				break;
			case APPLY:
				doInsert = true;
				break;
			case CANCEL:
				d.dispose();
				break;
		}

		if (doInsert) {
			saveSpecialFolder(e.getSpecialFolder());
			if (e.getSpecialFolder().getId() > 0) {
				mediaLibraryStorage.updateFolder(e.getSpecialFolder());
			} else {
				mediaLibraryStorage.insertFolder(e.getSpecialFolder());
			}
			DefaultMutableTreeNode selNode;
			if(getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode){
				selNode = (DefaultMutableTreeNode) getSelectionPath().getLastPathComponent();
				refreshNode(selNode);
				for(int j = 0; j < selNode.getChildCount(); j++) {
					DefaultMutableTreeNode n = (DefaultMutableTreeNode) selNode.getChildAt(j);
					if(n.getUserObject().equals(e.getSpecialFolder())){
						setNodeSelected(n);
						break;
					}
				}
			}
		}
	}

	private void saveSpecialFolder(DOSpecialFolder specialFolder) {
		try {
			specialFolder.getSpecialFolderImplementation().saveInstanceConfiguration(specialFolder.getConfigFilePath());
			if(log.isDebugEnabled()) log.debug(String.format("Successfully saved SpecialFolder of type '%s' to '%s'", specialFolder.getSpecialFolderImplementation().getClass().getSimpleName(), specialFolder.getConfigFilePath()));
		} catch (FileNotFoundException ex) {
			String msg = String.format(Messages.getString("ML.DLNAViewTree.SaveSpecialFolderError.FileNotFound"), specialFolder.getConfigFilePath());
			log.error(msg, ex);
			JOptionPane.showMessageDialog(getTopLevelAncestor(), msg, Messages.getString("ML.DLNAViewTree.SaveSpecialFolderError.Title.FileNotFound"), JOptionPane.ERROR_MESSAGE);
		} catch (IOException ex) {
			String msg = String.format(Messages.getString("ML.DLNAViewTree.SaveSpecialFolderError.IO"), specialFolder.getConfigFilePath());
			log.error(msg, ex);
			JOptionPane.showMessageDialog(null, msg, Messages.getString("ML.DLNAViewTree.SaveSpecialFolderError.Title.IO"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * The listener handling auto folder inserts
	 */
	private void handleAutoFolderDialogActionReceived(AutoFolderDialogActionEvent event) {
		DefaultMutableTreeNode selNode = getSelectionPath() == null  || !(getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) ? null : (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
		if (selNode == null || !(selNode.getUserObject() instanceof DOMediaLibraryFolder)) {
			log.warn("Folders can only be inserted into DLNAViewFolderMutableTreeNode");
			return;
		}
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selNode;
		DOMediaLibraryFolder selectedFolder = (DOMediaLibraryFolder) selNode.getUserObject();
		AddAutoFolderDialog fd = (AddAutoFolderDialog) event.getSource();
		switch (event.getActionType()) {
			case OK:
				// Get the position in the parent where the new node will be
				// inserted.
				// It will be the last folder
				int insertPos = getNewFolderInsertPosition(selNode);
				boolean folderCreated = false;

				switch (event.getAutoFolderType()) {
					case A_TO_Z:
						AutoFolderCreator.addAtoZVideoFolders(mediaLibraryStorage, selectedFolder, event.isAscending(), insertPos);
						folderCreated = true;
						break;
					case FILE_SYSTEM:
						if (event.getUserObject() != null && event.getUserObject() instanceof String) {
							File f = new File((String) event.getUserObject());
							if(f.isDirectory()) {
								AutoFolderCreator.addFileSystemFolders(mediaLibraryStorage, selectedFolder, insertPos, f);
								folderCreated = true;
							} else {
								JOptionPane.showMessageDialog(this, Messages.getString("ML.DLNAViewTree.FolderInvalid"));
							}
						}
						break;
					case TYPE_NAME:
						if (event.getUserObject() != null && event.getUserObject() instanceof AutoFolderProperty) {
							AutoFolderProperty prop = (AutoFolderProperty) event.getUserObject();
							AutoFolderCreator.addTypeFolder(mediaLibraryStorage, selectedFolder, prop, event.isAscending(), insertPos, event.getMinOccurences());
							folderCreated = true;
						}
						break;
					case TAG:
						String tagName;
						if (event.getUserObject() != null && event.getUserObject() instanceof String && !((tagName = (String) event.getUserObject()).equals(""))) {
							AutoFolderCreator.addTagFolder(mediaLibraryStorage, selectedFolder, tagName, event.isAscending(), insertPos, event.getMinOccurences());
							folderCreated = true;
						} else {
							JOptionPane.showMessageDialog(this, Messages.getString("ML.DLNAViewTree.NoTagSelected"));
						}
						break;
					default:
						// Do nothing
						break;
				}

				if(!folderCreated) {
					return;
				}
				
				refreshNode(selectedNode);

				// Set the new node selected
				DefaultMutableTreeNode sn = (DefaultMutableTreeNode) treeModel.getChild(selNode, insertPos);
				setNodeSelected(sn);
				fd.dispose();
				break;
			case CANCEL:
				fd.dispose();
				break;
		}
	}

	private void updateNodeIndexes(DefaultMutableTreeNode[] nodesToUpdate) {
		// Update the indexes and parent ids of all nodes and child nodes that
		// have to be refreshed
		for (int i = 0; i < nodesToUpdate.length; i++) {
			DefaultMutableTreeNode node = nodesToUpdate[i];
			for (int j = 0; j < node.getChildCount(); j++) {
				if (node.getChildAt(j) instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(j);
					int indexInParent = treeModel.getIndexOfChild(node, child);
					
					if(child.getUserObject() instanceof DOFolder){
						mediaLibraryStorage.updateMediaLibraryFolderLocation(((DOFolder)child.getUserObject()).getId(), ((DOFolder)node.getUserObject()).getId(), indexInParent);
					}
				}
			}
		}
	}

	public DefaultMutableTreeNode getSelectedNode() {
		return getSelectionPath() == null  || !(getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) ? null : (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
    }
}
