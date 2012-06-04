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

import java.util.ArrayList;
import java.util.List;

import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.FolderType;

public class DOMediaLibraryFolder extends DOFolder implements Cloneable {	
	private DOFilter                   filter;
	private boolean                    inheritsConditions;
	private List<DOFolder> 			   childFolders = new ArrayList<DOFolder>();
	private FileType                   fileType;
	private FileDisplayProperties      displayProperties;
	private boolean                    inheritSort;
	private boolean                    inheritDisplayFileAs;
	private boolean                    displayItems;
	private int maxFiles;

	public DOMediaLibraryFolder() {
		this(-1, null);
	}

	public DOMediaLibraryFolder(long id, DOMediaLibraryFolder parentFolder) {
		this(id, parentFolder, "");
	}

	public DOMediaLibraryFolder(long id, DOMediaLibraryFolder parentFolder, String displayName) {
		this(id, parentFolder, displayName, "", new ArrayList<DOCondition>(), true, true, FileType.UNKNOWN, 0, true, true);
	}

	public DOMediaLibraryFolder(long id, DOMediaLibraryFolder parentFolder, String displayName, String equation, List<DOCondition> conditions, boolean displayItems,
	        boolean inheritsConditions, FileType fileType, int positionInParent, boolean inheritSort, boolean inheritDisplayFileAs) {
		this(id, parentFolder, displayName, equation, conditions, displayItems, inheritsConditions, fileType, positionInParent,
		        new FileDisplayProperties(), inheritSort, inheritDisplayFileAs);
	}

	public DOMediaLibraryFolder(long id, DOMediaLibraryFolder parentFolder, String displayName, String equation, List<DOCondition> conditions, boolean displayItems,
	        boolean inheritsConditions, FileType fileType, int positionInParent, FileDisplayProperties displayProperties, boolean inheritSort,
	        boolean inheritDisplayFileAs) {
		super(displayName, id, parentFolder == null ? -1 : parentFolder.getId(), positionInParent, FolderType.MEDIALIBRARY);
		setParentFolder(parentFolder);
		setFilter(new DOFilter(equation, conditions));
		setInheritsConditions(inheritsConditions);
		setFileType(fileType);
		setDisplayProperties(displayProperties);
		setDisplayItems(displayItems);
		setInheritSort(inheritSort);
		setInheritDisplayFileAs(inheritDisplayFileAs);
	}

	public DOFilter getInheritedFilter() {
		String equation;
		if (getFilter().getConditions().size() > 1) {
			equation = "(" + this.filter.getEquation() + ")";
		} else {
			equation = this.filter.getEquation();
		}
		List<DOCondition> conditions = new ArrayList<DOCondition>();

		// add our conditions
		conditions.addAll(this.filter.getConditions());

		// add the parent conditions if we have isInheritsConditions is set
		// change the name of parent conditions with
		// <parent_id>_<condition_name> to have unique names
		if (this.isInheritsConditions()) {
			DOMediaLibraryFolder parent = this;
			String parentEquation = null;
			while ((parent = parent.getParentFolder()) != null) {
				parentEquation = parent.getFilter().getEquation();
				String[] elems = parentEquation.split(" ");
				for (DOCondition condition : parent.getFilter().getConditions()) {
					DOCondition newCon = new DOCondition(condition.getType(), condition.getOperator(), condition.getCondition(), parent.getId() + "_"
					        + condition.getName(), condition.getValueType(), condition.getUnit(), condition.getTagName());
					for (int i = 0; i < elems.length; i++) {
						if (elems[i].equals(condition.getName())) {
							elems[i] = newCon.getName();
						}
					}

					conditions.add(newCon);
				}

				parentEquation = "";
				for (String elem : elems) {
					parentEquation += elem + " ";
				}
				parentEquation = parentEquation.trim();

				if (equation == null || equation.equals("")) {
					if (parent.getFilter().getConditions().size() > 1) {
						equation = "(" + parentEquation + ")";
					} else {
						equation = parentEquation;
					}
				} else if (!parentEquation.equals("")) {
					if (parent.getFilter().getConditions().size() > 1) {
						equation += " AND (" + parentEquation + ")";
					} else {
						equation += " AND " + parentEquation;
					}
				}

				if (!parent.isInheritsConditions()) {
					break;
				}
			}
		}

		return new DOFilter(equation, conditions);
	}

	public void setDisplayItems(boolean displayItems) {
		this.displayItems = displayItems;
	}

	public boolean isDisplayItems() {
		return displayItems;
	}

	public boolean isInheritSort() {
		boolean retVal = inheritSort;
		if(getParentFolder() != null 
				&& getParentFolder().getFileType() != getFileType() 
				&& getParentFolder().getFileType() != FileType.FILE){
			retVal = false;
		}
		return retVal;
	}

	public void setInheritSort(boolean inherit) {
		this.inheritSort = inherit;
	}

	public void setInheritDisplayFileAs(boolean inheritDisplayFileAs) {
		this.inheritDisplayFileAs = inheritDisplayFileAs;
	}

	public boolean isInheritDisplayFileAs() {
		boolean retVal = inheritDisplayFileAs;
		if(getParentFolder() != null && getParentFolder().getFileType() != getFileType() && getParentFolder().getFileType() != FileType.FILE){
			retVal = false;
		}
		return retVal;
	}

