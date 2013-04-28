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
package net.pms.plugin.startstoplistener.pcw.configuration;

import java.io.IOException;
import net.pms.configuration.BaseConfiguration;

/**
 * Holds the global configuration for the plugin.
 */
public class GlobalConfiguration extends BaseConfiguration {
	
	/** The Constant KEY_percentPlayedRequired. */
	private static final String KEY_percentPlayedRequired = "percentPlayedRequired";
	
	/** The properties file path. */
	private String propertiesFilePath;
	
	/**
	 * Instantiates a new global configuration.
	 */
	public GlobalConfiguration() {
		propertiesFilePath = getGlobalConfigurationDirectory() + "PlayCountWatcher.conf";
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
	 * Gets the percent played required (0-100).
	 *
	 * @return the percent played required
	 */
	public int getPercentPlayedRequired() {
		return getValue(KEY_percentPlayedRequired, 80);
	}

	/**
	 * Sets the percent played required (0-100).
	 *
	 * @param percentPlayedRequired the new percent played required
	 */
	public void setPercentPlayedRequired(int percentPlayedRequired) {
		if(percentPlayedRequired > 0 && percentPlayedRequired < 101) {
			setValue(KEY_percentPlayedRequired, percentPlayedRequired);
		}
	}
}
