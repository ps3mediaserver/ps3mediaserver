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
package net.pms.plugin.filedetail;

import java.awt.Component;
import java.net.URL;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.savvasdalkitsis.jtmdb.Auth;
import com.savvasdalkitsis.jtmdb.GeneralSettings;
import com.savvasdalkitsis.jtmdb.Pair;
import com.savvasdalkitsis.jtmdb.ServerResponse;
import com.savvasdalkitsis.jtmdb.Session;

public class TmdbHelper {
	private static final Logger log = LoggerFactory.getLogger(TmdbHelper.class);
	private static Session session;
	private static boolean isInitialized;
	
	public static void initialize(){
		if(!isInitialized){
		    GeneralSettings.setApiKey("4cdddc892213dd24e5011fd710f8abf0");
		    Locale l = new Locale(net.pms.PMS.getConfiguration().getLanguage());
		    GeneralSettings.setAPILocale(l);
		    isInitialized = true;
		}
	}
	
	public static Session getSession(){
		if(!isInitialized) initialize();
		if(session == null){
			String userName = TmdbRatingPlugin.globalConfig.getUserName();
			String sessionStr = TmdbRatingPlugin.globalConfig.getSession();
			
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
			JOptionPane.showMessageDialog(parentComponent, TmdbRatingPlugin.messages.getString("TmdbHelper.1"));

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

		return session;
	}
}
