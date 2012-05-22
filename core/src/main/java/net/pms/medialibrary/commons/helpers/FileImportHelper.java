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
package net.pms.medialibrary.commons.helpers;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.ImageIcon;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.medialibrary.commons.MediaLibraryConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOAudioFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileScannerEngineConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOImageFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileProperty;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.exceptions.FileImportException;
import net.pms.medialibrary.commons.exceptions.FilePropertyImportException;
import net.pms.medialibrary.commons.exceptions.FilePropertyImportException.ExceptionType;
import net.pms.medialibrary.commons.interfaces.IProgress;
import net.pms.medialibrary.storage.MediaLibraryStorage;
import net.pms.plugins.FileImportPlugin;
import net.pms.plugins.PluginsFactory;

public class FileImportHelper {
	private static final Logger log = LoggerFactory.getLogger(FileImportHelper.class);
	
	private static Map<String, FileImportPlugin> fileImportPlugins; //key=name of the plugin, value=plugin
	private static Map<String, Date> pluginsLastQueryDate = new HashMap<String, Date>(); //key=name of the plugin, value=date when the last request to this plugin started
	
	private static int updateThreadCounter = 0;
	
	/**
	 * This method will return a map containing all prioritized engine names
	 * that can be used to query information for a file type.<br>
	 * The returned map will only contain engines being currently available.
	 * If engines are being found which haven't been configured for the given 
	 * template they will be added to the end of the list of available engines 
	 * @param template the template specifying the priorities of the engines that will be used
	 * @return a map containing the prioritized engines for all file types
	 */
	public static List<DOFileScannerEngineConfiguration> getFilePropertyEngines(DOFileImportTemplate template){
		List<DOFileScannerEngineConfiguration> res = new ArrayList<DOFileScannerEngineConfiguration>();
		
		//get the list of available plugins and create a map containing all available engines for a file property
		List<FileImportPlugin> importPlugins = PluginsFactory.getFileImportPlugins();
		Map<FileProperty, List<String>> filePropertyEngineNames = new HashMap<FileProperty, List<String>>();
		for(FileProperty fp : FileProperty.values()) {
			List<String> engineNames = new ArrayList<String>();
			for(FileImportPlugin mip : importPlugins) {
				if(mip.getSupportedFileProperties().contains(fp)) {
					engineNames.add(mip.getName());
				}
			}
			filePropertyEngineNames.put(fp, engineNames);
		}
		
		//prepare the list of enabled engines per file type
		List<String> enabledVideoEngineNames;
		if(template.getEnabledEngines().containsKey(FileType.VIDEO)){
			enabledVideoEngineNames = template.getEnabledEngines().get(FileType.VIDEO);
		} else {
			enabledVideoEngineNames = new ArrayList<String>();
		}
		List<String> enabledAudioEngineNames;
		if(template.getEnabledEngines().containsKey(FileType.AUDIO)){
			enabledAudioEngineNames = template.getEnabledEngines().get(FileType.AUDIO);
		} else {
			enabledAudioEngineNames = new ArrayList<String>();
		}
		List<String> enabledPictursEngineNames;
		if(template.getEnabledEngines().containsKey(FileType.PICTURES)){
			enabledPictursEngineNames = template.getEnabledEngines().get(FileType.PICTURES);
		} else {
			enabledPictursEngineNames = new ArrayList<String>();
		}
		
		// prioritize the engines according to the configuration
		for (FileProperty fp : filePropertyEngineNames.keySet()) {
			final List<String> configuredEngineNames = template.getEngineConfigurations(fp);
			List<String> availableEngineNames = filePropertyEngineNames.get(fp);
			List<String> enginesToUse = new ArrayList<String>();

			// only add engine names for engines which are enabled for the given
			// file type
			String fpStr = fp.toString();
			for (String engineName : availableEngineNames) {
				if (fpStr.startsWith("VIDEO_")) {
					if (enabledVideoEngineNames.contains(engineName)) {
						enginesToUse.add(engineName);
					}
				} else if (fpStr.startsWith("AUDIO_")) {
					if (enabledAudioEngineNames.contains(engineName)) {
						enginesToUse.add(engineName);
					}
				} else if (fpStr.startsWith("PICTURES_")) {
					if (enabledPictursEngineNames.contains(engineName)) {
						enginesToUse.add(engineName);
					}
				}
			}

			// sort the list of engines. The list being considered is the one
			// containing all available file import plugin names. If the plugin 
			// has been previously configured, use the pre-configured priority. 
			// If it hasn't been previously configured, add it to the bottom of
			// of the list. If more then one engine hadn't been previously
			// configured they will be sorted alphabetically.
			Collections.sort(enginesToUse, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					int res = 0;
					if (configuredEngineNames.contains(o1) && configuredEngineNames.contains(o2)) {
						res = configuredEngineNames.indexOf(o1) > configuredEngineNames.indexOf(o2) ? 1 : -1;
					} else if (configuredEngineNames.contains(o1) && !configuredEngineNames.contains(o2)) {
						res = -1;
					} else if (!configuredEngineNames.contains(o1) && configuredEngineNames.contains(o2)) {
						res = 1;
					} else {
						res = o1.compareTo(o2);
					}
					return res;
				}
			});
			
