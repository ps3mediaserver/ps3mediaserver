package net.pms.medialibrary.commons.interfaces;

import java.util.Date;
import java.util.List;

import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFolder;
import net.pms.medialibrary.commons.dataobjects.DOFolder;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.dataobjects.DOQuickTagEntry;
import net.pms.medialibrary.commons.dataobjects.DOTableColumnConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOTemplate;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.SortOption;

public interface IMediaLibraryStorage {

	//Global
	void reset();
	void cleanStorage();
	String getStorageVersion();
	String getMetaDataValue(String key);
	void setMetaDataValue(String key, String value);
	boolean isFunctional();
	
	//Table column configurations
	List<DOTableColumnConfiguration> getTableColumnConfiguration(FileType fileType);
	void insertTableColumnConfiguration(DOTableColumnConfiguration c, FileType fileType);
	void updateTableColumnConfiguration(DOTableColumnConfiguration c, FileType fileType);
	void updateTableColumnWidth(ConditionType ct, int width, FileType fileType);
	DOTableColumnConfiguration getTableColumnConfiguration(FileType fileType, int columnIndex);
	void clearTableColumnConfiguration(FileType fileType);
	DOTableColumnConfiguration getTableColumnConfiguration(FileType fileType, ConditionType ct);
	int getTableConfigurationMaxColumnIndex(FileType fileType);
	void deleteTableColumnConfiguration(DOTableColumnConfiguration doTableColumnConfiguration, FileType fileType);
	void deleteAllTableColumnConfiguration(FileType fileType);
	void moveTableColumnConfiguration(int fromIndex, int toIndex, FileType fileType);
	
	//FileInfo (global for video, audio and pictures)
	void deleteAllFileInfo();	
	void insertFileInfo(DOFileInfo fileInfo);
	void updateFileInfo(DOFileInfo fileInfo);
	void deleteFileInfoByFilePath(String filePath);
	Date getFileInfoLastUpdated(String fileName);
	long getRootFolderId();
	void updatePlayCount(long fileId, int playTimeSec, Date datePlayEnd);
	void updatePlayCount(String filePath, int playTimeSec, Date datePlayEnd);
	List<DOFileInfo> getFileInfo(DOFilter filter, boolean sortAscending, ConditionType sortField, int maxResults, SortOption sortOption);
	List<String> getExistingTags(FileType fileType);
	List<String> getTagValues(String tagName, boolean isAscending, int minOccurences);
	
	//VideoFileInfo
	void deleteAllVideos();
	List<DOVideoFileInfo> getVideoFileInfo(DOFilter filter, boolean sortAscending, ConditionType sortField, int maxResults, SortOption sortOption, boolean onlyActive);
	List<String> getVideoProperties(ConditionType conditionType, boolean isAscending, int minOccurences);
	int getFilteredVideoCount(DOFilter filter);
	int getVideoCount();	
	void deleteVideo(long fileId);
	
	//AudioFileInfo
	void deleteAudioFileInfo();
	int getAudioCount();
	
	//PicturesFileInfo
	void deletePicturesFileInfo();
	int getPicturesCount();
	
	//MediaLibraryFolders
	void insertFolder(DOFolder child);
	void updateFolder(DOFolder f);
	void updateMediaLibraryFolderLocation(long id, long parentId, int locationInParent);
	void deleteFolder(long id);
	DOMediaLibraryFolder getMediaLibraryFolder(long initialFolderId, int depth);
	void updateFolderDisplayName(long folderId, String displayName);
	
	//Templates
	void insertTemplate(DOTemplate template, DOFileEntryFolder fileFolder);
	void updateTemplate(DOTemplate template, DOFileEntryFolder fileFolder);
	void deleteTemplate(long id);
	List<DOTemplate> getAllTemplates();	
	boolean isTemplateIdInUse(long templateId);
	
	//FileFolder
	DOFileEntryFolder getFileFolder(long templateId);
	
	//Managed Folders
	List<DOManagedFile> getManagedFolders();
	void setManagedFolders(List<DOManagedFile> managedFolders);
	
	//File import
	List<DOFileImportTemplate> getFileImportTemplates();
	void insertFileImportTemplate(DOFileImportTemplate template);
	void updateFileImportTemplate(DOFileImportTemplate template);
	DOFileImportTemplate getFileImportTemplate(int templateId);
	void deleteFileImportTemplate(int templateId);
	boolean isFileImportTemplateInUse(int templateId);
	
	//Quick Tags
	void setQuickTagEntries(List<DOQuickTagEntry> quickTagEntries);
	List<DOQuickTagEntry> getQuickTagEntries();
}
