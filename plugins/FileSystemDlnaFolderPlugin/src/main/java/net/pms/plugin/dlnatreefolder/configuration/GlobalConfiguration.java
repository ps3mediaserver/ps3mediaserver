package net.pms.plugin.dlnatreefolder.configuration;

import java.io.File;
import java.io.IOException;
import net.pms.PMS;

public class GlobalConfiguration extends BaseConfiguration {
	private static final String KEY_isThumbnailGenerationEnabled = "thumbnailGenerationEnabled";
	private static final String KEY_thumbnailSeekPosSec = "thumbnailSeekPosSec";
	private static final String KEY_dvdIsoThumbnailsEnabled = "dvdIsoThumbnailsEnabled";
	private static final String KEY_imageThumbnailsEnabled = "imageThumbnailsEnabled";
	private static final String KEY_audioThumbnailMethod = "audioThumbnailMethod";
	private static final String KEY_hideTranscodeEnabled = "hideTranscodeEnabled";
	private static final String KEY_alternateThumbFolder = "alternateThumbFolder";
	private static final String KEY_hideEngineNames = "hideEngineNames";
	private static final String KEY_hideExtensions = "hideExtensions";
	private static final String KEY_hideEmptyFolders = "hideEmptyFolders";
	private static final String KEY_sortMethod = "sortMethod";
	private static final String KEY_browseArchives = "browseArchives";

	private String propertiesFilePath;
	
	public GlobalConfiguration() {
		propertiesFilePath = PMS.getConfiguration().getProfileDirectory() + File.separator + "plugins" + File.separator +
				"global" + File.separator + "FileSystemFolderPlugin.conf";
	}

	public void save() throws IOException {
			save(propertiesFilePath);
	}

	public void load() throws IOException {
		if(new File(propertiesFilePath).exists()) {
			load(propertiesFilePath);
		}
	}

	public boolean isThumbnailGenerationEnabled() {
		return getValue(KEY_isThumbnailGenerationEnabled, true);
	}

	public void setThumbnailGenerationEnabled(boolean thumbnailGenerationEnabled) {
		setValue(KEY_isThumbnailGenerationEnabled, thumbnailGenerationEnabled);
	}

	public int getThumbnailSeekPosSec() {
		return getValue(KEY_thumbnailSeekPosSec, 30);
	}

	public void setThumbnailSeekPosSec(int thumbnailSeekPosSec) {
		setValue(KEY_thumbnailSeekPosSec, thumbnailSeekPosSec);
	}

	public boolean isDvdIsoThumbnailsEnabled() {
		return getValue(KEY_dvdIsoThumbnailsEnabled, true);
	}

	public void setDvdIsoThumbnailsEnabled(boolean dvdIsoThumbnailsEnabled) {
		setValue(KEY_dvdIsoThumbnailsEnabled, dvdIsoThumbnailsEnabled);
	}

	public boolean getImageThumbnailsEnabled() {
		return getValue(KEY_imageThumbnailsEnabled, true);
	}

	public void setImageThumbnailsEnabled(boolean imageThumbnailsEnabled) {
		setValue(KEY_imageThumbnailsEnabled, imageThumbnailsEnabled);
	}

	public Integer getAudioThumbnailMethod() {
		return getValue(KEY_audioThumbnailMethod, 0);
	}

	public void setAudioThumbnailMethod(Integer audioThumbnailMethod) {
		setValue(KEY_audioThumbnailMethod, audioThumbnailMethod);
	}

	public boolean isHideTranscodeEnabled() {
		return getValue(KEY_hideTranscodeEnabled, false);
	}

	public void setHideTranscodeEnabled(boolean hideTranscodeEnabled) {
		setValue(KEY_hideTranscodeEnabled, hideTranscodeEnabled);
	}

	public String getAlternateThumbFolder() {
		return getValue(KEY_alternateThumbFolder, "");
	}

	public void setAlternateThumbFolder(String alternateThumbFolder) {
		setValue(KEY_alternateThumbFolder, alternateThumbFolder);
	}

	public boolean isHideEngineNames() {
		return getValue(KEY_hideEngineNames, false);
	}

	public void setHideEngineNames(boolean hideEngineNames) {
		setValue(KEY_hideEngineNames, hideEngineNames);
	}

	public boolean isHideExtensions() {
		return getValue(KEY_hideExtensions, true);
	}

	public void setHideExtensions(boolean hideExtensions) {
		setValue(KEY_hideExtensions, hideExtensions);
	}

	public boolean isHideEmptyFolders() {
		return getValue(KEY_hideEmptyFolders, false);
	}

	public void setHideEmptyFolders(boolean hideEmptyFolders) {
		setValue(KEY_hideEmptyFolders, hideEmptyFolders);
	}

	public Integer getSortMethod() {
		return getValue(KEY_sortMethod, 0);
	}

	public void setSortMethod(Integer sortMethod) {
		setValue(KEY_sortMethod, sortMethod);
	}

	public boolean isBrowseArchives() {
		return getValue(KEY_browseArchives, false);
	}

	public void setBrowseArchives(boolean browseArchives) {
		setValue(KEY_browseArchives, browseArchives);
	}
}
