/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
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
package net.pms.dlna;

import net.pms.dlna.virtual.VirtualFolder;

public class ChapterFileTranscodeVirtualFolder extends VirtualFolder {
	private boolean resolved;
	private int interval;

	public ChapterFileTranscodeVirtualFolder(String name, String thumbnailIcon, int interval) {
		super(name, thumbnailIcon);
		this.interval = interval;
	}

	@Override
	public void resolve() {
		super.resolve();
		if (!resolved && children.size() == 1) { //OK
			DLNAResource child = children.get(0);
			child.resolve();
			int nbMinutes = (int) (child.media.getDurationInSeconds() / 60);
			int nbIntervals = nbMinutes / interval;
			for (int i = 1; i <= nbIntervals; i++) {
				DLNAResource newChildNoSub = (DLNAResource) child.clone();
				newChildNoSub.player = child.player;
				newChildNoSub.media = child.media;
				newChildNoSub.noName = true;
				newChildNoSub.media_audio = child.media_audio;
				newChildNoSub.media_subtitle = child.media_subtitle;
				newChildNoSub.splitStart = 60 * i * interval;
				newChildNoSub.splitLength = newChildNoSub.media.getDurationInSeconds() - newChildNoSub.splitStart;
				addChildInternal(newChildNoSub);
			}
		}
		resolved = true;
	}

}
