/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  I. Sokolov
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
package net.pms.formats.v2;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaSubtitle;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.lang3.StringUtils.*;
import static org.mozilla.universalchardet.Constants.*;

public class SubtitleUtils {
	private final static Logger LOGGER = LoggerFactory.getLogger(SubtitleUtils.class);
	private final static PmsConfiguration configuration = PMS.getConfiguration();
	private final static Map<String, String> fileCharsetToMencoderSubcpOptionMap = new HashMap<String, String>() {
		{
			// Cyrillic / Russian
			put(CHARSET_IBM855, "enca:ru:cp1251");
			put(CHARSET_ISO_8859_5, "enca:ru:cp1251");
			put(CHARSET_KOI8_R, "enca:ru:cp1251");
			put(CHARSET_MACCYRILLIC, "enca:ru:cp1251");
			put(CHARSET_WINDOWS_1251, "enca:ru:cp1251");
			put(CHARSET_IBM866, "enca:ru:cp1251");
			// Greek
			put(CHARSET_WINDOWS_1253, "cp1253");
			put(CHARSET_ISO_8859_7, "ISO-8859-7");
			// Western Europe
			put(CHARSET_WINDOWS_1252, "cp1252");
			// Hebrew
			put(CHARSET_WINDOWS_1255, "cp1255");
			put(CHARSET_ISO_8859_8, "ISO-8859-8");
			// Chinese
			put(CHARSET_ISO_2022_CN, "ISO-2022-CN");
			put(CHARSET_BIG5, "enca:zh:big5");
			put(CHARSET_GB18030, "enca:zh:big5");
			put(CHARSET_EUC_TW, "enca:zh:big5");
			put(CHARSET_HZ_GB_2312, "enca:zh:big5");
			// Korean
			put(CHARSET_ISO_2022_KR, "cp949");
			put(CHARSET_EUC_KR, "euc-kr");
			// Japanese
			put(CHARSET_ISO_2022_JP, "ISO-2022-JP");
			put(CHARSET_EUC_JP, "euc-jp");
			put(CHARSET_SHIFT_JIS, "shift-jis");
		}
	};

	/**
	 * Returns value for -subcp option for non UTF-8 external subtitles based on
	 * detected charset.
	 * @param dlnaMediaSubtitle DLNAMediaSubtitle with external subtitles file.
	 * @return value for mencoder's -subcp option or null if can't determine.
	 */
	public static String getSubCpOptionForMencoder(DLNAMediaSubtitle dlnaMediaSubtitle) {
		if (dlnaMediaSubtitle == null) {
			throw new NullPointerException("dlnaMediaSubtitle can't be null.");
		}
		if (isBlank(dlnaMediaSubtitle.getExternalFileCharacterSet())) {
			return null;
		}
		return fileCharsetToMencoderSubcpOptionMap.get(dlnaMediaSubtitle.getExternalFileCharacterSet());
	}

