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

import net.pms.medialibrary.commons.enumarations.ConditionOperator;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.ConditionUnit;
import net.pms.medialibrary.commons.enumarations.ConditionValueType;

public class DOCondition implements Cloneable{
	private String name;
	private ConditionType conditionType;
	private ConditionOperator  conditionOperator;
	private String condition;
	private ConditionValueType valueType;
	private ConditionUnit unit;
	private String tagName;
	
	public DOCondition(){
		this(ConditionType.UNKNOWN, ConditionOperator.UNKNOWN, "", "", ConditionValueType.UNKNOWN, ConditionUnit.UNKNOWN, "");
	}
	
	public DOCondition(ConditionType conditionType, ConditionOperator  conditionOperator, String condition, String name,ConditionValueType valueType, ConditionUnit unit, String tagName){
		this.conditionType = conditionType;
		this.conditionOperator = conditionOperator;
		this.condition = condition;
		this.name = name;
		this.valueType = valueType;
		this.unit = unit;
		this.tagName = tagName;
	}
	
	public void setType(ConditionType conditionType) {
		this.conditionType = conditionType;
	}
	public ConditionType getType() {
		if(conditionType == null) conditionType = ConditionType.UNKNOWN;
		return conditionType;
	}
	public void setOperator(ConditionOperator conditionOperator) {
		this.conditionOperator = conditionOperator;
	}
	public ConditionOperator getOperator() {
		if(conditionOperator == null) conditionOperator = ConditionOperator.UNKNOWN;
		return conditionOperator;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getCondition() {
		if(condition == null) condition = "";
		return condition;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		if(name == null) name = "";
		return name;
	}

	public void setValueType(ConditionValueType valueType) {
	    this.valueType = valueType;
    }

	public ConditionValueType getValueType() {
		if(valueType == null) valueType = ConditionValueType.UNKNOWN;
	    return valueType;
    }

	public void setUnit(ConditionUnit unit) {
	    this.unit = unit;
    }

	public ConditionUnit getUnit() {
		if(unit == null) unit = ConditionUnit.UNKNOWN;
	    return unit;
    }
	
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public boolean isValid(){
		boolean isValid = true;
		if(getValueType() == ConditionValueType.DOUBLE 
				|| getValueType() == ConditionValueType.FILESIZE 
				|| getValueType() == ConditionValueType.TIMESPAN){
			if (!getCondition().matches("^[0-9]*\\.?[0-9]*$")) {
				isValid = false;
	        }
		} else if(getValueType() == ConditionValueType.INTEGER){
			if (!getCondition().matches("^[0-9]*$")) {
				isValid = false;
	        }
		} else if(getValueType() == ConditionValueType.DATETIME){
			if (!getCondition().matches("^(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]) (0[1-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$")
					&& !getCondition().matches("^(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")) {
				isValid = false;
	        }
		}
		return isValid;
	}
	
	public boolean equalCondition(DOCondition c){
		if(c.getType() == getType()
				&& c.getOperator() == getOperator()
				&& c.getCondition().equals(getCondition())
				&& c.getValueType() == getValueType()
				&& c.getUnit() == getUnit()
				&& c.getTagName().equals(getTagName())){
				return true;
			}
			
			return false;		
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof DOCondition)){
			return false;
		}

		DOCondition compCondition = (DOCondition)o;
		if(equalCondition(compCondition)
			&& compCondition.getName().equals(getName())){
			return true;
		}
		
		return false;
	}
	
	public int hashCodeCondition(){
		int hashCode = 24 + getType().hashCode();
		hashCode *= 24 + getOperator().hashCode();
		hashCode *= 24 + getCondition().hashCode();
		hashCode *= 24 + getValueType().hashCode();
		hashCode *= 24 + getUnit().hashCode();
		hashCode *= 24 + getTagName().hashCode();
		return hashCode;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + hashCodeCondition();
		hashCode *= 24 + getName().hashCode();
		return hashCode;
	}

	@Override
	public String toString(){
		return getCondition();
	}
	
	public DOCondition clone(){
		return new DOCondition(getType(), getOperator(), getCondition(), getName(), getValueType(), getUnit(), getTagName());
	}
}
