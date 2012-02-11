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
import static org.junit.Assert.assertNull;
import net.pms.formats.Format;
import net.pms.formats.FormatFactory;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * Test basic functionality of {@link Format}.
 */
public class FormatFactoryTest {
	/**
	 * Set up testing conditions before running the tests.
	 */
	@Before
	public final void setUp() {
		// Silence all log messages from the PMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.reset();
	}

	/**
	 * Test edge cases for {@link FormatFactory#getAssociatedExtension(String)}.
	 */
	@Test
	public final void testFormatFactoryEdgeCases() {
		// Null string
		Format result = FormatFactory.getAssociatedExtension(null);
		assertNull("Matched format " + result + " for null string", result);

		// Empty string
		result = FormatFactory.getAssociatedExtension("");
		assertNull("Matched extension for empty string", result);

		// Non-existent format
		result = FormatFactory
				.getAssociatedExtension("qwerty://test.org/test.qwerty");
		assertNull(
				"Matched extension for \"qwerty://test.org/test.qwerty\" string",
				result);

		// Combination of MPG and WEB: which will win?
		testSingleFormat("http://test.org/test.mpg", "MPG");
	}

	/**
	 * Test whether {@link FormatFactory#getAssociatedExtension(String)} manages
	 * to retrieve the correct format.
	 */
	@Test
	public final void testFormatRetrieval() {
		testSingleFormat("test.dvr", "DVRMS");
		testSingleFormat("test.flac", "FLAC");
		testSingleFormat("test.gif", "GIF");
		testSingleFormat("test.iso", "ISO");
		testSingleFormat("test.jpg", "JPG");
		testSingleFormat("test.wma", "M4A");
		testSingleFormat("test.mkv", "MKV");
		testSingleFormat("test.mp3", "MP3");
		testSingleFormat("test.mpg", "MPG");
		testSingleFormat("test.ogg", "OGG");
		testSingleFormat("test.png", "PNG");
		testSingleFormat("test.arw", "RAW");
		testSingleFormat("test.tiff", "TIF");
		testSingleFormat("test.wav", "WAV");
		testSingleFormat("http://test.org/", "WEB");
	}

	/**
	 * Verify if a filename is recognized as a given format. Use
	 * <code>null</code> as formatName when no match is expected.
	 * 
	 * @param filename
	 *            The filename to verify.
	 * @param formatName
	 *            The name of the expected format.
	 */
	private void testSingleFormat(final String filename, final String formatName) {
		Format result = FormatFactory.getAssociatedExtension(filename);

		if (result != null) {
			assertEquals("\"" + filename + "\" is expected to match "
					+ formatName + ", but matches " + result + " instead.",
					formatName, result.toString());
		} else {
			assertNull("\"" + filename + "\" matches nothing, but "
					+ formatName + " was expected", formatName);
		}
	}
}
