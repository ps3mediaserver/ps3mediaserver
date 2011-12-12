package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.medialibrary.commons.enumarations.FileType;

public class FileTypeCBItem implements Comparable<FileTypeCBItem> {
	private FileType fileType;
	private String displayName;
	
	public FileTypeCBItem(){
		this(FileType.UNKNOWN, "");
	}
	
	public FileTypeCBItem(FileType fileType, String displayName){
		this.setFileType(fileType);
		this.setDisplayName(displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public FileType getFileType() {
		return fileType;
	}
	
	@Override
	public String toString(){
		return getDisplayName();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof FileTypeCBItem)){
			return false;
		}

		FileTypeCBItem compObj = (FileTypeCBItem)o;
		if(getDisplayName() == compObj.getDisplayName()
			&& getFileType() == compObj.getFileType()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getFileType().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(FileTypeCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}
