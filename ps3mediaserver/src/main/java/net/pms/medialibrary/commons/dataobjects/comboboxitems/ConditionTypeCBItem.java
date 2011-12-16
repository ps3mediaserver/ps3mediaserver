package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.medialibrary.commons.enumarations.ConditionType;

public class ConditionTypeCBItem implements Comparable<ConditionTypeCBItem> {
	private ConditionType conditionType;
	private String displayName;
	
	public ConditionTypeCBItem(){
		this(ConditionType.UNKNOWN, "");
	}
	
	public ConditionTypeCBItem(ConditionType conditionType, String displayName){
		this.setConditionType(conditionType);
		this.setDisplayName(displayName);
	}

	public void setConditionType(ConditionType conditionType) {
		this.conditionType = conditionType;
	}

	public ConditionType getConditionType() {
		return conditionType;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public String toString(){
		return getDisplayName();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof ConditionTypeCBItem)){
			return false;
		}

		ConditionTypeCBItem compObj = (ConditionTypeCBItem)o;
		if(getDisplayName().equals(compObj.getDisplayName())
			&& getConditionType() == compObj.getConditionType()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getConditionType().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(ConditionTypeCBItem o) {
	    return this.displayName.compareTo(o.displayName);
    }
}
