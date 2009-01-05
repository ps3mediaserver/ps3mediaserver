package net.pms.dlna;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.h2.tools.Script;

import net.pms.PMS;
import net.pms.newgui.LooksFrame;


public class DLNAMediaDatabase implements Runnable {
	
	//private String name;
	private String url;
	private String dir;
	public static String NONAME = "###";
	private Thread scanner;
	
	public DLNAMediaDatabase(String name) {
		dir = "database" ;
		url = "jdbc:h2:" + dir + "/" + name;
		PMS.info("Using database URL: " + url);
		PMS.minimal("Using database located at : " + new File(dir).getAbsolutePath());
		
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			PMS.error(null, e);
		}
	}
	
	private Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(url, "sa", "");
		return conn;
	}
	
	public void init(boolean force) {
		int count = -1;
		String version = null;
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		try {
			conn = getConnection();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT count(*) FROM FILES");
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
		} catch (SQLException se) {
			PMS.info("Database not created or corrupted");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {}
		}
		if (force || (count == -1 || version == null || version.equals("0.10"))) { // here we can force a deletion for a specific version
			PMS.info("Database will be (re)initialized");
			try {
				conn = getConnection();
				executeUpdate(conn, "DROP TABLE FILES");
				executeUpdate(conn, "DROP TABLE METADATA");
				executeUpdate(conn, "DROP TABLE REGEXP_RULES");
			} catch (SQLException se) {}
			try {
				StringBuffer sb = new StringBuffer();
				sb.append("CREATE TABLE FILES (");
				sb.append("  FILENAME          VARCHAR2(1000)       NOT NULL");
				sb.append(", MODIFIED          TIMESTAMP            NOT NULL");
				sb.append(", TYPE              NUMERIC");
				sb.append(", DURATION          VARCHAR2(255)");
				sb.append(", BITRATE           NUMERIC");
				sb.append(", WIDTH             NUMERIC");
				sb.append(", HEIGHT            NUMERIC");
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
				sb.append(", MODEL             VARCHAR2(255)");
				sb.append(", EXPOSURE          NUMERIC");
				sb.append(", ORIENTATION       NUMERIC");
				sb.append(", ISO               NUMERIC");
				sb.append(", constraint PK1 primary key (FILENAME, MODIFIED))");
				executeUpdate(conn, sb.toString());
				executeUpdate(conn, "CREATE TABLE METADATA (KEY VARCHAR2(255) NOT NULL, VALUE VARCHAR2(255) NOT NULL)");
				executeUpdate(conn, "INSERT INTO METADATA VALUES ('VERSION', '" + PMS.VERSION + "')");
				executeUpdate(conn, "CREATE INDEX IDXARTIST on FILES (ARTIST asc);");
				executeUpdate(conn, "CREATE INDEX IDXALBUM on FILES (ALBUM asc);");
				executeUpdate(conn, "CREATE INDEX IDXGENRE on FILES (GENRE asc);");
				executeUpdate(conn, "CREATE INDEX IDXYEAR on FILES (YEAR asc);");
				executeUpdate(conn, "CREATE TABLE REGEXP_RULES ( ID VARCHAR2(255) PRIMARY KEY, RULE VARCHAR2(255), ORDR NUMERIC);");
				executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '###', '(?i)^\\W.+', 0 );");
				executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '0-9', '(?i)^\\d.+', 1 );");
				for(int i=65;i<=90;i++) {
					executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '" + ((char) i) + "', '(?i)^" + ((char) i) + ".+', " + (i-63) + " );");
				}
				PMS.info("Database initialized");
			} catch (SQLException se) {
				PMS.minimal("Error in table creation: " + se.getMessage());
			} finally {
				if (conn != null)
					try {
						conn.close();
					} catch (SQLException e) {}
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
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			conn = getConnection();
			stmt = conn.prepareStatement("SELECT * FROM FILES WHERE FILENAME = ? AND MODIFIED = ?");
			stmt.setString(1, name);
			stmt.setTimestamp(2, new Timestamp(modified));
			rs = stmt.executeQuery();
			while (rs.next()) {
				found = true;
			}
		} catch (SQLException se) {
			PMS.error(null, se);
			return false;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {}
		}
		return found;
	}
	
	public ArrayList<DLNAMediaInfo> getData(String name, long modified) {
		ArrayList<DLNAMediaInfo> list = new ArrayList<DLNAMediaInfo>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			conn = getConnection();
			stmt = conn.prepareStatement("SELECT * FROM FILES WHERE FILENAME = ? AND MODIFIED = ?");
			stmt.setString(1, name);
			stmt.setTimestamp(2, new Timestamp(modified));
			rs = stmt.executeQuery();
			while (rs.next()) {
				DLNAMediaInfo media = new DLNAMediaInfo();
				media.duration = rs.getString("DURATION");
				media.bitrate = rs.getInt("BITRATE");
				media.width = rs.getInt("WIDTH");
				media.height = rs.getInt("HEIGHT");
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
				media.model = rs.getString("MODEL");
				media.exposure = rs.getInt("EXPOSURE");
				media.orientation = rs.getInt("ORIENTATION");
				media.iso = rs.getInt("ISO");
				media.mediaparsed = true;
				list.add(media);
			}
		} catch (SQLException se) {
			PMS.error(null, se);
			return null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {}
		}
		return list;
	}

	public void insertData(String name, long modified, int type, DLNAMediaInfo media) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement("INSERT INTO FILES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, name);
			ps.setTimestamp(2, new Timestamp(modified));
			ps.setInt(3, type);
			if (media != null) {
				ps.setString(4, media.duration);
				ps.setInt(5, media.bitrate);
				ps.setInt(6, media.width);
				ps.setInt(7, media.height);
				ps.setLong(8, media.size);
				ps.setInt(9, media.nrAudioChannels);
				ps.setString(10, media.sampleFrequency);
				ps.setString(11, media.codecV);
				ps.setString(12, media.codecA);
				ps.setString(13, media.frameRate);
				ps.setString(14, media.aspect);
				ps.setInt(15, media.bitsperSample);
				ps.setBytes(16, media.thumb);
				ps.setString(17, StringUtils.trimToEmpty(media.album));
				ps.setString(18, StringUtils.trimToEmpty(media.artist));
				ps.setString(19, StringUtils.trimToEmpty(media.songname));
				ps.setString(20, StringUtils.trimToEmpty(media.genre));
				ps.setString(21, media.container);
				ps.setInt(22, media.year);
				ps.setInt(23, media.track);
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
				ps.setString(24, audios.toString());
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
				ps.setString(25, audios.toString());
				ps.setString(26, media.model);
				ps.setInt(27, media.exposure);
				ps.setInt(28, media.orientation);
				ps.setInt(29, media.iso);
			} else {
				ps.setString(4, null);
				ps.setInt(5, 0);
				ps.setInt(6, 0);
				ps.setInt(7, 0);
				ps.setLong(8, 0);
				ps.setInt(9, 0);
				ps.setString(10, null);
				ps.setString(11, null);
				ps.setString(12, null);
				ps.setString(13, null);
				ps.setString(14, null);
				ps.setInt(15, 0);
				ps.setBytes(16, null);
				ps.setString(17, null);
				ps.setString(18, null);
				ps.setString(19, null);
				ps.setString(20, null);
				ps.setString(21, null);
				ps.setInt(22, 0);
				ps.setInt(23, 0);
				ps.setString(24, null);
				ps.setString(25, null);
				ps.setString(26, null);
				ps.setInt(27, 0);
				ps.setInt(28, 0);
				ps.setInt(29, 0);
			}
			ps.executeUpdate();
		} catch (SQLException se) {
			if (se.getMessage().contains("[23001")) {
				PMS.info("Duplicate key while inserting this entry: " + name  + " into the database: " + se.getMessage());
			} else
				PMS.error(null, se);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {}
		}
	}
	
	private String blank(String s) {
		if (s == null || s.length() == 0)
			return " ";
		return s;
	}
	
	public ArrayList<String> getStrings(String sql) {
		ArrayList<String> list = new ArrayList<String>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				String str = rs.getString(1);
				if (StringUtils.isBlank(str)) {
					if (!list.contains(NONAME))
						list.add(NONAME);
				} else if (!list.contains(str))
					list.add(str);
			}
		} catch (SQLException se) {
			PMS.error(null, se);
			return null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {}
		}
		return list;
	}
	
	public void cleanup() {
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM FILES");
			ResultSet rs = ps.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			ps.close();
			((LooksFrame) PMS.get().getFrame()).setStatusLine("Cleanup database... 0%");
			int i = 0;
			int oldpercent = 0;
			if (count > 0) {
				ps = conn.prepareStatement("SELECT FILENAME, MODIFIED FROM FILES", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
				rs = ps.executeQuery();
				while (rs.next()) {
					String filename = rs.getString("FILENAME");
					long modified = rs.getTimestamp("MODIFIED").getTime();
					File entry = new File(filename);
					if (!entry.exists() || entry.lastModified() != modified) {
						rs.deleteRow();
					}
					i++;
					int newpercent = i * 100 / count;
					if (newpercent > oldpercent) {
						((LooksFrame) PMS.get().getFrame()).setStatusLine("Cleanup database... " + newpercent + "%");
						oldpercent = newpercent;
					}
				}
				rs.close();
				ps.close();
			}
			conn.close();
		} catch (SQLException se) {
			PMS.error(null, se);
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {}
		}
	}
	
	
	public ArrayList<File> getFiles(String sql) {
		ArrayList<File> list = new ArrayList<File>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement("SELECT DISTINCT FILENAME, MODIFIED FROM FILES WHERE " + sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				String filename = rs.getString("FILENAME");
				long modified = rs.getTimestamp("MODIFIED").getTime();
				File entry = new File(filename);
				if (entry.exists() && entry.lastModified() == modified)
					list.add(entry);
			}
		} catch (SQLException se) {
			PMS.error(null, se);
			return null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {}
		}
		return list;
	}
	
	public synchronized boolean isScanLibraryRunning() {
		return scanner != null && scanner.isAlive();
	}
	
	public synchronized void scanLibrary() {
		if (scanner == null) {
			scanner = new Thread(this);
			scanner.start();
		} else if (scanner.isAlive()) {
			PMS.minimal("Scanner is already running !");
		} else {
			scanner = new Thread(this);
			scanner.start();
		}
	}
	
	public synchronized void stopScanLibrary() {
		if (scanner != null && scanner.isAlive()) {
			PMS.get().getRootFolder().stopscan();
			//scanner.interrupt();
		}
	}
	
	public void run() {
		PMS.get().getRootFolder().scan();
	}
	
	public void compact() {
		PMS.minimal("Compacting database...");
		((LooksFrame) PMS.get().getFrame()).setStatusLine("Compacting database...");
        String file = "database/backup.sql";
        try {
        	Script.execute(url, "sa", "", file);
        	DeleteDbFiles.execute(dir, "medias", true);
        	RunScript.execute(url, "sa", "", file, null, false);
        } catch (Exception s) {
        	PMS.error("Error in compacting database: ", s);
        } finally {
        	File testsql = new File(file);
        	if (testsql.exists()) {
        		if (!testsql.delete())
        			testsql.deleteOnExit();
        	}
        }
        ((LooksFrame) PMS.get().getFrame()).setStatusLine(null);
    }

}
