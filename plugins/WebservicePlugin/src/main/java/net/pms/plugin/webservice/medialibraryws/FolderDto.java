package net.pms.plugin.webservice.medialibraryws;

import net.pms.medialibrary.commons.enumarations.FolderType;

public class FolderDto {
	private String               name;
	private long                 id;
	private int                  positionInParent;
	private FolderType           folderType;
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getId() {
		return id;
	}
	public void setPositionInParent(int positionInParent) {
		this.positionInParent = positionInParent;
	}
	public int getPositionInParent() {
		return positionInParent;
	}
	public void setFolderType(FolderType folderType) {
		this.folderType = folderType;
	}
	public FolderType getFolderType() {
		return folderType;
	}
}
