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

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOCondition;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.enumarations.ConditionOperator;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.ConditionUnit;
import net.pms.medialibrary.commons.enumarations.ConditionValueType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.events.ConditionRemoveEvent;
import net.pms.medialibrary.commons.events.ConditionRemoveListener;
import net.pms.medialibrary.commons.exceptions.ConditionException;

public class FilterEditor extends JPanel {
	private static final long serialVersionUID = -4061458154905908214L;
	private int                       lastConditionNumber = 0;
	private JPanel                    pNoConditionsSet;
	private List<ConditionEntryPanel> conditionPanelsList;
	private JLabel                    lEquation;
	private JTextField                tfEquation;
	private JPanel                    pNewCondition;
	private JCheckBox                 cbEditEquation;
	private JButton                   bNewCondition;
	private JScrollPane spConditions;
	private FileType fileType;
	
	public FilterEditor(DOFilter filter, FileType fileType){
		super(new GridLayout());
		init();
		setFileType(fileType);
		setFilter(filter);
	}

	public DOFilter getFilter() throws ConditionException {

		List<DOCondition> conditions = new ArrayList<DOCondition>();
		String equation = tfEquation.getText().trim();
		for (ConditionEntryPanel conPan : conditionPanelsList) {
			conditions.add(conPan.getCondition());
		}

		return new DOFilter(equation, conditions);
	}
	
	public void setFilter(DOFilter filter) {
		tfEquation.setText(filter.getEquation());

		conditionPanelsList.clear();
		if (filter.getConditions().size() > 0) {
			lastConditionNumber = 0;
			for (int i = 0; i < filter.getConditions().size() && i < 40; i++) {
				DOCondition currCon = filter.getConditions().get(i);
				if(currCon.getName().startsWith("c")){
					int currConditionNumber = Integer.parseInt(currCon.getName().substring(1, currCon.getName().length()));
					if (currConditionNumber > lastConditionNumber) {
						lastConditionNumber = currConditionNumber;
					}
				}
				addCondition(currCon);
			}
		}
		
		refreshPanel();
	}

	public void setFileType(FileType fileType) {
		if(conditionPanelsList != null){
			for (ConditionEntryPanel p : conditionPanelsList) {
				p.setFileType(fileType);
			}
		}
		this.fileType = fileType;
	}

	private FileType getFileType() {
		return fileType;
	}

