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
package net.pms.medialibrary.commons.events;

import java.util.EventObject;

import net.pms.medialibrary.commons.enumarations.DialogActionType;
import net.pms.medialibrary.commons.enumarations.AutoFolderType;

public class AutoFolderDialogActionEvent extends EventObject{	
	private static final long serialVersionUID = 1L;
	private AutoFolderType autoFolderType;
	private boolean isAscending;
	private DialogActionType actionType;
	private Object userObject;
	private int minOccurences;

	public AutoFolderDialogActionEvent(Object source, AutoFolderType autoFolderType, boolean isAscending, int minOccurences, DialogActionType actionType, Object userObject) {
		super(source);
		this.autoFolderType = autoFolderType;
		this.isAscending = isAscending;
		this.actionType = actionType;
		this.userObject = userObject;
		this.minOccurences = minOccurences;
	}

	public AutoFolderType getAutoFolderType() {
		return autoFolderType;
	}

	public boolean isAscending() {
		return isAscending;
	}

	public DialogActionType getActionType() {
		return actionType;
	}

	public Object getUserObject() {
		return userObject;
	}

	public int getMinOccurences() {
		return minOccurences;
	}

}
