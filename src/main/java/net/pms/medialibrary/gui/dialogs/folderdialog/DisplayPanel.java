package net.pms.medialibrary.gui.dialogs.folderdialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryBase;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFile;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFolder;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryPlugin;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.dataobjects.DOTemplate;
import net.pms.medialibrary.commons.dataobjects.DOThumbnailPriority;
import net.pms.medialibrary.commons.dataobjects.FileDisplayProperties;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionTypeCBItem;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.SortOptionCBItem;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.TemplateCBItem;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.DialogActionType;
import net.pms.medialibrary.commons.enumarations.FileDisplayType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.SortOption;
import net.pms.medialibrary.commons.enumarations.ThumbnailPrioType;
import net.pms.medialibrary.commons.events.ConfigureFileDialogListener;
import net.pms.medialibrary.commons.events.FileEntryPluginDialogActionEvent;
import net.pms.medialibrary.commons.events.FileEntryPluginDialogActionListener;
import net.pms.medialibrary.commons.events.FilterFileDialogDialogEventArgs;
import net.pms.medialibrary.commons.events.FolderDialogActionListener;
import net.pms.medialibrary.commons.exceptions.TemplateException;
import net.pms.medialibrary.commons.helpers.FolderHelper;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.gui.dialogs.ConfigureFileEntryDialog;
import net.pms.medialibrary.gui.dialogs.FileEntryPluginDialog;
import net.pms.medialibrary.gui.shared.FileDisplayPanel;
import net.pms.plugins.FileDetailPlugin;
import net.pms.plugins.PluginsFactory;

class DisplayPanel extends JPanel {
	private static final Logger log = LoggerFactory.getLogger(DisplayPanel.class);
	private static final long                serialVersionUID   = 4154536836952807722L;
	private IMediaLibraryStorage             storage;
	private DOMediaLibraryFolder             folder;
	private List<FolderDialogActionListener> folderDialogActionListeners;

	private JCheckBox                        cbDisplayItems;
	private JTextField tfMaxFiles;
	private JCheckBox                        cbInheritSort;
	private JCheckBox                        cbInheritDisplayFileAs;
	private JComboBox                        cbTemplate;
	private JButton                          bNewTemplate;
	private JButton                          bDeleteTemplate;
	private JButton                          bEditTemplate;
	private JButton                          bTemplateApply;
	private JButton                          bTemplateCancel;
	private JRadioButton                     rbDisplayItemAsFile;
	private JRadioButton                     rbDisplayIemAsFolder;
	private JScrollPane                      spTree;
	private DefaultTreeModel                 treeModel;
	private JLabel                           lTemplate;
	private JComboBox						 cbSortOption;
	private JComboBox                        cbSortType;
	private JRadioButton                     rbSortAsc;
	private JRadioButton                     rbSortDesc;
	private FileDisplayPanel                 pFileDispay;

	private JPanel                           pTemplate;
	private JPopupMenu                       treeContextMenu;

	private FolderHelper                     folderHelper       = new FolderHelper();
	private boolean                          isCreatingTemplate = false;
	private boolean                          isEditingTemplate  = false;

	protected long                           editingTemplateId;

	private JTree                            tree;

	private JMenuItem                        miRemove;
	private JMenuItem                        miRename;
	private JMenuItem                        miEdit;
	private JMenuItem                        miAddFile;
	private JMenuItem                        miAddFolder;
	private JMenuItem                        miAddInfo;
	private JMenu                            mAdd;
	private JLabel lMaxFiles;

	DisplayPanel(DOMediaLibraryFolder f, IMediaLibraryStorage storage, List<FolderDialogActionListener> folderDialogActionListeners) {
		this.folderDialogActionListeners = folderDialogActionListeners;
		this.storage = storage;
		folder = f;

		setLayout(new GridLayout());
		initContextMenu();
		init();
	}

	void setFileType(FileType fileType) {
		pFileDispay.setFileType(fileType);

		// update sort type combo box
		Object selectedItem = cbSortType.getSelectedItem();
		cbSortType.removeAllItems();
		for (ConditionTypeCBItem item : FolderHelper.getHelper().getSortByConditionTypes(Arrays.asList(fileType))) {
			cbSortType.addItem(item);
		}
		cbSortType.setSelectedItem(selectedItem);

		if (fileType != FileType.VIDEO) {
			rbDisplayItemAsFile.setSelected(true);
		}

		cbDisplayItems.setText(Messages.getString("ML.DisplayPanel.DisplayItems." + fileType.toString()));
		lMaxFiles.setText(Messages.getString("ML.DisplayPanel.lMaxFiles." + fileType.toString()));

		updateGUI();
	}

	void canInheritSortOrder(boolean inherit) {
		cbInheritSort.setEnabled(inherit);
		updateGUI();
	}

	void canInheritDisplayAs(boolean inherit) {
		cbInheritDisplayFileAs.setEnabled(inherit);
		updateGUI();
	}

	boolean isDisplayItems() {
		return cbDisplayItems.isSelected();
	}

	int getMaxFiles() {
		int res = 0;
		try{
			res = Integer.parseInt(tfMaxFiles.getText());
		} catch (Exception ex) {}
		return res;
	}

	boolean isInheritDisplayFileAs() {
		return cbInheritDisplayFileAs.isSelected();
	}

