package net.pms.medialibrary.external;

import java.util.List;

import net.pms.medialibrary.commons.enumarations.FileProperty;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.exceptions.FileImportException;

/**
 * Classes implementing this interface and packaged as plugins will be used by
 * pms-mlx to import additional informations about files while scanning folders
 * or editing files in the library. A file can be of type video, audio or a
 * picture.
 * 
 * @author pw
 * 
 */
public interface FileImportPlugin extends PmsPluginBase {

	/**
	 * The plugin should import the properties that will be available through
	 * getFileProperty and getTags when this method is being called.<br>
	 * This method will only be called if a required file type or tag is needed.<br>
	 * It's up to the plugin developer to decide if he wants to use the title or
	 * the filePath.
	 * 
	 * @exception FileImportException
	 *                must be thrown if it the file import failed or no
	 *                information could be found for the given title and path
	 * @param title
	 *            the title for which to search. It usually corresponds to the
	 *            file name but it might have been modified (cleaned)
	 * @param filePath
	 *            the absolute path of the file to import
	 */
	public void importFile(String title, String filePath)
			throws FileImportException;

	/**
	 * The plugin should import the properties that will be available through
	 * getFileProperty and getTags when this method is being called.<br>
	 * This method will only be called if isImportByIdPossible returns true.
	 * 
	 * @exception FileImportException
	 *                must be thrown if it wasn't possible to import the
	 *                information for the given id
	 * @param id
	 *            the id for which the plugin should search. E.g. the tmdb
	 *            plugin will accept the tmdbId, the one for imdb the imdbId
	 */
	public void importFileById(String id) throws FileImportException;

	/**
	 * Tells if it is possible to call importById
	 * 
	 * @return true if it is possible to import file info by id, false otherwise
	 */
	public boolean isImportByIdPossible();

	/**
	 * Tells if the methods searchForFile and importFileBySearchObject can be
	 * called
	 * 
	 * @return true if searchForFile and importFileBySearchObject can be called,
	 *         false otherwise
	 */
	public boolean isSearchForFilePossible();

	/**
	 * When this method is being called, it has to return a list of Object
	 * containing the possible results for the queried name.<br>
	 * The user will be asked to choose which result is the correct one (based
	 * on Object.toString()), then importFileBySearchObject will be called with
	 * the selected Object
	 * 
	 * @param name
	 *            the string to search for
	 * @return a list of Objects containing possible results, where
	 *         Object.toString() has to return a comprehensive name for the user
	 */
	public List<Object> searchForFile(String name);

	/**
	 * Import file properties based on the search object (having been previously
	 * returned by searchForFile)
	 * 
	 * @param searchObject
	 *            the object containing the required information to collect the
	 *            import data
	 */
	public void importFileBySearchObject(Object searchObject);

	/**
	 * Returns a list containing all file properties for which it is possible to
	 * call getFileProperty()
	 * 
	 * @return the list of supported file properties
	 */
	public List<FileProperty> getSupportedFileProperties();

	/**
	 * Returns the value for the file property. A generic object can be
	 * returned, but it will be checked in pms-mlx if the correct type has been
	 * returned for the FileProperty.<br>
	 * If no value could be found, null should be returned. For integers < 0
	 * will be handled the same way as well as an empty String for String values<br>
	 * <br>
	 * Expected values for file types:<br>
	 * VIDEO_CERTIFICATION = String<br>
	 * VIDEO_CERTIFICATIONREASON = String<br>
	 * VIDEO_BUDGET = Integer<br>
	 * VIDEO_COVERURL = String<br>
	 * VIDEO_DIRECTOR = String<br>
	 * VIDEO_GENRES = List of String<br>
	 * VIDEO_HOMEPAGEURL = String<br>
	 * VIDEO_IMDBID = String<br>
	 * VIDEO_NAME = String<br>
	 * VIDEO_ORIGINALNAME = String<br>
	 * VIDEO_OVERVIEW = String<br>
	 * VIDEO_RATINGPERCENT = Integer<br>
	 * VIDEO_RATINGVOTERS = Integer<br>
	 * VIDEO_REVENUE = Integer<br>
	 * VIDEO_TAGLINE = String<br>
	 * VIDEO_TMDBID = Integer<br>
	 * VIDEO_TRAILERURL = String<br>
	 * VIDEO_YEAR = Integer<br>
	 * <br>
	 * AUDIO_SONGNAME = String<br>
	 * AUDIO_ARTIST = String<br>
	 * AUDIO_ALBUM = String<br>
	 * AUDIO_GENRE = String<br>
	 * AUDIO_YEAR = Integer<br>
	 * AUDIO_COVERPATH = String<br>
	 * <br>
	 * PICTURES_WIDTH = Integer<br>
	 * PICTURES_HEIGHT = Integer<br>
	 * 
	 * @param property
	 *            The file property for which to get the value
	 * @return the value associated with the file property
	 */
	public Object getFileProperty(FileProperty property);

	/**
	 * Gets the list of supported tags for a file type Beside the predefined
	 * FileProperties, custom tags consisting of a key-value pair can be
	 * configured. E.g. key=Actor, value=Jeff Bridges or key=language,
	 * value=German. This method will return the keys
	 * 
	 * @param fileType
	 *            The file type for which to get the tags. Only file types
	 *            returned by getSupportedFileTypes() will be queried
	 * @return the list of tag names for which getTags can be called with
	 */
	public List<String> getSupportedTags(FileType fileType);

	/**
	 * Returns a list of values for the tag. A key can have multiple values!
	 * 
	 * @param tagName
	 *            name of the tag
	 * @return list of values for the tag
	 */
	public List<String> getTags(String tagName);

	/**
	 * Returns the list of supported file types (audio, video, picture)
	 * 
	 * @return supported file types
	 */
	public List<FileType> getSupportedFileTypes();

	/**
	 * Some web sites don't allow more then x/requests per second. If the value
	 * returned by this method is > 0 pms will ensure that two successive calls
	 * to a importFile method have a timespan of at least minPollingInterval in
	 * milliseconds
	 * 
	 * @return minimum polling interval in milliseconds
	 */
	public int getMinPollingIntervalMs();
}
