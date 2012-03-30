package net.pms.plugin.fileimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.medialibrary.commons.enumarations.FileProperty;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.exceptions.FileImportException;
import net.pms.medialibrary.external.FileImportPlugin;

/** 
 * Class used to collect information about a movie from tmdb
 * 
 * @author pw
 *
 */
public class ImdbMovieImportPlugin implements FileImportPlugin {	
	private static final Logger log = LoggerFactory.getLogger(ImdbMovieImportPlugin.class);
	
	private JSONObject movieObject;

	@Override
	public String getName() {
		return "imdb";
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public String getDescription() {
		return "Imports movie information from IMDB using services provided by http://www.imdbapi.com";
	}

	@Override
	public void shutdown() {
		//do nothing
	}

	@Override
	public JComponent getGlobalConfigurationPanel() {
		return null;
	}

	@Override
	public void importFile(String title, String filePath) throws FileImportException {
		//re-init object to avoid having obsolete data hanging around
		movieObject = null;
		
		try {
			URL call = new URL("http://www.imdbapi.com/?t=" + URLEncoder.encode(String.format("%s", title), "UTF8"));
			String jsonString = readUrlResponse(call).trim();

			movieObject = new JSONObject(jsonString.toString());
			Object response = movieObject.get("Response");
			if(response == null || response.toString().equals("Parse Error")) {
				movieObject = null;
				throw new FileImportException(String.format("Parse error in response when searching for title='%s'", title));
			}			
		} catch (IOException e) {
			throw new FileImportException(String.format("IOException when trying to query imdb for title='%s'", title), e);
		} catch (JSONException e) {
			throw new FileImportException(String.format("JSONException when trying to query imdb for title='%s'", title), e);
		}
	}

	@Override
	public void importFileById(String id) throws FileImportException {
		//re-init object to avoid having obsolete data hanging around
		movieObject = null;
		
		try {
			URL call = new URL("http://www.imdbapi.com/?i=" + id);
			String jsonString = readUrlResponse(call).trim();

			movieObject = new JSONObject(jsonString.toString());
			Object response = movieObject.get("Response");
			if(response == null || response.toString().equals("Parse Error")) {
				movieObject = null;
				throw new FileImportException(String.format("Parse error in response when searching for id='%s'", id));
			}			
		} catch (IOException e) {
			throw new FileImportException(String.format("IOException when trying to query imdb for id='%s'", id), e);
		} catch (JSONException e) {
			throw new FileImportException(String.format("JSONException when trying to query imdb for id='%s'", id), e);
		}
	}

	@Override
	public boolean isImportByIdPossible() {
		return true;
	}

	@Override
	public boolean isSearchForFilePossible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Object> searchForFile(String name) {
		return null;
	}

	@Override
	public void importFileBySearchObject(Object searchObject) {
		//do nothing
	}

	@Override
	public List<FileProperty> getSupportedFileProperties() {
		//add all supported properties
		List<FileProperty> res = new ArrayList<FileProperty>();
		res.add(FileProperty.VIDEO_CERTIFICATION);
		res.add(FileProperty.VIDEO_COVERURL);
		res.add(FileProperty.VIDEO_DIRECTOR);
		res.add(FileProperty.VIDEO_GENRES);
		res.add(FileProperty.VIDEO_IMDBID);
		res.add(FileProperty.VIDEO_OVERVIEW);
		res.add(FileProperty.VIDEO_RATINGPERCENT);
		res.add(FileProperty.VIDEO_RATINGVOTERS);
		res.add(FileProperty.VIDEO_NAME);
		res.add(FileProperty.VIDEO_YEAR);
		
		return res;
	}

	@Override
	public Object getFileProperty(FileProperty property) {
		Object res = null;
		// return the proper object for every supported file property
		switch (property) {
		case VIDEO_CERTIFICATION:
			res = getValue("Rated");
			break;
		case VIDEO_COVERURL:
			res = getValue("Poster");
			break;
		case VIDEO_DIRECTOR:
			res = getValue("Director");
			break;
		case VIDEO_GENRES:
			Object val = getValue("Genre");
			if(val != null) {
				List<String> genres = new ArrayList<String>();
				for(String genre : val.toString().split(",")) {
					String g = genre.trim();
					if(!genres.contains(g)) {
						genres.add(g);
					}
				}
				res = genres;
			}
			break;
		case VIDEO_IMDBID:
			res = getValue("ID");
			break;
		case VIDEO_OVERVIEW:
			res = getValue("Plot");
			break;
		case VIDEO_RATINGPERCENT:
			Object ratingObj = getValue("Rating");
			if(ratingObj != null) {
				try {
					double r = Double.parseDouble(ratingObj.toString());
					res = (int)(10 * r);
				} catch (NumberFormatException ex) {
					log.error(String.format("Failed to parse rating='%s' as a double", ratingObj.toString()), ex);
				}
			}
			break;
		case VIDEO_RATINGVOTERS:
			ratingObj = getValue("Votes");
			if(ratingObj != null) {
				try {
					res = Integer.parseInt(ratingObj.toString());
				} catch (NumberFormatException ex) {
					log.error(String.format("Failed to parse rating='%s' as a double", ratingObj.toString()), ex);
				}
			}
			break;
		case VIDEO_NAME:
			res = getValue("Title");
			break;
		case VIDEO_YEAR:
			ratingObj = getValue("Released");
			if(ratingObj != null) {
				try {
					String dStr = ratingObj.toString();
					if(dStr.length() > 3) {
						res = Integer.parseInt(dStr.substring(dStr.length() - 4, dStr.length()));
					}
				} catch (NumberFormatException ex) {
					log.error("Failed to parse release year='%s' as a double", ex);
				}
			}
			break;
		}
		return res;
	}

	private Object getValue(String key) {
		Object res = null;
		try {
			res = movieObject.get(key);
		} catch (JSONException e) {
			log.warn(String.format("Failed to get key='%s'", key));
		}
		return res;
	}

	@Override
	public List<String> getSupportedTags(FileType fileType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getTags(String tagName) {
		return null;
	}

	@Override
	public List<FileType> getSupportedFileTypes() {
		return Arrays.asList(FileType.VIDEO);
	}

	@Override
	public int getMinPollingIntervalMs() {
		return 1000;
	}
	
	/**
	 * This method will open a connection to the provided url and return its
	 * response.
	 * 
	 * @param url
	 *            The url to open a connection to.
	 * @return The respone.
	 * @throws IOException
	 */
	public static String readUrlResponse(URL url) throws IOException {
		URLConnection yc = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		String inputLine;
		StringBuffer responce = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			responce.append(inputLine);
		}
		in.close();
		return responce.toString();
	}
	
}