	/**
	 * Shift timing of subtitles in SSA/ASS or SRT format
	 *
	 * @param inputSubtitles Subtitles file in SSA/ASS or SRT format
	 * @param timeShift  Time stamp value
	 * @return Converted subtitles file
	 * @throws IOException
	 */
	public static File shiftSubtitlesTiming(final File inputSubtitles, double timeShift, SubtitleType subtitleType) throws IOException {
		if (inputSubtitles == null) {
			throw new NullPointerException("inputSubtitles should not be null.");
		}
		if (isBlank(inputSubtitles.getName())) {
			throw new NullPointerException("inputSubtitles should not have blank name.");
		}
		if (subtitleType == null) {
			throw new NullPointerException("subtitleType should not be null.");
		}
		if (!(timeShift > 0)) {
			return inputSubtitles; // time shifting is not needed
		}

		final File convertedSubtitles = new File(configuration.getTempFolder(), getBaseName(inputSubtitles.getName()) + System.currentTimeMillis() + ".tmp");
		FileUtils.forceDeleteOnExit(convertedSubtitles);
		final BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(inputSubtitles)));
		final BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(convertedSubtitles)));
		String line;
		double startTime;
		double endTime;

		try {
			if (SubtitleType.ASS.equals(subtitleType)) {
				while ((line = input.readLine()) != null) {
					if (startsWith(line, "Dialogue:")) {
						String[] timings = splitPreserveAllTokens(line, ",");
						if (timings.length >= 3 && isNotBlank(timings[1]) && isNotBlank(timings[1])) {
							startTime = convertSubtitleTimingStringToTime(timings[1]);
							endTime = convertSubtitleTimingStringToTime(timings[2]);
							if (startTime >= timeShift) {
								timings[1] = convertTimeToSubtitleTimingString(startTime - timeShift, TimingFormat.ASS_TIMING);
								timings[2] = convertTimeToSubtitleTimingString(endTime - timeShift, TimingFormat.ASS_TIMING);
								output.write(join(timings, ",") + "\n");
							} else {
								continue;
							}
						} else {
							output.write(line + "\n");
						}
					} else {
						output.write(line + "\n");
					}
				}
			} else if (SubtitleType.SUBRIP.equals(subtitleType)) {
				int n = 1;
				while ((line = input.readLine()) != null) {
					if (contains(line, ("-->"))) {
						startTime = convertSubtitleTimingStringToTime(line.substring(0, line.indexOf("-->") - 1));
						endTime = convertSubtitleTimingStringToTime(line.substring(line.indexOf("-->") + 4));
						if (startTime >= timeShift) {
							output.write("" + (n++) + "\n");
							output.write(convertTimeToSubtitleTimingString(startTime - timeShift, TimingFormat.SRT_TIMING));
							output.write(" --> ");
							output.write(convertTimeToSubtitleTimingString(endTime - timeShift, TimingFormat.SRT_TIMING) + "\n");

							while (isNotBlank(line = input.readLine())) { // Read all following subs lines
								output.write(line + "\n");
							}
							output.write("" + "\n");
						}
					}
				}
			}
		} finally {
			if (output != null) {
				output.flush();
				output.close();
			}
			if (input != null) {
				input.close();
			}
		}

		return convertedSubtitles;
	}

	enum TimingFormat {
		ASS_TIMING,
		SRT_TIMING,
		SECONDS_TIMING;
	}

	/**
	 * Converts time in seconds to subtitle timing string.
	 *
	 * @param time in seconds
	 * @param timingFormat format of timing string
	 * @return timing string
	 */
	static String convertTimeToSubtitleTimingString(final double time, final TimingFormat timingFormat) {
		if (timingFormat == null) {
			throw new NullPointerException("timingFormat should not be null.");
		}

		double s = Math.abs(time % 60);
		int h = (int) (time / 3600);
		int m = Math.abs(((int) (time / 60)) % 60);
		switch (timingFormat) {
			case ASS_TIMING:
				return trim(String.format(Locale.ENGLISH, "% 02d:%02d:%02.2f", h, m, s));
			case SRT_TIMING:
				return trim(String.format(Locale.ENGLISH, "% 03d:%02d:%02.3f", h, m, s));
			case SECONDS_TIMING:
				return trim(String.format(Locale.ENGLISH, "% 03d:%02d:%02.0f", h, m, s));
			default:
				return trim(String.format(Locale.ENGLISH, "% 03d:%02d:%02.0f", h, m, s));
		}
	}

	/**
	 * Converts subtitle timing string to seconds.
	 *
	 * @param timingString in format OO:00:00.000
	 * @return seconds or null if conversion failed
	 */
	static Double convertSubtitleTimingStringToTime(final String timingString) throws NumberFormatException {
		if (isBlank(timingString)) {
			throw new IllegalArgumentException("timingString should not be blank.");
		}

		final StringTokenizer st = new StringTokenizer(timingString, ":");
		try {
			int h = Integer.parseInt(st.nextToken());
			int m = Integer.parseInt(st.nextToken());
			double s = Double.parseDouble(st.nextToken());
			if (h >= 0) {
				return h * 3600 + m * 60 + s;
			} else {
				return h * 3600 - m * 60 - s;
			}
		} catch (NumberFormatException nfe) {
			LOGGER.debug("Failed to convert timing string \"" + timingString + "\".");
			throw nfe;
		}
	}
}
