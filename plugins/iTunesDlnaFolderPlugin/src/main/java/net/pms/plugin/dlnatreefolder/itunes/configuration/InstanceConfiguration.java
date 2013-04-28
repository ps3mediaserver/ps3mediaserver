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
package net.pms.plugin.dlnatreefolder.itunes.configuration;

import net.pms.configuration.BaseConfiguration;

/**
 * Holds the instance configuration for the plugin
 */
public class InstanceConfiguration extends BaseConfiguration {
	private static final String KEY_iTunesFilePath = "iTunesFilePath";

	/**
	 * Gets the iTunes file path.
	 *
	 * @return the iTunes file path
	 */
	public String getiTunesFilePath() {
		return getValue(KEY_iTunesFilePath, "");
	}

	/**
	 * Sets the iTunes file path
	 *
	 * @param iTunesFilePath the iTunes file path
	 */
	public void setiTunesFilePath(String iTunesFilePath) {
		setValue(KEY_iTunesFilePath, iTunesFilePath);
	}
}
