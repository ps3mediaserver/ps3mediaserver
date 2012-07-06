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