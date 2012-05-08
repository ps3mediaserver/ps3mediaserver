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
import net.pms.plugins.DlnaTreeFolderPlugin;

public class DOSpecialFolder extends DOFolder implements Cloneable {
	private String        configFilePath;
	private DlnaTreeFolderPlugin specialFolderImplementation;
	
	public DOSpecialFolder(){
		this("", null, "", -1, -1, -1);
	}

	public DOSpecialFolder(String configFilePath, DlnaTreeFolderPlugin specialFolderImplementation, String name, long id, long parentId, int positionInParent) {
		super(name, id, parentId, positionInParent, FolderType.SPECIAL);
		setConfigFilePath(configFilePath);
		setSpecialFolderImplementation(specialFolderImplementation);
	}

	public DOSpecialFolder(String configFilePath, DlnaTreeFolderPlugin specialFolderImplementation, String name, long id, DOMediaLibraryFolder parentFolder, int positionInParent) {
	    this(configFilePath, specialFolderImplementation, name, id, parentFolder == null ? -1 : parentFolder.getId(), positionInParent);
	    setParentFolder(parentFolder);
    }

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	public String getConfigFilePath() {
		return configFilePath;
	}

	public void setSpecialFolderImplementation(DlnaTreeFolderPlugin specialFolder) {
		this.specialFolderImplementation = specialFolder;
	}

	public DlnaTreeFolderPlugin getSpecialFolderImplementation() {
		return specialFolderImplementation;
	}

	@Override
	public DOSpecialFolder clone() {
		return new DOSpecialFolder(getConfigFilePath(), getSpecialFolderImplementation(), getName(), getId(), getParentId(), getPositionInParent());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DOSpecialFolder)) { 
			return false; 
		}

		DOSpecialFolder compObj = (DOSpecialFolder) obj;
		if (super.equals(compObj) 
				&& getConfigFilePath().equals(compObj.getConfigFilePath())) { 
			return true; 
		}

		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + super.hashCode();
		hashCode *= 24 + getConfigFilePath().hashCode();
		return hashCode;
	}
}
