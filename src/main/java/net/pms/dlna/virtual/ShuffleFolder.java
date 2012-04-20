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
package net.pms.dlna.virtual;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.virtual.VirtualFolder;
import java.util.List;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShuffleFolder extends VirtualFolder {
	private static final Logger logger = LoggerFactory.getLogger(ShuffleFolder.class);
	private boolean resolved;

	public ShuffleFolder() {
		super(SHUFFLE_FOLDER, null);
	}

	@Override
	public void resolve() {
		super.resolve();

		logger.debug("Shuffling children...");		

		List<DLNAResource> shuffledChildren = getChildren();
		Collections.shuffle(shuffledChildren);

		// Most DLNA clients sort this list again once they receive it, here 
		// we add the index numbers for the shuffled list
		int i = 1;
		for (DLNAResource res : shuffledChildren) {
			res.setDisplayNamePrefix(i + " ");
			i++;
		}

		setChildren(shuffledChildren);

		resolved = true;
	}
}