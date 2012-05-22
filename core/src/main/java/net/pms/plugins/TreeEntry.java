package net.pms.plugins;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Plugins can use this class to render tree nodes properly. If a tree node is a
 * {@link DefaultMutableTreeNode} and its user object is a TreeEntry, the icon
 * will be used
 * 
 * @author pw
 * 
 */
public class TreeEntry {
	private Object userObject;
	private Icon icon;

	public TreeEntry(Object userObject, Icon icon) {
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
	public String toString() {
		return getUserObject() == null ? "" : getUserObject().toString();
	}
}
