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

import net.pms.medialibrary.commons.dataobjects.DOAudioFileInfo;
import net.pms.medialibrary.commons.exceptions.StorageException;

import org.h2.jdbcx.JdbcConnectionPool;

/**
 * Package class used to structure code for MediaLibraryStorage
 */
class DBAudioFileInfo extends DBFileInfo {
	
	DBAudioFileInfo(JdbcConnectionPool cp) {
		super(cp);
    }
	
	/*********************************************
	 * 
	 * Package Methods
	 * 
	 *********************************************/

	int deleteAudioFileInfo() {
	    // TODO Auto-generated method stub
	    return 0;
    }

    int getAudioCount() throws StorageException {
		int count = 0;

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT Count(ID) FROM AUDIO");
			rs = stmt.executeQuery();
			if(rs.next()){
				count = rs.getInt(1);
			}
		} catch (SQLException se) {
			throw new StorageException("Failed to get audio count", se);
		} finally {
			close(conn, stmt, rs);
		}
		
		return count;
    }

	void insertAudioFileInfo(DOAudioFileInfo fileInfo) throws StorageException {
		super.insertFileInfo(fileInfo);

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = cp.getConnection();

			DOAudioFileInfo tmpFileInfo = (DOAudioFileInfo) fileInfo;
			stmt = conn.prepareStatement("INSERT INTO AUDIO (FILEID, LANG, NRAUDIOCHANNELS, SAMPLEFREQ, CODECA, BITSPERSAMPLE"
			        + ", ALBUM, ARTIST, SONGNAME, GENRE, YEAR, TRACK, DELAYMS, DURATIONSEC, COVERPATH) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stmt.clearParameters();
			stmt.setInt(1, fileInfo.getId());
			stmt.setString(2, tmpFileInfo.getLang());
			stmt.setInt(3, tmpFileInfo.getNrAudioChannels());
			stmt.setString(4, tmpFileInfo.getSampleFrequency());
			stmt.setString(5, tmpFileInfo.getCodecA());
			stmt.setInt(6, tmpFileInfo.getBitsperSample());
			stmt.setString(7, tmpFileInfo.getAlbum());
			stmt.setString(8, tmpFileInfo.getArtist());
			stmt.setString(9, tmpFileInfo.getSongName());
			stmt.setString(10, tmpFileInfo.getGenre());
			stmt.setInt(11, tmpFileInfo.getYear());
			stmt.setInt(12, tmpFileInfo.getTrack());
			stmt.setInt(13, tmpFileInfo.getDelay());
			stmt.setInt(14, tmpFileInfo.getDuration());
			stmt.setString(15, tmpFileInfo.getCoverPath());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new StorageException("Failed to insert audiofileinfo for file " + fileInfo.getFilePath(), e);
		} finally {
			close(conn, stmt);
		}
	}

	void updateAudioFileInfo(DOAudioFileInfo fileInfo) {
	    // TODO Auto-generated method stub
	    
    }
}
