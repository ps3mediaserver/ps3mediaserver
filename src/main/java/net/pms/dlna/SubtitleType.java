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
package net.pms.dlna;

import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

public enum SubtitleType {
	UNKNOWN (),
	SUBRIP ("srt"), 	// srt
	TEXT ("txt"),		// txt
	MICRODVD ("sub"),	// sub
	SAMI ("smi"),		// smi
	ASS ("ssa", "ass"), // ssa, ass
	VOBSUB ("idx"),		// idx+sub
	EMBEDDED ();

	private Set<String> fileExtensions;

	private static Map<String, SubtitleType> fileExtensionToSubtitleTypeMap;
	static {
		fileExtensionToSubtitleTypeMap = new HashMap<String, SubtitleType>();
		for (SubtitleType subtitleType : values()) {
			for (String fileExtension : subtitleType.fileExtensions) {
				fileExtensionToSubtitleTypeMap.put(fileExtension, subtitleType);
			}
		}
	}
	public static SubtitleType getSubtitleTypeByFileExtension(String fileExtension) {
		if (isBlank(fileExtension)) {
			return UNKNOWN;
		}
		SubtitleType subtitleType = fileExtensionToSubtitleTypeMap.get(fileExtension.toLowerCase());
		if (subtitleType == null) {
			subtitleType = UNKNOWN;
		}
		return subtitleType;
	}

	private SubtitleType(String... fileExtensions) {
		this.fileExtensions = new HashSet<String>(Arrays.asList(fileExtensions));
	}
}