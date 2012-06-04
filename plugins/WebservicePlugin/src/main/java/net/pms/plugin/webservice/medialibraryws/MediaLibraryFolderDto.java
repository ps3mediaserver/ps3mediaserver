package net.pms.plugin.webservice.medialibraryws;

import java.util.List;

import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.dataobjects.FileDisplayProperties;
import net.pms.medialibrary.commons.enumarations.FileType;

public class MediaLibraryFolderDto extends FolderDto {
	private DOFilter filter;
	private boolean inheritsConditions;
	private List<FolderDto> childFolders;
	private FileType fileType;
	private FileDisplayProperties displayProperties;
	private boolean inheritSort;
	private boolean inheritDisplayFileAs;
	private boolean displayItems;
	private int maxFiles;
	
	public void setFilter(DOFilter filter) {
		this.filter = filter;
	}
	public DOFilter getFilter() {
		return filter;
	}
	public void setInheritsConditions(boolean inheritsConditions) {
		this.inheritsConditions = inheritsConditions;
	}
	public boolean isInheritsConditions() {
		return inheritsConditions;
	}
	public void setChildFolders(List<FolderDto> childFolders) {
		this.childFolders = childFolders;
	}
	public List<FolderDto> getChildFolders() {
		return childFolders;
	}
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}
	public FileType getFileType() {
		return fileType;
	}
	public void setInheritSort(boolean inheritSort) {
		this.inheritSort = inheritSort;
	}
	public boolean isInheritSort() {
		return inheritSort;
	}
	public void setDisplayProperties(FileDisplayProperties displayProperties) {
		this.displayProperties = displayProperties;
	}
	public FileDisplayProperties getDisplayProperties() {
		return displayProperties;
	}
	public void setInheritDisplayFileAs(boolean inheritDisplayFileAs) {
		this.inheritDisplayFileAs = inheritDisplayFileAs;
	}
	public boolean isInheritDisplayFileAs() {
		return inheritDisplayFileAs;
	}
	public void setDisplayItems(boolean displayItems) {
		this.displayItems = displayItems;
	}
	public boolean isDisplayItems() {
		return displayItems;
	}
	public void setMaxFiles(int maxFiles) {
		this.maxFiles = maxFiles;
	}
	public int getMaxFiles() {
		return maxFiles;
	}

}
