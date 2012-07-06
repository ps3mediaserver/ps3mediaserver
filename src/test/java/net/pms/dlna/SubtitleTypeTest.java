package net.pms.dlna;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class SubtitleTypeTest {
	@Test
	public void testGetSubtitleTypeByFileExtension_matchingTypes() throws Exception {
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("srt")).isEqualTo(SubtitleType.SUBRIP);
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("txt")).isEqualTo(SubtitleType.TEXT);
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("sub")).isEqualTo(SubtitleType.MICRODVD);
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("smi")).isEqualTo(SubtitleType.SAMI);
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("ssa")).isEqualTo(SubtitleType.ASS);
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("ass")).isEqualTo(SubtitleType.ASS);
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("idx")).isEqualTo(SubtitleType.VOBSUB);
	}

	@Test
	public void testGetSubtitleTypeByFileExtension_nullOrBlankExtension() throws Exception {
		assertThat(SubtitleType.getSubtitleTypeByFileExtension(null)).isEqualTo(SubtitleType.UNKNOWN);
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("")).isEqualTo(SubtitleType.UNKNOWN);
	}

	@Test
	public void testGetSubtitleTypeByFileExtension_unknownExtension() throws Exception {
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("xyz")).isEqualTo(SubtitleType.UNKNOWN);
	}

	@Test
	public void testGetSubtitleTypeByFileExtension_extensionCaseInsensitivity() throws Exception {
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("ssA")).isEqualTo(SubtitleType.ASS);
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("SSA")).isEqualTo(SubtitleType.ASS);
		assertThat(SubtitleType.getSubtitleTypeByFileExtension("sSa")).isEqualTo(SubtitleType.ASS);
	}
}
