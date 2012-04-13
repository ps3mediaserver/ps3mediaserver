package net.pms.medialibrary.commons;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.medialibrary.commons.dataobjects.OmitPrefixesConfiguration;
import net.pms.medialibrary.commons.enumarations.MediaLibraryConstants.MetaDataKeys;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class MediaLibraryConfiguration {
	private static final Logger log = LoggerFactory.getLogger(MediaLibraryConfiguration.class);
	private IMediaLibraryStorage storage;
	private static MediaLibraryConfiguration instance;

	private MediaLibraryConfiguration(){
		storage = MediaLibraryStorage.getInstance();
	}
	
	public static MediaLibraryConfiguration getInstance(){
		if(instance == null){
			instance = new MediaLibraryConfiguration();
		}
		
		return instance;
	}
	
	public void setMaxLineLength(int length){
		storage.setMetaDataValue(MetaDataKeys.MAX_LINE_LENGTH.toString(), String.valueOf(length));
	}
	
	public int getMaxLineLength(){
		String s = storage.getMetaDataValue(MetaDataKeys.MAX_LINE_LENGTH.toString());
		
		int length = 0;
		try{
			length = Integer.parseInt(s);
		} catch(Exception e){
			log.error("Failed to parse max line length as an int");
		}
		return length;
	}
	
	public void setMediaLibraryEnabled(boolean enabled){
		storage.setMetaDataValue(MetaDataKeys.MEDIA_LIBRARY_ENABLE.toString(), String.valueOf(enabled));		
	}
	
	public boolean isMediaLibraryEnabled(){
		String s = storage.getMetaDataValue(MetaDataKeys.MEDIA_LIBRARY_ENABLE.toString());		
		
		boolean enabled = true;
		try{
			enabled = Boolean.parseBoolean(s);
		} catch(Exception e){
			log.error("Failed to parse MediaLibraryEnabled as a boolean");
		}
		return enabled;
	}

	public String getPictureSaveFolderPath() {
		String path = storage.getMetaDataValue(MetaDataKeys.PICTURE_SAVE_FOLDER_PATH.toString());
		if(!path.endsWith(File.separator)){
			path += File.separator;
		}
		return path;
    }

	public void setPictureSaveFolderPath(String folderPath) {
		storage.setMetaDataValue(MetaDataKeys.PICTURE_SAVE_FOLDER_PATH.toString(), folderPath);
    }

	public Object getDbVersion() {
	    return storage.getMetaDataValue(MetaDataKeys.VERSION.toString());
    }
	
	public void setOmitPrefixesConfiguration(OmitPrefixesConfiguration config){
		storage.setMetaDataValue(MetaDataKeys.OMIT_SORT.toString(), String.valueOf(config.isSorting()));
		storage.setMetaDataValue(MetaDataKeys.OMIT_FILTER.toString(), String.valueOf(config.isFiltering()));
		
		String prefixes = "";
		for(String k : config.getPrefixes()){
			prefixes += k + " ";
		}
		prefixes = prefixes.trim();
		storage.setMetaDataValue(MetaDataKeys.OMIT_PREFIXES.toString(), String.valueOf(prefixes));
	}
	
	public OmitPrefixesConfiguration getOmitPrefixesConfiguration(){
		String sortStr = storage.getMetaDataValue(MetaDataKeys.OMIT_SORT.toString());
		String filterStr = storage.getMetaDataValue(MetaDataKeys.OMIT_FILTER.toString());
		String prefixesStr = storage.getMetaDataValue(MetaDataKeys.OMIT_PREFIXES.toString());
		
		boolean sort = sortStr == null ? false : Boolean.valueOf(sortStr);
		boolean filter = filterStr == null ? false : Boolean.valueOf(filterStr);
		List<String> prefixes = prefixesStr == null ? new ArrayList<String>() : Arrays.asList(prefixesStr.split(" "));
		
		return new OmitPrefixesConfiguration(sort, filter, prefixes);
	}
}