	private void init(){
		
		// Equation
		lEquation = new JLabel(Messages.getString("ML.ConditionPanel.lEquation"));
		tfEquation = new JTextField();

		// Conditions
		spConditions = new JScrollPane();
		spConditions.setBorder(BorderFactory.createEmptyBorder());
		
		// Create the panel that will be displayed if no conditions are set
		pNoConditionsSet = new JPanel();
		pNoConditionsSet.add(new JLabel(Messages.getString("ML.ConditionPanel.lNoConditionsSet")));

		// Add New Condition Button
		pNewCondition = new JPanel();
		bNewCondition = new JButton(Messages.getString("ML.ConditionPanel.bNewCondition"));
		bNewCondition.setName("buttonNewCondition");
		bNewCondition.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				addNewCondition();
			}
		});
		pNewCondition.add(bNewCondition);
		
		cbEditEquation = new JCheckBox(Messages.getString("ML.ConditionPanel.cbEditEquation"));
		cbEditEquation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateEquationEnabled();
			}
		});

		// Create the panels containing the conditions
		conditionPanelsList = new ArrayList<ConditionEntryPanel>();

		updateEquationEnabled();
	}
	
	private void addNewCondition(){
		// add a new condition
		if (conditionPanelsList.size() < 40) {
			if(conditionPanelsList.size() == 0){
				lastConditionNumber = 0;
			}
			String conName = "c" + (++lastConditionNumber);
			DOCondition initialCondition = new DOCondition(ConditionType.FILE_DATEINSERTEDDB, ConditionOperator.IS, "", conName,
			        ConditionValueType.DATETIME, ConditionUnit.UNKNOWN, "");
			addCondition(initialCondition);
			// update the equation
			String eq = tfEquation.getText();
			if (eq.length() == 0) {
				tfEquation.setText(conName);
			} else {
				tfEquation.setText(eq + " AND " + conName);
			}
			refreshPanel();
			spConditions.scrollRectToVisible(new Rectangle(1, spConditions.getPreferredSize().height, 1, 1));			
		} else {
			JOptionPane.showMessageDialog(this, String.format(Messages.getString("ML.ConditionPanel.ReachedFolderLimitMsg"), 40));
		}
	}
	
	private void addCondition(DOCondition condition){
		ConditionEntryPanel cp = new ConditionEntryPanel(condition, getFileType());
		cp.addConditionRemoveListener(new ConditionRemoveListener() {
			
			@Override
			public void removeConditionReceived(ConditionRemoveEvent event) {
				removeCondition((ConditionEntryPanel) event.getSource());
			}
		});		
		conditionPanelsList.add(cp);
	}
	
	private void removeCondition(ConditionEntryPanel entry){
		if (conditionPanelsList.contains(entry)) {
			String conName = entry.getName();
			String equation = tfEquation.getText();

			equation = removeConditionFromEquation(conName, equation);

			tfEquation.setText(equation.trim());

			// remove the condition from the list
			conditionPanelsList.remove(entry);

			// rebuild the panel
			refreshPanel();
		}
	}

	private String removeConditionFromEquation(String conditionName, String equation) {
		if (!conditionName.equals("")) {
			if (equation.startsWith(conditionName)) {
				if (equation.contains("AND")) {
					equation = equation.replace(conditionName + " AND", "");
				} else {
					equation = equation.replace(conditionName, "");
				}
			} else if (equation.endsWith(conditionName)) {
				if (equation.contains("AND")) {
					equation = equation.replace("AND " + conditionName, "");
				} else {
					equation = equation.replace(conditionName, "");
				}
			} else {
				equation = equation.replace(" AND " + conditionName, "");
			}
		}
		return equation.trim();
	}

	private void refreshPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, p, 3px, fill:50:grow, 3px, p, 3px", // columns
		        "3px, fill:30:grow, 3px, p, p"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		PanelBuilder conBuilder;
		FormLayout conLayout = new FormLayout("3px, r:p, 3px, p, 3px, fill:p, 3px, fill:10:grow, 3px, p, 3px", // columns
		        "p, p, p, p, p, p, p, p, p, p, " +
		        "p, p, p, p, p, p, p, p, p, p, " +
		        "p, p, p, p, p, p, p, p, p, p, " +
		        "p, p, p, p, p, p, p, p, p, p, fill:p:grow"); // rows
		conBuilder = new PanelBuilder(conLayout);
		conBuilder.setOpaque(true);

		// Conditions
		if (conditionPanelsList.size() > 0) {
			// Add conditions if we've got any
			for (int i = 0; i < conditionPanelsList.size(); i++) {
				conBuilder.add(conditionPanelsList.get(i).getlName(), cc.xy(2, i + 1));
				conBuilder.add(conditionPanelsList.get(i).getCbConditionType(), cc.xy(4, i + 1));
				conBuilder.add(conditionPanelsList.get(i).getConditionOperatorPanel(), cc.xy(6, i + 1));
				conBuilder.add(conditionPanelsList.get(i).getCCondtion(), cc.xy(8, i + 1));
				conBuilder.add(conditionPanelsList.get(i).getBRemove(), cc.xy(10, i + 1));
			}
		} else {
			// Show the 'no conditions set' label if there are none
			conBuilder.add(pNoConditionsSet, cc.xyw(2, 41, 9, CellConstraints.CENTER, CellConstraints.CENTER));
		}
		spConditions.setViewportView(conBuilder.getPanel());

		builder.add(spConditions, cc.xyw(2, 2, 5));
		builder.add(lEquation, cc.xy(2, 4));
		builder.add(tfEquation, cc.xy(4, 4));
		builder.add(cbEditEquation, cc.xy(6, 4));
		builder.add(pNewCondition, cc.xyw(2, 5, 5));

		removeAll();
		
		add(builder.getPanel());
		validate();
	}

	private void updateEquationEnabled() {
		if (cbEditEquation.isSelected()) {
			tfEquation.setEditable(true);
		} else {
			tfEquation.setEditable(false);
		}
	}

	public void resetConditions() {
		conditionPanelsList.clear();
		tfEquation.setText("");
		refreshPanel();
	}

	public boolean hasConditions() {
	    return conditionPanelsList.size() > 0;
	}

}
