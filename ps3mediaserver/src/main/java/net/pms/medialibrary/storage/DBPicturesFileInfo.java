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
