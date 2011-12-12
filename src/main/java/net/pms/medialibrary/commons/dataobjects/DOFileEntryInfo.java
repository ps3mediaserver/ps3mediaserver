package net.pms.medialibrary.commons.dataobjects;

import java.util.List;

import net.pms.medialibrary.commons.enumarations.FileDisplayType;


public class DOFileEntryInfo extends DOFileEntryBase {
	
	public DOFileEntryInfo(){
		this(-1, null, -1, "", null, 0);
	}
	
	public DOFileEntryInfo(long id, DOFileEntryFolder parent, 
			int positionInParent, String displayNameMask, List<DOThumbnailPriority> thumbnailPriorities, int maxLineLength){
		super(id, parent, positionInParent, displayNameMask, thumbnailPriorities, FileDisplayType.INFO, maxLineLength, null, null);
	}

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DOFileEntryInfo)){
			return false;
		}
		
		return super.equals(obj);
	}
	
	@Override
	public int hashCode(){
		return super.hashCode();
	}
	
	public DOFileEntryInfo clone(){
		return new DOFileEntryInfo(getId(), getParent(), getPositionInParent(), getDisplayNameMask(), getThumbnailPriorities(), getMaxLineLength());
	}
}
