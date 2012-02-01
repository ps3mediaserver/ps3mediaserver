package net.pms.medialibrary.gui.tab.libraryview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ListModel;

import net.pms.Messages;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.medialibrary.commons.dataobjects.DOAudioFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOImageFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOTableColumnConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionOperator;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.ConditionValueType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.helpers.DLNAHelper;
import net.pms.medialibrary.commons.helpers.FolderHelper;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.storage.MediaLibraryStorage;

import com.jgoodies.binding.adapter.AbstractTableAdapter;

public class FileDisplayTableAdapter extends AbstractTableAdapter<DOFileInfo> {
	private static final long serialVersionUID = 1369478633722749585L;
	private static Map<FileType, DOTableColumnConfiguration[]> columnConfigurations = new HashMap<FileType, DOTableColumnConfiguration[]>();
	private FileType fileType;

	public FileDisplayTableAdapter(ListModel listModel, FileType fileType) {
		super(listModel, getColumnNames(fileType, true));
		setFileType(fileType);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DOFileInfo fileInfo = (DOFileInfo)getRow(rowIndex);
		DOTableColumnConfiguration[] cConfs = getColumnConfigurations(getFileType());
		
		DOTableColumnConfiguration cd = cConfs[columnIndex];	
		
		Object res = null;
		if(fileInfo instanceof DOVideoFileInfo){
			res = getVideoFileValue((DOVideoFileInfo)fileInfo, cd);
		} else if(fileInfo instanceof DOAudioFileInfo){
			res = getAudioFileValue((DOAudioFileInfo)fileInfo, cd);
		} else if(fileInfo instanceof DOImageFileInfo){
			res = getPictureFileValue((DOImageFileInfo)fileInfo, cd);
		} else{
			res = getFileValue(fileInfo, cd);
		}
		return res;
	}

	private Object getFileValue(DOFileInfo file, DOTableColumnConfiguration cd) {
		Object res = null;
		
		if(res == null){
			StringBuilder sb;
			switch(cd.getConditionType()){
				case FILE_CONTAINS_TAG:
					List<String> keys = GUIHelper.asSortedList(file.getTags().keySet());
					sb = new StringBuilder();
					for(String key : keys){
						List<String> tagValues = file.getTags().get(key);
						Collections.sort(tagValues);
						sb.append(key);
						sb.append("=");
						sb.append(tagValues);
						sb.append(", ");
					}
					String tmpRes = sb.toString();
					if(tmpRes.length() > 0){
						tmpRes = tmpRes.substring(0, tmpRes.length() - 2);
					}
					res = tmpRes;
					break;
				case FILE_DATEINSERTEDDB:
					res = file.getDateInsertedDb();
					break;
				case FILE_DATELASTUPDATEDDB:
					res = file.getDateLastUpdatedDb();
					break;
				case FILE_DATEMODIFIEDOS:
					res = file.getDateModifiedOs();
					break;
				case FILE_FILENAME:
					res = file.getFileName();
					break;
				case FILE_FOLDERPATH:
					res = file.getFolderPath();
					break;
				case FILE_ISACTIF:
					res = file.isActif();
					break;
				case FILE_PLAYCOUNT:
					res = file.getPlayCount();
					break;
				case FILE_SIZEBYTE:
					res = file.getSize() / 1024 / 1024;
					break;
				case FILE_TYPE:
					res = Messages.getString("ML.FileType." + file.getType());
					break;
				case FILEPLAYS_DATEPLAYEND:
					res = file.getPlayHistory().size() > 0 ? file.getPlayHistory().get(0) : null;
					break;
				case FILE_THUMBNAILPATH:
					res = file.getThumbnailPath();
					break;
			}
		}		
		return res;		
	}
	
