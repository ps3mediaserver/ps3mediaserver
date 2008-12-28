package net.pms.configuration;

public class MacDefaultPaths implements DefaultPaths {

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
		return null;
	}

	@Override
	public String getVlcPath() {
		return "osx/vlc";
	}

}
