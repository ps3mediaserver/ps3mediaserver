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

import net.pms.medialibrary.commons.enumarations.ThumbnailPrioType;

public class DOThumbnailPriority {
	private long id;
	private ThumbnailPrioType thumbnailPriorityType;
	private String picturePath;
	private int seekPosition;
	private int priorityIndex;
	
	public DOThumbnailPriority(){
		this(-1, ThumbnailPrioType.THUMBNAIL, "", 0);
	}
	
	public DOThumbnailPriority(long id, ThumbnailPrioType thumbnailPriorityType, String picturePath, int priorityIndex){
		this(id, thumbnailPriorityType, -1, picturePath, priorityIndex);
	}
	
	public DOThumbnailPriority(long id, ThumbnailPrioType thumbnailPriorityType, int seekPosition, int priorityIndex){
		this(id, thumbnailPriorityType, seekPosition, "", priorityIndex);
	}

	public DOThumbnailPriority(long id, ThumbnailPrioType thumbnailPriorityType, int seekPosition, String picturePath, int priorityIndex){
		setId(id);
		setThumbnailPriorityType(thumbnailPriorityType);
		setSeekPosition(seekPosition);
		setPicturePath(picturePath);
		setPriorityIndex(priorityIndex);
	}

	public void setThumbnailPriorityType(ThumbnailPrioType thumbnailPriorityType) {
	    this.thumbnailPriorityType = thumbnailPriorityType;
    }

	public ThumbnailPrioType getThumbnailPriorityType() {
	    return thumbnailPriorityType;
    }

	public void setPicturePath(String picturePath) {
	    this.picturePath = picturePath;
    }

	public String getPicturePath() {
	    return picturePath;
    }

	public void setSeekPosition(int seekPosition) {
	    this.seekPosition = seekPosition;
    }

	public int getSeekPosition() {
	    return seekPosition;
    }

	public void setPriorityIndex(int priorityIndex) {
	    this.priorityIndex = priorityIndex;
    }

	public int getPriorityIndex() {
	    return priorityIndex;
    }

	public void setId(long id) {
	    this.id = id;
    }

	public long getId() {
	    return id;
    }

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DOThumbnailPriority)){
			return false;
		}


		DOThumbnailPriority compObj = (DOThumbnailPriority) obj;
		if(getId() == compObj.getId()
				&& getThumbnailPriorityType() == compObj.getThumbnailPriorityType()
				&& getPicturePath().equals(compObj.getPicturePath())
				&& getSeekPosition() == compObj.getSeekPosition()
				&& getPriorityIndex() == compObj.getPriorityIndex()){
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + String.valueOf(getId()).hashCode();
		hashCode *= 24 + getPicturePath().hashCode();
		hashCode *= 24 + getSeekPosition();
		hashCode *= 24 + getPriorityIndex();
		return hashCode;
	}
	
	@Override
	public DOThumbnailPriority clone(){
		return new DOThumbnailPriority(getId(), getThumbnailPriorityType(), getSeekPosition(), getPicturePath(), getPriorityIndex());
	}
	
	@Override
	public String toString(){
		return String.format("id=%s, prioIndex=%s, type=%s, seekPos=%s, picPath=%s", getId(), getPriorityIndex(), getThumbnailPriorityType(), getSeekPosition(), getPicturePath());
	}

}
