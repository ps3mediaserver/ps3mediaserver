package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.medialibrary.commons.enumarations.ConditionOperator;

public class ConditionOperatorCBItem implements Comparable<ConditionOperatorCBItem> {
	private ConditionOperator conditionOperator;
	private String displayName;
	
	public ConditionOperatorCBItem(){
		this(ConditionOperator.UNKNOWN, "");
	}
	
	public ConditionOperatorCBItem(ConditionOperator conditionType, String displayName){
		this.setConditionOperator(conditionType);
		this.setDisplayName(displayName);
	}

	public void setConditionOperator(ConditionOperator conditionOperator) {
		this.conditionOperator = conditionOperator;
	}

	public ConditionOperator getConditionOperator() {
		return conditionOperator;
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
		if(!(o instanceof ConditionOperatorCBItem)){
			return false;
		}

		ConditionOperatorCBItem compObj = (ConditionOperatorCBItem)o;
		if(getConditionOperator() == compObj.getConditionOperator()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getConditionOperator().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(ConditionOperatorCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}
