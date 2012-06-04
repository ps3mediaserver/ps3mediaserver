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
package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.medialibrary.commons.enumarations.AutoFolderType;

public class AutoFolderTypeCBItem implements Comparable<AutoFolderTypeCBItem> {
	private AutoFolderType autoFolderType;
	private String displayName;
	
	public AutoFolderTypeCBItem(){
		this(AutoFolderType.UNKNOWN, "");
	}
	
	public AutoFolderTypeCBItem(AutoFolderType autoFolderType, String displayName){
		this.setAutoFolderType(autoFolderType);
		this.setDisplayName(displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public void setAutoFolderType(AutoFolderType autoFolderType) {
		this.autoFolderType = autoFolderType;
	}

	public AutoFolderType getAutoFolderType() {
		return autoFolderType;
	}
	
	@Override
	public String toString(){
		return getDisplayName();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof AutoFolderTypeCBItem)){
			return false;
		}

		AutoFolderTypeCBItem compObj = (AutoFolderTypeCBItem)o;
		if(getDisplayName() == compObj.getDisplayName()
			&& getAutoFolderType() == compObj.getAutoFolderType()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getAutoFolderType().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(AutoFolderTypeCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}
