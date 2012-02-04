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
	
	public void addPropertyChangeListeners(ActionListener l) {
		propertyChangeListeners.add(l);
	}

	public void removePropertyChangeListeners(ActionListener l) {
		propertyChangeListeners.remove(l);
	}
	
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
		try { retVal = retVal.replace("%is_actif", String.valueOf(isActif())); } catch(Exception ex){ }
		
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
	
	public String getFilePath(){
		String path = getFolderPath();
		if(!path.endsWith(File.separator)){
			path += File.separator;
		}
		return path + getFileName();	
	}

	public void setId(int id) {
	    this.id = id;
    }

	public int getId() {
	    return id;
    }

	public void setFolderPath(String folderPath) {
		if(!getFolderPath().equals(folderPath)) {
		    this.folderPath = folderPath;
		    firepropertyChangedEvent(ConditionType.FILE_FOLDERPATH);
	    }
    }

	public String getFolderPath() {
		if(folderPath == null) folderPath = "";
		if(!folderPath.endsWith(File.separator)) folderPath += File.separator;
	    return folderPath;
    }

	public void setFileName(String fileName) {
		if(!getFileName().equals(fileName)) {
		    this.fileName = fileName;
		    firepropertyChangedEvent(ConditionType.FILE_FILENAME);
	    }
    }

	public String getFileName() {
		if(fileName == null) fileName = "";
	    return fileName;
    }

	public void setType(FileType type) {
	    this.type = type;
    }

	public FileType getType() {
		if(type == null) type = FileType.UNKNOWN;
	    return type;
    }

	public void setDateLastUpdatedDb(Date dateLastUpdatedDb) {
		if(!getDateLastUpdatedDb().equals(dateLastUpdatedDb)) {
		    this.dateLastUpdatedDb = dateLastUpdatedDb;
		    firepropertyChangedEvent(ConditionType.FILE_DATELASTUPDATEDDB);
	    }
    }

	public Date getDateLastUpdatedDb() {
		if(dateLastUpdatedDb == null) dateLastUpdatedDb = new Date(0);
	    return dateLastUpdatedDb;
    }

	public void setDateInsertedDb(Date dateInsertedDb) {
		if(!getDateInsertedDb().equals(dateInsertedDb)) {
		    this.dateInsertedDb = dateInsertedDb;
		    firepropertyChangedEvent(ConditionType.FILE_DATEINSERTEDDB);
	    }
    }

	public Date getDateInsertedDb() {
		if(dateInsertedDb == null) dateInsertedDb = new Date(0);
	    return dateInsertedDb;
    }

	public void setDateModifiedOs(Date dateModifiedOs) {
		if(!getDateModifiedOs().equals(dateModifiedOs)) {
		    this.dateModifiedOs = dateModifiedOs;
		    firepropertyChangedEvent(ConditionType.FILE_DATEMODIFIEDOS);
	    }
    }

	public Date getDateModifiedOs() {
		if(dateModifiedOs == null) dateModifiedOs = new Date(0);
	    return dateModifiedOs;
    }

	public void setTags(Map<String, List<String>> tags) {
		if(!getTags().equals(tags)) {
		    this.tags = tags;
		    firepropertyChangedEvent(ConditionType.FILE_CONTAINS_TAG);
	    }
    }

	public Map<String, List<String>> getTags() {
		if(tags == null) tags = new HashMap<String, List<String>>();
	    return tags;
    }

	public void setThumbnailPath(String thumbnailPath) {
		if(!getThumbnailPath().equals(thumbnailPath)) {
		    this.thumbnailPath = thumbnailPath;
		    firepropertyChangedEvent(ConditionType.FILE_THUMBNAILPATH);
	    }
    }

	public String getThumbnailPath() {
		if(thumbnailPath == null) thumbnailPath = "";
	    return thumbnailPath;
    }

	public void setSize(long size) {
		if(getSize() != size) {
		    this.size = size;
		    firepropertyChangedEvent(ConditionType.FILE_SIZEBYTE);
	    }
    }

	public long getSize() {
	    return size;
    }

	public void setPlayCount(int playCount) {
		if(getPlayCount() != playCount) {
		    this.playCount = playCount;
		    firepropertyChangedEvent(ConditionType.FILE_PLAYCOUNT);
	    }
    }

	public int getPlayCount() {
	    return playCount;
    }

	public void addPlayToHistory(Date d) {
		if(d != null && !getPlayHistory().contains(d)){
			getPlayHistory().add(d);
		    firepropertyChangedEvent(ConditionType.FILEPLAYS_DATEPLAYEND);
		}
	}

	public List<Date> getPlayHistory() {
		if(playHistory == null) playHistory = new ArrayList<Date>();
		return playHistory;
	}

	public void setActif(boolean actif) {
		if(isActif() != actif){
			this.actif = actif;
		    firepropertyChangedEvent(ConditionType.FILE_ISACTIF);
		}
	}

	public boolean isActif() {
		return actif;
	}

	@Override
	public String toString() {
		return getFilePath();
	}
	
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
	
	@Override
	public int hashCode(){
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
		hashCode *= 24 + (isActif() ? 1 : 2);
		//hashCode *= 24 + getDateLastPlayed().hashCode();
		return hashCode;
	}

    @Override
	public DOFileInfo clone() {
		DOFileInfo fi = new DOFileInfo();
		fi.setFolderPath(getFolderPath());
		fi.setFileName(getFileName());
		fi.setType(getType());
		fi.setDateLastUpdatedDb(getDateLastUpdatedDb());
		fi.setDateInsertedDb(getDateInsertedDb());
		fi.setDateModifiedOs(getDateModifiedOs());
		fi.setTags(getTags());
		fi.setThumbnailPath(getThumbnailPath());
		fi.setSize(getSize());		
		fi.setPlayCount(getPlayCount());
		fi.setActif(isActif());
		fi.playHistory = playHistory;
		return fi;
	}
    
    protected void firepropertyChangedEvent(ConditionType ct) {
    	ActionEvent e = new ActionEvent(this, ct.hashCode(), ct.toString());
    	for(ActionListener l : propertyChangeListeners) {
    		l.actionPerformed(e);
    	}
    }
	
}
