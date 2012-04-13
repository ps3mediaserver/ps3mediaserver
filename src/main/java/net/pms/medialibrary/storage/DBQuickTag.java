package net.pms.medialibrary.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.pms.medialibrary.commons.dataobjects.DOQuickTagEntry;
import net.pms.medialibrary.commons.enumarations.KeyCombination;
import net.pms.medialibrary.commons.exceptions.StorageException;

import org.h2.jdbcx.JdbcConnectionPool;

class DBQuickTag extends DBBase {

	DBQuickTag(JdbcConnectionPool cp) {
		super(cp);
	}

	/*********************************************
	 * 
	 * Package Methods
	 * 
	 *********************************************/

	void setQuickTags(List<DOQuickTagEntry> quickTags) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = cp.getConnection();

			// delete all existing quick tags
			stmt = conn.prepareStatement("DELETE FROM QUICKTAG");
			stmt.executeUpdate();

			// add new quick tags
			for (DOQuickTagEntry quickTag : quickTags) {
				stmt = conn.prepareStatement("INSERT INTO QUICKTAG (NAME, TAGNAME, TAGVALUE, VIRTUALKEY, KEYCOMBINATION) VALUES (?, ?, ?, ?, ?)");
				stmt.clearParameters();
				stmt.setString(1, quickTag.getName());
				stmt.setString(2, quickTag.getTagName());
				stmt.setString(3, quickTag.getTagValue());
				stmt.setInt(4, quickTag.getKeyCode());
				stmt.setString(5, quickTag.getKeyCombination().toString());
				stmt.executeUpdate();
			}
		} catch (SQLException ex) {
			throw new StorageException(String.format(
					"Failed to insert %s quick tags", quickTags.size()), ex);
		} finally {
			close(conn, stmt);
		}
	}

	public List<DOQuickTagEntry> getQuickTags() throws StorageException {
		List<DOQuickTagEntry> res = new ArrayList<DOQuickTagEntry>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;

		try {
			conn = cp.getConnection();

			// delete all existing quick tags
			stmt = conn.prepareStatement("SELECT NAME, TAGNAME, TAGVALUE, VIRTUALKEY, KEYCOMBINATION"
										 + " FROM QUICKTAG"
										 + " ORDER BY NAME ASC");
			rs = stmt.executeQuery();
			while(rs.next()) {
				res.add(new DOQuickTagEntry(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), 
						KeyCombination.valueOf(rs.getString(5))));
			}
		} catch (SQLException ex) {
			throw new StorageException("Failed to get quick tags", ex);
		} finally {
			close(conn, stmt, rs);
		}
		return res;
	}
}