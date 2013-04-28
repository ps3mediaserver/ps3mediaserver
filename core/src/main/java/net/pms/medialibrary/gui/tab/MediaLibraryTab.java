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

import java.util.EventObject;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.OptionType;
import net.pms.medialibrary.commons.events.LibraryShowListener;
import net.pms.medialibrary.commons.events.SelectionChangeEvent;
import net.pms.medialibrary.commons.events.SelectionChangeListener;
import net.pms.medialibrary.gui.tab.dlnaview.DLNAViewPanel;
import net.pms.medialibrary.gui.tab.libraryview.LibraryViewPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MediaLibraryTab {
	private static final Logger log = LoggerFactory.getLogger(MediaLibraryTab.class);
	private OptionChooser optionChooser;
	private DLNAViewPanel dlnaViewPanel;
	private GeneralOptionsView generalOptionsPanel;
	private LibraryViewPanel libraryManagerView;
	
	public MediaLibraryTab(){
		init();
		optionChooser.setOptionType(OptionType.GENERAL);
	}

	public JComponent build() {
		FormLayout layout = new FormLayout("3px, p, 5px, fill:50:grow, 3px",
				"3px, fill:10:grow, 3px");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.add(optionChooser, cc.xy(2, 2));
		builder.add(dlnaViewPanel, cc.xy(4, 2));
		builder.add(generalOptionsPanel, cc.xy(4, 2));
		builder.add(libraryManagerView, cc.xy(4, 2));

		return builder.getPanel();
	}
	
	private void init(){        
		optionChooser = new OptionChooser();
		optionChooser.addSelectionChangeListener(new SelectionChangeListener() {
			
			@Override
			public void SelectionChanged(EventObject e) {
				if(e instanceof SelectionChangeEvent){
					setOptionTypeSelected(((SelectionChangeEvent) e).getOptionType());
				}
			}
		});
        dlnaViewPanel = new DLNAViewPanel();
        dlnaViewPanel.setLibraryShowListener(new LibraryShowListener() {			
			@Override
			public void show(DOFilter filter, FileType fileType) {
				libraryManagerView.configure(filter, fileType);
				setOptionTypeSelected(OptionType.LIBRARY);
				optionChooser.setOptionType(OptionType.LIBRARY);
			}
		});
        generalOptionsPanel = new GeneralOptionsView();
        libraryManagerView = new LibraryViewPanel();
        
        setOptionTypeSelected(OptionType.GENERAL);
	}
	
	private void setOptionTypeSelected(OptionType optionType){
		switch(optionType){
			case GENERAL:
				dlnaViewPanel.setVisible(false);
				generalOptionsPanel.setVisible(true);
				libraryManagerView.setVisible(false);
				break;
			case LIBRARY:
				dlnaViewPanel.setVisible(false);
				generalOptionsPanel.setVisible(false);
				libraryManagerView.setVisible(true);
				break;
			case TREE:
				dlnaViewPanel.setVisible(true);
				generalOptionsPanel.setVisible(false);
				libraryManagerView.setVisible(false);
				break;
			default:
				log.warn(String.format("Unhandled option type received (%s). This should never happen!", optionType));
				break;
		}
		
	}
}
