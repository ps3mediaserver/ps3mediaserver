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
package net.pms.plugin.fileimport.imdb.configuration;

import java.io.IOException;
import net.pms.configuration.BaseConfiguration;

/**
 * Holds the global configuration for the plugin.
 */
public class GlobalConfiguration extends BaseConfiguration {
	public enum PlotType { Short, Long }
	
	private static final String KEY_coverWidth = "coverWidth";
	private static final String KEY_plotType = "plotType";
	private static final String KEY_useRottenTomatoes = "useRottenTomatoes";
	private static final String KEY_receiveTimeoutMs = "receiveTimeoutMs";
	
	/** The properties file path. */
	private String propertiesFilePath;
	
	/**
	 * Instantiates a new global configuration.
	 */
	public GlobalConfiguration() {
		propertiesFilePath = getGlobalConfigurationDirectory() + "ImdbMovieImportPlugin.conf";
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
	 * Gets the cover width. in pixels
	 *
	 * @return the cover width
	 */
	public int getCoverWidth() {
		return getValue(KEY_coverWidth, 400);
	}
	
	/**
	 * Sets the cover width in pixels.
	 *
	 * @param coverWidth the new cover width
	 */
	public void setCoverWidth(int coverWidth) {
		setValue(KEY_coverWidth, coverWidth);
	}

	/**
	 * Gets the plot type (long or short).
	 *
	 * @return the plot type
	 */
	public PlotType getPlotType() {
		return getValue(KEY_plotType, PlotType.Long);
	}

	/**
	 * Sets the plot type (long or short).
	 *
	 * @param plotType the new plot type
	 */
	public void setPlotType(PlotType plotType) {
		setValue(KEY_plotType, plotType);
	}

	/**
	 * Checks if ratings should be imported from rotten tomatoes.
	 *
	 * @return true, if ratings should be imported from rotten tomatoes
	 */
	public boolean isUseRottenTomatoes() {
		return getValue(KEY_useRottenTomatoes, true);
	}

	/**
	 * Sets if ratings should be imported from rotten tomatoes.
	 *
	 * @param useRottenTomatoes true if ratings should be imported from rotten tomatoes
	 */
	public void setUseRottenTomatoes(boolean useRottenTomatoes) {
		setValue(KEY_useRottenTomatoes, useRottenTomatoes);
	}

	/**
	 * Gets the receive timeout (in milliseconds)
	 * 
	 * @return the receive timeout
	 */
	public int getReceiveTimeoutSec() {
		return getValue(KEY_receiveTimeoutMs, 300);
	}

	/**
	 * Sets the receive timeout (in milliseconds)
	 * 
	 * @param receiveTimeoutMs the receive timeout
	 */
	public void setReceiveTimeoutMs(int receiveTimeoutMs) {
		setValue(KEY_receiveTimeoutMs, receiveTimeoutMs);
	}
}
