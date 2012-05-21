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
package net.pms.medialibrary.commons.helpers;

import java.util.Comparator;

import net.pms.medialibrary.commons.dataobjects.DOFolder;

public class FolderComparator implements Comparator<DOFolder>{
	public enum CompareType{
		POSITION_IN_PARENT;
	}
	
	private CompareType compareType;
	
	public FolderComparator(){
		this(CompareType.POSITION_IN_PARENT);
	}
	
	public FolderComparator(CompareType compareType){
		this.compareType = compareType;
	}

	@Override
	public int compare(DOFolder o1, DOFolder o2) {
		int retVal = 0;
		
		if(this.compareType == CompareType.POSITION_IN_PARENT){
			if(o1.getPositionInParent() == o2.getPositionInParent()){
				retVal = 0;
			}else if(o1.getPositionInParent() < o2.getPositionInParent()){
				retVal = -1;
			}else{
				retVal = 1;
			}
		}
		return retVal;
	}

}
