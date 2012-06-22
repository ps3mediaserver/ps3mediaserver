package net.pms.plugin.fileimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.savvasdalkitsis.jtmdb.CastInfo;
import com.savvasdalkitsis.jtmdb.GeneralSettings;
import com.savvasdalkitsis.jtmdb.Genre;
import com.savvasdalkitsis.jtmdb.Movie;
import com.savvasdalkitsis.jtmdb.Studio;

import net.pms.medialibrary.commons.enumarations.FileProperty;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.exceptions.FileImportException;
import net.pms.plugins.FileImportPlugin;
import net.pms.util.PmsProperties;

/** 
 * Class used to collect information about a movie from tmdb
 * 
 * @author pw
 *
 */
public class TmdbMovieImportPlugin implements FileImportPlugin {
	private static final Logger log = LoggerFactory.getLogger(TmdbMovieImportPlugin.class);
	public static final ResourceBundle messages = ResourceBundle.getBundle("net.pms.plugin.fileimport.tmdb.lang.messages");

	/** Holds only the project version. It's used to always use the maven build number in code */
	private static final PmsProperties properties = new PmsProperties();
	static {
		try {
			properties.loadFromResourceFile("/tmdbmovieimportplugin.properties", TmdbMovieImportPlugin.class);
		} catch (IOException e) {
			log.error("Could not load filesystemfolderplugin.properties", e);
		}
	}
	
	//available tags
	private enum Tag {
		Actor,
		Studio,
		Author,
		Producer
	}
	
	//the tmdb movie having been imported
	private Movie movie;
	
	//constants used to manage the min polling interval
	private final int MIN_POLLING_INTERVAL_MS = 1000;
	private final int POLLING_INCREMENT_MS = 200;

	//tmdb states that 10 requests every 10 seconds per IP are allowed.
	//http://help.themoviedb.org/kb/general/api-request-limits
	private int currentPollingIntervalMs = MIN_POLLING_INTERVAL_MS;

	private final int MAX_RETRIES = 3;
	private int nbRetriesDone = 0;

	@Override
	public String getName() {
		return "tmdb";
	}

	@Override
    public void importFile(String title, String filePath) throws FileImportException {
		if(log.isDebugEnabled()) log.debug("importing TMDb movie with title: " + title);
		
		//delete information which might still be cached since the last query
		movie = null;
		
	    try {
	    	//search for the title
	        List<Movie> movies = Movie.search(title);
	        
			if (movies != null && movies.size() > 0) {
				//we've found at least one result
				
				//use the first one
				movie = Movie.getInfo(movies.get(0).getID());
				
				//log the results received
				String moviesStr = String.format("Movie matched for '%s' on TMDb has id=%s, name='%s'", title, movies.get(0).getID(), movies.get(0).getName());
				if(movies.size() > 1){
					moviesStr += ". other (not considered) matches are ";
					for(int i = 1; i < movies.size(); i++) {
						moviesStr += String.format("id=%s, name='%s';", movies.get(i).getID(), movies.get(i).getName());
					}
					moviesStr = moviesStr.substring(0, moviesStr.length() - 2);
				}
				
				//set the polling interval to the min value and reset nbRetriesDone if we could execute the query
				currentPollingIntervalMs = MIN_POLLING_INTERVAL_MS;
				nbRetriesDone = 0;
				
				if(log.isInfoEnabled()) log.info(moviesStr);
			}else {
	        	throw new FileImportException(String.format("No movie information found for title='%s'", title));			
			}
	    } catch(IOException ex) {
	    	if(ex.getMessage().contains("response code: 503")){
	    		//sometimes tmdb craps out with a 503 error. Try again if the max retries limit hasn't been reached
	    		if(nbRetriesDone < MAX_RETRIES - 1) {
	    			//increment the wait timeout and min polling interval
	    			nbRetriesDone++;
	    			currentPollingIntervalMs += POLLING_INCREMENT_MS;
	    			
	    			log.info(String.format("Incremented polling interval after 503 error response. Polling interval=%s", currentPollingIntervalMs));
	    			
	    			//wait before trying again
	    			try {
						Thread.sleep(currentPollingIntervalMs);
					} catch (InterruptedException e) {
						log.error("Failed to pause thread to respect wait timeout");
					}
	    			
	    			//do a recursive call and try again
	    			importFile(title, filePath);
	    		} else {
	            	throw new FileImportException(String.format("Failed to import movie information for title='%s'", title), ex);	    			
	    		}
	    	}
        } catch (Throwable t) {
        	throw new FileImportException(String.format("Failed to import movie information for title='%s'", title), t);
        }	    
    }

