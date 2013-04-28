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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.pms.medialibrary.commons.dataobjects.DOFileEntryBase;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFile;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFolder;
import net.pms.medialibrary.commons.dataobjects.DOTemplate;
import net.pms.medialibrary.commons.dataobjects.DOThumbnailPriority;
import net.pms.medialibrary.commons.enumarations.FileDisplayMode;
import net.pms.medialibrary.commons.exceptions.StorageException;

import org.h2.jdbcx.JdbcConnectionPool;

class DBTemplates extends DBBase {
	
	DBTemplates(JdbcConnectionPool cp) {
	    super(cp);
    }
	
	/*********************************************
	 * 
	 * Package Methods
	 * 
	 *********************************************/
	
    void insertTemplate(DOTemplate template, DOFileEntryFolder fileFolder) throws StorageException {    	
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;

		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("INSERT INTO TEMPLATE (NAME) VALUES (?)");		
			stmt.clearParameters();
			stmt.setString(1, template.getName());
			stmt.executeUpdate();

			rs = stmt.getGeneratedKeys();	
			if ( rs != null && rs.next() ) 
			{ 
				template.setId(rs.getInt(1));
			}	    	
	    	insertTemplateEntry(template, fileFolder, conn, stmt);
		} catch (SQLException ex) { 
			throw new StorageException(String.format("Failed to insert view with name for id=%s, name=%s", template.getId(), template.getName()), ex);
		} finally {
			close(conn, stmt, rs);		
		}	
    	
    }

	void deleteTemplate(long templateId) throws StorageException{
    	Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();

			//Delete thumbnail priorities
			stmt = conn.prepareStatement("DELETE FROM FILEENTRYTHUMBNAILPRIORITIES WHERE TEMPLATEID = ?");
			stmt.setLong(1, templateId);
			stmt.executeUpdate();

			//Delete entries
			stmt = conn.prepareStatement("DELETE FROM TEMPLATEENTRY WHERE TEMPLATEID = ?");
			stmt.setLong(1, templateId);
			stmt.executeUpdate();

			//Delete template
			stmt = conn.prepareStatement("DELETE FROM TEMPLATE WHERE ID = ?");
			stmt.setLong(1, templateId);
			stmt.executeUpdate();
		} catch (SQLException ex) {
			throw new StorageException("Failed to delete template entry with id=" + templateId, ex);
		} finally {
			close(conn, stmt);
		}
	}
	
    List<DOTemplate> getAllTemplates() throws StorageException {
		List<DOTemplate> retVal = new ArrayList<DOTemplate>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;

		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT ID, NAME FROM TEMPLATE ORDER BY NAME");
			rs = stmt.executeQuery();
			while (rs.next()) {
				retVal.add(new DOTemplate(rs.getString(2), rs.getInt(1)));
			}

		} catch (SQLException se) {
			throw new StorageException("Failed to get all templates", se);
		}  finally {
			close(conn, stmt, rs);
		}

		return retVal;
    }

	boolean isTemplateIdInUse(long templateId) throws StorageException {
		boolean res = false;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
    		stmt = conn.prepareStatement("SELECT COUNT(FOLDERID)" 
    				+ " FROM MEDIALIBRARYFOLDERS"
    		        + " WHERE TEMPLATEID = ?");
    		stmt.clearParameters();
    		stmt.setLong(1, templateId);
    		
    		rs = stmt.executeQuery();
    		
    		if(rs.next()){
    			long nbUsed = rs.getLong(1);
    			if(nbUsed > 0){
    				res = true;
    			}
    		}
		} catch(SQLException ex){
			throw new StorageException("Failed to get used template", ex);
		}  finally {
			close(conn, stmt, rs);
		}
		
	    return res;
    }

    void updateTemplate(DOTemplate template, DOFileEntryFolder fileFolder) throws StorageException {
    	Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();

			//Update template name
			stmt = conn.prepareStatement("UPDATE TEMPLATE SET NAME = ? WHERE ID = ?");
			stmt.setString(1, template.getName());
			stmt.setLong(2, template.getId());
			stmt.executeUpdate();

			//Delete existing thumbnail priorities
			stmt = conn.prepareStatement("DELETE FROM FILEENTRYTHUMBNAILPRIORITIES" +
			" WHERE TEMPLATEID = ?");
			stmt.setLong(1, template.getId());
			stmt.executeUpdate();

			//Delete existing entries
			stmt = conn.prepareStatement("DELETE FROM TEMPLATEENTRY WHERE TEMPLATEID = ?");
			stmt.setLong(1, template.getId());
			stmt.executeUpdate();
	
			//do the insert after having cleaned
			insertTemplateEntry(template, fileFolder, conn, stmt);
		} catch (SQLException ex) {
			throw new StorageException(String.format("Failed to updated template  with name='%s', id=%s", template.getName(), template.getId()), ex);
		} finally {
			close(conn, stmt);	
		}    	
    }
	
	/*********************************************
	 * 
	 * Private Methods
	 * 
	 *********************************************/

	private void insertTemplateEntry(DOTemplate template, DOFileEntryBase f, Connection conn, PreparedStatement stmt) throws SQLException {
		ResultSet rs = null;
		
		try {
			stmt = conn.prepareStatement("INSERT INTO TEMPLATEENTRY (PARENTID, TEMPLATEID, POSITIONINPARENT, DISPLAYNAMEMASK, ENTRYTYPE, FILEDISPLAYMODE, MAXLINELENGTH, PLUGIN, PLUGINCONFIG) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

			long parentId = -1;
			FileDisplayMode fileDisplayMode = FileDisplayMode.UNKNOWN;

			if (f.getParent() != null) {
				parentId = f.getParent().getId();
			}

			if (f instanceof DOFileEntryFile) {
				fileDisplayMode = ((DOFileEntryFile) f).getFileDisplayMode();
			}

			stmt.clearParameters();
			stmt.setLong(1, parentId);
			stmt.setLong(2, template.getId());
			stmt.setInt(3, f.getPositionInParent());
			stmt.setString(4, f.getDisplayNameMask());
			stmt.setString(5, f.getFileEntryType().toString());
			stmt.setString(6, fileDisplayMode.toString());
			stmt.setInt(7, f.getMaxLineLength());
			stmt.setString(8, f.getPluginName());
			stmt.setString(9, f.getPluginConfigFilePath());
			stmt.executeUpdate();

			rs = stmt.getGeneratedKeys();
			if (rs != null && rs.next()) {
				f.setId(rs.getInt(1));
			}

			// insert thumbnail priorities
			if (f.getThumbnailPriorities() != null) {
				deleteFileEntryThumbnailPriorities(f.getId());
				for (DOThumbnailPriority tp : f.getThumbnailPriorities()) {
					if (!thumbnailPriorityExists(tp, conn, stmt, rs)) {
						stmt = conn.prepareStatement("INSERT INTO THUMBNAILPRIORITIES (PICTUREPATH, THUMBNAILPRIORITYTYPE, SEEKSEC) VALUES (?, ?, ?)");
						stmt.clearParameters();
						stmt.setString(1, tp.getPicturePath());
						stmt.setString(2, tp.getThumbnailPriorityType().toString());
						stmt.setInt(3, tp.getSeekPosition());
						stmt.executeUpdate();

						rs = stmt.getGeneratedKeys();
						if (rs != null && rs.next()) {
							tp.setId(rs.getInt(1));
						}
					}

					stmt = conn.prepareStatement("INSERT INTO FILEENTRYTHUMBNAILPRIORITIES (THUMBNAILPRIORITIESID, FOLDERID, PRIORITYINDEX, TEMPLATEID) VALUES (?, ?, ?, ?)");
					stmt.clearParameters();
					stmt.setLong(1, tp.getId());
					stmt.setLong(2, f.getId());
					stmt.setInt(3, tp.getPriorityIndex());
					stmt.setLong(4, template.getId());
					stmt.executeUpdate();
				}
			}
		} finally {
			close(rs);		
		}

		// Insert the child entries
		if (f instanceof DOFileEntryFolder) {
			for (DOFileEntryBase ff : ((DOFileEntryFolder) f).getChildren()) {
				insertTemplateEntry(template, ff, conn, stmt);
			}
		}
	}

	private void deleteFileEntryThumbnailPriorities(long fileEntryId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("DELETE FROM FILEENTRYTHUMBNAILPRIORITIES WHERE FOLDERID = ?");
			stmt.setLong(1, fileEntryId);
			stmt.executeUpdate();
		} finally {
			close(conn, stmt);
		}
	}
	
	private boolean thumbnailPriorityExists(DOThumbnailPriority tp, Connection conn, PreparedStatement stmt, ResultSet rs) throws SQLException {
		boolean found = false;
			
		String statement = "SELECT ID" +
        			" FROM THUMBNAILPRIORITIES" +
        			" WHERE PICTUREPATH=? AND THUMBNAILPRIORITYTYPE=? AND SEEKSEC=?";
		stmt = conn.prepareStatement(statement);
		stmt.clearParameters();
		stmt.setString(1, tp.getPicturePath());
		stmt.setString(2, tp.getThumbnailPriorityType().toString());
		stmt.setInt(3, tp.getSeekPosition());
		rs = stmt.executeQuery();
			
		if(rs.next()){
			tp.setId(rs.getLong(1));
			found = true;
		}
		
		return found;
    }
}