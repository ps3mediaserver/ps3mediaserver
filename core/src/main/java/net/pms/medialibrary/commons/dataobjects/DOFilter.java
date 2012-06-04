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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.pms.medialibrary.commons.dataobjects.DOCondition;
import net.pms.medialibrary.commons.exceptions.FilterFormatException;

public class DOFilter implements Cloneable {
	private List<DOCondition> conditions;
	private String            equation;

	/**
	 * Builds a DOFilter with empty equation and no conditions
	 */
	public DOFilter() {
		this("", new ArrayList<DOCondition>());
	}

	/**
	 * Builds a DOFilter
	 * 
	 * @param equation
	 * @param conditions
	 */
	public DOFilter(String equation, List<DOCondition> conditions) {
		this.setEquation(equation);
		this.setConditions(conditions);
	}

	public void setConditions(List<DOCondition> conditions) {
		this.conditions = conditions;
	}

	public List<DOCondition> getConditions() {
		if(conditions == null) conditions = new ArrayList<DOCondition>();
		return conditions;
	}

	public void setEquation(String equation) {
		this.equation = equation;
	}

	public String getEquation() {
		if(equation == null) equation = "";
		return equation;
	}

	/**
	 * if the filter is valid, nothing happens
	 * 
	 * @throws ParseException
	 *             thrown when an error occurs while validating the equation
	 * @throws FilterFormatException
	 *             thrown when errors occur while validating conditions
	 */
	public void validate() throws ParseException, FilterFormatException {
		Queue<Character> parenthesis = new LinkedList<Character>();
		String currWord = "";
		boolean expectOperator = false;
		equation = equation.trim();

		if (equation.equals("")) { return; }

		// check that the equation is well formed in means of parenthesis and
		// word order
		for (int i = 0; i < equation.length(); i++) {
			char c = equation.charAt(i);

			// check parenthesis
			if (c == '(') {
				if (expectOperator) { throw new ParseException("No paranthesis allowed in front of an operator", i); }
				parenthesis.add('(');
			} else if (c == ')') {
				if (parenthesis.isEmpty() || !parenthesis.remove().equals('(')) { throw new ParseException("Error parsing equation. Brackets didn't match", i); }
			}

			// check rest
			currWord += c;
			if (currWord.equals("AND") || currWord.equals("OR")) {
				if (!expectOperator) { throw new ParseException("Error parsing equation because AND/OR was misplaced", i); }
				expectOperator = false;
				currWord = "";
				continue;
			}

			// check if word is complete and possibly clean string
			boolean wordComplete = false;
			if (c == ' ' || c == '(' || c == ')') {
				currWord = currWord.substring(0, currWord.length() - 1);
				wordComplete = true;
			} else if (i == equation.length() - 1) {
				wordComplete = true;
			}

			// don't do anything if we've got an empty string
			if (currWord.equals("")) {
				continue;
			}

			// verify that we've got the according condition in the list if a
			// word end has been detected
			if (wordComplete) {
				if (expectOperator) { throw new ParseException(String.format("Error parsing equation because condition '%1$1s' was misplaced", currWord), i); }

				boolean conditionNameExists = false;
				for (DOCondition condition : this.getConditions()) {
					if (condition.getName().equals(currWord)) {
						conditionNameExists = true;
						break;
					}
				}

				if (!conditionNameExists) { throw new ParseException(String.format(
				        "Error parsing equation  because condition '%1$1s' hasn't been found in the list of conditions", currWord), i); }

				expectOperator = true;
				currWord = "";
			}
		}

		// Check that all the conditions are unique
		List<DOCondition> uniqueCons = new ArrayList<DOCondition>();
		for (DOCondition con : this.conditions) {
			if (uniqueCons.contains(con)) {
				throw new FilterFormatException(String.format("You are not allowed to use the same contion twice for one folder" + System.getProperty("line.separator")
				        + "%1$s and %2$s are the same", uniqueCons.get(uniqueCons.indexOf(con)).getName(), con.getName()));
			} else {
				uniqueCons.add(con);
			}
		}

		if (!expectOperator) { throw new ParseException("A parameter is expected after the last operator", equation.length()); }
		if (parenthesis.size() > 0) { throw new ParseException("Error parsing equation. Brackets didn't match", equation.length()); }
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DOFilter)) { 
			return false; 
		}

		DOFilter compObj = (DOFilter) obj;
		if (getEquation().equals(compObj.getEquation()) 
				&& getConditions().size() == compObj.getConditions().size()) {
			for(DOCondition c : getConditions()){
				boolean found = false;
				for(DOCondition c2 : compObj.getConditions()){
					if(c.equalCondition(c2)){
						found = true;
						break;
					}					
				}
				if(!found){
					return false;
				}	
			}
			return true; 
		}

		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + getEquation().hashCode();
		hashCode *= 24 + getConditions().hashCode();
		return hashCode;
	}

	@Override
	public String toString() {
		return getEquation();
	}

	@Override
	public DOFilter clone() {
		DOFilter clone = new DOFilter();
		clone.setEquation(getEquation());
		ArrayList<DOCondition> cloneCon = new ArrayList<DOCondition>();
		for (DOCondition con : getConditions()) {
			cloneCon.add(con.clone());
		}
		clone.setConditions(cloneCon);

		return clone;
	}
}
