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
package net.pms.medialibrary.commons.dataobjects;

import java.util.ArrayList;
import java.util.List;

import net.pms.medialibrary.commons.enumarations.FileProperty;

public class DOFileScannerEngineConfiguration {
	private boolean isEnabled;
	private List<String> engineNames;
	private FileProperty fileProperty;
	
	public DOFileScannerEngineConfiguration() {
		this(false, new ArrayList<String>(), FileProperty.UNKNOWN);
	}
	
	public DOFileScannerEngineConfiguration(boolean isEnabled, List<String> engineNames, FileProperty fileProperty) {
		setEnabled(isEnabled);
		setEngineNames(engineNames);
		setFileProperty(fileProperty);
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public List<String> getEngineNames() {
		if(engineNames == null) engineNames = new ArrayList<String>();
		return engineNames;
	}

	public void setEngineNames(List<String> engineNames) {
		this.engineNames = engineNames;
	}

	public FileProperty getFileProperty() {
		return fileProperty;
	}

	public void setFileProperty(FileProperty fileProperty) {
		this.fileProperty = fileProperty;
	}
}
