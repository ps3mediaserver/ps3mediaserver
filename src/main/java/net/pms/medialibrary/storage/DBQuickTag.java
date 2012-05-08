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