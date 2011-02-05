/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
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
package net.pms.dlna;

import org.apache.commons.lang.StringUtils;

import net.pms.util.Iso639;

public class DLNAMediaLang {
	
	public static String UND = "und";
	
	public int id;
	public String lang;
	
	public String getLang() {
		if (StringUtils.isNotBlank(lang))
			return Iso639.getLanguage(lang);
		return Iso639.getLanguage(DLNAMediaLang.UND);
	}
	
	public boolean matchCode(String code) {
		return Iso639.isCodesMatching(lang, code);
	}
	
}
