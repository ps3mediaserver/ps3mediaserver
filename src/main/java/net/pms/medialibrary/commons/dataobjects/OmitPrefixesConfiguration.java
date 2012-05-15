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

import java.util.ArrayList;
import java.util.List;

public class OmitPrefixesConfiguration {
	private boolean sorting;
	private boolean filtering;
	private List<String> prefixes;
	
	public OmitPrefixesConfiguration(){
		this(false, false, new ArrayList<String>());
	}
	
	public OmitPrefixesConfiguration(boolean sorting, boolean filtering, List<String> prefixes){
		setSorting(sorting);
		setFiltering(filtering);
		setPrefixes(prefixes);
	}
	
	public void setSorting(boolean sorting) {
	    this.sorting = sorting;
    }
	
	public boolean isSorting() {
	    return sorting;
    }
	
	public void setFiltering(boolean filtering) {
	    this.filtering = filtering;
    }
	
	public boolean isFiltering() {
	    return filtering;
    }
	
	public void setPrefixes(List<String> prefixes) {
	    this.prefixes = prefixes;
    }
	
	public List<String> getPrefixes() {
	    return prefixes;
    }
}
