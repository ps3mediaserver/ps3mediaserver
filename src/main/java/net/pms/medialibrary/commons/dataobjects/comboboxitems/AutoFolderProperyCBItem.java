package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.medialibrary.commons.enumarations.AutoFolderProperty;

public class AutoFolderProperyCBItem implements Comparable<AutoFolderProperyCBItem> {
	private AutoFolderProperty autoFolderProperty;
	private String displayName;
	
	public AutoFolderProperyCBItem(){
		
	}
	
	public AutoFolderProperyCBItem(AutoFolderProperty autoFolderProperty, String displayName){
		this.setAutoFolderProperty(autoFolderProperty);
		this.setDisplayName(displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setAutoFolderProperty(AutoFolderProperty autoFolderProperty) {
		this.autoFolderProperty = autoFolderProperty;
	}

	public AutoFolderProperty getAutoFolderProperty() {
		return autoFolderProperty;
	}
	
	@Override
	public String toString(){
		return getDisplayName();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof AutoFolderProperyCBItem)){
			return false;
		}

		AutoFolderProperyCBItem compObj = (AutoFolderProperyCBItem)o;
		if(getDisplayName() == compObj.getDisplayName()
			&& getAutoFolderProperty() == compObj.getAutoFolderProperty()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getAutoFolderProperty().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(AutoFolderProperyCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}
