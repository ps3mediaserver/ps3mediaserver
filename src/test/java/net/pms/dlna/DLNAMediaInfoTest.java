/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2013  I. Sokolov
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

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class DLNAMediaInfoTest {
	@Test
	public void testIsAspectRatioMismatch() throws Exception {
		DLNAMediaInfo media = new DLNAMediaInfo();
		media.setAspectRatioContainer("16:9");
		media.setAspectRatioVideoTrack("16:9");
		assertThat(media.isAspectRatioMismatch()).isFalse();

		media.setAspectRatioContainer("wide");
		media.setAspectRatioVideoTrack("wiDe");
		assertThat(media.isAspectRatioMismatch()).isFalse();

		media.setAspectRatioContainer("");
		media.setAspectRatioVideoTrack("16:9");
		assertThat(media.isAspectRatioMismatch()).isFalse();

		media.setAspectRatioContainer("");
		media.setAspectRatioVideoTrack("");
		assertThat(media.isAspectRatioMismatch()).isFalse();

		media.setAspectRatioContainer("16:9");
		media.setAspectRatioVideoTrack("4:3");
		assertThat(media.isAspectRatioMismatch()).isTrue();

		media.setAspectRatioContainer("16:9");
		media.setAspectRatioVideoTrack("");
		assertThat(media.isAspectRatioMismatch()).isTrue();
	}
}
