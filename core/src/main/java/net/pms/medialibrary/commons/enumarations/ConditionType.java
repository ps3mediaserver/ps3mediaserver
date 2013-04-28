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
package net.pms.medialibrary.commons.enumarations;

public enum ConditionType {
	UNKNOWN,
	
	//File
	FILE_FILENAME,
	FILE_FOLDERPATH,
	FILE_SIZEBYTE,
	FILE_DATELASTUPDATEDDB,
	FILE_DATEINSERTEDDB,
	FILE_DATEMODIFIEDOS,
	FILE_PLAYCOUNT,
	FILE_TYPE,
	FILE_CONTAINS_TAG,
	FILEPLAYS_DATEPLAYEND,
	FILE_ISACTIF,
	FILE_THUMBNAILPATH,

	//Video
	VIDEO_ORIGINALNAME,
	VIDEO_NAME,
	VIDEO_SORTNAME,
	VIDEO_TMDBID,
	VIDEO_IMDBID,
	VIDEO_TAGLINE,
	VIDEO_OVERVIEW,
	VIDEO_BUDGET,
	VIDEO_REVENUE,
	VIDEO_DIRECTOR,
	VIDEO_YEAR,
	VIDEO_DURATIONSEC,
	VIDEO_WIDTH,
	VIDEO_HEIGHT,
	VIDEO_HOMEPAGEURL,
	VIDEO_TRAILERURL,
	VIDEO_CERTIFICATION,
	VIDEO_CERTIFICATIONREASON,
	VIDEO_RATINGPERCENT,
	VIDEO_RATINGVOTERS,
	VIDEO_CODECV,
	VIDEO_MIMETYPE,
	VIDEO_ASPECTRATIO,
	VIDEO_BITRATE,
	VIDEO_BITSPERPIXEL,
	VIDEO_CONTAINER,
	VIDEO_DVDTRACK,
	VIDEO_FRAMERATE,
	VIDEO_MODEL,
	VIDEO_MUXABLE,
	VIDEO_MUXINGMODE,
	VIDEO_CONTAINS_VIDEOAUDIO,
	VIDEO_CONTAINS_SUBTITLES,
	VIDEO_CONTAINS_GENRE,
	
	//Image
	IMAGE_WIDTH,
	IMAGE_HEIGHT,
	
	//Audio
	AUDIO_SONGNAME,
	AUDIO_ARTIST,
	AUDIO_ALBUM,
	AUDIO_GENRE,
	AUDIO_YEAR,
	AUDIO_TRACK,
	AUDIO_LANG,
	AUDIO_NRAUDIOCHANNELS,
	AUDIO_SAMPLEFREQ,
	AUDIO_CODECA,
	AUDIO_DURATION_SEC,
	AUDIO_DELAYMS,
	AUDIO_BITSPERSAMPLE,
	AUDIO_COVERPATH,
	AUDIO_MUXINGMODE
}
