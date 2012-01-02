package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.Font;

import javax.swing.JLabel;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.ConditionType;

public class PropertyInfoTitleLabel extends JLabel {
	private static final long serialVersionUID = 4201268643022810386L;

	public PropertyInfoTitleLabel(ConditionType ct) {
		this(Messages.getString("ML.Condition.Header.Type." + ct) + ":");
	}
	
	public PropertyInfoTitleLabel(String text) {
		setFont(getFont().deriveFont(Font.BOLD));
		setText(text);
	}
}
