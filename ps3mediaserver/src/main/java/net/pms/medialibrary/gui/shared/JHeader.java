package net.pms.medialibrary.gui.shared;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.ConditionType;

public class JHeader extends JComponent {
	private static final long serialVersionUID = 4201268643022810386L;
	
	private JCheckBox cbTitle;
	private JLabel lTitle;

	public JHeader(ConditionType ct) {
		this(ct, false);
	}

	public JHeader(ConditionType ct, boolean isConfirmEdit) {
		this(Messages.getString("ML.Condition.Header.Type." + ct) + ":", isConfirmEdit);
	}

	public JHeader(String text) {
		this(text, false);
	}
	
	public JHeader(String text, boolean isConfirmEdit) {
		this(text, isConfirmEdit, false);
	}
	
	public JHeader(String text, boolean isConfirmEdit, boolean setSelected) {
		setLayout(new BorderLayout());
		JComponent cp;
		
		cbTitle = new JCheckBox(text);
		cbTitle.setSelected(setSelected);
		lTitle = new JLabel(text);
		
		if(isConfirmEdit) {
			cp = cbTitle;
		} else {
			cp = lTitle;
		}
		cp.setFont(cp.getFont().deriveFont(Font.BOLD));
		add(cp, BorderLayout.LINE_START);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		cbTitle.setEnabled(enabled);
		lTitle.setEnabled(enabled);
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

	public void addChangeListener(ChangeListener l) {
		cbTitle.addChangeListener(l);
	}
}
