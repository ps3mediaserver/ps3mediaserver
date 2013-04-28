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
package net.pms.medialibrary.gui.dialogs.folderdialog;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.exceptions.ConditionException;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.gui.dialogs.ConditionsViewer;
import net.pms.medialibrary.gui.shared.FilterEditor;

class ConditionPanel extends JPanel {
	private static final long         serialVersionUID    = -9135369991397644949L;
	private DOMediaLibraryFolder      folder;
	private JCheckBox                 cbInheritConditions;
	private JScrollPane spConditions;
	private JLabel lInherit;
	private JButton bShowInherit;
	private FilterEditor filterEditor;

	ConditionPanel(DOMediaLibraryFolder f) {
		folder = f;
		setLayout(new GridLayout());
		
		initConditionPanel();
		refreshPanel();
		updateGUI();
	}

	boolean canInheritConditions() {
		return cbInheritConditions.isEnabled();
	}

	void setCanInheritConditions(boolean canInherit) {
		cbInheritConditions.setEnabled(canInherit);
	}
	
	boolean isInheritsConditions(){
		return cbInheritConditions.isSelected();
	}
	
	void setInheritsConditions(boolean inherit){
		cbInheritConditions.setSelected(inherit);
	}

	void resetConditions() {
		filterEditor.resetConditions();
	}

	DOFilter getFilter() throws ConditionException {
		return filterEditor.getFilter();
	}

	void setFileType(FileType fileType) {
		filterEditor.setFileType(fileType);
		updateGUI();
	}

	boolean hasConditions() {
		return filterEditor.hasConditions();
    }

	private void updateGUI() {

		if (canInheritConditions()) {
			cbInheritConditions.setEnabled(true);
			cbInheritConditions.setSelected(folder.isInheritsConditions());
		} else {
			cbInheritConditions.setEnabled(false);
			cbInheritConditions.setSelected(false);
		}
		
		lInherit.setVisible(cbInheritConditions.isSelected());
		bShowInherit.setVisible(cbInheritConditions.isSelected());
	}

	private void initConditionPanel() {
		spConditions = new JScrollPane();
		spConditions.setBorder(BorderFactory.createEmptyBorder());
		
		// Inheritance
		lInherit = new JLabel();	    
	    int nbInherit = 0;
	    if(folder.getParentFolder() != null){
	    	nbInherit = folder.getParentFolder().getInheritedFilter().getConditions().size();
	    }
	    
	    if(nbInherit == 1){
	    	lInherit.setText(String.format(Messages.getString("ML.ConditionPanel.lInheritSing"), nbInherit));	    	
	    } else {
	    	lInherit.setText(String.format(Messages.getString("ML.ConditionPanel.lInheritPlu"), nbInherit));
	    }
	    
		bShowInherit = new JButton(Messages.getString("ML.ConditionPanel.bShow"));
		if(nbInherit == 0) {
			bShowInherit.setEnabled(false);
		}
		bShowInherit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showInheritedConditions();
			}
		});

		cbInheritConditions = new JCheckBox(Messages.getString("ML.ConditionPanel.cbInheritConditions"));
		cbInheritConditions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				folder.setInheritsConditions(cbInheritConditions.isSelected());
				lInherit.setVisible(cbInheritConditions.isSelected());
				bShowInherit.setVisible(cbInheritConditions.isSelected());
			}
		});
		// Filter editor
		filterEditor = new FilterEditor(folder.getFilter(), folder.getFileType());
	}
	
	private void showInheritedConditions(){
		ConditionsViewer cv = new ConditionsViewer(folder.getParentFolder().getInheritedFilter(), folder.getName(), (Dialog) getTopLevelAncestor());
		cv.setLocation(GUIHelper.getCenterDialogOnParentLocation(cv.getPreferredSize(), this));
		cv.setModal(true);
		cv.setVisible(true);		
	}

	private void refreshPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, fill:50:grow, 3px", // columns
		        "3px, p, 3px, fill:120:grow, 3px"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(getInheritPanel(), cc.xy(2, 2));
		builder.add(filterEditor, cc.xy(2, 4));

		removeAll();
		add(builder.getPanel());
		validate();
	}

	private Component getInheritPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("p, 3px, p, 3px, p:grow", // columns
		        "p"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(lInherit, cc.xy(1, 1));
		builder.add(bShowInherit, cc.xy(3, 1));
		builder.add(cbInheritConditions, cc.xy(5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
	    
	    
	    return builder.getPanel();
    }
}
