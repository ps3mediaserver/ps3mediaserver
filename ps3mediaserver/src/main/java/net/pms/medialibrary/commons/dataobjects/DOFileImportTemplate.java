package net.pms.medialibrary.commons.dataobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pms.medialibrary.commons.enumarations.FileProperty;
import net.pms.medialibrary.commons.enumarations.FileType;

public class DOFileImportTemplate {
	private int id = 1;
	private String name;
	private List<DOFileScannerEngineConfiguration> configuredEnginesPerFileProperty;
	private Map<FileType, List<String>> enabledEnginesForFileType;
	private Map<FileType, Map<String, List<String>>> enabledTags;
	
	public DOFileImportTemplate(){
		this(0, "", new ArrayList<DOFileScannerEngineConfiguration>(), new HashMap<FileType, List<String>>(), new HashMap<FileType, Map<String, List<String>>>());
	}
	
	public DOFileImportTemplate(int id, String name, List<DOFileScannerEngineConfiguration> engines, Map<FileType, List<String>> enabledEngines, Map<FileType, Map<String, List<String>>> enabledTags){
		setId(id);
		setName(name);
		setConfiguredEngines(engines);
		setEnabledEngines(enabledEngines);
		setEnabledTags(enabledTags);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void setConfiguredEngines(List<DOFileScannerEngineConfiguration> engines) {
		this.configuredEnginesPerFileProperty = engines;
	}
	
	public void addConfiguredEngine(DOFileScannerEngineConfiguration engine) {
		if(engine == null) return;
		if(configuredEnginesPerFileProperty == null) configuredEnginesPerFileProperty = new ArrayList<DOFileScannerEngineConfiguration>();
		
		DOFileScannerEngineConfiguration engineToRemove = null;
		for(DOFileScannerEngineConfiguration existingEngine : configuredEnginesPerFileProperty) {
			if(existingEngine.getFileProperty() == engine.getFileProperty()) {
				engineToRemove = existingEngine;
				break;
			}
		}
		if(engineToRemove != null) {
			configuredEnginesPerFileProperty.remove(engineToRemove);
		}
		
		configuredEnginesPerFileProperty.add(engine);
	}

	public List<DOFileScannerEngineConfiguration> getAllConfiguredEngines() {
		if(configuredEnginesPerFileProperty == null) configuredEnginesPerFileProperty = new ArrayList<DOFileScannerEngineConfiguration>();
		return configuredEnginesPerFileProperty;
	}
	
	public List<String> getConfiguredEngines(FileProperty fileProperty) {
		List<String> res = new ArrayList<String>();

		for(DOFileScannerEngineConfiguration existingEngine : configuredEnginesPerFileProperty) {
			if(existingEngine.getFileProperty() == fileProperty) {
				res = existingEngine.getEngineNames();
				break;
			}
		}
		
		return res;
	}

	public void clearConfiguredEngines() {
		if(configuredEnginesPerFileProperty == null) configuredEnginesPerFileProperty = new ArrayList<DOFileScannerEngineConfiguration>();
		configuredEnginesPerFileProperty.clear();
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public Map<FileType, List<String>> getEnabledEngines() {
		if(enabledEnginesForFileType == null) enabledEnginesForFileType = new HashMap<FileType, List<String>>();
		if(!enabledEnginesForFileType.containsKey(FileType.VIDEO)) enabledEnginesForFileType.put(FileType.VIDEO, new ArrayList<String>());
		if(!enabledEnginesForFileType.containsKey(FileType.AUDIO)) enabledEnginesForFileType.put(FileType.AUDIO, new ArrayList<String>());
		if(!enabledEnginesForFileType.containsKey(FileType.PICTURES)) enabledEnginesForFileType.put(FileType.PICTURES, new ArrayList<String>());
		if(!enabledEnginesForFileType.containsKey(FileType.FILE)) enabledEnginesForFileType.put(FileType.FILE, new ArrayList<String>());
		
		return enabledEnginesForFileType;
	}

	public void setEnabledEngines(Map<FileType, List<String>> enabledEnginesForFileType) {
		this.enabledEnginesForFileType = enabledEnginesForFileType;
	}

	public Map<FileType, Map<String, List<String>>> getEnabledTags() {
		if(enabledTags == null) enabledTags = new HashMap<FileType, Map<String,List<String>>>();
		if(!enabledTags.containsKey(FileType.VIDEO)) enabledTags.put(FileType.VIDEO, new HashMap<String, List<String>>());
		if(!enabledTags.containsKey(FileType.AUDIO)) enabledTags.put(FileType.AUDIO, new HashMap<String, List<String>>());
		if(!enabledTags.containsKey(FileType.PICTURES)) enabledTags.put(FileType.PICTURES, new HashMap<String, List<String>>());
		if(!enabledTags.containsKey(FileType.FILE)) enabledTags.put(FileType.FILE, new HashMap<String, List<String>>());
		
		return enabledTags;
	}

	public void setEnabledTags(Map<FileType, Map<String, List<String>>> enabledTags) {
		this.enabledTags = enabledTags;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DOFileImportTemplate)) {
			return false; 
		}

		DOFileImportTemplate compObj = (DOFileImportTemplate) obj;
		if (getId()  == compObj.getId() 
				&& getName().equals(compObj.getName())
				&& getAllConfiguredEngines().equals(compObj.getAllConfiguredEngines())
				&& getEnabledEngines().equals(compObj.getEnabledEngines())
				&& getEnabledTags().equals(compObj.getEnabledTags())) {
			return true; 
		}

		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + getId();
		hashCode *= 24 + getName().hashCode();
		hashCode *= 24 + getAllConfiguredEngines().hashCode();
		hashCode *= 24 + getEnabledEngines().hashCode();
		hashCode *= 24 + getEnabledTags().hashCode();
		return hashCode;
	}
}