	public void setChildFolders(List<DOFolder> childFolders) {
		this.childFolders = childFolders;
	}

	public List<DOFolder> getChildFolders() {
		return childFolders;
	}

	public void setInheritsConditions(boolean inheritsConditions) {
		this.inheritsConditions = inheritsConditions;
	}

	public boolean isInheritsConditions() {
		boolean retVal = inheritsConditions;
		if(getParentFolder() != null && getParentFolder().getFileType() != getFileType() && getParentFolder().getFileType() != FileType.FILE){
			retVal = false;
		}
		return retVal;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFilter(DOFilter filter) {
		this.filter = filter;
	}

	public DOFilter getFilter() {
		return filter;
	}

	public FileDisplayProperties getDisplayProperties() {
		if ((this.isInheritDisplayFileAs() || this.isInheritSort()) 
				&& getParentFolder() != null 
				&& (getFileType() == getParentFolder().getFileType() || getParentFolder().getFileType() == FileType.FILE)) {
			FileDisplayProperties props = getParentFolder().getDisplayProperties().clone();

			if (isInheritDisplayFileAs()) {
				this.displayProperties.setDisplayNameMask(props.getDisplayNameMask());
				this.displayProperties.setFileDisplayType(props.getFileDisplayType());
				this.displayProperties.setTemplate(props.getTemplate());
				this.displayProperties.setThumbnailPriorities(props.getThumbnailPriorities());
			}
			if (this.isInheritSort()) {
				this.displayProperties.setSortAscending(props.isSortAscending());
				this.displayProperties.setSortType(props.getSortType());
				this.displayProperties.setSortOption(props.getSortOption());
			}
		}

		return this.displayProperties;
	}

	public void setDisplayProperties(FileDisplayProperties displayProperties) {
		this.displayProperties = displayProperties.clone();
	}

	public void setMaxFiles(int maxFiles) {
		this.maxFiles = maxFiles;
	}

	public int getMaxFiles() {
		return maxFiles;
	}

	@Override
	public DOMediaLibraryFolder clone() {
		DOMediaLibraryFolder clone = new DOMediaLibraryFolder();
		clone.setName(getName());
		clone.setId(getId());
		clone.setParentId(getParentId());
		clone.setPositionInParent(getPositionInParent());
		clone.setFolderType(getFolderType());
		
		List<DOFolder> tmpChildren = new ArrayList<DOFolder>();
		for (DOFolder child : getChildFolders()) {
			tmpChildren.add(child);
		}
		clone.setChildFolders(tmpChildren);

		clone.setFileType(getFileType());
		clone.setFilter(getFilter().clone());
		clone.setInheritsConditions(isInheritsConditions());
		clone.setParentFolder(getParentFolder());
		clone.setPositionInParent(getPositionInParent());
		clone.setDisplayItems(isDisplayItems());
		clone.setInheritSort(isInheritSort());
		clone.setInheritDisplayFileAs(isInheritDisplayFileAs());
		clone.setDisplayProperties(getDisplayProperties().clone());
		clone.setMaxFiles(getMaxFiles());

		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DOMediaLibraryFolder)) { 
			return false; 
		}

		DOMediaLibraryFolder compObj = (DOMediaLibraryFolder) obj;
		if (super.equals(compObj) 
				&& isDisplayItems() == compObj.isDisplayItems() 
				&& isInheritSort() == compObj.isInheritSort() 
				&& isInheritsConditions() == compObj.isInheritsConditions()
				&& isInheritDisplayFileAs() == compObj.isInheritDisplayFileAs()
		        && getFileType().equals(compObj.getFileType()) 
		        && getInheritedFilter().equals(compObj.getInheritedFilter())
		        && getDisplayProperties().equals(compObj.getDisplayProperties())
		        && getMaxFiles() == compObj.getMaxFiles()) { 
			/*if(getChildFolders() == null && compObj.getChildFolders() == null){
				return true;
			} else if((getChildFolders() == null 	
					|| compObj.getChildFolders() == null
					|| getChildFolders().size() != compObj.getChildFolders().size())){
				return false;
			}  else {
				for(DOFolder c1 : getChildFolders()){
					boolean found = false;
					for(DOFolder c2 : compObj.getChildFolders()){
						if(c1.getId() == c2.getId()
								&& c1.getName().equals(c2.getName())){
							found = true;
							break;
						}						
					}
					if(!found){
						return false;
					}
				}
			}*/
			return true; 
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode =  24 + super.hashCode();
		hashCode *= 24 + (isDisplayItems() ? 1 : 2);
		hashCode *= 24 + (isInheritSort() ? 3 : 4);
		hashCode *= 24 + (isInheritsConditions() ? 5 : 6);
		hashCode *= 24 + (isInheritDisplayFileAs() ? 7 : 8);		
		hashCode *= 24 + getFileType().hashCode();
		hashCode *= 24 + getInheritedFilter().hashCode();	
		hashCode *= 24 + getDisplayProperties().hashCode();
		hashCode *= 24 + getFolderType().hashCode();
		hashCode *= 24 + getMaxFiles();
		for(DOFolder c : getChildFolders()){
			hashCode *= 24 + c.getId();			
		}		
		
		return hashCode;
	}	
	
	@Override
	public String toString(){
		return getName();
	}
}