	private Object getVideoFileValue(DOVideoFileInfo video, DOTableColumnConfiguration cd) {
		Object res = getFileValue(video, cd);
		
		if(res == null){
			StringBuilder sb;
			switch(cd.getConditionType()){
				case VIDEO_CERTIFICATION:
					res = video.getAgeRating().getLevel();
					break;
				case VIDEO_CERTIFICATIONREASON:
					res = video.getAgeRating().getReason();
					break;
				case VIDEO_ASPECTRATIO:
					res = video.getAspectRatio();
					break;
				case VIDEO_BITRATE:
					res = video.getBitrate();
					break;
				case VIDEO_BITSPERPIXEL:
					res = video.getBitsPerPixel();
					break;
				case VIDEO_BUDGET:
					res = video.getBudget();
					break;
				case VIDEO_CODECV:
					res = video.getCodecV();
					break;
				case VIDEO_CONTAINER:
					res = video.getContainer();
					break;
				case VIDEO_CONTAINS_GENRE:
					List<String> genres = video.getGenres();
					Collections.sort(genres);
					sb = new StringBuilder();
					for(String genre : genres){
						sb.append(genre);
						sb.append(", ");
					}
					String tmpRes = sb.toString();
					if(tmpRes.length() > 0){
						tmpRes = tmpRes.substring(0, tmpRes.length() - 2);
					}
					res = tmpRes;
					break;
				case VIDEO_CONTAINS_SUBTITLES:
					List<DLNAMediaSubtitle> subtitlesCodes = video.getSubtitlesCodes();
					Collections.sort(subtitlesCodes, new Comparator<DLNAMediaSubtitle>() {

						@Override
						public int compare(DLNAMediaSubtitle o1, DLNAMediaSubtitle o2) {
							return o1.getLangFullName().compareTo(o2.getLangFullName());
						}
					});
					sb = new StringBuilder();
					for(DLNAMediaSubtitle sub : subtitlesCodes){
						sb.append(sub.getLangFullName());
						sb.append(", ");
					}
					tmpRes = sb.toString();
					if(tmpRes.length() > 0){
						tmpRes = tmpRes.substring(0, tmpRes.length() - 2);
					}
					res = tmpRes;
					break;
				case VIDEO_CONTAINS_VIDEOAUDIO:
					List<DLNAMediaAudio> audioCodes = video.getAudioCodes();
					Collections.sort(audioCodes, new Comparator<DLNAMediaAudio>() {
	
						@Override
						public int compare(DLNAMediaAudio o1, DLNAMediaAudio o2) {
							return o1.getLangFullName().compareTo(o2.getLangFullName());
						}
					});
					sb = new StringBuilder();
					for (DLNAMediaAudio audio : audioCodes) {
						sb.append(audio.getLangFullName());
						sb.append(" (");
						sb.append(audio.getAudioCodec());
						sb.append("), ");
					}
					tmpRes = sb.toString();
					if (tmpRes.length() > 0) {
						tmpRes = tmpRes.substring(0, tmpRes.length() - 2);
					}
					res = tmpRes;
					break;
				case VIDEO_DIRECTOR:
					res = video.getDirector();
					break;
				case VIDEO_DURATIONSEC:
					res = DLNAHelper.formatSecToHHMMSS((int)video.getDurationSec());
					break;
				case VIDEO_DVDTRACK:
					res = video.getDvdtrack();
					break;
				case VIDEO_FRAMERATE:
					res = video.getFrameRate();
					break;
				case VIDEO_HEIGHT:
					res = video.getHeight();
					break;
				case VIDEO_HOMEPAGEURL:
					res = video.getHomepageUrl();
					break;
				case VIDEO_IMDBID:
					res = video.getImdbId();
					break;
				case VIDEO_MIMETYPE:
					res = video.getMimeType();
					break;
				case VIDEO_MODEL:
					res = video.getModel();
					break;
				case VIDEO_MUXABLE:
					res = video.isMuxable();
					break;
				case VIDEO_NAME:
					res = video.getName();
					break;
				case VIDEO_ORIGINALNAME:
					res = video.getOriginalName();
					break;
				case VIDEO_OVERVIEW:
					res = video.getOverview();
					break;
				case VIDEO_RATINGPERCENT:
					res = video.getRating().getRatingPercent();
					break;
				case VIDEO_RATINGVOTERS:
					res = video.getRating().getVotes();
					break;
				case VIDEO_REVENUE:
					res = video.getRevenue();
					break;
				case VIDEO_SORTNAME:
					res = video.getSortName();
					break;
				case VIDEO_TAGLINE:
					res = video.getTagLine();
					break;
				case VIDEO_TMDBID:
					res = video.getTmdbId();
					break;
				case VIDEO_TRAILERURL:
					res = video.getTrailerUrl();
					break;
				case VIDEO_WIDTH:
					res = video.getWidth();
					break;
				case VIDEO_YEAR:
					res = video.getYear();
					break;
			}
		}
		
		return res;
	}
	
