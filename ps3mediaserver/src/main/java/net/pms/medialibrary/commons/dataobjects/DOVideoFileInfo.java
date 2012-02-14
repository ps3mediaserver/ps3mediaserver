package net.pms.medialibrary.commons.dataobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;

public class DOVideoFileInfo extends DOFileInfo {
	private String originalName;
	private String name;
	private String sortName;
	private int tmdbId;
	private String imdbId;
	private String overview;
	private DORating rating = new DORating();
	private String tagLine = "";
	private DOCertification ageRating = new DOCertification();
	private int year = 0;
	private int budget;
	private int revenue;
	private String homepageUrl;
	private String trailerUrl;
	private List<String> genres = new ArrayList<String>();
	private String director = "";
	
	private String aspectRatio = "";
	private int bitrate = 0;
	private int bitsPerPixel = 0;
	private String codecV = "";
	private double durationSec = 0;
	private String container = "";
	private int dvdtrack = 0;
	private String frameRate = "";
	private byte[] h264_annexB = new byte[0];
	private int height = 0;
	private String mimeType = "";
	private String model = "";
	private boolean muxable = false;
	private int width = 0;
	private String muxingMode;
	private List<DLNAMediaAudio> audioCodes = new ArrayList<DLNAMediaAudio>();
	private List<DLNAMediaSubtitle> subtitlesCodes = new ArrayList<DLNAMediaSubtitle>();
	
	public DOVideoFileInfo() {
		setType(FileType.VIDEO);
	}
	
