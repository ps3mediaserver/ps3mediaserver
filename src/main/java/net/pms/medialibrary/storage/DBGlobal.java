package net.pms.medialibrary.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.pms.medialibrary.commons.enumarations.MediaLibraryConstants.MetaDataKeys;
import net.pms.medialibrary.commons.exceptions.StorageException;

import org.h2.jdbcx.JdbcConnectionPool;

class DBGlobal {
	private JdbcConnectionPool cp;
	
	DBGlobal(JdbcConnectionPool cp){
		this.cp = cp;
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
			try { if (rs != null) rs.close(); } catch (SQLException ex){ } finally { rs = null; }
			try { if (stmt != null) stmt.close(); } catch (SQLException ex){ } finally { stmt = null; }
			try { if (conn != null) conn.close(); } catch (SQLException ex){ } finally { conn = null; }	
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
			try { if (stmt != null) stmt.close(); } catch (SQLException ex){ } finally { stmt = null; }
			try { if (conn != null) conn.close(); } catch (SQLException ex){ } finally { conn = null; }
		}
	}
}
