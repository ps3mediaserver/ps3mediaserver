package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.medialibrary.commons.enumarations.ConditionUnit;

public class ConditionUnitCBItem implements Comparable<ConditionUnitCBItem> {
	private ConditionUnit conditionUnit;
	private String displayName;
	
	public ConditionUnitCBItem(){
		this(ConditionUnit.UNKNOWN, "");
	}
	
	public ConditionUnitCBItem(ConditionUnit conditionUnit, String displayName){
		this.setConditionUnit(conditionUnit);
		this.setDisplayName(displayName);
	}

	public void setConditionUnit(ConditionUnit conditionType) {
		this.conditionUnit = conditionType;
	}

	public ConditionUnit getConditionUnit() {
		return conditionUnit;
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
		if(!(o instanceof ConditionUnitCBItem)){
			return false;
		}

		ConditionUnitCBItem compObj = (ConditionUnitCBItem)o;
		if(getDisplayName() == compObj.getDisplayName()
			&& getConditionUnit() == compObj.getConditionUnit()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getConditionUnit().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(ConditionUnitCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}
