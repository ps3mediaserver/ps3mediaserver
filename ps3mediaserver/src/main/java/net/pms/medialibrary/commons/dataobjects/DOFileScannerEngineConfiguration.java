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
