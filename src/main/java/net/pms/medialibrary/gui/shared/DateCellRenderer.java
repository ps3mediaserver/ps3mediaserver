package net.pms.medialibrary.gui.shared;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DateCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1722251051690955342L;

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof Date) {
			String strDate = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(value);
			setText(strDate);
			if(getPreferredSize().width > table.getColumnModel().getColumn(column).getWidth()){
				strDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(value);
				setText(strDate);
			}
		}

		return this;
	}
}