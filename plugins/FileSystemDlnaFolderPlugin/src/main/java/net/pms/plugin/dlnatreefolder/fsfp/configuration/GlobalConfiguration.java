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

import java.io.IOException;
import net.pms.configuration.BaseConfiguration;

/**
 * Holds the global configuration for the plugin
 */
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
	
	/**
	 * Instantiates a new global configuration.
	 */
	public GlobalConfiguration() {
		propertiesFilePath = getGlobalConfigurationDirectory() + "FileSystemFolderPlugin.conf";
	}

	/**
	 * Save the current configuration
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void save() throws IOException {
			save(propertiesFilePath);
	}

	/**
	 * Load the last saved configuration
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void load() throws IOException {
		load(propertiesFilePath);
	}

	/**
	 * Checks if thumbnail generation is enabled.
	 *
	 * @return true, if thumbnail generation is enabled
	 */
	public boolean isThumbnailGenerationEnabled() {
		return getValue(KEY_isThumbnailGenerationEnabled, true);
	}

	/**
	 * Sets the thumbnail generation enabled.
	 *
	 * @param thumbnailGenerationEnabled true if enabled
	 */
	public void setThumbnailGenerationEnabled(boolean thumbnailGenerationEnabled) {
		setValue(KEY_isThumbnailGenerationEnabled, thumbnailGenerationEnabled);
	}

	/**
	 * Gets the thumbnail seek position in seconds.
	 *
	 * @return the seek position in seconds
	 */
	public int getThumbnailSeekPosSec() {
		return getValue(KEY_thumbnailSeekPosSec, 30);
	}

	/**
	 * Sets the thumbnail seek position in seconds.
	 *
	 * @param thumbnailSeekPosSec the new seek position in seconds.
	 */
	public void setThumbnailSeekPosSec(int thumbnailSeekPosSec) {
		setValue(KEY_thumbnailSeekPosSec, thumbnailSeekPosSec);
	}

	/**
	 * Checks if DVD ISO thumbnails are enabled.
	 *
	 * @return true, if is DVD ISO thumbnails are enabled
	 */
	public boolean isDvdIsoThumbnailsEnabled() {
		return getValue(KEY_dvdIsoThumbnailsEnabled, true);
	}

	/**
	 * Sets the DVD ISO thumbnails enabled.
	 *
	 * @param dvdIsoThumbnailsEnabled true if enabled
	 */
	public void setDvdIsoThumbnailsEnabled(boolean dvdIsoThumbnailsEnabled) {
		setValue(KEY_dvdIsoThumbnailsEnabled, dvdIsoThumbnailsEnabled);
	}

	/**
	 * Gets if the image thumbnails are enabled.
	 *
	 * @return true if enabled
	 */
	public boolean getImageThumbnailsEnabled() {
		return getValue(KEY_imageThumbnailsEnabled, true);
	}

	/**
	 * Sets the image thumbnails enabled.
	 *
	 * @param imageThumbnailsEnabled the new image thumbnails enabled
	 */
	public void setImageThumbnailsEnabled(boolean imageThumbnailsEnabled) {
		setValue(KEY_imageThumbnailsEnabled, imageThumbnailsEnabled);
	}

	/**
	 * Gets the audio thumbnail method.
	 *
	 * @return the audio thumbnail method
	 */
	public int getAudioThumbnailMethod() {
		return getValue(KEY_audioThumbnailMethod, 0);
	}

	/**
	 * Sets the audio thumbnail method.
	 *
	 * @param audioThumbnailMethod the new audio thumbnail method
	 */
	public void setAudioThumbnailMethod(int audioThumbnailMethod) {
		setValue(KEY_audioThumbnailMethod, audioThumbnailMethod);
	}

	/**
	 * Checks if the transcode folder should be hidden.
	 *
	 * @return true, if hidden
	 */
	public boolean isHideTranscodeEnabled() {
		return getValue(KEY_hideTranscodeEnabled, false);
	}

	/**
	 * Sets the hide transcode enabled.
	 *
	 * @param hideTranscodeEnabled the new hide transcode enabled
	 */
	public void setHideTranscodeEnabled(boolean hideTranscodeEnabled) {
		setValue(KEY_hideTranscodeEnabled, hideTranscodeEnabled);
	}

	/**
	 * Gets the alternate thumb folder.
	 *
	 * @return the alternate thumb folder
	 */
	public String getAlternateThumbFolder() {
		return getValue(KEY_alternateThumbFolder, "");
	}

	/**
	 * Sets the alternate thumb folder.
	 *
	 * @param alternateThumbFolder the new alternate thumb folder
	 */
	public void setAlternateThumbFolder(String alternateThumbFolder) {
		setValue(KEY_alternateThumbFolder, alternateThumbFolder);
	}

	/**
	 * Checks if is hide engine names.
	 *
	 * @return true, if is hide engine names
	 */
	public boolean isHideEngineNames() {
		return getValue(KEY_hideEngineNames, false);
	}

	/**
	 * Sets the hide engine names.
	 *
	 * @param hideEngineNames the new hide engine names
	 */
	public void setHideEngineNames(boolean hideEngineNames) {
		setValue(KEY_hideEngineNames, hideEngineNames);
	}

	/**
	 * Checks if is hide extensions.
	 *
	 * @return true, if is hide extensions
	 */
	public boolean isHideExtensions() {
		return getValue(KEY_hideExtensions, true);
	}

	/**
	 * Sets the hide extensions.
	 *
	 * @param hideExtensions the new hide extensions
	 */
	public void setHideExtensions(boolean hideExtensions) {
		setValue(KEY_hideExtensions, hideExtensions);
	}

	/**
	 * Checks if is hide empty folders.
	 *
	 * @return true, if is hide empty folders
	 */
	public boolean isHideEmptyFolders() {
		return getValue(KEY_hideEmptyFolders, false);
	}

	/**
	 * Sets the hide empty folders.
	 *
	 * @param hideEmptyFolders the new hide empty folders
	 */
	public void setHideEmptyFolders(boolean hideEmptyFolders) {
		setValue(KEY_hideEmptyFolders, hideEmptyFolders);
	}

	/**
	 * Gets the sort method.
	 *
	 * @return the sort method
	 */
	public int getSortMethod() {
		return getValue(KEY_sortMethod, 0);
	}

	/**
	 * Sets the sort method.
	 *
	 * @param sortMethod the new sort method
	 */
	public void setSortMethod(int sortMethod) {
		setValue(KEY_sortMethod, sortMethod);
	}

	/**
	 * Checks if is browse archives.
	 *
	 * @return true, if is browse archives
	 */
	public boolean isBrowseArchives() {
		return getValue(KEY_browseArchives, false);
	}

	/**
	 * Sets the browse archives.
	 *
	 * @param browseArchives the new browse archives
	 */
	public void setBrowseArchives(boolean browseArchives) {
		setValue(KEY_browseArchives, browseArchives);
	}
}
