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
package net.pms.medialibrary.gui.shared;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.freixas.jcalendar.JCalendar;
import org.freixas.jcalendar.JCalendarCombo;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOCondition;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionOperatorCBItem;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionTypeCBItem;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionUnitCBItem;
import net.pms.medialibrary.commons.enumarations.ConditionOperator;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.ConditionUnit;
import net.pms.medialibrary.commons.enumarations.ConditionValueType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.events.ConditionRemoveEvent;
import net.pms.medialibrary.commons.events.ConditionRemoveListener;
import net.pms.medialibrary.commons.exceptions.ConditionException;
import net.pms.medialibrary.commons.helpers.FolderHelper;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ConditionEntryPanel implements ActionListener {
    private FileType fileType = FileType.UNKNOWN;
	private FolderHelper folderHelper = new FolderHelper();
	private ConditionValueType currentValueType = ConditionValueType.UNKNOWN;
	
	private JComboBox cbConditionType;
	private JComboBox cbConditionOperator;
	private JButton bRemove;
	private JLabel lName;
	private JTextField tfCondition;
	private JComboBox cbConditionUnit;
	private JCalendarCombo calendar;
	private JComboBox cbTagName;
	private JComponent cCondition = new JPanel(new GridLayout());
	
    private List<ConditionRemoveListener> conditionRemoveListseners = new ArrayList<ConditionRemoveListener>();
    private boolean isRefreshing = false;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public ConditionEntryPanel(String name, FileType fileType){
		this(new DOCondition(ConditionType.UNKNOWN, ConditionOperator.UNKNOWN, "", name, ConditionValueType.UNKNOWN, ConditionUnit.UNKNOWN, ""), fileType);
	}

	public ConditionEntryPanel(DOCondition condition, FileType fileType) {		
		this.fileType = fileType;
		currentValueType = condition.getValueType();
		
		init(condition);
		applyLayout(condition.getValueType());
		
		getCbConditionType().setSelectedItem(folderHelper.getConditionTypeCBItem(condition.getType()));
		getCbConditionOperator().setSelectedItem(folderHelper.getConditionOperatorCBItem(condition.getOperator()));
		cbConditionUnit.setSelectedItem(folderHelper.getConditionUnitCBItem(condition.getUnit()));
		cbTagName.setSelectedItem(condition.getTagName());
	}	
	
	public void addConditionRemoveListener(ConditionRemoveListener l){
		if(!conditionRemoveListseners.contains(l)){
			conditionRemoveListseners.add(l);
		}
	}

	public DOCondition getCondition() throws ConditionException {
		DOCondition tmpCon = new DOCondition();
		tmpCon.setName(getlName().getText());
		tmpCon.setValueType(currentValueType);
		if(getCbConditionType().getSelectedItem() != null && getCbConditionType().getSelectedItem() instanceof ConditionTypeCBItem){
			tmpCon.setType(((ConditionTypeCBItem) getCbConditionType().getSelectedItem()).getConditionType());
		}else{
			tmpCon.setType(ConditionType.UNKNOWN);
		}
		if(getCbConditionOperator().getSelectedItem() != null && getCbConditionOperator().getSelectedItem() instanceof ConditionOperatorCBItem){
			tmpCon.setOperator(((ConditionOperatorCBItem) getCbConditionOperator().getSelectedItem()).getConditionOperator());
		}else{
			tmpCon.setOperator(ConditionOperator.IS);
		}
		if(cbConditionUnit.getSelectedItem() != null && cbConditionUnit.getSelectedItem() instanceof ConditionUnitCBItem){
			tmpCon.setUnit(((ConditionUnitCBItem) cbConditionUnit.getSelectedItem()).getConditionUnit());
		}else{
			tmpCon.setUnit(ConditionUnit.UNKNOWN);
		}
		if(cbTagName.getSelectedItem() != null){
			tmpCon.setTagName(cbTagName.getSelectedItem().toString());
		}else{
			tmpCon.setTagName("");
		}
		
		if(tmpCon.getValueType() == ConditionValueType.DATETIME){
			tmpCon.setCondition(sdf.format(calendar.getDate()));
		} else {
			tmpCon.setCondition(tfCondition.getText().trim());			
		}
		
		//Validate condition
		String exceptionString = "";
		boolean isConditionValid = true;
		if(tmpCon.getValueType() == ConditionValueType.DOUBLE 
				|| tmpCon.getValueType() == ConditionValueType.FILESIZE){
			if (!tmpCon.getCondition().matches("^[0-9]+\\.?[0-9]*$")) {
				isConditionValid = false;
				exceptionString = String.format(Messages.getString("ML.ConditionPanel.ConditionInvalidDec"), tmpCon.getName(), System.getProperty("line.separator"));
	        }
		} else if(tmpCon.getValueType() == ConditionValueType.INTEGER 
				|| tmpCon.getValueType() == ConditionValueType.TIMESPAN){
			if (!tmpCon.getCondition().matches("^[0-9]+$")) {
				isConditionValid = false;
				exceptionString = String.format(Messages.getString("ML.ConditionPanel.ConditionInvalidInt"), tmpCon.getName(), System.getProperty("line.separator"));
		    }
		}
		
		if(!isConditionValid){
			throw new ConditionException(exceptionString);
		}
		
		return tmpCon;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public FileType getFileType() {
		return fileType;
	}

	public String getName(){
		return getlName().getText();
	}

	public JComboBox getCbConditionType() {
		return cbConditionType;
	}

	public JComboBox getCbConditionOperator() {
		return cbConditionOperator;
	}
	
	public JComponent getConditionOperatorPanel() {
		JPanel p = new JPanel(new BorderLayout());
		p.add(cbTagName, BorderLayout.WEST);
		p.add(getCbConditionOperator(), BorderLayout.CENTER);
		return p;
	}

	public JButton getBRemove() {
		return bRemove;
	}

	public JLabel getlName() {
		return lName;
	}
	
	public JComponent getCCondtion(){
		return cCondition;
	}

	private void applyLayout(ConditionValueType conditionValueType){
		FormLayout layoutCCondition = new FormLayout("10:grow, 3px, p", // columns
		"p"); // rows
		PanelBuilder builderCCondition = new PanelBuilder(layoutCCondition);
		builderCCondition.setOpaque(true);

		CellConstraints cc = new CellConstraints();

		switch (conditionValueType) {
			case TIMESPAN:		    	
				Object selectedItemCD = cbConditionUnit.getSelectedItem();
				cbConditionUnit.removeAllItems();
				for (ConditionUnit unit : ConditionUnit.values()) {
					if(unit.toString().startsWith("TIMESPAN_")){
						cbConditionUnit.addItem(new ConditionUnitCBItem(unit, Messages.getString("ML.Condition.Unit." + unit)));
					}
				}
				if (selectedItemCD != null) {
					cbConditionUnit.setSelectedItem(selectedItemCD);
				} else {
					cbConditionUnit.setSelectedItem(new ConditionUnitCBItem(ConditionUnit.TIMESPAN_MINUTES, Messages.getString("ML.Condition.Unit." + ConditionUnit.TIMESPAN_MINUTES)));
				}

				builderCCondition.add(tfCondition, cc.xy(1, 1));
				builderCCondition.add(cbConditionUnit, cc.xy(3, 1));
				break;
			case FILESIZE:
				selectedItemCD = cbConditionUnit.getSelectedItem();
				cbConditionUnit.removeAllItems();
				for (ConditionUnit unit : ConditionUnit.values()) {
					if(unit.toString().startsWith("FILESIZE_")){
						cbConditionUnit.addItem(new ConditionUnitCBItem(unit, Messages.getString("ML.Condition.Unit." + unit)));
					}
				}
				if (selectedItemCD != null) {
					cbConditionUnit.setSelectedItem(selectedItemCD);
				} else {
					cbConditionUnit.setSelectedItem(new ConditionUnitCBItem(ConditionUnit.FILESIZE_MEGABYTE, Messages.getString("ML.Condition.Unit." + ConditionUnit.FILESIZE_MEGABYTE)));
				}

				builderCCondition.add(tfCondition, cc.xy(1, 1));
				builderCCondition.add(cbConditionUnit, cc.xy(3, 1));
				break;
			case DATETIME:
				builderCCondition.add(calendar, cc.xyw(1, 1, 3));
				break;
			default:
				builderCCondition.add(tfCondition, cc.xyw(1, 1, 3));
				break;
		}
		cCondition.removeAll();
		cCondition.add(builderCCondition.getPanel());
		cCondition.validate();
	}
	
	private void conditionTypeOperatorChanged(){
		if(getCbConditionType().getSelectedItem() == null || !(getCbConditionType().getSelectedItem() instanceof ConditionTypeCBItem)
				|| (getCbConditionOperator().getSelectedItem() == null && !((ConditionTypeCBItem) getCbConditionType().getSelectedItem()).getConditionType().toString().contains("_CONTAINS_"))){
			return;
		}
		

		ConditionType ct = ((ConditionTypeCBItem) getCbConditionType().getSelectedItem()).getConditionType();
		ConditionOperator co = ConditionOperator.UNKNOWN;
		if(getCbConditionOperator().getSelectedItem() != null) {
			co = ((ConditionOperatorCBItem) getCbConditionOperator().getSelectedItem()).getConditionOperator();	
		}	

		cbTagName.setVisible(ct == ConditionType.FILE_CONTAINS_TAG);
		
		ConditionValueType cvt = folderHelper.getConditionValueType(ct, co);
		currentValueType = cvt;
		applyLayout(cvt);
	}
	
	private void conditionTypeChanged(){
		if(getCbConditionType().getSelectedItem() == null || !(getCbConditionType().getSelectedItem() instanceof ConditionTypeCBItem)){
			return;
		}
		
		ConditionType ct = ((ConditionTypeCBItem) getCbConditionType().getSelectedItem()).getConditionType();
		
		//TODO Only remove them if others have to be displayed
		Object selectedItem = getCbConditionOperator().getSelectedItem();
		getCbConditionOperator().removeAllItems();
		ConditionOperatorCBItem[] operators = folderHelper.getConditionOperators(ct);
		if(operators.length > 0){
			getCbConditionOperator().setEnabled(true);
        	for(ConditionOperatorCBItem fo : operators){
        		getCbConditionOperator().addItem(fo);
        	}
		} else {
			getCbConditionOperator().setEnabled(false);
		}
    	if(selectedItem != null){
    		getCbConditionOperator().setSelectedItem(selectedItem);
    	}
		
		conditionTypeOperatorChanged();
	}
	
	private void conditionOperatorChanged(){
		conditionTypeOperatorChanged();
	}
	
	private void init(DOCondition condition){
		lName = new JLabel(condition.getName());
			
		cbConditionType = new JComboBox(folderHelper.getFilteringConditionTypes(Arrays.asList(fileType)));
		cbConditionOperator = new JComboBox();
		cbConditionUnit = new JComboBox();
		cbTagName = new JComboBox(folderHelper.getExistingTags(fileType).toArray());
		cbTagName.setEditable(true);
		
		bRemove = new JButton(new ImageIcon(getClass().getResource("/resources/images/tp_remove.png")));
		getBRemove().setToolTipText(String.format(Messages.getString("ML.ConditionPanel.Remove"), condition.getName()));
		tfCondition = new JTextField(condition.getCondition());
		
		calendar = new JCalendarCombo(Calendar.getInstance(), Locale.getDefault(), JCalendar.DISPLAY_DATE | JCalendar.DISPLAY_TIME, false);
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, new Locale(net.pms.PMS.getConfiguration().getLanguage()));
		calendar.setDateFormat(df);
		
		if(condition.getValueType() == ConditionValueType.DATETIME){
    		Date selectDate = new  Date();
    		try {
    			selectDate = sdf.parse(condition.getCondition());
    		}catch(Exception ex){}
    		calendar.setDate(selectDate);
		}
		
		getCbConditionType().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {	    		
				if(!isRefreshing){
					conditionTypeChanged();
				}
			}
		});
		
		getCbConditionOperator().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!isRefreshing){
					conditionOperatorChanged();	
				}
			}
		});
		
		getBRemove().addActionListener(this);
	}

	@Override
    public void actionPerformed(ActionEvent e) {
	    if(e.getSource() == getBRemove()){
			for(ConditionRemoveListener l:conditionRemoveListseners){
				l.removeConditionReceived(new ConditionRemoveEvent(this));
			}		
	    }
    }
}
