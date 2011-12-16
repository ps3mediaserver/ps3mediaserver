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
