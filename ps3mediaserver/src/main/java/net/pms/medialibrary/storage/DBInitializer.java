package net.pms.medialibrary.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOTableColumnConfiguration;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.MediaLibraryConstants.MetaDataKeys;
import net.pms.medialibrary.commons.exceptions.StorageException;
import net.pms.medialibrary.commons.helpers.AutoFolderCreator;
import net.pms.medialibrary.commons.helpers.ConfigurationHelper;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DBInitializer {
	private static final Logger log = LoggerFactory.getLogger(DBInitializer.class);

	private final String DB_VERSION = "0.8";
	
	private String name;
	private IMediaLibraryStorage storage;
	private JdbcConnectionPool cp;

	DBInitializer(String name, IMediaLibraryStorage storage){
		if(log.isDebugEnabled()) log.debug("Initializing DBInitializer with name '" + name + "'");
		
		this.name = name;
		this.storage = storage;
		cp = getConnectionPool();
		
		if(log.isInfoEnabled()) log.info("DBInitializer initialized");
	}

	/*********************************************
	 * 
	 * Package Methods
	 * 
	 *********************************************/
	
	JdbcConnectionPool getConnectionPool(){
		if(cp != null){
			return cp;
		} else {
			String dbPath = ConfigurationHelper.getDbDir() +  name;
	    	if(log.isInfoEnabled()) log.info("Medialibrary database location = '" + dbPath + "'");
	    	String url = "jdbc:h2:" + dbPath;
	
	    	String driverName = "org.h2.Driver";
	    	try {
	    		Class.forName(driverName);
	    		if(log.isDebugEnabled()) log.debug("Loaded driver '" + driverName + "'");
	    	} catch (ClassNotFoundException e) {
	    		log.error("Failed to load database driver named '" + driverName + "'", e);
	    		return null;
	    	}
	
	    	JdbcDataSource ds = new JdbcDataSource();
	    	ds.setURL(url);
	    	ds.setUser("sa");
	    	ds.setPassword("");
	    	return(JdbcConnectionPool.create(ds));
		}
	}

	void configureDb() {
		String realStorageVersion = storage.getStorageVersion();
		if(realStorageVersion == null){
			if(log.isInfoEnabled()) log.info("Reinitializing DB because the version number could not be found. Create DB version " + DB_VERSION);
			initDb();			
		}
		else if(DB_VERSION.equals(realStorageVersion)){
			if(log.isInfoEnabled()) log.info(String.format("Database version %s is up and running", DB_VERSION));
		} else {
			double newestDbVersion;
			double runningDbVersion;
			try {
				newestDbVersion = Double.parseDouble(DB_VERSION);
				runningDbVersion = Double.parseDouble(realStorageVersion);
				if(!(newestDbVersion > runningDbVersion)){
					log.info(String.format("Don't update DB. newestDbVersion='%s' or runningDbVersion='%s'", DB_VERSION, realStorageVersion));
				}
			} catch(Exception ex){
				log.error(String.format("Failed to parse newestDbVersion='%s' or runningDbVersion='%s'", DB_VERSION, realStorageVersion));
			}
			
			if(log.isInfoEnabled()) log.info(String.format("Updating DB from version %s to %s", realStorageVersion, DB_VERSION));
			updateDb(realStorageVersion);
		}
    }

	void resetDb(){
		initDb();
	}
	
	boolean isConnected(){
		boolean res = false;

		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			res = !conn.isClosed();
		} catch (SQLException se) {
			res = false;
		} finally {
			DBBase.close(conn, stmt);
    	}
    	
    	return res;
	}
	
	boolean isInitialized(){		
		boolean res = false;

		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("SELECT VALUE FROM METADATA");
			stmt.executeQuery();
			res = true;
		} catch (SQLException se) {
			//do nothing
		} finally {
			DBBase.close(conn, stmt);
    	}
    	
    	return res;		
	}
	
	/*********************************************
	 * 
	 * Private Methods
	 * 
	 *********************************************/
	
	private void initDb() {
		if(log.isInfoEnabled()) log.info("Start initializing the database");
		Connection conn = null;
		Statement stmt = null;
		
		try { 
			conn = cp.getConnection(); 
			stmt = conn.createStatement();
		} catch (SQLException se) {
			log.error("Failed to initialize DB because no DB connection could be made", se);
			return;
		}		
		
		//Try to delete all the tables
		//If they don't exist (or another problem occurs..) the exception is caught and nothing done with it
		try { stmt.executeUpdate("DROP TABLE FILE"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE VIDEO"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE VIDEOAUDIO"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE AUDIO"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE PICTURES"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE FILEPLAYS"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE SUBTITLES"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE METADATA"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE FOLDERS"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE MEDIALIBRARYFOLDERS"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE SPECIALFOLDERS"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE CONDITIONS"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE THUMBNAILPRIORITIES"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE FOLDERTHUMBNAILPRIORITIES"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE FILEENTRYTHUMBNAILPRIORITIES"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE FILETAGS"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE TEMPLATE"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE TEMPLATEENTRY"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE MANAGEDFOLDERS"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE TABLECOLUMNCONFIGURATION"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE FILEIMPORTTEMPLATE"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE FILEIMPORTTEMPLATEENTRY"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE FILEIMPORTTEMPLATEACTIVEENGINE"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE FILEIMPORTTEMPLATETAGS"); } catch (SQLException se) {}
		try { stmt.executeUpdate("DROP TABLE QUICKTAG"); } catch (SQLException se) {}
		if(log.isInfoEnabled()) log.info("All database tables dropped");
				
		try {
			//Create table FILE
			StringBuffer sb = new StringBuffer();
			sb.append("CREATE TABLE FILE (");
			sb.append("  ID                BIGINT AUTO_INCREMENT");
			sb.append(", FOLDERPATH   	   VARCHAR_IGNORECASE(1024)       NOT NULL");
			sb.append(", FILENAME   	   VARCHAR_IGNORECASE(1024)       NOT NULL");
			sb.append(", TYPE   		   VARCHAR(256)       NOT NULL");
			sb.append(", SIZEBYTE   		   BIGINT");
			sb.append(", DATELASTUPDATEDDB DATETIME             NOT NULL");
			sb.append(", DATEINSERTEDDB    DATETIME             NOT NULL");
			sb.append(", DATEMODIFIEDOS    DATETIME             NOT NULL");
			sb.append(", THUMBNAILPATH     VARCHAR_IGNORECASE(1024)");
			sb.append(", PLAYCOUNT   	   INT DEFAULT 0");
			sb.append(", ENABLED           BIT");
			sb.append(", CONSTRAINT PK_FILE PRIMARY KEY (ID)");
			sb.append(", CONSTRAINT UC_FILEPATH UNIQUE (FOLDERPATH, FILENAME))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_FILE_DATELASTUPDATED ON FILE (DATELASTUPDATEDDB desc);");
			stmt.executeUpdate("CREATE INDEX IDX_FILE_SIZE ON FILE (SIZEBYTE desc);");
			stmt.executeUpdate("CREATE INDEX IDX_FILE_TYPE ON FILE (TYPE asc);");
			stmt.executeUpdate("CREATE INDEX IDX_FILE_PLAYCOUNT ON FILE (PLAYCOUNT asc);");
			if(log.isDebugEnabled()) log.debug("Table FILE created");

			//Create table VIDEO
			sb = new StringBuffer();
			sb.append("CREATE TABLE VIDEO (");
			sb.append("  ID                BIGINT AUTO_INCREMENT");
			sb.append(", FILEID            BIGINT UNIQUE REFERENCES FILE(ID)");
			sb.append(", AGERATINGLEVEL    VARCHAR_IGNORECASE(256)");
			sb.append(", AGERATINGREASON   VARCHAR_IGNORECASE(512)");
			sb.append(", RATINGPERCENT     INT");
			sb.append(", RATINGVOTERS      INT");
			sb.append(", DIRECTOR          VARCHAR_IGNORECASE(512)");
			sb.append(", OVERVIEW          VARCHAR_IGNORECASE(2056)");
			sb.append(", TAGLINE           VARCHAR_IGNORECASE(512)");
			sb.append(", NAME              VARCHAR_IGNORECASE(512)");
			sb.append(", ORIGINALNAME      VARCHAR_IGNORECASE(512)");
			sb.append(", SORTNAME          VARCHAR_IGNORECASE(512)");
			sb.append(", TMDBID            INT");
			sb.append(", IMDBID            VARCHAR_IGNORECASE(32)");
			sb.append(", TRAILERURL        VARCHAR_IGNORECASE(1024)");
			sb.append(", HOMEPAGEURL       VARCHAR_IGNORECASE(1024)");
			sb.append(", BUDGET            INT");
			sb.append(", REVENUE           INT");
			sb.append(", ASPECTRATIO       VARCHAR_IGNORECASE(16)");
			sb.append(", BITRATE           VARCHAR_IGNORECASE(128)");
			sb.append(", BITSPERPIXEL      INT");
			sb.append(", CODECV       	   VARCHAR_IGNORECASE(32)");
			sb.append(", DURATIONSEC       NUMERIC");
			sb.append(", CONTAINER         VARCHAR_IGNORECASE(32)");
			sb.append(", DVDTRACK      	   INT");
			sb.append(", FRAMERATE         VARCHAR_IGNORECASE(16)");
			sb.append(", HEIGHT            INT");
			sb.append(", MIMETYPE          VARCHAR_IGNORECASE(32)");
			sb.append(", MODEL             VARCHAR_IGNORECASE(128)");
			sb.append(", MUXABLE           BIT");
			sb.append(", WIDTH             INT");
			sb.append(", YEAR              INT");
			sb.append(", MUXINGMODE        VARCHAR2(32)");
			sb.append(", CONSTRAINT PK_VIDEO PRIMARY KEY (ID))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_VIDEO_RATINGPERCENT ON VIDEO (RATINGPERCENT asc);");
			stmt.executeUpdate("CREATE INDEX IDX_VIDEO_NAME ON VIDEO (NAME asc);");
			stmt.executeUpdate("CREATE INDEX IDX_VIDEO_ORIGINALNAME ON VIDEO (ORIGINALNAME asc);");
			stmt.executeUpdate("CREATE INDEX IDX_VIDEO_YEAR ON VIDEO (YEAR asc);");
			if(log.isDebugEnabled()) log.debug("Table VIDEO created");
			
			//Create table PICTURES
			sb = new StringBuffer();
			sb.append("CREATE TABLE PICTURES (");
			sb.append("  ID                BIGINT AUTO_INCREMENT");
			sb.append(", FILEID            BIGINT UNIQUE REFERENCES FILE(ID)");
			sb.append(", WIDTH             INT");
			sb.append(", HEIGHT            INT");
			sb.append(", EXPOSURE          INT");
			sb.append(", ISO               INT");
			sb.append(", ORIENTATION       INT");
			sb.append(", CONSTRAINT PK_PICTURES PRIMARY KEY (ID))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_PICTURES_WIDTH ON PICTURES (WIDTH asc);");
			stmt.executeUpdate("CREATE INDEX IDX_PICTURES_HEIGHT ON PICTURES (HEIGHT asc);");
			if(log.isDebugEnabled()) log.debug("Table PICTURES created");
			
			//Create table AUDIO
			sb = new StringBuffer();
			sb.append("CREATE TABLE AUDIO (");
			sb.append("  ID                BIGINT AUTO_INCREMENT");
			sb.append(", FILEID            BIGINT UNIQUE REFERENCES FILE(ID)");
			sb.append(", NRAUDIOCHANNELS   NUMERIC");
			sb.append(", SAMPLEFREQ        VARCHAR_IGNORECASE(16)");
			sb.append(", CODECA            VARCHAR_IGNORECASE(32)");
			sb.append(", BITSPERSAMPLE     INT");
			sb.append(", ALBUM             VARCHAR_IGNORECASE(255)");
			sb.append(", ARTIST            VARCHAR_IGNORECASE(255)");
			sb.append(", SONGNAME          VARCHAR_IGNORECASE(255)");
			sb.append(", GENRE             VARCHAR_IGNORECASE(64)");
			sb.append(", YEAR              INT");
			sb.append(", TRACK             INT");
			sb.append(", DURATIONSEC       INT");
			sb.append(", MUXINGMODE        VARCHAR2(32)");
			sb.append(", COVERPATH         VARCHAR_IGNORECASE(1024)");
			sb.append(", CONSTRAINT PK_AUDIO PRIMARY KEY (ID))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_AUDIO_ARTIST ON AUDIO (ARTIST asc);");
			stmt.executeUpdate("CREATE INDEX IDX_AUDIO_ALBUM ON AUDIO (ALBUM asc);");
			stmt.executeUpdate("CREATE INDEX IDX_AUDIO_GENRE ON AUDIO (GENRE asc);");
			stmt.executeUpdate("CREATE INDEX IDX_AUDIO_YEAR ON AUDIO (YEAR asc);");
			if(log.isDebugEnabled()) log.debug("Table AUDIO created");
			
			//Create table VIDEOAUDIO
			sb = new StringBuffer();
			sb.append("CREATE TABLE VIDEOAUDIO (");
			sb.append("  ID                BIGINT AUTO_INCREMENT");
			sb.append(", FILEID            BIGINT REFERENCES FILE(ID)");
			sb.append(", LANG              VARCHAR_IGNORECASE(3)");
			sb.append(", NRAUDIOCHANNELS   NUMERIC");
			sb.append(", SAMPLEFREQ        VARCHAR_IGNORECASE(16)");
			sb.append(", CODECA            VARCHAR_IGNORECASE(32)");
			sb.append(", BITSPERSAMPLE     INT");
			sb.append(", DELAYMS           INT");
			sb.append(", MUXINGMODE        VARCHAR2(32)");
			sb.append(", CONSTRAINT PK_VIDEOAUDIO PRIMARY KEY (ID))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_VIDEOAUDIO_FILEID ON VIDEOAUDIO (FILEID asc);");
			stmt.executeUpdate("CREATE INDEX IDX_VIDEOAUDIO_LANG ON VIDEOAUDIO (LANG asc);");
			if(log.isDebugEnabled()) log.debug("Table VIDEOAUDIO created");
			
			//Create table SUBTITLES (that will reference a file that is a video)
			sb = new StringBuffer();
			sb.append("CREATE TABLE SUBTITLES (");
			sb.append("  ID                BIGINT AUTO_INCREMENT");
			sb.append(", FILEID            BIGINT REFERENCES FILE(ID)");
			sb.append(", FILEPATH          VARCHAR_IGNORECASE(1024) DEFAULT ''");
			sb.append(", LANG              VARCHAR_IGNORECASE(3)");
			sb.append(", TYPE              INT");
			sb.append(", CONSTRAINT PK_SUBTITLES PRIMARY KEY (ID))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_SUBTITLES_LANG ON SUBTITLES (LANG asc);");
			stmt.executeUpdate("CREATE INDEX IDX_SUBTITLES_FILEID ON SUBTITLES (FILEID asc);");
			if(log.isDebugEnabled()) log.debug("Table SUBTITLES created");
			
			//Create table FILEPLAYS
			sb = new StringBuffer();
			sb.append("CREATE TABLE FILEPLAYS (");
			sb.append("  FILEID            BIGINT REFERENCES FILE(ID)");
			sb.append(", PLAYTIMESEC       INT");
			sb.append(", DATEPLAYEND       TIMESTAMP");
			sb.append(", CONSTRAINT PK_FILEPLAYS PRIMARY KEY (FILEID, DATEPLAYEND))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table FILEPLAYS created");
			
			//Create table TEMPLATE
			sb = new StringBuffer();
			sb.append("CREATE TABLE TEMPLATE (");
			sb.append("  ID                BIGINT AUTO_INCREMENT");
			sb.append(", NAME              VARCHAR_IGNORECASE(256) UNIQUE");
			sb.append(", CONSTRAINT PK_TEMPLATE PRIMARY KEY (ID))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_TEMPLATE_NAME ON TEMPLATE (NAME asc);");
			if(log.isDebugEnabled()) log.debug("Table TEMPLATE created");
			
			//Create table TEMPLATEENTRY
			sb = new StringBuffer();
			sb.append("CREATE TABLE TEMPLATEENTRY (");
			sb.append("  ID                BIGINT AUTO_INCREMENT");
			sb.append(", PARENTID          BIGINT");
			sb.append(", TEMPLATEID        BIGINT REFERENCES TEMPLATE(ID)");
			sb.append(", POSITIONINPARENT  INT");
			sb.append(", MAXLINELENGTH     INT");
			sb.append(", DISPLAYNAMEMASK   VARCHAR_IGNORECASE(1024)");
			sb.append(", ENTRYTYPE         VARCHAR_IGNORECASE(256)");
			sb.append(", FILEDISPLAYMODE   VARCHAR_IGNORECASE(256)");
			sb.append(", PLUGIN            VARCHAR(1024)");
			sb.append(", PLUGINCONFIG      VARCHAR(2048)");
			sb.append(", CONSTRAINT PK_TEMPLATEENTRY PRIMARY KEY (TEMPLATEID, PARENTID, POSITIONINPARENT))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table TEMPLATEENTRY created");
			
			//Create table FOLDERS
			sb = new StringBuffer();
			sb.append("CREATE TABLE FOLDERS (");
			sb.append("  ID                BIGINT AUTO_INCREMENT");
			sb.append(", PARENTID          BIGINT");
			sb.append(", NAME              VARCHAR_IGNORECASE(512)");
			sb.append(", TYPE              VARCHAR(128)");
			sb.append(", POSITIONINPARENT  INT");
			sb.append(", CONSTRAINT PK_FOLDERS PRIMARY KEY (ID))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_FOLDERS_PARENTID ON FOLDERS (PARENTID asc);");
			if(log.isDebugEnabled()) log.debug("Table FOLDERS created");
			
			//Create table MEDIALIBRARYFOLDERS
			sb = new StringBuffer();
			sb.append("CREATE TABLE MEDIALIBRARYFOLDERS (");
			sb.append("  FOLDERID          BIGINT REFERENCES FOLDERS(ID)");
			sb.append(", EQUATION          VARCHAR_IGNORECASE(1024)");
			sb.append(", DISPLAYITEMS      BIT");
			sb.append(", INHERITSCONDITIONS BIT");
			sb.append(", FILETYPE 		   VARCHAR_IGNORECASE(16)");
			sb.append(", DISPLAYNAMEMASK   VARCHAR_IGNORECASE(1024)");
			sb.append(", DISPLAYTYPE       VARCHAR_IGNORECASE(64)");
			sb.append(", TEMPLATEID        BIGINT DEFAULT -1");
			sb.append(", INHERITSSORT      BIT");
			sb.append(", INHERITDISPLAYFILES BIT");
			sb.append(", SORTASCENDING     BIT");
			sb.append(", SORTTYPE          VARCHAR_IGNORECASE(64)");
			sb.append(", SORTOPTION        VARCHAR_IGNORECASE(64)");
			sb.append(", MAXFILES          INT DEFAULT 0");
			sb.append(", CONSTRAINT PK_MEDIALIBRARYFOLDERS PRIMARY KEY (FOLDERID))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table MEDIALIBRARYFOLDERS created");
			
			//Create table SPECIALFOLDERS
			sb = new StringBuffer();
			sb.append("CREATE TABLE SPECIALFOLDERS (");
			sb.append("  FOLDERID          BIGINT REFERENCES FOLDERS(ID)");
			sb.append(", CLASSNAME		   VARCHAR(512)");
			sb.append(", SAVEFILEPATH      VARCHAR_IGNORECASE(1024)");
			sb.append(", CONSTRAINT PK_SPECIALFOLDERS PRIMARY KEY (FOLDERID))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table SPECIALFOLDERS created");
			
			//Create table CONDITIONS
			sb = new StringBuffer();
			sb.append("CREATE TABLE CONDITIONS (");
			sb.append("  FOLDERID          BIGINT REFERENCES MEDIALIBRARYFOLDERS(FOLDERID)");
			sb.append(", NAME              VARCHAR_IGNORECASE(256)");
			sb.append(", TYPE     		   VARCHAR(256)");
			sb.append(", OPERATOR 		   VARCHAR(256)");
			sb.append(", CONDITION         VARCHAR_IGNORECASE(1024)");
			sb.append(", VALUETYPE		   VARCHAR(256)");
			sb.append(", UNIT   		   VARCHAR(256)");
			sb.append(", TAGNAME   		   VARCHAR(512)");
			sb.append(", CONSTRAINT PK_CONDITIONS PRIMARY KEY (FOLDERID, TYPE, OPERATOR, CONDITION))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_CONDITIONS_FOLDERID ON CONDITIONS (FOLDERID asc);");
			if(log.isDebugEnabled()) log.debug("Table CONDITIONS created");
			
			//Create table FILETAGS
			sb = new StringBuffer();
			sb.append("CREATE TABLE FILETAGS (");
			sb.append("  FILEID            BIGINT REFERENCES FILE(ID)");
			sb.append(", KEY               VARCHAR_IGNORECASE(256)");
			sb.append(", VALUE  	       VARCHAR_IGNORECASE(256)");
			sb.append(", CONSTRAINT PK_FILETAGS PRIMARY KEY (FILEID, KEY, VALUE))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_FILETAGS_FILEID ON FILETAGS (FILEID asc);");
			stmt.executeUpdate("CREATE INDEX IDX_FILETAGS_KEY ON FILETAGS (KEY asc);");
			if(log.isDebugEnabled()) log.debug("Table FILETAGS created");
			
			//Create table THUMBNAILPRIORITIES
			sb = new StringBuffer();
			sb.append("CREATE TABLE THUMBNAILPRIORITIES (");
			sb.append("  ID		           BIGINT AUTO_INCREMENT PRIMARY KEY");
			sb.append(", THUMBNAILPRIORITYTYPE VARCHAR(128)");
			sb.append(", SEEKSEC       	   INT");
			sb.append(", PICTUREPATH       VARCHAR_IGNORECASE(2048)");
			sb.append(", CONSTRAINT PK_THUMBNAILPRIORITIES PRIMARY KEY (ID)");
			sb.append(", CONSTRAINT UC_THUMBNAILPRIORITIES UNIQUE (THUMBNAILPRIORITYTYPE, SEEKSEC, PICTUREPATH))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table THUMBNAILPRIORITIES created");
			
			//Create table FOLDERTHUMBNAILPRIORITIES
			sb = new StringBuffer();
			sb.append("CREATE TABLE FOLDERTHUMBNAILPRIORITIES (");
			sb.append("  FOLDERID          BIGINT REFERENCES MEDIALIBRARYFOLDERS(FOLDERID)");
			sb.append(", THUMBNAILPRIORITIESID BIGINT REFERENCES THUMBNAILPRIORITIES(ID)");
			sb.append(", PRIORITYINDEX     INT");
			sb.append(", CONSTRAINT PK_FILETHUMBNAILPRIORITIES PRIMARY KEY (FOLDERID, PRIORITYINDEX, THUMBNAILPRIORITIESID))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table FOLDERTHUMBNAILPRIORITIES created");
			
			//Create table FILEENTRYTHUMBNAILPRIORITIES
			sb = new StringBuffer();
			sb.append("CREATE TABLE FILEENTRYTHUMBNAILPRIORITIES (");
			sb.append("  FOLDERID          BIGINT REFERENCES TEMPLATEENTRY(ID)");
			sb.append(", THUMBNAILPRIORITIESID BIGINT REFERENCES THUMBNAILPRIORITIES(ID)");
			sb.append(", TEMPLATEID        BIGINT REFERENCES TEMPLATE(ID)");
			sb.append(", PRIORITYINDEX     INT");
			sb.append(", CONSTRAINT PK_FILEENTRYTHUMBNAILPRIORITIES PRIMARY KEY (FOLDERID, THUMBNAILPRIORITIESID))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table FILEENTRYTHUMBNAILPRIORITIES created");
			
			//Create table FILEIMPORTTEMPLATE
			sb = new StringBuffer();
			sb.append("CREATE TABLE FILEIMPORTTEMPLATE (");
			sb.append("  ID             INT AUTO_INCREMENT");
			sb.append(", NAME		 	VARCHAR(64)");
			sb.append(", CONSTRAINT PK_FILEIMPORTTEMPLATE PRIMARY KEY (ID))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table FILEIMPORTTEMPLATE created");
			
			//Create table FILEIMPORTTEMPLATEENTRY
			sb = new StringBuffer();
			sb.append("CREATE TABLE FILEIMPORTTEMPLATEENTRY (");
			sb.append("  TEMPLATEID     INT REFERENCES FILEIMPORTTEMPLATE(ID)");
			sb.append(", FILEPROPERTY	VARCHAR(64)");
			sb.append(", ENGINENAME		VARCHAR(64)");
			sb.append(", PRIO			INT");
			sb.append(", ISENABLED		BIT");
			sb.append(", CONSTRAINT PK_FILEIMPORTTEMPLATEENTRY PRIMARY KEY (TEMPLATEID, FILEPROPERTY, ENGINENAME))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_FILEIMPORTTEMPLATEENTRY_TEMPLATEID ON FILEIMPORTTEMPLATEENTRY (TEMPLATEID asc);");
			if(log.isDebugEnabled()) log.debug("Table FILEIMPORTTEMPLATEENTRY created");
			
			//Create table FILEIMPORTTEMPLATEACTIVEENGINE
			sb = new StringBuffer();
			sb.append("CREATE TABLE FILEIMPORTTEMPLATEACTIVEENGINE (");
			sb.append("  TEMPLATEID     INT REFERENCES FILEIMPORTTEMPLATE(ID)");
			sb.append(", FILETYPE	VARCHAR(64)");
			sb.append(", ENGINENAME		VARCHAR(64)");
			sb.append(", CONSTRAINT PK_FILEIMPORTTEMPLATEACTIVEENGINE PRIMARY KEY (TEMPLATEID, FILETYPE, ENGINENAME))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_FILEIMPORTTEMPLATEACTIVEENGINE_TEMPLATEID ON FILEIMPORTTEMPLATEACTIVEENGINE (TEMPLATEID asc);");
			if(log.isDebugEnabled()) log.debug("Table FILEIMPORTTEMPLATEACTIVEENGINE created");
			
			//Create table FILEIMPORTTEMPLATETAGS
			sb = new StringBuffer();
			sb.append("CREATE TABLE FILEIMPORTTEMPLATETAGS (");
			sb.append("  TEMPLATEID     INT REFERENCES FILEIMPORTTEMPLATE(ID)");
			sb.append(", FILETYPE	VARCHAR(64)");
			sb.append(", ENGINENAME		VARCHAR(64)");
			sb.append(", TAGNAME		VARCHAR(128)");
			sb.append(", CONSTRAINT PK_FILEIMPORTTEMPLATETAGS PRIMARY KEY (TEMPLATEID, FILETYPE, ENGINENAME, TAGNAME))");
			stmt.executeUpdate(sb.toString());
			stmt.executeUpdate("CREATE INDEX IDX_FILEIMPORTTEMPLATETAGS_TEMPLATEID ON FILEIMPORTTEMPLATETAGS (TEMPLATEID asc);");
			if(log.isDebugEnabled()) log.debug("Table FILEIMPORTTEMPLATETAGS created");
			
			//Create table MANAGEDFOLDERS
			sb = new StringBuffer();
			sb.append("CREATE TABLE MANAGEDFOLDERS (");
			sb.append("  WATCH          BIT");
			sb.append(", FOLDERPATH 	VARCHAR(1024)");
			sb.append(", VIDEO     		BIT");
			sb.append(", AUDIO     		BIT");
			sb.append(", PICTURES 		BIT");
			sb.append(", FILEIMPORTTEMPLATEID INT REFERENCES FILEIMPORTTEMPLATE(ID)");
			sb.append(", ISFILEIMPORTENABLED BIT");
			sb.append(", SUBFOLDERS		BIT");
			sb.append(", CONSTRAINT PK_MANAGEDFOLDERS PRIMARY KEY (WATCH, FOLDERPATH, VIDEO, AUDIO, PICTURES, SUBFOLDERS))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table MANAGEDFOLDERS created");

			//Create table TABLECOLUMNCONFIGURATION
			sb = new StringBuffer();
			sb.append("CREATE TABLE TABLECOLUMNCONFIGURATION (");
			sb.append("  FILETYPE       VARCHAR(256)");
			sb.append(", CONDITIONTYPE  VARCHAR(256)");
			sb.append(", COLUMNINDEX    INT");
			sb.append(", WIDTH          INT");
			sb.append(", CONSTRAINT UC_TABLECOLUMNCONFIGURATION UNIQUE (FILETYPE, COLUMNINDEX)");
			sb.append(", CONSTRAINT PK_TABLECOLUMNCONFIGURATION PRIMARY KEY (FILETYPE, CONDITIONTYPE))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table TABLECOLUMNCONFIGURATION created");

			//Create table QUICKTAG
			sb = new StringBuffer();
			sb.append("CREATE TABLE QUICKTAG (");
			sb.append("  NAME           VARCHAR(256)");
			sb.append(", TAGNAME        VARCHAR(1024)");
			sb.append(", TAGVALUE       VARCHAR(1024)");
			sb.append(", VIRTUALKEY     INT");
			sb.append(", KEYCOMBINATION VARCHAR(128))");
			stmt.executeUpdate(sb.toString());
			if(log.isDebugEnabled()) log.debug("Table QUICKTAG created");
			
			//Create and populate table METADATA
			stmt.executeUpdate("CREATE TABLE METADATA (KEY VARCHAR2(255) NOT NULL, VALUE VARCHAR2(255) NOT NULL, CONSTRAINT PK_METADATA PRIMARY KEY (KEY, VALUE))");
			stmt.executeUpdate("CREATE INDEX IDX_METADATA_KEY ON METADATA (KEY asc);");
			if(log.isDebugEnabled()) log.debug("Table METADATA created");
			if(log.isInfoEnabled()) log.info("All database tables created");
			
			insertDefaultValues(stmt, conn);
				
			if(log.isInfoEnabled()) log.info("Media Library Database initialized");
		} catch (SQLException se) {
			log.error("Failed ti initialize database ", se);
		} finally {
			DBBase.close(conn, stmt);
		}
	}
	
	private void insertDefaultValues(Statement stmt, Connection conn) throws SQLException{
		// METADATA
		String pictureSaveFilePath = PMS.getConfiguration().getProfileDirectory() + File.separatorChar + "pictures";
		File dir = new File(pictureSaveFilePath);
		if(!dir.isDirectory()){
			dir.mkdirs();
		}
		stmt.executeUpdate("INSERT INTO METADATA VALUES ('" + MetaDataKeys.PICTURE_SAVE_FOLDER_PATH + "', '" + pictureSaveFilePath + "')");
		stmt.executeUpdate("INSERT INTO METADATA (KEY, VALUE) VALUES ('" + MetaDataKeys.VERSION + "', '" + DB_VERSION + "')");
		stmt.executeUpdate("INSERT INTO METADATA (KEY, VALUE) VALUES ('" + MetaDataKeys.MAX_LINE_LENGTH + "', '" + "60" + "')");
		stmt.executeUpdate("INSERT INTO METADATA (KEY, VALUE) VALUES ('" + MetaDataKeys.MEDIA_LIBRARY_ENABLE + "', 'TRUE')");
		stmt.executeUpdate("INSERT INTO METADATA (KEY, VALUE) VALUES ('" + MetaDataKeys.OMIT_PREFIXES + "', 'the le la les')");
		stmt.executeUpdate("INSERT INTO METADATA (KEY, VALUE) VALUES ('" + MetaDataKeys.OMIT_SORT + "', 'TRUE')");
		stmt.executeUpdate("INSERT INTO METADATA (KEY, VALUE) VALUES ('" + MetaDataKeys.OMIT_FILTER + "', 'TRUE')");
		if(log.isInfoEnabled()) log.info("Default metadata values inserted into database");
		
		//create default import template
		//it won't have any active plugins defined, it's up to the user to configure it for first time use
		DOFileImportTemplate template = new DOFileImportTemplate(-1, Messages.getString("ML.FileImportTemplate.DefaultTemplateName"), null, null, null);
		try {
			new DBFileImport(cp).insertTemplate(template);
		} catch (StorageException e) {
			log.error("Failed to insert default file import template", e);
		}

		//Create default table columns
		insertDefaultLibraryViewColumns();
		
		//insert default folder structure for the tree view
		AutoFolderCreator.addInitialFolderStructure(storage);		
	}
	
	private void insertDefaultLibraryViewColumns(){
		//video
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_ISACTIF, 0, 36), FileType.VIDEO);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_FOLDERPATH, 1, 245), FileType.VIDEO);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_FILENAME, 2, 160), FileType.VIDEO);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.VIDEO_NAME, 3, 160), FileType.VIDEO);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.VIDEO_WIDTH, 4, 42), FileType.VIDEO);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.VIDEO_HEIGHT, 5, 48), FileType.VIDEO);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.VIDEO_DURATIONSEC, 6, 57), FileType.VIDEO);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_DATEINSERTEDDB, 7, 164), FileType.VIDEO);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_PLAYCOUNT, 8, 49), FileType.VIDEO);
		
		//files
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_ISACTIF, 0, 36), FileType.FILE);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_FOLDERPATH, 1, 245), FileType.FILE);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_FILENAME, 2, 160), FileType.FILE);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_DATEINSERTEDDB, 3, 100), FileType.FILE);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_PLAYCOUNT, 4, 42), FileType.FILE);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILEPLAYS_DATEPLAYEND, 5, 100), FileType.FILE);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_DATELASTUPDATEDDB, 6, 100), FileType.FILE);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_DATEMODIFIEDOS, 7, 100), FileType.FILE);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_SIZEBYTE, 8, 53), FileType.FILE);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_TYPE, 9, 53), FileType.FILE);
		insertTableColumnConfiguration(new DOTableColumnConfiguration(ConditionType.FILE_CONTAINS_TAG, 10, 100), FileType.FILE);
		
		if(log.isInfoEnabled()) log.info("Default library view columns inserted");
	}
	
	private void updateDb(String realStorageVersion) {
		if(realStorageVersion.equals("0.1")){
			updateDb01_02();
			realStorageVersion = "0.2";
		}
		if(realStorageVersion.equals("0.2")){
			updateDb02_03();
			realStorageVersion = "0.3";
		}
		if(realStorageVersion.equals("0.3")){
			updateDb03_04();
			realStorageVersion = "0.4";
		}
		if(realStorageVersion.equals("0.4")){
			updateDb04_05();
			realStorageVersion = "0.5";
		}
		if(realStorageVersion.equals("0.5")){
			updateDb05_06();
			realStorageVersion = "0.6";
		}
		if(realStorageVersion.equals("0.6")){
			updateDb06_07();
			realStorageVersion = "0.7";
		}
		if(realStorageVersion.equals("0.7")){
			updateDb07_08();
			realStorageVersion = "0.8";
		}
	}

	private void updateDb01_02() {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			//do updates
			stmt = conn.prepareStatement("ALTER TABLE MEDIALIBRARYFOLDERS ADD MAXFILES INT DEFAULT 0");
			stmt.executeUpdate();
			
			//update db version
			storage.setMetaDataValue(MetaDataKeys.VERSION.toString(), "0.2");
			if(log.isInfoEnabled()) log.info("Updated DB from version 0.1 to 0.2");
		} catch (SQLException se) {
			log.error("Failed to update DB from version 0.1 to 0.2", se);
		} finally {
			DBBase.close(conn, stmt);
    	}
	}

	private void updateDb02_03() {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			//do updates

			//Create table MANAGEDFOLDERS
			StringBuffer sb = new StringBuffer();
			sb.append("CREATE TABLE TABLECOLUMNCONFIGURATION (");
			sb.append("  FILETYPE       VARCHAR(256)");
			sb.append(", CONDITIONTYPE  VARCHAR(256)");
			sb.append(", COLUMNINDEX    INT");
			sb.append(", WIDTH          INT");
			sb.append(", CONSTRAINT UC_TABLECOLUMNCONFIGURATION UNIQUE (FILETYPE, COLUMNINDEX)");
			sb.append(", CONSTRAINT PK_TABLECOLUMNCONFIGURATION PRIMARY KEY (FILETYPE, CONDITIONTYPE))");
			stmt = conn.prepareStatement(sb.toString());
			stmt.executeUpdate();
			if(log.isDebugEnabled()) log.debug("Table TABLECOLUMNCONFIGURATION created");
			
			//set all files to enabled
			stmt = conn.prepareStatement("UPDATE FILE SET ENABLED = 1");
			stmt.executeUpdate();	
			
			insertDefaultLibraryViewColumns();
			
			//update db version
			storage.setMetaDataValue(MetaDataKeys.VERSION.toString(), "0.3");
			
			if(log.isInfoEnabled()) log.info("Updated DB from version 0.2 to 0.3");
		} catch (SQLException se) {
			log.error("Failed to update DB from version 0.2 to 0.3", se);
		} finally {
			DBBase.close(conn, stmt);
    	}
	}

	private void updateDb03_04() {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			//do updates
			stmt = conn.prepareStatement("ALTER TABLE MEDIALIBRARYFOLDERS ADD SORTOPTION VARCHAR_IGNORECASE(64)");
			stmt.executeUpdate();
			stmt = conn.prepareStatement("UPDATE MEDIALIBRARYFOLDERS SET SORTOPTION = 'FileProperty'");
			stmt.executeUpdate();
			
			stmt = conn.prepareStatement("ALTER TABLE VIDEO ADD MUXINGMODE VARCHAR(32)");
			stmt.executeUpdate();
			stmt = conn.prepareStatement("ALTER TABLE VIDEOAUDIO ADD MUXINGMODE VARCHAR(32)");
			stmt.executeUpdate();
			stmt = conn.prepareStatement("ALTER TABLE AUDIO ADD MUXINGMODE VARCHAR(32)");
			stmt.executeUpdate();
			
			//update db version
			storage.setMetaDataValue(MetaDataKeys.VERSION.toString(), "0.4");
			if(log.isInfoEnabled()) log.info("Updated DB from version 0.3 to 0.4");
		} catch (SQLException se) {
			log.error("Failed to update DB from version 0.3 to 0.4", se);
		} finally {
			DBBase.close(conn, stmt);
    	}
	}

	private void updateDb04_05() {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			
			//create table FILEIMPORTTEMPLATE
			StringBuffer sb = new StringBuffer();
			sb.append("CREATE TABLE FILEIMPORTTEMPLATE (");
			sb.append("  ID             INT AUTO_INCREMENT");
			sb.append(", NAME		 	VARCHAR(64)");
			sb.append(", CONSTRAINT PK_FILEIMPORTTEMPLATE PRIMARY KEY (ID))");
			stmt = conn.prepareStatement(sb.toString());
			stmt.executeUpdate();
			
			//Create table FILEIMPORTTEMPLATEENTRY
			sb = new StringBuffer();
			sb.append("CREATE TABLE FILEIMPORTTEMPLATEENTRY (");
			sb.append("  TEMPLATEID     INT REFERENCES FILEIMPORTTEMPLATE(ID)");
			sb.append(", FILEPROPERTY	VARCHAR(64)");
			sb.append(", ENGINENAME		VARCHAR(64)");
			sb.append(", PRIO			INT");
			sb.append(", CONSTRAINT PK_FILEIMPORTTEMPLATEENTRY PRIMARY KEY (TEMPLATEID, FILEPROPERTY, ENGINENAME))");
			stmt = conn.prepareStatement(sb.toString());
			stmt.executeUpdate();
			stmt = conn.prepareStatement("CREATE INDEX IDX_FILEIMPORTTEMPLATEENTRY_TEMPLATEID ON FILEIMPORTTEMPLATEENTRY (TEMPLATEID asc);");
			stmt.executeUpdate();
			if(log.isDebugEnabled()) log.debug("Table FILEIMPORTTEMPLATEENTRY created");
			
			//Create table FILEIMPORTTEMPLATEACTIVEENGINE
			sb = new StringBuffer();
			sb.append("CREATE TABLE FILEIMPORTTEMPLATEACTIVEENGINE (");
			sb.append("  TEMPLATEID     INT REFERENCES FILEIMPORTTEMPLATE(ID)");
			sb.append(", FILETYPE	VARCHAR(64)");
			sb.append(", ENGINENAME		VARCHAR(64)");
			sb.append(", CONSTRAINT PK_FILEIMPORTTEMPLATEACTIVEENGINE PRIMARY KEY (TEMPLATEID, FILETYPE, ENGINENAME))");
			stmt = conn.prepareStatement(sb.toString());
			stmt.executeUpdate();
			stmt = conn.prepareStatement("CREATE INDEX IDX_FILEIMPORTTEMPLATEACTIVEENGINE_TEMPLATEID ON FILEIMPORTTEMPLATEACTIVEENGINE (TEMPLATEID asc);");
			stmt.executeUpdate();
			if(log.isDebugEnabled()) log.debug("Table FILEIMPORTTEMPLATEACTIVEENGINE created");
			
			//Create table FILEIMPORTTEMPLATETAGS
			sb = new StringBuffer();
			sb.append("CREATE TABLE FILEIMPORTTEMPLATETAGS (");
			sb.append("  TEMPLATEID     INT REFERENCES FILEIMPORTTEMPLATE(ID)");
			sb.append(", FILETYPE	VARCHAR(64)");
			sb.append(", ENGINENAME		VARCHAR(64)");
			sb.append(", TAGNAME		VARCHAR(128)");
			sb.append(", CONSTRAINT PK_FILEIMPORTTEMPLATETAGS PRIMARY KEY (TEMPLATEID, FILETYPE, ENGINENAME, TAGNAME))");
			stmt = conn.prepareStatement(sb.toString());
			stmt.executeUpdate();
			stmt = conn.prepareStatement("CREATE INDEX IDX_FILEIMPORTTEMPLATETAGS_TEMPLATEID ON FILEIMPORTTEMPLATETAGS (TEMPLATEID asc);");
			stmt.executeUpdate();
			if(log.isDebugEnabled()) log.debug("Table FILEIMPORTTEMPLATETAGS created");
			
			//add new colums to MANAGEDFOLDERS
			stmt = conn.prepareStatement("ALTER TABLE MANAGEDFOLDERS ADD COLUMN FILEIMPORTTEMPLATEID INT");
			stmt.executeUpdate();
			stmt = conn.prepareStatement("ALTER TABLE MANAGEDFOLDERS ADD FOREIGN KEY (FILEIMPORTTEMPLATEID) REFERENCES FILEIMPORTTEMPLATE(ID)");
			stmt.executeUpdate();
			stmt = conn.prepareStatement("ALTER TABLE MANAGEDFOLDERS ADD COLUMN ISFILEIMPORTENABLED BIT");
			stmt.executeUpdate();
			
			//create default import template
			DOFileImportTemplate template = new DOFileImportTemplate(-1, Messages.getString("ML.FileImportTemplate.DefaultTemplateName"), null, null, null);
			try {
				new DBFileImport(cp).insertTemplate(template);
			} catch (StorageException e) {
				log.error("Failed to insert default file import template", e);
			}
			
			//set the import template for the managed folders having the tmdb option enabled
			stmt = conn.prepareStatement("UPDATE MANAGEDFOLDERS"
					+ " SET ISFILEIMPORTENABLED = ?, FILEIMPORTTEMPLATEID = ?"
					+ " WHERE TMDB = ?");
			stmt.setBoolean(1, true);
			stmt.setInt(2, 1);
			stmt.setBoolean(3, true);
			stmt.executeUpdate();
			
			//disable it for the others
			stmt = conn.prepareStatement("UPDATE MANAGEDFOLDERS"
					+ " SET ISFILEIMPORTENABLED = ?, FILEIMPORTTEMPLATEID = ?"
					+ " WHERE TMDB = ?");
			stmt.setBoolean(1, false);
			stmt.setInt(2, 1);
			stmt.setBoolean(3, false);
			stmt.executeUpdate();

			//change PK constraints for MANAGEDFOLDERS
			stmt = conn.prepareStatement("ALTER TABLE MANAGEDFOLDERS DROP CONSTRAINT PK_MANAGEDFOLDERS");
			stmt.executeUpdate();
			stmt = conn.prepareStatement("ALTER TABLE MANAGEDFOLDERS ADD CONSTRAINT PK_MANAGEDFOLDERS PRIMARY KEY (WATCH, FOLDERPATH, VIDEO, AUDIO, PICTURES, SUBFOLDERS)");
			stmt.executeUpdate();
			
			//remove the TMDB column from MANAGEDFOLDERS
			stmt = conn.prepareStatement("ALTER TABLE MANAGEDFOLDERS DROP COLUMN TMDB");
			stmt.executeUpdate();
			
			//update class names for plugins having changed
			stmt = conn.prepareStatement("UPDATE SPECIALFOLDERS SET CLASSNAME = ? WHERE CLASSNAME = ?");
			stmt.setString(1, "net.pms.plugin.dlnatreefolder.FileSystemFolderPlugin");
			stmt.setString(2, "net.pms.medialibrary.specialfolder.AutoDiscoverSpecialFolder");
			stmt.executeUpdate();

			stmt.setString(1, "net.pms.plugin.filedetail.TmdbRatingPlugin");
			stmt.setString(2, "net.pms.medialibrary.plugin.TmdbRater");
			stmt.executeUpdate();

			stmt.setString(1, "net.pms.plugin.dnlatreefolder.VideoSettingsFolderPlugin");
			stmt.setString(2, "net.pms.medialibrary.specialfolder.VideoSettingsSpecialFolder");
			stmt.executeUpdate();

			stmt.setString(1, "net.pms.plugin.dlnatreefolder.WebFolderPlugin");
			stmt.setString(2, "net.pms.medialibrary.specialfolder.WebSpecialFolder");
			stmt.executeUpdate();

			stmt.setString(1, "net.pms.plugin.dlnatreefolder.iPhotoFolderPlugin");
			stmt.setString(2, "net.pms.medialibrary.specialfolder.iPhotoSpecialFolder");
			stmt.executeUpdate();

			stmt.setString(1, "net.pms.plugin.dlnatreefolder.iTunesFolderPlugin");
			stmt.setString(2, "net.pms.medialibrary.specialfolder.iTunesSpecialFolder");
			stmt.executeUpdate();
			
			//update db version
			storage.setMetaDataValue(MetaDataKeys.VERSION.toString(), "0.5");
			if(log.isInfoEnabled()) log.info("Updated DB from version 0.4 to 0.5");
		} catch (SQLException se) {
			log.error("Failed to update DB from version 0.4 to 0.5", se);
		} finally {
			DBBase.close(conn, stmt);
    	}
	}

	private void updateDb05_06() {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			//do updates
			stmt = conn.prepareStatement("ALTER TABLE CONDITIONS ADD TAGNAME VARCHAR_IGNORECASE(512)");
			stmt.executeUpdate();
			
			//update db version
			storage.setMetaDataValue(MetaDataKeys.VERSION.toString(), "0.6");
			if(log.isInfoEnabled()) log.info("Updated DB from version 0.5 to 0.6");
		} catch (SQLException se) {
			log.error("Failed to update DB from version 0.5 to 0.6", se);
		} finally {
			DBBase.close(conn, stmt);
    	}
	}

	private void updateDb06_07() {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			//do updates
			stmt = conn.prepareStatement("ALTER TABLE FILEIMPORTTEMPLATEENTRY ADD ISENABLED BIT DEFAULT 1");
			stmt.executeUpdate();
			
			//update db version
			storage.setMetaDataValue(MetaDataKeys.VERSION.toString(), "0.7");
			if(log.isInfoEnabled()) log.info("Updated DB from version 0.6 to 0.7");
		} catch (SQLException se) {
			log.error("Failed to update DB from version 0.6 to 0.7", se);
		} finally {
			DBBase.close(conn, stmt);
    	}
	}

	private void updateDb07_08() {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			//do updates
			stmt = conn.prepareStatement("CREATE TABLE QUICKTAG (NAME VARCHAR(256), TAGNAME VARCHAR(1024), TAGVALUE VARCHAR(1024), VIRTUALKEY INT, KEYCOMBINATION VARCHAR(128))");
			stmt.executeUpdate();
			
			//update db version
			storage.setMetaDataValue(MetaDataKeys.VERSION.toString(), "0.8");
			if(log.isInfoEnabled()) log.info("Updated DB from version 0.7 to 0.8");
		} catch (SQLException se) {
			log.error("Failed to update DB from version 0.7 to 0.8", se);
		} finally {
			DBBase.close(conn, stmt);
    	}
	}
	
	private void insertTableColumnConfiguration(DOTableColumnConfiguration c, FileType fileType) {
		Connection conn = null;
		PreparedStatement stmt = null;
			
		try {
			conn = cp.getConnection();
			stmt = conn.prepareStatement("INSERT INTO TABLECOLUMNCONFIGURATION (COLUMNINDEX, WIDTH, FILETYPE, CONDITIONTYPE) VALUES (?, ?, ?, ?)");
			stmt.clearParameters();
			stmt.setInt(1, c.getColumnIndex());
			stmt.setInt(2, c.getWidth());
			stmt.setString(3, fileType.toString());
			stmt.setString(4, c.getConditionType().toString());
			stmt.executeUpdate();
		} catch (SQLException se) {
			log.error(String.format("Failed to insert TABLECOLUMNCONFIGURATION for columnIndex=%s, width=%s, fileType=%s, conditionType=%s", 
					c.getColumnIndex(), c.getWidth(), fileType, c.getConditionType()), se);
		} finally {
			DBBase.close(conn, stmt);
		}
	}
}
