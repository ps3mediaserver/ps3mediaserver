package net.pms.medialibrary.gui.tab.libraryview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.list.SelectionInList;
import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOTableColumnConfiguration;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionTypeCBItem;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.gui.dialogs.fileeditdialog.FileEditDialog;
import net.pms.medialibrary.gui.shared.DateCellRenderer;
import net.pms.medialibrary.gui.shared.ETable;
import net.pms.medialibrary.gui.shared.JCustomCheckBoxMenuItem;
import net.pms.medialibrary.gui.shared.SpringUtilities;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class FileDisplayTable extends JPanel {
	private static final long serialVersionUID = -3062848510551753642L;
	private final int MAX_MENUITEMS_PER_COLUMN = 20;
	
	private FileType fileType;
	private ETable table;
	private JPopupMenu columnSelectorMenu;
	
	private JPopupMenu fileEditMenu;
	private DOFileInfo fileToEdit;
	
	private boolean isUpdating = false;
	private boolean isColumnDragging = false;
	private int colMoveToIndex;
	private int colMoveFromIndex;
	
	private SelectionInList<DOFileInfo> selectionInList = new SelectionInList<DOFileInfo>();

	public FileDisplayTable(FileType fileType) {
		super(new BorderLayout());
		setFileType(fileType);
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

	private void init() {
		//configure the table
		table = new ETable();
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
		table.addMouseListener( new MouseAdapter()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void mouseClicked( MouseEvent e )
			{
				if (SwingUtilities.isRightMouseButton(e))
				{
					AbstractTableAdapter<DOFileInfo> tm = ((AbstractTableAdapter<DOFileInfo>) table.getModel());
					int rowNumber = table.rowAtPoint(e.getPoint());

					//store for use on menu item click
					fileToEdit = tm.getRow(rowNumber);
					
					// select the clicked row
					boolean doSelect = true;
					if(table.getSelectedRowCount() > 0) {
						for(DOFileInfo fi : getSelectedFiles()) {
							if(fi.equals(fileToEdit)) {
								doSelect = false;
								break;
							}
						}
					}
					
					if(doSelect) {
						table.getSelectionModel().setSelectionInterval(rowNumber, rowNumber);
					}

					//show the context menu
					fileEditMenu.show(table, e.getX(), e.getY());

					//make it look a bit nicer
					table.requestFocus();
				}
			}
		});
		
		updateTableModel();
		
		//configure the context menu for column selection
		columnSelectorMenu = new JPopupMenu();
		columnSelectorMenu.setLayout(new SpringLayout());
		refreshColumnSelectorMenu();
		
		//configure the context menu for file edition
		initFileEditMenu(); 
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(table.getPreferredSize());
		add(scrollPane);
	}
	
	private void initFileEditMenu() {
		String iconsFolder = "/resources/images/";
		
		fileEditMenu = new JPopupMenu();
		
		JMenuItem editMenuItem = new JMenuItem(Messages.getString("ML.ContextMenu.EDIT"));
		editMenuItem.setIcon(new ImageIcon(getClass().getResource(iconsFolder + "edit-16.png")));
		editMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {				
				//create the edit dialog
				FileEditDialog fed = new FileEditDialog(fileToEdit, getSelectedFiles());
				fed.setModal(true);
				fed.setResizable(false);
				fed.pack();
				fed.setLocation(GUIHelper.getCenterDialogOnParentLocation(fed.getSize(), table));
				fed.setVisible(true);
			}
		});
		fileEditMenu.add(editMenuItem);
	}
	
	private List<DOFileInfo> getSelectedFiles() {
		List<DOFileInfo> selectedFiles = new ArrayList<DOFileInfo>();
		@SuppressWarnings("unchecked")
		AbstractTableAdapter<DOFileInfo> tm = ((AbstractTableAdapter<DOFileInfo>) table.getModel());
		for(int rowNumber : table.getSelectedRows()) {
			selectedFiles.add(tm.getRow(rowNumber));
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