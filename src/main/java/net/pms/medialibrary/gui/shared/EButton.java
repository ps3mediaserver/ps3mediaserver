package net.pms.medialibrary.gui.shared;

import javax.swing.JButton;

public class EButton extends JButton {
	private static final long serialVersionUID = 5903290559320676023L;
	private Object userObject;
	
	public EButton(String text, Object userObject) {
		super(text);
		setUserObject(userObject);
	}
	
	public Object getUserObject() {
		return userObject;
	}
	
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
}
