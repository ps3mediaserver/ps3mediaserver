package net.pms.medialibrary.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.exceptions.StorageException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBManagedFolders extends DBBase {
	private static final Logger log = LoggerFactory.getLogger(DBManagedFolders.class);
	
	public DBManagedFolders(JdbcConnectionPool cp){
		super(cp);
	}

	public List<DOManagedFile> getManagedFolders() throws StorageException {
		List<DOManagedFile> res = new ArrayList<DOManagedFile>();

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT WATCH, FOLDERPATH, VIDEO, AUDIO, PICTURES, SUBFOLDERS, FILEIMPORTTEMPLATEID, ISFILEIMPORTENABLED" +
											" FROM MANAGEDFOLDERS");
			rs = stmt.executeQuery();
			while(rs.next()) {
				DOManagedFile f = new DOManagedFile();
				f.setWatchEnabled(rs.getBoolean(1));
				f.setPath(rs.getString(2));
				f.setVideoEnabled(rs.getBoolean(3));
				f.setAudioEnabled(rs.getBoolean(4));
				f.setPicturesEnabled(rs.getBoolean(5));
				f.setSubFoldersEnabled(rs.getBoolean(6));
				f.setFileImportTemplate(MediaLibraryStorage.getInstance().getFileImportTemplate(rs.getInt(7)));
				f.setFileImportEnabled(rs.getBoolean(8));
				
				res.add(f);
			}
		} catch (SQLException se) {
			throw new StorageException("Failed to get managed folders", se);
		} finally {
			close(conn, stmt, rs);
		}
	
		return res;
    }
	
	public void setManagedFolders(List<DOManagedFile> folders) throws StorageException{
		Connection conn = null;
		PreparedStatement stmt = null;
		Savepoint sp = null;
		
		try {
	        conn = cp.getConnection();
	        conn.setAutoCommit(false);
	        sp = conn.setSavepoint();
	        
			//delete all folders first
			stmt = conn.prepareStatement("DELETE FROM MANAGEDFOLDERS");
			stmt.clearParameters();
			stmt.executeUpdate();
			
			//insert all folders
			for(DOManagedFile f : folders){
				stmt = conn.prepareStatement("INSERT INTO MANAGEDFOLDERS (WATCH, FOLDERPATH, VIDEO, AUDIO, PICTURES, SUBFOLDERS, FILEIMPORTTEMPLATEID, ISFILEIMPORTENABLED) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
	        	stmt.clearParameters();
	        	stmt.setBoolean(1, f.isWatchEnabled());
	        	stmt.setString(2, f.getPath());
	        	stmt.setBoolean(3, f.isVideoEnabled());
	        	stmt.setBoolean(4, f.isAudioEnabled());
	        	stmt.setBoolean(5, f.isPicturesEnabled());
	        	stmt.setBoolean(6, f.isSubFoldersEnabled());
	        	stmt.setInt(7, f.getFileImportTemplate().getId());
	        	stmt.setBoolean(8, f.isFileImportEnabled());
	        	stmt.executeUpdate();	
			}
			
			conn.commit();
			conn.releaseSavepoint(sp);
		} catch (SQLException se) {
			if(sp != null){
				try {
	                conn.rollback(sp);
	                if(log.isInfoEnabled()) log.info("Rolled back database after failure in updating managed folders");
                } catch (SQLException e) {
	                log.error("Failed to roll back database after failure in updating managed folders", e);
                }
			}
			throw new StorageException("Failed to insert managed folders", se);
		} finally {
			close(conn, stmt);
		}		
	}
}
