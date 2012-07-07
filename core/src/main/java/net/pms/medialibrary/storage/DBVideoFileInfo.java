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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.formats.SubtitleType;
import net.pms.medialibrary.commons.MediaLibraryConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOCertification;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.dataobjects.DORating;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.dataobjects.OmitPrefixesConfiguration;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.SortOption;
import net.pms.medialibrary.commons.exceptions.StorageException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DBVideoFileInfo extends DBFileInfo {	
	private static final Logger log = LoggerFactory.getLogger(DBVideoFileInfo.class);
	
	DBVideoFileInfo(JdbcConnectionPool cp){
		super(cp);
	}
	
	/*********************************************
	 * 
	 * Package Methods
	 * 
	 *********************************************/
	
	int cleanVideoFileInfos() throws StorageException{
		int res = 0;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		Savepoint savePoint = null;

		// Delete all entries related to video files in the DB
		try {
			conn = cp.getConnection();
			
			//prepare for rollback
			conn.setAutoCommit(false);
			savePoint = conn.setSavepoint();
			
			//get all filePaths
			String statement = "SELECT FILE.ID, FILE.FOLDERPATH, FILE.FILENAME" +
					" FROM FILE, VIDEO" +
					" WHERE VIDEO.FILEID = FILE.ID";
			stmt = conn.prepareStatement(statement);
			rs = stmt.executeQuery();
			
			
			//delete all entries which can't be found
			while(rs.next()){
				long fileId = rs.getLong(1);
				String filePath = rs.getString(2) + File.separator + rs.getString(3);
				File file = new File(filePath);
				if(!file.exists()){
					deleteVideo(fileId, conn, stmt);
					res++;
				}
			}
			conn.commit();
        } catch (SQLException e) {
			try {
				conn.rollback(savePoint);
			} catch (SQLException e1) {
				log.error("Failed to roll back transaction to save point after a problem occured while cleaning videos", e);
			}
			throw new StorageException("Failed to clear videos properly", e);
        } finally {
			close(conn, stmt, rs, savePoint);
        }
		
		return res;
	}

	int deleteAllVideos() throws StorageException {
		int res = 0;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		Savepoint savePoint = null;

		// Delete all entries related to video files in the DB
		try {
			conn = cp.getConnection();

			//prepare for rollback
			conn.setAutoCommit(false);
			savePoint = conn.setSavepoint();
			
			//clear audio tracks
			stmt = conn.prepareStatement("DELETE FROM VIDEOAUDIO");
	        stmt.executeUpdate();

			//clear subtitles
			stmt = conn.prepareStatement("DELETE FROM SUBTITLES ");
	        stmt.executeUpdate();
	        
			//for all videos, delete itself, associated file and file tags
			String statement = "SELECT FILEID FROM VIDEO";
			stmt = conn.prepareStatement(statement);
			rs = stmt.executeQuery();
			int nbDeletedVideos = 0;
			while(rs.next()){
				String fileIdStr = "";
				try{
    				long fileId = rs.getLong(1);
    				deleteVideo(fileId, conn, stmt);
    		        nbDeletedVideos++;
				} catch (SQLException e) {
					throw new StorageException("Failed to clear video id=" + fileIdStr + " properly", e);
		        }
			}

			conn.commit();
			res = nbDeletedVideos;
        } catch (SQLException e) {
			try {
				conn.rollback(savePoint);
			} catch (SQLException e1) {
				log.error("Failed to roll back transaction to save point after a problem occured during delete", e);
			}
			throw new StorageException("Failed to clear videos properly", e);
        } finally {
			close(conn, stmt, rs, savePoint);
        }
        
        return res;
	}

	void deleteVideo(long fileId) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
		Savepoint savePoint = null;
		
		try {
			conn = cp.getConnection();
			
			//prepare for rollback
			conn.setAutoCommit(false);
			savePoint = conn.setSavepoint();
			
			deleteVideo(fileId, conn, stmt);
			conn.commit();
        } catch (SQLException e) {
			try {
				conn.rollback(savePoint);
			} catch (SQLException e1) {
				log.error("Failed to roll back transaction to save point after a problem occured during delete", e);
			}
			throw new StorageException("Failed to delete video with id=" + fileId, e);
		} finally {
			close(conn, stmt, savePoint);
        }
	}
	
	List<DOVideoFileInfo> getVideoFileInfo(DOFilter filter, boolean sortAscending, final ConditionType sortField, SortOption sortOption, int maxResults, boolean onlyActive) throws StorageException {
		HashMap<Integer, DOVideoFileInfo> videos = new LinkedHashMap<Integer, DOVideoFileInfo>();

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;

		// Get video
		try {
			conn = cp.getConnection();
			
			// create the where clause
			String whereClause = "VIDEO.FILEID = FILE.ID";
			if(onlyActive) {
				whereClause += " AND FILE.ENABLED = 1";
			}
			if (filter.getConditions().size() > 0) {
				whereClause += " AND (" + formatEquation(filter) + ")";
			}
			
			// create order condition
			String orderByClause = sortField.toString();
			if (orderByClause.contains("_")) {
				orderByClause = orderByClause.replace('_', '.');
			}
			
			if (sortAscending) {
				orderByClause += " ASC";
			} else {
				orderByClause += " DESC";
			}

			if(sortField != ConditionType.VIDEO_NAME){
				orderByClause += ", " + ConditionType.VIDEO_NAME.toString().replace('_', '.') + " ASC";
			}
			orderByClause += ", " + ConditionType.FILE_FILENAME.toString().replace('_', '.') + " ASC";
			orderByClause += ", " + ConditionType.FILE_DATEINSERTEDDB.toString().replace('_', '.') + " ASC";
			
			if(log.isDebugEnabled()) log.debug(String.format("Video query clause: WHERE %s ORDER BY %s", whereClause, orderByClause));

			String statement = "SELECT FILE.ID, FILE.FOLDERPATH, FILE.FILENAME, FILE.TYPE, FILE.SIZEBYTE, FILE.DATELASTUPDATEDDB, FILE.DATEINSERTEDDB" 
			        + ", FILE.DATEMODIFIEDOS, FILE.THUMBNAILPATH, FILE.PLAYCOUNT, FILE.ENABLED" // FILE
			        + ", VIDEO.ORIGINALNAME, VIDEO.NAME, VIDEO.SORTNAME, VIDEO.TMDBID, VIDEO.IMDBID, VIDEO.OVERVIEW, VIDEO.BUDGET, VIDEO.REVENUE, VIDEO.HOMEPAGEURL, VIDEO.TRAILERURL" 
			        + ", VIDEO.AGERATINGLEVEL, VIDEO.AGERATINGREASON, VIDEO.RATINGPERCENT, VIDEO.RATINGVOTERS, VIDEO.DIRECTOR, VIDEO.TAGLINE"
			        + ", VIDEO.ASPECTRATIO, VIDEO.BITRATE, VIDEO.BITSPERPIXEL, VIDEO.CODECV, VIDEO.DURATIONSEC, VIDEO.CONTAINER, VIDEO.DVDTRACK, VIDEO.FRAMERATE"
			        + ", VIDEO.HEIGHT, VIDEO.MIMETYPE, VIDEO.MODEL, VIDEO.MUXABLE, VIDEO.WIDTH, VIDEO.YEAR, VIDEO.MUXINGMODE" // VIDEO
			        + ", FILEPLAYS.DATEPLAYEND" //last play
			        + ", VIDEOAUDIO.LANG, VIDEOAUDIO.NRAUDIOCHANNELS, VIDEOAUDIO.SAMPLEFREQ, VIDEOAUDIO.CODECA, VIDEOAUDIO.BITSPERSAMPLE, VIDEOAUDIO.DELAYMS, VIDEOAUDIO.MUXINGMODE" //VIDEOAUDIO
			        + ", SUBTITLES.FILEPATH, SUBTITLES.LANG, SUBTITLES.TYPE" //SUBTITLES
			        + ", FILETAGS.KEY, FILETAGS.VALUE" //TAGS
			        + " FROM FILE, VIDEO" 
			        + " LEFT JOIN VIDEOAUDIO ON VIDEO.FILEID = VIDEOAUDIO.FILEID"
			        + " LEFT JOIN SUBTITLES ON VIDEO.FILEID = SUBTITLES.FILEID" 
			        + " LEFT JOIN FILETAGS ON VIDEO.FILEID = FILETAGS.FILEID" 
			        + " LEFT JOIN FILEPLAYS ON VIDEO.FILEID = FILEPLAYS.FILEID" 
			        + " WHERE " + whereClause
			        + " ORDER BY " + orderByClause;
			stmt = conn.prepareStatement(statement);

			rs = stmt.executeQuery();
			while (rs.next()) {
				DOVideoFileInfo videoFile = new DOVideoFileInfo();
				try {
					int pos = 1;
					videoFile.setId(rs.getInt(pos++));

					if (!videos.containsKey(videoFile.getId())) {
						//import all fields when having a video with a new id
						videoFile.setFolderPath(rs.getString(pos++));
						videoFile.setFileName(rs.getString(pos++));
						videoFile.setType(FileType.valueOf(rs.getString(pos++)));
						videoFile.setSize(rs.getLong(pos++));
						videoFile.setDateLastUpdatedDb(new Date(rs.getTimestamp(pos++).getTime()));
						videoFile.setDateInsertedDb(new Date(rs.getTimestamp(pos++).getTime()));
						videoFile.setDateModifiedOs(new Date(rs.getTimestamp(pos++).getTime()));
						videoFile.setThumbnailPath(rs.getString(pos++));
						videoFile.setPlayCount(rs.getInt(pos++));
						videoFile.setActive(rs.getBoolean(pos++));
						
						videoFile.setOriginalName(rs.getString(pos++));
						videoFile.setName(rs.getString(pos++));
						videoFile.setSortName(rs.getString(pos++));
						videoFile.setTmdbId(rs.getInt(pos++));
						videoFile.setImdbId(rs.getString(pos++));
						videoFile.setOverview(rs.getString(pos++));
						videoFile.setBudget(rs.getInt(pos++));
						videoFile.setRevenue(rs.getInt(pos++));
						videoFile.setHomepageUrl(rs.getString(pos++));
						videoFile.setTrailerUrl(rs.getString(pos++));
						videoFile.setAgeRating(new DOCertification(rs.getString(pos++), rs.getString(pos++)));
						videoFile.setRating(new DORating(rs.getInt(pos++), rs.getInt(pos++)));
						videoFile.setDirector(rs.getString(pos++));
						videoFile.setTagLine(rs.getString(pos++));
						videoFile.setAspectRatio(rs.getString(pos++));
						videoFile.setBitrate(rs.getInt(pos++));
						videoFile.setBitsPerPixel(rs.getInt(pos++));
						videoFile.setCodecV(rs.getString(pos++));
						videoFile.setDurationSec(rs.getInt(pos++));	
						videoFile.setContainer(rs.getString(pos++));
						videoFile.setDvdtrack(rs.getInt(pos++));
						videoFile.setFrameRate(rs.getString(pos++));
						videoFile.setHeight(rs.getInt(pos++));
						videoFile.setMimeType(rs.getString(pos++));
						videoFile.setModel(rs.getString(pos++));
						videoFile.setMuxable(rs.getBoolean(pos++));
	
						videoFile.setWidth(rs.getInt(pos++));
						videoFile.setYear(rs.getInt(pos++));
						videoFile.setMuxingMode(rs.getString(pos++));

						videos.put(videoFile.getId(), videoFile);
					}else{
						//skip the already imported fields if the video with this id is already contained in the list
						pos = 43;
						
						videoFile = videos.get(videoFile.getId());
					}
					
					//play count history
					Timestamp playTimestamp = rs.getTimestamp(pos++);
					if(playTimestamp != null) {
						Date playDate = new Date(playTimestamp.getTime());
						if(!videoFile.getPlayHistory().contains(playDate)) {
							videoFile.addPlayToHistory(playDate);
						}
					}

					// Audio track
					DLNAMediaAudio audioTrack = new DLNAMediaAudio();
					audioTrack.setLang(rs.getString(pos++));
					audioTrack.setNrAudioChannels(rs.getInt(pos++));
					audioTrack.setSampleFrequency(rs.getString(pos++));
					audioTrack.setCodecA(rs.getString(pos++));
					audioTrack.setBitsperSample(rs.getInt(pos++));
					audioTrack.setDelay(rs.getInt(pos++));
					audioTrack.setMuxingModeAudio(rs.getString(pos++));

					boolean doInsertAudioTrack = true;
					for (DLNAMediaAudio currTrack : videoFile.getAudioCodes()) {
						if(currTrack.equals(audioTrack)) {
							doInsertAudioTrack = false;
							break;
						}
					}
					if (doInsertAudioTrack) {
						videoFile.getAudioCodes().add(audioTrack);
					}

					// Subtitle track
					DLNAMediaSubtitle subtitleTrack = new DLNAMediaSubtitle();
					String subtitleFilePath = rs.getString(pos++);
					File subTitleFile;
					if (subtitleFilePath != null && !subtitleFilePath.equals("") && (subTitleFile = new File(subtitleFilePath)).exists()) {
						subtitleTrack.setExternalFile(subTitleFile);
					}
					subtitleTrack.setLang(rs.getString(pos++));
					subtitleTrack.setType(SubtitleType.values()[rs.getInt(pos++)]);

					boolean doInsertSubtitleTrack = true;
					for (DLNAMediaSubtitle currTrack : videoFile.getSubtitlesCodes()) {
						if(currTrack.equals(subtitleTrack)) { 
							doInsertSubtitleTrack = false;
							break;
						}
					}
					if (doInsertSubtitleTrack) {
						videoFile.getSubtitlesCodes().add(subtitleTrack);
					}

					// Genres and Tags
					String tagKey = rs.getString(pos++);
					String tagValue = rs.getString(pos++);
					if (tagKey == null) {
						// do nothing
					} else if (tagKey.equals(GENRE_KEY)) {
						//it's a genre
						if (!videoFile.getGenres().contains(tagValue)) {
							videoFile.getGenres().add(tagValue);
						}
					} else {
						//it's a tag
						if(videoFile.getTags().containsKey(tagKey)) {
							//add the tag to the existing list
							List<String> tagValues = videoFile.getTags().get(tagKey);
							if(!tagValues.contains(tagValue)) {
								tagValues.add(tagValue);
							}
						} else {
							//create a new list as it doesn't exist yet
							List<String> l = new ArrayList<String>();
							l.add(tagValue);
							videoFile.getTags().put(tagKey, l);
						}
					}
				} catch (Exception ex) {
					log.error("Failed to read file from library. This should never happen!!!", ex);
				}
			}
		} catch (SQLException ex) {
			throw new StorageException("Failed to get video files", ex);
		} finally {
			close(conn, stmt, rs);
		}
		
		List<DOVideoFileInfo> res = new ArrayList<DOVideoFileInfo>(videos.values());
		
		//re-sort if needed (according to sort prefixes and sortOption)
		if(sortOption == SortOption.Random){
			Collections.shuffle(res);
		} else if(sortOption == SortOption.FileProperty){
			final OmitPrefixesConfiguration omitConfig = MediaLibraryConfiguration.getInstance().getOmitPrefixesConfiguration();
			if (omitConfig.isSorting() && (sortField == ConditionType.VIDEO_NAME || sortField == ConditionType.VIDEO_ORIGINALNAME || sortField == ConditionType.VIDEO_SORTNAME)) {
				Collections.sort(res, new Comparator<DOVideoFileInfo>() {
	
					@Override
					public int compare(DOVideoFileInfo o1, DOVideoFileInfo o2) {
						String s1 = "";
						String s2 = "";
						
						switch (sortField) {
						case VIDEO_NAME:
							s1 = o1.getName();
							s2 = o2.getName();
							break;
						case VIDEO_ORIGINALNAME:
							s1 = o1.getOriginalName();
							s2 = o2.getOriginalName();
							break;
						case VIDEO_SORTNAME:
							s1 = o1.getSortName();
							s2 = o2.getSortName();
							break;
						}
						
						s1 = s1.toLowerCase();
						s2 = s2.toLowerCase();
	
						for(String prefix : omitConfig.getPrefixes()){
							String compStr = prefix.toLowerCase();
							if(Character.isJavaIdentifierStart(compStr.charAt(compStr.length() -1))){
								compStr += " ";
							}
							if (s1.startsWith(compStr)) s1 = s1.substring(compStr.length());
							if (s2.startsWith(compStr)) s2 = s2.substring(compStr.length());						
						}
	
						return s1.compareTo(s2);
					}
				});
				
				if(!sortAscending){
					Collections.reverse(res);
				}
			}			
		}
		
		//limit the number of videos if configured
		if(maxResults > 0 && maxResults < res.size()){
			res = res.subList(0, maxResults);
		}
		
		return res;
	}
	
	List<String> getVideoProperties(ConditionType conditionType, boolean isAscending, int minOccurences) throws StorageException{
		List<String> retVal = new ArrayList<String>();

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		// Get connection
		try {
			conn = cp.getConnection();
		} catch (SQLException ex) {
			throw new StorageException("Failed to get get video properties", ex);
		}
		
		String suf = "VIDEO_CONTAINS_";
		if(conditionType.toString().startsWith(suf)){
			try {
				
				retVal = new ArrayList<String>();
				String tableName = conditionType.toString().substring(suf.length());
				String columnName = "";

				if(conditionType == ConditionType.VIDEO_CONTAINS_VIDEOAUDIO || conditionType == ConditionType.VIDEO_CONTAINS_SUBTITLES){
					columnName = "LANG";

					String q = "SELECT " + columnName
							+ " FROM " + tableName
							+ " GROUP BY " + columnName
							+ " HAVING COUNT(" + columnName + ") >= ?"
							+ " ORDER BY " + columnName + " " + (isAscending ? "ASC" : "DESC");
					stmt = conn.prepareStatement(q);
					stmt.setInt(1, minOccurences);
					rs = stmt.executeQuery();
					while (rs.next()) {
						retVal.add(rs.getString(1));
					}
				} else if(conditionType == ConditionType.VIDEO_CONTAINS_GENRE){
					retVal = getTagValues(GENRE_KEY, isAscending, minOccurences);
				}	
			} catch (SQLException se) {
				throw new StorageException("Failed to get get video properties", se);
			} finally {
				close(conn, stmt, rs);
			}
		}
		else{
			try {			
				retVal = new ArrayList<String>();
				String columnName = conditionType.toString().substring(6);
				
				stmt = conn.prepareStatement("SELECT DISTINCT " + columnName
												+ " FROM VIDEO"
												+ " GROUP BY " + columnName
												+ " HAVING COUNT(" + columnName + ") >= ?"
												+ " ORDER BY " + columnName + " " + (isAscending ? "ASC" : "DESC"));
				stmt.setInt(1, minOccurences);
				rs = stmt.executeQuery();
				while (rs.next()) {
					retVal.add(rs.getString(1));
				}
			} catch (SQLException se) {
				throw new StorageException("Failed to get get video properties", se);
			} finally {
				close(conn, stmt, rs);
			}
		}
		return retVal;
	}
	
	int getFilteredVideoCount(DOFilter filter) throws StorageException {
		int nbItems = 0;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			
			String statement = "SELECT COUNT(FILE.ID)" +
        				" FROM FILE, VIDEO" +
        				" LEFT JOIN VIDEOAUDIO ON VIDEO.FILEID = VIDEOAUDIO.FILEID" +
        				" LEFT JOIN SUBTITLES ON VIDEO.FILEID = SUBTITLES.FILEID" +
        				" LEFT JOIN FILETAGS ON VIDEO.FILEID = FILETAGS.FILEID";
			if(filter.getConditions().size() > 0) {
				statement += " WHERE " + formatEquation(filter);
			}
			stmt = conn.prepareStatement(statement);
			rs = stmt.executeQuery();
			
			if(rs.next()){
				nbItems = rs.getInt(1);
			}			
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to get filtered video count for filter with equation='%s' and %s conditions", filter.getEquation(), filter.getConditions().size()), se);
		} finally {
			close(conn, stmt, rs);
		}	
		
		return nbItems;
    }

    int getVideoCount() throws StorageException {
		int count = 0;

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT Count(ID) FROM VIDEO");
			rs = stmt.executeQuery();
			if(rs.next()){
				count = rs.getInt(1);
			}
		    
		} catch (SQLException se) {
			throw new StorageException("Failed to get video count", se);
		} finally {
			close(conn, stmt, rs);
		}
		
		return count;
    }

	void insertVideoFileInfo(DOVideoFileInfo fileInfo) throws StorageException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Savepoint savePoint = null;

		try {			
			conn = cp.getConnection();
			
			//prepare transaction
			conn.setAutoCommit(false);
			savePoint = conn.setSavepoint();
			
			super.insertFileInfo(fileInfo, conn, stmt, rs);

			stmt = conn.prepareStatement("INSERT INTO VIDEO (FILEID, AGERATINGLEVEL, AGERATINGREASON, RATINGPERCENT, RATINGVOTERS"
			        + ", DIRECTOR, TAGLINE, ASPECTRATIO, BITRATE, BITSPERPIXEL, CODECV, DURATIONSEC, CONTAINER, DVDTRACK, FRAMERATE, MIMETYPE, MODEL, MUXABLE"
			        + ", WIDTH, YEAR, HEIGHT, ORIGINALNAME, NAME, TMDBID, IMDBID, OVERVIEW, BUDGET, REVENUE, HOMEPAGEURL, TRAILERURL, SORTNAME, MUXINGMODE)"
			        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stmt.clearParameters();
			stmt.setInt(1, fileInfo.getId());
			stmt.setString(2, fileInfo.getAgeRating().getLevel());
			stmt.setString(3, fileInfo.getAgeRating().getReason());
			stmt.setInt(4, fileInfo.getRating().getRatingPercent());
			stmt.setInt(5, fileInfo.getRating().getVotes());
			stmt.setString(6, fileInfo.getDirector());
			stmt.setString(7, fileInfo.getTagLine());
			stmt.setString(8, fileInfo.getAspectRatio());
			stmt.setInt(9, fileInfo.getBitrate());
			stmt.setInt(10, fileInfo.getBitsPerPixel());
			stmt.setString(11, fileInfo.getCodecV());
			stmt.setDouble(12, fileInfo.getDurationSec());
			stmt.setString(13, fileInfo.getContainer());
			stmt.setInt(14, fileInfo.getDvdtrack());
			stmt.setString(15, fileInfo.getFrameRate());
			stmt.setString(16, fileInfo.getMimeType());
			stmt.setString(17, fileInfo.getModel());
			stmt.setBoolean(18, fileInfo.isMuxable());
			stmt.setInt(19, fileInfo.getWidth());
			stmt.setInt(20, fileInfo.getYear());
			stmt.setInt(21, fileInfo.getHeight());

			stmt.setString(22, fileInfo.getOriginalName());
			stmt.setString(23, fileInfo.getName());
			stmt.setInt(24, fileInfo.getTmdbId());
			stmt.setString(25, fileInfo.getImdbId());
			stmt.setString(26, fileInfo.getOverview());
			stmt.setInt(27, fileInfo.getBudget());
			stmt.setInt(28, fileInfo.getRevenue());
			stmt.setString(29, fileInfo.getHomepageUrl());
			stmt.setString(30, fileInfo.getTrailerUrl());
			stmt.setString(31, fileInfo.getSortName());
			stmt.setString(32, fileInfo.getMuxingMode());
			stmt.executeUpdate();

			insertOrUpdateVideoPropertyLists(fileInfo, stmt, conn);
			
			conn.commit();
		} catch (Exception e) {
			try {
				conn.rollback(savePoint);
			} catch (SQLException e1) {
				log.error("Failed to roll back transaction to save point after a problem occured during insert", e);
			}
			throw new StorageException("Failed to insert video file info " + fileInfo.getFilePath(), e);
		} finally {
			close(conn, stmt, rs, savePoint);
		}
	}

	void updateFileInfo(DOVideoFileInfo fileInfo) throws StorageException {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Savepoint savePoint = null;

		try {
			conn = cp.getConnection();
			
			//prepare for rollback
			conn.setAutoCommit(false);
			savePoint = conn.setSavepoint();
			
			//update the properties from the super class
			super.updateFileInfo(fileInfo, conn, stmt, rs);
			
			//update video properties
    		stmt = conn.prepareStatement("UPDATE VIDEO SET AGERATINGLEVEL = ?, AGERATINGREASON = ?, RATINGPERCENT = ?, RATINGVOTERS = ?"
    		        + ", DIRECTOR = ?, TAGLINE = ?, ASPECTRATIO = ?, BITRATE = ?, BITSPERPIXEL = ?, CODECV = ?, DURATIONSEC = ?, CONTAINER = ?, DVDTRACK = ?, FRAMERATE = ?, MIMETYPE = ?, MODEL = ?, MUXABLE = ?"
    		        + ", WIDTH = ?, YEAR = ?, HEIGHT = ?, ORIGINALNAME = ?, NAME = ?, TMDBID = ?, IMDBID = ?, OVERVIEW = ?, BUDGET = ?, REVENUE = ?, HOMEPAGEURL = ?, TRAILERURL = ?, SORTNAME = ?, MUXINGMODE = ?"
    		        + " WHERE FILEID = ?");
    		stmt.clearParameters();
    		stmt.setString(1, fileInfo.getAgeRating().getLevel());
    		stmt.setString(2, fileInfo.getAgeRating().getReason());
    		stmt.setInt(3, fileInfo.getRating().getRatingPercent());
    		stmt.setInt(4, fileInfo.getRating().getVotes());
    		stmt.setString(5, fileInfo.getDirector());
    		stmt.setString(6, fileInfo.getTagLine());
    		stmt.setString(7, fileInfo.getAspectRatio());
    		stmt.setInt(8, fileInfo.getBitrate());
    		stmt.setInt(9, fileInfo.getBitsPerPixel());
    		stmt.setString(10, fileInfo.getCodecV());
    		stmt.setDouble(11, fileInfo.getDurationSec());
    		stmt.setString(12, fileInfo.getContainer());
    		stmt.setInt(13, fileInfo.getDvdtrack());
    		stmt.setString(14, fileInfo.getFrameRate());
    		stmt.setString(15, fileInfo.getMimeType());
    		stmt.setString(16, fileInfo.getModel());
    		stmt.setBoolean(17, fileInfo.isMuxable());
    		stmt.setInt(18, fileInfo.getWidth());
    		stmt.setInt(19, fileInfo.getYear());
    		stmt.setInt(20, fileInfo.getHeight());
    
    		stmt.setString(21, fileInfo.getOriginalName());
    		stmt.setString(22, fileInfo.getName());
    		stmt.setInt(23, fileInfo.getTmdbId());
    		stmt.setString(24, fileInfo.getImdbId());
    		stmt.setString(25, fileInfo.getOverview());
    		stmt.setInt(26, fileInfo.getBudget());
    		stmt.setInt(27, fileInfo.getRevenue());
    		stmt.setString(28, fileInfo.getHomepageUrl());
    		stmt.setString(29, fileInfo.getTrailerUrl());
    		stmt.setString(30, fileInfo.getSortName());
    		stmt.setString(31, fileInfo.getMuxingMode());
    		stmt.setInt(32, fileInfo.getId());
    		stmt.executeUpdate();

    		insertOrUpdateVideoPropertyLists(fileInfo, stmt, conn);
			
			conn.commit();
		} catch (Exception e) {
			try {
				conn.rollback(savePoint);
			} catch (SQLException e1) {
				log.error("Failed to roll back transaction to save point after a problem occured during update", e);
			}
			throw new StorageException("Failed to update video file info " + fileInfo.getFilePath(), e);
		} finally {
			close(conn, stmt, rs, savePoint);
		}
    }

	private void insertOrUpdateVideoPropertyLists(DOVideoFileInfo videoFileInfo, PreparedStatement stmt, Connection conn) throws StorageException{
		insertOrUpdateAudioTracks(videoFileInfo, stmt, conn);
		insertOrUpdateSubtitles(videoFileInfo, stmt, conn);
		insertOrUpdateGenres(videoFileInfo, stmt, conn);
	}
	
	private void insertOrUpdateAudioTracks(DOVideoFileInfo videoFileInfo, PreparedStatement stmt, Connection conn) throws StorageException {
		//delete all audio tracks that might be linked to the video file
		try {
			stmt = conn.prepareStatement("DELETE FROM VIDEOAUDIO WHERE FILEID = ?");
			stmt.clearParameters();
			stmt.setInt(1, videoFileInfo.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new StorageException("Failed to delete all audio tracks linked to video with id=" + videoFileInfo.getId(), e);
		}
		
		// Insert audio tracks for video
		for (DLNAMediaAudio media : videoFileInfo.getAudioCodes()) {
			try {
				stmt = conn.prepareStatement("INSERT INTO VIDEOAUDIO(FILEID, LANG, NRAUDIOCHANNELS, SAMPLEFREQ, CODECA, BITSPERSAMPLE, DELAYMS, MUXINGMODE)"
				        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
				stmt.clearParameters();
				stmt.setInt(1, videoFileInfo.getId());
				stmt.setString(2, media.getLang());
				stmt.setInt(3, media.getNrAudioChannels());
				stmt.setString(4, media.getSampleFrequency());
				stmt.setString(5, media.getCodecA());
				stmt.setInt(6, media.getBitsperSample());
				stmt.setInt(7, media.getDelay());
				stmt.setString(8, media.getMuxingModeAudio());
				stmt.executeUpdate();
			} catch (Exception e) {
				throw new StorageException("Failed to insert audio file with lang=" + media.getLang() + " for file " + videoFileInfo.getFileName(false), e);
			}
		}
	}
	
	private void insertOrUpdateSubtitles(DOVideoFileInfo videoFileInfo, PreparedStatement stmt, Connection conn) throws StorageException {
		//delete all subtitles that might be linked to the video file
		try {
			stmt = conn.prepareStatement("DELETE FROM SUBTITLES WHERE FILEID = ?");
			stmt.clearParameters();
			stmt.setInt(1, videoFileInfo.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new StorageException("Failed to delete all subtitles linked to video with id=" + videoFileInfo.getId(), e);
		}

		// Insert subtitles for video
		for (DLNAMediaSubtitle subtitle : videoFileInfo.getSubtitlesCodes()) {
			try {
				stmt = conn.prepareStatement("INSERT INTO SUBTITLES (FILEID, FILEPATH, LANG, TYPE)" 
						+ " VALUES (?, ?, ?, ?)");
				stmt.clearParameters();
				stmt.setInt(1, videoFileInfo.getId());
				String filePath = "";
				if (subtitle.getExternalFile() != null) {
					filePath = subtitle.getExternalFile().getAbsolutePath();
				}
				stmt.setString(2, filePath);
				stmt.setString(3, subtitle.getLang());
				stmt.setInt(4, subtitle.getType().ordinal());
				stmt.executeUpdate();
			} catch (Exception e) {
				throw new StorageException("Failed to insert subtitles lang=" + subtitle.getLang() + " for file " + videoFileInfo.getFileName(false), e);
			}
		}
	}
	
	private void insertOrUpdateGenres(DOVideoFileInfo videoFileInfo, PreparedStatement stmt, Connection conn) throws StorageException {
		//delete all genres that might be linked to the video file
		try {
			stmt = conn.prepareStatement("DELETE FROM FILETAGS WHERE FILEID = ? AND KEY = ?");
			stmt.clearParameters();
			stmt.setInt(1, videoFileInfo.getId());
			stmt.setString(2, GENRE_KEY);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new StorageException("Failed to delete all genres linked to video with id=" + videoFileInfo.getId(), e);
		}

		// Insert genres for video
		for (String genre : videoFileInfo.getGenres()) {
			try {
				stmt = conn.prepareStatement("INSERT INTO FILETAGS(FILEID, KEY, VALUE)" 
						+ " VALUES (?, ?, ?)");
				stmt.clearParameters();
				stmt.setInt(1, videoFileInfo.getId());
				stmt.setString(2, GENRE_KEY);
				stmt.setString(3, genre);
				stmt.executeUpdate();
			} catch (Exception e) {
				throw new StorageException("Failed to insert genre " + genre + " for file " + videoFileInfo.getFileName(false), e);
			}
		}	
	}
	
	private void deleteVideo(long fileId, Connection conn, PreparedStatement stmt) throws StorageException {
		String fileIdStr = "";
		try {
			fileIdStr = String.valueOf(fileId);

			stmt = conn.prepareStatement("DELETE FROM VIDEO WHERE FILEID = ?");
			stmt.setLong(1, fileId);
			stmt.executeUpdate();

			stmt = conn.prepareStatement("DELETE FROM VIDEOAUDIO WHERE FILEID = ?");
			stmt.setLong(1, fileId);
			stmt.executeUpdate();

			stmt = conn.prepareStatement("DELETE FROM SUBTITLES WHERE FILEID = ?");
			stmt.setLong(1, fileId);
			stmt.executeUpdate();

			super.deleteFile(fileId, conn, stmt);
		} catch (SQLException e) {
			throw new StorageException("Failed to clear video id=" + fileIdStr + " properly", e);
		}
	}
}