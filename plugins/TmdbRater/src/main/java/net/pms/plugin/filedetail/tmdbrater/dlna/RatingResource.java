package net.pms.plugin.filedetail.tmdbrater.dlna;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.savvasdalkitsis.jtmdb.Movie;
import com.savvasdalkitsis.jtmdb.ServerResponse;
import com.savvasdalkitsis.jtmdb.Session;

import net.pms.dlna.DLNAResource;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.plugin.filedetail.TmdbHelper;

public class RatingResource extends DLNAResource {
	private static final Logger log = LoggerFactory.getLogger(RatingResource.class);
	private DOVideoFileInfo video;
	private float rating;
	private String videoOk;
	private String videoKo;
	private InputStream resStream;

	public RatingResource(DOVideoFileInfo video, float rating) {
	    this.video = video;
	    this.rating = rating;
	    
		videoOk = "/resources/videos/action_success-512.mpg";
		videoKo = "/resources/videos/button_cancel-512.mpg";
    }

	@Override
    public InputStream getInputStream() throws IOException {
		
		try{
	    	Session session = TmdbHelper.getSession();
	    	if(session != null && video != null && video.getTmdbId() > 0){
	    		//update rating
	    		if(log.isInfoEnabled()) log.info(String.format("Rate video '%s' %s/10. Session=%s", video.getTmdbId(), rating, session.getSession()));
	    		
		    		ServerResponse resp = Movie.addRating(video.getTmdbId(), rating, session);
		    		if(resp == ServerResponse.SUCCESS){
		    			if(log.isInfoEnabled()) log.info(String.format("Successfully rated video '%s' (id=%s) with %s stars", video.getName(), video.getId(), rating));
		    			resStream = getResourceInputStream(videoOk);
		    		}else {
		    			log.warn(String.format("Failed to rate video '%s' (id=%s). Server response was='%s'", video.getName(), video.getId(), resp));
		    			resStream = getResourceInputStream(videoKo);	
		    		}
	    	} 			
		}catch(IOException ex){
			String msg = ex.getMessage();
			if(msg.contains("401")){
    			log.warn(String.format("Failed to rate video '%s' (id=%s). Server response was=401 (not authorized)", video.getName(), video.getId()));
    			resStream = getResourceInputStream(videoKo);
			} else {
				throw ex;
			}
		}catch(Exception ex){
			log.error("Failed to rate movie", ex);
			resStream = getResourceInputStream(videoKo);
		}	

	    return resStream;
    }
	
	/*protected InputStream getResourceInputStream(String fileName) {
		fileName = "/resources/" + fileName;
		ClassLoader cll = this.getClass().getClassLoader();
		InputStream is = cll.getResourceAsStream(fileName.substring(1));
		while (is == null && cll.getParent() != null) {
			cll = cll.getParent();
			is = cll.getResourceAsStream(fileName.substring(1));
		}
		return is;
	}*/

	@Override
	protected InputStream getResourceInputStream(String resourcePath){
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);		
	}
	

	@Override
    public String getName() {
	    return String.valueOf(rating).replace(".0", "");
    }

	@Override
    public InputStream getThumbnailInputStream() throws IOException{
		return getResourceInputStream((String.format("/resources/images/rate-%s.png", rating)).toString());
	}

	@Override
    public String getSystemName() {
	    return getName();
    }

	@Override
    public boolean isFolder() {
	    return false;
    }

	@Override
    public boolean isValid() {
	    return true;
    }

	@Override
    public long length() {
	    return 0;
    }

}
