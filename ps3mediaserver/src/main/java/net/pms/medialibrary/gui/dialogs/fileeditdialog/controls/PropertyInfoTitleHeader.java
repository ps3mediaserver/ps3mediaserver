package net.pms.medialibrary.gui.dialogs.fileeditdialog.controls;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.ConditionType;

public class PropertyInfoTitleHeader extends JComponent {
	private static final long serialVersionUID = 4201268643022810386L;
	
	private JCheckBox cbTitle;
	private JLabel lTitle;

	public PropertyInfoTitleHeader(ConditionType ct) {
		this(ct, false);
	}

	public PropertyInfoTitleHeader(ConditionType ct, boolean isConfirmEdit) {
		this(Messages.getString("ML.Condition.Header.Type." + ct) + ":", isConfirmEdit);
	}

	public PropertyInfoTitleHeader(String text) {
		this(text, false);
	}
	
	public PropertyInfoTitleHeader(String text, boolean isConfirmEdit) {
		setLayout(new GridLayout());
		JComponent cp;
		
		cbTitle = new JCheckBox(text);
		lTitle = new JLabel(text);
		
		if(isConfirmEdit) {
			cp = cbTitle;
		} else {
			cp = lTitle;
		}
		cp.setFont(cp.getFont().deriveFont(Font.BOLD));
		add(cp);
	}
	
	public void setSelected(boolean selected) {
		if(cbTitle != null) {
			cbTitle.setSelected(selected);
		}
	}

	public boolean isSelected() {
		if(cbTitle != null) {
			return cbTitle.isSelected();
		}
		return true;
	}
}
