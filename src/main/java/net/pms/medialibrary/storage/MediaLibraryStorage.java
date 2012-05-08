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
package net.pms.medialibrary.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.medialibrary.commons.dataobjects.DOAudioFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFolder;
import net.pms.medialibrary.commons.dataobjects.DOFolder;
import net.pms.medialibrary.commons.dataobjects.DOImageFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.dataobjects.DOQuickTagEntry;
import net.pms.medialibrary.commons.dataobjects.DOTableColumnConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOTemplate;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.SortOption;
import net.pms.medialibrary.commons.enumarations.MediaLibraryConstants.MetaDataKeys;
import net.pms.medialibrary.commons.exceptions.StorageException;
import net.pms.medialibrary.commons.helpers.FileImportHelper;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;

public class MediaLibraryStorage implements IMediaLibraryStorage {		
	public static final int ROOT_FOLDER_ID = 1;
	public static final int ALL_CHILDREN = Integer.MAX_VALUE;

	private static final Logger log = LoggerFactory.getLogger(MediaLibraryStorage.class);
	private static MediaLibraryStorage instance;
	
	private JdbcConnectionPool cp;
	
	private DBInitializer dbInitializer;
	private DBGlobal dbGlobal;
	private DBFileInfo dbFileInfo;
	private DBVideoFileInfo dbVideoFileInfo;
	private DBAudioFileInfo dbAudioFileInfo;
	private DBPicturesFileInfo dbPicturesFileInfo;
	private DBFolders dbMediaLibraryFolders;
	private DBTemplates dbTemplates;
	private DBFileFolder dbFileFolder;
	private DBManagedFolders dbManagedFolders;
	private DBTableColumn dbTableColumn;
	private DBFileImport dbFileImport;
	private DBQuickTag dbQuickTag;
	
	/**
	 * Constructor
	 * @param name The name of the database file. The location will be determined automatically depending on OS
	 */
	private MediaLibraryStorage(String name){
		dbInitializer = new DBInitializer(name, this);
		cp = dbInitializer.getConnectionPool();
		if(log.isDebugEnabled()) log.debug("JdbcConnectionPool created");
		
		dbGlobal = new DBGlobal(cp);
		dbFileInfo = new DBFileInfo(cp);
		dbVideoFileInfo = new DBVideoFileInfo(cp);
		dbAudioFileInfo = new DBAudioFileInfo(cp);
		dbPicturesFileInfo = new DBPicturesFileInfo(cp);
		dbMediaLibraryFolders = new DBFolders(cp);
		dbTemplates = new DBTemplates(cp);
		dbFileFolder = new DBFileFolder(cp);
		dbManagedFolders = new DBManagedFolders(cp);
		dbTableColumn = new DBTableColumn(cp);
		dbFileImport = new DBFileImport(cp);
		dbQuickTag = new DBQuickTag(cp);
		
		if(dbInitializer.isConnected()){
			dbInitializer.configureDb();
		}
	}
	
	/**
	 * Creates a new instance of the MediaLibraryStorage which can be retrieved through getInstance()
	 * @param fileName the name of the database file
	 */
	public static void configure(String fileName){
		if(instance != null){
			if(log.isDebugEnabled()) log.debug("Dispose of the currently active instance, as configure has been called");
			instance.dipose();
			instance = null;
		}
		instance = new MediaLibraryStorage(fileName);
	}
	
	/**
	 * Gets the static instance of MediaLibraryStorage
	 * @return MediaLibraryStorage instance
	 */
	public static MediaLibraryStorage getInstance() {
		return instance;
	}
	
	/**
	 * Disposes the storage by releasing the connection pool
	 */
	public void dipose(){
		if(cp != null){
	        cp.dispose();
            cp = null;
	        if(log.isInfoEnabled()) log.info("Disposed of the JDBC connection pool while disposing MediaLibraryStorage");
		}
	}
	
	/*********************************************
	 * 
	 * Global
	 * 
	 *********************************************/