	boolean isInheritSort() {
		return cbInheritSort.isSelected();
	}

	FileDisplayProperties getDisplayProperties() throws TemplateException {
		if (isEditingTemplate) { 
			throw new TemplateException(Messages.getString("ML.DisplayPanel.ForceSaveTemplateMsg")); 
		}
		
		FileDisplayProperties props = new FileDisplayProperties();
		props.setDisplayNameMask(getDisdplayNameMask());
		props.setFileDisplayType(getFileDisplayType());
		props.getTemplate().setName(getTemplateName());
		props.getTemplate().setId(getTemplateId());
		props.setSortAscending(getSortAscending());
		props.setSortType(getSortType());
		props.setSortOption(getSortOption());
		props.setThumbnailPriorities(pFileDispay.getThumbnailPriorities());
		return props;
	}
	
	SortOption getSortOption(){
		SortOptionCBItem item = (SortOptionCBItem) cbSortOption.getSelectedItem();
		return item.getSortOption();
	}

	boolean getSortAscending() {
		return rbSortAsc.isSelected();
	}

	ConditionType getSortType() {
		if (cbSortType.getSelectedItem() instanceof ConditionTypeCBItem) {
			return ((ConditionTypeCBItem) cbSortType.getSelectedItem()).getConditionType();
		} else {
			return ConditionType.FILE_DATEINSERTEDDB;
		}
	}

	private String getDisdplayNameMask() {
		return pFileDispay.getDisplaynameMask();
	}

	private FileDisplayType getFileDisplayType() {
		FileDisplayType fdt = FileDisplayType.UNKNOWN;
		if (rbDisplayItemAsFile.isSelected()) {
			fdt = FileDisplayType.FILE;
		} else {
			fdt = FileDisplayType.FOLDER;
		}
		return fdt;
	}

	private String getTemplateName() {
		return cbTemplate.getSelectedItem() == null ? "" : cbTemplate.getSelectedItem().toString();
	}

	private long getTemplateId() {
		long id = -1;
		if (cbTemplate.getSelectedItem() != null && cbTemplate.getSelectedItem() instanceof TemplateCBItem) {
			id = ((TemplateCBItem) cbTemplate.getSelectedItem()).getId();
		}
		return id;
	}

