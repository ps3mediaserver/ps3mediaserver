package net.pms.medialibrary.gui.shared;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Sets a white background for items shown in a JList
 * @author pw
 *
 */
public class ActiveEnginesListCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 6106965726208539020L;

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component res = super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
		if(!isSelected) {
			res.setBackground(Color.WHITE);
		}
		return res;
	}
}
