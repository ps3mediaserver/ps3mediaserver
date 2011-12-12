package net.pms.medialibrary.gui.shared;

import java.util.List;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class MultiselectJlist extends JList {
	private static final long serialVersionUID = -4099191706242329704L;

	public MultiselectJlist(List<?> items) {
		super(items.toArray());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setSelectionModel(new DefaultListSelectionModel() {
			private static final long serialVersionUID = 6067504797350429462L;

			@Override
			public void setSelectionInterval(int start, int end) {
				if(isSelectedIndex(start)) 
				{
				    removeSelectionInterval(start, end);
				}
				else 
				{
				    addSelectionInterval(start, end);
				}
			}
		});
	}
}