	private void init() {
		// display items
		cbDisplayItems = new JCheckBox(Messages.getString("ML.DisplayPanel.DisplayItems." + folder.getFileType().toString()));
		cbDisplayItems.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tfMaxFiles.setVisible(cbDisplayItems.isSelected());
				lMaxFiles.setVisible(cbDisplayItems.isSelected());
			}
		});
		tfMaxFiles = new JTextField();
		tfMaxFiles.setPreferredSize(new Dimension(50, tfMaxFiles.getPreferredSize().height));
		lMaxFiles = new JLabel();

		// create sort line
		cbSortOption = new JComboBox();
		for(SortOption so : SortOption.values()){
			if(so == SortOption.Unknown) continue;
			
			cbSortOption.addItem(new SortOptionCBItem(so, Messages.getString("ML.SortOption." + so.toString())));
		}
		cbSortOption.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cbSortOption.getSelectedItem() == null) return;
				
				boolean isVisible = ((SortOptionCBItem)cbSortOption.getSelectedItem()).getSortOption() == SortOption.FileProperty;
				cbSortType.setVisible(isVisible);
				rbSortAsc.setVisible(isVisible);
				rbSortDesc.setVisible(isVisible);
			}
		});
		ButtonGroup bgSort = new ButtonGroup();
		rbSortAsc = new JRadioButton(Messages.getString("ML.DisplayPanel.rbSortAsc"));
		rbSortDesc = new JRadioButton(Messages.getString("ML.DisplayPanel.rbSortDesc"));
		bgSort.add(rbSortAsc);
		bgSort.add(rbSortDesc);
		cbSortType = new JComboBox(FolderHelper.getHelper().getSortByConditionTypes(Arrays.asList(folder.getFileType())));
		cbInheritSort = new JCheckBox(Messages.getString("ML.DisplayPanel.cbInheritSort"));
		cbInheritSort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				folder.setInheritSort(cbInheritSort.isSelected());
				updateGUI();
			}
		});

		// create display as (file or folder)
		ButtonGroup rbGroupDisplayItemAs = new ButtonGroup();
		rbDisplayItemAsFile = new JRadioButton(Messages.getString("ML.DisplayPanel.rbDisplayItemAsFile"));
		rbDisplayItemAsFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateGUI();
			}
		});
		rbDisplayIemAsFolder = new JRadioButton(Messages.getString("ML.DisplayPanel.rbDisplayIemAsFolder"));
		rbDisplayIemAsFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateGUI();
			}
		});
		cbInheritDisplayFileAs = new JCheckBox(Messages.getString("ML.DisplayPanel.cbInheritDisplayFileAs"));
		cbInheritDisplayFileAs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cbInheritDisplayFileAsSelected();
			}
		});
		rbGroupDisplayItemAs.add(rbDisplayItemAsFile);
		rbGroupDisplayItemAs.add(rbDisplayIemAsFolder);

		// create file display type
		DOFileEntryBase fileEntry = new DOFileEntryBase(-1, null, 0, folder.getDisplayProperties().getDisplayNameMask(), folder.getDisplayProperties()
		        .getThumbnailPriorities(), folder.getDisplayProperties().getFileDisplayType(), 0, null, null);
		pFileDispay = new FileDisplayPanel(fileEntry, folder.getFileType());
		pFileDispay.setFileDisplayModeVisible(false);
		pFileDispay.setMaxLineLengthVisible(false);

		spTree = new JScrollPane();
		spTree.setPreferredSize(new Dimension(50, 115));

		// create folder display type
		initFolderDisplayType();

		refreshDisplayPanel();
	}

	private void cbInheritDisplayFileAsSelected() {
		if(isEditingTemplate){
			JOptionPane.showMessageDialog(this, Messages.getString("ML.DisplayPanel.SaveTemplateOnInheritDisplayAs"));
			cbInheritDisplayFileAs.setSelected(false);
		} else {
			folder.setInheritDisplayFileAs(cbInheritDisplayFileAs.isSelected());
			updateGUI();
		}
    }

	private void initFolderDisplayType() {

		lTemplate = new JLabel(Messages.getString("ML.DisplayPanel.lTemplate"));
		cbTemplate = new JComboBox();
		cbTemplate.setPreferredSize(new Dimension(150, cbTemplate.getPreferredSize().height));

		bNewTemplate = new JButton(Messages.getString("ML.DisplayPanel.bNewTemplate"));
		bNewTemplate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				isCreatingTemplate = true;

				bNewTemplate.setVisible(false);
				bEditTemplate.setVisible(false);
				bDeleteTemplate.setVisible(false);
				bTemplateApply.setVisible(true);
				bTemplateCancel.setVisible(true);

				cbTemplate.setEditable(true);
				cbTemplate.removeAllItems();
				cbTemplate.requestFocus();

				List<DOThumbnailPriority> thumbnailPriorities = new ArrayList<DOThumbnailPriority>();
				thumbnailPriorities.add(new DOThumbnailPriority(-1, ThumbnailPrioType.THUMBNAIL, 10, 0));
				thumbnailPriorities.add(new DOThumbnailPriority(-1, ThumbnailPrioType.GENERATED, 10, 1));
				DOFileEntryFolder ff = new DOFileEntryFolder(new ArrayList<DOFileEntryBase>(), -1, null, 1, "%title (%year) - %rating_percent/100", thumbnailPriorities, 0);
				showTree(ff);

				tree.setTransferHandler(new TemplateTreeTransferHandler());
				tree.setDragEnabled(true);
				tree.addKeyListener(new InternalKeyAdapter());
				tree.setEditable(true);
			}
		});
		
		bEditTemplate = new JButton(Messages.getString("ML.DisplayPanel.bEditTemplate"));
		bEditTemplate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (cbTemplate.getSelectedItem() != null) {
					editingTemplateId = ((TemplateCBItem) cbTemplate.getSelectedItem()).getId();

					tree.setTransferHandler(new TemplateTreeTransferHandler());
					tree.setDragEnabled(true);
					tree.addKeyListener(new InternalKeyAdapter());

					isEditingTemplate = true;

					bNewTemplate.setVisible(false);
					bEditTemplate.setVisible(false);
					bDeleteTemplate.setVisible(false);
					bTemplateApply.setVisible(true);
					bTemplateCancel.setVisible(true);

					cbTemplate.setEditable(true);
					tree.setEditable(true);

					String currTemplateName = cbTemplate.getSelectedItem().toString();
					cbTemplate.removeAllItems();
					cbTemplate.addItem(currTemplateName);
					cbTemplate.requestFocus();
				}
			}
		});

		bDeleteTemplate = new JButton(Messages.getString("ML.DisplayPanel.bDeleteTemplate"));
		bDeleteTemplate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (cbTemplate.getSelectedItem() != null) {
					long selTemplateId = ((TemplateCBItem) cbTemplate.getSelectedItem()).getId();
					if (storage.isTemplateIdInUse(selTemplateId)) {
						JOptionPane.showMessageDialog(SwingUtilities.getRoot(cbTemplate), Messages.getString("ML.DisplayPanel.TemplateInUse"));
					} else if (JOptionPane.showConfirmDialog(SwingUtilities.getRoot(cbTemplate), String.format(Messages.getString("ML.DisplayPanel.ConfirmDeleteTemplateMsg"), cbTemplate.getSelectedItem())
					        , Messages.getString("ML.DisplayPanel.ConfirmDeleteTemplateTitle"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						storage.deleteTemplate(selTemplateId);
						spTree.setViewportView(null);
						updateTemplateList();
					}
				}
			}
		});

		bTemplateApply = new JButton(Messages.getString("ML.DisplayPanel.bTemplateApply"));
		bTemplateApply.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (cbTemplate.getSelectedItem() == null) {
					JOptionPane.showMessageDialog(bTemplateApply, Messages.getString("ML.DisplayPanel.SetNameForTemplateMsg"));
					return;
				}
				tree.setTransferHandler(null);
				tree.setDragEnabled(false);
				for (KeyListener kl : tree.getKeyListeners()) {
					tree.removeKeyListener(kl);
				}

				bNewTemplate.setVisible(true);
				bEditTemplate.setVisible(true);
				bDeleteTemplate.setVisible(true);
				bTemplateApply.setVisible(false);
				bTemplateCancel.setVisible(false);

				cbTemplate.setEditable(false);
				tree.setEditable(false);

				DOTemplate currTemplate = null;
				if (isCreatingTemplate) {
					currTemplate = new DOTemplate(cbTemplate.getSelectedItem().toString(), -1);
					DOFileEntryFolder fileFolder = getFileFolder();
					storage.insertTemplate(currTemplate, fileFolder);
				} else if (isEditingTemplate) {
					currTemplate = new DOTemplate(cbTemplate.getSelectedItem().toString(), editingTemplateId);
					DOFileEntryFolder fileFolder = getFileFolder();
					storage.updateTemplate(currTemplate, fileFolder);
				}

				updateTemplateList();

				isEditingTemplate = false;
				isCreatingTemplate = false;

				cbTemplate.setSelectedItem(new TemplateCBItem(currTemplate.getId(), currTemplate.getName()));

				for (FolderDialogActionListener l : folderDialogActionListeners) {
					l.templateUpdatePerformed();
				}
			}
		});

		bTemplateCancel = new JButton(Messages.getString("ML.DisplayPanel.bTemplateCancel"));
		bTemplateCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				tree.setTransferHandler(null);
				tree.setDragEnabled(false);
				for (KeyListener kl : tree.getKeyListeners()) {
					tree.removeKeyListener(kl);
				}

				bNewTemplate.setVisible(true);
				bEditTemplate.setVisible(true);
				bDeleteTemplate.setVisible(true);
				bTemplateApply.setVisible(false);
				bTemplateCancel.setVisible(false);
				cbTemplate.setEditable(false);
				cbTemplate.removeAllItems();
				spTree.setViewportView(null);

				for (DOTemplate template : storage.getAllTemplates()) {
					cbTemplate.addItem(new TemplateCBItem(template.getId(), template.getName()));
				}

				isCreatingTemplate = false;
				isEditingTemplate = false;
			}
		});

		switch (folder.getDisplayProperties().getFileDisplayType()) {
			case FILE:
				rbDisplayItemAsFile.setSelected(true);
				break;
			case FOLDER:
				rbDisplayIemAsFolder.setSelected(true);
		}

		if (folder.getDisplayProperties().isSortAscending()) {
			rbSortAsc.setSelected(true);
		} else {
			rbSortDesc.setSelected(true);
		}

		cbTemplate.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!isCreatingTemplate && cbTemplate.getSelectedItem() instanceof TemplateCBItem) {
					clearTree();
					showTree(((TemplateCBItem) cbTemplate.getSelectedItem()).getId());
				}

			}
		});

		// select configured values
		updateTemplateList();
		bTemplateApply.setVisible(false);
		bTemplateCancel.setVisible(false);
		tfMaxFiles.setVisible(folder.isDisplayItems());
		lMaxFiles.setVisible(folder.isDisplayItems());

		cbInheritDisplayFileAs.setSelected(folder.isInheritDisplayFileAs());
		cbDisplayItems.setSelected(folder.isDisplayItems());
		tfMaxFiles.setText(String.valueOf(folder.getMaxFiles()));
		cbInheritSort.setSelected(folder.isInheritSort());
		cbTemplate.setSelectedItem(new TemplateCBItem(folder.getDisplayProperties().getTemplate().getId(), folder.getDisplayProperties().getTemplate().getName()));
		cbSortType.setSelectedItem(folderHelper.getConditionTypeCBItem(folder.getDisplayProperties().getSortType()));
	}

	/**
	 * Initializes the context menu The menu item event listeners also live here
	 */
	private void initContextMenu() {
		String iconsFolder = "/resources/images/";

		treeContextMenu = new JPopupMenu();
		treeContextMenu.setEnabled(true);

		mAdd = new JMenu(Messages.getString("ML.DisplayPanel.Menu.Add"));
		mAdd.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "add-16.png")));

		miAddInfo = new JMenuItem(Messages.getString("ML.DisplayPanel.Menu.Info"));
		miAddInfo.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "filefolderentry-16.png")));
		miAddInfo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addNode(FileDisplayType.INFO);
			}
		});
		mAdd.add(miAddInfo);

		miAddFolder = new JMenuItem(Messages.getString("ML.DisplayPanel.Menu.AddFolder"));
		miAddFolder.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "filefolder-16.png")));
		miAddFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addNode(FileDisplayType.FOLDER);
			}
		});
		mAdd.add(miAddFolder);

		miAddFile = new JMenuItem(Messages.getString("ML.DisplayPanel.Menu.AddFile"));
		miAddFile.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "filefolderfile_single-16.png")));
		miAddFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addNode(FileDisplayType.FILE);
			}
		});
		mAdd.add(miAddFile);
		
		mAdd.addSeparator();
		
		//Get, sort and add plugins to context menu
		List<FileDetailPlugin> plugins = PluginsFactory.getFileDetailPlugins();
		Collections.sort(plugins, new Comparator<FileDetailPlugin>() {
			@Override
            public int compare(FileDetailPlugin o1, FileDetailPlugin o2) {
	            return o1.getName().compareTo(o2.getName());
            }
		});
		
		for(FileDetailPlugin entry : plugins){
			if(entry.isAvailable()){
    			FileEntryPluginMenuItem dynItem = new FileEntryPluginMenuItem(entry);
    			dynItem.addActionListener(new ActionListener() {
    				
    				@Override
    				public void actionPerformed(ActionEvent e) {
    					if (tree.getSelectionPath() != null) {						
    						DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
    						if (parent.getUserObject() instanceof DOFileEntryFolder) {
    							DOFileEntryFolder parentFolder = (DOFileEntryFolder) parent.getUserObject();
    							FileDetailPlugin plugin = ((FileEntryPluginMenuItem)e.getSource()).getPlugin();
    
    							String configDir = PMS.getConfiguration().getProfileDirectory() + File.separatorChar + "mlx_fileentry_plugin_configs" + File.separatorChar;		
    							File cfgDir = new File(configDir);
    							if(!cfgDir.isDirectory()){
    								cfgDir.mkdirs();
    							}
    							
    							File configFile;
    							int i = 1;
    							do {
    								configFile = new File(configDir + plugin.getClass().getSimpleName() + "_" + i++ + ".cfg");
    							} while (configFile.exists());
    							
    							DOFileEntryPlugin fileEntry = new DOFileEntryPlugin(-1, parentFolder, 0, "", parentFolder.getThumbnailPriorities(), parentFolder.getMaxLineLength(), plugin, configFile.getAbsolutePath());
    							
    							if(plugin.getConfigurationPanel() != null){
    								if(configFile.exists()){
    									try {
    	                                    plugin.loadConfiguration(configFile.getAbsolutePath());
                                        } catch (IOException ex) {
    	                                    log.error(String.format("Failed to load configuration file %s for plugin of type %s", configFile.getAbsoluteFile(), plugin.getClass().getName()), ex);
                                        }
    								}
                                    showPluginDialog(fileEntry, true);
    							} else {
    								DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(fileEntry);
    								treeModel.insertNodeInto(newNode, (MutableTreeNode) tree.getSelectionPath().getLastPathComponent(), 0);
    								tree.setSelectionPath(new TreePath(newNode.getPath()));
    								tree.startEditingAtPath(tree.getSelectionPath());
    							}
    						}
    					}
    				}
    			});
    			mAdd.add(dynItem);
    		}			
		}

		miRename = new JMenuItem(Messages.getString("ML.DisplayPanel.Menu.Rename"));
		miRename.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "rename-16.png")));
		miRename.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				renameSelectedNode();
			}
		});
		miRemove = new JMenuItem(Messages.getString("ML.DisplayPanel.Menu.Delete"));
		miRemove.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "delete-16.png")));
		miRemove.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedNodeRequested();
			}
		});
		miEdit = new JMenuItem(Messages.getString("ML.DisplayPanel.Menu.Edit"));
		miEdit.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "edit-16.png")));
		miEdit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editNodeRequested();
			}
		});

		treeContextMenu.add(mAdd);
		treeContextMenu.add(miEdit);
		treeContextMenu.add(miRename);
		treeContextMenu.add(miRemove);
	}

	private void showPluginDialog(DOFileEntryPlugin plugin, boolean isNew) {
		if(plugin == null){
			return;
		}
		
		FileEntryPluginDialog d = new FileEntryPluginDialog(plugin, isNew);
		d.addFileEntryPluginDialogActionListener(new FileEntryPluginDialogActionListener() {
			
			@Override
			public void fileEntryPluginDialogActionReceived(FileEntryPluginDialogActionEvent e) {				
				if (e.getActionType() == DialogActionType.APPLY || e.getActionType() == DialogActionType.OK) {
					try {
		                e.getFileEntryPlugin().getPlugin().saveConfiguration(e.getFileEntryPlugin().getPluginConfigFilePath());
	                } catch (IOException e1) {
		                log.error(String.format("Failed to save config file %s for plugin %s", e.getFileEntryPlugin().getPluginConfigFilePath(), e.getFileEntryPlugin().getClass().getName()), e1);
	                }
	                
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(e.getFileEntryPlugin());
					DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
					if(e.isNew()) {
						treeModel.insertNodeInto(newNode, selNode, getNewNodeInsertPosition(e.getFileEntryPlugin(), selNode));
						tree.setSelectionPath(new TreePath(newNode.getPath()));
						((FileEntryPluginDialog)e.getSource()).setIsNew(false);
					} else {
						selNode.setUserObject(e.getFileEntryPlugin());		
						treeModel.reload(selNode);
					}
				}

                if (e.getActionType() == DialogActionType.OK || e.getActionType() == DialogActionType.CANCEL) {
                		((FileEntryPluginDialog)e.getSource()).dispose();
				}
			}
		});
		d.setMinimumSize(new Dimension(600, 300));
		d.setModal(true);
		d.setLocation(GUIHelper.getCenterDialogOnParentLocation(getSize(), this));
		d.setVisible(true);
	}

	private void addNode(FileDisplayType fileDisplayType) {
		if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (parentNode.getUserObject() instanceof DOFileEntryFolder) {
				DOFileEntryFolder parentFolder = (DOFileEntryFolder) parentNode.getUserObject();
				DOFileEntryBase fileEntry = new DOFileEntryBase(-1, parentFolder, 0, "", parentFolder.getThumbnailPriorities(), fileDisplayType, parentFolder.getMaxLineLength(), null, null);
				ConfigureFileEntryDialog d = new ConfigureFileEntryDialog(fileEntry, parentFolder, folder.getFileType());
				d.addConfigureFileDialogDialogListener(new ConfigureFileDialogListener() {

					@Override
					public void configureFileDialogAction(FilterFileDialogDialogEventArgs e) {
						switch (e.getActionType()) {
							case OK:
								DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(e.getEntry());
								DefaultMutableTreeNode nodeToDropOnto = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
								treeModel.insertNodeInto(newNode, nodeToDropOnto, getNewNodeInsertPosition(e.getEntry(), nodeToDropOnto));
								tree.expandPath(tree.getSelectionPath());
								break;
						}
						((ConfigureFileEntryDialog) e.getSource()).dispose();
					}
				});
				d.setModal(true);
				d.pack();
				d.setLocation(GUIHelper.getCenterDialogOnParentLocation(d.getSize(), tree));
				d.setVisible(true);
			}
		}
	}
	
	private int getNewNodeInsertPosition(DOFileEntryBase fileEntry, DefaultMutableTreeNode nodeToDropOnto){
		int res = 0;
		if(!isFolder(fileEntry)){
        	for(int i = 0; i <nodeToDropOnto.getChildCount(); i++){
        		if(!isFolder((DOFileEntryBase)((DefaultMutableTreeNode)nodeToDropOnto.getChildAt(i)).getUserObject())){
        			res = i;
        			break;
        		}
        	}  
        	if(res == -1){
        		res = nodeToDropOnto.getChildCount();
        	}		
		}
		return res;
	}
    
    private boolean isFolder(DOFileEntryBase fileEntry){
    	boolean res = true;
    	if(fileEntry instanceof DOFileEntryFile
    			|| (fileEntry instanceof DOFileEntryPlugin && !((DOFileEntryPlugin)fileEntry).getPlugin().isFolder())
    			|| (fileEntry instanceof DOFileEntryInfo)) {
    		res = false;
    	}
    	return res;
    }

	private void renameSelectedNode() {
		tree.startEditingAtPath(tree.getSelectionPath());
	}

	private void deleteSelectedNodeRequested() {
		if (tree.getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode nodeToRemove = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (nodeToRemove.getParent() == null) {
				JOptionPane.showMessageDialog(getTopLevelAncestor(), Messages.getString("ML.DisplayPanel.DontDeleteRootMsg"));
				return;
			}

			treeModel.removeNodeFromParent(nodeToRemove);
		}
	}

	private void editNodeRequested() {
		if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if(dtn.getUserObject() instanceof DOFileEntryPlugin) {
                showPluginDialog((DOFileEntryPlugin) dtn.getUserObject(), false);
			}else if (dtn.getUserObject() instanceof DOFileEntryBase) {
				DOFileEntryBase f = (DOFileEntryBase) dtn.getUserObject();

				ConfigureFileEntryDialog d = new ConfigureFileEntryDialog(f, f.getParent(), folder.getFileType());
				d.addConfigureFileDialogDialogListener(new ConfigureFileDialogListener() {

					@Override
					public void configureFileDialogAction(FilterFileDialogDialogEventArgs e) {
						if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
							DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
							switch (e.getActionType()) {
								case OK:
									dtn.setUserObject(e.getEntry());
									break;
							}
							((ConfigureFileEntryDialog) e.getSource()).dispose();
						}
					}
				});
				d.setModal(true);
				d.pack();
				d.setLocation(GUIHelper.getCenterDialogOnParentLocation(d.getSize(), tree));
				d.setVisible(true);
			}
		}
	}

	private void clearTree() {
		if (treeModel != null && treeModel.getRoot() != null) {
			((DefaultMutableTreeNode) treeModel.getRoot()).removeAllChildren();
		}
	}

	private DOFileEntryFolder getFileFolder() {
		DOFileEntryFolder rootFolder = null;
		if (treeModel != null && treeModel.getRoot() instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
			if (rootNode.getUserObject() instanceof DOFileEntryFolder) {
				rootFolder = (DOFileEntryFolder) rootNode.getUserObject();
				rootFolder = addChildFolders(rootNode, rootFolder);
			}
		}
		return rootFolder;
	}

	private DOFileEntryFolder addChildFolders(DefaultMutableTreeNode node, DOFileEntryFolder folder) {
		int positionInParent = 0;
		DOFileEntryFolder parentFolder;
		if (node.getUserObject() instanceof DOFileEntryFolder) {
			parentFolder = (DOFileEntryFolder) node.getUserObject();
			parentFolder.getChildren().clear();
		} else {
			return folder;
		}

		for (int i = 0; i < node.getChildCount(); i++) {

			if (node.getChildAt(i) instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
				if (childNode.getUserObject() instanceof DOFileEntryBase) {
					DOFileEntryBase childFile = (DOFileEntryBase) childNode.getUserObject();
					childFile.setParent(parentFolder);
					childFile.setPositionInParent(positionInParent++);

					folder.getChildren().add(childFile);
					if (childFile instanceof DOFileEntryFolder) {
						addChildFolders(childNode, (DOFileEntryFolder) childFile);
					}
				}
			}
		}

		return folder;
	}

	private void updateTemplateList() {
		cbTemplate.removeAllItems();

		for (DOTemplate template : storage.getAllTemplates()) {
			cbTemplate.addItem(new TemplateCBItem(template.getId(), template.getName()));
		}
	}

	private void refreshDisplayPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, fill:500:grow, 3px", // columns
		        "2dlu, p, 3px, p, 3px, p, 3px, p, 3px, p, 3px, p, 3px, fill:p:grow, 2dlu"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(getDisplayItemsPanel(), cc.xy(2, 2));
		builder.addSeparator(Messages.getString("ML.DisplayPanel.sSorting"), cc.xy(2, 4));
		builder.add(refreshSortPanel(), cc.xy(2, 6));
		builder.addSeparator(Messages.getString("ML.DisplayPanel.sDisplayAs"), cc.xy(2, 8));

		builder.add(refreshDisplayFileAsPanel(), cc.xy(2, 10));

		// file
		builder.add(pFileDispay, cc.xy(2, 14));

		// folder
		pTemplate = refreshTemplatePanel();
		builder.add(pTemplate, cc.xy(2, 12));
		builder.add(spTree, cc.xy(2, 14));

		removeAll();
		add(builder.getPanel());
		setFileType(folder.getFileType());
		validate();
	}

	private Component getDisplayItemsPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, p, 3px, fill:10:grow, 3px, p, 3px", // columns
		        "p"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		
		builder.add(cbDisplayItems, cc.xy(2, 1));
		builder.add(lMaxFiles, cc.xy(4, 1, CellConstraints.RIGHT, CellConstraints.CENTER));
		builder.add(tfMaxFiles, cc.xy(6, 1));
		
		return builder.getPanel();
	}

	private JPanel refreshSortPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, p, 3px, p, 3px, p, 7px, p, 3px, r:p:grow, 3px", // columns
		        "p"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(cbSortOption, cc.xy(2, 1));
		builder.add(cbSortType, cc.xy(4, 1));
		builder.add(rbSortAsc, cc.xy(6, 1));
		builder.add(rbSortDesc, cc.xy(8, 1));
		builder.add(cbInheritSort, cc.xy(10, 1));

		return builder.getPanel();
	}

	private JPanel refreshTemplatePanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, p, 3px, p, 3px, p, 3px, p, 3px, p, 3px, p, 3px, p, 3px", // columns
		        "p"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(lTemplate, cc.xy(2, 1));
		builder.add(cbTemplate, cc.xy(4, 1));
		builder.add(bNewTemplate, cc.xy(6, 1));
		builder.add(bEditTemplate, cc.xy(8, 1));
		builder.add(bDeleteTemplate, cc.xy(10, 1));
		builder.add(bTemplateApply, cc.xy(12, 1));
		builder.add(bTemplateCancel, cc.xy(14, 1));

		return builder.getPanel();
	}

	private JPanel refreshDisplayFileAsPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, p, 10px, p, 3px, r:p:grow, 3px", // columns
		        "p"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(rbDisplayItemAsFile, cc.xy(2, 1));
		builder.add(rbDisplayIemAsFolder, cc.xy(4, 1));
		builder.add(cbInheritDisplayFileAs, cc.xy(6, 1));

		return builder.getPanel();
	}

	private void updateGUI() {
		if (cbInheritSort.isEnabled()) {
			cbInheritSort.setSelected(folder.isInheritSort());
		} else {
			cbInheritSort.setSelected(false);
		}

		if (cbInheritDisplayFileAs.isEnabled()) {
			cbInheritDisplayFileAs.setSelected(folder.isInheritDisplayFileAs());
		} else {
			cbInheritDisplayFileAs.setSelected(false);
		}
		
		cbSortOption.setSelectedItem(new SortOptionCBItem(folder.getDisplayProperties().getSortOption(), Messages.getString("ML.SortOption." + folder.getDisplayProperties().getSortOption().toString())));

		FileDisplayProperties fdp = folder.getDisplayProperties();
		if (cbInheritSort.isSelected()) {
			// update the fields that have to be
			cbSortType.setSelectedItem(folderHelper.getConditionTypeCBItem(fdp.getSortType()));
			cbSortType.setEnabled(false);
			rbSortAsc.setSelected(fdp.isSortAscending());
			rbSortAsc.setEnabled(false);
			rbSortDesc.setEnabled(false);
			cbSortOption.setEnabled(false);
		} else {
			cbSortType.setEnabled(true);
			rbSortAsc.setEnabled(true);
			rbSortDesc.setEnabled(true);
			cbSortOption.setEnabled(true);
		}

		if (cbInheritDisplayFileAs.isSelected()) {
			if (fdp.getFileDisplayType() == FileDisplayType.FILE) {
				rbDisplayItemAsFile.setSelected(true);
			} else {
				rbDisplayIemAsFolder.setSelected(true);
			}
			pFileDispay.setThumbnailPriorities(fdp.getThumbnailPriorities());
			pFileDispay.setDisplayNameMask(fdp.getDisplayNameMask());
			cbTemplate.setSelectedItem(new TemplateCBItem(fdp.getTemplate().getId(), fdp.getTemplate().getName()));
			pFileDispay.setEnabled(false);
			rbDisplayItemAsFile.setEnabled(false);
			rbDisplayIemAsFolder.setEnabled(false);
			lTemplate.setEnabled(false);
			cbTemplate.setEnabled(false);
			bNewTemplate.setVisible(false);
			bEditTemplate.setVisible(false);
			bDeleteTemplate.setVisible(false);
		} else {
			pFileDispay.setEnabled(true);
			rbDisplayItemAsFile.setEnabled(true);
			rbDisplayIemAsFolder.setEnabled(true);
			lTemplate.setEnabled(true);
			if (!isCreatingTemplate && !isEditingTemplate) {
				cbTemplate.setEnabled(true);
				bNewTemplate.setVisible(true);
				bEditTemplate.setVisible(true);
				bDeleteTemplate.setVisible(true);
			}
		}

		if (rbDisplayItemAsFile.isSelected()) {
			pFileDispay.setVisible(true);

			pTemplate.setVisible(false);
			spTree.setVisible(false);
		} else if (rbDisplayIemAsFolder.isSelected()) {
			pFileDispay.setVisible(false);

			pTemplate.setVisible(true);
			spTree.setVisible(true);
		}

		if (folder.getFileType() != FileType.VIDEO) {
			rbDisplayIemAsFolder.setVisible(false);
			rbDisplayItemAsFile.setVisible(false);
		} else {
			rbDisplayIemAsFolder.setVisible(true);
			rbDisplayItemAsFile.setVisible(true);			
		}
	}

	private void showTree(long templateId) {
		showTree(storage.getFileFolder(templateId));
		expandAllNodes(tree, new TreePath((TreeNode) treeModel.getRoot()), true);
	}

	@SuppressWarnings("unchecked")
	private void expandAllNodes(JTree tree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
				TreeNode n = e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAllNodes(tree, path, expand);
			}
		}

		// Expand or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	private void showTree(DOFileEntryFolder fileFolder) {
		if (fileFolder != null) {
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(fileFolder);
			TemplateTreeCellRenderer cellRenderer = new TemplateTreeCellRenderer();
			treeModel = new DefaultTreeModel(rootNode);
			tree = new JTree(treeModel);
			tree.setCellRenderer(cellRenderer);
			tree.setRootVisible(true);
			tree.setAutoscrolls(true);
			tree.setEditable(false);

			tree.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					JTree tree = (JTree) e.getSource();
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					tree.setSelectionPath(selPath);

					if (e.getButton() == MouseEvent.BUTTON3) {
						Object o = tree.getLastSelectedPathComponent();
						if (tree.isEditable() && o != null && o instanceof DefaultMutableTreeNode) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
							if (node.getUserObject() instanceof DOFileEntryFolder) {
								mAdd.setVisible(true);
								miEdit.setEnabled(true);
							} else if (node.getUserObject() instanceof DOFileEntryFile) {
								mAdd.setVisible(false);
								miEdit.setEnabled(true);
							} else if (node.getUserObject() instanceof DOFileEntryInfo) {
								mAdd.setVisible(false);
								miEdit.setEnabled(true);
							} else if (node.getUserObject() instanceof DOFileEntryPlugin) {
								mAdd.setVisible(false);
								if(((DOFileEntryPlugin)node.getUserObject()).getPlugin().getConfigurationPanel() == null){
									miEdit.setEnabled(false);									
								} else {
									miEdit.setEnabled(true);
								}
							}
							treeContextMenu.show(tree, e.getX(), e.getY());
						}
					}
				}
			});

			tree.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() != null
					        && tree.getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
						if (selNode != null) {
							if (e.getKeyCode() == (KeyEvent.VK_DELETE | KeyEvent.VK_D)) {
								deleteSelectedNodeRequested();
							} else if (e.getKeyCode() == KeyEvent.VK_R) {
								renameSelectedNode();
							} else if (e.getKeyCode() == KeyEvent.VK_I) {
								addNode(FileDisplayType.INFO);
							} else if (e.getKeyCode() == KeyEvent.VK_O) {
								addNode(FileDisplayType.FOLDER);
							} else if (e.getKeyCode() == KeyEvent.VK_F) {
								addNode(FileDisplayType.FILE);
							}
						}
					}
				}
			});

			tree.setDropMode(DropMode.ON_OR_INSERT);

			tree.setCellEditor(new TemplateTreeCellEditor(tree, cellRenderer));

			showTree(rootNode);

			spTree.setViewportView(tree);
		}
	}

	private void showTree(DefaultMutableTreeNode folderNode) {
		assert (folderNode != null);

		if (folderNode.getUserObject() instanceof DOFileEntryFolder) {
			DOFileEntryFolder folder = (DOFileEntryFolder) folderNode.getUserObject();
			int nbMissing = 0;
			for (int i = 0; i < folder.getChildren().size(); i++) {
				DOFileEntryBase currFile = folder.getChildren().get(i);
				//don't show unavailable plugins
				if(currFile instanceof DOFileEntryPlugin && (((DOFileEntryPlugin)currFile).getPlugin() == null || !((DOFileEntryPlugin)currFile).getPlugin().isAvailable())){
					nbMissing++;
					continue;
				}
				
				DefaultMutableTreeNode newFolderNode = new DefaultMutableTreeNode(currFile);
				treeModel.insertNodeInto(newFolderNode, folderNode, currFile.getPositionInParent() - nbMissing);
				if (currFile instanceof DOFileEntryFolder) {
					showTree(newFolderNode);
				}
			}
		}
	}

	private class InternalKeyAdapter extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() != null
			        && tree.getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
				if (currentNode != null) {
					if (e.getKeyCode() == 127) { // delete
						treeModel.removeNodeFromParent(currentNode);
					}
				}
			}
		}
	}
}
