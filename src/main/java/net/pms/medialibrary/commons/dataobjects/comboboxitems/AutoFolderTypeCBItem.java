package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.medialibrary.commons.enumarations.AutoFolderType;

public class AutoFolderTypeCBItem implements Comparable<AutoFolderTypeCBItem> {
	private AutoFolderType autoFolderType;
	private String displayName;
	
	public AutoFolderTypeCBItem(){
		this(AutoFolderType.UNKNOWN, "");
	}
	
	public AutoFolderTypeCBItem(AutoFolderType autoFolderType, String displayName){
		this.setAutoFolderType(autoFolderType);
		this.setDisplayName(displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public void setAutoFolderType(AutoFolderType autoFolderType) {
		this.autoFolderType = autoFolderType;
	}

	public AutoFolderType getAutoFolderType() {
		return autoFolderType;
	}
	
	@Override
	public String toString(){
		return getDisplayName();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof AutoFolderTypeCBItem)){
			return false;
		}

		AutoFolderTypeCBItem compObj = (AutoFolderTypeCBItem)o;
		if(getDisplayName() == compObj.getDisplayName()
			&& getAutoFolderType() == compObj.getAutoFolderType()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getAutoFolderType().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(AutoFolderTypeCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}
