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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionOperatorCBItem;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionTypeCBItem;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionUnitCBItem;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.FileTypeCBItem;
import net.pms.medialibrary.commons.enumarations.ConditionOperator;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.ConditionUnit;
import net.pms.medialibrary.commons.enumarations.ConditionValueType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class FolderHelper {	
	private static final Logger log = LoggerFactory.getLogger(FolderHelper.class);
	public HashMap<ConditionType, ConditionOperatorCBItem[]> typeOperators;
	private static FolderHelper staticHelper;
	
	public FolderHelper(){
		init();
	}
	
	public static FolderHelper getHelper(){
		if(staticHelper == null){
			staticHelper = new FolderHelper();
		}
		return staticHelper;
	}
	
	private void init() {
		this.typeOperators=new HashMap<ConditionType, ConditionOperatorCBItem[]>();

		for(ConditionType ct:ConditionType.values()){
			ConditionOperator[] operators = getConditionOperatorForType(ct);
			ConditionOperatorCBItem[] op = new ConditionOperatorCBItem[operators.length];
			int i = 0;
			for(ConditionOperator co : operators){
				try{
					String name;
					if(ct.toString().contains("_CONTAINS_")){
						//use a different localization string for these conditions
						name = Messages.getString("ML.Condition.Operator.Contains." + co.toString());						
					} else {
						name = Messages.getString("ML.Condition.Operator." + co.toString());						
					}
					op[i++] = new ConditionOperatorCBItem(co, name);
				}catch(Exception ex){
					log.error("Failed to initialize FolderHelper", ex);
				}
			}
			this.typeOperators.put(ct,  op);
		}
		
	}
	
	private ConditionOperator[] getConditionOperatorForType(ConditionType conditionType){
		ConditionOperator[] retVal = new ConditionOperator[0];
		
		if(conditionType == ConditionType.FILE_TYPE
				|| conditionType == ConditionType.VIDEO_MUXABLE){
			retVal = new ConditionOperator[2];
			retVal[0] = ConditionOperator.IS;
			retVal[1] = ConditionOperator.IS_NOT;
		} else if(conditionType == ConditionType.FILE_FILENAME
				|| conditionType == ConditionType.FILE_FOLDERPATH
				|| conditionType == ConditionType.FILE_THUMBNAILPATH
				|| conditionType == ConditionType.VIDEO_ORIGINALNAME
				|| conditionType == ConditionType.VIDEO_IMDBID
				|| conditionType == ConditionType.VIDEO_HOMEPAGEURL
				|| conditionType == ConditionType.VIDEO_TRAILERURL
				|| conditionType == ConditionType.VIDEO_CERTIFICATION
				|| conditionType == ConditionType.VIDEO_CERTIFICATIONREASON
				|| conditionType == ConditionType.VIDEO_ASPECTRATIO
				|| conditionType == ConditionType.VIDEO_BITRATE
				|| conditionType == ConditionType.VIDEO_DIRECTOR
				|| conditionType == ConditionType.VIDEO_FRAMERATE
				|| conditionType == ConditionType.VIDEO_NAME
				|| conditionType == ConditionType.VIDEO_SORTNAME
				|| conditionType == ConditionType.VIDEO_CODECV
				|| conditionType == ConditionType.VIDEO_MODEL
				|| conditionType == ConditionType.VIDEO_CONTAINER
				|| conditionType == ConditionType.VIDEO_MIMETYPE
				|| conditionType == ConditionType.VIDEO_OVERVIEW
				|| conditionType == ConditionType.VIDEO_TAGLINE
				|| conditionType == ConditionType.VIDEO_MUXINGMODE
				|| conditionType == ConditionType.AUDIO_LANG
				|| conditionType == ConditionType.AUDIO_NRAUDIOCHANNELS
				|| conditionType == ConditionType.AUDIO_CODECA
				|| conditionType == ConditionType.AUDIO_ALBUM
				|| conditionType == ConditionType.AUDIO_ARTIST
				|| conditionType == ConditionType.AUDIO_SONGNAME
				|| conditionType == ConditionType.AUDIO_GENRE
				|| conditionType == ConditionType.AUDIO_SAMPLEFREQ){
			retVal = new ConditionOperator[8];
			retVal[0] = ConditionOperator.IS;
			retVal[1] = ConditionOperator.IS_NOT;
			retVal[2] = ConditionOperator.STARTS_WITH;
			retVal[3] = ConditionOperator.DOES_NOT_START_WITH;
			retVal[4] = ConditionOperator.ENDS_WITH;
			retVal[5] = ConditionOperator.DOES_NOT_END_WITH;
			retVal[6] = ConditionOperator.CONTAINS;
			retVal[7] = ConditionOperator.DOES_NOT_CONTAIN;
		} else if(conditionType == ConditionType.FILE_DATELASTUPDATEDDB
				|| conditionType == ConditionType.FILE_DATEINSERTEDDB
				|| conditionType == ConditionType.FILE_DATEMODIFIEDOS
				|| conditionType == ConditionType.FILEPLAYS_DATEPLAYEND){
			retVal = new ConditionOperator[4];
			retVal[0] = ConditionOperator.IS_AFTER;
			retVal[1] = ConditionOperator.IS_BEFORE;
			retVal[2] = ConditionOperator.IS_IN_THE_LAST_SEC;
			retVal[3] = ConditionOperator.IS_NOT_IN_THE_LAST_SEC;
		}else if(conditionType == ConditionType.FILE_SIZEBYTE
				|| conditionType == ConditionType.FILE_PLAYCOUNT
				|| conditionType == ConditionType.VIDEO_BUDGET
				|| conditionType == ConditionType.VIDEO_REVENUE
				|| conditionType == ConditionType.VIDEO_TMDBID
				|| conditionType == ConditionType.VIDEO_RATINGPERCENT
				|| conditionType == ConditionType.VIDEO_RATINGVOTERS
				|| conditionType == ConditionType.VIDEO_BITSPERPIXEL
				|| conditionType == ConditionType.VIDEO_DURATIONSEC
				|| conditionType == ConditionType.VIDEO_HEIGHT
				|| conditionType == ConditionType.VIDEO_WIDTH
				|| conditionType == ConditionType.VIDEO_YEAR
				|| conditionType == ConditionType.VIDEO_DVDTRACK
				|| conditionType == ConditionType.IMAGE_WIDTH
				|| conditionType == ConditionType.IMAGE_HEIGHT
				|| conditionType == ConditionType.AUDIO_BITSPERSAMPLE
				|| conditionType == ConditionType.AUDIO_YEAR
				|| conditionType == ConditionType.AUDIO_TRACK
				|| conditionType == ConditionType.AUDIO_DELAYMS
				|| conditionType == ConditionType.AUDIO_DURATION_SEC){
			retVal = new ConditionOperator[4];
			retVal[0] = ConditionOperator.IS;
			retVal[1] = ConditionOperator.IS_NOT;
			retVal[2] = ConditionOperator.IS_GREATER_THAN;
			retVal[3] = ConditionOperator.IS_LESS_THAN;
		}else if(conditionType == ConditionType.FILE_CONTAINS_TAG
				|| conditionType == ConditionType.VIDEO_CONTAINS_GENRE
				|| conditionType == ConditionType.VIDEO_CONTAINS_SUBTITLES
				|| conditionType == ConditionType.VIDEO_CONTAINS_VIDEOAUDIO) {
			retVal = new ConditionOperator[4];
			retVal[0] = ConditionOperator.IS;
			retVal[1] = ConditionOperator.CONTAINS;
			retVal[2] = ConditionOperator.STARTS_WITH;
			retVal[3] = ConditionOperator.ENDS_WITH;
		}
		
		return retVal;
	}
	
	public ConditionOperatorCBItem[] getConditionOperators(ConditionType conditionType){
		return this.typeOperators.get(conditionType);
	}

	public ConditionOperatorCBItem getConditionOperatorCBItem(ConditionOperator conditionOperator) {
		String name = Messages.getString("ML.Condition.Operator." + conditionOperator.toString());
		return new ConditionOperatorCBItem(conditionOperator, name);
	}

	public ConditionTypeCBItem getConditionTypeCBItem(ConditionType conditionType) {
		String name = Messages.getString("ML.Condition.Type." + conditionType.toString());
		return new ConditionTypeCBItem(conditionType, name);
	}

	public ConditionTypeCBItem getMaskCBItem(ConditionType conditionType) {
		return new ConditionTypeCBItem(conditionType, Messages.getString("ML.Condition.Header.Type." + conditionType));
	}

	public ConditionUnitCBItem getConditionUnitCBItem(ConditionUnit unit) {
	    return new ConditionUnitCBItem(unit, Messages.getString("ML.Condition.Unit." + unit));
    }

	public FileTypeCBItem[] getAllFileTypeCBItems() {
		FileTypeCBItem[] retVal = new FileTypeCBItem[4];
		retVal[0] = getFileTypeCBItem(FileType.VIDEO);
		retVal[1] = getFileTypeCBItem(FileType.AUDIO);
		retVal[2] = getFileTypeCBItem(FileType.PICTURES);
		retVal[3] = getFileTypeCBItem(FileType.FILE);	
		return retVal;
	}

	public FileTypeCBItem getFileTypeCBItem(FileType fileType) {
		return new FileTypeCBItem(fileType, Messages.getString("ML.FileType." + fileType));
	}
	
	public String getDisplayNameMaskSubstitute(ConditionType conditionType){
		switch(conditionType){
			case AUDIO_ALBUM: return "%album";
			case AUDIO_ARTIST: return "%artist";
			case AUDIO_BITSPERSAMPLE: return "%bits_per_sample";
			case AUDIO_CODECA: return "%audio_codec";
			case AUDIO_COVERPATH: return "%cover_path";
			case AUDIO_DELAYMS: return "%delay_ms";
			case AUDIO_DURATION_SEC: return "%duration_sec";
			case AUDIO_GENRE: return "%genre";
			case AUDIO_LANG: return "%lang";
			case AUDIO_NRAUDIOCHANNELS: return "%nr_audio_channels";
			case AUDIO_SAMPLEFREQ: return "%sample_freq";
			case AUDIO_SONGNAME: return "%song_name";
			case AUDIO_TRACK: return "%track_nr";
			case AUDIO_YEAR: return "%year";
			case FILE_CONTAINS_TAG: return "%tag_<tag_name>";
			case FILE_DATEINSERTEDDB: return "%date_inserted_db";
			case FILE_DATELASTUPDATEDDB: return "%date_last_updated_db";
			case FILE_DATEMODIFIEDOS: return "%date_last_modified_os";
			case FILE_FILENAME: return "%file_name";
			case FILE_FOLDERPATH: return "%folder_path";
			case FILE_PLAYCOUNT: return "%play_count";
			case FILE_SIZEBYTE: return "%file_size";
			case FILE_TYPE: return "%type";
			case FILEPLAYS_DATEPLAYEND: return "%date_last_played";
			case FILE_THUMBNAILPATH: return "%cover_path";
			case IMAGE_HEIGHT: return "%height";
			case IMAGE_WIDTH: return "%width";
			case UNKNOWN: return "%unknown";
			case VIDEO_ORIGINALNAME: return "%original_name";
			case VIDEO_TMDBID: return "%tmdb_id";
			case VIDEO_IMDBID: return "%imdb_id";
			case VIDEO_BUDGET: return "%budget";
			case VIDEO_REVENUE: return "%revenue";
			case VIDEO_HOMEPAGEURL: return "%homepage_url";
			case VIDEO_TRAILERURL: return "%trailer_url";
			case VIDEO_CERTIFICATION: return "%certification";
			case VIDEO_CERTIFICATIONREASON: return "%certification_reason";
			case VIDEO_ASPECTRATIO: return "%aspect_ratio";
			case VIDEO_BITRATE: return "%bitrate";
			case VIDEO_BITSPERPIXEL: return "%bits_per_pixel";
			case VIDEO_CODECV: return "%video_codec";
			case VIDEO_CONTAINER: return "%container";
			case VIDEO_CONTAINS_GENRE: return "%genres";
			case VIDEO_CONTAINS_SUBTITLES: return "%subtitle_languages";
			case VIDEO_CONTAINS_VIDEOAUDIO: return "%audio_languages";
			case VIDEO_DIRECTOR: return "%director";
			case VIDEO_DURATIONSEC: return "%duration";
			case VIDEO_DVDTRACK: return "%dvd_track";
			case VIDEO_FRAMERATE: return "%frame_rate";
			case VIDEO_HEIGHT: return "%height";
			case VIDEO_MIMETYPE: return "%mime_type";
			case VIDEO_MODEL: return "%model";
			case VIDEO_MUXABLE: return "%muxable";
			case VIDEO_OVERVIEW: return "%plot";
			case VIDEO_RATINGPERCENT: return "%rating_percent";
			case VIDEO_RATINGVOTERS: return "%rating_voters";
			case VIDEO_TAGLINE: return "%tagline";
			case VIDEO_NAME: return "%name";
			case VIDEO_SORTNAME: return "%sort_name";
			case VIDEO_WIDTH: return "%width";
			case VIDEO_YEAR: return "%year";
			case VIDEO_MUXINGMODE: return "%muxing_mode";
			default: return ""; 
		}
	}
	
	public ConditionTypeCBItem[] getMaskConditionTypes(List<FileType> fileTypes) {
		List<ConditionTypeCBItem> items = new ArrayList<ConditionTypeCBItem>();
		List<FileType> handledTypes = new ArrayList<FileType>();
		for (FileType ft : fileTypes) {
			if (!handledTypes.contains(ft)) {
				switch (ft) {
					case VIDEO:					
						items.add(getMaskCBItem(ConditionType.FILE_FILENAME));
						items.add(getMaskCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getMaskCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getMaskCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getMaskCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getMaskCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getMaskCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getMaskCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						items.add(getMaskCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getMaskCBItem(ConditionType.VIDEO_ORIGINALNAME));
						items.add(getMaskCBItem(ConditionType.VIDEO_NAME));
						items.add(getMaskCBItem(ConditionType.VIDEO_SORTNAME));
						items.add(getMaskCBItem(ConditionType.VIDEO_TMDBID));
						items.add(getMaskCBItem(ConditionType.VIDEO_IMDBID));
						items.add(getMaskCBItem(ConditionType.VIDEO_TAGLINE));
						items.add(getMaskCBItem(ConditionType.VIDEO_OVERVIEW));
						items.add(getMaskCBItem(ConditionType.VIDEO_BUDGET));
						items.add(getMaskCBItem(ConditionType.VIDEO_REVENUE));
						items.add(getMaskCBItem(ConditionType.VIDEO_DIRECTOR));
						items.add(getMaskCBItem(ConditionType.VIDEO_YEAR));
						items.add(getMaskCBItem(ConditionType.VIDEO_DURATIONSEC));
						items.add(getMaskCBItem(ConditionType.VIDEO_WIDTH));
						items.add(getMaskCBItem(ConditionType.VIDEO_HEIGHT));
						items.add(getMaskCBItem(ConditionType.VIDEO_HOMEPAGEURL));
						items.add(getMaskCBItem(ConditionType.VIDEO_TRAILERURL));
						items.add(getMaskCBItem(ConditionType.VIDEO_CERTIFICATION));
						items.add(getMaskCBItem(ConditionType.VIDEO_CERTIFICATIONREASON));
						items.add(getMaskCBItem(ConditionType.VIDEO_RATINGPERCENT));
						items.add(getMaskCBItem(ConditionType.VIDEO_RATINGVOTERS));
						items.add(getMaskCBItem(ConditionType.VIDEO_CODECV));
						items.add(getMaskCBItem(ConditionType.VIDEO_MIMETYPE));
						items.add(getMaskCBItem(ConditionType.VIDEO_ASPECTRATIO));
						items.add(getMaskCBItem(ConditionType.VIDEO_DVDTRACK));
						items.add(getMaskCBItem(ConditionType.VIDEO_FRAMERATE));
						items.add(getMaskCBItem(ConditionType.VIDEO_BITRATE));
						items.add(getMaskCBItem(ConditionType.VIDEO_CONTAINER));
						items.add(getMaskCBItem(ConditionType.VIDEO_CONTAINS_GENRE));
						items.add(getMaskCBItem(ConditionType.VIDEO_CONTAINS_VIDEOAUDIO));
						items.add(getMaskCBItem(ConditionType.VIDEO_CONTAINS_SUBTITLES));
						items.add(getMaskCBItem(ConditionType.VIDEO_MUXINGMODE));
						break;
					case AUDIO:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_ALBUM));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_ARTIST));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_CODECA));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_DURATION_SEC));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_GENRE));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_NRAUDIOCHANNELS));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_SAMPLEFREQ));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_SONGNAME));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_TRACK));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_YEAR));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_MUXINGMODE));
						break;
					case PICTURES:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.IMAGE_HEIGHT));
						items.add(getConditionTypeCBItem(ConditionType.IMAGE_WIDTH));
						break;
					case FILE:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						break;
				}
				handledTypes.add(ft);
			}
		}
		Collections.sort(items);
		return items.toArray(new ConditionTypeCBItem[0]);
	}
	
	public ConditionTypeCBItem[] getSortByConditionTypes(List<FileType> fileTypes) {
		List<ConditionTypeCBItem> items = new ArrayList<ConditionTypeCBItem>();
		List<FileType> handledTypes = new ArrayList<FileType>();
		for (FileType ft : fileTypes) {
			if (!handledTypes.contains(ft)) {
				switch (ft) {
					case VIDEO:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						//items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_NAME));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_SORTNAME));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_ORIGINALNAME));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_BUDGET));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_REVENUE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_TAGLINE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_OVERVIEW));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_DIRECTOR));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_DURATIONSEC));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_YEAR));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_WIDTH));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_HEIGHT));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CERTIFICATION));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CERTIFICATIONREASON));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_RATINGPERCENT));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_RATINGVOTERS));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CODECV));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_MIMETYPE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_BITRATE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CONTAINER));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_MUXINGMODE));
//						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CONTAINS_VIDEOAUDIO));
//						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CONTAINS_SUBTITLES));
//						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CONTAINS_GENRE));
						break;
					case AUDIO:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						//items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_ALBUM));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_ARTIST));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_CODECA));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_DURATION_SEC));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_GENRE));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_NRAUDIOCHANNELS));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_SAMPLEFREQ));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_SONGNAME));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_TRACK));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_YEAR));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_MUXINGMODE));
						break;
					case PICTURES:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						//items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.IMAGE_HEIGHT));
						items.add(getConditionTypeCBItem(ConditionType.IMAGE_WIDTH));
						break;
					case FILE:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						items.add(getConditionTypeCBItem(ConditionType.FILE_ISACTIF));
						//items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						break;
				}
				handledTypes.add(ft);
			}
		}
		Collections.sort(items);
		return items.toArray(new ConditionTypeCBItem[0]);
	}
	
	public ConditionTypeCBItem[] getFilteringConditionTypes(List<FileType> fileTypes) {
		List<ConditionTypeCBItem> items = new ArrayList<ConditionTypeCBItem>();
		List<FileType> handledTypes = new ArrayList<FileType>();
		for (FileType ft : fileTypes) {
			if (!handledTypes.contains(ft)) {
				switch (ft) {
					case VIDEO:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						items.add(getConditionTypeCBItem(ConditionType.FILE_ISACTIF));
						items.add(getConditionTypeCBItem(ConditionType.FILE_THUMBNAILPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_ORIGINALNAME));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_NAME));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_SORTNAME));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_TMDBID));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_IMDBID));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_TAGLINE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_OVERVIEW));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_BUDGET));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_REVENUE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_DIRECTOR));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_YEAR));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_DURATIONSEC));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_WIDTH));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_HEIGHT));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_HOMEPAGEURL));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_TRAILERURL));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CERTIFICATION));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CERTIFICATIONREASON));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_RATINGPERCENT));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_RATINGVOTERS));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CODECV));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_MIMETYPE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_ASPECTRATIO));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_BITRATE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CONTAINER));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_DVDTRACK));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_FRAMERATE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_MODEL));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_MUXABLE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_MUXINGMODE));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CONTAINS_VIDEOAUDIO));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CONTAINS_SUBTITLES));
						items.add(getConditionTypeCBItem(ConditionType.VIDEO_CONTAINS_GENRE));
						break;
					case AUDIO:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.FILE_THUMBNAILPATH));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_ALBUM));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_ARTIST));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_CODECA));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_DURATION_SEC));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_GENRE));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_NRAUDIOCHANNELS));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_SAMPLEFREQ));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_SONGNAME));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_TRACK));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_YEAR));
						items.add(getConditionTypeCBItem(ConditionType.AUDIO_MUXINGMODE));
						break;
					case PICTURES:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						items.add(getConditionTypeCBItem(ConditionType.FILE_THUMBNAILPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						items.add(getConditionTypeCBItem(ConditionType.IMAGE_HEIGHT));
						items.add(getConditionTypeCBItem(ConditionType.IMAGE_WIDTH));
						break;
					case FILE:
						items.add(getConditionTypeCBItem(ConditionType.FILE_FILENAME));
						items.add(getConditionTypeCBItem(ConditionType.FILE_FOLDERPATH));
						items.add(getConditionTypeCBItem(ConditionType.FILE_SIZEBYTE));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATELASTUPDATEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEINSERTEDDB));
						items.add(getConditionTypeCBItem(ConditionType.FILE_DATEMODIFIEDOS));
						items.add(getConditionTypeCBItem(ConditionType.FILEPLAYS_DATEPLAYEND));
						items.add(getConditionTypeCBItem(ConditionType.FILE_CONTAINS_TAG));
						items.add(getConditionTypeCBItem(ConditionType.FILE_PLAYCOUNT));
						break;
				}
				handledTypes.add(ft);
			}
		}
		Collections.sort(items);
		return items.toArray(new ConditionTypeCBItem[0]);
	}

	public ConditionValueType getConditionValueType(ConditionType conditionType, ConditionOperator conditionOperator) {
		ConditionValueType cvt = ConditionValueType.UNKNOWN;
		if(conditionOperator == ConditionOperator.IS_IN_THE_LAST_SEC 
				|| conditionOperator == ConditionOperator.IS_NOT_IN_THE_LAST_SEC
				|| conditionType == ConditionType.VIDEO_DURATIONSEC
	    		|| conditionType == ConditionType.AUDIO_DURATION_SEC){
	    	cvt = ConditionValueType.TIMESPAN;
	    }else if(conditionType == ConditionType.FILE_SIZEBYTE){
	    	cvt = ConditionValueType.FILESIZE;
	    } else if(conditionType == ConditionType.FILE_FILENAME
	    		|| conditionType == ConditionType.FILE_FOLDERPATH
	    		|| conditionType == ConditionType.FILE_CONTAINS_TAG
	    	    || conditionType == ConditionType.FILE_THUMBNAILPATH
	    		|| conditionType == ConditionType.VIDEO_ORIGINALNAME
	    		|| conditionType == ConditionType.VIDEO_NAME
	    		|| conditionType == ConditionType.VIDEO_IMDBID
	    		|| conditionType == ConditionType.VIDEO_TAGLINE
	    		|| conditionType == ConditionType.VIDEO_OVERVIEW
	    		|| conditionType == ConditionType.VIDEO_HOMEPAGEURL
	    		|| conditionType == ConditionType.VIDEO_TRAILERURL
	    		|| conditionType == ConditionType.VIDEO_DIRECTOR
	    		|| conditionType == ConditionType.VIDEO_CERTIFICATION
	    		|| conditionType == ConditionType.VIDEO_CERTIFICATIONREASON
	    		|| conditionType == ConditionType.VIDEO_CODECV
	    		|| conditionType == ConditionType.VIDEO_MIMETYPE
	    		|| conditionType == ConditionType.VIDEO_ASPECTRATIO
	    		|| conditionType == ConditionType.VIDEO_BITRATE
	    		|| conditionType == ConditionType.VIDEO_BITSPERPIXEL
	    		|| conditionType == ConditionType.VIDEO_CONTAINER
	    		|| conditionType == ConditionType.VIDEO_FRAMERATE
	    		|| conditionType == ConditionType.VIDEO_MODEL
	    		|| conditionType == ConditionType.VIDEO_BITRATE
	    		|| conditionType == ConditionType.VIDEO_CONTAINS_VIDEOAUDIO
	    		|| conditionType == ConditionType.VIDEO_CONTAINS_SUBTITLES
	    		|| conditionType == ConditionType.VIDEO_CONTAINS_GENRE
	    		|| conditionType == ConditionType.VIDEO_MUXINGMODE
	    		|| conditionType == ConditionType.AUDIO_SONGNAME
	    		|| conditionType == ConditionType.AUDIO_ARTIST
	    		|| conditionType == ConditionType.AUDIO_ALBUM
	    		|| conditionType == ConditionType.AUDIO_GENRE
	    		|| conditionType == ConditionType.AUDIO_LANG
	    		|| conditionType == ConditionType.AUDIO_NRAUDIOCHANNELS
	    		|| conditionType == ConditionType.AUDIO_SAMPLEFREQ
	    		|| conditionType == ConditionType.AUDIO_CODECA
	    		|| conditionType == ConditionType.AUDIO_BITSPERSAMPLE
	    		|| conditionType == ConditionType.AUDIO_COVERPATH
	    		|| conditionType == ConditionType.AUDIO_MUXINGMODE){
	    	cvt = ConditionValueType.STRING;
	    } else if(conditionType == ConditionType.FILE_DATELASTUPDATEDDB
	    		|| conditionType == ConditionType.FILE_DATEINSERTEDDB
	    		|| conditionType == ConditionType.FILE_DATEMODIFIEDOS
	    		|| conditionType == ConditionType.FILEPLAYS_DATEPLAYEND){
	    	cvt = ConditionValueType.DATETIME;	    	
	    } else if(conditionType == ConditionType.FILE_PLAYCOUNT
	    		|| conditionType == ConditionType.VIDEO_BUDGET
	    		|| conditionType == ConditionType.VIDEO_REVENUE
	    		|| conditionType == ConditionType.VIDEO_YEAR
	    		|| conditionType == ConditionType.VIDEO_WIDTH
	    		|| conditionType == ConditionType.VIDEO_HEIGHT
	    		|| conditionType == ConditionType.VIDEO_RATINGPERCENT
	    		|| conditionType == ConditionType.VIDEO_RATINGVOTERS
	    		|| conditionType == ConditionType.VIDEO_WIDTH
	    		|| conditionType == ConditionType.VIDEO_DVDTRACK
	    		|| conditionType == ConditionType.AUDIO_YEAR
	    		|| conditionType == ConditionType.AUDIO_TRACK){
	    	cvt = ConditionValueType.INTEGER;	    	
	    } else if(conditionType == ConditionType.IMAGE_WIDTH
	    		|| conditionType == ConditionType.IMAGE_HEIGHT
	    		|| conditionType == ConditionType.AUDIO_DELAYMS
	    		|| conditionType == ConditionType.VIDEO_TMDBID){
	    	cvt = ConditionValueType.DOUBLE;	    	
	    }else if(conditionType == ConditionType.VIDEO_MUXABLE
	    		|| conditionType == ConditionType.FILE_ISACTIF){
	    	cvt = ConditionValueType.BOOLEAN;	    	
	    }
	    return cvt;
    }

	public List<String> getExistingTags(FileType fileType) {
		return MediaLibraryStorage.getInstance().getExistingTags(fileType);
	}
}