	@Override
	public boolean isImportByIdPossible() {
		return true;
	}

	@Override
	public void importFileById(String id) throws FileImportException {
		int tmdbId;
		
		//try to convert the received id to an int
		try {
			tmdbId = Integer.parseInt(id);
		} catch (NumberFormatException ex) {
			throw new FileImportException(String.format("Failed to import film by tmdb id='%s' because it couldn't be converted to an Integer", id));
		}
		
		log.debug("Importing TMDb movie by id=" + id);
	    try {
			movie = Movie.getInfo(tmdbId);
			log.debug("Imported TMDb movie by id=" + id);
        } catch (Throwable t) {
        	throw new FileImportException(String.format("Failed to import movie information for id='%s'", id), t);
        }
	}

	@Override
	public List<FileType> getSupportedFileTypes() {
		return Arrays.asList(FileType.VIDEO);
	}

	@Override
	public List<String> getSupportedTags(FileType fileType) {
		List<String> res = new ArrayList<String>();
		for(Tag t : Tag.values()) {
			res.add(t.toString());
		}
		
		return res;
	}

	@Override
	public List<String> getTags(String tagName) {
		List<String> res = null;
		
		if (tagName.equals(Tag.Actor.toString())) {
			res = new ArrayList<String>();
			if (movie != null && movie.getCast() != null) {
				for (CastInfo ci : movie.getCast()) {
					if (ci.getJob().equals("Actor")) {
						res.add(ci.getName());
					}
				}
			}
			
		} else if (tagName.equals(Tag.Studio.toString())) {
			res = new ArrayList<String>();
			if (movie != null && movie.getStudios() != null) {
				for (Studio s : movie.getStudios()) {
					res.add(s.getName());
				}
			}
			
		} else if (tagName.equals(Tag.Author.toString())) {
			res = new ArrayList<String>();
			if (movie != null && movie.getCast() != null) {
				for (CastInfo ci : movie.getCast()) {
					if (ci.getJob().equals("Author")) {
						res.add(ci.getName());
					}
				}
			}
			
		} else if (tagName.equals(Tag.Producer.toString())) {
			res = new ArrayList<String>();
			if (movie != null && movie.getCast() != null) {
				for (CastInfo ci : movie.getCast()) {
					if (ci.getJob().equals("Producer")) {
						res.add(ci.getName());
					}
				}
			}
		}
	
		return res;
	}

	@Override
	public String getVersion() {
		return properties.get("project.version");
	}

	@Override
	public List<FileProperty> getSupportedFileProperties() {
		//add all supported properties
		List<FileProperty> res = new ArrayList<FileProperty>();
		res.add(FileProperty.VIDEO_CERTIFICATION);
		res.add(FileProperty.VIDEO_BUDGET);
		res.add(FileProperty.VIDEO_COVERURL);
		res.add(FileProperty.VIDEO_DIRECTOR);
		res.add(FileProperty.VIDEO_GENRES);
		res.add(FileProperty.VIDEO_HOMEPAGEURL);
		res.add(FileProperty.VIDEO_IMDBID);
		res.add(FileProperty.VIDEO_ORIGINALNAME);
		res.add(FileProperty.VIDEO_OVERVIEW);
		res.add(FileProperty.VIDEO_RATINGPERCENT);
		res.add(FileProperty.VIDEO_RATINGVOTERS);
		res.add(FileProperty.VIDEO_REVENUE);
		res.add(FileProperty.VIDEO_TAGLINE);
		res.add(FileProperty.VIDEO_NAME);
		res.add(FileProperty.VIDEO_TRAILERURL);
		res.add(FileProperty.VIDEO_YEAR);
		res.add(FileProperty.VIDEO_TMDBID);
		
		return res;
	}