	public String getDisplayString(String displayNameMask){
		String retVal = super.getDisplayString(displayNameMask);
		try { retVal = retVal.replace("%original_name", getOriginalName()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%name", getName()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%sort_name", getSortName()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%tmdb_id", Integer.toString(getTmdbId())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%imdb_id", getImdbId()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%overview", getOverview()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%rating_percent", Integer.toString(getRating().getRatingPercent())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%rating_voters", Integer.toString(getRating().getVotes())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%tagline", getTagLine()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%certification_reason", getAgeRating().getReason()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%certification", getAgeRating().getLevel()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%year", Integer.toString(getYear())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%budget", Integer.toString(getBudget())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%revenue", Integer.toString(getRevenue())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%homepage_url", getHomepageUrl()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%trailer_url", getTrailerUrl()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%director", getDirector()); } catch(Exception ex){ }		
		try { retVal = retVal.replace("%bitrate", Integer.toString(getBitrate())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%video_codec", getCodecV()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%duration", Double.toString(getDurationSec())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%container", getContainer()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%frame_rate", getFrameRate()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%height", Integer.toString(getHeight())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%mime_type", getMimeType()); } catch(Exception ex){ }
		try { retVal = retVal.replace("%muxable", Boolean.toString(isMuxable())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%width", Integer.toString(getWidth())); } catch(Exception ex){ }
		try { retVal = retVal.replace("%muxing_mode", getMuxingMode()); } catch(Exception ex){ }
		if(displayNameMask.contains("%genres")){
			List<String> genres = getGenres();
			Collections.sort(genres);

			StringBuilder sb = new StringBuilder();
			for(String genre : genres) {
				sb.append(genre);
				sb.append(", ");
			}
			String genresString = sb.toString();
			if(genresString.endsWith(", ")){
				genresString = genresString.substring(0, genresString.length() - 2);
			}
			retVal = retVal.replace("%genres", genresString);
		}
		if(displayNameMask.contains("%audio_languages")){
			List<DLNAMediaAudio> audioCodes = getAudioCodes();
			Collections.sort(audioCodes, new Comparator<DLNAMediaAudio>() {
				
				@Override
				public int compare(DLNAMediaAudio o1, DLNAMediaAudio o2) {
					return o1.getLangFullName().compareTo(o2.getLangFullName());
				}
			});
			StringBuilder sb = new StringBuilder();
			for(DLNAMediaAudio audio : audioCodes){
				sb.append(String.format("%s (%s), ", audio.getLangFullName(), audio.getCodecA()));
			}

			String audiosString = sb.toString();
			if(audiosString.endsWith(", ")){
				audiosString = audiosString.substring(0, audiosString.length() - 2);
			}
			retVal = retVal.replace("%audio_languages", audiosString);
		}
		if(displayNameMask.contains("%subtitle_languages")){
			List<DLNAMediaSubtitle> subtitlesCodes = getSubtitlesCodes();
			Collections.sort(subtitlesCodes, new Comparator<DLNAMediaSubtitle>() {

				@Override
				public int compare(DLNAMediaSubtitle o1, DLNAMediaSubtitle o2) {
					return o1.getLangFullName().compareTo(o2.getLangFullName());
				}
			});
			StringBuilder sb = new StringBuilder();
			for(DLNAMediaSubtitle subtitle : subtitlesCodes){
				sb.append(subtitle.getLangFullName());
				sb.append(", ");
			}
			String subtitlesString = sb.toString();
			if(subtitlesString.endsWith(", ")){
				subtitlesString = subtitlesString.substring(0, subtitlesString.length() - 2);
			}
			retVal = retVal.replace("%subtitle_languages", subtitlesString);
		}
		
		return retVal;
	}
	
	

	public void setAgeRating(DOCertification ageRating) {
		if(!getAgeRating().equals(ageRating)) {
		    this.ageRating = ageRating;
		    firepropertyChangedEvent(ConditionType.VIDEO_CERTIFICATION);
	    }
    }

	public DOCertification getAgeRating() {
		if(ageRating == null) ageRating = new DOCertification();
	    return ageRating;
    }

	public void setRating(DORating rating) {
		if(!getRating().equals(rating)) {
		    this.rating = rating;
		    firepropertyChangedEvent(ConditionType.VIDEO_RATINGPERCENT);
	    }
    }

	public DORating getRating() {
		if(rating == null) rating = new DORating();
	    return rating;
    }

	public void setDirector(String director) {
		if(!getDirector().equals(director)) {
		    this.director = director;
		    firepropertyChangedEvent(ConditionType.VIDEO_DIRECTOR);
	    }
    }

	public String getDirector() {
		if(director == null) director = "";
	    return director;
    }

	public void setGenres(List<String> genres) {
		if(!getGenres().equals(genres)) {
		    this.genres = genres;
		    firepropertyChangedEvent(ConditionType.VIDEO_CONTAINS_GENRE);
	    }
    }

	public List<String> getGenres() {
		if(genres == null) genres = new ArrayList<String>();
	    return genres;
    }

	public void setOverview(String overview) {
		if(!getOverview().equals(overview)) {
		    this.overview = overview;
		    firepropertyChangedEvent(ConditionType.VIDEO_OVERVIEW);
	    }
    }

	public String getOverview() {
		if(overview == null) overview = "";
	    return overview;
    }

	public void setTagLine(String tagLine) {
		if(!getTagLine().equals(tagLine)) {
		    this.tagLine = tagLine;
		    firepropertyChangedEvent(ConditionType.VIDEO_TAGLINE);
	    }
    }

	public String getTagLine() {
		if(tagLine == null) tagLine = "";
	    return tagLine;
    }

	public void setName(String name) {
		if(!getName().equals(name)) {
		    this.name = name;
		    firepropertyChangedEvent(ConditionType.VIDEO_NAME);
	    }
    }

	public String getName() {
		if(name == null) name = "";
	    return name;
    }

	public void setSortName(String sortName) {
		if(!getSortName().equals(sortName)) {
			this.sortName = sortName;
		    firepropertyChangedEvent(ConditionType.VIDEO_SORTNAME);
		}
	}

	public String getSortName() {
		if(sortName == null) sortName = "";
		return sortName;
	}

	public void setTrailerUrl(String trailerUrl) {
		if(!getTrailerUrl().equals(trailerUrl)) {
		    this.trailerUrl = trailerUrl;
		    firepropertyChangedEvent(ConditionType.VIDEO_TRAILERURL);
	    }
    }

	public String getTrailerUrl() {
		if(trailerUrl == null) trailerUrl = "";
	    return trailerUrl;
    }

	public void setOriginalName(String originalName) {
		if(!getOriginalName().equals(originalName)) {
		    this.originalName = originalName;
		    firepropertyChangedEvent(ConditionType.VIDEO_ORIGINALNAME);
	    }
    }

	public String getOriginalName() {
		if(originalName == null) originalName = "";
	    return originalName;
    }

	public void setTmdbId(int tmdbId) {
		if(getTmdbId() != tmdbId) {
		    this.tmdbId = tmdbId;
		    firepropertyChangedEvent(ConditionType.VIDEO_TMDBID);
	    }
    }

	public int getTmdbId() {
	    return tmdbId;
    }

	public void setImdbId(String imdbId) {
		if(!getImdbId().equals(imdbId)) {
		    this.imdbId = imdbId;
		    firepropertyChangedEvent(ConditionType.VIDEO_IMDBID);
	    }
    }

	public String getImdbId() {
		if(imdbId == null) imdbId = "";
	    return imdbId;
    }

	public void setRevenue(int revenue) {
		if(getRevenue() != revenue) {
		    this.revenue = revenue;
		    firepropertyChangedEvent(ConditionType.VIDEO_REVENUE);
	    }
    }

	public int getRevenue() {
	    return revenue;
    }

	public void setBudget(int budget) {
		if(getBudget() != budget) {
		    this.budget = budget;
		    firepropertyChangedEvent(ConditionType.VIDEO_BUDGET);
	    }
    }

	public int getBudget() {
	    return budget;
    }

	public void setHomepageUrl(String homepageUrl) {
		if(!getHomepageUrl().equals(homepageUrl)) {
		    this.homepageUrl = homepageUrl;
		    firepropertyChangedEvent(ConditionType.VIDEO_HOMEPAGEURL);
	    }
    }

	public String getHomepageUrl() {
		if(homepageUrl == null) homepageUrl = "";
	    return homepageUrl;
    }

	public void setAspectRatio(String aspectRatio) {
		if(!getAspectRatio().equals(aspectRatio)) {
		    this.aspectRatio = aspectRatio;
		    firepropertyChangedEvent(ConditionType.VIDEO_ASPECTRATIO);
	    }
    }

	public String getAspectRatio() {
		if(aspectRatio == null) aspectRatio ="";
	    return aspectRatio;
    }

	public void setBitrate(int bitrate) {
		if(getBitrate() != bitrate) {
		    this.bitrate = bitrate;
		    firepropertyChangedEvent(ConditionType.VIDEO_BITRATE);
	    }
    }

	public int getBitrate() {
	    return bitrate;
    }

	public void setBitsPerPixel(int bitsPerPixel) {
		if(getBitsPerPixel() != bitsPerPixel) {
		    this.bitsPerPixel = bitsPerPixel;
		    firepropertyChangedEvent(ConditionType.VIDEO_BITSPERPIXEL);
	    }
    }

	public int getBitsPerPixel() {
	    return bitsPerPixel;
    }

	public void setCodecV(String codecV) {
		if(!getCodecV().equals(codecV)) {
		    this.codecV = codecV;
		    firepropertyChangedEvent(ConditionType.VIDEO_CODECV);
	    }
    }

	public String getCodecV() {
		if(codecV == null) codecV ="";
	    return codecV;
    }

	public void setDurationSec(double durationSec) {
		if(getDurationSec() != durationSec) {
		    this.durationSec = durationSec;
		    firepropertyChangedEvent(ConditionType.VIDEO_DURATIONSEC);
	    }
    }

	public double getDurationSec() {
	    return durationSec;
    }

	public void setContainer(String container) {
		if(!getContainer().equals(container)) {
		    this.container = container;
		    firepropertyChangedEvent(ConditionType.VIDEO_CONTAINER);
	    }
    }

	public String getContainer() {
		if(container == null) container ="";
	    return container;
    }

	public void setDvdtrack(int dvdtrack) {
		if(getDvdtrack() != dvdtrack) {
		    this.dvdtrack = dvdtrack;
		    firepropertyChangedEvent(ConditionType.VIDEO_DVDTRACK);
	    }
    }

	public int getDvdtrack() {
	    return dvdtrack;
    }

	public void setFrameRate(String frameRate) {
		if(!getFrameRate().equals(frameRate)) {
		    this.frameRate = frameRate;
		    firepropertyChangedEvent(ConditionType.VIDEO_FRAMERATE);
	    }
    }

	public String getFrameRate() {
		if(frameRate == null) frameRate ="";
	    return frameRate;
    }

	public void setH264_annexB(byte[] h264_annexB) {
	    this.h264_annexB = h264_annexB;
    }

	public byte[] getH264_annexB() {
	    return h264_annexB;
    }

	public void setHeight(int height) {
		if(getHeight() != height) {
		    this.height = height;
		    firepropertyChangedEvent(ConditionType.VIDEO_HEIGHT);
	    }
    }

	public int getHeight() {
	    return height;
    }

	public void setMimeType(String mimeType) {
		if(!getMimeType().equals(mimeType)) {
		    this.mimeType = mimeType;
		    firepropertyChangedEvent(ConditionType.VIDEO_MIMETYPE);
	    }
    }

	public String getMimeType() {
		if(mimeType == null) mimeType = "";
	    return mimeType;
    }

	public void setModel(String model) {
		if(!getModel().equals(model)) {
		    this.model = model;
		    firepropertyChangedEvent(ConditionType.VIDEO_MODEL);
	    }
    }

	public String getModel() {
		if(model == null) model = "";
	    return model;
    }

	public void setMuxable(boolean muxable) {
		if(isMuxable() != muxable) {
		    this.muxable = muxable;
		    firepropertyChangedEvent(ConditionType.VIDEO_MUXABLE);
	    }
    }

	public boolean isMuxable() {
	    return muxable;
    }

	public void setWidth(int width) {
		if(getWidth() != width) {
		    this.width = width;
		    firepropertyChangedEvent(ConditionType.VIDEO_WIDTH);
	    }
    }

	public int getWidth() {
	    return width;
    }

	public void setYear(int year) {
		if(getYear() != year) {
		    this.year = year;
		    firepropertyChangedEvent(ConditionType.VIDEO_YEAR);
	    }
    }

	public int getYear() {
	    return year;
    }

	public void setAudioCodes(List<DLNAMediaAudio> audioCodes) {
		if(!getAudioCodes().equals(audioCodes)) {
		    this.audioCodes = audioCodes;
		    firepropertyChangedEvent(ConditionType.VIDEO_CONTAINS_VIDEOAUDIO);
	    }
    }

	public List<DLNAMediaAudio> getAudioCodes() {
		if(audioCodes == null) audioCodes = new ArrayList<DLNAMediaAudio>();
	    return audioCodes;
    }

	public void setSubtitlesCodes(List<DLNAMediaSubtitle> subtitlesCodes) {
		if(!getSubtitlesCodes().equals(subtitlesCodes)) {
		    this.subtitlesCodes = subtitlesCodes;
		    firepropertyChangedEvent(ConditionType.VIDEO_CONTAINS_SUBTITLES);
	    }
    }

	public List<DLNAMediaSubtitle> getSubtitlesCodes() {
		if(subtitlesCodes == null) subtitlesCodes = new ArrayList<DLNAMediaSubtitle>();
	    return subtitlesCodes;
    }

	public void setMuxingMode(String muxingMode) {
		if(getMuxingMode() != muxingMode) {
			this.muxingMode = muxingMode;
		    firepropertyChangedEvent(ConditionType.VIDEO_MUXINGMODE);
		}
	}

	public String getMuxingMode() {
		if(muxingMode == null) muxingMode = "";
		return muxingMode;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + super.hashCode();
		hashCode *= 24 + getOriginalName().hashCode();
		hashCode *= 24 + getName().hashCode();
		hashCode *= 24 + getSortName().hashCode();
		hashCode *= 24 + getTmdbId();
		hashCode *= 24 + getImdbId().hashCode();
		hashCode *= 24 + getOverview().hashCode();
		hashCode *= 24 + getRating().hashCode();
		hashCode *= 24 + getTagLine().hashCode();
		hashCode *= 24 + getAgeRating().hashCode();
		hashCode *= 24 + getYear();
		hashCode *= 24 + getBudget();
		hashCode *= 24 + getRevenue();
		hashCode *= 24 + getHomepageUrl().hashCode();
		hashCode *= 24 + getTrailerUrl().hashCode();
		hashCode *= 24 + getGenres().hashCode();
		hashCode *= 24 + getDirector().hashCode();

		hashCode *= 24 + getAspectRatio().hashCode();
		hashCode *= 24 + getBitrate();
		hashCode *= 24 + getBitsPerPixel();
		hashCode *= 24 + getCodecV().hashCode();
		hashCode *= 24 + getDurationSec();
		hashCode *= 24 + getContainer().hashCode();
		hashCode *= 24 + getDvdtrack();
		hashCode *= 24 + getFrameRate().hashCode();
		hashCode *= 24 + getHeight();
		hashCode *= 24 + getMimeType().hashCode();
		hashCode *= 24 + getModel().hashCode();
		hashCode *= 24 + (isMuxable() ? 1 : 2);
		hashCode *= 24 + getWidth();
		hashCode *= 24 + getAudioCodes().hashCode();
		hashCode *= 24 + getSubtitlesCodes().hashCode();
		hashCode *= 24 + getMuxingMode().hashCode();
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DOVideoFileInfo)){
			return false;
		}
		
		DOVideoFileInfo compObj = (DOVideoFileInfo)obj;
		if(getOriginalName().equals(compObj.getOriginalName())
				&& getName().equals(compObj.getName())
				&& getSortName().equals(compObj.getSortName())
				&& getTmdbId() == compObj.getTmdbId()
				&& getImdbId().equals(compObj.getImdbId())
				&& getOverview().equals(compObj.getOverview())
				&& getRating().equals(compObj.getRating())
				&& getTagLine().equals(compObj.getTagLine())
				&& getAgeRating().equals(compObj.getAgeRating())
				&& getYear() == compObj.getYear()
				&& getBudget() == compObj.getBudget()
				&& getRevenue() == compObj.getRevenue()
				&& getHomepageUrl().equals(compObj.getHomepageUrl())
				&& getTrailerUrl().equals(compObj.getTrailerUrl())
				&& getGenres().equals(compObj.getGenres())
				&& getDirector().equals(compObj.getDirector())				
				&& getAspectRatio().equals(compObj.getAspectRatio())
				&& getBitrate() == compObj.getBitrate()
				&& getBitsPerPixel() == compObj.getBitsPerPixel()
				&& getCodecV().equals(compObj.getCodecV())
				&& getDurationSec() == compObj.getDurationSec()
				&& getContainer().equals(compObj.getContainer())
				&& getDvdtrack() == compObj.getDvdtrack()
				&& getFrameRate().equals(compObj.getFrameRate())
				//&& h264_annexB.equals(compObj.h264_annexB)
				&& getHeight() == compObj.getHeight()
				&& getMimeType().equals(compObj.getMimeType())
				&& getModel().equals(compObj.getModel())
				&& isMuxable() == compObj.isMuxable()
				&& getWidth() == compObj.getWidth()
				&& getMuxingMode().equals(compObj.getMuxingMode())){
			
				if(getAudioCodes() != null && compObj.getAudioCodes() != null
						&& !getAudioCodes().equals(compObj.getAudioCodes())){
					return false;
				}
				if(getSubtitlesCodes() != null && compObj.getSubtitlesCodes() != null
						&& !getSubtitlesCodes().equals(compObj.getSubtitlesCodes())){
					return false;
				}
			return true;
		}
		
		return false;
	}
}