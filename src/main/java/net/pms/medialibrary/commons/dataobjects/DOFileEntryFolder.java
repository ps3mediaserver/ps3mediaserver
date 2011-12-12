package net.pms.medialibrary.commons.dataobjects;

import java.util.ArrayList;
import java.util.List;

import net.pms.medialibrary.commons.enumarations.FileDisplayType;

public class DOFileEntryFolder extends DOFileEntryBase {
	private List<DOFileEntryBase> children;
	
	public DOFileEntryFolder(){
		this(new ArrayList<DOFileEntryBase>(), -1, null, -1, "", null, 0);
	}
	
	public DOFileEntryFolder(List<DOFileEntryBase> children, long id, DOFileEntryFolder parent, 
			int positionInParent, String displayNameMask, List<DOThumbnailPriority> thumbnailPriorities, int maxLineLength){
		super(id, parent, positionInParent, displayNameMask, thumbnailPriorities, FileDisplayType.FOLDER, maxLineLength, null, null);
		this.children = children;
	}

	public void addChild(DOFileEntryBase file) {	
		file.setParent(this);
		this.children.add(file);
	}

	public List<DOFileEntryBase> getChildren() {
		if(children == null) children = new ArrayList<DOFileEntryBase>();
		return children;
	}

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DOFileEntryFolder)){
			return false;
		}
		
		DOFileEntryFolder compObj = (DOFileEntryFolder)obj;
		if(super.equals(compObj)){
			for(DOFileEntryBase c : getChildren()){
				boolean found = false;
				for(DOFileEntryBase c2 : getChildren()){
					if(c.getId() == c2.getId())	{
						found = true;
						break;
					}
				}
				if(!found){
					return false;
				}
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + super.hashCode();
		for(DOFileEntryBase child : getChildren()){
			hashCode *= 24 + child.getId();		
		}
		return hashCode;
	}

	@Override
	public DOFileEntryFolder clone(){
		return new DOFileEntryFolder(getChildren(), getId(), getParent(), getPositionInParent(), 
				getDisplayNameMask(), getThumbnailPriorities(), getMaxLineLength());
	}
}
