/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;

/**
 * The Class DOFileInfo.
 */
public class DOFileInfo {
	
	private int id = -1;
	private String folderPath = "";
	private String fileName = "";
	private FileType type = FileType.UNKNOWN;
	private Date dateLastUpdatedDb;
	private Date dateInsertedDb;
	private Date dateModifiedOs;
	private List<Date> playHistory;
	private Map<String, List<String>> tags = new HashMap<String, List<String>>();
	private String thumbnailPath = "";
	private long size;
	private int playCount;
	private boolean actif;
	private List<ActionListener> propertyChangeListeners = new ArrayList<ActionListener>();
	
	/**
	 * Gets the file name.
	 *
	 * @param withExtension if false, the extension will be removed
	 * @return the file name
	 */
	public String getFileName(boolean withExtension) {
		String retVal = fileName;
		
		if(!withExtension){
			int extensionStart = retVal.lastIndexOf('.');
			if(extensionStart > 0){
				retVal = retVal.substring(0, extensionStart);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Adds the property change listener.
	 *
	 * @param l the action listener
	 */
	public void addPropertyChangeListener(ActionListener l) {
		propertyChangeListeners.add(l);
	}

	/**
	 * Removes the property change listener.
	 *
	 * @param l the action listener
	 */
	public void removePropertyChangeListener(ActionListener l) {
		propertyChangeListeners.remove(l);
	}
	
	/**
	 * Gets the display string.
	 *
	 * @param displayNameMask the display name mask
	 * @return the display string
	 */
	public String getDisplayString(String displayNameMask){
		String retVal = displayNameMask;
		try { retVal = retVal.replace("%folder_path", getFolderPath()); } catch(Exception ex){ }		
		try { retVal = retVal.replace("%file_name", getFileName()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%type", getType().toString()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%date_last_updated_db", getDateLastUpdatedDb().toString()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%date_inserted_db", getDateInsertedDb().toString()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%date_last_modified_os", getDateModifiedOs().toString()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%date_last_played", getPlayHistory().size() == 0 ? Messages.getString("ML.Condition.NeverPlayed") : getPlayHistory().get(getPlayHistory().size() - 1).toString()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%play_count", String.valueOf(getPlayCount())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%is_actif", String.valueOf(isActive())); } catch(Exception ex){ }
		
		String tagPrefix = "%tag_";
		if(displayNameMask.contains(tagPrefix)) {
			int tagNameStartIndex = displayNameMask.indexOf(tagPrefix) + tagPrefix.length();
			int tagNameEndIndex = displayNameMask.indexOf(" ", tagNameStartIndex);
			if(tagNameEndIndex == -1) {
				tagNameEndIndex = displayNameMask.length() - 1;
			}
			String tagName = displayNameMask.substring(tagNameStartIndex, tagNameEndIndex);
			String tagsString = "";
			StringBuilder sb = new StringBuilder();
			if(getTags() != null && getTags().containsKey(tagName)){
				List<String> tagValues = getTags().get(tagName);
				Collections.sort(tagValues);
				for(String tagValue : tagValues){
					sb.append(tagValue);
					sb.append(", ");
				}
				tagsString = sb.toString();
				if(tagsString.endsWith(", ")){
					tagsString = tagsString.substring(0, tagsString.length() - 2);
				}
			}
			retVal = retVal.replace(tagPrefix + tagName, tagsString);
		}
		
		return retVal;
	}
	
	/**
	 * Gets the file path.
	 *
	 * @return the file path
	 */
	public String getFilePath(){
		String path = getFolderPath();
		if(!path.endsWith(File.separator)){
			path += File.separator;
		}
		return path + getFileName();	
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(int id) {
	    this.id = id;
    }

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
	    return id;
    }

	/**
	 * Sets the folder path.
	 *
	 * @param folderPath the new folder path
	 */
	public void setFolderPath(String folderPath) {
		if(!getFolderPath().equals(folderPath)) {
		    this.folderPath = folderPath;
		    firepropertyChangedEvent(ConditionType.FILE_FOLDERPATH);
	    }
    }

	/**
	 * Gets the folder path.
	 *
	 * @return the folder path
	 */
	public String getFolderPath() {
		if(folderPath == null) folderPath = "";
		if(!folderPath.endsWith(File.separator)) folderPath += File.separator;
	    return folderPath;
    }

	/**
	 * Sets the file name.
	 *
	 * @param fileName the new file name
	 */
	public void setFileName(String fileName) {
		if(!getFileName().equals(fileName)) {
		    this.fileName = fileName;
		    firepropertyChangedEvent(ConditionType.FILE_FILENAME);
	    }
    }

	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		if(fileName == null) fileName = "";
	    return fileName;
    }

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(FileType type) {
	    this.type = type;
    }

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public FileType getType() {
		if(type == null) type = FileType.UNKNOWN;
	    return type;
    }

	/**
	 * Sets the last updated date (DB).
	 *
	 * @param dateLastUpdatedDb the new date last updated db
	 */
	public void setDateLastUpdatedDb(Date dateLastUpdatedDb) {
		if(!getDateLastUpdatedDb().equals(dateLastUpdatedDb)) {
		    this.dateLastUpdatedDb = dateLastUpdatedDb;
		    firepropertyChangedEvent(ConditionType.FILE_DATELASTUPDATEDDB);
	    }
    }

	/**
	 * Gets the last updated date (DB).
	 *
	 * @return the date last updated
	 */
	public Date getDateLastUpdatedDb() {
		if(dateLastUpdatedDb == null) dateLastUpdatedDb = new Date(0);
	    return dateLastUpdatedDb;
    }

	/**
	 * Sets the date inserted (DB).
	 *
	 * @param dateInsertedDb the new date inserted
	 */
	public void setDateInsertedDb(Date dateInsertedDb) {
		if(!getDateInsertedDb().equals(dateInsertedDb)) {
		    this.dateInsertedDb = dateInsertedDb;
		    firepropertyChangedEvent(ConditionType.FILE_DATEINSERTEDDB);
	    }
    }

	/**
	 * Gets the date inserted (DB).
	 *
	 * @return the date inserted
	 */
	public Date getDateInsertedDb() {
		if(dateInsertedDb == null) dateInsertedDb = new Date(0);
	    return dateInsertedDb;
    }

	/**
	 * Sets the date modified (OS).
	 *
	 * @param dateModifiedOs the new date modified os
	 */
	public void setDateModifiedOs(Date dateModifiedOs) {
		if(!getDateModifiedOs().equals(dateModifiedOs)) {
		    this.dateModifiedOs = dateModifiedOs;
		    firepropertyChangedEvent(ConditionType.FILE_DATEMODIFIEDOS);
	    }
    }

	/**
	 * Gets the date modified os.
	 *
	 * @return the date modified os
	 */
	public Date getDateModifiedOs() {
		if(dateModifiedOs == null) dateModifiedOs = new Date(0);
	    return dateModifiedOs;
    }

	/**
	 * Sets the tags.
	 *
	 * @param tags the tags
	 */
	public void setTags(Map<String, List<String>> tags) {
		if(!getTags().equals(tags)) {
		    this.tags = tags;
		    firepropertyChangedEvent(ConditionType.FILE_CONTAINS_TAG);
	    }
    }

	/**
	 * Gets the tags.
	 *
	 * @return the tags
	 */
	public Map<String, List<String>> getTags() {
		if(tags == null) tags = new HashMap<String, List<String>>();
	    return tags;
    }

	/**
	 * Sets the thumbnail path.
	 *
	 * @param thumbnailPath the new thumbnail path
	 */
	public void setThumbnailPath(String thumbnailPath) {
		if(!getThumbnailPath().equals(thumbnailPath)) {
		    this.thumbnailPath = thumbnailPath;
		    firepropertyChangedEvent(ConditionType.FILE_THUMBNAILPATH);
	    }
    }

	/**
	 * Gets the thumbnail path.
	 *
	 * @return the thumbnail path
	 */
	public String getThumbnailPath() {
		if(thumbnailPath == null) thumbnailPath = "";
	    return thumbnailPath;
    }

	/**
	 * Sets the size.
	 *
	 * @param size the new size
	 */
	public void setSize(long size) {
		if(getSize() != size) {
		    this.size = size;
		    firepropertyChangedEvent(ConditionType.FILE_SIZEBYTE);
	    }
    }

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public long getSize() {
	    return size;
    }

	/**
	 * Sets the play count.
	 *
	 * @param playCount the new play count
	 */
	public void setPlayCount(int playCount) {
		if(getPlayCount() != playCount) {
		    this.playCount = playCount;
		    firepropertyChangedEvent(ConditionType.FILE_PLAYCOUNT);
	    }
    }

	/**
	 * Gets the play count.
	 *
	 * @return the play count
	 */
	public int getPlayCount() {
	    return playCount;
    }

	/**
	 * Adds the play to history.
	 *
	 * @param d the d
	 */
	public void addPlayToHistory(Date d) {
		if(d != null && !getPlayHistory().contains(d)){
			getPlayHistory().add(d);
		    firepropertyChangedEvent(ConditionType.FILEPLAYS_DATEPLAYEND);
		}
	}

	/**
	 * Gets the play history.
	 *
	 * @return the play history
	 */
	public List<Date> getPlayHistory() {
		if(playHistory == null) playHistory = new ArrayList<Date>();
		return playHistory;
	}

	/**
	 * Sets the active.
	 *
	 * @param active true if it is active; otherwise false
	 */
	public void setActive(boolean active) {
		if(isActive() != active){
			this.actif = active;
		    firepropertyChangedEvent(ConditionType.FILE_ISACTIF);
		}
	}

	/**
	 * Checks if the file is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		return actif;
	}

	/**
	 * Merge properties and tags.
	 *
	 * @param fileInfo the file info
	 */
	public void mergePropertiesAndTags(DOFileInfo fileInfo) {
		if(fileInfo.getThumbnailPath() != null && !fileInfo.getThumbnailPath().equals("")) {
			fileInfo.setThumbnailPath(fileInfo.getThumbnailPath());
		}
		
		//merge tags
		Map<String, List<String>> allTags = getTags();
		Map<String, List<String>> newTags = fileInfo.getTags();
		
		for (String tagName : newTags.keySet()) {
			if (!allTags.containsKey(tagName)) {
				allTags.put(tagName, new ArrayList<String>());
			}

			List<String> allTagValues = allTags.get(tagName);
			List<String> newTagValues = newTags.get(tagName);
			for (String tagValue : newTagValues) {
				if (!allTagValues.contains(tagValue)) {
					allTagValues.add(tagValue);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getFilePath();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DOFileInfo)){
			return false;
		}
		
		DOFileInfo compObj = (DOFileInfo)obj;
		if(getId() == compObj.getId()
				&& getFolderPath().equals(compObj.getFolderPath())
				&& getFileName().equals(compObj.getFileName())
				&& getType() == compObj.getType()
				&& getDateLastUpdatedDb().equals(compObj.getDateLastUpdatedDb())
				&& getDateInsertedDb().equals(compObj.getDateInsertedDb())
				&& getDateModifiedOs().equals(compObj.getDateModifiedOs())
				&& getTags().equals(compObj.getTags())
				&& getThumbnailPath().equals(compObj.getThumbnailPath())
				&& getSize() == compObj.getSize()
				&& getPlayCount() == compObj.getPlayCount()
				//&& getDateLastPlayed().equals(compObj.getDateLastPlayed())
				){
			return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 24 + getId();
		hashCode *= 24 + getFolderPath().hashCode();
		hashCode *= 24 + getFileName().hashCode();
		hashCode *= 24 + getType().hashCode();
		hashCode *= 24 + getDateLastUpdatedDb().hashCode();
		hashCode *= 24 + getDateInsertedDb().hashCode();
		hashCode *= 24 + getDateModifiedOs().hashCode();
		hashCode *= 24 + getTags().hashCode();
		hashCode *= 24 + getThumbnailPath().hashCode();
		hashCode *= 24 + getSize();
		hashCode *= 24 + getPlayCount();
		hashCode *= 24 + (isActive() ? 1 : 2);
		//hashCode *= 24 + getDateLastPlayed().hashCode();
		return hashCode;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
//    @Override
//	public DOFileInfo clone() {
//		DOFileInfo fi = new DOFileInfo();
//		fi.setFolderPath(getFolderPath());
//		fi.setFileName(getFileName());
//		fi.setType(getType());
//		fi.setDateLastUpdatedDb(getDateLastUpdatedDb());
//		fi.setDateInsertedDb(getDateInsertedDb());
//		fi.setDateModifiedOs(getDateModifiedOs());
//		fi.setTags(getTags());
//		fi.setThumbnailPath(getThumbnailPath());
//		fi.setSize(getSize());		
//		fi.setPlayCount(getPlayCount());
//		fi.setActive(isActive());
//		fi.playHistory = playHistory;
//		return fi;
//	}
	
    /**
     * Fires a property changed event.
     *
     * @param ct the condition type having changed
     */
    protected void firepropertyChangedEvent(ConditionType ct) {
    	ActionEvent e = new ActionEvent(this, ct.hashCode(), ct.toString());
    	for(ActionListener l : propertyChangeListeners) {
    		l.actionPerformed(e);
    	}
    }	
}
