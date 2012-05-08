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
