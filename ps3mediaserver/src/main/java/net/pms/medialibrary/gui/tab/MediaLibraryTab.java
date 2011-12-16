package net.pms.medialibrary.gui.tab;

import java.util.EventObject;

import javax.swing.JComponent;

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
		}
		
	}
}
