package net.pms.medialibrary.commons.dataobjects;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pms.Messages;
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
	
	public String getFileName(boolean withExtension){
		String retVal = fileName;
		
		if(!withExtension){
			int extensionStart = retVal.lastIndexOf('.');
			if(extensionStart > 0){
				retVal = retVal.substring(0, extensionStart);
			}
		}
		
		return retVal;
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
	    this.folderPath = folderPath;
    }

	public String getFolderPath() {
		if(folderPath == null) folderPath = "";
		if(!folderPath.endsWith(File.separator)) folderPath += File.separator;
	    return folderPath;
    }

	public void setFileName(String fileName) {
	    this.fileName = fileName;
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
	    this.dateLastUpdatedDb = dateLastUpdatedDb;
    }

	public Date getDateLastUpdatedDb() {
		if(dateLastUpdatedDb == null) dateLastUpdatedDb = new Date(0);
	    return dateLastUpdatedDb;
    }

	public void setDateInsertedDb(Date dateInsertedDb) {
	    this.dateInsertedDb = dateInsertedDb;
    }

	public Date getDateInsertedDb() {
		if(dateInsertedDb == null) dateInsertedDb = new Date(0);
	    return dateInsertedDb;
    }

	public void setDateModifiedOs(Date dateModifiedOs) {
	    this.dateModifiedOs = dateModifiedOs;
    }

	public Date getDateModifiedOs() {
		if(dateModifiedOs == null) dateModifiedOs = new Date(0);
	    return dateModifiedOs;
    }

	public void setTags(Map<String, List<String>> tags) {
	    this.tags = tags;
    }

	public Map<String, List<String>> getTags() {
		if(tags == null) tags = new HashMap<String, List<String>>();
	    return tags;
    }

	public void setThumbnailPath(String thumbnailPath) {
	    this.thumbnailPath = thumbnailPath;
    }

	public String getThumbnailPath() {
		if(thumbnailPath == null) thumbnailPath = "";
	    return thumbnailPath;
    }

	public void setSize(long size) {
	    this.size = size;
    }

	public long getSize() {
	    return size;
    }

	public void setPlayCount(int playCount) {
	    this.playCount = playCount;
    }

	public int getPlayCount() {
	    return playCount;
    }

	public void addPlayToHistory(Date d) {
		if(d != null){
			getPlayHistory().add(d);
		}
	}

	public List<Date> getPlayHistory() {
		if(playHistory == null) playHistory = new ArrayList<Date>();
		return playHistory;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
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
	
}
