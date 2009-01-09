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

import net.pms.PMS;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.encoders.Player;

public class FileTranscodeVirtualFolder extends VirtualFolder {

	private boolean resolved;
	
	@Override
	public void resolve() {
		super.resolve();
		if (!resolved && children.size() == 1) { //OK
			DLNAResource child = children.get(0);
			child.resolve();
			if (child.ext.getProfiles() != null) {
				DLNAResource ref = child;
				for(int i=0;i<child.ext.getProfiles().size();i++) {
					Player pl = PMS.get().getPlayer(child.ext.getProfiles().get(i), child.ext);
					if (pl !=null && !child.player.equals(pl)) {
						DLNAResource avisnewChild = (DLNAResource) child.clone();
						avisnewChild.player = pl;
						avisnewChild.noName = true;
						avisnewChild.id = avisnewChild.parent.id + "$" + children.size();
						avisnewChild.media = child.media;
						children.add(avisnewChild);
						avisnewChild.parent = this;
						if (avisnewChild.player.id().equals("mencoder"))
							ref = avisnewChild;
					}
				}
				for(int i=0;i<child.media.audioCodes.size();i++) {
					for(int j=-1;j<child.media.subtitlesCodes.size();j++) {
						DLNAResource newChild = (DLNAResource) ref.clone();
						newChild.player = ref.player;
						newChild.id = newChild.parent.id + "$" + children.size();
						newChild.media = ref.media;
						newChild.noName = true;
						children.add(newChild);
						newChild.parent = this;
						newChild.aid = ref.media.audioCodes.get(i).id;
						newChild.alang = ref.media.audioCodes.get(i).lang;
						newChild.aformat = ref.media.audioCodes.get(i).format;
						if (j >= 0) {
							newChild.sid = ref.media.subtitlesCodes.get(j).id;
							newChild.slang = ref.media.subtitlesCodes.get(j).lang;
						}
						PMS.info("Duplicate " + ref.getName() + " with player: " + ref.player.toString() + " and aid: " + newChild.aid + " and sid: " + newChild.sid);
					}
				}
			}
		}
		resolved = true;
	}

	@Override
	public void discoverChildren() {
		super.discoverChildren();
	}

	public FileTranscodeVirtualFolder(String name, String thumbnailIcon) {
		super(name, thumbnailIcon);
	}

}
