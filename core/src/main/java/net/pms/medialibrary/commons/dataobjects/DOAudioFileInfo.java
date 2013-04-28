/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
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
package net.pms.medialibrary.commons.dataobjects;

public class DOAudioFileInfo extends DOFileInfo {
	private int bitsperSample;
	private String sampleFrequency;
	private int nrAudioChannels;
	private String codecA;
	private String album;
	private String artist;
	private String songName;
	private String genre;
	private int year;
	private int track;
	private int delay;
	private int duration;
	private String lang;
	private String coverPath;
	private String muxingMode;
	
	public DOAudioFileInfo(){
		this(16, "", 0, "", "", "", "", "", 0, 0, 0, 0, "", "", "");
	}
	
	public DOAudioFileInfo(int bitsperSample, String sampleFrequency, int nrAudioChannels, String codecA, String album, String artist, String songName, String genre, 
			int year, int track, int delay, int duration, String lang, String coverPath, String muxingMode){
		setBitsperSample(bitsperSample);
		setSampleFrequency(sampleFrequency);
		setNrAudioChannels(nrAudioChannels);
		setCodecA(codecA);
		setAlbum(album);
		setArtist(artist);
		setSongName(songName);
		setGenre(genre);
		setYear(year);
		setTrack(track);
		setDelay(delay);
		setDuration(duration);
		setLang(lang);
		setCoverPath(coverPath);
		setMuxingMode(muxingMode);
	}
	
	public void setBitsperSample(int bitsperSample) {
	    this.bitsperSample = bitsperSample;
    }
	
	public int getBitsperSample() {
	    return bitsperSample;
    }

	public void setSampleFrequency(String sampleFrequency) {
	    this.sampleFrequency = sampleFrequency;
    }

	public String getSampleFrequency() {
	    return sampleFrequency;
    }

	public void setNrAudioChannels(int nrAudioChannels) {
	    this.nrAudioChannels = nrAudioChannels;
    }

	public int getNrAudioChannels() {
	    return nrAudioChannels;
    }

	public void setCodecA(String codecA) {
	    this.codecA = codecA;
    }

	public String getCodecA() {
	    return codecA;
    }

	public void setAlbum(String album) {
	    this.album = album;
    }

	public String getAlbum() {
	    return album;
    }

	public void setArtist(String artist) {
	    this.artist = artist;
    }

	public String getArtist() {
	    return artist;
    }

	public void setSongName(String songName) {
	    this.songName = songName;
    }

	public String getSongName() {
	    return songName;
    }

	public void setGenre(String genre) {
	    this.genre = genre;
    }

	public String getGenre() {
	    return genre;
    }

	public void setYear(int year) {
	    this.year = year;
    }

	public int getYear() {
	    return year;
    }

	public void setTrack(int track) {
	    this.track = track;
    }

	public int getTrack() {
	    return track;
    }

	public void setDelay(int delay) {
	    this.delay = delay;
    }

	public int getDelay() {
	    return delay;
    }

	public void setDuration(int duration) {
	    this.duration = duration;
    }

	public int getDuration() {
	    return duration;
    }

	public void setLang(String lang) {
	    this.lang = lang;
    }

	public String getLang() {
	    return lang;
    }

	public void setCoverPath(String coverPath) {
	    this.coverPath = coverPath;
    }

	public String getCoverPath() {
	    return coverPath;
    }

	public void setMuxingMode(String muxingMode) {
		this.muxingMode = muxingMode;
	}

	public String getMuxingMode() {
		return muxingMode;
	}
}
