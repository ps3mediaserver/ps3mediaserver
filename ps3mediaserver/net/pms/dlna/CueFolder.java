package net.pms.dlna;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import jwbroek.cuelib.CueParser;
import jwbroek.cuelib.CueSheet;
import jwbroek.cuelib.FileData;
import jwbroek.cuelib.Position;
import jwbroek.cuelib.TrackData;
import net.pms.PMS;
import net.pms.encoders.MEncoderVideo;
import net.pms.encoders.MPlayerAudio;
import net.pms.encoders.Player;
import net.pms.formats.Format;

public class CueFolder extends DLNAResource {
	
	private File playlistfile;
	public File getPlaylistfile() {
		return playlistfile;
	}

	private boolean valid = true;
	
	public CueFolder(File f) {
		playlistfile = f;
		lastmodified = playlistfile.lastModified();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return playlistfile.getName();
	}

	@Override
	public String getSystemName() {
		return playlistfile.getName();
	}

	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public void resolve() {
		if (playlistfile.length() < 10000000) {
			CueSheet sheet = null;
			try {
				sheet = CueParser.parse(playlistfile);
			} catch (IOException e) {
				PMS.minimal("Error in parsing cue: " + e.getMessage());
				return;
			}
			if (sheet != null) {
				
				List<FileData> files = sheet.getFileData();
				// only the first one
				if (files.size() > 0) {
					FileData f = files.get(0);
					List<TrackData> tracks = f.getTrackData();
					Player defaultPlayer = null;
					DLNAMediaInfo originalMedia = null;
					for(int i=0;i<tracks.size();i++) {
						TrackData track = tracks.get(i);
						if (i > 0) {
							double end = getTime(track.getIndices().get(0).getPosition());
							DLNAResource prec = children.get(i-1);
							if (prec.isFolder())
								prec = children.get(i);
							prec.splitLength = end - prec.splitStart;
							prec.media.setDurationString(prec.splitLength);
							PMS.info("Track #" + i + " split range: " + prec.splitStart + " - " + prec.splitLength);
						}
						Position start = track.getIndices().get(0).getPosition();
						RealFile r = new RealFile(new File(playlistfile.getParentFile(), f.getFile()));
						addChild(r);
						if (i > 0) {
							r.media = new DLNAMediaInfo();
							r.media.mediaparsed = true;
						}
						r.resolve();
						if (i == 0)
							originalMedia = r.media;
						r.splitStart = getTime(start);
						r.splitTrack = i+1;
						if (r.player == null) { // assign a splitter engine if file is natively supported by renderer
							if (defaultPlayer == null) {
								if (r.ext.isAudio())
									defaultPlayer = new MPlayerAudio(PMS.getConfiguration());
								else
									defaultPlayer = new MEncoderVideo(PMS.getConfiguration());
							}
							r.player = defaultPlayer;
						}
						if (r.media != null) {
							try {
								r.media = (DLNAMediaInfo) originalMedia.clone();
							} catch (CloneNotSupportedException e) {
								PMS.minimal("Error in cloning media info: " + e.getMessage());
							}
							if (r.media != null && r.media.getFirstAudioTrack() != null) {
								if (r.ext.isAudio())
									r.media.getFirstAudioTrack().songname = track.getTitle();
								else
									r.media.getFirstAudioTrack().songname = "Chapter #" + (i+1);
								r.media.getFirstAudioTrack().track = i+1;
								r.media.size = -1;
								if (StringUtils.isNotBlank(sheet.getTitle())) {
									r.media.getFirstAudioTrack().album = sheet.getTitle();
								}
								if (StringUtils.isNotBlank(sheet.getPerformer())) {
									r.media.getFirstAudioTrack().artist = sheet.getPerformer();
								}
							}
							
						}
						
					}
					
					if (tracks.size() > 0) {
						// last track
						DLNAResource prec = children.get(childrenNumber()-1);
						prec.splitLength = prec.media.getDurationInSeconds() - prec.splitStart;
						prec.media.setDurationString(prec.splitLength);
						PMS.info("Track #" + childrenNumber() + " split range: " + prec.splitStart + " - " + prec.splitLength);
					}
				
					if (PMS.getConfiguration().getUseCache()) {
						if (!PMS.get().getDatabase().isDataExists(playlistfile.getAbsolutePath(), playlistfile.lastModified())) {
							PMS.get().getDatabase().insertData(playlistfile.getAbsolutePath(), playlistfile.lastModified(), Format.PLAYLIST, null);
						}
					}
					
				}
			}
		}
	}
	
	private double getTime(Position p) {
		return p.getMinutes() * 60 + p.getSeconds() + (double) (p.getFrames() / 100);
	}

}
