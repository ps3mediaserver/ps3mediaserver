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

import net.pms.medialibrary.commons.enumarations.KeyCombination;

/**
 * The Class DOQuickTagEntry is being used to define a key combination to use to add a tag to files
 */
public class DOQuickTagEntry {
	
	/** The name. */
	private String name;
	
	/** The tag name. */
	private String tagName;
	
	/** The tag value. */
	private String tagValue;
	
	/** The virtual key . */
	private int keyCode;
	
	/** The key combination. */
	private KeyCombination keyCombination;
	
	/**
	 * Instantiates a new quick tag entry with default parameters.
	 */
	public DOQuickTagEntry() {
		this("", "", "", 0, KeyCombination.Unknown);
	}
	
	/**
	 * Instantiates a new quick tag entry.
	 *
	 * @param name the name
	 * @param tagName the tag name
	 * @param tagValue the tag value
	 * @param virtualKey the virtual key
	 * @param keyCombination the key combination
	 */
	public DOQuickTagEntry(String name, String tagName, String tagValue, int keyCode, KeyCombination keyCombination) {
		setName(name);
		setTagName(tagName);
		setTagValue(tagValue);
		setKeyCode(keyCode);
		setKeyCombination(keyCombination);
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the tag name.
	 *
	 * @return the tag name
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * Sets the tag name.
	 *
	 * @param tagName the new tag name
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * Gets the tag value.
	 *
	 * @return the tag value
	 */
	public String getTagValue() {
		return tagValue;
	}

	/**
	 * Sets the tag value.
	 *
	 * @param tagValue the new tag value
	 */
	public void setTagValue(String tagValue) {
		this.tagValue = tagValue;
	}

	/**
	 * Gets the key code.
	 *
	 * @return the key code
	 */
	public int getKeyCode() {
		return keyCode;
	}

	/**
	 * Sets the key code.
	 *
	 * @param keyCode the new key code
	 */
	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	/**
	 * Gets the key combination.
	 *
	 * @return the key combination
	 */
	public KeyCombination getKeyCombination() {
		return keyCombination;
	}

	/**
	 * Sets the key combination.
	 *
	 * @param keyCombination the new key combination
	 */
	public void setKeyCombination(KeyCombination keyCombination) {
		this.keyCombination = keyCombination;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
