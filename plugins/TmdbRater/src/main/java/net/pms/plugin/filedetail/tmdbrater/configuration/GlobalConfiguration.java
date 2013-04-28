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
package net.pms.plugin.filedetail.tmdbrater.configuration;

import java.io.IOException;
import net.pms.configuration.BaseConfiguration;

// TODO: Auto-generated Javadoc
/**
 * Holds the global configuration for the plugin.
 */
public class GlobalConfiguration extends BaseConfiguration {
	
	/** The Constant KEY_username. */
	private static final String KEY_username = "userName";
	
	/** The Constant KEY_session. */
	private static final String KEY_session = "session";
	
	/** The properties file path. */
	private String propertiesFilePath;
	
	/**
	 * Instantiates a new global configuration.
	 */
	public GlobalConfiguration() {
		propertiesFilePath = getGlobalConfigurationDirectory() + "TmdbRatingPlugin.conf";
	}

	/**
	 * Save the current configuration.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void save() throws IOException {
			save(propertiesFilePath);
	}

	/**
	 * Load the last saved configuration.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void load() throws IOException {
		load(propertiesFilePath);
	}

	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName() {
		return getValue(KEY_username, null);
	}

	/**
	 * Sets the user name.
	 *
	 * @param userName the new user name
	 */
	public void setUserName(String userName) {
		setValue(KEY_username, userName);
	}

	/**
	 * Gets the session.
	 *
	 * @return the session
	 */
	public String getSession() {
		return getValue(KEY_session, null);
	}

	/**
	 * Sets the session.
	 *
	 * @param session the new session
	 */
	public void setSession(String session) {
		setValue(KEY_session, session);
	}
}
