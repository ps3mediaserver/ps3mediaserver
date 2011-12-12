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
