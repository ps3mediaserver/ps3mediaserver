package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.medialibrary.commons.enumarations.ThumbnailPrioType;

public class TumbnailPrioCBItem implements Comparable<TumbnailPrioCBItem> {
	private ThumbnailPrioType thumbnailPrioType;
	private String displayName;
	
	public TumbnailPrioCBItem(){
		this(ThumbnailPrioType.UNKNOWN, "");
	}
	
	public TumbnailPrioCBItem(ThumbnailPrioType thumbnailPrioType, String displayName){
		this.setThumbnailPrioType(thumbnailPrioType);
		this.setDisplayName(displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public void setThumbnailPrioType(ThumbnailPrioType thumbnailPrioType) {
		this.thumbnailPrioType = thumbnailPrioType;
	}

	public ThumbnailPrioType getThumbnailPrioType() {
		return thumbnailPrioType;
	}
	
	@Override
	public String toString(){
		return getDisplayName();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof TumbnailPrioCBItem)){
			return false;
		}

		TumbnailPrioCBItem compObj = (TumbnailPrioCBItem)o;
		if(getDisplayName() == compObj.getDisplayName()
			&& getThumbnailPrioType() == compObj.getThumbnailPrioType()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getThumbnailPrioType().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(TumbnailPrioCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}
