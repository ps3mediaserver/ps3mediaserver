/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.medialibrary.commons.dataobjects;

import net.pms.medialibrary.commons.enumarations.FolderType;

public class DOFolder {
	private String               name;
	private long                 id;
	private long                 parentId;
	private int                  positionInParent;
	private FolderType           folderType;
	private DOMediaLibraryFolder parentFolder;

	public DOFolder(String name, long id, long parentId, DOMediaLibraryFolder parentFolder, int positionInParent, FolderType folderType) {
		setName(name);
		setId(id);
		setParentId(parentId);
		setParentFolder(parentFolder);
		setPositionInParent(positionInParent);
		setFolderType(folderType);
	}

	public DOFolder(String name, long id, long parentId, int positionInParent, FolderType folderType) {
		setName(name);
		setId(id);
		setParentId(parentId);
		setPositionInParent(positionInParent);
		setFolderType(folderType);
	}

	public DOFolder() {
		this("", -1, -1, -1, FolderType.UNKNOWN);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		if(name == null) name = "";
		return name;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public long getParentId() {
		return parentFolder == null ? parentId : parentFolder.getId();
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
		if(folderType == null) folderType = FolderType.UNKNOWN;
		return folderType;
	}

	public void setParentFolder(DOMediaLibraryFolder parentFolder) {
		this.parentId = parentFolder == null ? -1 : parentFolder.getId();
		if(this.parentFolder != null && !this.parentFolder.equals(parentFolder)){
			this.parentFolder.getChildFolders().remove(this);
		}
		this.parentFolder = parentFolder;
		if(parentFolder != null && parentFolder.getChildFolders() != null && !parentFolder.getChildFolders().contains(this)){
			parentFolder.getChildFolders().add(this);
		}
	}

	public DOMediaLibraryFolder getParentFolder() {
		return parentFolder;
	}

	@Override
	public DOFolder clone() {
		return new DOFolder(getName(), getId(), getParentId(), getParentFolder(), getPositionInParent(), getFolderType());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DOFolder)) { 
			return false; 
		}

		DOFolder compObj = (DOFolder) obj;
		if (getName().equals(compObj.getName()) 
				&& getId() == compObj.getId() 
				&& getParentId() == compObj.getParentId()
		        && getPositionInParent() == compObj.getPositionInParent() 
		        && getFolderType() == compObj.getFolderType()) { 
			return true; 
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode =  24 + getName().hashCode();
		hashCode *= 24 + getId();
		hashCode *= 24 + getParentId();
		hashCode *= 24 + getPositionInParent();
		hashCode *= 24 + getFolderType().hashCode();
		
		return hashCode;
	}	

	@Override
	public String toString() {
		return getName();
	}
}
