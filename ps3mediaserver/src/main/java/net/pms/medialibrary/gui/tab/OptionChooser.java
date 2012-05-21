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
package net.pms.medialibrary.gui.tab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.OptionType;
import net.pms.medialibrary.commons.events.SelectionChangeEvent;
import net.pms.medialibrary.commons.events.SelectionChangeListener;


import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class OptionChooser extends JPanel {
    private static final long serialVersionUID = -360087402031907819L;
    private List<OptionEntry> options = new ArrayList<OptionEntry>();
    private OptionEntry generalEntry;
	private OptionEntry treeEntry;
	private OptionEntry libraryEntry;
	private List<SelectionChangeListener> selectionChangeListeners = new ArrayList<SelectionChangeListener>();
    
    public OptionChooser(){
    	setPreferredSize(new Dimension(180, 50));
    	setBorder(BorderFactory.createEtchedBorder());
    	setBackground(Color.white);
    	
    	initEntries();
    }

	public void addSelectionChangeListener(SelectionChangeListener selectionChangeListener) {
		if(!selectionChangeListeners.contains(selectionChangeListener)){
			selectionChangeListeners.add(selectionChangeListener);
		}
    }
    
    public OptionType getOptionType(){
    	OptionType optionType = OptionType.UNKNOWN;
		for (OptionEntry o : options) {
			if(o.isSelected()){
				if(o == generalEntry){
					optionType = OptionType.GENERAL;
				} else if(o == treeEntry){
					optionType = OptionType.TREE;
				} else if(o == libraryEntry){
					optionType = OptionType.LIBRARY;
				}
				break;
			}
		}
    	return optionType;
    }
    
    public void setOptionType(OptionType optionType){
    	switch(optionType){
    		case GENERAL:
    			setOptionSelected(generalEntry);
    			break;
    		case LIBRARY:
    			setOptionSelected(libraryEntry);
    			break;
    		case TREE:
    			setOptionSelected(treeEntry);
    			break;
    	}
    }
    

	private void initEntries() {
		String iconsFolder = "/resources/images/";
		
		FormLayout layout = new FormLayout("fill:10:grow",
		"p, p, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setOpaque(false);
        
        CellConstraints cc = new CellConstraints();
    	
    	generalEntry = new OptionEntry(Messages.getString("ML.OptionChooser.OptionType.GENERAL"), iconsFolder + "oc_general.png");
	    options.add(generalEntry);
    	generalEntry.addMouseListener(new MouseAdapter() {
    		@Override
			public void mouseClicked(MouseEvent e) {
				setOptionSelected(generalEntry);
			}
		});    	
	    builder.add(generalEntry, cc.xy(1, 1));
    	
    	treeEntry = new OptionEntry(Messages.getString("ML.OptionChooser.OptionType.TREE"), iconsFolder + "oc_tree.png");
	    options.add(treeEntry);
		treeEntry.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setOptionSelected(treeEntry);
			}
		});
	    builder.add(treeEntry, cc.xy(1, 2));
    	
    	libraryEntry = new OptionEntry(Messages.getString("ML.OptionChooser.OptionType.LIBRARY"), iconsFolder + "oc_library.png");
	    options.add(libraryEntry);
		libraryEntry.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setOptionSelected(libraryEntry);
			}
		});
	    builder.add(libraryEntry, cc.xy(1, 3));

		generalEntry.setSelected(true);
	    this.setLayout(new GridLayout());
	    this.add(builder.getPanel());
    }
	
	private void setOptionSelected(OptionEntry optionEntry){
		if (!optionEntry.isSelected()) {
			for (OptionEntry o : options) {
				o.setSelected(false);
			}
			optionEntry.setSelected(true);
			fireSelectionChange(optionEntry);
		}		
	}
	
	private void fireSelectionChange(OptionEntry optionEntry){
		OptionType optionType = getOptionType();
		for(SelectionChangeListener l : selectionChangeListeners){
			l.SelectionChanged(new SelectionChangeEvent(this, optionType));
		}
	}
}
