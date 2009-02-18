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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class PCMWrapperOutputStream extends PayloadOutputStream {
	
	protected int nbchannels;
	protected int sampleFrequency;
	protected int bitsperSample;

	public PCMWrapperOutputStream(OutputStream source, int nbchannels, int sampleFrequency, int bitsperSample) {
		super(source);
		this.nbchannels = nbchannels;
		this.sampleFrequency = sampleFrequency;
		this.bitsperSample = bitsperSample;
		swap = true; // swap byte order by default
		init();
	}
	
	protected void init() {
		blocksize = (2*(int) ((nbchannels+1)/2)) * sampleFrequency * bitsperSample / 1600;
		payload_before = true;
		payload = new byte [4];
		switch( nbchannels ) {
			case 1: payload[2] = 17; break;
			case 2: payload[2] = 49; break;
			case 3: payload[2] = 65; break;
			case 4: payload[2] = 113; break;
			case 5: payload[2] = -127; break;
			case 6: payload[2] = -111; break;
			case 7: payload[2] = -95; break;
			case 8: payload[2] = -79; break;
		}
		payload[0] = (byte) ((blocksize >> 8) & 0xff); 
		payload[1] = (byte) ((blocksize+256)%256);
		if (sampleFrequency == 96000)
			payload[2] = (byte) (payload[2] + 3);
		if (sampleFrequency == 192000)
			payload[2] = (byte) (payload[2] + 4);
		payload[3] = (byte) (16*(bitsperSample-12));
	}

	public static void main(String args[]) throws Exception {
		FileInputStream fis = new FileInputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\win32\\24bits.dts");
		FileOutputStream out = new FileOutputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\win32\\final.pcm");
		PCMWrapperOutputStream h = new PCMWrapperOutputStream(out, 2, 48000, 16);
		byte b [] = new byte [512*1024];
		int n = -1;
		while ((n=fis.read(b)) > -1) {
			h.write(b, 0, n);
		}
		h.close();
		fis.close();
	}

	@Override
	public void analyze_header(byte[] b, int off, int len) {
		// nothing special to do here
		
	}

}
