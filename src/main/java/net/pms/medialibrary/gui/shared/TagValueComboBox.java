package net.pms.medialibrary.gui.shared;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import net.pms.medialibrary.storage.MediaLibraryStorage;

public class TagValueComboBox extends JComboBox {
	private static final long serialVersionUID = 8015993256438845569L;
	private String tagName;

	public TagValueComboBox(String tagName) {
		setTagName(tagName);
	}

	public void setTagName(String tagName) {
		if(this.tagName == null || !this.tagName.equals(tagName)) {
			this.tagName = tagName;
			refreshItems();
		}
	}
	
	private void refreshItems() {
		List<String> tagValues = MediaLibraryStorage.getInstance().getTagValues(tagName, true, 1);
		setModel(new DefaultComboBoxModel(tagValues.toArray()));
	}
}
