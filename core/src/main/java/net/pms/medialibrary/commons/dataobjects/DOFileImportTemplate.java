/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pms.medialibrary.commons.enumarations.FileProperty;
import net.pms.medialibrary.commons.enumarations.FileType;

/**
 * The Class DOFileImportTemplate defines the how file properties and tags will be imported or updated. 
 */
public class DOFileImportTemplate {
	private int id = 1;
	private String name;
	
	/** The list of engine configurations. */
	private List<DOFileScannerEngineConfiguration> engineConfigurations;
	
	/** Contains the list of enabled engines configured for a file type */
	private Map<FileType, List<String>> enabledEnginesForFileType;
	/** Contains the list of tags for a file type */
	private Map<FileType, Map<String, List<String>>> enabledTags;
	
	/**
	 * Instantiates a new file import template with default parameters.
	 */
	public DOFileImportTemplate(){
		this(0, "", new ArrayList<DOFileScannerEngineConfiguration>(), new HashMap<FileType, List<String>>(), new HashMap<FileType, Map<String, List<String>>>());
	}
	
	/**
	 * Instantiates a new file import template.
	 *
	 * @param id the id
	 * @param name the name
	 * @param engines the engines
	 * @param enabledEngines the enabled engines
	 * @param enabledTags the enabled tags
	 */
	public DOFileImportTemplate(int id, String name, List<DOFileScannerEngineConfiguration> engines, Map<FileType, List<String>> enabledEngines, Map<FileType, Map<String, List<String>>> enabledTags){
		setId(id);
		setName(name);
		setEngineConfigurations(engines);
		setEnabledEngines(enabledEngines);
		setEnabledTags(enabledTags);
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}

	public void setEngineConfigurations(List<DOFileScannerEngineConfiguration> engineConfigurations) {
		this.engineConfigurations = engineConfigurations;
	}

	/**
	 * Adds the engine configuration.
	 *
	 * @param engineConfiguration the engine configuration
	 */
	public void addEngineConfiguration(DOFileScannerEngineConfiguration engineConfiguration) {
		if(engineConfiguration == null) return;
		if(engineConfigurations == null) engineConfigurations = new ArrayList<DOFileScannerEngineConfiguration>();
		
		DOFileScannerEngineConfiguration engineToRemove = null;
		for(DOFileScannerEngineConfiguration existingEngine : engineConfigurations) {
			if(existingEngine.getFileProperty() == engineConfiguration.getFileProperty()) {
				engineToRemove = existingEngine;
				break;
			}
		}
		if(engineToRemove != null) {
			engineConfigurations.remove(engineToRemove);
		}
		
		engineConfigurations.add(engineConfiguration);
	}

	/**
	 * Gets the engine configurations.
	 *
	 * @return the engine configurations
	 */
	public List<DOFileScannerEngineConfiguration> getEngineConfigurations() {
		if(engineConfigurations == null) engineConfigurations = new ArrayList<DOFileScannerEngineConfiguration>();
		return engineConfigurations;
	}

	/**
	 * Gets the engine configurations for the given file property.
	 *
	 * @param fileProperty the file property
	 * @return the engine configurations
	 */
	public List<String> getEngineConfigurations(FileProperty fileProperty) {
		List<String> res = new ArrayList<String>();

		for(DOFileScannerEngineConfiguration existingEngine : engineConfigurations) {
			if(existingEngine.getFileProperty() == fileProperty) {
				res = existingEngine.getEngineNames();
				break;
			}
		}
		
		return res;
	}

	/**
	 * Clear engine configurations.
	 */
	public void clearEngineConfigurations() {
		if(engineConfigurations == null) engineConfigurations = new ArrayList<DOFileScannerEngineConfiguration>();
		engineConfigurations.clear();
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the enabled engines.
	 *
	 * @return a map containing a list of engine names to use for each file type
	 */
	public Map<FileType, List<String>> getEnabledEngines() {
		if(enabledEnginesForFileType == null) enabledEnginesForFileType = new HashMap<FileType, List<String>>();
		if(!enabledEnginesForFileType.containsKey(FileType.VIDEO)) enabledEnginesForFileType.put(FileType.VIDEO, new ArrayList<String>());
		if(!enabledEnginesForFileType.containsKey(FileType.AUDIO)) enabledEnginesForFileType.put(FileType.AUDIO, new ArrayList<String>());
		if(!enabledEnginesForFileType.containsKey(FileType.PICTURES)) enabledEnginesForFileType.put(FileType.PICTURES, new ArrayList<String>());
		if(!enabledEnginesForFileType.containsKey(FileType.FILE)) enabledEnginesForFileType.put(FileType.FILE, new ArrayList<String>());
		
		return enabledEnginesForFileType;
	}

	/**
	 * Sets the enabled engines.
	 *
	 * @param enabledEnginesForFileType a map containing a list of engine names to use for each file type
	 */
	public void setEnabledEngines(Map<FileType, List<String>> enabledEnginesForFileType) {
		this.enabledEnginesForFileType = enabledEnginesForFileType;
	}

	/**
	 * Gets the enabled tags.
	 *
	 * @return a map containing a list of tag names to use for each file type
	 */
	public Map<FileType, Map<String, List<String>>> getEnabledTags() {
		if(enabledTags == null) enabledTags = new HashMap<FileType, Map<String,List<String>>>();
		if(!enabledTags.containsKey(FileType.VIDEO)) enabledTags.put(FileType.VIDEO, new HashMap<String, List<String>>());
		if(!enabledTags.containsKey(FileType.AUDIO)) enabledTags.put(FileType.AUDIO, new HashMap<String, List<String>>());
		if(!enabledTags.containsKey(FileType.PICTURES)) enabledTags.put(FileType.PICTURES, new HashMap<String, List<String>>());
		if(!enabledTags.containsKey(FileType.FILE)) enabledTags.put(FileType.FILE, new HashMap<String, List<String>>());
		
		return enabledTags;
	}

	/**
	 * Sets the enabled tags.
	 *
	 * @param enabledTags a map containing a list of tag names to use for each file type
	 */
	public void setEnabledTags(Map<FileType, Map<String, List<String>>> enabledTags) {
		this.enabledTags = enabledTags;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DOFileImportTemplate)) {
			return false; 
		}

		DOFileImportTemplate compObj = (DOFileImportTemplate) obj;
		if (getId()  == compObj.getId() 
				&& getName().equals(compObj.getName())
				&& getEngineConfigurations().equals(compObj.getEngineConfigurations())
				&& getEnabledEngines().equals(compObj.getEnabledEngines())
				&& getEnabledTags().equals(compObj.getEnabledTags())) {
			return true; 
		}

		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 24 + getId();
		hashCode *= 24 + getName().hashCode();
		hashCode *= 24 + getEngineConfigurations().hashCode();
		hashCode *= 24 + getEnabledEngines().hashCode();
		hashCode *= 24 + getEnabledTags().hashCode();
		return hashCode;
	}
}
