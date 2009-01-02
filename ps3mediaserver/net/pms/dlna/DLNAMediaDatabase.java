package net.pms.dlna;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

import net.pms.PMS;


public class DLNAMediaDatabase {
	
	private String name;
	public static String NONAME = "###";
	
	public DLNAMediaDatabase(String name) {
		this.name = name;
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			PMS.error(null, e);
		}
	}
	
	private Connection getConnection() throws SQLException {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:h2:/" + PMS.get().getTempFolder().getAbsolutePath().replace('\\', '/') + "/database/" + name, "sa", "");
		} catch (IOException e) {
			PMS.error(null, e);
		}
		return conn;
	}
	
	public void init(boolean force) {
		int count = -1;
		String version = null;
		Connection conn = null;
		try {
			conn = getConnection();
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM FILES");
			if (rs.next()) {
				count = rs.getInt(1) ;
			}
			rs.close();
			stmt.close();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT VALUE FROM METADATA WHERE KEY = 'VERSION'");
			if (rs.next()) {
				version = rs.getString(1) ;
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			PMS.info("Database not created or corrupted");
		}
		if (force || (count == -1 || version == null || version.equals("0.10"))) { // here we can force a deletion for a specific version
			PMS.info("Database will be (re)initialized");
			try {
				executeUpdate(conn, "DROP TABLE FILES");
				executeUpdate(conn, "DROP TABLE METADATA");
			} catch (SQLException se) {}
			try {
				StringBuffer sb = new StringBuffer();
				sb.append("CREATE TABLE FILES (");
				sb.append("  FILENAME          VARCHAR2(1000)       NOT NULL");
				sb.append(", MODIFIED          NUMERIC              NOT NULL");
				sb.append(", TYPE              NUMERIC");
				sb.append(", DURATION          VARCHAR2(255)");
				sb.append(", BITRATE           NUMERIC");
				sb.append(", RESOLUTION        VARCHAR2(255)");
				sb.append(", SIZE              NUMERIC");
				sb.append(", NRAUDIOCHANNELS   NUMERIC");
				sb.append(", SAMPLEFREQ        VARCHAR2(255)");
				sb.append(", CODECV            VARCHAR2(255)");
				sb.append(", CODECA            VARCHAR2(255)");
				sb.append(", FRAMERATE         VARCHAR2(255)");
				sb.append(", ASPECT            VARCHAR2(255)");
				sb.append(", BITSPERSAMPLE     NUMERIC");
				sb.append(", THUMB             BINARY");
				sb.append(", ALBUM             VARCHAR2(255)");
				sb.append(", ARTIST            VARCHAR2(255)");
				sb.append(", SONGNAME          VARCHAR2(255)");
				sb.append(", GENRE             VARCHAR2(255)");
				sb.append(", CONTAINER         VARCHAR2(255)");
				sb.append(", YEAR              NUMERIC");
				sb.append(", TRACK             NUMERIC");
				sb.append(", AUDIOIDS          VARCHAR2(255)");
				sb.append(", SUBIDS            VARCHAR2(255)");
				sb.append(", constraint PK1 primary key (FILENAME, MODIFIED))");
				executeUpdate(conn, sb.toString());
				executeUpdate(conn, "CREATE TABLE METADATA (KEY VARCHAR2(255) NOT NULL, VALUE VARCHAR2(255) NOT NULL)");
				executeUpdate(conn, "INSERT INTO METADATA VALUES ('VERSION', '1.00')");
				executeUpdate(conn, "CREATE INDEX IDXARTIST on FILES (ARTIST asc);");
				executeUpdate(conn, "CREATE INDEX IDXALBUM on FILES (ALBUM asc);");
				executeUpdate(conn, "CREATE INDEX IDXGENRE on FILES (GENRE asc);");
				executeUpdate(conn, "CREATE INDEX IDXYEAR on FILES (YEAR asc);");
				PMS.info("Database initialized");
			} catch (SQLException se) {
				PMS.minimal("Error in table creation: " + se.getMessage());
			}
		} else {
			PMS.info("Database file count: " + count);
			PMS.info("Database version: " + version);
		}
	}
	
	private void executeUpdate(Connection conn, String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();
	}
	
	public boolean isDataExists(String name, long modified) {
		boolean found = false;
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM FILES WHERE FILENAME = ? AND MODIFIED = ?");
			ps.setString(1, name);
			ps.setLong(2, modified);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				found = true;
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException se) {
			PMS.error(null, se);
			return false;
		}
		return found;
	}
	
	public ArrayList<DLNAMediaInfo> getData(String name, long modified) {
		ArrayList<DLNAMediaInfo> list = new ArrayList<DLNAMediaInfo>();
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM FILES WHERE FILENAME = ? AND MODIFIED = ?");
			ps.setString(1, name);
			ps.setLong(2, modified);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				DLNAMediaInfo media = new DLNAMediaInfo();
				media.duration = rs.getString("DURATION");
				media.bitrate = rs.getInt("BITRATE");
				media.resolution = rs.getString("RESOLUTION");
				media.size = rs.getLong("SIZE");
				media.nrAudioChannels = rs.getInt("NRAUDIOCHANNELS");
				media.sampleFrequency = rs.getString("SAMPLEFREQ");
				media.codecV = rs.getString("CODECV");
				media.codecA = rs.getString("CODECA");
				media.frameRate = rs.getString("FRAMERATE");
				media.aspect = rs.getString("ASPECT");
				media.bitsperSample = rs.getInt("BITSPERSAMPLE");
				media.thumb = rs.getBytes("THUMB");
				media.album = rs.getString("ALBUM");
				media.artist = rs.getString("ARTIST");
				media.songname = rs.getString("SONGNAME");
				media.genre = rs.getString("GENRE");
				media.container = rs.getString("CONTAINER");
				media.year = rs.getInt("YEAR");
				media.track = rs.getInt("TRACK");
				String audiosids = rs.getString("AUDIOIDS");
				StringTokenizer st = new StringTokenizer(audiosids, "|");
				media.audioCodes = new ArrayList<DLNAMediaLang>();
				while (st.hasMoreTokens()) {
					String medialang = st.nextToken();
					StringTokenizer st2 = new StringTokenizer(medialang, ",");
					DLNAMediaLang lang = new DLNAMediaLang();
					lang.id = Integer.parseInt(st2.nextToken());
					lang.format = st2.nextToken().trim();
					lang.lang = st2.nextToken().trim();
					media.audioCodes.add(lang);
				}
				String subids = rs.getString("SUBIDS");
				st = new StringTokenizer(subids, "|");
				media.subtitlesCodes = new ArrayList<DLNAMediaLang>();
				while (st.hasMoreTokens()) {
					String medialang = st.nextToken();
					StringTokenizer st2 = new StringTokenizer(medialang, ",");
					DLNAMediaLang lang = new DLNAMediaLang();
					lang.id = Integer.parseInt(st2.nextToken());
					lang.format = st2.nextToken().trim();
					lang.lang = st2.nextToken().trim();
					media.subtitlesCodes.add(lang);
				}
				media.mediaparsed = true;
				list.add(media);
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException se) {
			PMS.error(null, se);
			return null;
		}
		return list;
	}

	public void insertData(String name, long modified, int type, DLNAMediaInfo media) {
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement("INSERT INTO FILES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, name);
			ps.setLong(2, modified);
			ps.setInt(3, type);
			if (media != null) {
				ps.setString(4, media.duration);
				ps.setInt(5, media.bitrate);
				ps.setString(6, media.resolution);
				ps.setLong(7, media.size);
				ps.setInt(8, media.nrAudioChannels);
				ps.setString(9, media.sampleFrequency);
				ps.setString(10, media.codecV);
				ps.setString(11, media.codecA);
				ps.setString(12, media.frameRate);
				ps.setString(13, media.aspect);
				ps.setInt(14, media.bitsperSample);
				ps.setBytes(15, media.thumb);
				ps.setString(16, StringUtils.trimToEmpty(media.album));
				ps.setString(17, StringUtils.trimToEmpty(media.artist));
				ps.setString(18, StringUtils.trimToEmpty(media.songname));
				ps.setString(19, StringUtils.trimToEmpty(media.genre));
				ps.setString(20, media.container);
				ps.setInt(21, media.year);
				ps.setInt(22, media.track);
				StringBuffer audios = new StringBuffer();
				for(DLNAMediaLang medialang:media.audioCodes) {
					if (audios.length() > 0)
						audios.append("|");
					audios.append(medialang.id);
					audios.append(",");
					audios.append(blank(medialang.format));
					audios.append(",");
					audios.append(blank(medialang.lang));
				}
				ps.setString(23, audios.toString());
				audios = new StringBuffer();
				for(DLNAMediaLang medialang:media.subtitlesCodes) {
					if (audios.length() > 0)
						audios.append("|");
					audios.append(medialang.id);
					audios.append(",");
					audios.append(blank(medialang.format));
					audios.append(",");
					audios.append(blank(medialang.lang));
				}
				ps.setString(24, audios.toString());
			} else {
				ps.setString(4, null);
				ps.setInt(5, 0);
				ps.setString(6, null);
				ps.setLong(7, 0);
				ps.setInt(8, 0);
				ps.setString(9, null);
				ps.setString(10, null);
				ps.setString(11, null);
				ps.setString(12, null);
				ps.setString(13, null);
				ps.setInt(14, 0);
				ps.setBytes(15, null);
				ps.setString(16, null);
				ps.setString(17, null);
				ps.setString(18, null);
				ps.setString(19, null);
				ps.setString(20, null);
				ps.setInt(21, 0);
				ps.setInt(22, 0);
				ps.setString(23, null);
				ps.setString(24, null);
			}
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException se) {
			if (se.getMessage().contains("[23001")) {
				PMS.info("Duplicate key while inserting this entry: " + name  + " into the database: " + se.getMessage());
			} else
				PMS.error(null, se);
		}
	}
	
	private String blank(String s) {
		if (s == null || s.length() == 0)
			return " ";
		return s;
	}
	
	public ArrayList<String> getStrings(String sql) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String str = rs.getString(1);
				if (StringUtils.isBlank(str)) {
					if (!list.contains(NONAME))
						list.add(NONAME);
				} else
					list.add(str);
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException se) {
			PMS.error(null, se);
			return null;
		}
		return list;
	}
	
	public ArrayList<File> getFiles(String sql) {
		ArrayList<File> list = new ArrayList<File>();
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT FILENAME, MODIFIED FROM FILES WHERE " + sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String filename = rs.getString("FILENAME");
				long modified = rs.getLong("MODIFIED");
				File entry = new File(filename);
				if (entry.exists() && entry.lastModified() == modified)
					list.add(entry);
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException se) {
			PMS.error(null, se);
			return null;
		}
		return list;
	}

}
