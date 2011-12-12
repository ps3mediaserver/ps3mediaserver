package net.pms.medialibrary.external;

import javax.swing.Icon;

public class TreeEntry {
	private Object userObject;
	private Icon icon;
	
	public TreeEntry(Object userObject, Icon icon){
		setUserObject(userObject);
		setIcon(icon);
	}
	
	public void setUserObject(Object userObject) {
	    this.userObject = userObject;
    }
	
	public Object getUserObject() {
	    return userObject;
    }
	
	public void setIcon(Icon icon) {
	    this.icon = icon;
    }
	
	public Icon getIcon() {
	    return icon;
    }
	
	@Override
	public String toString(){
		return getUserObject() == null ? "" : getUserObject().toString();
	}
}
