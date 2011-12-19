package net.pms.configuration;

import net.pms.util.PropertiesUtil;

class WindowsDefaultPaths implements ProgramPaths {
	
	@Override
	public String getEac3toPath() {
		return getBinariesPath() + "win32/eac3to/eac3to.exe";
	}

	@Override
	public String getFfmpegPath() {
		return getBinariesPath() + "win32/ffmpeg.exe";
	}

	@Override
	public String getFlacPath() {
		return getBinariesPath() + "win32/flac.exe";
	}

	@Override
	public String getMencoderPath() {
		return getBinariesPath() + "win32/mencoder.exe";
	}

	@Override
	public String getMplayerPath() {
		return getBinariesPath() + "win32/mplayer.exe";
	}

	@Override
	public String getTsmuxerPath() {
		return getBinariesPath() + "win32/tsMuxeR.exe";
	}

	@Override
	public String getVlcPath() {
		return "videolan/vlc.exe";
	}

	@Override
	public String getDCRaw() {
		return getBinariesPath() + "win32/dcrawMS.exe";
	}
	
	@Override
	public String getIMConvertPath() {
		return getBinariesPath() + "win32/convert.exe";
	}

	/**
	 * Returns the path where binaries can be found. This path differs between
	 * the build phase and the test phase. The path will end with a slash unless
	 * it is empty.
	 *
	 * @return The path for binaries.
	 */
	private String getBinariesPath() {
		String path = PropertiesUtil.getProjectProperties().get("project.binaries");
		
		if (path != null && !"".equals(path)) {
			if (path.endsWith("/")) {
				return path;
			} else {
				return path + "/";
			}
		} else {
			return "";
		}
	}
}
