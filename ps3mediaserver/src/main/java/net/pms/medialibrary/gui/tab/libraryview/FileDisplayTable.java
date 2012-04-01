package net.pms.medialibrary.gui.tab.libraryview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.CellEditor;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.list.SelectionInList;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOQuickTagEntry;
import net.pms.medialibrary.commons.dataobjects.DOTableColumnConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionTypeCBItem;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.helpers.FileImportHelper;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.commons.interfaces.FileEditLinkedList;
import net.pms.medialibrary.commons.interfaces.IProgress;
import net.pms.medialibrary.external.FileImportPlugin;
import net.pms.medialibrary.gui.dialogs.FileImportTemplateDialog;
import net.pms.medialibrary.gui.dialogs.FileUpdateWithPluginDialog;
import net.pms.medialibrary.gui.dialogs.fileeditdialog.FileEditDialog;
import net.pms.medialibrary.gui.dialogs.quicktagdialog.QuickTagDialog;
import net.pms.medialibrary.gui.shared.DateCellRenderer;
import net.pms.medialibrary.gui.shared.EMenuItem;
import net.pms.medialibrary.gui.shared.ETable;
import net.pms.medialibrary.gui.shared.JCustomCheckBoxMenuItem;
import net.pms.medialibrary.gui.shared.SpringUtilities;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class FileDisplayTable extends JPanel {
	private static final long serialVersionUID = -3062848510551753642L;
	private static final Logger log = LoggerFactory.getLogger(FileDisplayTable.class);
	private final int MAX_MENUITEMS_PER_COLUMN = 20;
	
	private FileType fileType;
	private ETable table;
	private JPopupMenu columnSelectorMenu;
	
	private JPopupMenu fileEditMenu = new JPopupMenu();
	
	private boolean isUpdating = false;
	private boolean isColumnDragging = false;
	private int colMoveToIndex;
	private int colMoveFromIndex;
	
	private SelectionInList<DOFileInfo> selectionInList = new SelectionInList<DOFileInfo>();
	private List<DOQuickTagEntry> quickTags;

	public FileDisplayTable(FileType fileType) {
		super(new BorderLayout());
		setFileType(fileType);
		refreshQuickTags();
		init();
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setContent(List<DOFileInfo> files) {
		selectionInList.setList(files);
	}
	
	private void refreshQuickTags() {
		quickTags = MediaLibraryStorage.getInstance().getQuickTagEntries();
	}

	private void init() {
		initTable();
		
		updateTableModel();
		
		//configure the context menu for column selection
		columnSelectorMenu = new JPopupMenu();
		columnSelectorMenu.setLayout(new SpringLayout());
		refreshColumnSelectorMenu();
		
		//configure the context menu for file edition
		refreshFileEditMenu(); 
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(table.getPreferredSize());
		add(scrollPane);
	}
	
	private void initTable() {
		//configure the table
		table = new ETable();
		//align all the cells to the left and format the date according to available space
		table.setDefaultRenderer(Integer.class, new FileDisplayTableCellRenderer());
		table.setDefaultRenderer(Double.class, new FileDisplayTableCellRenderer());
		table.setDefaultRenderer(Date.class, new DateCellRenderer());
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setAutoCreateRowSorter(true);
		table.setRowHeight(table.getRowHeight() + 4);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		//listen to mouse events on header in order to show the context menu
		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3){
					columnSelectorMenu.setLocation(e.getLocationOnScreen());
					columnSelectorMenu.show(table.getTableHeader(), e.getX(), e.getY());
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				handleColumnMoved();
			}
		});
		
		//listen to column events like move, resize or add
		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
					
			@Override
			public void columnMoved(TableColumnModelEvent arg0) {
				if(arg0.getFromIndex() != (colMoveFromIndex > 0 ? colMoveFromIndex : arg0.getToIndex())) {
					if(!isColumnDragging) {
						colMoveFromIndex = arg0.getFromIndex();
						isColumnDragging = true;
					}
					colMoveToIndex = arg0.getToIndex();
				}
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent arg0) { }			
			@Override
			public void columnRemoved(TableColumnModelEvent arg0) { }	
			@Override
			public void columnMarginChanged(ChangeEvent arg0) { }			
			@Override
			public void columnAdded(TableColumnModelEvent arg0) {
				TableColumn c = ((DefaultTableColumnModel)arg0.getSource()).getColumn(arg0.getToIndex());
				c.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						TableColumn c = (TableColumn) e.getSource();
						if(!isUpdating && e.getPropertyName().equals("preferredWidth")) {
							//store the width of the column when it changes
							for(ConditionType ct : ConditionType.values()) {
								if(c.getHeaderValue().equals(Messages.getString("ML.Condition.Header.Type." + ct.toString()))) {
									MediaLibraryStorage.getInstance().updateTableColumnWidth(ct, c.getWidth(), fileType);
									break;
								}
							}
						}
					}
				});
			}
		});
		
		//listen to mouse events to select a row on right click and show the context menu
		//http://www.stupidjavatricks.com/?p=12
		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked( MouseEvent e )
			{
				if (SwingUtilities.isRightMouseButton(e))
				{
					int rowNumber = table.rowAtPoint(e.getPoint());

					//store for use on menu item click
					boolean doSelect = true;
					if(table.getSelectedRowCount() > 1) {
						for(int selectedRowNumber : table.getSelectedRows()) {
							if(selectedRowNumber == rowNumber) {
								doSelect = false;
								break;
							}
						}
					}
					
					if(doSelect) {
						table.getSelectionModel().setSelectionInterval(rowNumber, rowNumber);						
					}
					
					refreshFileEditMenu();

					//show the context menu
					fileEditMenu.show(table, e.getX(), e.getY());

					//make it look a bit nicer
					table.requestFocus();
				}
			}
		});
		
		//listen for key events
		table.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {				
				//handle quick tags
				for(DOQuickTagEntry quickTag : quickTags) {
					
					if(e.getKeyCode() != quickTag.getKeyCode()) {
						continue;
					}
					
					switch(quickTag.getKeyCombination()) {
					case Ctrl:
						if(!e.isControlDown()) {
							continue;
						}
						break;
					case Alt:
						if(!e.isAltDown()) {
							continue;
						}
						break;
					case Shift:
						if(!e.isShiftDown()) {
							continue;
						}
						break;
					case CtrlShift:
						if(!(e.isControlDown() && e.isShiftDown())) {
							continue;
						}
						break;
					case CtrlAlt:
						if(!(e.isControlDown() && e.isAltDown())) {
							continue;
						}
						break;
					case ShiftAlt:
						if(!(e.isShiftDown() && e.isAltDown())) {
							continue;
						}
						break;
					case CtrlShiftAlt:
						if(!(e.isShiftDown() && e.isShiftDown() && e.isAltDown())) {
							continue;
						}
						break;
					default:
						continue;
					}
					
					tagSelectedFiles(quickTag);
				}
				
				//handle the default events
				if(e.isControlDown()) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_ADD:
						List<DOFileInfo> updatedFiles = new ArrayList<DOFileInfo>();
						for(DOFileInfo fileInfo : getSelectedFiles()) {
								fileInfo.setPlayCount(fileInfo.getPlayCount() + 1);
								updatedFiles.add(fileInfo);
						}
						updateFiles(updatedFiles);
						break;
					case KeyEvent.VK_SUBTRACT:
						updatedFiles = new ArrayList<DOFileInfo>();
						for(DOFileInfo fileInfo : getSelectedFiles()) {
							if(fileInfo.getPlayCount() > 0) {
								fileInfo.setPlayCount(fileInfo.getPlayCount() - 1);
								updatedFiles.add(fileInfo);
							}
						}
						updateFiles(updatedFiles);
						break;
					case KeyEvent.VK_E:
						editSelectedFiles();
						break;
					}
				}
				

				if(!e.isShiftDown() && !e.isShiftDown() && !e.isAltDown()) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE) {
						deleteSelectedFiles();
					}		
				}
			}
		});
		
		//listen for cell edit events to update the changes in the DB
		CellEditorListener cellEditorListener = new CellEditorListener() {

			private boolean propertyChanged;

			@Override
			public void editingStopped(ChangeEvent e) {
				if(getSelectedFiles().size() != 1) {
					//only allow editing if a file is actually selected. This return should never be reached
					return;
				}
				
				FileDisplayTableAdapter tm = (FileDisplayTableAdapter) table.getModel();
				ConditionType ct = tm.getColumnConditionType(table.getSelectedColumn());
				DOFileInfo fileInfo = getSelectedFiles().get(0);

				Object obj = ((CellEditor) e.getSource()).getCellEditorValue();

				propertyChanged = false;
				fileInfo.addPropertyChangeListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						propertyChanged = true;
					}
				});
				
				if (obj instanceof Boolean) {
					boolean newVal = (Boolean) obj;
					switch (ct) {
					case FILE_ISACTIF:
						fileInfo.setActive(newVal);
						break;
					}
				}

				if (fileInfo instanceof DOVideoFileInfo) {
					DOVideoFileInfo video = (DOVideoFileInfo) fileInfo;

					if (obj instanceof String) {
						String newVal = (String) obj;
						switch (ct) {
						case VIDEO_NAME:
							video.setName(newVal);
							break;
						case VIDEO_ORIGINALNAME:
							video.setOriginalName(newVal);
							break;
						case VIDEO_SORTNAME:
							video.setSortName(newVal);
							break;
						case VIDEO_DIRECTOR:
							video.setDirector(newVal);
							break;
						case VIDEO_IMDBID:
							video.setImdbId(newVal);
							break;
						case VIDEO_HOMEPAGEURL:
							video.setHomepageUrl(newVal);
							break;
						case VIDEO_TRAILERURL:
							video.setTrailerUrl(newVal);
							break;
						case VIDEO_CERTIFICATION:
							video.getAgeRating().setLevel(newVal);
							break;
						case VIDEO_CERTIFICATIONREASON:
							video.getAgeRating().setReason(newVal);
							break;
						case VIDEO_TAGLINE:
							video.setTagLine(newVal);
							break;
						case VIDEO_OVERVIEW:
							video.setOverview(newVal);
							break;
						}
					} else if (obj instanceof Integer) {
						Integer newVal = (Integer) obj;
						switch (ct) {
						case VIDEO_YEAR:
							video.setYear(newVal);
							break;
						case VIDEO_TMDBID:
							video.setTmdbId(newVal);
							break;
						case VIDEO_RATINGPERCENT:
							video.getRating().setRatingPercent(newVal);
							break;
						case VIDEO_RATINGVOTERS:
							video.getRating().setVotes(newVal);
							break;
						case VIDEO_BUDGET:
							video.setBudget(newVal);
							break;
						case VIDEO_REVENUE:
							video.setRevenue(newVal);
							break;
						}
					}
				}
				
				if(propertyChanged) {
					MediaLibraryStorage.getInstance().updateFileInfo(fileInfo);
				}
			}

			@Override
			public void editingCanceled(ChangeEvent e) {
			}
		};


		table.getDefaultEditor(String.class).addCellEditorListener(cellEditorListener);
		table.getDefaultEditor(Integer.class).addCellEditorListener(cellEditorListener);
		table.getDefaultEditor(Boolean.class).addCellEditorListener(cellEditorListener);
	}

	private void updateFiles(List<DOFileInfo> updatedFiles) {
		for(DOFileInfo fileInfo : getSelectedFiles()) {
			MediaLibraryStorage.getInstance().updateFileInfo(fileInfo);
		}				
		table.validate();
		table.repaint();
	}

	private void refreshFileEditMenu() {
		String iconsFolder = "/resources/images/";
		
		fileEditMenu.removeAll();
		
		//edit
		JMenuItem miEdit = new JMenuItem(Messages.getString("ML.ContextMenu.EDIT"));
		miEdit.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "edit-16.png")));
		miEdit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {				
				editSelectedFiles();
			}
		});
		fileEditMenu.add(miEdit);

		//update
		JMenu mUpdate = new JMenu(Messages.getString("ML.ContextMenu.UPDATE"));
		mUpdate.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "update-16.png")));
		fileEditMenu.add(mUpdate);
		
		//sort the collection of templates by name
		List<DOFileImportTemplate> templates = MediaLibraryStorage.getInstance().getFileImportTemplates();
		Collections.sort(templates, new Comparator<DOFileImportTemplate>() {

			@Override
			public int compare(DOFileImportTemplate o1, DOFileImportTemplate o2) {
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
		});
		
		//add the templates
		for(DOFileImportTemplate template : templates) {
			final EMenuItem miTemplate = new EMenuItem(template);
			miTemplate.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					DOFileImportTemplate templateToUse = (DOFileImportTemplate)miTemplate.getUserObject();
					updateSelectedFiles(templateToUse);
				}
			});
			mUpdate.add(miTemplate);
		}

		//configure
		JMenuItem miConfigureTemplate = new JMenuItem(Messages.getString("ML.ContextMenu.CONFIGURE"));
		miConfigureTemplate.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "configure-16.png")));
		miConfigureTemplate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showFileImportTemplateDialog();
			}
		});
		mUpdate.addSeparator();
		mUpdate.add(miConfigureTemplate);
		
		//import by name or id
		if(getSelectedFiles().size() == 1) {
			JMenuItem miImportWithPlugin = new JMenuItem(Messages.getString("ML.ContextMenu.UPDATEBYNAMEORID"));
			miImportWithPlugin.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "id-16.png")));
			miImportWithPlugin.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					updateByNameOrId();
				}
			});
			mUpdate.addSeparator();
			mUpdate.add(miImportWithPlugin);
		}
		
		//tag
		JMenu mTag = new JMenu(Messages.getString("ML.ContextMenu.TAG"));
		mTag.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "tag-16.png")));
		fileEditMenu.add(mTag);
		
		//add quick tags
		for(DOQuickTagEntry quickTag : quickTags) {
			final EMenuItem miTemplate = new EMenuItem(quickTag);
			miTemplate.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					DOQuickTagEntry tagToUse = (DOQuickTagEntry)miTemplate.getUserObject();
					tagSelectedFiles(tagToUse);
				}
			});
			mTag.add(miTemplate);
		}

		//configure
		JMenuItem miConfigureTags = new JMenuItem(Messages.getString("ML.ContextMenu.CONFIGURE"));
		miConfigureTags.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "configure-16.png")));
		miConfigureTags.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				QuickTagDialog dialog = new QuickTagDialog(getFileType());
				dialog.pack();
				dialog.setMinimumSize(dialog.getSize());
				dialog.setLocation(GUIHelper.getCenterDialogOnParentLocation(dialog.getSize(), table));
				dialog.setModal(true);
				dialog.setVisible(true);

				refreshQuickTags();
			}
		});
		mTag.addSeparator();
		mTag.add(miConfigureTags);
		
		//mark as played
		JMenuItem miMarkPlayed = new JMenuItem(Messages.getString("ML.ContextMenu.MARKPLAYED"));
		miMarkPlayed.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "mark_played-16.png")));
		miMarkPlayed.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				List<DOFileInfo> updatedFiles = new ArrayList<DOFileInfo>();
				for(DOFileInfo fileInfo : getSelectedFiles()) {
					if(fileInfo.getPlayCount() == 0) {
						fileInfo.setPlayCount(1);
						updatedFiles.add(fileInfo);
					}
				}
				updateFiles(updatedFiles);
			}		
		});
		fileEditMenu.add(miMarkPlayed);
		
		//delete
		JMenuItem miDelete = new JMenuItem(Messages.getString("ML.ContextMenu.DELETE"));
		miDelete.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "delete-16.png")));
		miDelete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				deleteSelectedFiles();
			}
		});
		fileEditMenu.addSeparator();
		fileEditMenu.add(miDelete);
	}

	private void updateByNameOrId() {
		if(getSelectedFiles().size() == 1) {
			DOFileInfo fileInfo = getSelectedFiles().get(0);
			FileUpdateWithPluginDialog dialog = new FileUpdateWithPluginDialog(fileInfo);
			dialog.pack();
			dialog.setMinimumSize(dialog.getSize());
			dialog.setLocation(GUIHelper.getCenterDialogOnParentLocation(dialog.getSize(), this));
			dialog.setModal(true);
			dialog.setVisible(true);
			
			if(dialog.isUpdate()) {
				FileImportPlugin plugin = dialog.getPlugin();
				FileImportHelper.updateFileInfo(plugin, fileInfo);
				MediaLibraryStorage.getInstance().updateFileInfo(fileInfo);
			}
		}
	}

	private void showFileImportTemplateDialog() {
		//show the dialog
		FileImportTemplateDialog vid = new FileImportTemplateDialog(SwingUtilities.getWindowAncestor(this), 1);
		vid.setLocation(GUIHelper.getCenterDialogOnParentLocation(vid.getPreferredSize(), this));
		vid.setResizable(false);
		vid.setModal(true);
		
		vid.pack();
		vid.setVisible(true);
	}

	private void editSelectedFiles() {
		List<DOFileInfo> selectedFiles = getSelectedFiles();
		
		FileEditDialog editDialog;
		if(selectedFiles.size() == 1) {
			@SuppressWarnings("unchecked")
			FileEditLinkedList fel = new FileEditLinkedList() {
				AbstractTableAdapter<DOFileInfo> tm = (AbstractTableAdapter<DOFileInfo>) table.getModel();
				
				@Override
				public boolean hasPreviousFile() {
					return table.getSelectionModel().getLeadSelectionIndex() > 0;
				}
				
				@Override
				public boolean hasNextFile() {
					return table.getSelectionModel().getLeadSelectionIndex() + 1 < tm.getRowCount();
				}
				
				@Override
				public DOFileInfo getSelected() {
					return tm.getRow(table.convertRowIndexToModel(table.getSelectionModel().getLeadSelectionIndex()));
				}
				
				@Override
				public DOFileInfo selectPreviousFile() {
					int rowNumber = table.getSelectionModel().getLeadSelectionIndex() - 1;
					table.getSelectionModel().setSelectionInterval(rowNumber, rowNumber);
					table.scrollRectToVisible(new Rectangle(table.getCellRect(rowNumber, 0, true)));
					return getSelected();
				}
				
				@Override
				public DOFileInfo selectNextFile() {
					int rowNumber = table.getSelectionModel().getLeadSelectionIndex() + 1;
					table.getSelectionModel().setSelectionInterval(rowNumber, rowNumber);
					table.scrollRectToVisible(new Rectangle(table.getCellRect(rowNumber, 0, true)));
					return getSelected();
				}
			};
			editDialog = new FileEditDialog(fel);
			editDialog.setSize(getSingleEditSize());
			editDialog.setModal(true);
			editDialog.setLocation(GUIHelper.getCenterDialogOnParentLocation(editDialog.getSize(), table));
			editDialog.setVisible(true);
			setSingleEditSize(editDialog.getSize());
		} else {
			editDialog = new FileEditDialog(selectedFiles);
			editDialog.setSize(getMultiEditSize());
			editDialog.setModal(true);
			editDialog.setLocation(GUIHelper.getCenterDialogOnParentLocation(editDialog.getSize(), table));
			editDialog.setVisible(true);
			setMultiEditSize(editDialog.getSize());
		}
	}

	private void updateSelectedFiles(DOFileImportTemplate template) {
		List<DOFileInfo> filesToUpdate = getSelectedFiles();
		final int nbFilesToUpdate = filesToUpdate.size();
		IProgress progressReporter = new IProgress() {
			
			@Override
			public void workComplete() {
				repaint();
				PMS.get().getFrame().setStatusLine(String.format(Messages.getString("ML.Messages.UpdateFinished"), nbFilesToUpdate));
			}
			
			@Override
			public void reportProgress(int percentComplete) {
				repaint();
			}
		};
		
		FileImportHelper.updateFileInfos(template, filesToUpdate, true, progressReporter);
	}

	private void tagSelectedFiles(DOQuickTagEntry quickTag) {
		for (DOFileInfo file : getSelectedFiles()) {
			if(!file.getTags().containsKey(quickTag.getTagName())) {
				file.getTags().put(quickTag.getTagName(), new ArrayList<String>());
			}
			List<String> tagNames = file.getTags().get(quickTag.getTagName());
			if(!tagNames.contains(quickTag.getTagValue())) {
				tagNames.add(quickTag.getTagValue());
				MediaLibraryStorage.getInstance().updateFileInfo(file);
			}
		}
	}

	private void deleteSelectedFiles() {
		List<DOFileInfo> selectedFiles = getSelectedFiles();
		String questionStr;
		
		if(selectedFiles.size() == 0) {
			return;
		}
		
		if(selectedFiles.size() == 1) {
			questionStr = String.format(Messages.getString("ML.DeleteFileDialog.Option.SingleFile"), selectedFiles.get(0).getFilePath());
		} else {
			questionStr = String.format(Messages.getString("ML.DeleteFileDialog.Option.MultipleFiles"), selectedFiles.size());
		}
		
		Object[] options = { Messages.getString("ML.DeleteFileDialog.bCancel"), Messages.getString("ML.DeleteFileDialog.bDeleteFromComputer"),
				Messages.getString("ML.DeleteFileDialog.bRemoveFromLibrary") };
		int dialogResponse = JOptionPane.showOptionDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
						String.format(Messages.getString("ML.DeleteFileDialog.pQuestion"), questionStr), Messages.getString("ML.DeleteFileDialog.Header"),
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options,
						options[2]);
		
		boolean deleteFile = false;
		boolean removeFromLibrary = false;
		switch(dialogResponse) {
		case 0:
			//do nothing
			break;
		case 1:
			deleteFile = true;
			removeFromLibrary = true;
			break;
		case 2:
			removeFromLibrary = true;
			break;
		}
		
		if(removeFromLibrary) {
			for(DOFileInfo fileInfo : selectedFiles) {
				if(deleteFile) {
					File fDelete = new File(fileInfo.getFilePath());
					try {
						fDelete.delete();
						log.info("Deleted file " + fDelete.getAbsolutePath());
					} catch(SecurityException ex) {
						log.error("Failed to delete file " + fDelete.getAbsolutePath());
						return;
					}
				}
				
				MediaLibraryStorage.getInstance().deleteVideo(fileInfo.getId());
				selectionInList.getList().remove(fileInfo);
			}					
		}
	}
	
	private Dimension getMultiEditSize() {
		MediaLibraryStorage storage = MediaLibraryStorage.getInstance();
		int width = 750;
		int height = 430;
		try {
			width = Integer.parseInt(storage.getMetaDataValue("FileEditDialog_MultiEdit_Width"));
			height = Integer.parseInt(storage.getMetaDataValue("FileEditDialog_MultiEdit_Height"));
		} catch (NumberFormatException ex) { }
		return new Dimension(width, height);
	}
	
	private void setMultiEditSize(Dimension size) {
		MediaLibraryStorage storage = MediaLibraryStorage.getInstance();
		storage.setMetaDataValue("FileEditDialog_MultiEdit_Width", String.valueOf(size.width));
		storage.setMetaDataValue("FileEditDialog_MultiEdit_Height", String.valueOf(size.height));
	}

	private Dimension getSingleEditSize() {
		MediaLibraryStorage storage = MediaLibraryStorage.getInstance();
		int width = 750;
		int height = 420;
		try {
			width = Integer.parseInt(storage.getMetaDataValue("FileEditDialog_SingleEdit_Width"));
			height = Integer.parseInt(storage.getMetaDataValue("FileEditDialog_SingleEdit_Height"));
		} catch (NumberFormatException ex) { }
		return new Dimension(width, height);
	}
	
	private void setSingleEditSize(Dimension size) {
		MediaLibraryStorage storage = MediaLibraryStorage.getInstance();
		storage.setMetaDataValue("FileEditDialog_SingleEdit_Width", String.valueOf(size.width));
		storage.setMetaDataValue("FileEditDialog_SingleEdit_Height", String.valueOf(size.height));
	}

	private List<DOFileInfo> getSelectedFiles() {
		List<DOFileInfo> selectedFiles = new ArrayList<DOFileInfo>();
		@SuppressWarnings("unchecked")
		AbstractTableAdapter<DOFileInfo> tm = ((AbstractTableAdapter<DOFileInfo>) table.getModel());
		for(int rowNumber : table.getSelectedRows()) {
			selectedFiles.add(tm.getRow(table.convertRowIndexToModel(rowNumber)));
		}
		return selectedFiles;
	}
	
	private void handleColumnMoved() {
		if(colMoveFromIndex != colMoveToIndex) {
			MediaLibraryStorage.getInstance().moveTableColumnConfiguration(colMoveFromIndex, colMoveToIndex, getFileType());		
			colMoveFromIndex = colMoveToIndex = 0;
		}
		isColumnDragging = false;
	}

	private void refreshColumnSelectorMenu() {
		//clear all the menu items
		columnSelectorMenu.removeAll();
		
		//create the list of items that will be shown
		List<ConditionTypeCBItem> sortedItems = new ArrayList<ConditionTypeCBItem>();
		for(ConditionType ct : ConditionType.values()){
			if(ct == ConditionType.UNKNOWN) continue;
			if((getFileType() == FileType.FILE && ct.toString().startsWith("FILE"))
					|| (getFileType() == FileType.VIDEO && (ct.toString().startsWith("FILE") || ct.toString().startsWith("VIDEO")))
					|| (getFileType() == FileType.AUDIO && (ct.toString().startsWith("FILE") || ct.toString().startsWith("AUDIO")))
					|| (getFileType() == FileType.PICTURES && (ct.toString().startsWith("FILE") || ct.toString().startsWith("IMAGE")))){
				sortedItems.add(new ConditionTypeCBItem(ct, Messages.getString("ML.Condition.Header.Type." + ct.toString())));
			}
		}
		
		//sort the items by their localized name
		Collections.sort(sortedItems, new Comparator<ConditionTypeCBItem>() {
			@Override
			public int compare(ConditionTypeCBItem o1, ConditionTypeCBItem o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});

		//compute the dimension of the final layout
		int nbItemsMin = sortedItems.size() + 3;
		int rows = nbItemsMin > MAX_MENUITEMS_PER_COLUMN ? MAX_MENUITEMS_PER_COLUMN : nbItemsMin;
		int cols = (nbItemsMin > MAX_MENUITEMS_PER_COLUMN ? (int)Math.ceil((double)nbItemsMin / MAX_MENUITEMS_PER_COLUMN) : 1);
		
		//create the first two lines of the menu item, where the first line will contain the default items
		//plus the ones needed for filling it up.
		//the second line will consist of separators
		JMenuItem miShowAll = new JMenuItem(Messages.getString("ML.FileDisplayTable.CntextMenu.ShowAll"));
		miShowAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showAllColumns();
			}
		});
		columnSelectorMenu.add(miShowAll);
		JMenuItem miHideAll = new JMenuItem(Messages.getString("ML.FileDisplayTable.CntextMenu.HideAll"));
		miHideAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				hideAllColumns();
			}
		});
		columnSelectorMenu.add(miHideAll);
		
		for(int i = 2 ; i < cols; i++){
			JMenuItem miDummy = new JMenuItem();
			miDummy.setVisible(false);
			columnSelectorMenu.add(miDummy);
		}

		for(int i = 0 ; i < cols; i++){
			columnSelectorMenu.addSeparator();
		}
		
		//add all elements to menu, attach the listener handling the click event and set them selected if needed
		for(ConditionTypeCBItem ctItem : sortedItems) {
			DOTableColumnConfiguration cConf = MediaLibraryStorage.getInstance().getTableColumnConfiguration(getFileType(), ctItem.getConditionType());
			boolean isVisible = cConf != null;
			JCheckBoxMenuItem mi = new JCustomCheckBoxMenuItem(ctItem, isVisible);
			mi.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
						JCustomCheckBoxMenuItem mi = (JCustomCheckBoxMenuItem) e.getSource();
						ConditionType conditionType = ((ConditionTypeCBItem)mi.getUserObject()).getConditionType();
						
						if(mi.isSelected()){
							//add the column
							int colIndex = MediaLibraryStorage.getInstance().getTableConfigurationMaxColumnIndex(getFileType()) + 1;
							int rowWidth = 75;
							
							TableColumn newCol = new TableColumn();
							newCol.setHeaderValue(Messages.getString("ML.Condition.Header.Type." + conditionType.toString()));
							newCol.setWidth(rowWidth);
							newCol.setModelIndex(colIndex);

							//insert the column into the db before triggering the update to load the data properly
							MediaLibraryStorage.getInstance().insertTableColumnConfiguration(new DOTableColumnConfiguration(conditionType, colIndex, rowWidth), fileType);

							updateTableModel();
						} else {
							//remove the column
							if(table.getColumnCount() > 1){
								for(int i = 0; i < table.getColumnCount(); i++){
									TableColumn c = table.getColumn(table.getColumnName(i));

									if(c.getHeaderValue().equals(Messages.getString("ML.Condition.Header.Type." + conditionType.toString()))) {
										//delete the column from the db before triggering the update to remove the row properly
										MediaLibraryStorage.getInstance().deleteTableColumnConfiguration(new DOTableColumnConfiguration(conditionType, c.getModelIndex(), 0), fileType);

										updateTableModel();
										
										break;
									}
								}
							} else {
								//don't allow to remove the last column
								mi.setSelected(true);
							}
						}
				}
			});
			
			columnSelectorMenu.add(mi);	
		}
		
		//add some invisible menu items to fill up the remaining spaces if required
		while(rows * cols - cols >= columnSelectorMenu.getComponentCount()){
			rows--;
		}
		
		while(columnSelectorMenu.getComponentCount() < rows * cols){
			JMenuItem miDummy = new JMenuItem();
			miDummy.setVisible(false);
			columnSelectorMenu.add(miDummy);
		}
		
		//Lay out the panel.
		SpringUtilities.makeCompactGrid(columnSelectorMenu, //parent
		                                rows, cols,
		                                3, 3,  //initX, initY
		                                3, 3); //xPad, yPad
	}

	private void updateTableModel() {
		isUpdating = true;
		
		//rebuild the entire adapter, because their is no way to change the column names after initialization
		//table.removeColumn and table.getColumnModel().removeColumn have no effect
		table.setModel(new FileDisplayTableAdapter(selectionInList, getFileType()));										
		for(DOTableColumnConfiguration cConf : FileDisplayTableAdapter.getColumnConfigurations(getFileType())){
			table.getColumn(cConf.toString()).setPreferredWidth(cConf.getWidth());
		}
		
		isUpdating = false;
	}

	private void showAllColumns() {
		List<DOTableColumnConfiguration> configuredColumns = MediaLibraryStorage.getInstance().getTableColumnConfiguration(getFileType());
		for(ConditionType ct : ConditionType.values()){
			//don't show an entry for the type unkown
			if(ct == ConditionType.UNKNOWN){
				continue;
			}
			
			//only add entries for the current file type
			if((getFileType() == FileType.FILE && ct.toString().startsWith("FILE"))
					|| (getFileType() == FileType.VIDEO && (ct.toString().startsWith("FILE") || ct.toString().startsWith("VIDEO")))
					|| (getFileType() == FileType.AUDIO && (ct.toString().startsWith("FILE") || ct.toString().startsWith("AUDIO")))
					|| (getFileType() == FileType.PICTURES && (ct.toString().startsWith("FILE") || ct.toString().startsWith("IMAGE")))){
				boolean alreadyConfigured = false;
				
				//only add columns which aren't displayed yet
				for(DOTableColumnConfiguration cConf : configuredColumns){
					if(cConf.getConditionType() == ct){
						alreadyConfigured = true;
						break;
					}
				}
				
				if(!alreadyConfigured) {
					int columnWidth = 75;
					int columnIndex = MediaLibraryStorage.getInstance().getTableConfigurationMaxColumnIndex(getFileType()) + 1;
					MediaLibraryStorage.getInstance().insertTableColumnConfiguration(new DOTableColumnConfiguration(ct, columnIndex, columnWidth), getFileType());
				}
			}
		}
		refreshColumnSelectorMenu();
		updateTableModel();
	}
	
	private void hideAllColumns() {
		MediaLibraryStorage.getInstance().deleteAllTableColumnConfiguration(getFileType());
		refreshColumnSelectorMenu();
		updateTableModel();
	}
}