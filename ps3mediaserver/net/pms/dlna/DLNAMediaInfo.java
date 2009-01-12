/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
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
package net.pms.dlna;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.Tag;



import net.pms.PMS;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;
import net.pms.util.ProcessUtil;



public class DLNAMediaInfo {
		
	public static final long ENDFILE_POS = 99999475712L;
	public static final long TRANS_SIZE = 100000000000L;
	//private static SimpleDateFormat sdfExif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	
	// stored in database
	public String duration;
	public int bitrate;
	public int width;
	public int height;
	public long size;
	public int nrAudioChannels;
	public String sampleFrequency;
	public String codecV;
	public String codecA;
	public String frameRate;
	public String aspect;
	public byte thumb [];
	public String mimeType;
	public int bitsperSample = 16;
	public String album;
	public String artist;
	public String songname;
	public String genre;
	public String container;
	public int year;
	public int track;
	public ArrayList<DLNAMediaLang> audioCodes = new ArrayList<DLNAMediaLang>();
	public ArrayList<DLNAMediaLang> subtitlesCodes = new ArrayList<DLNAMediaLang>();
	public String model;
	public int exposure;
	public int orientation;
	public int iso;
	
	// no stored
	public boolean mediaparsed;
	public String types [] = null;
	public boolean losslessaudio;
	public int dvdtrack;
	public int maxsubid;
	public boolean secondaryFormatValid;
	public boolean parsing = false;
	
