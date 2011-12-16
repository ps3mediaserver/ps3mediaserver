package net.pms.medialibrary.gui.tab;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LibraryManagerView extends JPanel {
    private static final long serialVersionUID = -1315789010762303892L;

	public LibraryManagerView(){
		setLayout(new GridLayout());
		add(build());
	}
	
	private Component build(){
		FormLayout layout = new FormLayout("fill:10:grow", "fill:10:grow");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		CellConstraints cc = new CellConstraints();

		builder.add(new JLabel("****** Needs to be done ******"), cc.xy(1, 1, CellConstraints.CENTER, CellConstraints.CENTER));
		
		return builder.getPanel();		
	}
}
