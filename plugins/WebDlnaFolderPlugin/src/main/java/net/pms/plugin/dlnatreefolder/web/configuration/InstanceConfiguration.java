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
package net.pms.plugin.dlnatreefolder.web.configuration;

import java.io.File;

import net.pms.PMS;
import net.pms.configuration.BaseConfiguration;

/**
 * Holds the configuration for an instance of the plugin.
 *
 * @author pw
 */
public class InstanceConfiguration extends BaseConfiguration {
	private static final String KEY_filePath = "filePath";

	/**
	 * Sets the file path.
	 *
	 * @param filePath the new file path
	 */
	public void setFilePath(String filePath) {
		properties.put(KEY_filePath, filePath);
	}
	
	/**
	 * Gets the file path.
	 *
	 * @return the file path
	 */
	public String getFilePath() {
		Object res = properties.get(KEY_filePath);
		if(res == null) {
			res = PMS.getConfiguration().getProfileDirectory() + File.separatorChar + "WEB.conf";
		}
		return res.toString();
	}
}
