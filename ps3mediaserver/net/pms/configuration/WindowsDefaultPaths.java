package net.pms.configuration;

public class WindowsDefaultPaths implements DefaultPaths {

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

}
