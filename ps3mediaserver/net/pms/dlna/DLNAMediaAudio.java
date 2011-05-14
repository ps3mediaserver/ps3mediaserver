package net.pms.dlna;

public class DLNAMediaAudio extends DLNAMediaLang implements Cloneable {
	
	public int bitsperSample;
	public String sampleFrequency;
	public int nrAudioChannels;
	public String codecA;
	public String album;
	public String artist;
	public String songname;
	public String genre;
	public int year;
	public int track;
	public int delay;
	public String muxingModeAudio;

	public DLNAMediaAudio() {
		bitsperSample = 16;
	}

	public int getSampleRate() {
		int sr = 0;
		if (sampleFrequency != null && sampleFrequency.length() > 0) {
			try {
				sr = Integer.parseInt(sampleFrequency);
			} catch (NumberFormatException e) {}

		}
		return sr;
	}
	
	public boolean isAC3() {
		return codecA != null && (codecA.equalsIgnoreCase("ac3") || codecA.equalsIgnoreCase("a52") || codecA.equalsIgnoreCase("liba52"));
	}
	
	public boolean isTrueHD() {
		return codecA != null && codecA.equalsIgnoreCase("truehd");
	}
	
	public boolean isDTS() {
		return codecA != null && (codecA.startsWith("dts") || codecA.equalsIgnoreCase("dca"));
	}
	
	public boolean isMP3() {
		return codecA != null && codecA.equalsIgnoreCase("mp3");
	}
	
	public boolean isPCM() {
		return codecA != null && (codecA.startsWith("pcm") || codecA.equals("LPCM"));
	}
	
	public boolean isLossless() {
		return codecA != null && (isPCM() || codecA.startsWith("fla") || codecA.equals("mlp") || codecA.equals("wv"));
	}
	
	public String getAudioCodec() {
		if (isAC3())
			return "AC3";
		else if (isDTS())
			return "DTS";
		else if (isTrueHD())
			return "TrueHD";
		else if (isPCM())
			return "LPCM";
		else if (codecA != null && codecA.equals("vorbis"))
			return "OGG";
		else if (codecA != null && codecA.equals("aac"))
			return "AAC";
		else if (codecA != null && codecA.equals("mp3"))
			return "MP3";
		else if (codecA != null && codecA.startsWith("wm"))
			return "WMA";
		else if (codecA != null && codecA.equals("mp2"))
			return "Mpeg Audio";
		return codecA!=null?codecA:"-";
	}
	
	public String toString() {
		return "Audio: " + getAudioCodec() + " / lang: " + lang + " / ID: " + id;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
