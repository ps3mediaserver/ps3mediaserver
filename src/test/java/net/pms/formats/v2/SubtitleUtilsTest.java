/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012-2013  I. Sokolov
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

import ch.qos.logback.classic.LoggerContext;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaSubtitle;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static net.pms.formats.v2.SubtitleType.ASS;
import static net.pms.formats.v2.SubtitleType.VOBSUB;
import static net.pms.formats.v2.SubtitleUtils.TimingFormat.*;
import static net.pms.formats.v2.SubtitleUtils.getSubCpOptionForMencoder;
import static org.fest.assertions.Assertions.assertThat;

public class SubtitleUtilsTest {
	private final Class<?> CLASS = SubtitleUtilsTest.class;
	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	/**
	 * Set up testing conditions before running the tests.
	 */
	@Before
	public final void setUp() throws IOException, ConfigurationException {
		// Silence all log messages from the PMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.reset();

		SubtitleUtils.setConfiguration(new PmsConfiguration() {
			@Override
			public File getTempFolder() throws IOException {
				return temporaryFolder.newFolder("subutils");
			}
		});
	}

	@Test(expected = NullPointerException.class)
	public void testGetSubCpOptionForMencoder_withNullDLNAMediaSubtitle() throws Exception {
		getSubCpOptionForMencoder(null);
	}

	@Test
	public void testGetSubCpOptionForMencoder_withoutExternalSubtitles() throws Exception {
		DLNAMediaSubtitle subtitle = new DLNAMediaSubtitle();
		assertThat(getSubCpOptionForMencoder(subtitle)).isNull();
	}

	@Test
	public void testGetSubCpOptionForMencoder_withoutDetectedCharset() throws Exception {
		DLNAMediaSubtitle subtitle = new DLNAMediaSubtitle();
		File file_cp1251 = FileUtils.toFile(CLASS.getResource("../../util/russian-cp1251.srt"));
		subtitle.setType(VOBSUB);
		subtitle.setExternalFile(file_cp1251);
		assertThat(subtitle.getExternalFileCharacterSet()).isNull();
		assertThat(getSubCpOptionForMencoder(subtitle)).isNull();
	}

	@Test
	public void testGetSubCpOptionForMencoder() throws Exception {
		File file_big5 = FileUtils.toFile(CLASS.getResource("../../util/chinese-big5.srt"));
		DLNAMediaSubtitle sub1 = new DLNAMediaSubtitle();
		sub1.setExternalFile(file_big5);
		assertThat(getSubCpOptionForMencoder(sub1)).isEqualTo("enca:zh:big5");

		File file_gb18030 = FileUtils.toFile(CLASS.getResource("../../util/chinese-gb18030.srt"));
		DLNAMediaSubtitle sub2 = new DLNAMediaSubtitle();
		sub2.setExternalFile(file_gb18030);
		assertThat(getSubCpOptionForMencoder(sub2)).isEqualTo("enca:zh:big5");

		File file_cp1251 = FileUtils.toFile(CLASS.getResource("../../util/russian-cp1251.srt"));
		DLNAMediaSubtitle sub3 = new DLNAMediaSubtitle();
		sub3.setExternalFile(file_cp1251);
		assertThat(getSubCpOptionForMencoder(sub3)).isEqualTo("enca:ru:cp1251");

		File file_ibm866 = FileUtils.toFile(CLASS.getResource("../../util/russian-ibm866.srt"));
		DLNAMediaSubtitle sub4 = new DLNAMediaSubtitle();
		sub4.setExternalFile(file_ibm866);
		assertThat(getSubCpOptionForMencoder(sub4)).isEqualTo("enca:ru:cp1251");

		File file_koi8_r = FileUtils.toFile(CLASS.getResource("../../util/russian-koi8-r.srt"));
		DLNAMediaSubtitle sub5 = new DLNAMediaSubtitle();
		sub5.setExternalFile(file_koi8_r);
		assertThat(getSubCpOptionForMencoder(sub5)).isEqualTo("enca:ru:cp1251");
	}

