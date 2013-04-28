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

public class DOCertification {
	private String level;
	private String reason;
	
	public DOCertification(){
		this("", "");
	}
	
	public DOCertification(String level, String reason){
		setLevel(level);
		setReason(reason);
	}

	public void setLevel(String level) {
	    this.level = level;
    }

	public String getLevel() {
		if(level == null) level = "";
	    return level;
    }

	public void setReason(String reason) {
	    this.reason = reason;
    }

	public String getReason() {
		if(reason == null) reason = "";
	    return reason;
    }
	
	@Override
	public String toString(){
		return getLevel() + (getReason().equals("") ? "" : ": " + getReason());
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DOCertification)){
			return false;
		}
		
		DOCertification compObj = (DOCertification)obj;
		if(getLevel().equals(compObj.getLevel()) 
				&& getReason().equals(compObj.getReason())){
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + getLevel().hashCode();
		hashCode *= 24 + getReason().hashCode();
		return hashCode;
	}
	
	@Override
	public DOCertification clone(){
		return new DOCertification(getLevel(), getReason());
	}
}
