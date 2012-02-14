package net.pms.medialibrary.gui.dialogs.fileeditdialog.controls;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.pms.medialibrary.commons.enumarations.ConditionType;

public class PropertyInfoEntry extends JPanel {
	private static final long serialVersionUID = 6146935074550677958L;

	private PropertyInfoTitleHeader hTitle;
	private JTextField tfValue;
	private JComboBox cbValue;
	
	public PropertyInfoEntry(String value, ConditionType ct, boolean isConfirmEdit) {
		setLayout(new BorderLayout(0, 1));

		hTitle = new PropertyInfoTitleHeader(ct, isConfirmEdit);
		tfValue = new JTextField(value);
		tfValue.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				hTitle.setSelected(true);
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				hTitle.setSelected(true);
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				hTitle.setSelected(true);
			}
		});
		build();
	}
	
	public String getText() {
		String res;
		if(cbValue != null) {
			res = cbValue.getSelectedItem().toString();
		} else {
			res = tfValue.getText();
		}
		return res;
	}
	
	public boolean isSelected() {
		return hTitle.isSelected();
	}
	
	public void addValue(String value) {
		if(cbValue == null) {
			cbValue = new JComboBox();
			String val = tfValue.getText();
			if(!val.equals("")) {
				cbValue.addItem(val);
				cbValue.setSelectedIndex(0);
			}
			build();
		}
	}
	
	private void build() {
		removeAll();
		
		JPanel pTitle = new JPanel(new GridLayout());
		pTitle.setAlignmentY(LEFT_ALIGNMENT);
		pTitle.add(hTitle);
		add(pTitle, BorderLayout.NORTH);
		
		add(tfValue, BorderLayout.CENTER);
	}
}
