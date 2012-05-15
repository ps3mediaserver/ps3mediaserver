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
import net.pms.medialibrary.commons.enumarations.MediaLibraryConstants.MetaDataKeys;
import net.pms.medialibrary.commons.exceptions.StorageException;

import org.h2.jdbcx.JdbcConnectionPool;

class DBGlobal extends DBBase {
	
	DBGlobal(JdbcConnectionPool cp){
		super(cp);
	}
	
	/*********************************************
	 * 
	 * Package Methods
	 * 
	 *********************************************/
	
	String getDbVersion() throws StorageException{
		return getMetaDataValue(MetaDataKeys.VERSION.toString());
	}
		
	String getMetaDataValue(String key) throws StorageException{
		String retVal = null;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT VALUE FROM METADATA WHERE KEY = ?");
			stmt.setString(1, key);
			rs = stmt.executeQuery();
			if (rs.next()) {
				retVal = rs.getString(1);
			}
		} catch (SQLException se) {
			throw new StorageException("Failed to get meta data value for key=" + key, se);
		} finally {
			close(conn, stmt, rs);
		}
		
		return retVal;
	}
		
	void setMetaDataValue(String key, String value) throws StorageException{
		Connection conn = null;
		PreparedStatement stmt = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("DELETE FROM METADATA WHERE KEY = ?");
			stmt.setObject(1, key);
			stmt.executeUpdate();
			
			stmt = conn.prepareStatement("INSERT INTO METADATA (KEY, VALUE) VALUES (?, ?)");
        	stmt.setString(1, key);
        	stmt.setString(2, value);
        	stmt.executeUpdate();
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to set metadata value=%s for key=%s", value, key), se);
		} finally {
			close(conn, stmt);
		}
	}
}