	@Override
	public void reset() {
		dbInitializer.resetDb();
	}
	
	@Override
	public String getStorageVersion(){
		String res = null;
		try {
			res = dbGlobal.getDbVersion();
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}
		
	@Override
	public String getMetaDataValue(String key){
		String res = null;
		try {
			res = dbGlobal.getMetaDataValue(key);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}
		
	@Override
	public void setMetaDataValue(String key, String value){
		try {
			dbGlobal.setMetaDataValue(key, value);
			if(log.isDebugEnabled()) log.debug(String.format("Metadata value set. key=%s, value=%s", key, value));
		} catch (StorageException e) {
			log.error("Storage error (set)", e);
		}
	}

	@Override
    public boolean isFunctional() {
	    return dbInitializer != null && dbInitializer.isConnected();
    }

	@Override
    public void cleanStorage() {
		int nbAudio = 0;
		int nbPictures = 0;
	    int nbVideo = 0;
	    
	    try {
			nbVideo = dbVideoFileInfo.cleanVideoFileInfos();
			if(log.isInfoEnabled()) log.info(String.format("%s videos removed from library while cleaning storage", nbVideo));
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
	    
		String statusMsg = String.format(Messages.getString("ML.Messages.CleanLibraryDone"), nbVideo, nbAudio, nbPictures);
		PMS.get().getFrame().setStatusLine(statusMsg);
    }
	
	@Override
	public long getRootFolderId(){
		long rootFolderId;
		try{
			rootFolderId = Long.parseLong(MediaLibraryStorage.getInstance().getMetaDataValue(MetaDataKeys.ROOT_FOLDER_ID.toString()));
		} catch(Exception ex){
			rootFolderId = MediaLibraryStorage.ROOT_FOLDER_ID;
		}
		return rootFolderId;
	}
	
	/*********************************************
	 * 
	 * Table columns
	 * 
	 *********************************************/
	
	@Override
	public List<DOTableColumnConfiguration> getTableColumnConfiguration(FileType fileType) {
		List<DOTableColumnConfiguration> res = null;
		try {
			res = dbTableColumn.getTableColumnConfiguration(fileType);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}

	@Override
	public void insertTableColumnConfiguration(DOTableColumnConfiguration c, FileType fileType) {
		try {
			dbTableColumn.insertTableColumnConfiguration(c, fileType);
			if(log.isDebugEnabled()) log.debug(String.format("Inserted table column configuration. %s, index=%s, width=%s", c.getConditionType(), c.getColumnIndex(), c.getWidth()));
		} catch (StorageException e) {
			log.error("Storage error (insert)", e);
		}
	}

	@Override
	public void updateTableColumnConfiguration(DOTableColumnConfiguration c, FileType fileType) {
		try {
			dbTableColumn.updateTableColumnConfiguration(c, fileType);
			if(log.isDebugEnabled()) log.debug(String.format("Updated table column configuration. %s, index=%s, width=%s", c.getConditionType(), c.getColumnIndex(), c.getWidth()));
		} catch (StorageException e) {
			log.error("Storage error (update)", e);
		}
	}

	@Override
	public void updateTableColumnWidth(ConditionType ct, int width, FileType fileType) {
		try {
			dbTableColumn.updateTableColumnConfiguration(ct, width, fileType);
			if(log.isDebugEnabled()) log.debug(String.format("Updated table column width for conditionType=%s, fileType=%s, width=%s", ct, fileType, width));
		} catch (StorageException e) {
			log.error("Storage error (update)", e);
		}
	}

	@Override
	public void deleteTableColumnConfiguration(DOTableColumnConfiguration cConf, FileType fileType) {
		try {
			//get the existing columns
			List<DOTableColumnConfiguration> existingColumns = getTableColumnConfiguration(fileType);
			
			//clear the existing columns
			dbTableColumn.clearTableColumnConfiguration(fileType);
			
			//insert all columns with updated indexes except the deleted one
			int index = 0;
			for(DOTableColumnConfiguration c : existingColumns) {
				if(!cConf.equals(c)) {
					c.setColumnIndex(index++);
					dbTableColumn.insertTableColumnConfiguration(c, fileType);
				}
			}
			
			if(log.isDebugEnabled()) log.debug(String.format("Deleted table column configuration for file type=%s, column index=%s", fileType, cConf.getColumnIndex()));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}
	}

	@Override
	public void deleteAllTableColumnConfiguration(FileType fileType) {
		try {
			//clear the existing columns
			dbTableColumn.deleteAllTableColumnConfiguration(fileType);
			
			if(log.isDebugEnabled()) log.debug(String.format("Deleted all table column configuration for file type=%s", fileType));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}
	}

	@Override
	public void moveTableColumnConfiguration(int fromIndex, int toIndex, FileType fileType) {
		try {
			//get the existing columns
			List<DOTableColumnConfiguration> existingColumns = getTableColumnConfiguration(fileType);
			
			//get the column to move
			DOTableColumnConfiguration cMove = null;
			for(DOTableColumnConfiguration c : existingColumns) {
				if(c.getColumnIndex() == fromIndex) {
					cMove = c;
					break;
				}
			}
			
			//clear the existing columns
			dbTableColumn.clearTableColumnConfiguration(fileType);
			
			//insert all columns with updated indexes. Create new objects to avoid modifying the indexes of the existing ones
			int index = 0;
			for(DOTableColumnConfiguration c : existingColumns) {
				if(c.getColumnIndex() == fromIndex) {
					//don't add the column we're moving where it previously was
					continue;
				} else if(index == toIndex) {
					//insert the moved column
					dbTableColumn.insertTableColumnConfiguration(new DOTableColumnConfiguration(cMove.getConditionType(), index++, cMove.getWidth()), fileType);
				}
				
				dbTableColumn.insertTableColumnConfiguration(new DOTableColumnConfiguration(c.getConditionType(), index++, c.getWidth()), fileType);
			}
			
			//special case where the column moves to the last position
			if(toIndex == index) {
				dbTableColumn.insertTableColumnConfiguration(new DOTableColumnConfiguration(cMove.getConditionType(), index++, cMove.getWidth()), fileType);				
			}
			
			if(log.isDebugEnabled()) log.debug(String.format("Moved table column configuration for file type=%s, from=%s, to=%s", fileType, fromIndex, toIndex));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}		
	}

	@Override
	public DOTableColumnConfiguration getTableColumnConfiguration(FileType fileType, int columnIndex) {
		DOTableColumnConfiguration res = null;
		try {
			res = dbTableColumn.getTableColumnConfiguration(fileType, columnIndex);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}

	@Override
	public void clearTableColumnConfiguration(FileType fileType) {
		try {
			dbTableColumn.clearTableColumnConfiguration(fileType);
			if(log.isDebugEnabled()) log.debug(String.format("Deleted all column configurations for file type=%s", fileType));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}
	}

	@Override
	public DOTableColumnConfiguration getTableColumnConfiguration(FileType fileType, ConditionType ct) {
		DOTableColumnConfiguration res = null;
		try {
			res = dbTableColumn.getTableColumnConfiguration(fileType, ct);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}

	@Override
	public int getTableConfigurationMaxColumnIndex(FileType fileType) {
		int res = 0;
		try {
			res = dbTableColumn.getTableConfigurationMaxColumnIndex(fileType);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}
	
	/*********************************************
	 * 
	 * FileInfo (global for video, audio and pictures)
	 * 
	 *********************************************/

	@Override
	public void deleteAllFileInfo() {
		int nbDeletedVideos = 0;
//		int nbDeletedAudio = 0;
//		int nbDeletedPictures = 0;
		
		try {
			nbDeletedVideos = dbVideoFileInfo.deleteAllVideos();
			if(log.isInfoEnabled()) log.info(String.format("Deleted %s videos", nbDeletedVideos));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}

//		dbAudioFileInfo.deleteAudioFileInfo();
//		dbPicturesFileInfo.deletePicturesFileInfo();
		
		//show deletion in GUI
		PMS.get().getFrame().setStatusLine(nbDeletedVideos + " videos have been deleted from the library");
	}

	@Override
	public void insertFileInfo(DOFileInfo fileInfo) {
		String statusMsg = null;
		
		updateCover(fileInfo);
		
		switch(fileInfo.getType()){
			case AUDIO:
			try {
				dbAudioFileInfo.insertAudioFileInfo((DOAudioFileInfo)fileInfo);
				if(log.isInfoEnabled()) log.info(String.format("Imported audio file %s", fileInfo.getFilePath()));
			} catch (StorageException e) {
				log.error("Storage error (get)", e);
			}
				break;
			case PICTURES:
				dbPicturesFileInfo.insertPicturesFileInfo((DOImageFileInfo)fileInfo);
				if(log.isInfoEnabled()) log.info(String.format("Imported picture %s", fileInfo.getFilePath()));
				break;
			case VIDEO:
			try {
				dbVideoFileInfo.insertVideoFileInfo((DOVideoFileInfo)fileInfo);
				if(log.isInfoEnabled()) log.info(String.format("Imported video file %s", fileInfo.getFilePath()));
				statusMsg = Messages.getString("ML.Messages.VideoInserted") + " " + fileInfo.toString();
			} catch (StorageException e) {
				log.error("Storage error (get)", e);
			}
				break;
		}
		
		// notify of the insert in the GUI
		if(statusMsg != null) {
			PMS.get().getFrame().setStatusLine(statusMsg);
		}
	}

	private void updateCover(DOFileInfo fileInfo) {
		//copy the thumbnail if required
		File thumbnailFile = new File(fileInfo.getThumbnailPath());
		String coverPath = FileImportHelper.getCoverPath(fileInfo.getThumbnailPath(), fileInfo);
		if(thumbnailFile.exists() && !fileInfo.getThumbnailPath().equals(coverPath)) {
			try {
				FileImportHelper.copyFile(thumbnailFile, new File(coverPath), true);
				fileInfo.setThumbnailPath(coverPath);
			} catch (IOException e) {
				log.error("Failed to copy cover while importing a file info", e);
			}
		}
	}

	@Override
	public void updateFileInfo(DOFileInfo fileInfo) {
		String statusMsg = null;
		
		updateCover(fileInfo);
		
		switch(fileInfo.getType()){
			case AUDIO:
				dbAudioFileInfo.updateAudioFileInfo((DOAudioFileInfo)fileInfo);
				if(log.isInfoEnabled()) log.info(String.format("Updated audio file %s", fileInfo.getFilePath()));
				break;
			case PICTURES:
				dbPicturesFileInfo.updatePicturesFileInfo((DOImageFileInfo)fileInfo);
				if(log.isInfoEnabled()) log.info(String.format("Updated picture %s", fileInfo.getFilePath()));
				break;
			case VIDEO:
			try {
				dbVideoFileInfo.updateFileInfo((DOVideoFileInfo)fileInfo);
				if(log.isDebugEnabled()) log.debug(String.format("Updated video file %s", fileInfo.getFilePath()));
				statusMsg = Messages.getString("ML.Messages.VideoUpdated") + " " + fileInfo.toString();
			} catch (StorageException e) {
				log.error("Storage error (update)", e);
			}
			break;
		}
		
		// notify of the insert in the GUI
		if(statusMsg != null) {
			PMS.get().getFrame().setStatusLine(statusMsg);
		}
	}

	@Override
	public void deleteFileInfoByFilePath(String filePath) {
		try {
			dbFileInfo.deleteFileInfoByFilePath(filePath);
			if(log.isInfoEnabled()) log.info(String.format("Deleted file %s from library", filePath));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}
	}

	@Override
	public java.util.Date getFileInfoLastUpdated(String filePath) {
		java.util.Date res = null;
		try {
			res = dbFileInfo.getFileInfoLastUpdated(filePath);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}

	@Override
	public void updatePlayCount(long fileId, int playTimeSec, Date datePlayEnd) {
		try {
			dbFileInfo.updateFilePlay(fileId, playTimeSec, datePlayEnd);
			if(log.isDebugEnabled()) log.debug(String.format("Plays updated for file with id=%s play time=%ssec", fileId, playTimeSec));
		} catch (StorageException e) {
			log.error("Storage error (update)", e);
		}
	}

	@Override	
	public void updatePlayCount(String filePath, int playTimeSec, Date datePlayEnd) {
		try {
			long fileId = dbFileInfo.getIdForFilePath(filePath);
			if (fileId <= 0) {
				//TODO: insert the file if it doesn't already exist!?
				log.error("File id is less than zero : " + fileId + ", playTimeSec:" + playTimeSec + ", date play end:" + datePlayEnd + " for path :" + filePath);
			} else {
				updatePlayCount(fileId, playTimeSec, datePlayEnd);
				if (log.isDebugEnabled())
					log.debug(String.format("Plays updated for file '%s'. play time=%ssec", filePath, playTimeSec));
			}
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
	}

	@Override
	public List<DOFileInfo> getFileInfo(DOFilter filter, boolean sortAscending, ConditionType sortField, int maxResults, SortOption sortOption) {
		List<DOFileInfo> res = null;
		try {
			res = dbFileInfo.getFileInfo(filter, sortAscending, sortField, maxResults, sortOption);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}
	
	/*********************************************
	 * 
	 * VideoFileInfo
	 * 
	 *********************************************/

	@Override
	public void deleteAllVideos() {
		try {
			int nbDeleted = dbVideoFileInfo.deleteAllVideos();
			if(log.isInfoEnabled()) log.info(String.format("Deleted %s videos", nbDeleted));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}
	}
	
	@Override
	public void deleteVideo(long fileId) {
		try {
			dbVideoFileInfo.deleteVideo(fileId);
			if(log.isInfoEnabled()) log.info(String.format("Deleted video with id=%s", fileId));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}
	}

	@Override
	public List<DOVideoFileInfo> getVideoFileInfo(DOFilter filter, boolean sortAscending, ConditionType sortField, int maxResults, SortOption sortOption, boolean onlyActive) {
		List<DOVideoFileInfo> res = null;
		try {
			res = dbVideoFileInfo.getVideoFileInfo(filter, sortAscending, sortField, sortOption, maxResults, onlyActive);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}
	
	@Override
	public List<String> getVideoProperties(ConditionType conditionType, boolean isAscending, int minOccurences){
		List<String> res = null;
		try {
			res = dbVideoFileInfo.getVideoProperties(conditionType, isAscending, minOccurences);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}
	
	@Override
	public int getFilteredVideoCount(DOFilter filter) {
		int res = 0;
		try {
			res = dbVideoFileInfo.getFilteredVideoCount(filter);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
    }

	@Override
    public int getVideoCount() {
		int res = 0;
		try {
			res = dbVideoFileInfo.getVideoCount();
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
    }
	
	/*********************************************
	 * 
	 * AudioFileInfo
	 * 
	 *********************************************/

	@Override
	public void deleteAudioFileInfo() {
		int nbDeleted = dbAudioFileInfo.deleteAudioFileInfo();
		if(log.isInfoEnabled()) log.info(String.format("Deleted %s videos", nbDeleted));
	}

	@Override
    public int getAudioCount() {
		int res = 0;
		try {
			res = dbAudioFileInfo.getAudioCount();
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
    }
	
	/*********************************************
	 * 
	 * PicturesFileInfo
	 * 
	 *********************************************/

	@Override
	public void deletePicturesFileInfo() {
		int nbDeleted = dbPicturesFileInfo.deletePicturesFileInfo();
		if(log.isInfoEnabled()) log.info(String.format("Deleted %s videos", nbDeleted));
	}

	@Override
    public int getPicturesCount() {
		int res = 0;
		try {
			res = dbPicturesFileInfo.getPicturesCount();
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
    }
	
	/*********************************************
	 * 
	 * MediaLibraryFolders
	 * 
	 *********************************************/

	@Override
	public void insertFolder(DOFolder f) {
		f.setId(-1);
		try {
			dbMediaLibraryFolders.insertFolder(f);
			if(f instanceof DOMediaLibraryFolder){
				for(DOFolder child : ((DOMediaLibraryFolder)f).getChildFolders()){
					insertFolder(child);
				}
			}
			if(log.isDebugEnabled()) log.debug(String.format("Inserted folder '%s' (id=%s)", f.getName(), f.getId()));
		} catch (StorageException e) {
			log.error("Storage error (insert)", e);
		}
	}

	@Override
	public void updateFolder(DOFolder f) {
		try {
			dbMediaLibraryFolders.updateFolder(f);	
			if(log.isDebugEnabled()) log.debug(String.format("Updated folder '%s' (id=%s)", f.getName(), f.getId()));
		} catch (StorageException e) {
			log.error("Storage error (update)", e);
		}
	}
	
	@Override
	public void updateMediaLibraryFolderLocation(long id, long parentId, int locationInParent){
		try {
			dbMediaLibraryFolders.updateFolderLocation(id, parentId, locationInParent);
			if(log.isDebugEnabled()) log.debug(String.format("Updated folder location (id=%s)", id));
		} catch (StorageException e) {
			log.error("Storage error (update)", e);
		}
	}
	
	@Override
	public void deleteFolder(long id) {
		try {
			dbMediaLibraryFolders.deleteFolder(id);
			if(log.isDebugEnabled()) log.debug(String.format("Deleted folder with id=%s", id));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}
	}

	@Override
	public DOMediaLibraryFolder getMediaLibraryFolder(long initialFolderId, int depth) {
		DOMediaLibraryFolder res = null;
		try {
			res = dbMediaLibraryFolders.getMediaLibraryFolder(initialFolderId, depth);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}

	@Override
    public void updateFolderDisplayName(long folderId, String displayName) {
		try {
			dbMediaLibraryFolders.updateFolderDisplayName(folderId, displayName);
			if(log.isDebugEnabled()) log.debug(String.format("Updated name of folder with id=%s to '%s'", folderId, displayName));
		} catch (StorageException e) {
			log.error("Storage error (update)", e);
		}
    }
	
	/*********************************************
	 * 
	 * Templates
	 * 
	 *********************************************/

	@Override
    public void insertTemplate(DOTemplate template, DOFileEntryFolder fileFolder) {
		try {
			dbTemplates.insertTemplate(template, fileFolder);
			if(log.isDebugEnabled()) log.debug(String.format("Inserted template '%s' with id=%s", template.getName(), template.getId()));
		} catch (StorageException e) {
			log.error("Storage error (insert)", e);
		}
    }

	@Override
    public void updateTemplate(DOTemplate template, DOFileEntryFolder fileFolder) {
		try {
			dbTemplates.updateTemplate(template, fileFolder);
			if(log.isDebugEnabled()) log.debug(String.format("Updated template '%s' with id=%s", template.getName(), template.getId()));
		} catch (StorageException e) {
			log.error("Storage error (update)", e);
		}
    }

	@Override
	public void deleteTemplate(long templateId){
		try {
			dbTemplates.deleteTemplate(templateId);
			if(log.isDebugEnabled()) log.debug(String.format("Deleted template with id=%s", templateId));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}
	}

	@Override
    public List<DOTemplate> getAllTemplates() {
		List<DOTemplate> res = null;
		try {
			res = dbTemplates.getAllTemplates();
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
    }

	@Override
    public boolean isTemplateIdInUse(long templateId) {
		boolean res = true;
		try {
			res = dbTemplates.isTemplateIdInUse(templateId);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
    }
	
	/*********************************************
	 * 
	 * FileFolder
	 * 
	 *********************************************/

	@Override
    public DOFileEntryFolder getFileFolder(long templateId) {
		DOFileEntryFolder res = null;
		try {
			res = dbFileFolder.getFileFolder(templateId);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}
	
	/*********************************************
	 * 
	 * ManagedFolders
	 * 
	 *********************************************/

	@Override
	public List<DOManagedFile> getManagedFolders() {
		List<DOManagedFile> res = null;
		try {
			res = dbManagedFolders.getManagedFolders();
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
    }

	@Override
	public void setManagedFolders(List<DOManagedFile> folders){
		try {
			dbManagedFolders.setManagedFolders(folders);
			if(log.isDebugEnabled()) log.debug(String.format("Saved %s managed folders", folders.size()));
		} catch (StorageException e) {
			log.error("Storage error (set)", e);
		}
	}
	
	/*********************************************
	 * 
	 * File import
	 * 
	 *********************************************/

	@Override
	public List<DOFileImportTemplate> getFileImportTemplates() {
		List<DOFileImportTemplate> res = null;
		try {
			res = dbFileImport.getFileImportTemplates();
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}

	@Override
	public DOFileImportTemplate getFileImportTemplate(int templateId) {
		DOFileImportTemplate res = null;
		try {
			res = dbFileImport.getFileImportTemplate(templateId);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;	
	}

	@Override
	public boolean isFileImportTemplateInUse(int templateId) {
		boolean res = false;
		try {
			res = templateId == 1 || dbFileImport.isFileImportTemplateInUse(templateId);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}

	@Override
	public void insertFileImportTemplate(DOFileImportTemplate template) {
		try {
			dbFileImport.insertTemplate(template);
			if(log.isDebugEnabled()) log.debug(String.format("Inserted import template '%s' with id=%s", template.getName(), template.getId()));
		} catch (StorageException e) {
			log.error("Storage error (insert)", e);
		}
	}

	@Override
	public void updateFileImportTemplate(DOFileImportTemplate template) {
		try {
			dbFileImport.updateTemplate(template);
			if(log.isDebugEnabled()) log.debug(String.format("Updated import template '%s' with id=%s", template.getName(), template.getId()));
		} catch (StorageException e) {
			log.error("Storage error (update)", e);
		}		
	}

	@Override
	public void deleteFileImportTemplate(int templateId) {
		try {
			dbFileImport.deleteFileImportTemplate(templateId);
			if(log.isDebugEnabled()) log.debug(String.format("Deleted file import template with id=%s", templateId));
		} catch (StorageException e) {
			log.error("Storage error (delete)", e);
		}
	}

	@Override
	public List<String> getExistingTags(FileType fileType) {
		List<String> res = new ArrayList<String>();
		try {
			res = dbFileInfo.getExistingTags(fileType);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}
	
	@Override
	public List<String> getTagValues(String tagName, boolean isAscending, int minOccurences){
		List<String> res = null;
		try {
			res = dbFileInfo.getTagValues(tagName, isAscending, minOccurences);
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}
	
	/*********************************************
	 * 
	 * Quick Tags
	 * 
	 *********************************************/

	@Override
	public void setQuickTagEntries(List<DOQuickTagEntry> quickTags) {
		try {
			dbQuickTag.setQuickTags(quickTags);
			if(log.isDebugEnabled()) log.debug(String.format("Inserted %s quick tags", quickTags.size()));
		} catch (StorageException e) {
			log.error("Storage error (insert)", e);
		}
	}

	@Override
	public List<DOQuickTagEntry> getQuickTagEntries() {
		List<DOQuickTagEntry> res = null;
		try {
			res = dbQuickTag.getQuickTags();
		} catch (StorageException e) {
			log.error("Storage error (get)", e);
		}
		return res;
	}
}