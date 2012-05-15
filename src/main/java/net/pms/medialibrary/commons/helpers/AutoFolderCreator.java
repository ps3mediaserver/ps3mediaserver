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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOCondition;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryBase;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFile;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFolder;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryInfo;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.dataobjects.DOTemplate;
import net.pms.medialibrary.commons.dataobjects.DOThumbnailPriority;
import net.pms.medialibrary.commons.dataobjects.FileDisplayProperties;
import net.pms.medialibrary.commons.enumarations.AutoFolderProperty;
import net.pms.medialibrary.commons.enumarations.ConditionOperator;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.ConditionUnit;
import net.pms.medialibrary.commons.enumarations.ConditionValueType;
import net.pms.medialibrary.commons.enumarations.FileDisplayMode;
import net.pms.medialibrary.commons.enumarations.FileDisplayType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.SortOption;
import net.pms.medialibrary.commons.enumarations.ThumbnailPrioType;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.commons.helpers.ConfigurationHelper;

public class AutoFolderCreator {
	private static final Logger log = LoggerFactory.getLogger(AutoFolderCreator.class);
	
	/**
	 * Creates the initial folder structure in the storage assuming nothing has been inserted before
	 * @param storage The storage, where the new folders will be inserted
	 */
	public static void addInitialFolderStructure(IMediaLibraryStorage storage) {
		if(log.isDebugEnabled()) log.debug("Start creating the initial tree folder structure");
		
		String ps = File.separator;
		String defaultImagePath = ConfigurationHelper.getApplicationRootPath() + "resources" + ps + "images" + ps + "mlx_ps3" + ps;
		
		List<DOThumbnailPriority> defaultFileThumbnailPriority = new ArrayList<DOThumbnailPriority>();
		defaultFileThumbnailPriority.add(new DOThumbnailPriority(-1, ThumbnailPrioType.THUMBNAIL, 30, 0));
		defaultFileThumbnailPriority.add(new DOThumbnailPriority(-1, ThumbnailPrioType.GENERATED, 30, 1));
		
		//Create Movie FileFolder
		DOTemplate movieFileTemplate = new DOTemplate(Messages.getString("ML.InitialFolders.Templates.Movie"), -1);
		DOFileEntryFolder movieFileFolder = new DOFileEntryFolder(new ArrayList<DOFileEntryBase>(), -1, null, 0, "%name (%year) - %rating_percent/100 [%rating_voters]", defaultFileThumbnailPriority, 0);
		
		DOFileEntryFolder fo1 = new DOFileEntryFolder(new ArrayList<DOFileEntryBase>(), -1, movieFileFolder, 0, Messages.getString("ML.InitialFolders.Templates.Movie.Transcode"), defaultFileThumbnailPriority, 0);
		fo1.addChild(new DOFileEntryFile(FileDisplayMode.MULTIPLE, -1, fo1, 0, "", defaultFileThumbnailPriority, 0));
		movieFileFolder.addChild(fo1);
		
		DOFileEntryFolder fo2 = new DOFileEntryFolder(new ArrayList<DOFileEntryBase>(), -1, movieFileFolder, 1, Messages.getString("ML.InitialFolders.Templates.Movie.Plot"), defaultFileThumbnailPriority, 0);
		List<DOThumbnailPriority> tps = Arrays.asList(new DOThumbnailPriority(-1, ThumbnailPrioType.PICTURE, defaultImagePath + "hypnotoad.png" , 0));
		DOFileEntryInfo fe4 = new DOFileEntryInfo(-1, fo2, 0, "%overview", tps, 56);
		fo2.addChild(fe4);	
		movieFileFolder.addChild(fo2);
		
		tps = Arrays.asList(new DOThumbnailPriority(-1, ThumbnailPrioType.PICTURE, defaultImagePath + "movie.png" , 0));
		DOFileEntryFile f1 = new DOFileEntryFile(FileDisplayMode.SINGLE, -1, movieFileFolder, 2, "%name (%year) - %rating_percent/100 [%rating_voters]", tps, 0);
		movieFileFolder.addChild(f1);
		
		DOFileEntryInfo fe1 = new DOFileEntryInfo(-1, movieFileFolder, 3, "%genres", defaultFileThumbnailPriority, 0);		
		tps = Arrays.asList(new DOThumbnailPriority(-1, ThumbnailPrioType.PICTURE, defaultImagePath + "info.png" , 0));
		DOFileEntryInfo fe2 = new DOFileEntryInfo(-1, movieFileFolder, 4, "%certification : %tagline", tps, 0);
		tps = Arrays.asList(new DOThumbnailPriority(-1, ThumbnailPrioType.PICTURE, defaultImagePath + "technical.png" , 0));
		DOFileEntryInfo fe3 = new DOFileEntryInfo(-1, movieFileFolder, 5, "%container - %video_codec [%widthx%height]", tps, 0);
		movieFileFolder.addChild(fe1);
		movieFileFolder.addChild(fe2);	
		movieFileFolder.addChild(fe3);
		
		storage.insertTemplate(movieFileTemplate, movieFileFolder);
				
		//Insert root node (it will get the id=1)
		DOMediaLibraryFolder rootFolder = new DOMediaLibraryFolder(-1, null, Messages.getString("ML.InitialFolders.Root"), "", new ArrayList<DOCondition>(), false, false, FileType.FILE, 0, new FileDisplayProperties("%file_name", false, FileDisplayType.FILE, false), false, false);
		rootFolder.getDisplayProperties().setThumbnailPriorities(defaultFileThumbnailPriority);
		rootFolder.getDisplayProperties().setDisplayNameMask("%file_name");
		
		//Create default display properties
		FileDisplayProperties childProps = new FileDisplayProperties(true, false);
		childProps.setDisplayNameMask("%name (%year) - %rating_percent/100 [%rating_voters]");
		childProps.setSortType(ConditionType.VIDEO_NAME);
		childProps.setSortAscending(true);
		
		//Create folders in root
		DOMediaLibraryFolder videoFolder = new DOMediaLibraryFolder(-1, rootFolder, Messages.getString("ML.InitialFolders.Root.Video"), "", new ArrayList<DOCondition>(), false, true, FileType.VIDEO, 0, childProps, false, false);
		videoFolder.getDisplayProperties().setThumbnailPriorities(defaultFileThumbnailPriority);
		
		childProps.setDisplayNameMask("???");
		DOMediaLibraryFolder audioFolder = new DOMediaLibraryFolder(-1, rootFolder, Messages.getString("ML.InitialFolders.Root.Audio"), "", new ArrayList<DOCondition>(), true, true, FileType.AUDIO, 1, childProps, false, false);
		audioFolder.getDisplayProperties().setThumbnailPriorities(defaultFileThumbnailPriority);
		
		childProps.setDisplayNameMask("!!!");
		DOMediaLibraryFolder imageFolder = new DOMediaLibraryFolder(-1, rootFolder, Messages.getString("ML.InitialFolders.Root.Pictures"), "", new ArrayList<DOCondition>(), true, true, FileType.PICTURES, 2, childProps, false, false);
		imageFolder.getDisplayProperties().setThumbnailPriorities(defaultFileThumbnailPriority);
		
		//Create folders in video
		createAllFolder(videoFolder, 0, childProps, movieFileTemplate);
		
		DOMediaLibraryFolder playedFolder = createAllFolder(videoFolder, 1, childProps, movieFileTemplate);
		playedFolder.setInheritSort(false);
		playedFolder.getDisplayProperties().setSortType(ConditionType.FILEPLAYS_DATEPLAYEND);
		playedFolder.getDisplayProperties().setSortAscending(false);
		playedFolder.setName(Messages.getString("ML.InitialFolders.Root.Video.Played"));
		playedFolder.setFilter(new DOFilter("c1", Arrays.asList(new DOCondition(ConditionType.FILE_PLAYCOUNT, ConditionOperator.IS_GREATER_THAN, "0", "c1", ConditionValueType.INTEGER, null, ""))));
		
		DOMediaLibraryFolder notPlayedFolder = createAllFolder(videoFolder, 2, childProps, movieFileTemplate);
		notPlayedFolder.setName(Messages.getString("ML.InitialFolders.Root.Video.NotPlayed"));
		notPlayedFolder.setFilter(new DOFilter("c1", Arrays.asList(new DOCondition(ConditionType.FILE_PLAYCOUNT, ConditionOperator.IS, "0", "c1", ConditionValueType.INTEGER, null, ""))));
		
		DOMediaLibraryFolder recentlyAddedFolder = new DOMediaLibraryFolder(-1, videoFolder, Messages.getString("ML.InitialFolders.Root.Video.RecentlyAdded"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 3, childProps, false, true);
		recentlyAddedFolder.getDisplayProperties().setSortType(ConditionType.FILE_DATEINSERTEDDB);
		recentlyAddedFolder.getDisplayProperties().setSortAscending(false);
		recentlyAddedFolder.setFilter(new DOFilter("c1", Arrays.asList(new DOCondition(ConditionType.FILE_DATEINSERTEDDB, ConditionOperator.IS_IN_THE_LAST_SEC, "15", "c1", ConditionValueType.INTEGER, ConditionUnit.TIMESPAN_DAYS, ""))));
		
		storage.insertFolder(rootFolder);

		addAtoZVideoFolders(storage, videoFolder, true, 4);

		if(log.isInfoEnabled()) log.info("The initial tree folder structure has been created");
	}
	
	private static DOMediaLibraryFolder createAllFolder(DOMediaLibraryFolder parent, int positionInParent, FileDisplayProperties childProps, DOTemplate movieFileTemplate){
		DOMediaLibraryFolder allFolder = new DOMediaLibraryFolder(-1, parent, Messages.getString("ML.InitialFolders.Root.Video.All"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, positionInParent, childProps, true, true);

		//Create Movie FileFolder display properties
		FileDisplayProperties childMovieProps = new FileDisplayProperties("%name (%year) - %rating_percent/100 [%rating_voters]", true, FileDisplayType.FOLDER, false, ConditionType.FILE_DATEINSERTEDDB, false, movieFileTemplate, SortOption.FileProperty);
		
		//Create folders in All
		
		DOMediaLibraryFolder resolutionFolder = new DOMediaLibraryFolder(-1, allFolder, Messages.getString("ML.InitialFolders.Root.Video.All.Resolution"), "", new ArrayList<DOCondition>(), false, true, FileType.VIDEO, 0, childProps, true, true);
		
		DOMediaLibraryFolder allSortedFolder = new DOMediaLibraryFolder(-1, allFolder, Messages.getString("ML.InitialFolders.Root.Video.All.Sorted"), "", new ArrayList<DOCondition>(), false, true, FileType.VIDEO, 1, childProps, true, true);

		//create detail folder
		new DOMediaLibraryFolder(-1, allFolder, Messages.getString("ML.InitialFolders.Detail"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 2, childMovieProps, true, false);
		
		//create SD folder
		ArrayList<DOCondition> sdConditions = new ArrayList<DOCondition>();
		sdConditions.add(new DOCondition(ConditionType.VIDEO_WIDTH, ConditionOperator.IS_LESS_THAN, "1280", "c1", ConditionValueType.INTEGER, ConditionUnit.UNKNOWN, ""));
		sdConditions.add(new DOCondition(ConditionType.VIDEO_HEIGHT, ConditionOperator.IS_LESS_THAN, "720", "c2", ConditionValueType.INTEGER, ConditionUnit.UNKNOWN, ""));
		DOMediaLibraryFolder allSdFolder = new DOMediaLibraryFolder(-1, resolutionFolder, Messages.getString("ML.InitialFolders.Root.Video.All.Resolution.SD"), "c1 AND c2", sdConditions, true, true, FileType.VIDEO, 0, childProps, true, true);
		//create detail folder
		new DOMediaLibraryFolder(-1, allSdFolder, Messages.getString("ML.InitialFolders.Detail"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 0, childMovieProps, true, false);

		//create HD folder
		ArrayList<DOCondition> hdConditions = new ArrayList<DOCondition>();
		hdConditions.add(new DOCondition(ConditionType.VIDEO_WIDTH, ConditionOperator.IS_GREATER_THAN, "1279", "c1", ConditionValueType.INTEGER, ConditionUnit.UNKNOWN, ""));
		hdConditions.add(new DOCondition(ConditionType.VIDEO_HEIGHT, ConditionOperator.IS_GREATER_THAN, "719", "c2", ConditionValueType.INTEGER, ConditionUnit.UNKNOWN, ""));
		DOMediaLibraryFolder allHdFolder = new DOMediaLibraryFolder(-1, resolutionFolder, Messages.getString("ML.InitialFolders.Root.Video.All.Resolution.HD"), "c1 OR c2", hdConditions, true, true, FileType.VIDEO, 1, childProps, true, true);
		//create detail folder
		new DOMediaLibraryFolder(-1, allHdFolder, Messages.getString("ML.InitialFolders.Detail"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 0, childMovieProps, true, false);
		
		//create 720 folder
		ArrayList<DOCondition> hdReadyConditions = new ArrayList<DOCondition>();
		hdReadyConditions.add(new DOCondition(ConditionType.VIDEO_WIDTH, ConditionOperator.IS_LESS_THAN, "1281", "c1", ConditionValueType.INTEGER, ConditionUnit.UNKNOWN, ""));
		hdReadyConditions.add(new DOCondition(ConditionType.VIDEO_HEIGHT, ConditionOperator.IS_LESS_THAN, "721", "c2", ConditionValueType.INTEGER, ConditionUnit.UNKNOWN, ""));
		DOMediaLibraryFolder allHdReadyFolder = new DOMediaLibraryFolder(-1, allHdFolder, Messages.getString("ML.InitialFolders.Root.Video.All.Resolution.HD.720"), "c1 OR c2", hdReadyConditions, true, true, FileType.VIDEO, 0, childProps, true, true);
		//create detail folder
		new DOMediaLibraryFolder(-1, allHdReadyFolder, Messages.getString("ML.InitialFolders.Detail"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 0, childMovieProps, true, false);

		//create 1080 folder
		ArrayList<DOCondition> fullHdConditions = new ArrayList<DOCondition>();
		fullHdConditions.add(new DOCondition(ConditionType.VIDEO_WIDTH, ConditionOperator.IS_GREATER_THAN, "1919", "c1", ConditionValueType.INTEGER, ConditionUnit.UNKNOWN, ""));
		fullHdConditions.add(new DOCondition(ConditionType.VIDEO_HEIGHT, ConditionOperator.IS_GREATER_THAN, "1079", "c2", ConditionValueType.INTEGER, ConditionUnit.UNKNOWN, ""));
		DOMediaLibraryFolder allHdFullFolder = new DOMediaLibraryFolder(-1, allHdFolder, Messages.getString("ML.InitialFolders.Root.Video.All.Resolution.HD.1080"), "c1 OR c2", fullHdConditions, true, true, FileType.VIDEO, 1, childProps, true, true);
		//create detail folder
		new DOMediaLibraryFolder(-1, allHdFullFolder, Messages.getString("ML.InitialFolders.Detail"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 0, childMovieProps, true, false);
		
		//Create folders in Sorted		
		childProps.setSortAscending(false);
		childProps.setSortType(ConditionType.VIDEO_YEAR);
		childProps.setDisplayNameMask("%name (%year) - %rating_percent/100 [%rating_voters]");
		DOMediaLibraryFolder allByYearFolder = new DOMediaLibraryFolder(-1, allSortedFolder, Messages.getString("ML.InitialFolders.Root.Video.All.Sorted.Year"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 0, childProps, false, true);
		//create detail folder
		new DOMediaLibraryFolder(-1, allByYearFolder, Messages.getString("ML.InitialFolders.Detail"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 0, childMovieProps, true, false);

		childProps.setSortType(ConditionType.VIDEO_DURATIONSEC);
		DOMediaLibraryFolder allByDurationFolder = new DOMediaLibraryFolder(-1, allSortedFolder, Messages.getString("ML.InitialFolders.Root.Video.All.Sorted.Duration"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 1, childProps, false, true);
		//create detail folder
		new DOMediaLibraryFolder(-1, allByDurationFolder, Messages.getString("ML.InitialFolders.Detail"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 0, childMovieProps, true, false);

		childProps.setSortType(ConditionType.VIDEO_RATINGPERCENT);
		DOMediaLibraryFolder allByRatingFolder = new DOMediaLibraryFolder(-1, allSortedFolder, Messages.getString("ML.InitialFolders.Root.Video.All.Sorted.Rating"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 2, childProps, false, true);
		//create detail folder
		new DOMediaLibraryFolder(-1, allByRatingFolder, Messages.getString("ML.InitialFolders.Detail"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 0, childMovieProps, true, false);

		childProps.setSortType(ConditionType.FILE_DATEINSERTEDDB);
		DOMediaLibraryFolder allByDateAddedFolder = new DOMediaLibraryFolder(-1, allSortedFolder, Messages.getString("ML.InitialFolders.Root.Video.All.Sorted.Added"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 3, childProps, false, true);
		//create detail folder
		new DOMediaLibraryFolder(-1, allByDateAddedFolder, Messages.getString("ML.InitialFolders.Detail"), "", new ArrayList<DOCondition>(), true, true, FileType.VIDEO, 0, childMovieProps, true, false);
		
		return allFolder;
	}

	/**
	 * Adds the folder and all its child folders in the file system hierarchy to the tree
	 * @param storage The storage, where the new folders will be inserted
	 * @param parent The parent folder of the new node that will be inserted 
	 * @param insertPosition The position in which to insert the new folder
	 * @param baseFolder The root folder that will be inserted as well as all its children
	 */
	public static void addFileSystemFolders(IMediaLibraryStorage storage, DOMediaLibraryFolder parent, int insertPosition, File baseFolder) {
		if(log.isDebugEnabled()) log.debug("Insert file system folders for folder '" + baseFolder.getAbsolutePath() + "' into parent " + parent.getName() + " (id=" + parent.getId() + ")");
		
		//Insert base folder
		if(baseFolder.exists() && baseFolder.isDirectory() && (!baseFolder.isHidden() || baseFolder.getAbsolutePath().endsWith(":\\"))){
			String conStr = baseFolder.getAbsolutePath() + File.separator;
			DOCondition con = new DOCondition(ConditionType.FILE_FOLDERPATH, ConditionOperator.IS, conStr, "c1", ConditionValueType.STRING, ConditionUnit.UNKNOWN, "");
			List<DOCondition> cons = new ArrayList<DOCondition>();
			cons.add(con);
			
			DOMediaLibraryFolder newFolder = new DOMediaLibraryFolder(-1, parent, baseFolder.getAbsolutePath().endsWith(":\\") ? baseFolder.getAbsolutePath() : baseFolder.getName(), "c1", cons, true, false, parent.getFileType(), insertPosition, true, true);
			storage.insertFolder(newFolder);
			if(log.isInfoEnabled()) log.info("File system folders inserted for folder '" + baseFolder.getAbsolutePath() + "' into parent " + parent.getName() + " (id=" + parent.getId() + ")");

			int childInsertPos = 0;
			for(File f:baseFolder.listFiles()){
				if(f.isDirectory()){
					addFileSystemFolders(storage, newFolder, childInsertPos++, f);
				}
			}
		}
	}
	
	/**
	 * Adds a new folder for each char in A-Z to the node passed as a parameter
	 * @param parent 
	 * 
	 * @param storage The storage, where the new folders will be inserted
	 * @param parent The parent folder of the new node that will be inserted 
	 * @param isAscending If true, the list is sorted ascending, descending otherwise
	 * @param insertPos 
	 * @param insertPosition The position in which to insert the new folder
	 */	
	public static void addAtoZVideoFolders(IMediaLibraryStorage storage, DOMediaLibraryFolder parent, boolean isAscending, int positionInParent){
		if(log.isDebugEnabled()) log.debug("Get A-Z folders");
		
		//Create the base node
		List<DOCondition> cons = new ArrayList<DOCondition>();
		FileDisplayProperties displayProps = new FileDisplayProperties(false, true);
		displayProps.setFileDisplayType(FileDisplayType.FILE);
		DOMediaLibraryFolder newFolder = new DOMediaLibraryFolder(-1, parent, "A-Z", "", new ArrayList<DOCondition>(), false, true, FileType.VIDEO, positionInParent, true, true);
		
		//Add the number folder with an OR on all entries starting with 1-9
		String equation = "";
		int pos = 1;
		String suffix = " AND ";
		for(int i = (int)'A'; i <= (int)'Z'; i++){
			String conName = "c" + pos++;
			DOCondition con = new DOCondition(ConditionType.VIDEO_NAME, ConditionOperator.DOES_NOT_START_WITH, String.valueOf((char)i), conName, ConditionValueType.STRING, ConditionUnit.UNKNOWN, "");
			cons.add(con);
			equation += conName + suffix;
		}
		if(equation != null && equation.endsWith(suffix)){
			equation = equation.substring(0, equation.length() - suffix.length()).trim();
		}
		int insertPos = 0;
		DOMediaLibraryFolder numbersFolder = new DOMediaLibraryFolder(-1, newFolder, "#", equation, cons, true, true, newFolder.getFileType(), 0, new FileDisplayProperties(), true, true);;
		
		//Add A-Z folders (ascending or descending)
		if(isAscending){
			numbersFolder.setPositionInParent(insertPos++);
			for(int i = (int)'A'; i <= (int)'Z'; i++){
				String conStr = String.valueOf((char)i);
				DOCondition con = new DOCondition(ConditionType.VIDEO_NAME, ConditionOperator.STARTS_WITH, conStr, "c1", ConditionValueType.STRING, ConditionUnit.UNKNOWN, "");
				cons = new ArrayList<DOCondition>();
				cons.add(con);
				new DOMediaLibraryFolder(-1, newFolder, conStr, "c1", cons, true, true, newFolder.getFileType(), insertPos++, true, true);
			}
		}else{
			for(int i = (int)'Z'; i >= (int)'A'; i--){
				String conStr = String.valueOf((char)i);
				DOCondition con = new DOCondition(ConditionType.VIDEO_NAME, ConditionOperator.STARTS_WITH, conStr, "c1", ConditionValueType.STRING, ConditionUnit.UNKNOWN, "");
				cons = new ArrayList<DOCondition>();
				cons.add(con);
				new DOMediaLibraryFolder(-1, newFolder, conStr, "c1", cons, true, true, newFolder.getFileType(), insertPos++, true, true);
			}
			numbersFolder.setPositionInParent(insertPos++);
		}
		
		parent.getChildFolders().add(newFolder);
		storage.insertFolder(newFolder);
	}

	public static void addTypeFolder(IMediaLibraryStorage storage, DOMediaLibraryFolder parent, AutoFolderProperty prop, boolean isAscending, int insertPos, int minOccurences) {		
		if(log.isDebugEnabled()) log.debug("Insert folder of type " + prop.toString() + ", isAscending= " + isAscending + " into parent " + parent.getName() + " (id=" + parent.getId() + ")");
		
		ConditionType conditionType = ConditionType.UNKNOWN;
		ConditionValueType conditionValueType = ConditionValueType.UNKNOWN;
		ConditionUnit conditionUnit = ConditionUnit.UNKNOWN;
		
		switch(prop){
			case CERTIFICATION:
				conditionType = ConditionType.VIDEO_CERTIFICATION;
				conditionValueType = ConditionValueType.STRING;
				break;
			case AUDIO_LANGUAGE:
				conditionType = ConditionType.VIDEO_CONTAINS_VIDEOAUDIO;
				conditionValueType = ConditionValueType.STRING;
				break;
			case DIRECTOR:
				conditionType = ConditionType.VIDEO_DIRECTOR;
				conditionValueType = ConditionValueType.STRING;
				break;
			case GENRE:
				conditionType = ConditionType.VIDEO_CONTAINS_GENRE;
				conditionValueType = ConditionValueType.STRING;
				break;
			case SUBTITLE_LANGUAGE:
				conditionType = ConditionType.VIDEO_CONTAINS_SUBTITLES;
				conditionValueType = ConditionValueType.STRING;
				break;
			case YEAR:
				conditionType = ConditionType.VIDEO_YEAR;
				conditionValueType = ConditionValueType.INTEGER;
				break;
			default:
				return;
		}
		
		//Create the base node
		FileDisplayProperties displayProps = new FileDisplayProperties(false, true);
		DOMediaLibraryFolder newRootFolder = new DOMediaLibraryFolder(-1, parent, Messages.getString("ML.Condition.Header.Type." + conditionType.toString()),
				"", new ArrayList<DOCondition>(), false, true, parent.getFileType(), insertPos, displayProps, true, true);
				
		//Get the node names to add sorted from the db
		List<String> items = storage.getVideoProperties(conditionType, isAscending, minOccurences);
		
		//Add nodes
		int insertPosition = 0;
		for(String conStr : items){
			DOCondition con = new DOCondition(conditionType, ConditionOperator.IS, conStr, "c1", conditionValueType, conditionUnit, "");
			List<DOCondition> cons = new ArrayList<DOCondition>();
			cons.add(con);
			
			String displayName = conStr;
			if(displayName.equals("")){
				displayName = Messages.getString("ML.Condition.NoDisplayName");
			}
			
			//create new child folder. It will be automatically added to newRootFolder
			//by setting it as the parent
			new DOMediaLibraryFolder(-1, newRootFolder, displayName, "c1", cons, true, true, 
					newRootFolder.getFileType(), insertPosition++, displayProps, true, true);
		}
		storage.insertFolder(newRootFolder);
		if(log.isInfoEnabled()) log.info("Folder of type " + prop.toString() + ", isAscending= " + isAscending + " inserted into parent " + parent.getName() + " (id=" + parent.getId() + ")");	    
    }

	public static void addTagFolder(IMediaLibraryStorage storage, DOMediaLibraryFolder parent, String tagName, boolean isAscending, int insertPos, int minOccurences) {		
		if(log.isDebugEnabled()) log.debug("Insert folder for tag=" + tagName + ", isAscending= " + isAscending + " into parent " + parent.getName() + " (id=" + parent.getId() + ")");
		
		//Get the node names to add sorted from the db
		List<String> tagValues = storage.getTagValues(tagName, isAscending, minOccurences);
		
		//Create the base node
		FileDisplayProperties displayProps = new FileDisplayProperties(false, true);
		DOMediaLibraryFolder newRootFolder = new DOMediaLibraryFolder(-1, parent, tagName,
				"", new ArrayList<DOCondition>(), false, true, parent.getFileType(), insertPos, displayProps, true, true);
		
		//Add nodes
		int insertPosition = 0;
		for(String tagValue : tagValues){
			DOCondition con = new DOCondition(ConditionType.FILE_CONTAINS_TAG, ConditionOperator.IS, tagValue, "c1", ConditionValueType.STRING, ConditionUnit.UNKNOWN, tagName);
			List<DOCondition> cons = new ArrayList<DOCondition>();
			cons.add(con);

			//create new child folder. It will be automatically added to newRootFolder
			//by setting it as the parents
			new DOMediaLibraryFolder(-1, newRootFolder, tagValue, "c1", cons, true, true, 
					newRootFolder.getFileType(), insertPosition++, displayProps, true, true);
		}
		storage.insertFolder(newRootFolder);
		if(log.isInfoEnabled()) log.info("Folder for tag=" + tagName + ", isAscending= " + isAscending + " inserted into parent " + parent.getName() + " (id=" + parent.getId() + ")");	    
    }
}
