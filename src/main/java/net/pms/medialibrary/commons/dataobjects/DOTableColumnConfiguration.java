package net.pms.medialibrary.commons.dataobjects;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.ConditionType;

public class DOTableColumnConfiguration {
	private ConditionType conditionType;
	private int columnIndex;
	private int width;
	
	public DOTableColumnConfiguration(){
		this(ConditionType.UNKNOWN, 0, 75);
	}

	public DOTableColumnConfiguration(ConditionType conditionType,
			int columnIndex, int width) {
		setConditionType(conditionType);
		setColumnIndex(columnIndex);
		setWidth(width);
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setConditionType(ConditionType conditionType) {
		this.conditionType = conditionType;
	}

	public ConditionType getConditionType() {
		return conditionType;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	@Override
	public String toString() {
		return Messages.getString("ML.Condition.Header.Type." + conditionType.toString());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DOTableColumnConfiguration)){
			return false;
		}
		
		DOTableColumnConfiguration compObj = (DOTableColumnConfiguration)obj;
		if(getColumnIndex() == compObj.getColumnIndex()
				&& getConditionType() == compObj.getConditionType()){
			return true;
		}
		
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		int res = 65 * getColumnIndex();
		res += 65 * getConditionType().hashCode();
		
		return res;
	}
}
