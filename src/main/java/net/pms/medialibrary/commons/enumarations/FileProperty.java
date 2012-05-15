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

public enum FileProperty {
	UNKNOWN,

	//Video
	VIDEO_ORIGINALNAME,
	VIDEO_NAME,
	VIDEO_TMDBID,
	VIDEO_IMDBID,
	VIDEO_TAGLINE,
	VIDEO_OVERVIEW,
	VIDEO_BUDGET,
	VIDEO_REVENUE,
	VIDEO_DIRECTOR,
	VIDEO_YEAR,
	VIDEO_HOMEPAGEURL,
	VIDEO_TRAILERURL,
	VIDEO_CERTIFICATION,
	VIDEO_CERTIFICATIONREASON,
	VIDEO_RATINGPERCENT,
	VIDEO_RATINGVOTERS,
	VIDEO_COVERURL,
	VIDEO_GENRES,
	
	//Audio
	AUDIO_SONGNAME,
	AUDIO_ARTIST,
	AUDIO_ALBUM,
	AUDIO_GENRE,
	AUDIO_YEAR,
	AUDIO_COVERPATH,
	
	//Pictures
	PICTURES_WIDTH,
	PICTURES_HEIGHT
}
