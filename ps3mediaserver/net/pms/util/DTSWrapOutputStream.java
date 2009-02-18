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

import net.pms.PMS;

public class DTSWrapOutputStream extends PayloadOutputStream {
	
	private static int bits [] = new int [] { 16, 16, 20, 20, 0, 24, 24 };
	private PCMWrapperOutputStream out;
	private int framesize; 
	
	public DTSWrapOutputStream(PCMWrapperOutputStream out) {
		super(out);
		this.out = out;
		out.swap = false;
		payload_before = false;
		analyze_header_length = 15;
	}

	@Override
	public void analyze_header(byte[] b, int off, int len) {
		if (b[off] == 100 && b[off+1] == 88 && b[off+2] == 32 && b[off+3] == 37) {
			int skip = ((b[framesize+6] & 0xf) << 11) + ((b[framesize+7] & 0xff) << 3) + ((b[framesize+8]) >> 5) + 1;
			blocksize = skip;
			return;
		} else {
			blocksize = framesize;
		}
		
		if (payload != null)
			return;
		framesize = ((b[off+5] & 0x03) << 12) + ((b[off+6] & 0xff) << 4) + ((b[off+7] & 0xf0) >> 4) + 1;
		int bitspersample = ((b[off+11] & 0x01) << 2) + ((b[off+12] & 0xfc) >> 6);
		
		if (b.length > off+framesize + 8) {
			if (b[off+framesize] == 100 && b[off+framesize+1] == 88 && b[off+framesize+2] == 32 && b[off+framesize+3] == 37) {
				PMS.debug("Found DTS HD track");
				//int skip = ((b[off+framesize+6] & 0xf) << 11) + ((b[off+framesize+7] & 0xff) << 3) + ((b[off+framesize+8]) >> 5) + 1;
				//PMS.debug("Analyzing DTSHD stream / skip: " + skip);
			}
		}
		
		blocksize = framesize; // to fit into a dts frame size wrapped into a pcm stream
		if (bitspersample < 7) {
			bitspersample = bits[bitspersample];
			if (bitspersample == 20) // if 20 bits, no padding
				blocksize = -1;
		}
		
		//reset of default values
		int pcm_wrapped_frame_size = 2048; // TODO: DTS 768 kbps / DTS-HD MA / TRUE HD ?
		out.nbchannels = 2;
		out.sampleFrequency = 48000;
		out.bitsperSample = 16;
		out.init();
		
		
		payload = new byte [pcm_wrapped_frame_size-framesize]; // zero padding to respect the frame size of 2048
		for(int i=0;i<payload.length;i++)
			payload[i] = 0;
		PMS.debug("Analyzing DTS stream / framesize: " + framesize);
		PMS.debug("Analyzing DTS stream / bitspersample: " + bitspersample);

	}
	
	public static void main(String args[]) throws Exception {
//		try {
//			PMS.configuration = new PmsConfiguration();
//			PMS.get();
//		} catch (ConfigurationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		FileInputStream fis = new FileInputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\tmp\\24bits.dts");
		//FileInputStream fis = new FileInputStream("H:\\Tests\\m2ts\\dtshd.dts");
		FileOutputStream out = new FileOutputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\tmp\\final.pcm");
		DTSWrapOutputStream h = new DTSWrapOutputStream(new PCMWrapperOutputStream(out, 2, 48000, 16));
		long t1 = System.currentTimeMillis();
		byte b [] = new byte [512*1024];
		int n = -1;
		while ((n=fis.read(b)) > -1) {
			h.write(b, 0, n);
		}
		long t2 = System.currentTimeMillis();
		System.out.println(":" + (t2-t1) +   "- ");
		h.close(); 
		fis.close();
	}

}
