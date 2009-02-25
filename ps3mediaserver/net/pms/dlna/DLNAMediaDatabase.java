package net.pms.dlna;

import java.awt.Component;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.pms.Messages;
import net.pms.PMS;

import org.apache.commons.lang.StringUtils;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.h2.tools.Script;

import com.sun.jna.Platform;


public class DLNAMediaDatabase implements Runnable {
	
	//private String name;
	private String url;
	private String dir;
	public static String NONAME = "###"; //$NON-NLS-1$
	private Thread scanner;
	
	static {
		/*try {
			Server server = Server.createWebServer(null);
			server.start();
			PMS.minimal("Starting H2 console on port " + server.getPort()); //$NON-NLS-1$
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
	}
	
	public DLNAMediaDatabase(String name) {
		dir = "database" ; //$NON-NLS-1$
		File fileDir = new File(dir);
		boolean defaultLocation = fileDir.mkdir() || fileDir.exists();
		String strAppData = System.getenv("APPDATA"); //$NON-NLS-1$
		if (Platform.isWindows() && !defaultLocation && strAppData != null) {
			url = "jdbc:h2:" + strAppData + "\\PMS\\" + dir + "/" + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			fileDir = new File(strAppData + "\\PMS\\" + dir); //$NON-NLS-1$
		} else {
			url = "jdbc:h2:" + dir + "/" + name; //$NON-NLS-1$ //$NON-NLS-2$
		}
		PMS.info("Using database URL: " + url); //$NON-NLS-1$
		PMS.minimal("Using database located at : " + fileDir.getAbsolutePath()); //$NON-NLS-1$
		
		try {
			Class.forName("org.h2.Driver"); //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
			PMS.error(null, e);
		}
	}
	
	private Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(url, "sa", ""); //$NON-NLS-1$ //$NON-NLS-2$
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
			rs = stmt.executeQuery("SELECT count(*) FROM FILES"); //$NON-NLS-1$
			if (rs.next()) {
				count = rs.getInt(1) ;
			}
			rs.close();
			stmt.close();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT VALUE FROM METADATA WHERE KEY = 'VERSION'"); //$NON-NLS-1$
			if (rs.next()) {
				version = rs.getString(1) ;
			}
		} catch (SQLException se) {
			PMS.info("Database not created or corrupted"); //$NON-NLS-1$
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
		boolean force_reinit = version != null && version.startsWith("1.0"); // here we can force a deletion for a specific version //$NON-NLS-1$
		if (force || count == -1 || version == null || force_reinit) {
			PMS.info("Database will be (re)initialized"); //$NON-NLS-1$
			if (force_reinit)
				JOptionPane.showMessageDialog(
					(JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
                    Messages.getString("DLNAMediaDatabase.0"), //$NON-NLS-1$
                    "Information", //$NON-NLS-1$
                    JOptionPane.INFORMATION_MESSAGE);
			try {
				conn = getConnection();
				executeUpdate(conn, "DROP TABLE FILES"); //$NON-NLS-1$
				executeUpdate(conn, "DROP TABLE METADATA"); //$NON-NLS-1$
				executeUpdate(conn, "DROP TABLE REGEXP_RULES"); //$NON-NLS-1$
				executeUpdate(conn, "DROP TABLE AUDIOTRACKS"); //$NON-NLS-1$
				executeUpdate(conn, "DROP TABLE SUBTRACKS"); //$NON-NLS-1$
			} catch (SQLException se) {}
			try {
				StringBuffer sb = new StringBuffer();
				sb.append("CREATE TABLE FILES ("); //$NON-NLS-1$
				sb.append("  ID                INT AUTO_INCREMENT"); //$NON-NLS-1$
				sb.append(", FILENAME          VARCHAR2(1024)       NOT NULL"); //$NON-NLS-1$
				sb.append(", MODIFIED          TIMESTAMP            NOT NULL"); //$NON-NLS-1$
				sb.append(", TYPE              INT"); //$NON-NLS-1$
				sb.append(", DURATION          VARCHAR2(30)"); //$NON-NLS-1$
				sb.append(", BITRATE           INT"); //$NON-NLS-1$
				sb.append(", WIDTH             INT"); //$NON-NLS-1$
				sb.append(", HEIGHT            INT"); //$NON-NLS-1$
				sb.append(", SIZE              NUMERIC"); //$NON-NLS-1$
				sb.append(", CODECV            VARCHAR2(32)"); //$NON-NLS-1$
				sb.append(", FRAMERATE         VARCHAR2(16)"); //$NON-NLS-1$
				sb.append(", ASPECT            VARCHAR2(16)"); //$NON-NLS-1$
				sb.append(", BITSPERPIXEL      INT"); //$NON-NLS-1$
				sb.append(", THUMB             BINARY"); //$NON-NLS-1$
				sb.append(", CONTAINER         VARCHAR2(32)"); //$NON-NLS-1$
				sb.append(", MODEL             VARCHAR2(128)"); //$NON-NLS-1$
				sb.append(", EXPOSURE          INT"); //$NON-NLS-1$
				sb.append(", ORIENTATION       INT"); //$NON-NLS-1$
				sb.append(", ISO               INT"); //$NON-NLS-1$
				sb.append(", constraint PK1 primary key (ID, FILENAME, MODIFIED))"); //$NON-NLS-1$
				executeUpdate(conn, sb.toString());
				sb = new StringBuffer();
				sb.append("CREATE TABLE AUDIOTRACKS ("); //$NON-NLS-1$
				sb.append("  FILEID            INT              NOT NULL"); //$NON-NLS-1$
				sb.append(", ID                INT              NOT NULL"); //$NON-NLS-1$
				sb.append(", LANG              VARCHAR2(3)"); //$NON-NLS-1$
				sb.append(", NRAUDIOCHANNELS   NUMERIC"); //$NON-NLS-1$
				sb.append(", SAMPLEFREQ        VARCHAR2(16)"); //$NON-NLS-1$
				sb.append(", CODECA            VARCHAR2(32)"); //$NON-NLS-1$
				sb.append(", BITSPERSAMPLE     INT"); //$NON-NLS-1$
				sb.append(", ALBUM             VARCHAR2(255)"); //$NON-NLS-1$
				sb.append(", ARTIST            VARCHAR2(255)"); //$NON-NLS-1$
				sb.append(", SONGNAME          VARCHAR2(255)"); //$NON-NLS-1$
				sb.append(", GENRE             VARCHAR2(64)"); //$NON-NLS-1$
				sb.append(", YEAR              INT"); //$NON-NLS-1$
				sb.append(", TRACK             INT"); //$NON-NLS-1$
				sb.append(", DELAY             INT"); //$NON-NLS-1$
				sb.append(", constraint PKAUDIO primary key (FILEID, ID))"); //$NON-NLS-1$
				executeUpdate(conn, sb.toString());
				sb = new StringBuffer();
				sb.append("CREATE TABLE SUBTRACKS ("); //$NON-NLS-1$
				sb.append("  FILEID            INT              NOT NULL"); //$NON-NLS-1$
				sb.append(", ID                INT              NOT NULL"); //$NON-NLS-1$
				sb.append(", LANG              VARCHAR2(3)"); //$NON-NLS-1$
				sb.append(", TYPE              INT"); //$NON-NLS-1$
				sb.append(", constraint PKSUB primary key (FILEID, ID))"); //$NON-NLS-1$
				
				executeUpdate(conn, sb.toString());
				executeUpdate(conn, "CREATE TABLE METADATA (KEY VARCHAR2(255) NOT NULL, VALUE VARCHAR2(255) NOT NULL)"); //$NON-NLS-1$
				executeUpdate(conn, "INSERT INTO METADATA VALUES ('VERSION', '" + PMS.VERSION + "')"); //$NON-NLS-1$ //$NON-NLS-2$
				executeUpdate(conn, "CREATE INDEX IDXARTIST on AUDIOTRACKS (ARTIST asc);"); //$NON-NLS-1$
				executeUpdate(conn, "CREATE INDEX IDXALBUM on AUDIOTRACKS (ALBUM asc);"); //$NON-NLS-1$
				executeUpdate(conn, "CREATE INDEX IDXGENRE on AUDIOTRACKS (GENRE asc);"); //$NON-NLS-1$
				executeUpdate(conn, "CREATE INDEX IDXYEAR on AUDIOTRACKS (YEAR asc);"); //$NON-NLS-1$
				executeUpdate(conn, "CREATE TABLE REGEXP_RULES ( ID VARCHAR2(255) PRIMARY KEY, RULE VARCHAR2(255), ORDR NUMERIC);"); //$NON-NLS-1$
				executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '###', '(?i)^\\W.+', 0 );"); //$NON-NLS-1$
				executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '0-9', '(?i)^\\d.+', 1 );"); //$NON-NLS-1$
				for(int i=65;i<=90;i++) {
					executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '" + ((char) i) + "', '(?i)^" + ((char) i) + ".+', " + (i-63) + " );"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				PMS.info("Database initialized"); //$NON-NLS-1$
			} catch (SQLException se) {
				PMS.minimal("Error in table creation: " + se.getMessage()); //$NON-NLS-1$
			} finally {
				if (conn != null)
					try {
						conn.close();
					} catch (SQLException e) {}
			}
		} else {
			PMS.info("Database file count: " + count); //$NON-NLS-1$
			PMS.info("Database version: " + version); //$NON-NLS-1$
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
			stmt = conn.prepareStatement("SELECT * FROM FILES WHERE FILENAME = ? AND MODIFIED = ?"); //$NON-NLS-1$
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
			stmt = conn.prepareStatement("SELECT * FROM FILES WHERE FILENAME = ? AND MODIFIED = ?"); //$NON-NLS-1$
			stmt.setString(1, name);
			stmt.setTimestamp(2, new Timestamp(modified));
			rs = stmt.executeQuery();
			while (rs.next()) {
				DLNAMediaInfo media = new DLNAMediaInfo();
				int id = rs.getInt("ID");
				media.duration = rs.getString("DURATION"); //$NON-NLS-1$
				media.bitrate = rs.getInt("BITRATE"); //$NON-NLS-1$
				media.width = rs.getInt("WIDTH"); //$NON-NLS-1$
				media.height = rs.getInt("HEIGHT"); //$NON-NLS-1$
				media.size = rs.getLong("SIZE"); //$NON-NLS-1$
				media.codecV = rs.getString("CODECV"); //$NON-NLS-1$
				media.frameRate = rs.getString("FRAMERATE"); //$NON-NLS-1$
				media.aspect = rs.getString("ASPECT"); //$NON-NLS-1$
				media.bitsPerPixel = rs.getInt("BITSPERPIXEL"); //$NON-NLS-1$
				media.thumb = rs.getBytes("THUMB"); //$NON-NLS-1$
				media.container = rs.getString("CONTAINER"); //$NON-NLS-1$
				media.model = rs.getString("MODEL"); //$NON-NLS-1$
				media.exposure = rs.getInt("EXPOSURE"); //$NON-NLS-1$
				media.orientation = rs.getInt("ORIENTATION"); //$NON-NLS-1$
				media.iso = rs.getInt("ISO"); //$NON-NLS-1$
				media.mediaparsed = true;
				PreparedStatement audios = conn.prepareStatement("SELECT * FROM AUDIOTRACKS WHERE FILEID = ?") ; //$NON-NLS-1$
				audios.setInt(1, id);
				ResultSet subrs = audios.executeQuery();
				while (subrs.next()) {
					DLNAMediaAudio audio = new DLNAMediaAudio();
					audio.id = subrs.getInt("ID"); //$NON-NLS-1$
					audio.lang = subrs.getString("LANG"); //$NON-NLS-1$
					audio.nrAudioChannels = subrs.getInt("NRAUDIOCHANNELS"); //$NON-NLS-1$
					audio.sampleFrequency = subrs.getString("SAMPLEFREQ"); //$NON-NLS-1$
					audio.codecA = subrs.getString("CODECA"); //$NON-NLS-1$
					audio.bitsperSample = subrs.getInt("BITSPERSAMPLE"); //$NON-NLS-1$
					audio.album = subrs.getString("ALBUM"); //$NON-NLS-1$
					audio.artist = subrs.getString("ARTIST"); //$NON-NLS-1$
					audio.songname = subrs.getString("SONGNAME"); //$NON-NLS-1$
					audio.genre = subrs.getString("GENRE"); //$NON-NLS-1$
					audio.year = subrs.getInt("YEAR"); //$NON-NLS-1$
					audio.track = subrs.getInt("TRACK"); //$NON-NLS-1$
					audio.delay = subrs.getInt("DELAY"); //$NON-NLS-1$
					media.audioCodes.add(audio);
				}
				subrs.close();
				audios.close();
				
				PreparedStatement subs = conn.prepareStatement("SELECT * FROM SUBTRACKS WHERE FILEID = ?") ; //$NON-NLS-1$
				subs.setInt(1, id);
				subrs = subs.executeQuery();
				while (subrs.next()) {
					DLNAMediaSubtitle sub = new DLNAMediaSubtitle();
					sub.id = subrs.getInt("ID"); //$NON-NLS-1$
					sub.lang = subrs.getString("LANG"); //$NON-NLS-1$
					sub.type = subrs.getInt("TYPE"); //$NON-NLS-1$
					media.subtitlesCodes.add(sub);
				}
				subrs.close();
				audios.close();
				
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

	public synchronized void insertData(String name, long modified, int type, DLNAMediaInfo media) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement("INSERT INTO FILES(FILENAME, MODIFIED, TYPE, DURATION, BITRATE, WIDTH, HEIGHT, SIZE, CODECV, FRAMERATE, ASPECT, BITSPERPIXEL, THUMB, CONTAINER, MODEL, EXPOSURE, ORIENTATION, ISO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"); //$NON-NLS-1$
			ps.setString(1, name);
			ps.setTimestamp(2, new Timestamp(modified));
			ps.setInt(3, type);
			if (media != null) {
				ps.setString(4, media.duration);
				ps.setInt(5, media.bitrate);
				ps.setInt(6, media.width);
				ps.setInt(7, media.height);
				ps.setLong(8, media.size);
				ps.setString(9, media.codecV);
				ps.setString(10, media.frameRate);
				ps.setString(11, media.aspect);
				ps.setInt(12, media.bitsPerPixel);
				ps.setBytes(13, media.thumb);
				ps.setString(14, media.container);
				ps.setString(15, media.model);
				ps.setInt(16, media.exposure);
				ps.setInt(17, media.orientation);
				ps.setInt(18, media.iso);
				
			} else {
				ps.setString(4, null);
				ps.setInt(5, 0);
				ps.setInt(6, 0);
				ps.setInt(7, 0);
				ps.setLong(8, 0);
				ps.setString(9, null);
				ps.setString(10, null);
				ps.setString(11, null);
				ps.setInt(12, 0);
				ps.setBytes(13, null);
				ps.setString(14, null);
				ps.setString(15, null);
				ps.setInt(16, 0);
				ps.setInt(17, 0);
				ps.setInt(18, 0);
			}
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}
			rs.close();
			if (media != null && id > -1) {
				PreparedStatement insert = conn.prepareStatement("INSERT INTO AUDIOTRACKS VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"); //$NON-NLS-1$
				for(DLNAMediaAudio audio:media.audioCodes) {
					insert.clearParameters();
					insert.setInt(1, id);
					insert.setInt(2, audio.id);
					insert.setString(3, audio.lang);
					insert.setInt(4, audio.nrAudioChannels);
					insert.setString(5, audio.sampleFrequency);
					insert.setString(6, audio.codecA);
					insert.setInt(7, audio.bitsperSample);
					insert.setString(8, StringUtils.trimToEmpty(audio.album));
					insert.setString(9, StringUtils.trimToEmpty(audio.artist));
					insert.setString(10, StringUtils.trimToEmpty(audio.songname));
					insert.setString(11, StringUtils.trimToEmpty(audio.genre));
					insert.setInt(12, audio.year);
					insert.setInt(13, audio.track);
					insert.setInt(14, audio.delay);
					insert.executeUpdate();
				}
				
				insert = conn.prepareStatement("INSERT INTO SUBTRACKS VALUES (?, ?, ?, ?)"); //$NON-NLS-1$
				for(DLNAMediaSubtitle sub:media.subtitlesCodes) {
					if (sub.file == null) { // no save of external subtitles
						insert.clearParameters();
						insert.setInt(1, id);
						insert.setInt(2, sub.id);
						insert.setString(3, sub.lang);
						insert.setInt(4, sub.type);
						insert.executeUpdate();
					}
				}
				insert.close();
			}
			
			
		} catch (SQLException se) {
			if (se.getMessage().contains("[23001")) { //$NON-NLS-1$
				PMS.info("Duplicate key while inserting this entry: " + name  + " into the database: " + se.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
			PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM FILES"); //$NON-NLS-1$
			ResultSet rs = ps.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			ps.close();
			PMS.get().getFrame().setStatusLine("Cleanup database... 0%"); //$NON-NLS-1$
			int i = 0;
			int oldpercent = 0;
			if (count > 0) {
				ps = conn.prepareStatement("SELECT FILENAME, MODIFIED FROM FILES", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE); //$NON-NLS-1$
				rs = ps.executeQuery();
				while (rs.next()) {
					String filename = rs.getString("FILENAME"); //$NON-NLS-1$
					long modified = rs.getTimestamp("MODIFIED").getTime(); //$NON-NLS-1$
					File entry = new File(filename);
					if (!entry.exists() || entry.lastModified() != modified) {
						rs.deleteRow();
					}
					i++;
					int newpercent = i * 100 / count;
					if (newpercent > oldpercent) {
						PMS.get().getFrame().setStatusLine("Cleanup database... " + newpercent + "%"); //$NON-NLS-1$ //$NON-NLS-2$
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
			ps = conn.prepareStatement(sql.toLowerCase().startsWith("select")?sql:("SELECT FILENAME, MODIFIED FROM FILES WHERE " + sql)); //$NON-NLS-1$
			rs = ps.executeQuery();
			while (rs.next()) {
				String filename = rs.getString("FILENAME"); //$NON-NLS-1$
				long modified = rs.getTimestamp("MODIFIED").getTime(); //$NON-NLS-1$
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
			PMS.minimal("Scanner is already running !"); //$NON-NLS-1$
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
		PMS.minimal("Compacting database..."); //$NON-NLS-1$
		PMS.get().getFrame().setStatusLine("Compacting database..."); //$NON-NLS-1$
        String file = "database/backup.sql"; //$NON-NLS-1$
        try {
        	Script.execute(url, "sa", "", file); //$NON-NLS-1$ //$NON-NLS-2$
        	DeleteDbFiles.execute(dir, "medias", true); //$NON-NLS-1$
        	RunScript.execute(url, "sa", "", file, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception s) {
        	PMS.error("Error in compacting database: ", s); //$NON-NLS-1$
        } finally {
        	File testsql = new File(file);
        	if (testsql.exists()) {
        		if (!testsql.delete())
        			testsql.deleteOnExit();
        	}
        }
        PMS.get().getFrame().setStatusLine(null);
    }

}