			//determine if the the engine is enabled
			boolean isEnabled = false;
			for(DOFileScannerEngineConfiguration engine : template.getEngineConfigurations()) {
				if(engine.getFileProperty() == fp) {
					isEnabled = engine.isEnabled();
					break;
				}
			}

			res.add(new DOFileScannerEngineConfiguration(isEnabled, enginesToUse, fp));
		}
		
		return res;
	}

	/**
	 * The file info objects will be updated according to the rules defined in 
	 * the importConfig.
	 * @param importConfig defines which plugins will be used to update fields in fileInfo
	 * @param fileInfo the video that will be updated witch additional information
	 */
	public static void updateFileInfos(DOFileImportTemplate importConfig, List<DOFileInfo> fileInfos){
		updateFileInfos(importConfig, fileInfos, false, null);
	}

	/**
	 * The file info objects will be updated according to the rules defined in 
	 * the importConfig.
	 * @param importConfig defines which plugins will be used to update fields in fileInfo
	 * @param fileInfo the video that will be updated witch additional information
	 * @param async the operation will be performed asynchronously if true; synchronously if false
	 */
	public static void updateFileInfos(final DOFileImportTemplate importConfig, final List<DOFileInfo> fileInfos, boolean async, final IProgress callback){
		if(async) {
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					updateFileInfosInternal(importConfig, fileInfos, callback);
				}
			};
			Thread th = new Thread(r);
			th.setName("update" + updateThreadCounter++);
			th.start();
		} else {
			updateFileInfosInternal(importConfig, fileInfos, callback);			
		}
	}
	
	/**
	 * Private method used to run the same update operation sync and async
	 * @param importConfig defines which plugins will be used to update fields in fileInfo
	 * @param callback 
	 * @param fileInfo the video that will be updated witch additional information
	 */
	private static void updateFileInfosInternal(DOFileImportTemplate importConfig, List<DOFileInfo> fileInfos, IProgress callback){
		int nbFilesProcessed = 0;
		for(DOFileInfo fileInfo : fileInfos) {
			updateFileInfo(importConfig, (DOVideoFileInfo) fileInfo);
			
			//update the DB
			MediaLibraryStorage.getInstance().updateFileInfo(fileInfo);
			
			if(callback != null) {
				callback.reportProgress(100 * nbFilesProcessed / fileInfos.size());
			}
			
			nbFilesProcessed++;
		}
		
		if(callback != null) {
			callback.workComplete();
		}		
	}
	
	/**
	 * The file info object will be updated according to the plugin
	 *
	 * @param plugin the plugin
	 * @param fileInfo the video that will be updated witch additional information
	 */
	public static void updateFileInfo(FileImportPlugin plugin, DOFileInfo fileInfo) {
		// iterate through all file properties and respect the configured priorities to retrieve the information
		for (FileProperty fileProperty : plugin.getSupportedFileProperties()) {
			// try to get the value
			Object value = null;
			try {
				value = plugin.getFileProperty(fileProperty);
			} catch (Throwable t) {
				// catch throwable for every external call to avoid a plugin crashing pms
				log.error(String.format("Failed to query plugin='%s' for file property='%s'", plugin.getName(), fileProperty), t);
				continue;
			}

			// set the value if it is valid, log a comprehensive log message otherwise
			try {
				if (fileInfo instanceof DOVideoFileInfo) {
					setValue(value, fileProperty, (DOVideoFileInfo) fileInfo);
				} else if (fileInfo instanceof DOAudioFileInfo) {
					// TODO: implement
				} else if (fileInfo instanceof DOImageFileInfo) {
					// TODO: implement
				}
				log.debug(String.format("Imported %s='%s' with plugin='%s' for file='%s'", fileProperty, value, plugin.getName(), fileInfo.getFilePath()));
			} catch (FilePropertyImportException ex) {
				switch (ex.getExceptionType()) {
				case NoResult:
					log.debug(String.format("No result found for FileProperty='%s' with plugin='%s' for file='%s'", fileProperty, plugin.getName(), fileInfo.getFilePath()));
					break;
				case WrongType:
					log.error(String.format("The plugin='%s' returned a value of the wrong type for the FileProperty='%s'. Expected='%s', Received='%s'", plugin.getName(), fileProperty, ex.getExpectedType(), ex.getReceivedType()));
					break;
				case ProcessingFailed:
					log.error(String.format("An operation failed while trying to get the FileProperty='%s' with plugin='%s' for file='%s'", fileProperty, plugin.getName(), fileInfo.getFilePath()));
				}
			}
		}

		// load tags
		List<String> supportedTags = plugin.getSupportedTags(fileInfo.getType());
		if(supportedTags != null) {
			for (String tagName : supportedTags) {
				if(!tagName.matches("[a-zA-Z0-9]*")) {
					log.warn(String.format("Don't collect the tag with name='%s', because only alphanumeric tag names (a-z 0-9) are allowed", tagName));
					continue;
				}
				
				List<String> tagValues;
				try {
					tagValues = plugin.getTags(tagName);
				} catch (Throwable t) {
					// catch throwable for every external call to avoid a plugin crashing pms
					log.error(String.format("Failed to get tag '%s' for plugin '%s'", tagName, plugin.getName()), t);
					continue;
				}
	
				Map<String, List<String>> allTags = new HashMap<String, List<String>>();
				if (tagValues != null && tagValues.size() > 0) {
					if (!allTags.containsKey(tagName)) {
						allTags.put(tagName, new ArrayList<String>());
					}
	
					// make sure the values are unique for a tag and that they have been trimmed
					List<String> uniqueTagValues = allTags.get(tagName);
					for (String tagValue : tagValues) {
						tagValue = tagValue.trim();
						if (!uniqueTagValues.contains(tagValue)) {
							log.trace(String.format("Added tag %s=%s for file %s", tagName, tagValue, fileInfo.getFilePath()));
							uniqueTagValues.add(tagValue);
						}
					}
	
					fileInfo.getTags().put(tagName, uniqueTagValues);
				}
				fileInfo.setTags(allTags);
			}
		}
	}
	
	/**
	 * The file info object will be updated according to the rules defined in 
	 * the importConfig.
	 * @param importConfig defines which plugins will be used to update fields in fileInfo
	 * @param fileInfo the video that will be updated witch additional information
	 */
	public static void updateFileInfo(DOFileImportTemplate importConfig, DOFileInfo fileInfo) {
		//lazy-load file import plugins
		if(fileImportPlugins == null) {
			fileImportPlugins = new HashMap<String, FileImportPlugin>();
			for(FileImportPlugin p : PluginsFactory.getFileImportPlugins()){
				fileImportPlugins.put(p.getName(), p);
			}
		}
		
		//either use the name of the video + year or the clean file name
		String cleanName = null;
		if(fileInfo instanceof DOVideoFileInfo) {
			DOVideoFileInfo videoFileInfo = (DOVideoFileInfo) fileInfo;
			if(!videoFileInfo.getName().equals("")) {
				String yearString = videoFileInfo.getYear() == 0 ? "" : String.format(" (%s)", videoFileInfo.getYear());
				cleanName = videoFileInfo.getName() + yearString;
			}
		}
		if(cleanName == null) {
			cleanName = getCleanName(fileInfo.getFileName(false));
		}
		String filePath = fileInfo.getFilePath();

		Map<String, FileImportPlugin> queriedPlugins = new HashMap<String, FileImportPlugin>();  //key=plugin name, value = plugin
		List<String> failedImportPluginNames = new ArrayList<String>(); //list of all plugin names, where the plugin query failed
		
		//iterate through all file properties and respect the 
		//configured priorities to retrieve the information
		for(FileProperty fileProperty : FileProperty.values()) {
			if(!fileProperty.toString().startsWith("VIDEO_")){
				//we only want to import properties for video files for now; discard the rest
				continue;
			}
			
			//check if the file property is disabled
			boolean isEngineEnabled = false;
			for(DOFileScannerEngineConfiguration engine : importConfig.getEngineConfigurations()) {
				if(engine.getFileProperty() == fileProperty) {
					if(engine.isEnabled()) {
						isEngineEnabled = engine.isEnabled();
						break;
					}
				}
			}
			if(!isEngineEnabled) {
				continue;
			}
			
			//get the list of engines to use for this file property
			List<String> prioritizedEngineNames = importConfig.getEngineConfigurations(fileProperty);
			
			//get the next file property if no engines have been configured to retrieve
			//info about the current one
			if(prioritizedEngineNames == null || prioritizedEngineNames.size() == 0) {
				log.debug(String.format("No plugins configured to retrieve info for the FileProperty='%s'", fileProperty));
				continue;
			}
			
			//use the value from the first configured engine which is valid
			for(String engineName : prioritizedEngineNames) {
				if(failedImportPluginNames.contains(engineName)){
					//don't try to get infos from a plugin which couldn't import the data
					continue;
				}
				
				//get the plugin from the cached list of plugins or create it and 
				//add it to the cache if it doesn't exist
				if(!queriedPlugins.containsKey(engineName)) {
					FileImportPlugin p = initPlugin(engineName, cleanName, filePath);
					if(p == null) {
						failedImportPluginNames.add(engineName);
						continue;
					} else {
						queriedPlugins.put(engineName, p);
					}
				}
				
				//try to get the value
				Object value = null;
				try{
					 value = queriedPlugins.get(engineName).getFileProperty(fileProperty);
				} catch(Throwable t){
					//catch throwable for every external call to avoid a plugin crashing pms
					log.error(String.format("Failed to query plugin='%s' for file property='%s'", engineName, fileProperty), t);
					continue;
				}
				
				//set the value if it is valid, log a comprehensive log message otherwise
				try{
					if(fileInfo instanceof DOVideoFileInfo) {
						setValue(value, fileProperty, (DOVideoFileInfo) fileInfo);
					} else if(fileInfo instanceof DOAudioFileInfo) {
						//TODO: implement
					} else if(fileInfo instanceof DOImageFileInfo) {
						//TODO: implement
					}
						
					log.debug(String.format("Imported %s='%s' with plugin='%s' for file='%s'", fileProperty, value, engineName, filePath));
					break;
				}catch(FilePropertyImportException ex){
					switch (ex.getExceptionType()){
					case NoResult:
						log.debug(String.format("No result found for FileProperty='%s' with plugin='%s' for file='%s'", fileProperty, engineName, filePath));
						break;
					case WrongType:
						log.error(String.format("The plugin='%s' returned a value of the wrong type for the FileProperty='%s'. Expected='%s', Received='%s'", engineName, fileProperty, ex.getExpectedType(), ex.getReceivedType()));
						break;
					case ProcessingFailed:
						log.error(String.format("An operation failed while trying to get the FileProperty='%s' with plugin='%s' for file='%s'", fileProperty, engineName, filePath));
					}
				}
			}
		}
		
		//load tags
		for(FileType ft : importConfig.getEnabledTags().keySet()) {
			Map<String, List<String>> tagsForEngines = importConfig.getEnabledTags().get(ft);
			
			//handle all engines
			for(String engineName : tagsForEngines.keySet()) {
				if(failedImportPluginNames.contains(engineName)) {
					//don't try to get the tags if the movie couldn't be imported for the plugin
					continue;
				}

				//get the plugin from the cached list of plugins or create it and 
				//add it to the cache if it doesn't exist
				FileImportPlugin p = null;
				if(queriedPlugins.containsKey(engineName)) {
					p = queriedPlugins.get(engineName);					
				} else {
					p = initPlugin(engineName, cleanName, filePath);
					if(p == null) {
						failedImportPluginNames.add(engineName);
						continue;
					} else {
						queriedPlugins.put(engineName, p);
					}
				}

				//handle every tag for the engine
				Map<String, List<String>> allTags = fileInfo.getTags();
				for(String tag : tagsForEngines.get(engineName)) {
					List<String> tagValues;
					try {
						tagValues = p.getTags(tag);
					} catch(Throwable t) {
						//catch throwable for every external call to avoid a plugin crashing pms
						log.error(String.format("Failed to get tag '%s' for plugin '%s'", tag, engineName), t);
						continue;
					}
										
					if(tagValues != null && tagValues.size() > 0) {
						if(!allTags.containsKey(tag)){
							allTags.put(tag, new ArrayList<String>());
						}
						
						//make sure the values are unique for a tag and that they have been trimmed
						List<String> uniqueTagValues = allTags.get(tag);
						for(String tagValue : tagValues) {
							tagValue = tagValue.trim();
							if(!uniqueTagValues.contains(tagValue)){
								log.trace(String.format("Added tag %s=%s for file %s", tag, tagValue, fileInfo.getFilePath()));								
								uniqueTagValues.add(tagValue);
							}
						}
						
						fileInfo.getTags().put(tag, uniqueTagValues);
					}
					fileInfo.setTags(allTags);
				}
			}
		}
	}

	/**
	 * Updates the fields configured in propertiesToUse from fiSource to fiDestination
	 * @param fiSource the source
	 * @param fiDestination the destination
	 * @param propertiesToUse the list of properties to copy from the source to the destination
	 */
	public static void updateFileInfo(DOFileInfo fiSource, DOFileInfo fiDestination, List<ConditionType> propertiesToUse) {
		for(ConditionType ct : propertiesToUse) {
			switch (ct) {
			case FILE_THUMBNAILPATH:
				String coverPath = fiSource.getThumbnailPath();
				try {
					String savePath = getTmpCoverPath(fiSource.getThumbnailPath(), fiSource);
					FileImportHelper.saveUrlToFile(fiSource.getThumbnailPath(), savePath);
					coverPath = savePath;
				} catch (IOException e) {
					//do nothing
				}
				fiDestination.setThumbnailPath(coverPath);
				break;
			case FILE_CONTAINS_TAG:
				//merge tags
				Map<String, List<String>> allTags = fiDestination.getTags();
				Map<String, List<String>> newTags = fiSource.getTags();
				
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
				fiDestination.setTags(allTags);
				break;			
			}
		}
		
		if(fiSource instanceof DOVideoFileInfo && fiDestination instanceof DOVideoFileInfo) {
			updateFileInfo((DOVideoFileInfo) fiSource, (DOVideoFileInfo) fiDestination, propertiesToUse);
		}
	}


	/**
	 * Updates the fields configured in propertiesToUse from fiSource to fiDestination.
	 *
	 * @param fiSource the source
	 * @param fiDestination the destination
	 * @param propertiesToUse the list of properties to copy from the source to the destination
	 */
	private static void updateFileInfo(DOVideoFileInfo fiSource, DOVideoFileInfo fiDestination, List<ConditionType> propertiesToUse) {
		for(ConditionType ct : propertiesToUse) {
			switch (ct) {
			case FILE_THUMBNAILPATH:
				String coverPath = fiSource.getThumbnailPath();
				try {
					String savePath = getTmpCoverPath(fiSource.getThumbnailPath(), fiSource);
					FileImportHelper.saveUrlToFile(fiSource.getThumbnailPath(), savePath);
					coverPath = savePath;
				} catch (IOException e) {
					//do nothing
				}
				fiDestination.setThumbnailPath(coverPath);
				break;
			case VIDEO_CERTIFICATION:
				fiDestination.getAgeRating().setLevel(fiSource.getAgeRating().getLevel());
				break;
			case VIDEO_CERTIFICATIONREASON:
				fiDestination.getAgeRating().setReason(fiSource.getAgeRating().getReason());
				break;
			case VIDEO_BUDGET:
				fiDestination.setBudget(fiSource.getBudget());
				break;
			case VIDEO_DIRECTOR:
				fiDestination.setDirector(fiSource.getDirector());
				break;
			case VIDEO_CONTAINS_GENRE:				
				//merge new with existing genres
				List<String> allGenres = fiDestination.getGenres();
				for(String genre : fiSource.getGenres()) {
					if(!allGenres.contains(genre)) {
						allGenres.add(genre);
					}
				}				
				fiDestination.setGenres(allGenres);
				break;
			case VIDEO_HOMEPAGEURL:
				fiDestination.setHomepageUrl(fiSource.getHomepageUrl());
				break;
			case VIDEO_IMDBID:
				fiDestination.setImdbId(fiSource.getImdbId());
				break;
			case VIDEO_NAME:
				fiDestination.setName(fiSource.getName());
				break;
			case VIDEO_ORIGINALNAME:
				fiDestination.setOriginalName(fiSource.getOriginalName());
				break;
			case VIDEO_SORTNAME:
				fiDestination.setSortName(fiSource.getSortName());
			case VIDEO_OVERVIEW:
				fiDestination.setOverview(fiSource.getOverview());
				break;
			case VIDEO_RATINGPERCENT:
				fiDestination.getRating().setRatingPercent(fiSource.getRating().getRatingPercent());
				break;
			case VIDEO_RATINGVOTERS:
				fiDestination.getRating().setVotes(fiSource.getRating().getVotes());
				break;
			case VIDEO_REVENUE:
				fiDestination.setRevenue(fiSource.getRevenue());
				break;
			case VIDEO_TAGLINE:
				fiDestination.setTagLine(fiSource.getTagLine());
				break;
			case VIDEO_TMDBID:
				fiDestination.setTmdbId(fiSource.getTmdbId());
				break;
			case VIDEO_TRAILERURL:
				fiDestination.setTrailerUrl(fiSource.getTrailerUrl());
				break;
			case VIDEO_YEAR:
				fiDestination.setYear(fiSource.getYear());
				break;
			}
		}
	}
	
	/**
	 * Initializes a plugin and loads the data for the given cleanFileName and/or filePath.
	 *
	 * @param engineName the name of the import engine to use
	 * @param cleanFileName the cleaned name that will be passed on to the plugin for query
	 * @param filePath the file path that will be passed on to the plugin for query
	 * @return the FileImportPlugin if it could be loaded, null otherwise
	 */
	private static FileImportPlugin initPlugin(String engineName, String cleanFileName, String filePath) {
		FileImportPlugin res = null;
		
		//query the plugin that will be used if it hasn't been for a previous file property (lazy-initialize)
		FileImportPlugin pluginToUse = fileImportPlugins.get(engineName);
		if(pluginToUse == null) {
			log.error("The plugin '%s' couldn't be found in the cached list. This should never happen!");
		} else {
			try{
				//check if it is required to wait before calling the plugin
				if(pluginsLastQueryDate.containsKey(engineName)) {
					long currentIntervalMs = new Date().getTime() - pluginsLastQueryDate.get(engineName).getTime();
					long waitMs = pluginToUse.getMinPollingIntervalMs() - currentIntervalMs;
					if(waitMs > 0) {
						log.info(String.format("Wait %sms before importing file with plugin '%s' to enforce minimum polling interval of %sms", waitMs, pluginToUse.getName(), pluginToUse.getMinPollingIntervalMs()));
						Thread.sleep(waitMs);
					}
				}
				
				//store the date when the last query for a plugin began
				pluginsLastQueryDate.put(engineName, new Date());
				
				//do the actual import
				pluginToUse.importFile(cleanFileName, filePath);
				
				//we were able to import the file, keep the plugin for future for other file properties
				res = pluginToUse;
				
				log.info(String.format("Imported file information with plugin='%s' for file with clean name='%s', path='%s'", engineName, cleanFileName, filePath));
			} catch(FileImportException ex) {
				//this is the normal way for a plugin to say it couldn't import the data
				log.info(String.format("Failed to import file with plugin='%s' for clean file name='%s' and path='%s'", engineName, cleanFileName, filePath), ex);
			} catch(Throwable t){
				//catch throwable for every external call to avoid a plugin crashing pms
				log.error(String.format("Failed to import file with plugin='%s' for clean file name='%s' and path='%s' because of an unexpected problem!", engineName, cleanFileName, filePath), t);
			}
		}
		return res;
	}

	/**
	 * Sets the value in the fileInfo for the fileProperty if it is valid, throws a FilePropertyImportException otherwise
	 * 
	 * @param value the value to set
	 * @param fileProperty the file property to set in fileinfo
	 * @param fileInfo the specified file property will be set in this fileInfo
	 * @throws FilePropertyImportException thrown if either no result was received or the type of the value wasn't correct
	 */
	@SuppressWarnings("unchecked")
	private static void setValue(Object value, FileProperty fileProperty, DOVideoFileInfo fileInfo) throws FilePropertyImportException {		
		switch (fileProperty) {
		case VIDEO_COVERURL:
			validateStringValue(value, fileProperty);
			String coverUrl = (String) value;
			try {
				String savePath = getTmpCoverPath(coverUrl, fileInfo);
				FileImportHelper.saveUrlToFile(coverUrl, savePath);
				if(new File(savePath).exists()) {
					fileInfo.setThumbnailPath(savePath);
				} else {
					throw new FilePropertyImportException(fileProperty, null, String.class, ExceptionType.NoResult);
				}
			} catch (IOException e) {
				//do nothing
			}
			
			break;
		case VIDEO_CERTIFICATION:
			validateStringValue(value, fileProperty);
			fileInfo.getAgeRating().setLevel((String)value);
			break;
		case VIDEO_CERTIFICATIONREASON:
			validateStringValue(value, fileProperty);
			fileInfo.getAgeRating().setReason((String)value);
			break;
		case VIDEO_BUDGET:
			validateIntegerValue(value, fileProperty);
			fileInfo.setBudget((Integer)value);
			break;
		case VIDEO_DIRECTOR:
			validateStringValue(value, fileProperty);
			fileInfo.setDirector((String)value);
			break;
		case VIDEO_GENRES:
			if(value == null) {
				throw new FilePropertyImportException(fileProperty, null, List.class, ExceptionType.NoResult);
			} else if(!(value instanceof List<?>)){
				throw new FilePropertyImportException(fileProperty, value.getClass(), List.class, ExceptionType.WrongType);
			} else {
				//we've got an untyped list, check the contained elements
				for(Object o : (List<?>)value) {
					if(!(o instanceof String)) {
						throw new FilePropertyImportException(fileProperty, value.getClass(), List.class, ExceptionType.WrongType);
					}
				}
				
				if(((List<?>)value).size() == 0) {
					throw new FilePropertyImportException(fileProperty, null, List.class, ExceptionType.NoResult);
				}
			}
			
			//merge new with existing genres
			List<String> allGenres = fileInfo.getGenres();
			for(String genre : (List<String>)value) {
				if(!allGenres.contains(genre)) {
					allGenres.add(genre);
				}
			}
			
			fileInfo.setGenres(allGenres);
			break;
		case VIDEO_HOMEPAGEURL:
			validateStringValue(value, fileProperty);
			fileInfo.setHomepageUrl((String)value);
			break;
		case VIDEO_IMDBID:
			validateStringValue(value, fileProperty);
			fileInfo.setImdbId((String)value);
			break;
		case VIDEO_NAME:
			validateStringValue(value, fileProperty);
			fileInfo.setName((String)value);
			break;
		case VIDEO_ORIGINALNAME:
			validateStringValue(value, fileProperty);
			fileInfo.setOriginalName((String)value);
			break;
		case VIDEO_OVERVIEW:
			validateStringValue(value, fileProperty);
			fileInfo.setOverview((String)value);
			break;
		case VIDEO_RATINGPERCENT:
			validateIntegerValue(value, fileProperty);
			fileInfo.getRating().setRatingPercent((Integer)value);
			break;
		case VIDEO_RATINGVOTERS:
			validateIntegerValue(value, fileProperty);
			fileInfo.getRating().setVotes((Integer)value);
			break;
		case VIDEO_REVENUE:
			validateIntegerValue(value, fileProperty);
			fileInfo.setRevenue((Integer)value);
			break;
		case VIDEO_TAGLINE:
			validateStringValue(value, fileProperty);
			fileInfo.setTagLine((String)value);
			break;
		case VIDEO_TMDBID:
			validateIntegerValue(value, fileProperty);
			fileInfo.setTmdbId((Integer)value);
			break;
		case VIDEO_TRAILERURL:
			validateStringValue(value, fileProperty);
			fileInfo.setTrailerUrl((String)value);
			break;
		case VIDEO_YEAR:
			validateIntegerValue(value, fileProperty);
			fileInfo.setYear((Integer)value);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Creates the file path where to save the cover for the file info.<br>
	 * The path is consists of <pictures_save_folder_path>/<file_name>.cover.<extension> 
	 * @param coverPath the path of the cover (used to determine the extension)
	 * @param fileInfo the file info to create the cover path for
	 * @return the cover path or null if coverPath doesn't contain a dot
	 */
	public static String getCoverPath(String coverPath, DOFileInfo fileInfo) {
		String res = null;
		if(coverPath != null && coverPath.contains(".")) {
			String ext = coverPath.substring(coverPath.lastIndexOf('.'), coverPath.length());
			res = MediaLibraryConfiguration.getInstance().getPictureSaveFolderPath() + fileInfo.getFileName().replaceAll("\\\\|/|:|\\*|\\?|\"|<|>|\\|", "_") + ".cover" + ext;			
		}
		return res;
	}
	
	private static String getTmpCoverPath(String coverPath, DOFileInfo fileInfo) {
		String res = null;
		if(coverPath != null && coverPath.contains(".")) {
			String ext = coverPath.substring(coverPath.lastIndexOf('.'), coverPath.length());
			try {
				res = PMS.getConfiguration().getTempFolder() + File.separator + fileInfo.getFileName().replaceAll("\\\\|/|:|\\*|\\?|\"|<|>|\\|", "_") + ".cover" + ext;
			} catch (IOException e) {
				res = "";
			}			
		}
		return res;
	}

	/**
	 * Throws a FilePropertyImportException if the type of value is not a positive Integer, does nothing otherwise
	 * @param value value to validate
	 * @param fileProperty FileProperty for the value
	 * @throws FilePropertyImportException thrown if the type of value is not a positive Integer 
	 */
	private static void validateIntegerValue(Object value, FileProperty fileProperty) throws FilePropertyImportException {
		if(value == null) {
			throw new FilePropertyImportException(fileProperty, null, Integer.class, ExceptionType.NoResult);
		} else if(!(value instanceof Integer)){
			throw new FilePropertyImportException(fileProperty, value.getClass(), Integer.class, ExceptionType.WrongType);				
		} else if(((Integer)value) < 0) {
			throw new FilePropertyImportException(fileProperty, null, Integer.class, ExceptionType.NoResult);				
		}
	}

	/**
	 * Throws a FilePropertyImportException if the type of value is not a String or is an empty String, does nothing otherwise
	 * @param value value to validate
	 * @param fileProperty FileProperty for the value
	 * @throws FilePropertyImportException thrown if the type of value is not a String or is an empty String, does nothing otherwise
	 */
	private static void validateStringValue(Object value, FileProperty fileProperty) throws FilePropertyImportException {
		if(value == null) {
			throw new FilePropertyImportException(fileProperty, null, String.class, ExceptionType.NoResult);
		} else if(!(value instanceof String)){
			throw new FilePropertyImportException(fileProperty, value.getClass(), String.class, ExceptionType.WrongType);				
		} else if(((String)value).equals("")) {
			throw new FilePropertyImportException(fileProperty, null, Integer.class, ExceptionType.NoResult);				
		}
	}

	/**
	 * Cleans the received fileName according to the rules defined in filename_replace_expressions.txt
	 * @param fileName the name to clean
	 * @return the cleaned name
	 */
	private static String getCleanName(String fileName) {
		File configFile = new File(FilenameUtils.concat(PMS.getConfiguration().getProfileDirectory(), "filename_replace_expressions.txt"));
		
		String res = fileName;
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(configFile.getAbsolutePath()), "UTF-8");
			String regex;
			String replaceText;
			while (scanner.hasNextLine()) {
				regex = scanner.nextLine();
				if (scanner.hasNextLine()) {
					replaceText = scanner.nextLine();
				}else {
					break;
				}
				res = res.replaceAll(regex, replaceText);
			}
		} catch (FileNotFoundException e) {
			if(log.isDebugEnabled()) log.debug(String.format("File '%s' not found. Ignore file name cleaning", configFile.getAbsolutePath()));
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}

		res = res.trim();
		if(!res.equals(fileName)) {
			if(log.isInfoEnabled()) log.info(String.format("Cleaned up file '%s' to '%s'", fileName, res));
		}
		return res;
	}

	/**
	 * Saves the file specified by the url to the file saveFileName
	 * @param url url of the file to save
	 * @param saveFileName absolute path to save the file to
	 * @throws IOException thrown if the save operation failed or the content type of the file was of type text
	 */
	public static void saveUrlToFile(String url, String saveFileName) throws IOException {
		URL u = new URL(url);
		URLConnection uc = u.openConnection();
		String contentType = uc.getContentType();
		int contentLength = uc.getContentLength();
		if (contentType.startsWith("text/") || contentLength == -1) {
			throw new IOException("This is not a binary file.");
		}
		InputStream raw = uc.getInputStream();
		InputStream in = new BufferedInputStream(raw);
		byte[] data = new byte[contentLength];
		int bytesRead = 0;
		int offset = 0;
		while (offset < contentLength) {
			bytesRead = in.read(data, offset, data.length - offset);
			if (bytesRead == -1)
				break;
			offset += bytesRead;
		}
		in.close();

		if (offset != contentLength) {
			throw new IOException("Only read " + offset + " bytes; Expected "
					+ contentLength + " bytes");
		}

		FileOutputStream out = new FileOutputStream(saveFileName);
		out.write(data);
		out.flush();
		out.close();
		
		log.debug(String.format("Saved url='%s' to file='%s'", url, saveFileName));
	}

	/**
	 * Returns the list of all available file import engines for the file type
	 * @param fileType file type for which to get the engines
	 * @return list of all available engine names
	 */
	public static List<String> getAvailableEngineNames(FileType fileType) {
		List<String> res = new ArrayList<String>();
		
		for(FileImportPlugin p : PluginsFactory.getFileImportPlugins()) {
			try {
				if(p.getSupportedFileTypes().contains(fileType)) {
					res.add(p.getName());
				} 
			} catch (Throwable t) {
				//catch throwable for every external call to avoid a plugin crash pms
				log.error("Failed to get plugin name", t);
			}
		}
		
		return res;
	}

	/**
	 * Returns a map with key=file import engine name and value=available tags for the engine
	 * @param fileType limits the engines for the given file type
	 * @param template template containing the configured tags
	 * @return map with key=file import engine name and value=available tags for the engine
	 */
	public static Map<String, List<String>> getTagNamesPerEngine(FileType fileType, DOFileImportTemplate template) {
		Map<String, List<String>> res = new HashMap<String, List<String>>();
		for(FileImportPlugin p : PluginsFactory.getFileImportPlugins()) {
			//only add engine names which are configured to allow the current file type and have at least one tag
			if(template.getEnabledEngines().get(fileType).contains(p.getName()) && p.getSupportedFileTypes().contains(fileType)) {
				List<String> supportedTags = new ArrayList<String>();
				for(String tagName :  p.getSupportedTags(fileType)) {
					if(tagName.matches("[a-zA-Z0-9]*")) {
						supportedTags.add(tagName);
					} else {
						//don't allow tag names which aren't alphanumeric
						log.warn(String.format("Don't use the tag with name='%s', because only alphanumeric tag names (a-z 0-9) are allowed", tagName));
					}
				}
				
				if(supportedTags != null) {
					res.put(p.getName(), supportedTags);
				}
			}
		}		
		
		return res;
	}
	
	/**
	 * Creates a buffered image from an image
	 * @param image the image
	 * @return the buffered image
	 */
	public static BufferedImage getBufferedImage(Image image) {
	    if (image instanceof BufferedImage) {
	        return (BufferedImage)image;
	    }

	    // This code ensures that all the pixels in the image are loaded
	    image = new ImageIcon(image).getImage();

	    // Determine if the image has transparent pixels; for this method's
	    // implementation, see Determining If an Image Has Transparent Pixels
	    boolean hasAlpha = hasAlpha(image);

	    // Create a buffered image with a format that's compatible with the screen
	    BufferedImage bimage = null;
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    try {
	        // Determine the type of transparency of the new buffered image
	        int transparency = Transparency.OPAQUE;
	        if (hasAlpha) {
	            transparency = Transparency.BITMASK;
	        }

	        // Create the buffered image
	        GraphicsDevice gs = ge.getDefaultScreenDevice();
	        GraphicsConfiguration gc = gs.getDefaultConfiguration();
	        bimage = gc.createCompatibleImage(
	            image.getWidth(null), image.getHeight(null), transparency);
	    } catch (HeadlessException e) {
	        // The system does not have a screen
	    }

	    if (bimage == null) {
	        // Create a buffered image using the default color model
	        int type = BufferedImage.TYPE_INT_RGB;
	        if (hasAlpha) {
	            type = BufferedImage.TYPE_INT_ARGB;
	        }
	        bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
	    }

	    // Copy image to buffered image
	    Graphics g = bimage.createGraphics();

	    // Paint the image onto the buffered image
	    g.drawImage(image, 0, 0, null);
	    g.dispose();

	    return bimage;
	}
	
	// This method returns true if the specified image has transparent pixels
	private static boolean hasAlpha(Image image) {
	    // If buffered image, the color model is readily available
	    if (image instanceof BufferedImage) {
	        BufferedImage bimage = (BufferedImage)image;
	        return bimage.getColorModel().hasAlpha();
	    }

	    // Use a pixel grabber to retrieve the image's color model;
	    // grabbing a single pixel is usually sufficient
	     PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
	    try {
	        pg.grabPixels();
	    } catch (InterruptedException e) {
	    }

	    // Get the image's color model
	    ColorModel cm = pg.getColorModel();
	    return cm.hasAlpha();
	}
	
	/**
	 * Copies a file from the source to the destination
	 * 
	 * @param source
	 *            the source file
	 * @param dest
	 *            the destination file
	 * @param overwrite
	 *            if true, the destination file will be overwritten if it
	 *            already exists
	 * @throws IOException
	 * @return true if the file has been copied; otherwise false
	 */
	public static boolean copyFile(File source, File dest, boolean overwrite) throws IOException {
		if (dest.exists()) {
			if(!overwrite) {
				return false;
			}
		} else {
			dest.createNewFile();
		}
		
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(dest);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
		log.debug(String.format("Copied file from %s to %s", source.getAbsolutePath(), dest.getAbsolutePath()));
		return true;
	}
	
	/**
	 * Copies a file from the source to the destination
	 * 
	 * @param source
	 *            the source file
	 * @param dest
	 *            the destination file
	 * @param overwrite
	 *            if true, the destination file will be overwritten if it
	 *            already exists
	 * @throws IOException
	 * @return true if the file has been copied; otherwise false
	 */
	public static void copyFile(String source, String dest, boolean overwrite) throws IOException {
		copyFile(new File(source), new File(dest), overwrite);
	}
}
