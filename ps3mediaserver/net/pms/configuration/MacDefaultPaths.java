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
		return null;
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
		return "osx/vlc";
	}

	@Override
	public String getMencoderMTPath() {
		return "osx/mencoder_mt";
	}

	@Override
	public String getDCRaw() {
		return "osx/dcrawU";
	}

}
