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

import net.pms.medialibrary.commons.dataobjects.DOImageFileInfo;
import net.pms.medialibrary.commons.exceptions.StorageException;

import org.h2.jdbcx.JdbcConnectionPool;

class DBPicturesFileInfo extends DBBase {
	
	DBPicturesFileInfo(JdbcConnectionPool cp) {
	    super(cp);
    }
	
	/*********************************************
	 * 
	 * Package Methods
	 * @return 
	 * 
	 *********************************************/	

	int deletePicturesFileInfo() {
		// TODO Auto-generated method stub
		return 0;
	}

	int getPicturesCount() throws StorageException {
		int count = 0;

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT Count(ID) FROM PICTURES");
			rs = stmt.executeQuery();
			if(rs.next()){
				count = rs.getInt(1);
			}
		} catch (SQLException se) {
			throw new StorageException("Failed to get pictures count", se);
		} finally {
			close(conn, stmt, rs);
		}
		
		return count;
    }


	void insertPicturesFileInfo(DOImageFileInfo fileInfo) {
	    // TODO Auto-generated method stub
	    
    }

	void updatePicturesFileInfo(DOImageFileInfo fileInfo) {
	    // TODO Auto-generated method stub
	    
    }
}