	public ProcessWrapperImpl getFFMpegThumbnail(File media) {
		String args [] = new String[14];
		args[0] = getFfmpegPath();
		args[1] = "-ss";
		args[2] = "" + PMS.getConfiguration().getThumbnailSeekPos();
		/*if (media.length() > 1000000000)
			args[2] = "20";
		else if (media.length() > 100000000)
			args[2] = "10";
		else if (media.length() > 10000000)
			args[2] = "1";*/
		args[3] = "-i";
		args[4] = ProcessUtil.getShortFileNameIfWideChars(media.getAbsolutePath());
		args[5] = "-an";
		args[6] = "-an";
		args[7] = "-s";
		args[8] = "320x180";
		args[9] = "-vframes";
		args[10] = "1";
		args[11] = "-f";
		args[12] = "image2";
		args[13] = "pipe:";
		if (!PMS.getConfiguration().getThumbnailsEnabled()) {
			args[2] = "0";
			for(int i=5;i<=13;i++)
				args[i] = "-an";
		}
		OutputParams params = new OutputParams(PMS.getConfiguration());
		params.maxBufferSize = 1;
		params.noexitcheck = true; // not serious if anything happens during the thumbnailer
		final ProcessWrapperImpl pw = new ProcessWrapperImpl(args, params);
		// FAILSAFE
		parsing = true;
		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
				pw.stopProcess();
				parsing = false;
			}
		};
		Thread failsafe = new Thread(r);
		failsafe.start();
		pw.run();
		parsing = false;
		return pw;
	}


	private String getFfmpegPath() {
		String value = PMS.getConfiguration().getFfmpegPath();
		if (value == null) {
			PMS.minimal("No ffmpeg - unable to thumbnail");
			throw new RuntimeException("No ffmpeg - unable to thumbnail");
		} else {
			return value;
		}
	}

	
	public void parse(File f, Format ext, int type) {
		int i=0;
		while (parsing) {
			if (i == 5) {
				mediaparsed = true;
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			i++;
		}
		if (mediaparsed)
			return;
		
		
		if (f != null && f.isFile()) {
			size = f.length();
			ProcessWrapperImpl pw = null;
			boolean ffmpeg_parsing = true;
			if (type == Format.AUDIO || f.getName().toLowerCase().endsWith("flac")) {
				ffmpeg_parsing = false;
				try {
					AudioFile af = AudioFileIO.read(f);
					AudioHeader ah = af.getAudioHeader();
					if (ah != null) {
						int length = ah.getTrackLength();
						int rate = ah.getSampleRateAsNumber();
						if (ah.getEncodingType().toLowerCase().contains("flac 24")) {
							bitsperSample=24;
							/*if (rate == 32000) {
								rate = 3* rate;
								length = length /3;
							}*/
							
						}
						sampleFrequency = "" + rate;
						setDurationString(length);
						bitrate = (int) ah.getBitRateAsNumber();
						nrAudioChannels = 2;
						if (ah.getChannels() != null && ah.getChannels().toLowerCase().contains("mono")) {
							nrAudioChannels = 1;
						} else if (ah.getChannels() != null && ah.getChannels().toLowerCase().contains("stereo")) {
							nrAudioChannels = 2;
						} else if (ah.getChannels() != null) {
							nrAudioChannels = Integer.parseInt(ah.getChannels());
						}
						codecA = ah.getEncodingType().toLowerCase();
					}
					Tag t = af.getTag();
					if (t != null) {
						album = t.getFirstAlbum();
						artist = t.getFirstArtist();
						songname = t.getFirstTitle();
						String y = t.getFirstYear();
						if (t.getArtworkList().size() > 0) {
							thumb = t.getArtworkList().get(0).getBinaryData();
						}
						try {
							if (y.length() > 4)
								y = y.substring(0, 4);
							year = Integer.parseInt(((y != null && y.length() > 0)?y:"0"));
							y = t.getFirstTrack();
							track = Integer.parseInt(((y != null && y.length() > 0)?y:"1"));
							genre = t.getFirstGenre();
						} catch (Throwable e) {
							PMS.info("error in parsing unimportant metadata: " + e.getMessage());
						}
					}
				} catch (Throwable e) {
					PMS.info("Error in parsing audio file: " + e.getMessage() + " - " + (e.getCause()!=null?e.getCause().getMessage():"") + " / Switching to ffmpeg");
					ffmpeg_parsing = true;
				}
				if (songname == null || songname.length() == 0)
					songname = f.getName();
			}
			if (type == Format.IMAGE) {
				try {
					ffmpeg_parsing = false;
					ImageInfo info = Sanselan.getImageInfo(f);
					width = info.getWidth();
					height = info.getHeight();
					bitsperSample = info.getBitsPerPixel();
					String formatName = info.getFormatName();
					if (formatName.startsWith("JPEG")) {
						IImageMetadata meta = Sanselan.getMetadata(f);
						if (meta != null && meta instanceof JpegImageMetadata) {
							JpegImageMetadata jpegmeta = (JpegImageMetadata) meta;
							TiffField tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_MODEL);
							if (tf != null)
								model = tf.getStringValue().trim();
	
							tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_EXPOSURE_TIME);
							if (tf != null)
								exposure = (int) (1000*tf.getDoubleValue());
	
							tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_ORIENTATION);
							if (tf != null)
								orientation = tf.getIntValue();
	
							tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_ISO);
							if (tf != null)
								iso = tf.getIntValue();
						}
					}
				} catch (Throwable e) {
					ffmpeg_parsing = true;
					PMS.minimal("Error during the parsing of image with Sanselan... switching to Ffmpeg: " + e.getMessage());
				}
			}
			if (ffmpeg_parsing) {
				pw = getFFMpegThumbnail(f);
				boolean matchs = false;
				ArrayList<String> lines = (ArrayList<String>) pw.getResults();
				int langId = 0;
				int subId = 0;
				int nbUndAudioTracks = 0;
				for(String line:lines) {
					line = line.trim();
					if (line.startsWith("Output"))
						matchs = false;
					else if (line.startsWith("Input")) {
						if (line.indexOf(ProcessUtil.getShortFileNameIfWideChars(f.getAbsolutePath())) > -1) {
							matchs = true;
							container = line.substring(10, line.indexOf(",", 11)).trim();
						} else
							matchs = false;
					} else if (matchs) {
						if (line.indexOf("Duration") > -1) {
							StringTokenizer st = new StringTokenizer(line, ",");
							while (st.hasMoreTokens()) {
								String token = st.nextToken().trim();
								if (token.startsWith("Duration: ") && (codecA == null || !codecA.equals("flac"))) {
									duration = token.substring(10);
									int l = duration.substring(duration.indexOf(".")+1).length();
									if (l < 4) {
										duration = duration + "00".substring(0, 3-l); 
									}
									if (duration.indexOf("N/A") > -1)
										duration = null;
								} else if (token.startsWith("bitrate: ")) {
									String bitr = token.substring(9);
									int spacepos = bitr.indexOf(" ");
									if (spacepos > -1) {
										String value = bitr.substring(0, spacepos);
										String unit = bitr.substring(spacepos+1);
										bitrate = Integer.parseInt(value);
										if (unit.equals("kb/s"))
											bitrate = 1024 * bitrate;
										if (unit.equals("mb/s"))
											bitrate = 1048576 * bitrate;
									}
								}
							}
						} else if (line.indexOf("Audio:") > -1) {
							StringTokenizer st = new StringTokenizer(line, ",");
							int a = line.indexOf("(");
							int b = line.indexOf("):", a);
							DLNAMediaLang lang = new DLNAMediaLang();
							if (langId == 0 && container.equals("avi"))
								langId++;
							lang.id = langId++;
							if (a > -1 && b > a) {
								lang.lang = line.substring(a+1, b);
							} else {
								lang.lang = "und";
								nbUndAudioTracks++;
							}
							audioCodes.add(lang);
							while (st.hasMoreTokens()) {
								String token = st.nextToken().trim();
								if (token.startsWith("Stream")) {
									codecA = token.substring(token.indexOf("Audio: ")+7);
									lang.format = codecA;
									
								} else if (token.endsWith("Hz")) {
									sampleFrequency = token.substring(0, token.indexOf("Hz")).trim();
								} else if (token.equals("mono")) {
									nrAudioChannels = 1;
								} else if (token.equals("stereo")) {
									nrAudioChannels = 2;
								} else if (token.equals("5:1")) {
									nrAudioChannels = 6;
								} else if (token.equals("4 channels")) {
									nrAudioChannels = 4;
								} else if (token.equals("s32")) {
									bitsperSample = 32;
								} else if (token.equals("s24")) {
									bitsperSample = 24;
								}
							}
						} else if (line.indexOf("Video:") > -1) {
							StringTokenizer st = new StringTokenizer(line, ",");
							while (st.hasMoreTokens()) {
								String token = st.nextToken().trim();
								if (token.startsWith("Stream")) {
									codecV = token.substring(token.indexOf("Video: ")+7);
								} else if (token.indexOf(".") > -1 && token.indexOf("tb") > -1) {
									frameRate = token.substring(0, token.indexOf("tb")).trim();
								} else if (token.indexOf("x") > -1) {
									String resolution = token.trim();
									if (resolution.indexOf(" [") > -1)
										resolution = resolution.substring(0, resolution.indexOf(" ["));
									try {
										width = Integer.parseInt(resolution.substring(0, resolution.indexOf("x")));
										height = Integer.parseInt(resolution.substring(resolution.indexOf("x")+1));
									} catch (NumberFormatException nfe) {}
								}
							}
						} else if (line.indexOf("Subtitle:") > -1 && !line.contains("tx3g")) {
							int a = line.indexOf("(");
							int b = line.indexOf("):", a);
							if (a > -1 && b > a) {
								DLNAMediaLang lang = new DLNAMediaLang();
								lang.lang = line.substring(a+1, b);
								lang.id = subId++;
								subtitlesCodes.add(lang);
							}
						}
					}
				}
				
				// remove audio code when there's only one "und" track
				if (nbUndAudioTracks == 1 && audioCodes.size() == 1) {
					audioCodes.remove(0);
				}
			
				if (type == Format.VIDEO && pw != null) {
					InputStream is;
					try {
						is = pw.getInputStream(0);
						int sz = is.available();
						if (sz > 0) {
							thumb = new byte [sz];
							is.read(thumb);
						}
						is.close();
						
						if (sz > 0) {
							BufferedImage image = ImageIO.read(new ByteArrayInputStream(thumb));
							if (image != null) {
								Graphics g = image.getGraphics();
								g.setColor(Color.WHITE);
								g.setFont(new Font("Arial", Font.PLAIN, 14));
								int low = 0;
								if (width > 0) {
									if (width == 1920 || width == 1440)
										g.drawString("1080p", 0, low+=18);
									else if (width == 1280)
										g.drawString("720p", 0, low+=18);
								}
								if (nrAudioChannels > 0) {
									g.drawString(nrAudioChannels + " channels " + (codecA!=null?codecA:""), 0, low+=18);
								}
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								ImageIO.write(image, "jpeg", out);
								thumb = out.toByteArray();
							}
						}
						
					} catch (IOException e) {
						PMS.info("Error while decoding thumbnail of " + f.getAbsolutePath() + " : " + e.getMessage());
					}
				}
				
			}
			finalize(type);
			mediaparsed = true;
			
		}
		
			
		
	}
	
	public int getFrameNumbers() {
		double fr = Double.parseDouble(frameRate);
		return (int) (getDurationInSeconds()*fr);
	}
	
	public void setDurationString(double d) {
		int s = ((int)d) % 60;
		int h = (int)(d / 3600);
		int m = ((int)(d / 60)) % 60;
		duration = String.format("%02d:%02d:%02d.00", h, m, s);
	}
	
	public double getDurationInSeconds() {
		if (duration != null) {
		StringTokenizer st = new StringTokenizer(duration, ":");
		try {
			int h = Integer.parseInt(st.nextToken());
			int m = Integer.parseInt(st.nextToken());
			double s = Double.parseDouble(st.nextToken());
			return h*3600+m*60+s;
		} catch(NumberFormatException nfe) {
			
		}
		}
		return 0;
		
	}
	
	public void finalize(int type) {
		if (container != null && container.equals("avi")) {
			mimeType = HTTPResource.AVI_TYPEMIME;
		} else if (codecV != null && codecV.equals("mjpeg")) {
			mimeType = HTTPResource.JPEG_TYPEMIME;
		} else if (codecV != null && codecV.equals("png")) {
			mimeType = HTTPResource.PNG_TYPEMIME;
		} else if (codecV != null && codecV.equals("gif")) {
			mimeType = HTTPResource.GIF_TYPEMIME;
		} else if (codecV != null && (codecV.equals("h264") || codecV.equals("h263") || codecV.toLowerCase().equals("mpeg4"))) {
			mimeType = HTTPResource.MP4_TYPEMIME;
		} else if (codecV != null && (codecV.indexOf("mpeg") > -1 || codecV.indexOf("mpg") > -1)) {
			mimeType = HTTPResource.MPEG_TYPEMIME;
		} else if (codecV == null && codecA != null && codecA.contains("mp3")) {
			mimeType = HTTPResource.AUDIO_MP3_TYPEMIME;
		} else if (codecV == null && codecA != null && codecA.contains("aac")) {
			mimeType = HTTPResource.AUDIO_MP4_TYPEMIME;
		} else if (codecV == null && codecA != null && (codecA.startsWith("pcm") || codecA.contains("wav"))) {
			mimeType = HTTPResource.AUDIO_WAV_TYPEMIME;
		} else {
			mimeType = new HTTPResource().getDefaultMimeType(type);
		}
		
		if (codecA != null && getSampleRate() > 44000 && (codecA.contains("pcm") || codecA.equals("dts") || codecA.equals("dca") || codecA.contains("flac")))
				losslessaudio = true;
		
		if (bitsperSample == 24)
			secondaryFormatValid = true;
		
		if (subtitlesCodes != null && subtitlesCodes.size() > 0) {
			for(DLNAMediaLang lang:subtitlesCodes) {
				if (lang.id > maxsubid)
					maxsubid = lang.id;
			}
		}
		
		PMS.debug("Media info: mimeType: " + mimeType + " / " + toString());
	}
	
	
	public String toString() {
		return "container: " + container + " / bitrate: " + bitrate + " / size: " + size + " / codecV: " + codecV + " / duration: " + duration + " / width: " + width + " / height: " + height + " / frameRate: " + frameRate + " / codecA: " + codecA + " / sampleFrequency: " + sampleFrequency + " / nrAudioChannels: " + nrAudioChannels;
	}

	public InputStream getThumbnailInputStream() {
		return new ByteArrayInputStream(thumb);
	}
	
	public String getValidFps(boolean ratios) {
		String validFrameRate = null;
		if (frameRate != null && frameRate.length() > 0) {
			try {
				double fr = Double.parseDouble(frameRate);
				if (fr > 23.96 && fr < 23.99) {
					validFrameRate = ratios?"24000/1001":"23.976";
				} else if (fr > 23.99 && fr < 24.01) {
					validFrameRate = "24";
				} else if (fr > 24.99 && fr < 25.01) {
					validFrameRate = "25";
				} else if (fr > 29.96 && fr < 29.99) {
					validFrameRate = ratios?"30000/1001":"29.97";
				} else if (fr > 29.99 && fr < 30.01) {
					validFrameRate = "30";
				} else if (fr > 49.99 && fr < 50.01) {
					validFrameRate = "50";
				} else if (fr > 59.96 && fr < 59.99) {
					validFrameRate = ratios?"60000/1001":"59.97";
				} else if (fr > 59.99 && fr < 60.01) {
					validFrameRate = "60";
				}
			} catch (NumberFormatException nfe) {
				PMS.error(null, nfe);
			}
			
		}
		return validFrameRate;
	}
	
	public String getValidAspect(boolean ratios) {
		String a = null;
		if (aspect != null) {
			double ar = Double.parseDouble(aspect);
			if (ar > 1.7 && ar < 1.8)
				a = ratios?"16/9":"1.777777777777777";
			if (ar > 1.3 && ar < 1.4)
				a = ratios?"4/3":"1.333333333333333";
		}
		return a;
	}
	
	public String getResolution() {
		if (width > 0 && height > 0)
			return width + "x" + height;
		return null;
	}
	
	public boolean isHDVideo() {
		return (width > 1200 || height > 700);
	}
	
	public int getSampleRate() {
		int sr = 0;
		if (sampleFrequency != null && sampleFrequency.length() > 0) {
			try {
				sr = Integer.parseInt(sampleFrequency);
			} catch (NumberFormatException e) {}
			
		}
		return sr;
	}
	
	public int [] getAudioSubLangIds() {
		int audiosubs [] = null;
		if (PMS.getConfiguration().getMencoderAudioSubLanguages() != null && PMS.getConfiguration().getMencoderAudioSubLanguages().length() > 0) {
			int aid = -1;
			int sid = -1;
			try {
				StringTokenizer st1 = new StringTokenizer(PMS.getConfiguration().getMencoderAudioSubLanguages(), ";");
				while (st1.hasMoreTokens() && aid == -1 && sid == -1) {
					String pair = st1.nextToken();
					String audio = pair.substring(0, pair.indexOf(","));
					String sub = pair.substring(pair.indexOf(",")+1);
					for(DLNAMediaLang lang:audioCodes) {
						if (lang.lang.equals(audio)) {
							for(DLNAMediaLang sublang:subtitlesCodes) {
								if (sublang.lang.equals(sub)) {
									aid = lang.id;
									sid = sublang.id;
								}
							}
							if (sid == -1 && sub.equals("off")) {
								aid = lang.id;
								sid = maxsubid+1;
							}
						}
					}
				}
			} catch (Throwable t) {
				PMS.info("Unexpected error while parsing the audio/sub languages value: " + t.getMessage());
			}
			if (aid > -1 && sid > -1) {
				audiosubs = new int [2];
				audiosubs[0] = aid;
				audiosubs[1] = sid;
			}
		}
		return audiosubs;
	}
	
}
