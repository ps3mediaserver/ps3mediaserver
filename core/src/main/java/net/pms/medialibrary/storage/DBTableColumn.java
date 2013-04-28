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

import net.pms.medialibrary.commons.dataobjects.DOTableColumnConfiguration;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.exceptions.StorageException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Package class used to structure code for MediaLibraryStorage
 */
class DBTableColumn extends DBFileInfo {
	private static final Logger log = LoggerFactory.getLogger(DBTableColumn.class);
	
	DBTableColumn(JdbcConnectionPool cp) {
		super(cp);
    }

	List<DOTableColumnConfiguration> getTableColumnConfiguration(FileType fileType) throws StorageException {
		ArrayList<DOTableColumnConfiguration> res = new ArrayList<DOTableColumnConfiguration>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT CONDITIONTYPE, COLUMNINDEX, WIDTH"
					+ " FROM TABLECOLUMNCONFIGURATION"
					+ " WHERE FILETYPE = ?" 
					+ " ORDER BY COLUMNINDEX ASC");
			stmt.clearParameters();
			stmt.setString(1, fileType.toString());
			rs = stmt.executeQuery();
			
			while(rs.next()){
				ConditionType ct;
				try{
					ct = Enum.valueOf(ConditionType.class, rs.getString(1));
				}catch(Exception ex){
					log.warn(String.format("Failed to add column of type %s. This is probably a leftover from an update", rs.getString(1)));
					continue;
				}
				int columnIndex = rs.getInt(2);
				int width = rs.getInt(3);
				res.add( new DOTableColumnConfiguration(ct, columnIndex, width));
			}
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to get table column configuration for fileType=%s", fileType), se);
		} finally {
			close(conn, stmt, rs);
		}
		return res;
	}

	void insertTableColumnConfiguration(DOTableColumnConfiguration c, FileType fileType) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("INSERT INTO TABLECOLUMNCONFIGURATION (COLUMNINDEX, WIDTH, FILETYPE, CONDITIONTYPE) VALUES (?, ?, ?, ?)");
			stmt.clearParameters();
			stmt.setInt(1, c.getColumnIndex());
			stmt.setInt(2, c.getWidth());
			stmt.setString(3, fileType.toString());
			stmt.setString(4, c.getConditionType().toString());
			stmt.executeUpdate();
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to insert table column configuration for columnIndex=%s, width=%s, fileType=%s, conditionType=%s", 
					c.getColumnIndex(), c.getWidth(), fileType, c.getConditionType()), se);
		} finally {
			close(conn, stmt);
		}
	}

	public void updateTableColumnConfiguration(DOTableColumnConfiguration c, FileType fileType) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("UPDATE TABLECOLUMNCONFIGURATION"
					+ " SET COLUMNINDEX = ?, WIDTH = ? WHERE FILETYPE = ? AND CONDITIONTYPE = ?");
			stmt.clearParameters();
			stmt.setInt(1, c.getColumnIndex());
			stmt.setInt(2, c.getWidth());
			stmt.setString(3, fileType.toString());
			stmt.setString(4, c.getConditionType().toString());
			stmt.executeUpdate();
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to update table column configuration for columnIndex=%s, width=%s, fileType=%s, conditionType=%s", 
					c.getColumnIndex(), c.getWidth(), fileType, c.getConditionType()), se);
		} finally {
			close(conn, stmt);
		}
	}

	public void updateTableColumnConfiguration(ConditionType ct, int width, FileType fileType) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("UPDATE TABLECOLUMNCONFIGURATION"
					+ " SET WIDTH = ? WHERE FILETYPE = ? AND CONDITIONTYPE = ?");
			stmt.clearParameters();
			stmt.setInt(1, width);
			stmt.setString(2, fileType.toString());
			stmt.setString(3, ct.toString());
			stmt.executeUpdate();
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to update table column width for conditionType=%s, width=%s, fileType=%s", 
					ct, width, fileType), se);
		} finally {
			close(conn, stmt);
		}
	}

	public DOTableColumnConfiguration getTableColumnConfiguration(FileType fileType, int columnIndex) throws StorageException {
		DOTableColumnConfiguration res = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT CONDITIONTYPE, WIDTH"
					+ " FROM TABLECOLUMNCONFIGURATION"
					+ " WHERE FILETYPE = ? AND COLUMNINDEX = ?");
			stmt.setString(1, fileType.toString());
			stmt.setInt(2, columnIndex);
			rs = stmt.executeQuery();
			
			if(rs.next()){
				ConditionType ct = Enum.valueOf(ConditionType.class, rs.getString(1));
				int width = rs.getInt(2);
				res = new DOTableColumnConfiguration(ct, columnIndex, width);
			}
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to get table column configuration for fileType=%s, columnIndex=%s", fileType, columnIndex), se);
		} finally {
			close(conn, stmt, rs);
		}
		return res;
	}

	public DOTableColumnConfiguration getTableColumnConfiguration(FileType fileType, ConditionType ct) throws StorageException {
		DOTableColumnConfiguration res = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
				
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT COLUMNINDEX, WIDTH"
					+ " FROM TABLECOLUMNCONFIGURATION"
					+ " WHERE FILETYPE = ? AND CONDITIONTYPE = ?");
			stmt.setString(1, fileType.toString());
			stmt.setString(2, ct.toString());
			rs = stmt.executeQuery();
				
			if(rs.next()){
				int columnIndex = rs.getInt(1);
				int width = rs.getInt(2);
				res = new DOTableColumnConfiguration(ct, columnIndex, width);
			}
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to get table column configuration for fileType=%s, conditionType=%s", fileType, ct), se);
		} finally {
			close(conn, stmt, rs);
		}
		return res;
	}

	public void clearTableColumnConfiguration(FileType fileType) throws StorageException {
			Connection conn = null;
			PreparedStatement stmt = null;
				
			try {
				conn = cp.getConnection();
				stmt = conn.prepareStatement("DELETE FROM TABLECOLUMNCONFIGURATION"
						+ " WHERE FILETYPE = ?");
				stmt.setString(1, fileType.toString());
				stmt.executeUpdate();
			} catch (SQLException se) {
				throw new StorageException(String.format("Failed to delete table column configuration for fileType=%s", fileType), se);
			} finally {
				close(conn, stmt);
			}
	}

	public int getTableConfigurationMaxColumnIndex(FileType fileType) throws StorageException {
		int res = 0;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT MAX(COLUMNINDEX)"
					+ " FROM TABLECOLUMNCONFIGURATION"
					+ " WHERE FILETYPE = ?");
			stmt.setString(1, fileType.toString());
			rs = stmt.executeQuery();
			
			if(rs.next()){
				res = rs.getInt(1);
			}
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to get table column configuration for fileType=%s", fileType), se);
		} finally {
			close(conn, stmt, rs);
		}
		return res;
	}

	public void deleteTableColumnConfiguration(int columnIndex, FileType fileType) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("DELETE FROM TABLECOLUMNCONFIGURATION"
					+ " WHERE FILETYPE = ? AND COLUMNINDEX = ?");
			stmt.setString(1, fileType.toString());
			stmt.setInt(2, columnIndex);
			stmt.executeUpdate();
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to delete table column configuration for fileType=%s", fileType), se);
		} finally {
			close(conn, stmt);
		}
	}

	public void deleteAllTableColumnConfiguration(FileType fileType) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("DELETE FROM TABLECOLUMNCONFIGURATION"
					+ " WHERE FILETYPE = ? AND COLUMNINDEX > 0");
			stmt.setString(1, fileType.toString());
			stmt.executeUpdate();
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to delete table column configuration for fileType=%s", fileType), se);
		} finally {
			close(conn, stmt);
		}
	}
}
