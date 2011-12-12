package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.medialibrary.commons.enumarations.SortOption;

public class SortOptionCBItem implements Comparable<SortOptionCBItem> {
	private SortOption sortOption;
	private String displayName;
	
	public SortOptionCBItem(SortOption sortOption, String displayName){
		this.setSortOption(sortOption);
		this.setDisplayName(displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setSortOption(SortOption sortOption) {
		this.sortOption = sortOption;
	}

	public SortOption getSortOption() {
		return sortOption;
	}
	
	@Override
	public String toString(){
		return getDisplayName();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof SortOptionCBItem)){
			return false;
		}

		SortOptionCBItem compObj = (SortOptionCBItem)o;
		if(getDisplayName() == compObj.getDisplayName()
			&& getSortOption() == compObj.getSortOption()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getSortOption().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(SortOptionCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}
