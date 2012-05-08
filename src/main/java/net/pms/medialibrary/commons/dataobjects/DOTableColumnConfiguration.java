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
