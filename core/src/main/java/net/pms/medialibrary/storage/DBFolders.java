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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.pms.medialibrary.commons.dataobjects.DOCondition;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.dataobjects.DOFolder;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.dataobjects.DOSpecialFolder;
import net.pms.medialibrary.commons.dataobjects.DOThumbnailPriority;
import net.pms.medialibrary.commons.dataobjects.FileDisplayProperties;
import net.pms.medialibrary.commons.enumarations.ConditionOperator;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.ConditionUnit;
import net.pms.medialibrary.commons.enumarations.ConditionValueType;
import net.pms.medialibrary.commons.enumarations.FileDisplayType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.FolderType;
import net.pms.medialibrary.commons.enumarations.SortOption;
import net.pms.medialibrary.commons.enumarations.ThumbnailPrioType;
import net.pms.medialibrary.commons.exceptions.StorageException;
import net.pms.plugins.DlnaTreeFolderPlugin;
import net.pms.plugins.PluginsFactory;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DBFolders extends DBBase {
	private static final Logger log = LoggerFactory.getLogger(DBFolders.class);
	
	DBFolders(JdbcConnectionPool cp) {
	    super(cp);
    }
	
	/*********************************************
	 * 
	 * Package Methods
	 * 
	 *********************************************/

	void insertFolder(DOFolder f) throws StorageException {
		assert(f != null);
		
		Connection conn = null;
		PreparedStatement stmt = null;	
		ResultSet rs = null;
		
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("INSERT INTO FOLDERS (PARENTID, NAME, POSITIONINPARENT, TYPE) VALUES (?, ?, ?, ?)");
        	stmt.clearParameters();
        	stmt.setLong(1, f.getParentId());
        	stmt.setString(2, f.getName());
        	stmt.setInt(3, f.getPositionInParent());
        	stmt.setString(4, f.getFolderType().toString());
			stmt.execute();
			
			//set the id of the referenced folder
			rs = stmt.getGeneratedKeys();	
			if (rs != null && rs.next()) 
			{ 
				f.setId(rs.getInt(1));
			}
			
			//load additional data depending on the folder type
			if(f instanceof DOMediaLibraryFolder) {
				insertMediaLibraryFolder((DOMediaLibraryFolder) f, conn);
			} else if(f instanceof DOSpecialFolder){
				insertSpecialFolder((DOSpecialFolder)f, stmt, conn);
			}
		} catch (SQLException se) {
			throw new StorageException(String.format("Error inserting folder " + f.getName()), se);
		} finally {
			close(conn, stmt, rs);
		}
	}
	
	void deleteFolder(long id) throws StorageException {		
		Connection conn = null;
		PreparedStatement stmt = null;		
		
		try {
			conn = cp.getConnection();
			deleteFolder(id, conn);			
		} catch (SQLException ex) {
			throw new StorageException("Failed to delete folder with id=" + id, ex);
		} finally {
			close(conn, stmt);
		}
	}

	DOMediaLibraryFolder getMediaLibraryFolder(long initialFolderId, int depth) throws StorageException {
		DOMediaLibraryFolder rootFolder = null;		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try{
			conn = cp.getConnection();			
			rootFolder = (DOMediaLibraryFolder) getFolder(initialFolderId, conn, stmt);
        	if(depth != MediaLibraryStorage.ALL_CHILDREN){ depth--; }
    		rootFolder.setChildFolders(populateChildFolders(rootFolder, depth, conn, stmt));
    	}catch(Exception ex){
			throw new StorageException(String.format("Failed to get folder with initialFolderId=%s, depth=%s", initialFolderId, depth), ex);
    	} finally {
			close(conn, stmt);
		}
    		
		return rootFolder;
	}

    void updateFolderDisplayName(long folderId, String displayName) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;

		
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("UPDATE FOLDERS" +
											" SET NAME = ?" +
											" WHERE ID = ?");
			stmt.setString(1, displayName);
			stmt.setLong(2, folderId);
			stmt.executeUpdate();
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to get folder with folderId=%s, displayName=%s", folderId, displayName), se);
		} finally {
			close(conn, stmt);
		}	
    }

	void updateFolder(DOFolder f) throws StorageException {		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("UPDATE FOLDERS" +
											" SET PARENTID = ?, NAME = ?, POSITIONINPARENT = ?, TYPE = ?" +
											" WHERE ID = ?");
			stmt.setLong(1, f.getParentId());
			stmt.setString(2, f.getName());
			stmt.setInt(3, f.getPositionInParent());
			stmt.setString(4, f.getFolderType().toString());
			stmt.setLong(5, f.getId());
			stmt.executeUpdate();

			//update additional data depending on the folder type
			if(f instanceof DOMediaLibraryFolder){
				updateMediaLibraryFolder((DOMediaLibraryFolder) f, stmt, conn);
			} else if(f instanceof DOSpecialFolder){
				updateSpecialFolder((DOSpecialFolder)f, stmt, conn);
			}
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to update folder with folderId=%s, displayName=%s", f.getId(), f.getName()), se);
		} finally {
			close(conn, stmt);
		}	
	}
	
	void updateFolderLocation(long id, long parentId, int locationInParent) throws StorageException{
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("UPDATE FOLDERS" +
											" SET PARENTID = ?, POSITIONINPARENT = ?" +
											" WHERE ID = ?");
			stmt.clearParameters();
			stmt.setLong(1, parentId);
			stmt.setInt(2, locationInParent);
			stmt.setLong(3, id);
			stmt.executeUpdate();
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to updateFolderLocation for folder with id=%s, parentId=%s, locationInParent=%s", id, parentId, locationInParent), se);
		} finally {
			close(conn, stmt);
		}	
	}
	
	/*********************************************
	 * 
	 * Private Methods
	 * 
	 *********************************************/
	
	private void deleteFolder(long id, Connection conn) throws SQLException, StorageException {
		PreparedStatement stmt = null;
    		//Make the call recursive to delete all the dependent folders that would be without a parent
    		List<Integer> children = getChildFolderIds(id, conn, stmt);
    		for(Integer child : children){
    			deleteFolder(child, conn);
    		}
    		
    		DOFolder f = getFolder(id, conn, stmt);
    		
    		if(f instanceof DOMediaLibraryFolder) {
        		deleteFolderConditions(id, conn, stmt);
        		deleteMediaLibraryFolderThumbnailPriorities(id, conn, stmt);
    		
    			stmt = conn.prepareStatement("DELETE FROM MEDIALIBRARYFOLDERS WHERE FOLDERID = ?");
    			stmt.setLong(1, id);
    			stmt.executeUpdate();
    		} else if(f instanceof DOSpecialFolder){
    			File configFile = new File(((DOSpecialFolder)f).getConfigFilePath());
    			if(configFile.exists()){
    				configFile.delete();
    			}
    			
    			stmt = conn.prepareStatement("DELETE FROM SPECIALFOLDERS WHERE FOLDERID = ?");
    			stmt.setLong(1, id);
    			stmt.executeUpdate();		
    		}

			stmt = conn.prepareStatement("DELETE FROM FOLDERS WHERE ID = ?");
			stmt.setLong(1, id);
			stmt.executeUpdate();
	}
	
	private void updateSpecialFolder(DOSpecialFolder f, PreparedStatement stmt, Connection conn) throws SQLException {
		stmt = conn.prepareStatement("UPDATE SPECIALFOLDERS" + 
                        				" SET CLASSNAME = ?, SAVEFILEPATH = ?" + 
                        				" WHERE FOLDERID = ?");
		stmt.clearParameters();
		stmt.setString(1, f.getSpecialFolderImplementation().getClass().getName());
		stmt.setString(2, f.getConfigFilePath());
		stmt.setLong(3, f.getId());
		stmt.executeUpdate();
	}

	private void updateMediaLibraryFolder(DOMediaLibraryFolder f, PreparedStatement stmt, Connection conn) throws SQLException{
		ResultSet rs = null;
		FileDisplayProperties tmpDisplayProps = f.getDisplayProperties();

		try{
    		stmt = conn.prepareStatement("UPDATE MEDIALIBRARYFOLDERS" +
    										" SET EQUATION = ?, INHERITSCONDITIONS = ?, FILETYPE = ?, DISPLAYNAMEMASK = ?, TEMPLATEID = ?, DISPLAYITEMS = ?, DISPLAYTYPE = ?," +
    										" INHERITSSORT = ?, INHERITDISPLAYFILES = ?, SORTASCENDING = ?, SORTTYPE = ?, MAXFILES = ?, SORTOPTION = ?" +
    										" WHERE FOLDERID = ?");
    		stmt.clearParameters();
    		stmt.setString(1, f.getFilter().getEquation());
    		stmt.setBoolean(2, f.isInheritsConditions());
    		stmt.setString(3, f.getFileType().toString());
    		stmt.setString(4, tmpDisplayProps.getDisplayNameMask());
    		stmt.setLong(5, tmpDisplayProps.getTemplate().getId());
    		stmt.setBoolean(6, f.isDisplayItems());
    		stmt.setString(7, tmpDisplayProps.getFileDisplayType().toString());
    		
    		stmt.setBoolean(8, f.isInheritSort());	
    		stmt.setBoolean(9, f.isInheritDisplayFileAs());	
    		stmt.setBoolean(10, tmpDisplayProps.isSortAscending());
    		stmt.setString(11, tmpDisplayProps.getSortType().toString());
    		stmt.setLong(12, f.getMaxFiles());
    		stmt.setString(13, tmpDisplayProps.getSortOption().toString());
    		
    		stmt.setLong(14, f.getId());
    		stmt.executeUpdate();
    		
    		//delete and update conditions
    		deleteFolderConditions(f.getId(), conn, stmt);
    		for(DOCondition con:f.getFilter().getConditions()){
    			try{
    				stmt = conn.prepareStatement("INSERT INTO CONDITIONS (FOLDERID, NAME, TYPE, OPERATOR, CONDITION, VALUETYPE, UNIT, TAGNAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    				stmt.clearParameters();
    				stmt.setLong(1, f.getId());
    				stmt.setString(2, con.getName());
    				stmt.setString(3, con.getType().toString());
    				stmt.setString(4, con.getOperator().toString());
    				stmt.setString(5, con.getCondition());
    				stmt.setString(6, con.getValueType().toString());
    				stmt.setString(7, con.getUnit().toString());
    				stmt.setString(8, con.getTagName());
    				stmt.executeUpdate();	
    			} catch (SQLException se) {
    				log.error(String.format("Error updating condition '%s' for folder  with id=%s, name=%s", con.getName(), f.getId(), f.getName()), se);
    			}
    		}
    
    		//update thumbnail display properties
    		if(f.getDisplayProperties().getThumbnailPriorities() != null){
    			deleteMediaLibraryFolderThumbnailPriorities(f.getId(), conn, stmt);
    			for(DOThumbnailPriority tp : f.getDisplayProperties().getThumbnailPriorities()){
    				if(thumbnailPriorityExists(tp, conn, stmt)){
            			//Thumbnail priorities already exist. Don't create a new entry
    				} else {
    					stmt = conn.prepareStatement("INSERT INTO THUMBNAILPRIORITIES (PICTUREPATH, THUMBNAILPRIORITYTYPE, SEEKSEC) VALUES (?, ?, ?)");
    					stmt.clearParameters();
    					stmt.setString(1, tp.getPicturePath());
    					stmt.setString(2, tp.getThumbnailPriorityType().toString());
    					stmt.setInt(3, tp.getSeekPosition());
    					stmt.executeUpdate();
    					
            			rs = stmt.getGeneratedKeys();	
            			if (rs != null && rs.next()) 
            			{ 
            				tp.setId(rs.getInt(1));
            			}
    				}
    				
    				stmt = conn.prepareStatement("INSERT INTO FOLDERTHUMBNAILPRIORITIES (THUMBNAILPRIORITIESID, FOLDERID, PRIORITYINDEX) VALUES (?, ?, ?)");
    				stmt.clearParameters();
    				stmt.setLong(1, tp.getId());
    				stmt.setLong(2, f.getId());
    				stmt.setInt(3, tp.getPriorityIndex());
    				stmt.executeUpdate();
    			}
    		} 
		} finally {
			close(rs);
		}
		
	}

	private void insertSpecialFolder(DOSpecialFolder f, PreparedStatement stmt, Connection conn) throws SQLException {
		stmt = conn.prepareStatement("INSERT INTO SPECIALFOLDERS (FOLDERID, CLASSNAME, SAVEFILEPATH) VALUES (?, ?, ?)");
		stmt.clearParameters();
		stmt.setLong(1, f.getId());
		stmt.setString(2, f.getSpecialFolderImplementation().getClass().getName());
		stmt.setString(3, f.getConfigFilePath());
		stmt.executeUpdate();
    }

	private void insertMediaLibraryFolder(DOMediaLibraryFolder f, Connection conn) throws SQLException{
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try{
    		stmt = conn.prepareStatement("INSERT INTO MEDIALIBRARYFOLDERS (EQUATION, INHERITSCONDITIONS, FILETYPE, DISPLAYITEMS, INHERITSSORT" +
    				", INHERITDISPLAYFILES, DISPLAYNAMEMASK, TEMPLATEID, DISPLAYTYPE, SORTASCENDING, SORTTYPE, FOLDERID, MAXFILES, SORTOPTION) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    		stmt.clearParameters();
    		stmt.setString(1, f.getFilter().getEquation());
    		stmt.setBoolean(2, f.isInheritsConditions());
    		stmt.setString(3, f.getFileType().toString());
    		stmt.setBoolean(4, f.isDisplayItems());
    		stmt.setBoolean(5, f.isInheritSort());
    		stmt.setBoolean(6, f.isInheritDisplayFileAs());
    		
    		FileDisplayProperties fdp = f.getDisplayProperties();
    		stmt.setString(7, fdp.getDisplayNameMask());
    		stmt.setLong(8, fdp.getTemplate().getId());
    		stmt.setString(9, fdp.getFileDisplayType().toString());
    		stmt.setBoolean(10, fdp.isSortAscending());
    		stmt.setString(11, fdp.getSortType().toString());
    		stmt.setLong(12, f.getId());
    		stmt.setLong(13, f.getMaxFiles());
    		stmt.setString(14, fdp.getSortOption().toString());
    		stmt.executeUpdate();
    		
    		//update conditions (first delete, then insert)
    		for(DOCondition con:f.getFilter().getConditions()){	
    			stmt = conn.prepareStatement("INSERT INTO CONDITIONS (FOLDERID, NAME, TYPE, OPERATOR, CONDITION, VALUETYPE, UNIT, TAGNAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    			stmt.clearParameters();
    			stmt.setLong(1, f.getId());
    			stmt.setString(2, con.getName());
    			stmt.setString(3, con.getType().toString());
    			stmt.setString(4, con.getOperator().toString());
    			stmt.setString(5, con.getCondition());
    			stmt.setString(6, con.getValueType().toString());
    			stmt.setString(7, con.getUnit().toString());
    			stmt.setString(8, con.getTagName());
    			stmt.executeUpdate();
    		}
    
    		//update thumbnail display properties
    		if(f.getDisplayProperties().getThumbnailPriorities() != null){
    			deleteMediaLibraryFolderThumbnailPriorities(f.getId(), conn, stmt);
    			for(DOThumbnailPriority tp : f.getDisplayProperties().getThumbnailPriorities()){
    				if(!thumbnailPriorityExists(tp, conn, stmt)){
        				stmt = conn.prepareStatement("INSERT INTO THUMBNAILPRIORITIES (PICTUREPATH, THUMBNAILPRIORITYTYPE, SEEKSEC) VALUES (?, ?, ?)");
                		stmt.clearParameters();
                		stmt.setString(1, tp.getPicturePath());
                		stmt.setString(2, tp.getThumbnailPriorityType().toString());
                		stmt.setInt(3, tp.getSeekPosition());
                		stmt.executeUpdate();
    
            			rs = stmt.getGeneratedKeys();	
            			if (rs != null && rs.next()) 
            			{ 
            				tp.setId(rs.getInt(1));
            			}
    				}
    				
    				stmt = conn.prepareStatement("INSERT INTO FOLDERTHUMBNAILPRIORITIES (THUMBNAILPRIORITIESID, FOLDERID, PRIORITYINDEX) VALUES (?, ?, ?)");
            		stmt.clearParameters();
            		stmt.setLong(1, tp.getId());
            		stmt.setLong(2, f.getId());
            		stmt.setInt(3, tp.getPriorityIndex());
            		stmt.executeUpdate();
    			}
    		}		
		} finally {
			close(stmt, rs);
		}
	}

	private int deleteMediaLibraryFolderThumbnailPriorities(long mediaLibraryFolderId, Connection conn, PreparedStatement stmt) throws SQLException {
    		stmt = conn.prepareStatement("DELETE FROM FOLDERTHUMBNAILPRIORITIES WHERE FOLDERID = ?");
    		stmt.clearParameters();
    		stmt.setLong(1, mediaLibraryFolderId);
    		return stmt.executeUpdate();
	}
	
	private boolean thumbnailPriorityExists(DOThumbnailPriority tp, Connection conn, PreparedStatement stmt) throws SQLException {
		boolean found = false;
		ResultSet rs = null;
		
		try {
			stmt = conn.prepareStatement("SELECT ID" +
    										" FROM THUMBNAILPRIORITIES" +
    										" WHERE PICTUREPATH = ? AND THUMBNAILPRIORITYTYPE = ? AND SEEKSEC = ?");
			stmt.clearParameters();
			stmt.setString(1, tp.getPicturePath());
			stmt.setString(2, tp.getThumbnailPriorityType().toString());
			stmt.setInt(3, tp.getSeekPosition());
			rs = stmt.executeQuery();
			
			if(rs.next()){
				tp.setId(rs.getLong(1));
				found = true;
			}
		} finally {
			close(rs);
		}	
		
		return found;
    }

	private int deleteFolderConditions(long mediaLibraryFolderId, Connection conn, PreparedStatement stmt) throws SQLException {
		stmt = conn.prepareStatement("DELETE FROM CONDITIONS WHERE FOLDERID = ?");
		stmt.clearParameters();
		stmt.setLong(1, mediaLibraryFolderId);
		return stmt.executeUpdate();
	}

	private List<Integer> getChildFolderIds(long id, Connection conn, PreparedStatement stmt) throws SQLException {
		List<Integer> retVal = new ArrayList<Integer>();
		ResultSet rs = null;

		try {
			stmt = conn.prepareStatement("SELECT ID"
											+ " FROM FOLDERS"
											+ " WHERE PARENTID = ?");
			stmt.clearParameters();
			stmt.setLong(1, id);
			rs = stmt.executeQuery();
			while (rs.next()) {
				retVal.add(rs.getInt(1));
			}
		} finally {
			close(rs);
		}

		return retVal;
	}

	private DOFolder getFolder(long folderId, Connection conn, PreparedStatement stmt) throws SQLException, StorageException{
		DOFolder folder = null;		
		ResultSet rs = null;
		
		try {
			stmt = conn.prepareStatement("SELECT ID, PARENTID, NAME, TYPE, POSITIONINPARENT" +
					" FROM FOLDERS" +
					" WHERE ID = ?");
            stmt.clearParameters();
            stmt.setLong(1, folderId);
            rs = stmt.executeQuery();
			if(rs.next()) {
				folder = new DOFolder();
				folder.setId(rs.getLong(1));
				folder.setParentId(rs.getLong(2));
				folder.setName(rs.getString(3));
				folder.setFolderType(FolderType.valueOf(rs.getString(4)));
				folder.setPositionInParent(rs.getInt(5));
				
				switch(folder.getFolderType()){
					case MEDIALIBRARY:
						folder = getMediaLibraryFolder(folder, stmt, conn);
						break;
					case SPECIAL:
						folder = getSpecialFolder(folder, stmt, conn);
						break;
					default:
						log.warn(String.format("Unhandled folder type received (%s). This should never happen!", folder.getFolderType()));
						break;
				}
			}
			
		} catch(SQLException se) {
			throw new StorageException("Failed to get folder with id=" + folderId, se);
		} finally {
			close(rs);
		}	
		
		return folder;
	}
	
	private DOSpecialFolder getSpecialFolder(DOFolder f, PreparedStatement stmt, Connection conn) throws SQLException {
	    DOSpecialFolder res = new DOSpecialFolder();
		res.setName(f.getName());
		res.setFolderType(f.getFolderType());
		res.setId(f.getId());
		res.setParentId(f.getParentId());
		res.setPositionInParent(f.getPositionInParent());

		ResultSet rs = null;
		try {
    		//Get conditions for folder
    		stmt = conn.prepareStatement("SELECT CLASSNAME, SAVEFILEPATH" +
    										" FROM SPECIALFOLDERS" +
    										" WHERE FOLDERID = ?");
    		stmt.clearParameters();
    		stmt.setLong(1, f.getId());
    		rs = stmt.executeQuery();
    		
    		if (rs.next()) {
    			String className = rs.getString(1);
    			res.setConfigFilePath(rs.getString(2));
    			
    			//load and configure the special folder
    			DlnaTreeFolderPlugin sf = PluginsFactory.getDlnaTreeFolderPluginByName(className);
    			sf.setDisplayName(f.getName());
    			
    			File configFile = new File(res.getConfigFilePath());    			
    			try {
    				//create the file if it doesn't exist
    				if(!configFile.exists()) {
    					configFile.createNewFile();
    					log.warn(String.format("A new configuration file (%s) had to be created because the configured one didn't exist", configFile.getAbsoluteFile()));
    				}
    				
    				//load the config
    				sf.loadInstanceConfiguration(res.getConfigFilePath());
    			} catch(Exception ex) {
    				log.error(String.format("Failed to load configuration file '%s' for SpecialFolder of type '%s'", res.getConfigFilePath(), className), ex);
    			} catch(Throwable t) {
    				//catch throwable for every external call to avoid a plugin crashing pms
    				log.error(String.format("Failed to load configuration file '%s' for SpecialFolder of type '%s'", res.getConfigFilePath(), className), t);
    			}
    			
    			res.setSpecialFolderImplementation(sf);
    		}
		} finally {
			close(rs);			
		}
		
	    return res;
    }

	private DOMediaLibraryFolder getMediaLibraryFolder(DOFolder f, PreparedStatement stmt, Connection conn) throws SQLException{
		DOMediaLibraryFolder res = new DOMediaLibraryFolder();
		res.setName(f.getName());
		res.setFolderType(f.getFolderType());
		res.setId(f.getId());
		res.setParentId(f.getParentId());
		res.setPositionInParent(f.getPositionInParent());
		
		ResultSet rs = null;
		
		try {
    		//Get conditions for folder
    		stmt = conn.prepareStatement("SELECT TYPE, OPERATOR, CONDITION, NAME, VALUETYPE, UNIT, TAGNAME" +
    										" FROM CONDITIONS" +
    										" WHERE FOLDERID = ?");
    		stmt.clearParameters();
    		stmt.setLong(1, f.getId());
    		rs = stmt.executeQuery();
    		
    		List<DOCondition> conditions = new ArrayList<DOCondition>();
    		while (rs.next()) {
    			DOCondition condition = new DOCondition();
    			condition.setType(ConditionType.valueOf(rs.getString(1)));
    			condition.setOperator(ConditionOperator.valueOf(rs.getString(2)));
    			condition.setCondition(rs.getString(3));
    			condition.setName(rs.getString(4));
    			condition.setValueType(ConditionValueType.valueOf(rs.getString(5)));
    			condition.setUnit(ConditionUnit.valueOf(rs.getString(6)));
    			condition.setTagName(rs.getString(7));
    			
    			conditions.add(condition);
    		}
    
    		//Get thumbnail priorities for folder
    		stmt = conn.prepareStatement("SELECT PICTUREPATH, THUMBNAILPRIORITYTYPE, PRIORITYINDEX, SEEKSEC, ID" +
    										" FROM FOLDERTHUMBNAILPRIORITIES, THUMBNAILPRIORITIES" +
    										" WHERE FOLDERID = ? AND THUMBNAILPRIORITIESID = ID");
    		stmt.clearParameters();
    		stmt.setLong(1, f.getId());
    		rs = stmt.executeQuery();
    		List<DOThumbnailPriority> props = new ArrayList<DOThumbnailPriority>();
    		while (rs.next()) {
    			DOThumbnailPriority prio = new DOThumbnailPriority();
    			prio.setPicturePath(rs.getString(1));
    			prio.setThumbnailPriorityType(ThumbnailPrioType.valueOf(rs.getString(2)));
    			prio.setPriorityIndex(rs.getInt(3));
    			prio.setSeekPosition(rs.getInt(4));
    			prio.setId(rs.getLong(5));
    			
    			props.add(prio);
    		}
    		
    		stmt = conn.prepareStatement("SELECT EQUATION, INHERITSCONDITIONS, FILETYPE, INHERITSSORT, INHERITDISPLAYFILES, DISPLAYITEMS," +
    										" DISPLAYNAMEMASK, TEMPLATEID, DISPLAYTYPE, SORTASCENDING, SORTTYPE, MAXFILES, SORTOPTION" +
    										" FROM MEDIALIBRARYFOLDERS" +
    										" WHERE FOLDERID = ?");
    		stmt.clearParameters();
    		stmt.setLong(1, res.getId());
    		rs = stmt.executeQuery();
    		if(rs.next()) {
    			res.setFilter(new DOFilter(rs.getString(1), conditions));
    			res.setInheritsConditions(rs.getBoolean(2));
    			res.setFileType(FileType.valueOf(rs.getString(3)));
    			res.setInheritSort(rs.getBoolean(4));
    			res.setInheritDisplayFileAs(rs.getBoolean(5));
    			res.setDisplayItems(rs.getBoolean(6));
    			
    			FileDisplayProperties tmpDisplayProps = new FileDisplayProperties();
    			tmpDisplayProps.setThumbnailPriorities(props);
    			tmpDisplayProps.setDisplayNameMask(rs.getString(7));
    			tmpDisplayProps.getTemplate().setId(rs.getInt(8));
    			
    			FileDisplayType fdt = FileDisplayType.valueOf(rs.getString(9));
    			tmpDisplayProps.setFileDisplayType(fdt);
    			tmpDisplayProps.setSortAscending(rs.getBoolean(10));
    			ConditionType sortType;
    			sortType = ConditionType.valueOf(rs.getString(11));
    			tmpDisplayProps.setSortType(sortType);
    			res.setMaxFiles(rs.getInt(12));
    			tmpDisplayProps.setSortOption(SortOption.valueOf(rs.getString(13)));
    			
    			res.setDisplayProperties(tmpDisplayProps);
    		}
    		
    		if(res.getDisplayProperties().getFileDisplayType() == FileDisplayType.FOLDER){
    			long templateId = res.getDisplayProperties().getTemplate().getId();
    			
    			//Get template name
    			stmt = conn.prepareStatement("SELECT NAME FROM TEMPLATE WHERE ID = ?");
    			stmt.clearParameters();
    			stmt.setLong(1, templateId);
    			rs = stmt.executeQuery();
    			if(rs.next()) {
    				res.getDisplayProperties().getTemplate().setName(rs.getString(1));
    			}
    		}
		} finally {
			close(rs);		
		}
		
		return res;
	}
	
	private List<DOFolder> populateChildFolders(DOFolder folder, int depth, Connection conn, PreparedStatement stmt) throws SQLException, StorageException{
		if(depth == 0){
			return new ArrayList<DOFolder>();
		}
		
		List<DOFolder> tmpList = new ArrayList<DOFolder>();
		if(folder instanceof DOMediaLibraryFolder){
    		for(int childId : getChildFolderIds(folder.getId(), conn, stmt)){
    			DOFolder tmpFolder = getFolder(childId, conn, stmt);
    			tmpFolder.setParentFolder((DOMediaLibraryFolder) folder);
    			tmpFolder.setParentId(folder.getId());
    			if(depth != MediaLibraryStorage.ALL_CHILDREN){ depth--; }
    			if(tmpFolder instanceof DOMediaLibraryFolder){
    				((DOMediaLibraryFolder)tmpFolder).setChildFolders(populateChildFolders(tmpFolder, depth, conn, stmt));
    			}
    			tmpList.add(tmpFolder);
    		}
		}
		
		//sort by position in parent
		Collections.sort(tmpList, new Comparator<DOFolder>() {
			@Override
			public int compare(DOFolder o1, DOFolder o2) {
				return ((Integer)o1.getPositionInParent()).compareTo(o2.getPositionInParent());
			}
		});
		return tmpList;
	}
}