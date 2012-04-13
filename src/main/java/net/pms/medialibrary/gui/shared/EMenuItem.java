package net.pms.medialibrary.gui.shared;

import javax.swing.JMenuItem;

public class EMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1205245974383880877L;
	private Object userObject;

	public EMenuItem(Object obj) {
		super(obj.toString());
		setUserObject(obj);
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
}