	@Test
	public void testGetSubCpOptionForMencoder_UTF() throws Exception {
		File file_utf8 = FileUtils.toFile(CLASS.getResource("../../util/russian-utf8-without-bom.srt"));
		DLNAMediaSubtitle sub1 = new DLNAMediaSubtitle();
		sub1.setExternalFile(file_utf8);
		assertThat(getSubCpOptionForMencoder(sub1)).isNull();

		File file_utf8_2 = FileUtils.toFile(CLASS.getResource("../../util/russian-utf8-with-bom.srt"));
		DLNAMediaSubtitle sub2 = new DLNAMediaSubtitle();
		sub2.setExternalFile(file_utf8_2);
		assertThat(getSubCpOptionForMencoder(sub2)).isNull();

		File file_utf16_le = FileUtils.toFile(CLASS.getResource("../../util/russian-utf16-le.srt"));
		DLNAMediaSubtitle sub3 = new DLNAMediaSubtitle();
		sub3.setExternalFile(file_utf16_le);
		assertThat(getSubCpOptionForMencoder(sub3)).isNull();

		File file_utf16_be = FileUtils.toFile(CLASS.getResource("../../util/russian-utf16-be.srt"));
		DLNAMediaSubtitle sub4 = new DLNAMediaSubtitle();
		sub4.setExternalFile(file_utf16_be);
		assertThat(getSubCpOptionForMencoder(sub4)).isNull();

		File file_utf32_le = FileUtils.toFile(CLASS.getResource("../../util/russian-utf32-le.srt"));
		DLNAMediaSubtitle sub5 = new DLNAMediaSubtitle();
		sub5.setExternalFile(file_utf32_le);
		assertThat(getSubCpOptionForMencoder(sub5)).isNull();

		File file_utf32_be = FileUtils.toFile(CLASS.getResource("../../util/russian-utf32-be.srt"));
		DLNAMediaSubtitle sub6 = new DLNAMediaSubtitle();
		sub6.setExternalFile(file_utf32_be);
		assertThat(getSubCpOptionForMencoder(sub6)).isNull();

		File file_utf8_3 = FileUtils.toFile(CLASS.getResource("../../util/english-utf8-with-bom.srt"));
		DLNAMediaSubtitle sub7 = new DLNAMediaSubtitle();
		sub7.setExternalFile(file_utf8_3);
		assertThat(getSubCpOptionForMencoder(sub7)).isNull();
	}

