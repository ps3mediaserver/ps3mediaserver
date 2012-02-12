package net.pms.medialibrary.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.h2.jdbcx.JdbcConnectionPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.medialibrary.commons.MediaLibraryConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOCondition;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.dataobjects.OmitPrefixesConfiguration;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.ConditionValueType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.SortOption;
import net.pms.medialibrary.commons.exceptions.StorageException;

class DBFileInfo extends DBBase {
	private static final Logger log = LoggerFactory.getLogger(DBFileInfo.class);
	protected static final String GENRE_KEY = "_X-?__GENRE__%*Y_";
	
	DBFileInfo(JdbcConnectionPool cp){
		super(cp);
	}
	
	/*********************************************
	 * 
	 * Package Methods
	 * 
	 *********************************************/

	List<String> getExistingTags(FileType fileType) throws StorageException {		
		List<String> retVal = new ArrayList<String>();

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		//Get date last updated
		try {
			conn = cp.getConnection();
			
			stmt = conn.prepareStatement("SELECT DISTINCT FILETAGS.KEY"
						+ " FROM FILETAGS"
						+ " LEFT JOIN FILE ON FILETAGS.FILEID = FILE.ID"
						+ " WHERE FILE.TYPE = ? AND FILETAGS.KEY != ?"
						+ " ORDER BY KEY ASC");
			stmt.setString(1, fileType.toString());
			stmt.setString(2, GENRE_KEY);
			rs = stmt.executeQuery();
			while(rs.next()) {
				retVal.add(rs.getString(1));				
			}
		} catch (SQLException ex) {
			throw new StorageException("Failed to get tags for file type=" + fileType, ex);
		} finally {
			close(conn, stmt, rs);
		}		
		
		return retVal;
	}

	List<String> getTagValues(String tagName, boolean isAscending, int minOccurences) throws StorageException {		
		List<String> retVal = new ArrayList<String>();

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		//Get date last updated
		try {
			conn = cp.getConnection();
			
			String q = "SELECT FILETAGS.VALUE"
						+ " FROM FILETAGS"
						+ " WHERE FILETAGS.KEY = ?"
						+ " GROUP BY FILETAGS.VALUE"
						+ " HAVING COUNT(FILETAGS.FILEID) >= ?"
						+ " ORDER BY FILETAGS.VALUE " + (isAscending ? "ASC" : "DESC");
			
			stmt = conn.prepareStatement(q);
			stmt.setString(1, tagName);
			stmt.setInt(2, minOccurences);
			rs = stmt.executeQuery();
			while(rs.next()) {
				retVal.add(rs.getString(1));				
			}
		} catch (SQLException ex) {
			throw new StorageException("Failed to get available values for tag=" + tagName, ex);
		} finally {
			close(conn, stmt, rs);
		}		
		
		return retVal;
	}
	
