package net.pms.dlna.virtual;

import net.pms.Messages;

public class MediaLibrary extends VirtualFolder {
	
	private MediaLibraryFolder allFolder;
	public MediaLibraryFolder getAllFolder() {
		return allFolder;
	}

	private MediaLibraryFolder albumFolder;
	private MediaLibraryFolder artistFolder;
	private MediaLibraryFolder genreFolder;
	private MediaLibraryFolder playlistFolder;

	public MediaLibraryFolder getAlbumFolder() {
		return albumFolder;
	}

	public MediaLibrary() {
		super(Messages.getString("PMS.2"), null);
		init();
	}
	
	private void init() {
		VirtualFolder vfAudio = new VirtualFolder(Messages.getString("PMS.1"), null); //$NON-NLS-1$
		allFolder = new MediaLibraryFolder(Messages.getString("PMS.11"), "TYPE = 1 ORDER BY FILENAME ASC", MediaLibraryFolder.FILES); //$NON-NLS-1$ //$NON-NLS-2$
		vfAudio.addChild(allFolder);
		playlistFolder = new MediaLibraryFolder(Messages.getString("PMS.9"), "TYPE = 16 ORDER BY FILENAME ASC", MediaLibraryFolder.PLAYLISTS); //$NON-NLS-1$ //$NON-NLS-2$
		vfAudio.addChild(playlistFolder);
		artistFolder = new MediaLibraryFolder(Messages.getString("PMS.13"), new String [] {"SELECT DISTINCT ARTIST FROM FILES WHERE TYPE = 1 ORDER BY ARTIST ASC", "TYPE = 1 AND ARTIST = '${0}'"}, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		vfAudio.addChild(artistFolder);
		albumFolder = new MediaLibraryFolder(Messages.getString("PMS.16"), new String [] {"SELECT DISTINCT ALBUM FROM FILES WHERE TYPE = 1 ORDER BY ALBUM ASC", "TYPE = 1 AND ALBUM = '${0}'"}, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		vfAudio.addChild(albumFolder);
		genreFolder = new MediaLibraryFolder(Messages.getString("PMS.19"), new String [] {"SELECT DISTINCT GENRE FROM FILES WHERE TYPE = 1 ORDER BY GENRE ASC", "TYPE = 1 AND GENRE = '${0}'"}, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		vfAudio.addChild(genreFolder);
		MediaLibraryFolder mlf6 = new MediaLibraryFolder(Messages.getString("PMS.22"), new String [] { //$NON-NLS-1$
				"SELECT DISTINCT ARTIST FROM FILES WHERE TYPE = 1 ORDER BY ARTIST ASC", //$NON-NLS-1$
				"SELECT DISTINCT ALBUM FROM FILES WHERE TYPE = 1 AND ARTIST = '${0}' ORDER BY ALBUM ASC", //$NON-NLS-1$
				"TYPE = 1 AND ARTIST = '${1}' AND ALBUM = '${0}' ORDER BY TRACK ASC, FILENAME ASC"}, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES}); //$NON-NLS-1$
		vfAudio.addChild(mlf6);
		MediaLibraryFolder mlf7 = new MediaLibraryFolder(Messages.getString("PMS.26"), new String [] { //$NON-NLS-1$
				"SELECT DISTINCT GENRE FROM FILES WHERE TYPE = 1 ORDER BY GENRE ASC", //$NON-NLS-1$
				"SELECT DISTINCT ARTIST FROM FILES WHERE TYPE = 1 AND GENRE = '${0}' ORDER BY ARTIST ASC", //$NON-NLS-1$
				"SELECT DISTINCT ALBUM FROM FILES WHERE TYPE = 1 AND GENRE = '${1}' AND ARTIST = '${0}' ORDER BY ALBUM ASC", //$NON-NLS-1$
				"TYPE = 1 AND GENRE = '${2}' AND ARTIST = '${1}' AND ALBUM = '${0}' ORDER BY TRACK ASC, FILENAME ASC"}, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.TEXTS, MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES}); //$NON-NLS-1$
		vfAudio.addChild(mlf7);
		MediaLibraryFolder mlfAudioDate = new MediaLibraryFolder(Messages.getString("PMS.12"), new String[] { "SELECT FORMATDATETIME(MODIFIED, 'd MMM yyyy') FROM FILES WHERE TYPE = 1 ORDER BY MODIFIED DESC", "TYPE = 1 AND FORMATDATETIME(MODIFIED, 'd MMM yyyy') = '${0}' ORDER BY TRACK ASC, FILENAME ASC" }, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		vfAudio.addChild(mlfAudioDate);
		
		MediaLibraryFolder mlf8 = new MediaLibraryFolder("By Letter/Artist/Album", new String [] { //$NON-NLS-1$
			"SELECT ID FROM REGEXP_RULES ORDER BY ORDR ASC", //$NON-NLS-1$
			"SELECT DISTINCT ARTIST FROM FILES WHERE TYPE = 1 AND ARTIST REGEXP (SELECT RULE FROM REGEXP_RULES WHERE ID = '${0}') ORDER BY ARTIST ASC", //$NON-NLS-1$
			"SELECT DISTINCT ALBUM FROM FILES WHERE TYPE = 1 AND ARTIST = '${0}' ORDER BY ALBUM ASC", //$NON-NLS-1$
			"TYPE = 1 AND ARTIST = '${1}' AND ALBUM = '${0}'"}, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.TEXTS, MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES}); //$NON-NLS-1$
		vfAudio.addChild(mlf8);
		addChild(vfAudio);
		
		VirtualFolder vfImage = new VirtualFolder(Messages.getString("PMS.31"), null); //$NON-NLS-1$
		MediaLibraryFolder mlfPhoto01 = new MediaLibraryFolder(Messages.getString("PMS.32"), "TYPE = 2 ORDER BY FILENAME ASC", MediaLibraryFolder.FILES); //$NON-NLS-1$ //$NON-NLS-2$
		vfImage.addChild(mlfPhoto01);
		MediaLibraryFolder mlfPhoto02 = new MediaLibraryFolder(Messages.getString("PMS.12"), new String[] { "SELECT FORMATDATETIME(MODIFIED, 'd MMM yyyy') FROM FILES WHERE TYPE = 2 ORDER BY MODIFIED DESC", "TYPE = 2 AND FORMATDATETIME(MODIFIED, 'd MMM yyyy') = '${0}' ORDER BY FILENAME ASC" }, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		vfImage.addChild(mlfPhoto02);
		MediaLibraryFolder mlfPhoto03 = new MediaLibraryFolder(Messages.getString("PMS.21"), new String[] { "SELECT MODEL FROM FILES WHERE TYPE = 2 AND MODEL IS NOT NULL ORDER BY MODEL ASC", "TYPE = 2 AND MODEL = '${0}' ORDER BY FILENAME ASC" }, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		vfImage.addChild(mlfPhoto03);
		MediaLibraryFolder mlfPhoto04 = new MediaLibraryFolder(Messages.getString("PMS.25"), new String[] { "SELECT ISO FROM FILES WHERE TYPE = 2 AND ISO > 0 ORDER BY ISO ASC", "TYPE = 2 AND ISO = '${0}' ORDER BY FILENAME ASC" }, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		vfImage.addChild(mlfPhoto04);
		addChild(vfImage);
		
		VirtualFolder vfVideo = new VirtualFolder(Messages.getString("PMS.34"), null); //$NON-NLS-1$
		MediaLibraryFolder mlfVideo01 = new MediaLibraryFolder(Messages.getString("PMS.35"), "TYPE = 4 ORDER BY FILENAME ASC", MediaLibraryFolder.FILES); //$NON-NLS-1$ //$NON-NLS-2$
		vfVideo.addChild(mlfVideo01);
		MediaLibraryFolder mlfVideo02 = new MediaLibraryFolder(Messages.getString("PMS.12"), new String[] { "SELECT FORMATDATETIME(MODIFIED, 'd MMM yyyy') FROM FILES WHERE TYPE = 4 ORDER BY MODIFIED DESC", "TYPE = 4 AND FORMATDATETIME(MODIFIED, 'd MMM yyyy') = '${0}' ORDER BY FILENAME ASC" }, new int [] { MediaLibraryFolder.TEXTS, MediaLibraryFolder.FILES }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		vfVideo.addChild(mlfVideo02);
		MediaLibraryFolder mlfVideo03 = new MediaLibraryFolder(Messages.getString("PMS.36"), "TYPE = 4 AND (WIDTH >= 1200 OR HEIGHT >= 700) ORDER BY FILENAME ASC", MediaLibraryFolder.FILES ); //$NON-NLS-1$ //$NON-NLS-2$
		vfVideo.addChild(mlfVideo03);
		MediaLibraryFolder mlfVideo04 = new MediaLibraryFolder(Messages.getString("PMS.39"), "TYPE = 4 AND (WIDTH < 1200 AND HEIGHT < 700) ORDER BY FILENAME ASC", MediaLibraryFolder.FILES ); //$NON-NLS-1$ //$NON-NLS-2$
		vfVideo.addChild(mlfVideo04);
		MediaLibraryFolder mlfVideo05 = new MediaLibraryFolder("DVD Images", "TYPE = 32 ORDER BY FILENAME ASC", MediaLibraryFolder.ISOS ); //$NON-NLS-1$ //$NON-NLS-2$
		vfVideo.addChild(mlfVideo05);
		addChild(vfVideo);
	}

	public MediaLibraryFolder getArtistFolder() {
		return artistFolder;
	}

	public MediaLibraryFolder getGenreFolder() {
		return genreFolder;
	}

	public MediaLibraryFolder getPlaylistFolder() {
		return playlistFolder;
	}

}
