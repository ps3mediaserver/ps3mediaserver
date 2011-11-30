/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2011  G.Zsombor
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
package net.pms.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import net.pms.dlna.DLNAMediaInfo;

import org.junit.Test;

public class Issue1278 {

	@Test
	public void dlnaMediaInfoDoubleParseWithDot() {
		DLNAMediaInfo info = new DLNAMediaInfo();
		info.setFrameRate("23.976");
		String validFps = info.getValidFps(true);
		assertNotNull("validFps", validFps);
		assertEquals("proper ratio", "24000/1001", validFps);
		validFps = info.getValidFps(false);
		assertNotNull("validFps", validFps);
		assertEquals("proper ratio", "23.976", validFps);

	}

	@Test
	public void dlnaMediaInfoDoubleParseWithComma() {
		DLNAMediaInfo info = new DLNAMediaInfo();
		info.setFrameRate("23,976");
		String validFps = info.getValidFps(true);
		assertNotNull("validFps", validFps);
		assertEquals("proper ratio", "24000/1001", validFps);
		validFps = info.getValidFps(false);
		assertNotNull("validFps", validFps);
		assertEquals("proper ratio", "23.976", validFps);
	}

	@Test
	public void testNullFrameRate() {
		DLNAMediaInfo info = new DLNAMediaInfo();
		assertNull("valid fps", info.getValidFps(true));
	}

}
