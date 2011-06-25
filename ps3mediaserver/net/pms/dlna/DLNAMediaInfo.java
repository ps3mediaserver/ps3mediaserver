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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.formats.AudioAsVideo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;
import net.pms.util.AVCHeader;
import net.pms.util.CoverUtil;
import net.pms.util.FileUtil;
import net.pms.util.MpegUtil;
import net.pms.util.ProcessUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

public class DLNAMediaInfo implements Cloneable {
	private static final Logger logger = LoggerFactory.getLogger(DLNAMediaInfo.class);
	public static final long ENDFILE_POS = 99999475712L;
	public static final long TRANS_SIZE = 100000000000L;

	// Stored in database
	public String duration;
	public int bitrate;
	public int width;
	public int height;
	public long size;
	public String codecV;
	public String frameRate;
	public String aspect;
	public byte thumb[];
	public String mimeType;
	public int bitsPerPixel;
	public ArrayList<DLNAMediaAudio> audioCodes = new ArrayList<DLNAMediaAudio>();
	public ArrayList<DLNAMediaSubtitle> subtitlesCodes = new ArrayList<DLNAMediaSubtitle>();
	public String model;
	public int exposure;
	public int orientation;
	public int iso;
	public String muxingMode;
	public String muxingModeAudio;
	public String container;
	public byte h264_annexB[];

	// Not stored in database
	public boolean mediaparsed;
	public boolean thumbready; // isMediaParserV2 related, used to manage thumbnail management separated from the main parsing process
	public int dvdtrack;
	public boolean secondaryFormatValid = true;
	public boolean parsing = false;
	private boolean ffmpeg_failure;
	private boolean ffmpeg_annexb_failure;
	private boolean muxable;
	private Map<String, String> extras;
	public boolean encrypted;

	public boolean isMuxable(RendererConfiguration mediaRenderer) {
		// temporary fix, MediaInfo support will take care of that in the future

		// for now, http://ps3mediaserver.org/forum/viewtopic.php?f=11&t=6361&start=0
		if (mediaRenderer.isBRAVIA() && codecV != null && codecV.startsWith("mpeg2")) {
			muxable = true;
		}
		if (mediaRenderer.isBRAVIA() && height < 288) // not supported for these small heights
		{
			muxable = false;
		}

		return muxable;
	}

	public Map<String, String> getExtras() {
		return extras;
	}

	public void putExtra(String key, String value) {
		if (extras == null) {
			extras = new HashMap<String, String>();
		}
		extras.put(key, value);
	}

	public String getExtrasAsString() {
		if (extras == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : extras.entrySet()) {
			sb.append(entry.getKey());
			sb.append("|");
			sb.append(entry.getValue());
			sb.append("|");
		}
		return sb.toString();
	}

	public void setExtrasAsString(String value) {
		if (value != null) {
			StringTokenizer st = new StringTokenizer(value, "|");
			while (st.hasMoreTokens()) {
				try {
					putExtra(st.nextToken(), st.nextToken());
				} catch (NoSuchElementException nsee) {
				}
			}
		}
	}

	public DLNAMediaInfo() {
		thumbready = true; // this class manages thumb by default with the parser_v1 method
	}

	public void generateThumbnail(InputFile input, Format ext, int type) {
		DLNAMediaInfo forThumbnail = new DLNAMediaInfo();
		forThumbnail.duration = duration;
		forThumbnail.parse(input, ext, type, true);
		thumb = forThumbnail.thumb;
	}

