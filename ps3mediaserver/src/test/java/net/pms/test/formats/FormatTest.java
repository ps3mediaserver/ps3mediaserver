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

package net.pms.test.formats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.DVRMS;
import net.pms.formats.FLAC;
import net.pms.formats.Format;
import net.pms.formats.GIF;
import net.pms.formats.ISO;
import net.pms.formats.JPG;
import net.pms.formats.M4A;
import net.pms.formats.MKV;
import net.pms.formats.MP3;
import net.pms.formats.MPG;
import net.pms.formats.OGG;
import net.pms.formats.PNG;
import net.pms.formats.RAW;
import net.pms.formats.TIF;
import net.pms.formats.WAV;
import net.pms.formats.WEB;
import net.pms.network.HTTPResource;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;


/**
 * Test basic functionality of {@link Format}.
 */
public class FormatTest {
	@Before
	public void setUp() {
		// Silence all log messages from the PMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset(); 
	}

    /**
     * Test edge cases for {@link Format#match(String)}.
     */
    @Test
	public void testFormatEdgeCases() {
    	// Empty string
		assertEquals("MP3 matches \"\"", false, new MP3().match(""));

    	// Null string
		assertEquals("MP3 matches null", false, new MP3().match(null));

		// Mixed case
		assertEquals("TIFF does not match \"tEsT.TiFf\"", true, new TIF().match("tEsT.TiFf"));

		// Starting with identifier instead of ending
		assertEquals("TIFF matches \"tiff.test\"", false, new TIF().match("tiff.test"));

		// Substring
		assertEquals("TIFF matches \"not.tiff.but.mp3\"", false, new TIF().match("not.tiff.but.mp3"));
    }
    
    /**
     * Test if {@link Format#match(String)} manages to match the identifiers
     * specified in each format with getId().
     */
    @Test
	public void testFormatIdentifiers() {
		// Identifier tests based on the identifiers defined in getId() of each class
		assertEquals("DVRMS does not match \"test.dvr\"", true, new DVRMS().match("test.dvr"));
		assertEquals("FLAC does not match \"test.flac\"", true, new FLAC().match("test.flac"));
		assertEquals("GIF does not match \"test.gif\"", true, new GIF().match("test.gif"));
		assertEquals("ISO does not match \"test.iso\"", true, new ISO().match("test.iso"));
		assertEquals("JPG does not match \"test.jpg\"", true, new JPG().match("test.jpg"));
		assertEquals("M4A does not match \"test.wma\"", true, new M4A().match("test.wma"));
		assertEquals("MKV does not match \"test.mkv\"", true, new MKV().match("test.mkv"));
		assertEquals("MP3 does not match \"test.mp3\"", true, new MP3().match("test.mp3"));
		assertEquals("MPG does not match \"test.mpg\"", true, new MPG().match("test.mpg"));
		assertEquals("OGG does not match \"test.ogg\"", true, new OGG().match("test.ogg"));
		assertEquals("PNG does not match \"test.png\"", true, new PNG().match("test.png"));
		assertEquals("RAW does not match \"test.arw\"", true, new RAW().match("test.arw"));
		assertEquals("TIF does not match \"test.tiff\"", true, new TIF().match("test.tiff"));
		assertEquals("WAV does not match \"test.wav\"", true, new WAV().match("test.wav"));
		assertEquals("WEB does not match \"http\"", true, new WEB().match("http://test.org/"));
	}
}
