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

public class DORating {
	private int ratingPercent;
	private int votes;
	
	/**
	 * Constructor with default parameters
	 */
	public DORating(){
		this(0, 0);
	}
	
	/**
	 * Constructor populating the rating with passed values
	 * @param ratingPercent Rating 0-100
	 * @param votes Number of votes
	 */
	public DORating(int ratingPercent, int votes){
		setRatingPercent(ratingPercent);
		setVotes(votes);
	}

	public void setRatingPercent(int ratingPercent) {
	    this.ratingPercent = ratingPercent;
    }

	public int getRatingPercent() {
	    return ratingPercent;
    }

	public void setVotes(int votes) {
	    this.votes = votes;
    }

	public int getVotes() {
	    return votes;
    }
	
	@Override
	public String toString(){
		return getRatingPercent() + "% with " + getVotes() + " votes";
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + getRatingPercent();
		hashCode *= 24 + getVotes();
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DORating)){
			return false;
		}
		
		DORating compObj = (DORating)obj;
		if(getRatingPercent() == compObj.getRatingPercent() 
				&& getVotes() == compObj.getVotes()){
			return true;
		}
		
		return false;		
	}
}
