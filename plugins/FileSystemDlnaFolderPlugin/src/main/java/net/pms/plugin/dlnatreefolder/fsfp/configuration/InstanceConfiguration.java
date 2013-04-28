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
package net.pms.plugin.dlnatreefolder.fsfp.configuration;

import java.util.ArrayList;
import java.util.List;

import net.pms.configuration.BaseConfiguration;

/**
 * Holds the configuration for an instance of the plugin
 * 
 * @author pw
 */
public class InstanceConfiguration extends BaseConfiguration {

	/**
	 * Sets the folder paths.
	 *
	 * @param folderPaths the new folder paths
	 */
	public void setFolderPaths(List<String> folderPaths) {
		int i = 0;
		properties.clear();
		for (String path : folderPaths) {
			properties.put("p" + i++, path);
		}
	}

	/**
	 * Gets the folder paths.
	 *
	 * @return the folder paths
	 */
	public List<String> getFolderPaths() {
		int i = 0;
		Object prop = null;
		List<String> folderPaths = new ArrayList<String>();
		while (true) {
			prop = properties.get("p" + i++);
			if (prop == null) {
				break;
			}
			folderPaths.add((String) prop);
		}
		return folderPaths;
	}
}
