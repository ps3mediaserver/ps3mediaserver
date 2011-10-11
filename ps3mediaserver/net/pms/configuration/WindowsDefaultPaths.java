package net.pms.configuration;

class WindowsDefaultPaths implements ProgramPaths {
	@Override
	public String getEac3toPath() {
		return "win32/eac3to/eac3to.exe";
	}

	@Override
	public String getFfmpegPath() {
		return "win32/ffmpeg.exe";
	}

	@Override
	public String getFlacPath() {
		return "win32/flac.exe";
	}

	@Override
	public String getMencoderPath() {
		return "win32/mencoder.exe";
	}

	@Override
	public String getMplayerPath() {
		return "win32/mplayer.exe";
	}

	@Override
	public String getTsmuxerPath() {
		return "win32/tsMuxeR.exe";
	}

	@Override
	public String getVlcPath() {
		return "videolan/vlc.exe";
	}

	@Override
	public String getDCRaw() {
		return "win32/dcrawMS.exe";
	}
	
	@Override
	public String getIMConvertPath() {
		return "win32/convert.exe";
	}
}
