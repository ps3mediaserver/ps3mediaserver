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
import net.pms.medialibrary.commons.dataobjects.DOFileEntryInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryPlugin;
import net.pms.medialibrary.commons.dataobjects.DOThumbnailPriority;
import net.pms.medialibrary.commons.enumarations.FileDisplayMode;
import net.pms.medialibrary.commons.enumarations.FileDisplayType;
import net.pms.medialibrary.commons.enumarations.ThumbnailPrioType;
import net.pms.medialibrary.commons.exceptions.StorageException;
import net.pms.plugins.FileDetailPlugin;
import net.pms.plugins.PluginsFactory;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Package class used to structure code for MediaLibraryStorage
 */
class DBFileFolder extends DBBase {
	private static final Logger log = LoggerFactory.getLogger(DBFileFolder.class);
	
	DBFileFolder(JdbcConnectionPool cp) {
	    super(cp);
    }
	
	/*********************************************
	 * 
	 * Package Methods
	 * 
	 *********************************************/

    DOFileEntryFolder getFileFolder(long templateId) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		DOFileEntryFolder root = null;
		try {
			conn = cp.getConnection();
			root = getFileFolder(templateId, null, conn, stmt);    
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to retrieve file folder for template with id=%s", templateId), se);
		} finally {
			close(conn, stmt);
		}
		
		return root;
	}
	
	/*********************************************
	 * 
	 * Private Methods
	 * 
	 *********************************************/
	
	private DOFileEntryFolder getFileFolder(long templateId, DOFileEntryFolder baseFolder, Connection conn, PreparedStatement stmt) throws SQLException {
		long parentId = -1;
		if (baseFolder != null) {
			parentId = baseFolder.getId();
		}

		stmt = conn.prepareStatement("SELECT ID, POSITIONINPARENT, DISPLAYNAMEMASK, ENTRYTYPE, FILEDISPLAYMODE, MAXLINELENGTH, PLUGIN, PLUGINCONFIG" 
				+ " FROM TEMPLATEENTRY"
		        + " WHERE TEMPLATEID = ? AND PARENTID = ?" 
		        + " ORDER BY POSITIONINPARENT ASC");
		stmt.clearParameters();
		stmt.setLong(1, templateId);
		stmt.setLong(2, parentId);

		ResultSet rs = null;
		try {
			rs = stmt.executeQuery();
			while (rs.next()) {
				long id = rs.getLong(1);
				int posInParent = rs.getInt(2);
				String displayNameMask = rs.getString(3);

				FileDisplayType displayType = FileDisplayType.valueOf(FileDisplayType.class, rs.getString(4));
				List<DOThumbnailPriority> thumbnailPriorities = getFileEntryThumbnailPriorities(id, conn, stmt);
				int maxLineLength = rs.getInt(6);
				String pluginName = rs.getString(7);
				String pluginConfigFilePath = rs.getString(8);

				switch (displayType) {
					case FILE:
						FileDisplayMode fileDisplayMode = FileDisplayMode.valueOf(FileDisplayMode.class, rs.getString(5));
						DOFileEntryFile fef = new DOFileEntryFile(fileDisplayMode, id, baseFolder, posInParent, displayNameMask, thumbnailPriorities, maxLineLength);
						baseFolder.getChildren().add(fef);
						break;
					case INFO:
						DOFileEntryInfo fei = new DOFileEntryInfo(id, baseFolder, posInParent, displayNameMask, thumbnailPriorities, maxLineLength);
						baseFolder.getChildren().add(fei);
						break;
					case FOLDER:
						DOFileEntryFolder feff = new DOFileEntryFolder(new ArrayList<DOFileEntryBase>(), id, baseFolder, posInParent, displayNameMask, thumbnailPriorities, maxLineLength);
						if (baseFolder == null) {
							baseFolder = feff;
							baseFolder = getFileFolder(templateId, baseFolder, conn, stmt);
						} else {
							feff = getFileFolder(templateId, feff, conn, stmt);
							baseFolder.getChildren().add(feff);
						}
						break;
					case PLUGIN:
		    			try {
		    				FileDetailPlugin plugin = PluginsFactory.getFileDetailPluginByName(pluginName);
							DOFileEntryPlugin fep = new DOFileEntryPlugin(id, baseFolder, posInParent, displayNameMask, thumbnailPriorities, maxLineLength, plugin, pluginConfigFilePath);
							baseFolder.getChildren().add(fep);
		    			} catch(Exception ex) {
		    				log.error(String.format("Failed to load plugin %s", pluginName), ex);
		    			}
						break;
				default:
					log.warn(String.format("Unhandled display type received (%s). This should never happen!", displayType));
					break;
				}
			}
		} finally {
			close(rs);
		}

		return baseFolder;
	}

	private List<DOThumbnailPriority> getFileEntryThumbnailPriorities(long id, Connection conn, PreparedStatement stmt) throws SQLException {
		ResultSet rs = null;
		List<DOThumbnailPriority> props = new ArrayList<DOThumbnailPriority>();
		
		try {
			stmt = conn.prepareStatement("SELECT PICTUREPATH, THUMBNAILPRIORITYTYPE, PRIORITYINDEX, SEEKSEC, ID" +
											" FROM FILEENTRYTHUMBNAILPRIORITIES, THUMBNAILPRIORITIES" +
											" WHERE FOLDERID = ? AND THUMBNAILPRIORITIESID = ID");
			stmt.clearParameters();
			stmt.setLong(1, id);
			rs = stmt.executeQuery();
			while (rs.next()) {
				DOThumbnailPriority prio = new DOThumbnailPriority();
				prio.setPicturePath(rs.getString(1));
				prio.setThumbnailPriorityType(ThumbnailPrioType.valueOf(rs.getString(2)));
				prio.setPriorityIndex(rs.getInt(3));
				prio.setSeekPosition(rs.getInt(4));
				prio.setId(rs.getLong(5));
				
				props.add(prio);
			}	    
		} finally {
			close(rs);
		}

	    return props;
    }
}
