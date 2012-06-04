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

import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileDisplayType;
import net.pms.medialibrary.commons.enumarations.SortOption;

public class FileDisplayProperties implements Cloneable {
	private String displayNameMask;
	private FileDisplayType fileDisplayType;
	private ConditionType sortType;
	private boolean sortAscending;
	private DOTemplate template;
	private List<DOThumbnailPriority> thumbnailPriorities = new ArrayList<DOThumbnailPriority>();
	private SortOption sortOption;

	public FileDisplayProperties() {
		this(true, true);
	}

	public FileDisplayProperties(boolean displayItems, boolean inherit) {
		this(FileDisplayType.FILE, displayItems, inherit);
	}

	public FileDisplayProperties(FileDisplayType fileDisplayType,
			boolean displayItems, boolean inherit) {
		this("", displayItems, fileDisplayType, inherit);
	}

	public FileDisplayProperties(String displayNameMask, boolean displayItems,
			FileDisplayType fileDisplayType, boolean inherit) {
		this(displayNameMask, displayItems, fileDisplayType, inherit, ConditionType.FILE_DATEINSERTEDDB, false, new DOTemplate("", -1), SortOption.FileProperty);
	}

	public FileDisplayProperties(String displayNameMask, boolean displayItems,
			FileDisplayType fileDisplayType, boolean inherit,
			ConditionType sortType, boolean sortAscending, DOTemplate termplate, SortOption sortOption) {
		setDisplayNameMask(displayNameMask);
		setFileDisplayType(fileDisplayType);
		setSortType(sortType);
		setSortAscending(sortAscending);
		setSortOption(sortOption);
		this.template = termplate;
	}

	public void setDisplayNameMask(String displayNameMask) {
		this.displayNameMask = displayNameMask;
	}

	public String getDisplayNameMask() {
		if(displayNameMask == null) displayNameMask = "";
		return displayNameMask;
	}

	public void setFileDisplayType(FileDisplayType fileDisplayType) {
		this.fileDisplayType = fileDisplayType;
	}

	public FileDisplayType getFileDisplayType() {
		if(fileDisplayType == null) fileDisplayType = FileDisplayType.UNKNOWN;
		return fileDisplayType;
	}

	public void setSortType(ConditionType sortType) {
		this.sortType = sortType;
	}

	public ConditionType getSortType() {
		if(sortType == null) sortType = ConditionType.UNKNOWN;
		return sortType;
	}

	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}

	public DOTemplate getTemplate() {
		return template;
	}

	public void setTemplate(DOTemplate template) {
		this.template = template;
	}

	public void setThumbnailPriorities(
			List<DOThumbnailPriority> thumbnailPriorities) {
		this.thumbnailPriorities = thumbnailPriorities;
	}

	public List<DOThumbnailPriority> getThumbnailPriorities() {
		if(thumbnailPriorities == null) thumbnailPriorities = new ArrayList<DOThumbnailPriority>();
		return thumbnailPriorities;
	}

	public void setSortOption(SortOption sortOption) {
		this.sortOption = sortOption;
	}

	public SortOption getSortOption() {
		return sortOption;
	}

	@Override
	public String toString() {
		return String.format("mask=%s, fileDisplayType=%s, sortType=%s, templateId=%s, thumbnailPrios=%s, sortAscending=%s, sortOption=%s", 
				getDisplayNameMask(), getFileDisplayType(), getSortType(), getTemplate() == null ? "null" : getTemplate().getId(), getThumbnailPriorities().size(), isSortAscending(), getSortOption());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FileDisplayProperties)) { 
			return false; 
		}

		FileDisplayProperties compObj = (FileDisplayProperties) obj;
		if (getDisplayNameMask().equals(compObj.getDisplayNameMask()) 
				&& getFileDisplayType() == compObj.getFileDisplayType()
				&& getSortType() == compObj.getSortType()
				&& isSortAscending() == compObj.isSortAscending()
				&& getSortOption() == compObj.getSortOption()
				&& ((getTemplate() == null && compObj.getTemplate() == null) 
						|| (getTemplate() != null && compObj.getTemplate() != null && getTemplate().equals(compObj.getTemplate())))
				&& getThumbnailPriorities().size() == compObj.getThumbnailPriorities().size()) {
			for(int i = 0; i < this.getThumbnailPriorities().size(); i++){
				if(!this.getThumbnailPriorities().get(i).equals(compObj.getThumbnailPriorities().get(i))){
					return false;
				}
			}
			return true; 
		}

		return false;
	}

	@Override
	public FileDisplayProperties clone() {
		FileDisplayProperties fdp = new FileDisplayProperties();
		fdp.setDisplayNameMask(getDisplayNameMask());
		fdp.setFileDisplayType(getFileDisplayType());
		fdp.setTemplate(getTemplate() == null ? null : getTemplate().clone());
		fdp.setSortAscending(isSortAscending());
		fdp.setSortType(getSortType());
		fdp.setSortOption(getSortOption());
		ArrayList<DOThumbnailPriority> prios = new ArrayList<DOThumbnailPriority>();
		for (DOThumbnailPriority prio : getThumbnailPriorities()) {
			prios.add(prio.clone());
		}
		fdp.setThumbnailPriorities(prios);

		return fdp;
	}
}