	@Override
	public Object getFileProperty(FileProperty property) {
		//return the proper object for every supported file property
		switch (property) {
		case VIDEO_CERTIFICATION:
			return movie == null ? null : movie.getCertification();
		case VIDEO_BUDGET:
		    return movie == null ? null : movie.getBudget();
		case VIDEO_COVERURL:
			return movie == null ? null : !movie.getImages().posters.iterator().hasNext() ? null : movie.getImages().posters.iterator().next().getLargestImage().toString();
		case VIDEO_DIRECTOR:
			String director = null;
			if(movie != null && movie.getCast() != null){
			    for(CastInfo ci : movie.getCast()){
			    	if(ci.getJob().equals("Director")){
			    		director = ci.getName();
			    		break;
			    	}
			    }			
			}
		    return director;
		case VIDEO_GENRES:
			List<String> genres = null;
			if(movie != null && movie.getGenres() != null && !movie.getGenres().isEmpty()){
				genres = new ArrayList<String>();
			    for(Genre ci : movie.getGenres()){
			    	genres.add(ci.getName());
			    }
			}
		    return genres;
		case VIDEO_HOMEPAGEURL:
		    return movie == null || movie.getHomepage() == null ? null : movie.getHomepage().toString();
		case VIDEO_IMDBID:
		    return movie == null ? null : movie.getImdbID();
		case VIDEO_ORIGINALNAME:
		    return movie == null ? null : movie.getOriginalName();
		case VIDEO_OVERVIEW:
		    return movie == null || movie.getOverview().equals("null") ? null : movie.getOverview();
		case VIDEO_RATINGPERCENT:
			return movie == null ? null : (int)(movie.getRating() * 10);
		case VIDEO_RATINGVOTERS:
			return movie == null ? null : movie.getVotes();
		case VIDEO_REVENUE:
		    return movie == null ? null : (int)movie.getRevenue();
		case VIDEO_TAGLINE:
		    return movie == null ? null : movie.getTagline();
		case VIDEO_TMDBID:
		    return movie == null ? null : movie.getID();
		case VIDEO_NAME:
		    return movie == null ? null : movie.getName();
		case VIDEO_TRAILERURL:
		    return movie == null || movie.getTrailer() == null ? null : movie.getTrailer().toString();
		case VIDEO_YEAR:
			Calendar cal=Calendar.getInstance();
			cal.setTime(movie.getReleasedDate());
		    return movie == null || movie.getReleasedDate() == null ? null : cal.get(Calendar.YEAR);
		}
		return null;
	}

	@Override
	public int getMinPollingIntervalMs() {
		return currentPollingIntervalMs;
	}

	@Override
	public String getShortDescription() {
		return messages.getString("TmdbMovieImportPlugin.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		return messages.getString("TmdbMovieImportPlugin.LongDescription");
	}

	@Override
	public void shutdown() {
		// do nothing
	}

	@Override
	public boolean isSearchForFilePossible() {
		return true;
	}

	@Override
	public void importFileBySearchObject(Object searchObject) {
		if(searchObject != null && searchObject instanceof TmdbMovieInfoPluginMovie) {
			movie = ((TmdbMovieInfoPluginMovie)searchObject).getMovie();
		}
	}

	@Override
	public List<Object> searchForFile(String name) {
		List<Object> res = null;
	    try {
	    	//search for the name
	        List<Movie> movies = Movie.search(name);
	        
	        //create the return list if any movies were found
			if (movies != null && movies.size() > 0) {
				res = new ArrayList<Object>();
				for(int i = 0; i< movies.size(); i++){
					res.add(new TmdbMovieInfoPluginMovie(movies.get(i)));
				}
			}
        } catch (Throwable t) {
        	//don't propagate any error, return the default value and log the error
        	log.error(String.format("Failed to search for movie for name=%s'", name), t);
        }
	    return res;
	}

	@Override
	public JComponent getGlobalConfigurationPanel() {
		return null;
	}

	@Override
	public Icon getPluginIcon() {
		return new ImageIcon(getClass().getResource("/tmdb-32.png"));
	}

	@Override
	public String getUpdateUrl() {
		return null;
	}

	@Override
	public String getWebSiteUrl() {
		return "http://www.ps3mediaserver.org/";
	}

	@Override
	public void initialize() {
	    GeneralSettings.setApiKey("4cdddc892213dd24e5011fd710f8abf0");
	}

	@Override
	public void saveConfiguration() {
	}

	@Override
	public boolean isPluginAvailable() {
		return true;
	}
}
