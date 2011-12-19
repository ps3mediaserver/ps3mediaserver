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

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.common.collect.ArrayListModel;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOTableColumnConfiguration;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionTypeCBItem;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;
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
	private boolean handleEvents;
	private boolean columnDragging;
	private List<DOFileInfo> files;

	public FileDisplayTable(FileType fileType) {
		super(new BorderLayout());
		handleEvents = false;
		setFileType(fileType);
		init();
		handleEvents = true;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setContent(List<DOFileInfo> files) {
		handleEvents = false;
		this.files = files;
		ArrayListModel<DOFileInfo> arrayListModel = new ArrayListModel<DOFileInfo>(files);
		SelectionInList<DOFileInfo> selectionInList = new SelectionInList<DOFileInfo>((ListModel) arrayListModel);
		table.setModel(new FileDisplayTableAdapter(selectionInList, getFileType()));
		
		for(DOTableColumnConfiguration cConf : FileDisplayTableAdapter.getColumnConfigurations(getFileType())){
			table.getColumn(cConf).setPreferredWidth(cConf.getWidth());
		}
		handleEvents = true;
	}

	private void init() {
		//configure the table
		table = new ETable();
		table.setColumnModel(new FileDisplayTableColumnModel());
		table.setDefaultRenderer(Integer.class, new FileDisplayTableCellRenderer());
		table.setDefaultRenderer(Double.class, new FileDisplayTableCellRenderer());
		table.setDefaultRenderer(Date.class, new DateCellRenderer());
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setAutoCreateRowSorter(true);
		table.setRowHeight(table.getRowHeight() + 4);
		
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
				//Trick: when changing column order, only refresh the data (to have the correct 
				//values for a column) once it has been put into its new location
				if(columnDragging) {
					refreshData();
					columnDragging = false;
				}
			}
		});

		//listen to column moves in order to store them and restore them later
		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnMoved(TableColumnModelEvent e) {
				columnDragging = true;
				//Trick: store the ordering on every index change, but only reload the data 
				//on final repositioning (above mouse listener)
				if(e.getFromIndex() != e.getToIndex()){
					storeColumnState();
				}
			}
			
			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				storeColumnState();
				refreshData();
			}
			
			@Override
			public void columnAdded(TableColumnModelEvent e) { 
				storeColumnState();
				
				TableColumn c = table.getColumnModel().getColumn(e.getToIndex());
				DOTableColumnConfiguration cConf = (DOTableColumnConfiguration) c.getIdentifier();
				handleAddColumn(c, cConf);
				
				refreshData();
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) { }	
			@Override
			public void columnMarginChanged(ChangeEvent e) { }		
		});
		
		//configure default column properties
		//and listen to header resizes in order to store them and restore them later
		for(int i = 0; i < table.getColumnCount(); i++) {
			TableColumn c = table.getColumnModel().getColumn(i);
			DOTableColumnConfiguration cConf = (DOTableColumnConfiguration) c.getIdentifier();
			c.setIdentifier(MediaLibraryStorage.getInstance().getTableColumnConfiguration(getFileType(), i));
			handleAddColumn(c, cConf);
		}
		
		//configure the context menu
		columnSelectorMenu = new JPopupMenu();
		columnSelectorMenu.setLayout(new SpringLayout());
		refreshContextMenu();
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(table.getPreferredSize());
		add(scrollPane);
	}
	
	private void handleAddColumn(TableColumn c, DOTableColumnConfiguration cConf){
		c.setIdentifier(cConf);
		c.setHeaderValue(cConf);
		c.setPreferredWidth(cConf.getWidth());
		
		c.addPropertyChangeListener(new PropertyChangeListener() {	
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if(handleEvents && e.getPropertyName().equals("preferredWidth")){
					TableColumn c = (TableColumn) e.getSource();
					DOTableColumnConfiguration cConf = (DOTableColumnConfiguration) c.getIdentifier();

					if (c.getWidth() != cConf.getWidth()) {
						cConf.setWidth(c.getWidth());
						MediaLibraryStorage.getInstance().updateTableColumnConfiguration(cConf, getFileType());
					}
				}
			}
		});
	}

	private void refreshContextMenu() {
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
		Collections.sort(sortedItems, new Comparator<ConditionTypeCBItem>() {
			@Override
			public int compare(ConditionTypeCBItem o1, ConditionTypeCBItem o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});

		//compute the deminsions of the final layout
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
		
		//add all elements to menu and set them selected if needed
		for(ConditionTypeCBItem ctItem : sortedItems){			
			DOTableColumnConfiguration cConf = MediaLibraryStorage.getInstance().getTableColumnConfiguration(getFileType(), ctItem.getConditionType());
			boolean isVisible = cConf != null;
			JCheckBoxMenuItem mi = new JCustomCheckBoxMenuItem(ctItem, isVisible);
			mi.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(handleEvents){
						JCustomCheckBoxMenuItem mi = (JCustomCheckBoxMenuItem) e.getSource();
						ConditionType conditionType = ((ConditionTypeCBItem)mi.getUserObject()).getConditionType();
						
						if(mi.isSelected()){
							int index = MediaLibraryStorage.getInstance().getTableConfigurationMaxColumnIndex(getFileType()) - 1;
							int columnWidth = 75;
							TableColumn newColumn = new TableColumn();
							newColumn.setIdentifier(new DOTableColumnConfiguration(conditionType, index, columnWidth));
							newColumn.setHeaderValue(new DOTableColumnConfiguration(conditionType, index, columnWidth));
							newColumn.setPreferredWidth(columnWidth);
							table.addColumn(newColumn);
						}else{
							if(table.getColumnCount() > 1){
								for(int i = 0; i < table.getColumnCount(); i++){
									TableColumn c = table.getColumnModel().getColumn(i);
									DOTableColumnConfiguration cConf = (DOTableColumnConfiguration) c.getIdentifier();
									if(cConf.getConditionType() == conditionType){
										table.getColumnModel().removeColumn(c);
										break;
									}
								}
							} else {
								//don't allow to remove the last column
								mi.setSelected(true);
							}
						}
					}
				}
			});
			
			columnSelectorMenu.add(mi);	
		}
		
		//add some invisible menu items to fill up the remaining spaces
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

	private void showAllColumns() {
		handleEvents = false;
		
		boolean hasChanged = false;
		List<DOTableColumnConfiguration> configuredColumns = MediaLibraryStorage.getInstance().getTableColumnConfiguration(getFileType());
		for(ConditionType ct : ConditionType.values()){
			if(ct == ConditionType.UNKNOWN) continue;
			if((getFileType() == FileType.FILE && ct.toString().startsWith("FILE"))
					|| (getFileType() == FileType.VIDEO && (ct.toString().startsWith("FILE") || ct.toString().startsWith("VIDEO")))
					|| (getFileType() == FileType.AUDIO && (ct.toString().startsWith("FILE") || ct.toString().startsWith("AUDIO")))
					|| (getFileType() == FileType.PICTURES && (ct.toString().startsWith("FILE") || ct.toString().startsWith("IMAGE")))){
				boolean alreadyConfigured = false;
				for(DOTableColumnConfiguration cConf : configuredColumns){
					if(cConf.getConditionType() == ct){
						alreadyConfigured = true;
						break;
					}
				}
				if(!alreadyConfigured){
					DOTableColumnConfiguration newCConf = new DOTableColumnConfiguration(ct, -1, 75);
					TableColumn newCol = new TableColumn();
					newCol.setIdentifier(newCConf);
					newCol.setHeaderValue(newCConf);
					table.addColumn(newCol);
					hasChanged = true;
				}
			}
		}		
		handleEvents = true;
		if(hasChanged){
			storeColumnState();
			refreshData();
		}
	}
	
	private void hideAllColumns(){
		handleEvents = false;
		while(table.getColumnCount() > 1){
			table.getColumnModel().removeColumn(table.getColumnModel().getColumn(1));
		}
		handleEvents = true;
		storeColumnState();
	}
	
	private void storeColumnState() {
		if(handleEvents){ 
			//clear all column configurations for the current type
			MediaLibraryStorage.getInstance().clearTableColumnConfiguration(getFileType());
			
			//save the state of the displayed columns
			for(int i = 0; i< table.getColumnCount(); i++){
				TableColumn c = table.getColumnModel().getColumn(i);
				DOTableColumnConfiguration cConf = (DOTableColumnConfiguration) c.getIdentifier();
				cConf.setColumnIndex(i);
				cConf.setWidth(c.getWidth());
				MediaLibraryStorage.getInstance().insertTableColumnConfiguration(cConf, getFileType());
			}
			refreshContextMenu();
		}
	}
	
	//Trick: this method is needed to reload all data once either
	//columns have been added/removed or re-ordered because the fields
	//get messed up otherwise. Any better mechanism is very welcome!
	private void refreshData() {
		if(handleEvents){
			setContent(files);
		}
	}
}