	private ProcessWrapperImpl getFFMpegThumbnail(InputFile media) {
		String args[] = new String[14];
		args[0] = getFfmpegPath();
		boolean dvrms = media.file != null && media.file.getAbsolutePath().toLowerCase().endsWith("dvr-ms");
		if (dvrms && StringUtils.isNotBlank(PMS.getConfiguration().getFfmpegAlternativePath())) {
			args[0] = PMS.getConfiguration().getFfmpegAlternativePath();
		}
		args[1] = "-ss";
		args[2] = "" + PMS.getConfiguration().getThumbnailSeekPos();
		args[3] = "-i";
		if (media.file != null) {
			args[4] = ProcessUtil.getShortFileNameIfWideChars(media.file.getAbsolutePath());
		} else {
			args[4] = "-";
		}
		args[5] = "-an";
		args[6] = "-an";
		args[7] = "-s";
		args[8] = "320x180";
		args[9] = "-vframes";
		args[10] = "1";
		args[11] = "-f";
		args[12] = "image2";
		args[13] = "pipe:";
		if (!PMS.getConfiguration().getThumbnailsEnabled() || (PMS.getConfiguration().isUseMplayerForVideoThumbs() && !dvrms)) {
			args[2] = "0";
			for (int i = 5; i <= 13; i++) {
				args[i] = "-an";
			}
		}
		OutputParams params = new OutputParams(PMS.getConfiguration());
		params.maxBufferSize = 1;
		params.stdin = media.push;
		params.noexitcheck = true; // not serious if anything happens during the thumbnailer
		// true: consume stderr on behalf of the caller i.e. parse()
		final ProcessWrapperImpl pw = new ProcessWrapperImpl(args, params, false, true);
		// FAILSAFE
		parsing = true;
		Runnable r = new Runnable() {

			public void run() {
				try {
					Thread.sleep(10000);
					ffmpeg_failure = true;
				} catch (InterruptedException e) {
				}
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

	private ProcessWrapperImpl getMplayerThumbnail(InputFile media) throws IOException {
		String args[] = new String[14];
		args[0] = PMS.getConfiguration().getMplayerPath();
		args[1] = "-ss";
		boolean toolong = getDurationInSeconds() < PMS.getConfiguration().getThumbnailSeekPos();
		args[2] = "" + (toolong ? (getDurationInSeconds() / 2) : PMS.getConfiguration().getThumbnailSeekPos());
		args[3] = "-quiet";
		if (media.file != null) {
			args[4] = ProcessUtil.getShortFileNameIfWideChars(media.file.getAbsolutePath());
		} else {
			args[4] = "-";
		}
		args[5] = "-msglevel";
		args[6] = "all=4";
		args[7] = "-vf";
		args[8] = "scale=320:-2,expand=:180";
		args[9] = "-frames";
		args[10] = "1";
		args[11] = "-vo";
		String frameName = "" + media.hashCode();
		frameName = "mplayer_thumbs:subdirs=\"" + frameName + "\"";
		frameName = frameName.replace(',', '_');
		args[12] = "jpeg:outdir=" + frameName;
		args[13] = "-nosound";
		OutputParams params = new OutputParams(PMS.getConfiguration());
		params.workDir = PMS.getConfiguration().getTempFolder();
		params.maxBufferSize = 1;
		params.stdin = media.push;
		params.log = true;
		params.noexitcheck = true; // not serious if anything happens during the thumbnailer
		final ProcessWrapperImpl pw = new ProcessWrapperImpl(args, params);
		// FAILSAFE
		parsing = true;
		Runnable r = new Runnable() {

			public void run() {
				try {
					Thread.sleep(3000);
					//mplayer_thumb_failure = true;
				} catch (InterruptedException e) {
				}
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
			logger.info("No ffmpeg - unable to thumbnail");
			throw new RuntimeException("No ffmpeg - unable to thumbnail");
		} else {
			return value;
		}
	}

	public void parse(InputFile f, Format ext, int type, boolean thumbOnly) {
		int i = 0;
		while (parsing) {
			if (i == 5) {
				mediaparsed = true;
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			i++;
		}
		if (mediaparsed) {
			return;
		}


		if (f != null) {

			if (f.file != null) {
				size = f.file.length();
			} else {
				size = f.size;
			}
			ProcessWrapperImpl pw = null;
			boolean ffmpeg_parsing = true;
			if (type == Format.AUDIO || ext instanceof AudioAsVideo) {
				ffmpeg_parsing = false;
				DLNAMediaAudio audio = new DLNAMediaAudio();
				if (f.file != null) {
					try {
						AudioFile af = AudioFileIO.read(f.file);
						AudioHeader ah = af.getAudioHeader();
						if (ah != null && !thumbOnly) {
							int length = ah.getTrackLength();
							int rate = ah.getSampleRateAsNumber();
							if (ah.getEncodingType().toLowerCase().contains("flac 24")) {
								audio.bitsperSample = 24;
							}
							audio.sampleFrequency = "" + rate;
							setDurationString(length);
							bitrate = (int) ah.getBitRateAsNumber();
							audio.nrAudioChannels = 2;
							if (ah.getChannels() != null && ah.getChannels().toLowerCase().contains("mono")) {
								audio.nrAudioChannels = 1;
							} else if (ah.getChannels() != null && ah.getChannels().toLowerCase().contains("stereo")) {
								audio.nrAudioChannels = 2;
							} else if (ah.getChannels() != null) {
								audio.nrAudioChannels = Integer.parseInt(ah.getChannels());
							}
							audio.codecA = ah.getEncodingType().toLowerCase();
							if (audio.codecA.contains("(windows media")) {
								audio.codecA = audio.codecA.substring(0, audio.codecA.indexOf("(windows media")).trim();
							}
						}
						Tag t = af.getTag();
						if (t != null) {
							if (t.getArtworkList().size() > 0) {
								thumb = t.getArtworkList().get(0).getBinaryData();
							} else {
								if (PMS.getConfiguration().getAudioThumbnailMethod() > 0) {
									thumb = CoverUtil.get().getThumbnailFromArtistAlbum(PMS.getConfiguration().getAudioThumbnailMethod() == 1 ? CoverUtil.AUDIO_AMAZON : CoverUtil.AUDIO_DISCOGS, audio.artist, audio.album);
								}
							}
							if (!thumbOnly) {
								audio.album = t.getFirst(FieldKey.ALBUM);
								audio.artist = t.getFirst(FieldKey.ARTIST);
								audio.songname = t.getFirst(FieldKey.TITLE);
								String y = t.getFirst(FieldKey.YEAR);
								try {
									if (y.length() > 4) {
										y = y.substring(0, 4);
									}
									audio.year = Integer.parseInt(((y != null && y.length() > 0) ? y : "0"));
									y = t.getFirst(FieldKey.TRACK);
									audio.track = Integer.parseInt(((y != null && y.length() > 0) ? y : "1"));
									audio.genre = t.getFirst(FieldKey.GENRE);
								} catch (Throwable e) {
									logger.debug("error in parsing unimportant metadata: " + e.getMessage());
								}
							}
						}
					} catch (Throwable e) {
						logger.debug("Error in parsing audio file: " + e.getMessage() + " - " + (e.getCause() != null ? e.getCause().getMessage() : ""));
						ffmpeg_parsing = false;
					}
					if (audio.songname == null || audio.songname.length() == 0) {
						audio.songname = f.file.getName();
					}
					if (!ffmpeg_parsing) {
						audioCodes.add(audio);
					}
				}
			}
			if (type == Format.IMAGE) {
				if (f.file != null) {
					try {
						ffmpeg_parsing = false;
						ImageInfo info = Sanselan.getImageInfo(f.file);
						width = info.getWidth();
						height = info.getHeight();
						bitsPerPixel = info.getBitsPerPixel();
						String formatName = info.getFormatName();
						if (formatName.startsWith("JPEG")) {
							codecV = "jpg";
							IImageMetadata meta = Sanselan.getMetadata(f.file);
							if (meta != null && meta instanceof JpegImageMetadata) {
								JpegImageMetadata jpegmeta = (JpegImageMetadata) meta;
								TiffField tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_MODEL);
								if (tf != null) {
									model = tf.getStringValue().trim();
								}

								tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_EXPOSURE_TIME);
								if (tf != null) {
									exposure = (int) (1000 * tf.getDoubleValue());
								}

								tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_ORIENTATION);
								if (tf != null) {
									orientation = tf.getIntValue();
								}

								tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_ISO);
								if (tf != null) {
									iso = tf.getIntValue();
								}
							}
						} else if (formatName.startsWith("PNG")) {
							codecV = "png";
						} else if (formatName.startsWith("GIF")) {
							codecV = "gif";
						} else if (formatName.startsWith("TIF")) {
							codecV = "tiff";
						}
						container = codecV;
					} catch (Throwable e) {
						// ffmpeg_parsing = true;
						logger.info("Error during the parsing of image with Sanselan... switching to Ffmpeg: " + e.getMessage());
					}
				}
			}
			if (ffmpeg_parsing) {
				if (!thumbOnly || !PMS.getConfiguration().isUseMplayerForVideoThumbs()) {
					pw = getFFMpegThumbnail(f);
				}
				String input = "-";
				boolean dvrms = false;
				if (f.file != null) {
					input = ProcessUtil.getShortFileNameIfWideChars(f.file.getAbsolutePath());
					dvrms = f.file.getAbsolutePath().toLowerCase().endsWith("dvr-ms");
				}
				if (!ffmpeg_failure && !thumbOnly) {

					if (input.equals("-")) {
						input = "pipe:";
					}
					boolean matchs = false;
					ArrayList<String> lines = (ArrayList<String>) pw.getResults();
					int langId = 0;
					int subId = 0;
					for (String line : lines) {
						line = line.trim();
						if (line.startsWith("Output")) {
							matchs = false;
						} else if (line.startsWith("Input")) {
							if (line.indexOf(input) > -1) {
								matchs = true;
								container = line.substring(10, line.indexOf(",", 11)).trim();
							} else {
								matchs = false;
							}
						} else if (matchs) {
							if (line.indexOf("Duration") > -1) {
								StringTokenizer st = new StringTokenizer(line, ",");
								while (st.hasMoreTokens()) {
									String token = st.nextToken().trim();
									if (token.startsWith("Duration: ")) {
										duration = token.substring(10);
										int l = duration.substring(duration.indexOf(".") + 1).length();
										if (l < 4) {
											duration = duration + "00".substring(0, 3 - l);
										}
										if (duration.indexOf("N/A") > -1) {
											duration = null;
										}
									} else if (token.startsWith("bitrate: ")) {
										String bitr = token.substring(9);
										int spacepos = bitr.indexOf(" ");
										if (spacepos > -1) {
											String value = bitr.substring(0, spacepos);
											String unit = bitr.substring(spacepos + 1);
											bitrate = Integer.parseInt(value);
											if (unit.equals("kb/s")) {
												bitrate = 1024 * bitrate;
											}
											if (unit.equals("mb/s")) {
												bitrate = 1048576 * bitrate;
											}
										}
									}
								}
							} else if (line.indexOf("Audio:") > -1) {
								StringTokenizer st = new StringTokenizer(line, ",");
								int a = line.indexOf("(");
								int b = line.indexOf("):", a);
								DLNAMediaAudio audio = new DLNAMediaAudio();
								if (langId == 0 && (container.equals("avi") || container.equals("ogm") || container.equals("mov") || container.equals("flv"))) {
									langId++;
								}
								audio.id = langId++;
								if (a > -1 && b > a) {
									audio.lang = line.substring(a + 1, b);
								} else {
									audio.lang = DLNAMediaLang.UND;
								}
								// Get TS IDs
								a = line.indexOf("[0x");
								b = line.indexOf("]", a);
								if (a > -1 && b > a + 3) {
									String idString = line.substring(a + 3, b);
									try {
										audio.id = Integer.parseInt(idString, 16);
									} catch (NumberFormatException nfe) {
										logger.debug("Error in parsing Stream ID: " + idString);
									}
								}
								audioCodes.add(audio);
								while (st.hasMoreTokens()) {
									String token = st.nextToken().trim();
									if (token.startsWith("Stream")) {
										audio.codecA = token.substring(token.indexOf("Audio: ") + 7);

									} else if (token.endsWith("Hz")) {
										audio.sampleFrequency = token.substring(0, token.indexOf("Hz")).trim();
									} else if (token.equals("mono")) {
										audio.nrAudioChannels = 1;
									} else if (token.equals("stereo")) {
										audio.nrAudioChannels = 2;
									} else if (token.equals("5:1") || token.equals("5.1") || token.equals("6 channels")) {
										audio.nrAudioChannels = 6;
									} else if (token.equals("5 channels")) {
										audio.nrAudioChannels = 5;
									} else if (token.equals("4 channels")) {
										audio.nrAudioChannels = 4;
									} else if (token.equals("2 channels")) {
										audio.nrAudioChannels = 2;
									} else if (token.equals("s32")) {
										audio.bitsperSample = 32;
									} else if (token.equals("s24")) {
										audio.bitsperSample = 24;
									} else if (token.equals("s16")) {
										audio.bitsperSample = 16;
									}
								}
							} else if (line.indexOf("Video:") > -1) {
								StringTokenizer st = new StringTokenizer(line, ",");
								while (st.hasMoreTokens()) {
									String token = st.nextToken().trim();
									if (token.startsWith("Stream")) {
										codecV = token.substring(token.indexOf("Video: ") + 7);
									} else if ((token.indexOf("tbc") > -1 || token.indexOf("tb(c)") > -1)) {
										// A/V sync issues with newest FFmpeg, due to the new tbr/tbn/tbc outputs
										// Priority to tb(c)
										String frameRateDoubleString = token.substring(0, token.indexOf("tb")).trim();
										try {
											if (!frameRateDoubleString.equals(frameRate)) {// tbc taken into account only if different than tbr
												Double frameRateDouble = Double.parseDouble(frameRateDoubleString);
												frameRate = String.format(Locale.ENGLISH, "%.2f", frameRateDouble / 2);
											}
										} catch (NumberFormatException nfe) {
											// No need to log, could happen if tbc is "1k" or something like that, no big deal
										}

									} else if ((token.indexOf("tbr") > -1 || token.indexOf("tb(r)") > -1) && frameRate == null) {
										frameRate = token.substring(0, token.indexOf("tb")).trim();
									} else if ((token.indexOf("fps") > -1 || token.indexOf("fps(r)") > -1) && frameRate == null) { // dvr-ms ?
										frameRate = token.substring(0, token.indexOf("fps")).trim();
									} else if (token.indexOf("x") > -1) {
										String resolution = token.trim();
										if (resolution.indexOf(" [") > -1) {
											resolution = resolution.substring(0, resolution.indexOf(" ["));
										}
										try {
											width = Integer.parseInt(resolution.substring(0, resolution.indexOf("x")));
											height = Integer.parseInt(resolution.substring(resolution.indexOf("x") + 1));
										} catch (NumberFormatException nfe) {
										}
									}
								}
							} else if (line.indexOf("Subtitle:") > -1 && !line.contains("tx3g")) {
								DLNAMediaSubtitle lang = new DLNAMediaSubtitle();
								lang.type = (line.contains("dvdsub") && Platform.isWindows() ? DLNAMediaSubtitle.VOBSUB : DLNAMediaSubtitle.EMBEDDED);
								int a = line.indexOf("(");
								int b = line.indexOf("):", a);
								if (a > -1 && b > a) {
									lang.lang = line.substring(a + 1, b);
								} else {
									lang.lang = DLNAMediaLang.UND;
								}
								lang.id = subId++;
								subtitlesCodes.add(lang);
							}
						}
					}
				}

				if (!thumbOnly && container != null && f.file != null && container.equals("mpegts") && isH264() && getDurationInSeconds() == 0) {
					// let's do the parsing for getting the duration...
					try {
						int length = MpegUtil.getDurationFromMpeg(f.file);
						if (length > 0) {
							setDurationString(length);
						}
					} catch (IOException e) {
						logger.trace("Error in retrieving length: " + e.getMessage());
					}
				}

				if (PMS.getConfiguration().isUseMplayerForVideoThumbs() && type == Format.VIDEO && !dvrms) {
					try {
						getMplayerThumbnail(f);
						String frameName = "" + f.hashCode();
						frameName = PMS.getConfiguration().getTempFolder() + "/mplayer_thumbs/" + frameName + "00000001/00000001.jpg";
						frameName = frameName.replace(',', '_');
						File jpg = new File(frameName);
						if (jpg.exists()) {
							InputStream is = new FileInputStream(jpg);
							int sz = is.available();
							if (sz > 0) {
								thumb = new byte[sz];
								is.read(thumb);
							}
							is.close();
							if (!jpg.delete()) {
								jpg.deleteOnExit();
							}
							if (!jpg.getParentFile().delete()) {
								jpg.getParentFile().delete();
							}
						}
					} catch (IOException e) {
					}
				}

				if (type == Format.VIDEO && pw != null && thumb == null) {
					InputStream is;
					try {
						is = pw.getInputStream(0);
						int sz = is.available();
						if (sz > 0) {
							thumb = new byte[sz];
							is.read(thumb);
						}
						is.close();

						if (sz > 0 && !java.awt.GraphicsEnvironment.isHeadless()) {
							BufferedImage image = ImageIO.read(new ByteArrayInputStream(thumb));
							if (image != null) {
								Graphics g = image.getGraphics();
								g.setColor(Color.WHITE);
								g.setFont(new Font("Arial", Font.PLAIN, 14));
								int low = 0;
								if (width > 0) {
									if (width == 1920 || width == 1440) {
										g.drawString("1080p", 0, low += 18);
									} else if (width == 1280) {
										g.drawString("720p", 0, low += 18);
									}
								}
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								ImageIO.write(image, "jpeg", out);
								thumb = out.toByteArray();
							}
						}
					} catch (IOException e) {
						logger.debug("Error while decoding thumbnail : " + e.getMessage());
					}
				}
			}
			finalize(type, f);
			mediaparsed = true;
		}
	}

	public boolean isH264() {
		return codecV != null && codecV.contains("264");
	}

	public int getFrameNumbers() {
		double fr = Double.parseDouble(frameRate);
		return (int) (getDurationInSeconds() * fr);
	}

	public void setDurationString(double d) {
		duration = getDurationString(d);
	}

	public static String getDurationString(double d) {
		int s = ((int) d) % 60;
		int h = (int) (d / 3600);
		int m = ((int) (d / 60)) % 60;
		return String.format("%02d:%02d:%02d.00", h, m, s);
	}

	public double getDurationInSeconds() {
		if (StringUtils.isNotBlank(duration)) {
			StringTokenizer st = new StringTokenizer(duration, ":");
			try {
				int h = Integer.parseInt(st.nextToken());
				int m = Integer.parseInt(st.nextToken());
				double s = Double.parseDouble(st.nextToken());
				return h * 3600 + m * 60 + s;
			} catch (NumberFormatException nfe) {
			}
		}
		return 0;
	}

	public void finalize(int type, InputFile f) {
		String codecA = null;
		if (getFirstAudioTrack() != null) {
			codecA = getFirstAudioTrack().codecA;
		}
		if (container != null && container.equals("avi")) {
			mimeType = HTTPResource.AVI_TYPEMIME;
		} else if (container != null && (container.equals("asf") || container.equals("wmv"))) {
			mimeType = HTTPResource.WMV_TYPEMIME;
		} else if (container != null && (container.equals("matroska") || container.equals("mkv"))) {
			mimeType = HTTPResource.MATROSKA_TYPEMIME;
		} else if (codecV != null && codecV.equals("mjpeg")) {
			mimeType = HTTPResource.JPEG_TYPEMIME;
		} else if ("png".equals(codecV) || "png".equals(container)) {
			mimeType = HTTPResource.PNG_TYPEMIME;
		} else if ("gif".equals(codecV) || "gif".equals(container)) {
			mimeType = HTTPResource.GIF_TYPEMIME;
		} else if (codecV != null && (codecV.equals("h264") || codecV.equals("h263") || codecV.toLowerCase().equals("mpeg4") || codecV.toLowerCase().equals("mp4"))) {
			mimeType = HTTPResource.MP4_TYPEMIME;
		} else if (codecV != null && (codecV.indexOf("mpeg") > -1 || codecV.indexOf("mpg") > -1)) {
			mimeType = HTTPResource.MPEG_TYPEMIME;
		} else if (codecV == null && codecA != null && codecA.contains("mp3")) {
			mimeType = HTTPResource.AUDIO_MP3_TYPEMIME;
		} else if (codecV == null && codecA != null && codecA.contains("aac")) {
			mimeType = HTTPResource.AUDIO_MP4_TYPEMIME;
		} else if (codecV == null && codecA != null && codecA.contains("flac")) {
			mimeType = HTTPResource.AUDIO_FLAC_TYPEMIME;
		} else if (codecV == null && codecA != null && codecA.contains("vorbis")) {
			mimeType = HTTPResource.AUDIO_OGG_TYPEMIME;
		} else if (codecV == null && codecA != null && (codecA.contains("asf") || codecA.startsWith("wm"))) {
			mimeType = HTTPResource.AUDIO_WMA_TYPEMIME;
		} else if (codecV == null && codecA != null && (codecA.startsWith("pcm") || codecA.contains("wav"))) {
			mimeType = HTTPResource.AUDIO_WAV_TYPEMIME;
		} else {
			mimeType = new HTTPResource().getDefaultMimeType(type);
		}

		if (getFirstAudioTrack() == null || !(type == Format.AUDIO && getFirstAudioTrack().bitsperSample == 24 && getFirstAudioTrack().getSampleRate() > 48000)) {
			secondaryFormatValid = false;
		}

		// Check for external subs here
		if (f.file != null && type == Format.VIDEO && PMS.getConfiguration().getUseSubtitles()) {
			FileUtil.doesSubtitlesExists(f.file, this);
		}
	}
	private boolean h264_parsed;

	public boolean isVideoPS3Compatible(InputFile f) {
		if (!h264_parsed) {
			if (codecV != null && (codecV.equals("h264") || codecV.startsWith("mpeg2"))) { // what about VC1 ?
				muxable = true;
				if (codecV.equals("h264") && container != null && (container.equals("matroska") || container.equals("mkv") || container.equals("mov") || container.equals("mp4"))) { // containers without h264_annexB
					byte headers[][] = getAnnexBFrameHeader(f);
					if (ffmpeg_annexb_failure) {
						logger.info("Fatal error when retrieving AVC informations !");
					}
					if (headers != null) {
						h264_annexB = headers[1];
						if (h264_annexB != null) {
							int skip = 5;
							if (h264_annexB[2] == 1) {
								skip = 4;
							}
							byte header[] = new byte[h264_annexB.length - skip];
							System.arraycopy(h264_annexB, skip, header, 0, header.length);
							AVCHeader avcHeader = new AVCHeader(header);
							avcHeader.parse();
							logger.trace("H264 file: " + f.filename + ": Profile: " + avcHeader.getProfile() + " / level: " + avcHeader.getLevel() + " / ref frames: " + avcHeader.getRef_frames());
							muxable = true;
							if (avcHeader.getLevel() >= 41) { // Check if file is compliant with Level4.1
								if (width > 0 && height > 0) {
									int maxref = (int) Math.floor(8388608 / (width * height));
									if (avcHeader.getRef_frames() > maxref) {
										muxable = false;
									}
								}
							}
							if (!muxable) {
								logger.debug("H264 file: " + f.filename + " is not ps3 compatible !");
							}
						} else {
							muxable = false;
						}
					} else {
						muxable = false;
					}
				}
			}
			h264_parsed = true;
		}
		return muxable;
	}

	public boolean isMuxable(String filename, String codecA) {
		return codecA != null && (codecA.startsWith("dts") || codecA.equals("dca"));
	}

	public boolean isLossless(String codecA) {
		return codecA != null && (codecA.contains("pcm") || codecA.startsWith("dts") || codecA.equals("dca") || codecA.contains("flac")) && !codecA.contains("pcm_u8") && !codecA.contains("pcm_s8");
	}

	public String toString() {
		String s = "container: " + container + " / bitrate: " + bitrate + " / size: " + size + " / codecV: " + codecV + " / duration: " + duration + " / width: " + width + " / height: " + height + " / frameRate: " + frameRate + " / thumb size : " + (thumb != null ? thumb.length : 0) + " / muxingMode: " + muxingMode;
		for (DLNAMediaAudio audio : audioCodes) {
			s += "\n\taudio: id=" + audio.id + " / lang: " + audio.lang + " / codec: " + audio.codecA + " / sf:" + audio.sampleFrequency + " / na: " + audio.nrAudioChannels + " / bs: " + audio.bitsperSample;
			if (audio.artist != null) {
				s += " / " + audio.artist + "|" + audio.album + "|" + audio.songname + "|" + audio.year + "|" + audio.track;
			}
		}
		for (DLNAMediaSubtitle sub : subtitlesCodes) {
			s += "\n\tsub: id=" + sub.id + " / lang: " + sub.lang + " / type: " + sub.type;
		}
		return s;
	}

	public InputStream getThumbnailInputStream() {
		return new ByteArrayInputStream(thumb);
	}

	public String getValidFps(boolean ratios) {
		String validFrameRate = null;
		if (frameRate != null && frameRate.length() > 0) {
			try {
				double fr = Double.parseDouble(frameRate);
				if (fr > 23.9 && fr < 23.99) {
					validFrameRate = ratios ? "24000/1001" : "23.976";
				} else if (fr > 23.99 && fr < 24.1) {
					validFrameRate = "24";
				} else if (fr >= 24.99 && fr < 25.1) {
					validFrameRate = "25";
				} else if (fr > 29.9 && fr < 29.99) {
					validFrameRate = ratios ? "30000/1001" : "29.97";
				} else if (fr >= 29.99 && fr < 30.1) {
					validFrameRate = "30";
				} else if (fr > 47.9 && fr < 47.99) {
					validFrameRate = ratios ? "48000/1001" : "47.952";
				} else if (fr > 49.9 && fr < 50.1) {
					validFrameRate = "50";
				} else if (fr > 59.9 && fr < 59.99) {
					validFrameRate = ratios ? "60000/1001" : "59.94";
				} else if (fr >= 59.99 && fr < 60.1) {
					validFrameRate = "60";
				}
			} catch (NumberFormatException nfe) {
				logger.error(null, nfe);
			}

		}
		return validFrameRate;
	}

	public DLNAMediaAudio getFirstAudioTrack() {
		if (audioCodes.size() > 0) {
			return audioCodes.get(0);
		}
		return null;
	}

	public String getValidAspect(boolean ratios) {
		String a = null;
		if (aspect != null) {
			double ar = Double.parseDouble(aspect);
			if (ar > 1.7 && ar < 1.8) {
				a = ratios ? "16/9" : "1.777777777777777";
			}
			if (ar > 1.3 && ar < 1.4) {
				a = ratios ? "4/3" : "1.333333333333333";
			}
		}
		return a;
	}

	public String getResolution() {
		if (width > 0 && height > 0) {
			return width + "x" + height;
		}
		return null;
	}

	public int getRealVideoBitrate() {
		if (bitrate > 0) {
			return (int) (bitrate / 8);
		}
		int realBitrate = 10000000;
		try {
			realBitrate = (int) (size / getDurationInSeconds());
		} catch (Throwable t) {
		}
		return realBitrate;
	}

	public boolean isHDVideo() {
		return (width > 1200 || height > 700);
	}

	public boolean isMpegTS() {
		return container != null && container.equals("mpegts");
	}

	public byte[][] getAnnexBFrameHeader(InputFile f) {
		String cmdArray[] = new String[14];
		cmdArray[0] = PMS.getConfiguration().getFfmpegPath();
		cmdArray[1] = "-vframes";
		cmdArray[2] = "1";
		cmdArray[3] = "-i";
		if (f.push == null && f.filename != null) {
			cmdArray[4] = f.filename;
		} else {
			cmdArray[4] = "-";
		}
		cmdArray[5] = "-vcodec";
		cmdArray[6] = "copy";
		cmdArray[7] = "-f";
		cmdArray[8] = "h264";
		cmdArray[9] = "-vbsf";
		cmdArray[10] = "h264_mp4toannexb";
		cmdArray[11] = "-an";
		cmdArray[12] = "-y";
		cmdArray[13] = "pipe:";

		byte returnData[][] = new byte[2][];

		OutputParams params = new OutputParams(PMS.getConfiguration());
		params.maxBufferSize = 1;
		params.stdin = f.push;

		final ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);

		Runnable r = new Runnable() {

			public void run() {
				try {
					Thread.sleep(3000);
					ffmpeg_annexb_failure = true;
				} catch (InterruptedException e) {
				}
				pw.stopProcess();
			}
		};
		Thread failsafe = new Thread(r);
		failsafe.start();
		pw.run();
		if (ffmpeg_annexb_failure) {
			return null;
		}

		InputStream is = null;
		ByteArrayOutputStream baot = new ByteArrayOutputStream();
		try {
			is = pw.getInputStream(0);
			byte b[] = new byte[4096];
			int n = -1;
			while ((n = is.read(b)) > 0) {
				baot.write(b, 0, n);
			}
			byte data[] = baot.toByteArray();
			baot.close();

			returnData[0] = data;

			is.close();

			int kf = 0;
			for (int i = 3; i < data.length; i++) {
				if (data[i - 3] == 1 && (data[i - 2] & 37) == 37 && (data[i - 1] & -120) == -120) {
					kf = i - 2;
					break;
				}
			}

			int st = 0;
			boolean found = false;
			if (kf > 0) {
				for (int i = kf; i >= 5; i--) {
					if (data[i - 5] == 0 && data[i - 4] == 0 && data[i - 3] == 0 && (data[i - 2] & 1) == 1 && (data[i - 1] & 39) == 39) {
						st = i - 5;
						found = true;
						break;
					}
				}
			}

			if (found) {
				byte header[] = new byte[kf - st];
				System.arraycopy(data, st, header, 0, kf - st);
				returnData[1] = header;
			}
		} catch (IOException e) {
		}
		return returnData;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Object cloned = super.clone();
		if (cloned instanceof DLNAMediaInfo) {
			DLNAMediaInfo mediaCloned = ((DLNAMediaInfo) cloned);
			mediaCloned.audioCodes = new ArrayList<DLNAMediaAudio>();
			for (DLNAMediaAudio audio : audioCodes) {
				mediaCloned.audioCodes.add((DLNAMediaAudio) audio.clone());
			}
			mediaCloned.subtitlesCodes = new ArrayList<DLNAMediaSubtitle>();
			for (DLNAMediaSubtitle sub : subtitlesCodes) {
				mediaCloned.subtitlesCodes.add((DLNAMediaSubtitle) sub.clone());
			}
		}

		return cloned;
	}
}
