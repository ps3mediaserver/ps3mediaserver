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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.pms.medialibrary.commons.enumarations.FileDisplayType;

public class DOFileEntryBase implements Cloneable{
	private long id;
	private DOFileEntryFolder parent;
	private int positionInParent;
	private String displayNameMask;
	private List<DOThumbnailPriority> thumbnailPrioritis;
	private FileDisplayType fileEntryType;
	private int maxLineLength;
	private String pluginName;
	private String pluginConfigFilePath;
	
	public DOFileEntryBase(){
		this(-1, null, -1, "", null, FileDisplayType.UNKNOWN, 0, "", "");
	}
	
	public DOFileEntryBase(long id, DOFileEntryFolder parent, int positionInParent, String displayNameMask,
			List<DOThumbnailPriority> thumbnailPrioritis, FileDisplayType fileEntryType, int maxLineLength, String pluginName, String pluginConfigFilePath){
		setId(id);
		setParent(parent);
		setPositionInParent(positionInParent);
		setDisplayNameMask(displayNameMask);
		setThumbnailPrioritis(thumbnailPrioritis);
		setFileEntryType(fileEntryType);
		setMaxLineLength(maxLineLength);
		setPluginName(pluginName);
		setPluginConfigFilePath(pluginConfigFilePath);
	}

	public void setPositionInParent(int positionInParent) {
		this.positionInParent = positionInParent;
	}

	public int getPositionInParent() {
		return positionInParent;
	}

	public void setDisplayNameMask(String displayNameMask) {
		this.displayNameMask = displayNameMask;
	}

	public String getDisplayNameMask() {
		if(displayNameMask == null) displayNameMask = "";
		return displayNameMask;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setParent(DOFileEntryFolder parent) {
	    this.parent = parent;
    }

	public DOFileEntryFolder getParent() {
	    return parent;
    }
	
	public void setThumbnailPrioritis(List<DOThumbnailPriority> thumbnailPrioritis) {
	    this.thumbnailPrioritis = thumbnailPrioritis;
    }

	public List<DOThumbnailPriority> getThumbnailPriorities() {
		if(thumbnailPrioritis == null) thumbnailPrioritis = new ArrayList<DOThumbnailPriority>();
		Collections.sort(thumbnailPrioritis, new Comparator<DOThumbnailPriority>() {
			@Override
			public int compare(DOThumbnailPriority o1, DOThumbnailPriority o2) {
				return o1.getPriorityIndex() == o2.getPriorityIndex() ? 0 :
				o1.getPriorityIndex() > o2.getPriorityIndex() ? 1 : -1;
			}
		});
	    return thumbnailPrioritis;
    }

	public void setFileEntryType(FileDisplayType fileEntryType) {
	    this.fileEntryType = fileEntryType;
    }

	public FileDisplayType getFileEntryType() {
		if(fileEntryType == null) fileEntryType = FileDisplayType.UNKNOWN;
	    return fileEntryType;
    }

	public void setMaxLineLength(int maxLineLength) {
	    this.maxLineLength = maxLineLength;
    }

	public int getMaxLineLength() {
	    return maxLineLength;
    }

	public void setPluginName(String pluginName) {
	    this.pluginName = pluginName;
    }

	public String getPluginName() {
		if(pluginName == null) pluginName = "";
	    return pluginName;
    }

	public void setPluginConfigFilePath(String pluginConfigFilePath) {
	    this.pluginConfigFilePath = pluginConfigFilePath;
    }

	public String getPluginConfigFilePath() {
		if(pluginConfigFilePath == null) pluginConfigFilePath = "";
	    return pluginConfigFilePath;
    }

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DOFileEntryBase)){
			return false;
		}
		
		DOFileEntryBase compObj = (DOFileEntryBase)obj;
		if(getId() == compObj.getId()
				&& ((getParent() == null && compObj.getParent() == null) 
						|| (getParent() != null && compObj.getParent() != null && getParent().getId() == compObj.getParent().getId()))
				&& getDisplayNameMask().equals(compObj.getDisplayNameMask())
				&& getPositionInParent() == compObj.getPositionInParent()
				&& getThumbnailPriorities().equals(compObj.getThumbnailPriorities())
				&& getFileEntryType() == compObj.getFileEntryType()
				&& getMaxLineLength() == compObj.getMaxLineLength()
				&& getPluginName().equals(compObj.getPluginName())
				&& getPluginConfigFilePath().equals(compObj.getPluginConfigFilePath())){
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + String.valueOf(getId()).hashCode();
		hashCode *= 24 + (getParent() == null ? -1 : String.valueOf(getParent().getId()).hashCode());
		hashCode *= 24 + getDisplayNameMask().hashCode();
		hashCode *= 24 + getPositionInParent();
		hashCode *= 24 + getThumbnailPriorities().hashCode();
		hashCode *= 24 + getFileEntryType().hashCode();
		hashCode *= 24 + getMaxLineLength();
		hashCode *= 24 + getPluginName().hashCode();
		hashCode *= 24 + getPluginConfigFilePath().hashCode();
		return hashCode;
	}
	
	@Override
	public String toString(){
		return getDisplayNameMask();
	}

	@Override
	public DOFileEntryBase clone(){
		return new DOFileEntryBase(getId(), getParent(), getPositionInParent(), getDisplayNameMask(), 
				getThumbnailPriorities(), getFileEntryType(), getMaxLineLength(), getPluginName(), getPluginConfigFilePath());
	}
}
