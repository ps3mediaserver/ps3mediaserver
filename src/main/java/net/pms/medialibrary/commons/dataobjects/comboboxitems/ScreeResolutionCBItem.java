package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.medialibrary.commons.enumarations.ScreenResolution;

public class ScreeResolutionCBItem implements Comparable<ScreeResolutionCBItem>{
	private ScreenResolution screenResolution;
	private String displayName;
	
	public ScreeResolutionCBItem(ScreenResolution autoFolderProperty, String displayName){
		this.setScreenResolution(autoFolderProperty);
		this.setDisplayName(displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setScreenResolution(ScreenResolution screenResolution) {
		this.screenResolution = screenResolution;
	}

	public ScreenResolution getScreenResolution() {
		return screenResolution;
	}
	
	@Override
	public String toString(){
		return getDisplayName();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof ScreeResolutionCBItem)){
			return false;
		}

		ScreeResolutionCBItem compObj = (ScreeResolutionCBItem)o;
		if(getDisplayName() == compObj.getDisplayName()
			&& getScreenResolution() == compObj.getScreenResolution()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getScreenResolution().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(ScreeResolutionCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}
