package net.pms.medialibrary.commons.dataobjects;

import java.util.List;

import net.pms.medialibrary.commons.enumarations.FileDisplayMode;
import net.pms.medialibrary.commons.enumarations.FileDisplayType;

public class DOFileEntryFile extends DOFileEntryBase {
	private FileDisplayMode fileDisplayMode;
	
	public DOFileEntryFile(){
		this(FileDisplayMode.UNKNOWN, -1, null, -1, "", null, 0);
	}
	
	public DOFileEntryFile(FileDisplayMode fileDisplayMode, long id, DOFileEntryFolder parent, 
			int positionInParent, String displayNameMask, List<DOThumbnailPriority> thumbnailPriorities, int maxLineLength){
		super(id, parent, positionInParent, displayNameMask, thumbnailPriorities, FileDisplayType.FILE, maxLineLength, null, null);
		setFileDisplayMode(fileDisplayMode);
	}

	public void setFileDisplayMode(FileDisplayMode fileDisplayMode) {
	    this.fileDisplayMode = fileDisplayMode;
    }

	public FileDisplayMode getFileDisplayMode() {
		if(fileDisplayMode == null) fileDisplayMode = FileDisplayMode.UNKNOWN;
	    return fileDisplayMode;
    }

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DOFileEntryFile)){
			return false;
		}
		
		DOFileEntryFile compObj = (DOFileEntryFile)obj;
		if(super.equals(compObj)
				&& getFileDisplayMode().equals(compObj.getFileDisplayMode())){
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + super.hashCode();
		hashCode *= 24 + getFileDisplayMode().hashCode();
		return hashCode;
	}

	@Override
	public DOFileEntryFile clone(){
		return new DOFileEntryFile(getFileDisplayMode(), getId(), getParent(), getPositionInParent(), 
				getDisplayNameMask(), getThumbnailPriorities(), getMaxLineLength());
	}
}