	@Test
	public void testConvertTimeToSubtitleTimingString() {
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(5057.056, ASS_TIMING)).isEqualTo("1:24:17.06");
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(-5057.056, ASS_TIMING)).isEqualTo("-1:24:17.06");
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(5057.1, ASS_TIMING)).isEqualTo("1:24:17.10");
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(46814, ASS_TIMING)).isEqualTo("13:00:14.00");
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(-46814, ASS_TIMING)).isEqualTo("-13:00:14.00");
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(5057.056, SRT_TIMING)).isEqualTo("01:24:17,056");
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(-5057.056, SRT_TIMING)).isEqualTo("-01:24:17,056");
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(5057.1, SRT_TIMING)).isEqualTo("01:24:17,100");
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(5057.056, SECONDS_TIMING)).isEqualTo("01:24:17");
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(-5057.056, SECONDS_TIMING)).isEqualTo("-01:24:17");
		assertThat(SubtitleUtils.convertTimeToSubtitleTimingString(5057.956, SECONDS_TIMING)).isEqualTo("01:24:18");
	}

	@Test(expected = NullPointerException.class)
	public void testConvertTimeToSubtitleTimingString_withNullTimingFormat() {
		SubtitleUtils.convertTimeToSubtitleTimingString(5057.056, null);
	}

	@Test
	public void testConvertSubtitleTimingStringToTime() {
		assertThat(SubtitleUtils.convertSubtitleTimingStringToTime("1:24:17.10")).isEqualTo(5057.1);
		assertThat(SubtitleUtils.convertSubtitleTimingStringToTime("1:24:17,10")).isEqualTo(5057.1);
		assertThat(SubtitleUtils.convertSubtitleTimingStringToTime("01:24:17.056")).isEqualTo(5057.056);
		assertThat(SubtitleUtils.convertSubtitleTimingStringToTime("01:24:17,056")).isEqualTo(5057.056);
		assertThat(SubtitleUtils.convertSubtitleTimingStringToTime("-01:24:17.056")).isEqualTo(-5057.056);
		assertThat(SubtitleUtils.convertSubtitleTimingStringToTime("-01:24:17,056")).isEqualTo(-5057.056);
		assertThat(SubtitleUtils.convertSubtitleTimingStringToTime("01:24:17")).isEqualTo(5057);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertSubtitleTimingStringToTime_withNullTimingString() {
		SubtitleUtils.convertSubtitleTimingStringToTime(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertSubtitleTimingStringToTime_withEmptyTimingString() {
		SubtitleUtils.convertSubtitleTimingStringToTime("");
	}

	@Test(expected = NullPointerException.class)
	public void testShiftSubtitlesTimingWithUtfConversion_withNullInputSubtitles() throws IOException {
		SubtitleUtils.shiftSubtitlesTimingWithUtfConversion(null, 12);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testShiftSubtitlesTimingWithUtfConversion_withInputSubtitles_withoutExternalFile() throws IOException {
		SubtitleUtils.shiftSubtitlesTimingWithUtfConversion(new DLNAMediaSubtitle(), 12);
	}

	@Test(expected = FileNotFoundException.class)
	public void testShiftSubtitlesTimingWithUtfConversion_withInputSubtitles_withBlankExternalFileName() throws IOException {
		final DLNAMediaSubtitle inputSubtitles = new DLNAMediaSubtitle();
		inputSubtitles.setExternalFile(new File("no-name-file.test") {
			@Override
			public String getName() {
				return "";
			}
		});
		SubtitleUtils.shiftSubtitlesTimingWithUtfConversion(inputSubtitles, 12);
	}

	@Test(expected = NullPointerException.class)
	public void testShiftSubtitlesTimingWithUtfConversion_withNullSubtitleType() throws IOException {
		final DLNAMediaSubtitle inputSubtitles = new DLNAMediaSubtitle() {
			@Override
			public SubtitleType getType() {
				return null;
			}
		};
		inputSubtitles.setExternalFile(FileUtils.toFile(CLASS.getResource("../../util/russian-utf8-without-bom.srt")));
		SubtitleUtils.shiftSubtitlesTimingWithUtfConversion(inputSubtitles, 12);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testShiftSubtitlesTimingWithUtfConversion_withInvalidSubtitleType() throws IOException {
		final DLNAMediaSubtitle inputSubtitles = new DLNAMediaSubtitle();
		inputSubtitles.setExternalFile(FileUtils.toFile(CLASS.getResource("../../util/russian-utf8-without-bom.srt")));
		SubtitleUtils.shiftSubtitlesTimingWithUtfConversion(inputSubtitles, 12);
	}

	@Test
	public void testShiftSubtitlesTimingWithUtfConversion_charsetConversion_withoutTimeShift() throws IOException {
		final DLNAMediaSubtitle inputSubtitles = new DLNAMediaSubtitle();
		inputSubtitles.setType(ASS);
		inputSubtitles.setExternalFile(FileUtils.toFile(CLASS.getResource("../../util/russian-cp1251.srt")));
		final DLNAMediaSubtitle convertedSubtitles = SubtitleUtils.shiftSubtitlesTimingWithUtfConversion(inputSubtitles, 0);
		assertThat(convertedSubtitles.isExternalFileUtf8()).isTrue();
	}

	@Test
	public void testShiftSubtitlesTimingWithUtfConversion_doNotConvertUtf8_withoutTimeShift() throws IOException {
		final DLNAMediaSubtitle inputSubtitles = new DLNAMediaSubtitle();
		inputSubtitles.setType(ASS);
		inputSubtitles.setExternalFile(FileUtils.toFile(CLASS.getResource("../../util/russian-utf8-without-bom.srt")));
		final DLNAMediaSubtitle convertedSubtitles = SubtitleUtils.shiftSubtitlesTimingWithUtfConversion(inputSubtitles, 0);
		assertThat(convertedSubtitles.getExternalFile()).hasSameContentAs(inputSubtitles.getExternalFile());
	}

	@Test
	public void testIsSupportsTimeShifting() {
		assertThat(SubtitleUtils.isSupportsTimeShifting(null)).isFalse();
		assertThat(SubtitleUtils.isSupportsTimeShifting(SubtitleType.ASS)).isTrue();
		assertThat(SubtitleUtils.isSupportsTimeShifting(SubtitleType.SUBRIP)).isTrue();
		assertThat(SubtitleUtils.isSupportsTimeShifting(SubtitleType.VOBSUB)).isFalse();
	}
}
