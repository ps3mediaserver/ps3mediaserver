package net.pms.configuration;

class MacDefaultPaths implements ProgramPaths {
	@Override
	public String getEac3toPath() {
		return null;
	}

	@Override
	public String getFfmpegPath() {
		return "osx/ffmpeg";
	}

	@Override
	public String getFlacPath() {
		return "osx/flac";
	}

	@Override
	public String getMencoderPath() {
		return "osx/mencoder";
	}

	@Override
	public String getMplayerPath() {
		return "osx/mplayer";
	}

	@Override
	public String getTsmuxerPath() {
		return "osx/tsMuxeR";
	}

	@Override
	public String getVlcPath() {
		return "/Applications/VLC.app/Contents/MacOS/VLC";
	}

	@Override
	public String getDCRaw() {
		return "osx/dcraw";
	}
	
	@Override
	public String getIMConvertPath() {
		return "osx/convert";
	}
}
