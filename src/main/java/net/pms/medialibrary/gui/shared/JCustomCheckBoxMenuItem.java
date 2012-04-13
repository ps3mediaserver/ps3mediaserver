package net.pms.medialibrary.gui.shared;

import javax.swing.JCheckBoxMenuItem;

public class JCustomCheckBoxMenuItem extends JCheckBoxMenuItem {
	private static final long serialVersionUID = 7347159126521278081L;
	private Object userObject;

	public JCustomCheckBoxMenuItem(Object userObject, boolean isSelected){
		super(userObject.toString(), isSelected);
		setUserObject(userObject);
	}

	public void setUserObject(Object userObject) {
		setText(userObject.toString());
		this.userObject = userObject;
	}

	public Object getUserObject() {
		return userObject;
	}
}
