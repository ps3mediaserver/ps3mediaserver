package net.pms.medialibrary.commons.helpers;

import java.awt.Component;
import java.net.URL;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.MediaLibraryConstants.MetaDataKeys;
import net.pms.medialibrary.storage.MediaLibraryStorage;
import net.sf.jtmdb.Auth;
import net.sf.jtmdb.GeneralSettings;
import net.sf.jtmdb.Pair;
import net.sf.jtmdb.ServerResponse;
import net.sf.jtmdb.Session;

public class TmdbHelper {
	private static final Logger log = LoggerFactory.getLogger(TmdbHelper.class);
	private static Session session;
	private static boolean isInitialized;
	
	public static void initialize(){
		if(!isInitialized){
		    GeneralSettings.setApiKey("4cdddc892213dd24e5011fd710f8abf0");
		    Locale l = new Locale(net.pms.PMS.getConfiguration().getLanguage());
		    GeneralSettings.setAPILocale(l);
		    //GeneralSettings.setLogEnabled(true);
		    isInitialized = true;
		}
	}
	
	public static Session getSession(){
		if(!isInitialized) initialize();
		if(session == null){
			String userName = MediaLibraryStorage.getInstance().getMetaDataValue(MetaDataKeys.TMDB_USER_NAME.toString());
			String sessionStr = MediaLibraryStorage.getInstance().getMetaDataValue(MetaDataKeys.TMDB_SESSION.toString());
			
			if(userName != null && sessionStr != null){
				session = new Session(userName, sessionStr);
				if(log.isInfoEnabled()) log.info("Loaded session from store for user " + userName);
			}
			
		}
		return session;
	}

	public static Session createSession(Component parentComponent) {
		if(!isInitialized) initialize();
		try {
			session = null;
			
			String token = Auth.getToken();
			URL authUrl = Auth.authorizeToken(token);
			java.awt.Desktop.getDesktop().browse(authUrl.toURI());
			JOptionPane.showMessageDialog(parentComponent, Messages.getString("ML.Messages.CreateTmdbSession"));

			Pair<Session, ServerResponse> resp = Auth.getSession(token);
			if (resp.getSecond() == ServerResponse.SUCCESS) {
				session = resp.getFirst();
				if (log.isInfoEnabled()) log.info("Created new session for user " + session.getUserName());
			} else {
				log.warn(String.format("Failed to create session. Server response was %s", resp.getSecond()));
			}
		} catch (Exception e) {
			log.error("Failed to create session", e);
		}

		MediaLibraryStorage.getInstance().setMetaDataValue(MetaDataKeys.TMDB_USER_NAME.toString(), session == null ? "" : session.getUserName());
		MediaLibraryStorage.getInstance().setMetaDataValue(MetaDataKeys.TMDB_SESSION.toString(), session == null ? "" : session.getSession());

		return session;
	}
}
