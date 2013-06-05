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
package net.pms.encoders;

public enum PlayerPurpose {
	VIDEO_FILE_PLAYER(0),
	AUDIO_FILE_PLAYER(1),
	VIDEO_WEB_STREAM_PLAYER(2),
	AUDIO_WEB_STREAM_PLAYER(3),
	MISC_PLAYER(4);

	private final int id;

	PlayerPurpose(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
