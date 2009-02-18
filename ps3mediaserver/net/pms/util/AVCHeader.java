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
package net.pms.util;

/**
 * @author shagrath
 * Derived from the mpeg4ip project and the ToNMT tool
 */
public class AVCHeader {
	
	private byte buffer [];
	private int currentBit;
	private int profile;
	private int level;
	private int ref_frames;
	
	public AVCHeader(byte buffer[]) {
		this.buffer = buffer;
		currentBit = 0;
	}
	
	public void parse() {
		profile = getValue(8);
		skipBit(8);
		level = getValue(8);
		if (profile == 100 || profile == 110 || profile == 122 || profile == -112) {
			if (getExpGolombCode() == 3)
				getExpGolombCode();
			getExpGolombCode();
			getExpGolombCode();
			getBit();
			if (getBit() == 1) {
				for (int i=0;i<8;i++) {
					int seqScalingListPresentFlag=getBit();
					if (seqScalingListPresentFlag==1) {
						int lastScale=8, nextScale=8;
						int scalingListSize = i<6?16:64;
					    for (int pos=0; pos<scalingListSize; pos++)
					    {
					        if (nextScale!=0)
					        {
					            int deltaScale=getExpGolombCode();
					            nextScale=(lastScale+deltaScale+256)%256;
					        }
					        lastScale=nextScale;
					    }	
					}
				}
			}
			getExpGolombCode();
			int picOrderCntType=getExpGolombCode();
			if (picOrderCntType == 0) {
				getExpGolombCode();
			} else if (picOrderCntType == 1) {
				getBit();
				getExpGolombCode();
				getExpGolombCode();
				int n = getExpGolombCode();
				for(int i=0;i<n;i++)
					getExpGolombCode();
			}
			ref_frames = getExpGolombCode();
		}
	}
	
	private int getBit() {
		int pos = (int) (currentBit / 8);
		int modulo = currentBit % 8;
		currentBit++;
		return (buffer[pos] & (1 << (7-modulo))) >> (7-modulo);
	}
	
	private void skipBit(int number) {
		currentBit += number;
	}
	
	private int getValue(int length) {
		int total = 0;
		for(int i=0;i<length;i++)
			total += getBit() << (length-i-1);
		return total;
	}
	
	private int getExpGolombCode() {
		int bits=0;
		while (getBit()==0) {
			bits++;
		}
		if (bits > 0) {
			return (1 << bits) - 1 + getValue(bits);
		} else {
			return 0;
		}
	}

	public int getProfile() {
		return profile;
	}

	public int getLevel() {
		return level;
	}

	public int getRef_frames() {
		return ref_frames;
	}

}
