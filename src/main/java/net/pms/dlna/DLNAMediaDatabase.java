package net.pms.dlna;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

import net.pms.PMS;
import net.pms.configuration.FormatConfiguration;

import org.apache.commons.lang.StringUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.h2.tools.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

public class DLNAMediaDatabase implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(DLNAMediaDatabase.class);
	private String url;
	private String dir;
	public static final String NONAME = "###";
	private Thread scanner;
	private JdbcConnectionPool cp;

	public DLNAMediaDatabase(String name) {
		dir = "database";
		File fileDir = new File(dir);
		boolean defaultLocation = fileDir.mkdir() || fileDir.exists();
		if (defaultLocation) {
			// check if the database wasn't created during the installation run, with UAC activated.
			String to_delete = "to_delete";
			File checkDir = new File(to_delete);
			if (checkDir.exists()) {
				defaultLocation = checkDir.delete();
			} else {
				defaultLocation = checkDir.mkdir();
				if (defaultLocation) {
					defaultLocation = checkDir.delete();
				}
			}
		}
		if (Platform.isWindows() && !defaultLocation) {
			String profileDir = PMS.getConfiguration().getProfileDirectory();
			url = String.format("jdbc:h2:%s\\%s/%s", profileDir, dir, name);
			fileDir = new File(profileDir, dir);
		} else {
			url = "jdbc:h2:" + dir + "/" + name;
		}
		logger.debug("Using database URL: " + url);
		logger.info("Using database located at: " + fileDir.getAbsolutePath());

		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			logger.error(null, e);
		}

		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL(url);
		ds.setUser("sa");
		ds.setPassword("");
		cp = JdbcConnectionPool.create(ds);
	}

	private Connection getConnection() throws SQLException {
		return cp.getConnection();
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
				count = rs.getInt(1);
			}
			rs.close();
			stmt.close();

			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT VALUE FROM METADATA WHERE KEY = 'VERSION'");
			if (rs.next()) {
				version = rs.getString(1);
			}
		} catch (SQLException se) {
			logger.debug("Database not created or corrupted");
		} finally {
			close(rs);
			close(stmt);
			close(conn);
		}
		boolean force_reinit = !PMS.getVersion().equals(version); // here we can force a deletion for a specific version
		if (force || count == -1 || force_reinit) {
			logger.debug("Database will be (re)initialized");
//			if (force_reinit) {
//				JOptionPane.showMessageDialog(
//					(JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
//					Messages.getString("DLNAMediaDatabase.0"),
//					"Information",
//					JOptionPane.INFORMATION_MESSAGE);
//			}
			try {
				conn = getConnection();
				executeUpdate(conn, "DROP TABLE FILES");
				executeUpdate(conn, "DROP TABLE METADATA");
				executeUpdate(conn, "DROP TABLE REGEXP_RULES");
				executeUpdate(conn, "DROP TABLE AUDIOTRACKS");
				executeUpdate(conn, "DROP TABLE SUBTRACKS");
			} catch (SQLException se) {
				logger.debug("Caught exception", se);
			}
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("CREATE TABLE FILES (");
				sb.append("  ID                INT AUTO_INCREMENT");
				sb.append(", FILENAME          VARCHAR2(1024)       NOT NULL");
				sb.append(", MODIFIED          TIMESTAMP            NOT NULL");
				sb.append(", TYPE              INT");
				sb.append(", DURATION          DOUBLE");
				sb.append(", BITRATE           INT");
				sb.append(", WIDTH             INT");
				sb.append(", HEIGHT            INT");
				sb.append(", SIZE              NUMERIC");
				sb.append(", CODECV            VARCHAR2(32)");
				sb.append(", FRAMERATE         VARCHAR2(16)");
				sb.append(", ASPECT            VARCHAR2(16)");
				sb.append(", BITSPERPIXEL      INT");
				sb.append(", THUMB             BINARY");
				sb.append(", CONTAINER         VARCHAR2(32)");
				sb.append(", MODEL             VARCHAR2(128)");
				sb.append(", EXPOSURE          INT");
				sb.append(", ORIENTATION       INT");
				sb.append(", ISO               INT");
				sb.append(", MUXINGMODE        VARCHAR2(32)");
				sb.append(", constraint PK1 primary key (FILENAME, MODIFIED, ID))");
				executeUpdate(conn, sb.toString());
				sb = new StringBuilder();
				sb.append("CREATE TABLE AUDIOTRACKS (");
				sb.append("  FILEID            INT              NOT NULL");
				sb.append(", ID                INT              NOT NULL");
				sb.append(", LANG              VARCHAR2(3)");
				sb.append(", FLAVOR            VARCHAR2(128)");
				sb.append(", NRAUDIOCHANNELS   NUMERIC");
				sb.append(", SAMPLEFREQ        VARCHAR2(16)");
				sb.append(", CODECA            VARCHAR2(32)");
				sb.append(", BITSPERSAMPLE     INT");
				sb.append(", ALBUM             VARCHAR2(255)");
				sb.append(", ARTIST            VARCHAR2(255)");
				sb.append(", SONGNAME          VARCHAR2(255)");
				sb.append(", GENRE             VARCHAR2(64)");
				sb.append(", YEAR              INT");
				sb.append(", TRACK             INT");
				sb.append(", DELAY             INT");
				sb.append(", MUXINGMODE        VARCHAR2(32)");
				sb.append(", constraint PKAUDIO primary key (FILEID, ID))");
				executeUpdate(conn, sb.toString());
				sb = new StringBuilder();
				sb.append("CREATE TABLE SUBTRACKS (");
				sb.append("  FILEID            INT              NOT NULL");
				sb.append(", ID                INT              NOT NULL");
				sb.append(", LANG              VARCHAR2(3)");
				sb.append(", FLAVOR            VARCHAR2(128)");
				sb.append(", TYPE              INT");
				sb.append(", constraint PKSUB primary key (FILEID, ID))");

				executeUpdate(conn, sb.toString());
				executeUpdate(conn, "CREATE TABLE METADATA (KEY VARCHAR2(255) NOT NULL, VALUE VARCHAR2(255) NOT NULL)");
				executeUpdate(conn, "INSERT INTO METADATA VALUES ('VERSION', '" + PMS.getVersion() + "')");
				executeUpdate(conn, "CREATE INDEX IDXARTIST on AUDIOTRACKS (ARTIST asc);");
				executeUpdate(conn, "CREATE INDEX IDXALBUM on AUDIOTRACKS (ALBUM asc);");
				executeUpdate(conn, "CREATE INDEX IDXGENRE on AUDIOTRACKS (GENRE asc);");
				executeUpdate(conn, "CREATE INDEX IDXYEAR on AUDIOTRACKS (YEAR asc);");
				executeUpdate(conn, "CREATE TABLE REGEXP_RULES ( ID VARCHAR2(255) PRIMARY KEY, RULE VARCHAR2(255), ORDR NUMERIC);");
				executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '###', '(?i)^\\W.+', 0 );");
				executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '0-9', '(?i)^\\d.+', 1 );");
				for (int i = 65; i <= 90; i++) {
					executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '" + ((char) i) + "', '(?i)^" + ((char) i) + ".+', " + (i - 63) + " );");
				}
				{
				    	int i, j;
					i = 198; j = 28;
					executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '" + ((char) i) + "', '(?i)^" + ((char) i) + ".+', " + j + " );");
					i = 216; j = 29;
					executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '" + ((char) i) + "', '(?i)^" + ((char) i) + ".+', " + j + " );");
					i = 197; j = 30;
					executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '" + ((char) i) + "', '(?i)^" + ((char) i) + ".+', " + j + " );");
				}
				logger.debug("Database initialized");
			} catch (SQLException se) {
				logger.info("Error in table creation: " + se.getMessage());
			} finally {
			    close(conn);
			}
		} else {
			logger.debug("Database file count: " + count);
			logger.debug("Database version: " + version);
		}
	}

	private void executeUpdate(Connection conn, String sql) throws SQLException {
		if (conn != null) {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
		}
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
			logger.error(null, se);
			return false;
		} finally {
			close(rs);
			close(stmt);
			close(conn);
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
				int id = rs.getInt("ID");
				media.setDuration(toDouble(rs,"DURATION"));
				media.setBitrate(rs.getInt("BITRATE"));
				media.setWidth(rs.getInt("WIDTH"));
				media.setHeight(rs.getInt("HEIGHT"));
				media.setSize(rs.getLong("SIZE"));
				media.setCodecV(rs.getString("CODECV"));
				media.setFrameRate(rs.getString("FRAMERATE"));
				media.setAspect(rs.getString("ASPECT"));
				media.setBitsPerPixel(rs.getInt("BITSPERPIXEL"));
				media.setThumb(rs.getBytes("THUMB"));
				media.setContainer(rs.getString("CONTAINER"));
				media.setModel(rs.getString("MODEL"));
				if (media.getModel() != null && !FormatConfiguration.JPG.equals(media.getContainer())) {
					media.setExtrasAsString(media.getModel());
				}
				media.setExposure(rs.getInt("EXPOSURE"));
				media.setOrientation(rs.getInt("ORIENTATION"));
				media.setIso(rs.getInt("ISO"));
				media.setMuxingMode(rs.getString("MUXINGMODE"));
				media.setMediaparsed(true);
				PreparedStatement audios = conn.prepareStatement("SELECT * FROM AUDIOTRACKS WHERE FILEID = ?");
				audios.setInt(1, id);
				ResultSet subrs = audios.executeQuery();
				while (subrs.next()) {
					DLNAMediaAudio audio = new DLNAMediaAudio();
					audio.setId(subrs.getInt("ID"));
					audio.setLang(subrs.getString("LANG"));
					audio.setFlavor(subrs.getString("FLAVOR"));
					audio.setNrAudioChannels(subrs.getInt("NRAUDIOCHANNELS"));
					audio.setSampleFrequency(subrs.getString("SAMPLEFREQ"));
					audio.setCodecA(subrs.getString("CODECA"));
					audio.setBitsperSample(subrs.getInt("BITSPERSAMPLE"));
					audio.setAlbum(subrs.getString("ALBUM"));
					audio.setArtist(subrs.getString("ARTIST"));
					audio.setSongname(subrs.getString("SONGNAME"));
					audio.setGenre(subrs.getString("GENRE"));
					audio.setYear(subrs.getInt("YEAR"));
					audio.setTrack(subrs.getInt("TRACK"));
					audio.setDelay(subrs.getInt("DELAY"));
					audio.setMuxingModeAudio(subrs.getString("MUXINGMODE"));
					media.getAudioCodes().add(audio);
				}
				subrs.close();
				audios.close();

				PreparedStatement subs = conn.prepareStatement("SELECT * FROM SUBTRACKS WHERE FILEID = ?");
				subs.setInt(1, id);
				subrs = subs.executeQuery();
				while (subrs.next()) {
					DLNAMediaSubtitle sub = new DLNAMediaSubtitle();
					sub.setId(subrs.getInt("ID"));
					sub.setLang(subrs.getString("LANG"));
					sub.setFlavor(subrs.getString("FLAVOR"));
					sub.setType(subrs.getInt("TYPE"));
					media.getSubtitlesCodes().add(sub);
				}
				subrs.close();
				subs.close();

				list.add(media);
			}
		} catch (SQLException se) {
			logger.error(null, se);
			return null;
		} finally {
			close(rs);
			close(stmt);
			close(conn);
		}
		return list;
	}
	
	private Double toDouble(ResultSet rs, String column) throws SQLException {
		Object obj = rs.getObject(column);
		if (obj instanceof Double) {
			return (Double) obj;
		}
		return null;
	}

	public synchronized void insertData(String name, long modified, int type, DLNAMediaInfo media) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement("INSERT INTO FILES(FILENAME, MODIFIED, TYPE, DURATION, BITRATE, WIDTH, HEIGHT, SIZE, CODECV, FRAMERATE, ASPECT, BITSPERPIXEL, THUMB, CONTAINER, MODEL, EXPOSURE, ORIENTATION, ISO, MUXINGMODE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, name);
			ps.setTimestamp(2, new Timestamp(modified));
			ps.setInt(3, type);
			if (media != null) {
				if (media.getDuration() != null) {
					ps.setDouble(4, media.getDurationInSeconds());
				} else {
					ps.setNull(4, Types.DOUBLE);
				}
				ps.setInt(5, media.getBitrate());
				ps.setInt(6, media.getWidth());
				ps.setInt(7, media.getHeight());
				ps.setLong(8, media.getSize());
				ps.setString(9, media.getCodecV());
				ps.setString(10, media.getFrameRate());
				ps.setString(11, media.getAspect());
				ps.setInt(12, media.getBitsPerPixel());
				ps.setBytes(13, media.getThumb());
				ps.setString(14, media.getContainer());
				if (media.getExtras() != null) {
					ps.setString(15, media.getExtrasAsString());
				} else {
					ps.setString(15, media.getModel());
				}
				ps.setInt(16, media.getExposure());
				ps.setInt(17, media.getOrientation());
				ps.setInt(18, media.getIso());
				ps.setString(19, media.getMuxingModeAudio());

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
				ps.setString(19, null);
			}
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}
			rs.close();
			if (media != null && id > -1) {
				PreparedStatement insert = null;
				if (media.getAudioCodes().size() > 0) {
					insert = conn.prepareStatement("INSERT INTO AUDIOTRACKS VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				}
				for (DLNAMediaAudio audio : media.getAudioCodes()) {
					insert.clearParameters();
					insert.setInt(1, id);
					insert.setInt(2, audio.getId());
					insert.setString(3, audio.getLang());
					insert.setString(4, audio.getFlavor());
					insert.setInt(5, audio.getNrAudioChannels());
					insert.setString(6, audio.getSampleFrequency());
					insert.setString(7, audio.getCodecA());
					insert.setInt(8, audio.getBitsperSample());
					insert.setString(9, StringUtils.trimToEmpty(audio.getAlbum()));
					insert.setString(10, StringUtils.trimToEmpty(audio.getArtist()));
					insert.setString(11, StringUtils.trimToEmpty(audio.getSongname()));
					insert.setString(12, StringUtils.trimToEmpty(audio.getGenre()));
					insert.setInt(13, audio.getYear());
					insert.setInt(14, audio.getTrack());
					insert.setInt(15, audio.getDelay());
					insert.setString(16, StringUtils.trimToEmpty(audio.getMuxingModeAudio()));
					insert.executeUpdate();
				}

				if (media.getSubtitlesCodes().size() > 0) {
					insert = conn.prepareStatement("INSERT INTO SUBTRACKS VALUES (?, ?, ?, ?, ?)");
				}
				for (DLNAMediaSubtitle sub : media.getSubtitlesCodes()) {
					if (sub.getFile() == null) { // no save of external subtitles
						insert.clearParameters();
						insert.setInt(1, id);
						insert.setInt(2, sub.getId());
						insert.setString(3, sub.getLang());
						insert.setString(4, sub.getFlavor());
						insert.setInt(5, sub.getType());
						insert.executeUpdate();
					}
				}
				close(insert);
			}
		} catch (SQLException se) {
			if (se.getMessage().contains("[23001")) {
				logger.debug("Duplicate key while inserting this entry: " + name + " into the database: " + se.getMessage());
			} else {
				logger.error(null, se);
			}
		} finally {
			close(ps);
			close(conn);
		}
	}

	public synchronized void updateThumbnail(String name, long modified, int type, DLNAMediaInfo media) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement("UPDATE FILES SET THUMB = ? WHERE FILENAME = ? AND MODIFIED = ?");
			ps.setString(2, name);
			ps.setTimestamp(3, new Timestamp(modified));
			if (media != null) {
				ps.setBytes(1, media.getThumb());
			} else {
				ps.setNull(1, Types.BINARY);
			}
			ps.executeUpdate();
		} catch (SQLException se) {
			if (se.getMessage().contains("[23001")) {
				logger.debug("Duplicate key while inserting this entry: " + name + " into the database: " + se.getMessage());
			} else {
				logger.error(null, se);
			}
		} finally {
			close(ps);
			close(conn);
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
					if (!list.contains(NONAME)) {
						list.add(NONAME);
					}
				} else if (!list.contains(str)) {
					list.add(str);
				}
			}
		} catch (SQLException se) {
			logger.error(null, se);
			return null;
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return list;
	}

	public void cleanup() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			ps = conn.prepareStatement("SELECT COUNT(*) FROM FILES");
			rs = ps.executeQuery();
			int count = 0;

			if (rs.next()) {
				count = rs.getInt(1);
			}

			rs.close();
			ps.close();
			PMS.get().getFrame().setStatusLine("Cleanup database... 0%");
			int i = 0;
			int oldpercent = 0;

			if (count > 0) {
				ps = conn.prepareStatement("SELECT FILENAME, MODIFIED, ID FROM FILES", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
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
						PMS.get().getFrame().setStatusLine("Cleanup database... " + newpercent + "%");
						oldpercent = newpercent;
					}
				}
			}
		} catch (SQLException se) {
			logger.error(null, se);
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public ArrayList<File> getFiles(String sql) {
		ArrayList<File> list = new ArrayList<File>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql.toLowerCase().startsWith("select") ? sql : ("SELECT FILENAME, MODIFIED FROM FILES WHERE " + sql));
			rs = ps.executeQuery();
			while (rs.next()) {
				String filename = rs.getString("FILENAME");
				long modified = rs.getTimestamp("MODIFIED").getTime();
				File entry = new File(filename);
				if (entry.exists() && entry.lastModified() == modified) {
					list.add(entry);
				}
			}
		} catch (SQLException se) {
			logger.error(null, se);
			return null;
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return list;
	}

	private void close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			logger.error("error during closing:" + e.getMessage(), e);
		}
	}

	private void close(Statement ps) {
		try {
			if (ps != null) {
				ps.close();
			}
		} catch (SQLException e) {
			logger.error("error during closing:" + e.getMessage(), e);
		}
	}

	private void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			logger.error("error during closing:" + e.getMessage(), e);
		}
	}

	public synchronized boolean isScanLibraryRunning() {
		return scanner != null && scanner.isAlive();
	}

	public synchronized void scanLibrary() {
		if (scanner == null) {
			scanner = new Thread(this, "Library Scanner");
			scanner.start();
		} else if (scanner.isAlive()) {
			logger.info("Scanner is already running !");
		} else {
			scanner = new Thread(this, "Library Scanner");
			scanner.start();
		}
	}

	public synchronized void stopScanLibrary() {
		if (scanner != null && scanner.isAlive()) {
			PMS.get().getRootFolder(null).stopscan();
		}
	}

	public void run() {
		PMS.get().getRootFolder(null).scan();
	}

	public void compact() {
		logger.info("Compacting database...");
		PMS.get().getFrame().setStatusLine("Compacting database...");
		String file = "database/backup.sql";
		try {
			Script.execute(url, "sa", "", file);
			DeleteDbFiles.execute(dir, "medias", true);
			RunScript.execute(url, "sa", "", file, null, false);
		} catch (Exception s) {
			logger.error("Error in compacting database: ", s);
		} finally {
			File testsql = new File(file);
			if (testsql.exists() && !testsql.delete()) {
				testsql.deleteOnExit();
			}
		}
		PMS.get().getFrame().setStatusLine(null);
	}
}
