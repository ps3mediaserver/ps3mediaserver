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

public class DOTemplate {
	private String name;
	private long id;
	
	public DOTemplate(String name, long id){
		this.setName(name);
		this.setId(id);
	}

	public void setName(String name) {
	    this.name = name;
    }

	public String getName() {
		if(name == null) name = "";
	    return name;
    }

	public void setId(long id) {
	    this.id = id;
    }

	public long getId() {
	    return id;
    }
	
	@Override 
	public String toString(){
		return getName();
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DOTemplate)){
			return false;
		}
		
		DOTemplate compObj = (DOTemplate)obj;
		if(getId() == compObj.getId() 
				&& getName().equals(compObj.getName())){
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + String.valueOf(getId()).hashCode();
		hashCode *= 24 + getName().hashCode();
		return hashCode;
	}

	@Override
	public DOTemplate clone(){
		return new DOTemplate(getName(), getId());
	}
}