	void delete(long id) throws StorageException{
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			delete(id, conn, stmt);
		} catch (Exception e) {
			throw new StorageException("Failed to delete file with id=" + id, e);
		} finally {
			close(conn, stmt);
		}		
	}
	
	void delete(long id, Connection conn, PreparedStatement stmt) throws SQLException{			
		//delete play counts
		stmt = conn.prepareStatement("DELETE FROM FILEPLAYS WHERE FILEID = ?");
		stmt.setLong(1, id);
		stmt.execute();
			
		//delete file tags
		stmt = conn.prepareStatement("DELETE FROM FILETAGS WHERE FILEID = ?");
		stmt.setLong(1, id);
	    stmt.executeUpdate();
			
		//delete file
		stmt = conn.prepareStatement("DELETE FROM FILE WHERE ID = ?");
		stmt.setLong(1, id);
		stmt.execute();
	}

	void deleteFileInfoByFilePath(String filePath) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		int pos = filePath.lastIndexOf(File.separatorChar) + 1;
		String fileName = filePath.substring(0, pos);
		String folderPath = filePath.substring(pos);

		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT ID FROM FILE WHERE FOLDERPATH = ? AND FILENAME = ?");
			stmt.setString(1, folderPath);
			stmt.setString(2, fileName);
			rs = stmt.executeQuery();
			if(rs.next()){
				long fileId = rs.getLong(1);
				delete(fileId, conn, stmt);
			}
		} catch (Exception e) {
			throw new StorageException("Failed to delete fileinfo for " + filePath, e);
		} finally {
			close(conn, stmt, rs);
		}
    }

	void insertFileInfo(DOFileInfo fileInfo) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		fileInfo.setDateInsertedDb(new java.util.Date());
		fileInfo.setDateLastUpdatedDb(new java.util.Date());

		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("INSERT INTO FILE (FOLDERPATH, FILENAME, TYPE, DATELASTUPDATEDDB, DATEINSERTEDDB, DATEMODIFIEDOS, THUMBNAILPATH, SIZEBYTE, PLAYCOUNT, ENABLED)"
			                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stmt.setString(1, fileInfo.getFolderPath());
			stmt.setString(2, fileInfo.getFileName());
			stmt.setString(3, fileInfo.getType().toString());
			stmt.setTimestamp(4, new Timestamp(fileInfo.getDateLastUpdatedDb().getTime()));
			stmt.setTimestamp(5, new Timestamp(fileInfo.getDateInsertedDb().getTime()));
			stmt.setTimestamp(6, new Timestamp(fileInfo.getDateModifiedOs().getTime()));
			stmt.setString(7, fileInfo.getThumbnailPath());
			stmt.setLong(8, fileInfo.getSize());
			stmt.setInt(9, fileInfo.getPlayCount());
			stmt.setBoolean(10, fileInfo.isActif());
			stmt.executeUpdate();
			
			rs = stmt.getGeneratedKeys();
			if (rs != null && rs.next()) {
				fileInfo.setId(rs.getInt(1));
			}

			insertOrUpdateTags(fileInfo.getId(), fileInfo.getTags(), stmt, conn);
		} catch (Exception e) {
			throw new StorageException("Failed to insert fileinfo for file" + fileInfo.getFilePath(), e);
		} finally {
			close(conn, stmt, rs);
		}
    }
	
	void updateFileInfo(DOFileInfo fileInfo) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		fileInfo.setDateLastUpdatedDb(new java.util.Date());

		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("UPDATE FILE SET FOLDERPATH = ?, FILENAME = ?, TYPE = ?, DATELASTUPDATEDDB = ?, DATEINSERTEDDB = ?, DATEMODIFIEDOS = ?,"
							+ " THUMBNAILPATH = ?, SIZEBYTE = ?, PLAYCOUNT = ?, ENABLED = ?"
		    		        + " WHERE ID = ?");
			stmt.setString(1, fileInfo.getFolderPath());
			stmt.setString(2, fileInfo.getFileName());
			stmt.setString(3, fileInfo.getType().toString());
			stmt.setTimestamp(4, new Timestamp(fileInfo.getDateLastUpdatedDb().getTime()));
			stmt.setTimestamp(5, new Timestamp(fileInfo.getDateInsertedDb().getTime()));
			stmt.setTimestamp(6, new Timestamp(fileInfo.getDateModifiedOs().getTime()));
			stmt.setString(7, fileInfo.getThumbnailPath());
			stmt.setLong(8, fileInfo.getSize());
			stmt.setInt(9, fileInfo.getPlayCount());
			stmt.setBoolean(10, fileInfo.isActif());
			stmt.setLong(11, fileInfo.getId());
			stmt.executeUpdate();
			
			rs = stmt.getGeneratedKeys();
			if (rs != null && rs.next()) {
				//set the auto increment id as the file id
				fileInfo.setId(rs.getInt(1));
			}
			
			insertOrUpdateTags(fileInfo.getId(), fileInfo.getTags(), stmt, conn);
		} catch (Exception e) {
			throw new StorageException("Failed to insert fileinfo for file" + fileInfo.getFilePath(), e);
		} finally {
			close(conn, stmt, rs);
		}
	}
	
	private void insertOrUpdateTags(long filedId, Map<String, List<String>> tags, PreparedStatement stmt, Connection conn) throws SQLException, StorageException {
		//delete all existing tags
		try {
			stmt = conn.prepareStatement("DELETE FROM FILETAGS WHERE FILEID = ? AND KEY != ?");
			stmt.setLong(1, filedId);
			stmt.setString(2, GENRE_KEY);
		    stmt.executeUpdate();
		} catch (Exception e) {
			throw new StorageException("Failed to delete tags for file with id=" + filedId, e);
		}
		
		// Insert tags
		for (String key : tags.keySet()) {
			List<String> values = tags.get(key);
			for(String value : values) {
				try {
					stmt = conn.prepareStatement("INSERT INTO FILETAGS(FILEID, KEY, VALUE)" 
							+ " VALUES (?, ?, ?)");
					stmt.clearParameters();
					stmt.setLong(1, filedId);
					stmt.setString(2, key);
					stmt.setString(3, value);
					stmt.executeUpdate();
				} catch (Exception e) {
					log.warn("Failed to insert tag=" + key + " with value= " + value + " for file with id=" + filedId, e);
				}
			}
		}
		
	}
	
	List<DOFileInfo> getFileInfo(DOFilter filter, boolean sortAscending, final ConditionType sortField, int maxResults, SortOption sortOption) throws StorageException {
		HashMap<Integer, DOFileInfo> files = new LinkedHashMap<Integer, DOFileInfo>();

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;

		// Get file
		try {
			conn = cp.getConnection();
			
			// create the where clause
			String whereClause = formatEquation(filter);
			
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

			orderByClause += ", " + ConditionType.FILE_FILENAME.toString().replace('_', '.') + " ASC";
			orderByClause += ", " + ConditionType.FILE_DATEINSERTEDDB.toString().replace('_', '.') + " ASC";
			
			if(log.isDebugEnabled()) log.debug(String.format("File query clause: WHERE %s ORDER BY %s", whereClause, orderByClause));

			String statement = "SELECT FILE.ID, FILE.FOLDERPATH, FILE.FILENAME, FILE.TYPE, FILE.SIZEBYTE, FILE.DATELASTUPDATEDDB, FILE.DATEINSERTEDDB" 
			        + ", FILE.DATEMODIFIEDOS, FILE.THUMBNAILPATH, FILE.PLAYCOUNT, FILE.ENABLED" // FILE
			        + ", FILEPLAYS.DATEPLAYEND" //last play
			        + ", FILETAGS.KEY, FILETAGS.VALUE" //TAGS
			        + " FROM FILE" 
			        + " LEFT JOIN FILETAGS ON FILE.ID = FILETAGS.FILEID" 
			        + " LEFT JOIN FILEPLAYS ON FILE.ID = FILEPLAYS.FILEID";
					if(filter.getConditions().size() > 0){
						statement += " WHERE " + whereClause;
					}
					statement += " ORDER BY " + orderByClause;
			stmt = conn.prepareStatement(statement);

			rs = stmt.executeQuery();
			while (rs.next()) {
				DOFileInfo file = new DOFileInfo();
				try {
					int pos = 1;
					file.setId(rs.getInt(pos++));

					if (!files.containsKey(file.getId())) {				
						file.setFolderPath(rs.getString(pos++));
						file.setFileName(rs.getString(pos++));
						file.setType(FileType.valueOf(rs.getString(pos++)));
						file.setSize(rs.getLong(pos++));
						file.setDateLastUpdatedDb(new Date(rs.getTimestamp(pos++).getTime()));
						file.setDateInsertedDb(new Date(rs.getTimestamp(pos++).getTime()));
						file.setDateModifiedOs(new Date(rs.getTimestamp(pos++).getTime()));
						file.setThumbnailPath(rs.getString(pos++));
						file.setPlayCount(rs.getInt(pos++));
						file.setActif(rs.getBoolean(pos++));

						files.put(file.getId(), file);
					}else{
						pos = 11;
					}
					
					try{
						file.addPlayToHistory(new Date(rs.getTimestamp(pos++).getTime()));
					}catch(Exception ex){ }

					DOFileInfo currFile = files.get(file.getId());

					// Genres and Tags
					String tagKey = rs.getString(pos++);
					String tagValue = rs.getString(pos++);
					if (tagKey == null) {
						// do nothing
					} else if (tagKey.equals(GENRE_KEY)) {
						// do nothing
					} else {
						if (currFile.getTags().containsKey(tagKey)) {
							currFile.getTags().remove(tagKey);
						}
						if(currFile.getTags().containsKey(tagKey)) {
							currFile.getTags().get(tagKey).add(tagValue);
						} else {
							List<String> l = new ArrayList<String>();
							l.add(tagValue);
							currFile.getTags().put(tagKey, l);
						}
					}
				} catch (Exception ex) {
					throw new StorageException("Failed to read file from library. This should never happen!!!", ex);
				}
			}
		} catch (SQLException ex) {
			throw new StorageException("Failed to get files", ex);
		} finally {
			close(conn, stmt, rs);
		}
		
		List<DOFileInfo> res = new ArrayList<DOFileInfo>(files.values());		
		
		//Shuffle the list if configured
		if(sortOption == SortOption.Random){
			Collections.shuffle(res);
		}
		
		//limit the number of videos if configured
		if(maxResults > 0 && maxResults < res.size()){
			res = res.subList(0, maxResults);
		}			

		return res;
	}
	
	java.util.Date getFileInfoLastUpdated(String filePath) throws StorageException {		
		Date retVal = new Date(0);

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		//split the file name and path
		int lastPathSeparatorIndex = filePath.lastIndexOf(File.separatorChar) + 1;
		String folderPath = filePath.substring(0, lastPathSeparatorIndex);
		String fileName = filePath.substring(lastPathSeparatorIndex);
		
		//Get date last updated
		try {
			conn = cp.getConnection();
			
			stmt = conn.prepareStatement("SELECT DATELASTUPDATEDDB FROM FILE WHERE FOLDERPATH = ? AND FILENAME = ?");
			stmt.setString(1, folderPath);
			stmt.setString(2, fileName);
			rs = stmt.executeQuery();
			if(rs.next()) {
				retVal = rs.getDate(1);				
			}
		} catch (SQLException ex) {
			throw new StorageException("Failed to get last update date for file " + filePath, ex);
		} finally {
			close(conn, stmt, rs);
		}		
		
		return retVal;
	}
	
	void updateFilePlay(long filedId, int playTimeSec, java.util.Date datePlayEnd) throws StorageException{
		Connection conn = null;
		PreparedStatement stmt = null;
		
		//increment the play count for the file
		try {
			conn = cp.getConnection();			
			stmt = conn.prepareStatement("UPDATE FILE SET PLAYCOUNT = PLAYCOUNT + 1 WHERE ID = ?");
			stmt.setLong(1, filedId);
			stmt.executeUpdate();
		} catch (SQLException ex) {
			throw new StorageException("Failed to increment play count for file with id=" + filedId, ex);
		}
		
		//store play in plays table
		try {
			stmt = conn.prepareStatement("INSERT INTO FILEPLAYS (FILEID, PLAYTIMESEC, DATEPLAYEND)"
	                + "VALUES (?, ?, ?)");
			stmt.setLong(1, filedId);
			stmt.setInt(2, playTimeSec);
			stmt.setTimestamp(3, new Timestamp(datePlayEnd.getTime()));
			stmt.execute();
		} catch (SQLException ex) {
			throw new StorageException(String.format("Failed to inserted play for fileId=%s, playTimeSec=%s, datePlayEnd=%s", filedId, playTimeSec, datePlayEnd), ex);
		} finally {
			close(conn, stmt);
		}
	}

	long getIdForFilePath(String filePath) throws StorageException {
		long retVal = -1;

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String fileName = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1);
		String folderPath = filePath.substring(0, filePath.lastIndexOf(File.separatorChar) + 1);
		
		//increment the play count for the file
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT ID FROM FILE FILE WHERE FILENAME = ? AND FOLDERPATH = ?");
			stmt.setString(1, fileName);
			stmt.setString(2, folderPath);
			rs = stmt.executeQuery();
			if(rs.next()){
				retVal = rs.getLong(1);
			}
		} catch (SQLException ex) {
			throw new StorageException("Failed to get ID for for file path=" + filePath, ex);
		} finally {
			close(conn, stmt, rs);
		}
		
		return retVal;
    }
	
	/*********************************************
	 * 
	 * Protected Methods
	 * 
	 *********************************************/

	protected String formatEquation(DOFilter filter) {
		String retVal = filter.getEquation();
		String[] elems = retVal.split(" ");
		String[] formattedElems = elems.clone();
		
		for(DOCondition condition : filter.getConditions()){
			
			//replace the _ with . in order to use the corrected table in the DB
			String conStr = condition.getType().toString();
			if(conStr.contains("FILE_")){
				conStr = conStr.replaceAll("FILE_", "FILE.");
			}else if(conStr.contains("FILEPLAYS_")){
				conStr = conStr.replaceAll("FILEPLAYS_", "FILEPLAYS.");
			}else if(conStr.contains("VIDEO_")){
				conStr = conStr.replaceAll("VIDEO_", "VIDEO.");
			}else if(conStr.contains("AUDIO_")){
				conStr = conStr.replaceAll("AUDIO_", "AUDIO.");
			}else if(conStr.contains("IMAGE_")){
				conStr = conStr.replaceAll("IMAGE_", "PICTURES.");
			}
			
			//escape some characters			
			String prepCon = sqlFormatString(condition.getCondition());
			
			//convert the units that need to be
			if(condition.getValueType() == ConditionValueType.FILESIZE) {
				prepCon = getConvertedFileSizeUnitString(condition);
			} else if(condition.getValueType() == ConditionValueType.TIMESPAN) {
				prepCon = getConvertedTimespanUnitString(condition);
			}
			
			String querySuffix = "";
			String innerSelect;
			
			//these conditions are special cases
			switch (condition.getType()) {
			case FILE_CONTAINS_TAG:
				innerSelect = "SELECT DISTINCT(FILE.ID)"
								+ " FROM FILE"
							    + " LEFT JOIN FILETAGS ON FILE.ID = FILETAGS.FILEID"
								+ String.format(" WHERE FILETAGS.KEY = '%s' AND FILETAGS.VALUE", condition.getTagName());
				
				conStr = String.format("(FILE.ID = FILETAGS.FILEID AND FILE.ID IN (%s", innerSelect);
				querySuffix = "))";
				break;
			case VIDEO_CONTAINS_VIDEOAUDIO:
				innerSelect = "SELECT DISTINCT(FILE.ID)"
						+ " FROM FILE"
					    + " LEFT JOIN VIDEOAUDIO ON FILE.ID = VIDEOAUDIO.FILEID"
						+ " WHERE VIDEOAUDIO.LANG";
		
				conStr = String.format("(FILE.ID = FILETAGS.FILEID AND FILE.ID IN (%s", innerSelect);
				querySuffix = "))";
				break;
			case VIDEO_CONTAINS_GENRE:
				innerSelect = "SELECT DISTINCT(FILE.ID)"
						+ " FROM FILE"
					    + " LEFT JOIN FILETAGS ON FILE.ID = FILETAGS.FILEID"
						+ String.format(" WHERE FILETAGS.KEY = '%s' AND FILETAGS.VALUE", GENRE_KEY);
		
				conStr = String.format("(FILE.ID = FILETAGS.FILEID AND FILE.ID IN (%s", innerSelect);
				querySuffix = "))";
				break;
			case VIDEO_CONTAINS_SUBTITLES:
				innerSelect = "SELECT DISTINCT(FILE.ID)"
						+ " FROM FILE"
					    + " LEFT JOIN SUBTITLES ON FILE.ID = SUBTITLES.FILEID"
						+ " WHERE SUBTITLES.LANG";
		
				conStr = String.format("(FILE.ID = FILETAGS.FILEID AND FILE.ID IN (%s", innerSelect);
				querySuffix = "))";
				break;
			default:
				// do nothing, the 'normal' cases are handled later
				break;
			}
			
			String originalConString = conStr;
			List<String> notLikeCons;
			switch (condition.getOperator()) {
			case CONTAINS:
				conStr += " LIKE '%" + prepCon + "%'";
				break;
			case DOES_NOT_CONTAIN:
				conStr += " NOT LIKE '%" + prepCon + "%'";
				break;
			case ENDS_WITH:
				conStr += " LIKE '%" + prepCon + "'";
				break;
			case DOES_NOT_END_WITH:
				conStr += " NOT LIKE '%" + prepCon + "'";
				break;
			case IS:
				if (condition.getValueType() == ConditionValueType.STRING) {
					conStr += " LIKE '" + prepCon + "'";
				} else {
					conStr += " = '" + prepCon + "'";
				}
				break;
			case IS_AFTER:
				conStr += " > '" + prepCon + "'";
				break;
			case IS_BEFORE:
				conStr += " < '" + prepCon + "'";
				break;
			case IS_GREATER_THAN:
				conStr += " > '" + prepCon + "'";
				break;
			case IS_LESS_THAN:
				conStr += " < '" + prepCon + "'";
				break;
			case IS_IN_THE_LAST_SEC:
				conStr += " > '" + getConvertedTimespanDateString(condition)
						+ "'";
				break;
			case IS_NOT:
				if (condition.getValueType() == ConditionValueType.STRING) {
					conStr += " NOT LIKE '" + prepCon + "'";
				} else {
					conStr += " <> '" + prepCon + "'";
				}
				break;
			case IS_NOT_IN_THE_LAST_SEC:
				conStr += " < '" + getConvertedTimespanDateString(condition)
						+ "'";
				break;
			case STARTS_WITH:
				notLikeCons = new ArrayList<String>();
				conStr += " LIKE '" + prepCon + "%'";

				// Do some special filtering for certain condition types
				OmitPrefixesConfiguration omitConfig = MediaLibraryConfiguration.getInstance().getOmitPrefixesConfiguration();
				if (omitConfig.isFiltering()
						&& (condition.getType() == ConditionType.VIDEO_NAME || condition.getType() == ConditionType.VIDEO_ORIGINALNAME)) {
					for (String prefix : omitConfig.getPrefixes()) {
						String formattedPrefixes = sqlFormatString(prefix);
						if (Character.isJavaIdentifierStart(formattedPrefixes.charAt(formattedPrefixes.length() - 1))) {
							formattedPrefixes += " ";
						}
						if (prefix.toLowerCase().startsWith(prepCon.toLowerCase())) {
							notLikeCons.add(String.format(" AND (%s NOT LIKE '%s%%' OR %s LIKE '%s%%')",
													originalConString, formattedPrefixes, originalConString, formattedPrefixes + prepCon));
						} else {
							conStr += String.format(" OR %s LIKE '%s%%'", originalConString, formattedPrefixes + prepCon);
						}
					}
					conStr = "(" + conStr + ")";

					if (notLikeCons.size() > 0) {
						for (String notLikeCon : notLikeCons) {
							conStr += notLikeCon;
						}
					}
				}
				break;
			case DOES_NOT_START_WITH:
				notLikeCons = new ArrayList<String>();
				conStr += " NOT LIKE '" + prepCon + "%'";

				// Do some special filtering for certain condition types
				omitConfig = MediaLibraryConfiguration.getInstance().getOmitPrefixesConfiguration();
				if (omitConfig.isFiltering()
						&& (condition.getType() == ConditionType.VIDEO_NAME || condition.getType() == ConditionType.VIDEO_ORIGINALNAME)) {
					for (String prefix : omitConfig.getPrefixes()) {
						String formattedPrefix = sqlFormatString(prefix);
						if (Character.isJavaIdentifierStart(formattedPrefix.charAt(formattedPrefix.length() - 1))) {
							formattedPrefix += " ";
						}
						if (!prefix.toLowerCase().startsWith(prepCon.toLowerCase())) {
							conStr += String.format(" AND %s NOT LIKE '%s%%'", originalConString, formattedPrefix + " " + prepCon);
						}
					}
				}
				break;
			case UNKNOWN:
				log.error("An UNKNOWN condition has been found while formatting the equation. This should never happen!");
				break;
			}

			conStr += querySuffix;
			
			//Update equation
			for(int i = 0; i < elems.length; i++){
				String elem = elems[i];
				String compConditionName = elem;
				
				String openBrackets = "";
				while(compConditionName.length() > 0 && (compConditionName.charAt(0) == '(' || compConditionName.charAt(0) == ' ')){
					if(compConditionName.charAt(0) == ' '){
						continue;
					}
					
					compConditionName = compConditionName.substring(1);
					openBrackets += "(";
				}
				
				String closeBrackets = "";
				while(compConditionName.length() > 0 && (compConditionName.charAt(compConditionName.length() - 1) == ')' || compConditionName.charAt(compConditionName.length() - 1) == ' ')){
					if(compConditionName.charAt(compConditionName.length() - 1) == ' '){
						continue;
					}
					
					compConditionName = compConditionName.substring(0, compConditionName.length() - 1);
					closeBrackets += ")";
				}
				
				if(compConditionName.equals(condition.getName())){
					formattedElems[i] = openBrackets + conStr + closeBrackets;
				}
			}
		}
		
		retVal = "";
		for(int i = 0; i < formattedElems.length; i++){
			retVal += formattedElems[i] + " ";
		}
		
		return retVal.trim();
	}

	private String sqlFormatString(String condition) {
		String res = condition;
		if(res.contains("'")){ 
			res = res.replaceAll("'", "''"); 
		}
		if(res.contains("\\")){ 
			res = res.replaceAll("\\\\", "\\\\\\\\"); 
		}
		return res;
	}

	protected String getConvertedTimespanUnitString(DOCondition condition) {
		int tOriginal = Integer.parseInt(condition.getCondition());
		int res = 0;
		switch(condition.getUnit()) {
		case TIMESPAN_SECONDS:
			res = tOriginal;
			break;
		case TIMESPAN_MINUTES:
			res = tOriginal * 60;
			break;
		case TIMESPAN_HOURS:
			res = tOriginal * 60 * 60;
			break;
		case TIMESPAN_DAYS:
			res = tOriginal * 60 * 60 * 24;
			break;
		case TIMESPAN_WEEKS:
			res = tOriginal * 60 * 60 * 24 * 7;
			break;
		case TIMESPAN_MONTHS:
			res = tOriginal * 60 * 60 * 24 * 30;
			break;
		case TIMESPAN_YEARS:
			res = tOriginal * 60 * 60 * 24 * 365;
			break;
		}
		
		return String.valueOf(res);
	}

	protected String getConvertedTimespanDateString(DOCondition condition) {
		assert(condition != null);
		
    	Calendar c = Calendar.getInstance();
		try{
			int timespan = Integer.parseInt(condition.getCondition());
    		switch(condition.getUnit()){
    			case TIMESPAN_SECONDS:
    		    	c.add(Calendar.SECOND, -timespan);
    				break;
    			case TIMESPAN_MINUTES:
    		    	c.add(Calendar.MINUTE, -timespan);
    				break;
    			case TIMESPAN_HOURS:
    		    	c.add(Calendar.HOUR, -timespan);
    				break;
    			case TIMESPAN_DAYS:
    		    	c.add(Calendar.DAY_OF_MONTH, -timespan);
    				break;
    			case TIMESPAN_WEEKS:
    		    	c.add(Calendar.WEEK_OF_YEAR, -timespan);
    				break;
    			case TIMESPAN_MONTHS:
    		    	c.add(Calendar.MONTH, -timespan);
    				break;
    			case TIMESPAN_YEARS:
    		    	c.add(Calendar.YEAR, -timespan);
    				break;
    		}
    	}catch(Exception ex){
    		log.error("Failed to convert time span to length in seconds. string was '" + condition.getCondition() + "'");
    	}

	    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
    }

	protected String getConvertedFileSizeUnitString(DOCondition condition) {
		assert(condition != null);
		
		long size = 0;
		try{
    		size = Long.parseLong(condition.getCondition());
    		switch(condition.getUnit()){
    			case FILESIZE_BYTE:
    				//do nothing
    				break;
    			case FILESIZE_KILOBYTE:
    				size = size * 1024;
    				break;
    			case FILESIZE_MEGABYTE:
    				size = size * 1024 * 1024;
    				break;
    			case FILESIZE_GIGABYTE:
    				size = size * 1024 * 1024 * 1024;
    				break;
    			case FILESIZE_TERABYTE:
    				size = size * 1024 * 1024 * 1024 * 1024;
    				break;
    		}
    	}catch(Exception ex){
    		log.error("Failed to convert time span to length in seconds. string was '" + condition.getCondition() + "'");
    	}

	    return String.valueOf(size);
    }
}