	public ConditionType getColumnConditionType(int columnIndex) {
		DOTableColumnConfiguration[] cConfs = getColumnConfigurations(getFileType());		
		DOTableColumnConfiguration cd = cConfs[columnIndex];
		return cd.getConditionType();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean res = false;		
		switch(getColumnConditionType(columnIndex)) {
		case VIDEO_NAME:
		case VIDEO_ORIGINALNAME:
		case VIDEO_YEAR:
		case VIDEO_SORTNAME:
		case VIDEO_DIRECTOR:
		case VIDEO_IMDBID:
		case VIDEO_HOMEPAGEURL:
		case VIDEO_TRAILERURL:
		case VIDEO_TMDBID:
		case VIDEO_RATINGPERCENT:
		case VIDEO_RATINGVOTERS:
		case VIDEO_CERTIFICATION:
		case VIDEO_CERTIFICATIONREASON:
		case VIDEO_TAGLINE:
		case VIDEO_OVERVIEW:
		case VIDEO_BUDGET:
		case VIDEO_REVENUE:
		case FILE_ISACTIF:
			res = true;
			break;
		}
		return res;
	}

	@Override
    public Class<?> getColumnClass(int c) {
		ConditionValueType vt = FolderHelper.getHelper().getConditionValueType(getColumnConditionType(c), ConditionOperator.IS);
		
		Class<?> res = null;
		switch(vt){
		case BOOLEAN:
			res = Boolean.class;
			break;
		case DATETIME:
			res = Date.class;
			break;
		case DOUBLE:
			res = Double.class;
			break;
		case FILESIZE:
			res = Integer.class;
			break;
		case INTEGER:
			res = Integer.class;
			break;
		case STRING:
			res = String.class;
			break;
		case TIMESPAN:
			res = Integer.class;
			break;
		default:
			res = String.class;
			break;
		}
        return res;
    }
	
	private Object getAudioFileValue(DOAudioFileInfo audio, DOTableColumnConfiguration cd) {
		Object res = getFileValue(audio, cd);
		
		switch (cd.getConditionType()) {
		case AUDIO_ALBUM:
			res = audio.getAlbum();
			break;
		case AUDIO_ARTIST:
			res = audio.getArtist();
			break;
		case AUDIO_BITSPERSAMPLE:
			res = audio.getBitsperSample();
			break;
		case AUDIO_CODECA:
			res = audio.getCodecA();
			break;
		case AUDIO_COVERPATH:
			res = audio.getCoverPath();
			break;
		case AUDIO_DELAYMS:
			res = audio.getDelay();
			break;
		case AUDIO_DURATION_SEC:
			res = audio.getDuration();
			break;
		case AUDIO_GENRE:
			res = audio.getGenre();
			break;
		case AUDIO_LANG:
			res = audio.getLang();
			break;
		case AUDIO_NRAUDIOCHANNELS:
			res = audio.getNrAudioChannels();
			break;
		case AUDIO_SAMPLEFREQ:
			res = audio.getSampleFrequency();
			break;
		case AUDIO_SONGNAME:
			res = audio.getSongName();
			break;
		case AUDIO_TRACK:
			res = audio.getTrack();
			break;
		case AUDIO_YEAR:
			res = audio.getYear();
			break;
		}
		return res;
	}
	
	private Object getPictureFileValue(DOImageFileInfo pic, DOTableColumnConfiguration cd) {
		Object res = getFileValue(pic, cd);
		
		switch (cd.getConditionType()) {
		case IMAGE_HEIGHT:
			break;
		case IMAGE_WIDTH:
			break;
		}
		return res;
	}
	
	@Override
	public int getColumnCount() {
		return getColumnConfigurations(getFileType()).length;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public FileType getFileType() {
		return fileType;
	}
	
	public static String[] getColumnNames(FileType fileType){
		ArrayList<String> columnNames = new ArrayList<String>();
		for(DOTableColumnConfiguration c : getColumnConfigurations(fileType)){
			columnNames.add(c.toString());
		}		
		return columnNames.toArray(new String[columnNames.size()]);
	}
	
	public static DOTableColumnConfiguration[] getColumnConfigurations(FileType fileType) {
		if(!columnConfigurations.containsKey(fileType)) {
			List<DOTableColumnConfiguration> columnConfigsList = MediaLibraryStorage.getInstance().getTableColumnConfiguration(fileType);
			columnConfigurations.put(fileType, columnConfigsList.toArray(new DOTableColumnConfiguration[columnConfigsList.size()]));
		}
		return columnConfigurations.get(fileType);
	}

	private static String[] getColumnNames(FileType fileType, boolean reinit) {
		if(reinit) {
			columnConfigurations.remove(fileType);
		}
		return getColumnNames(fileType);
	}
}
