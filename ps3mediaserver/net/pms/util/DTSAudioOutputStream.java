package net.pms.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DTSAudioOutputStream extends FlowParserOutputStream {

	private static int bits [] = new int [] { 16, 16, 20, 20, 0, 24, 24 };
	private boolean dts = false;
	private boolean dtsHD = false;
	private int framesize; 
	private OutputStream out;
	private int padding;
	
	public DTSAudioOutputStream(OutputStream out) {
		super(out, 600000);
		if (out instanceof PCMAudioOutputStream) {
			PCMAudioOutputStream pout = (PCMAudioOutputStream) out;
			pout.swapOrderBits = 0;
		}
		this.out = out;
		neededByteNumber = 15;
	}
	
	@Override
	protected void afterChunkSend() throws IOException {
		padWithZeros(padding);
	}

	@Override
	protected void analyzeBuffer(byte[] data, int off, int len) {
		if (data[off+0] == 100 && data[off+1] == 88 && data[off+2] == 32 && data[off+3] == 37) {
			dtsHD = true;
			streamableByteNumber = ((data[off+6] & 0x0f) << 11) + ((data[off+7] & 0xff) << 3) + ((data[off+8] & 0xf0) >> 5) + 1;
			discard = true;
		} else if (data[off+0] == 127 && data[off+1] == -2 && data[off+2] == -128 && data[off+3] == 1) {
			discard = false;
			dts = true;
			streamableByteNumber = framesize;
			if (framesize == 0) {
				framesize = ((data[off+5] & 0x03) << 12) + ((data[off+6] & 0xff) << 4) + ((data[off+7] & 0xf0) >> 4) + 1;
				int bitspersample = ((data[off+11] & 0x01) << 2) + ((data[off+12] & 0xfc) >> 6);
				streamableByteNumber = framesize;
				//reset of default values
				int pcm_wrapped_frame_size = 2048;
				if (out instanceof PCMAudioOutputStream) {
					PCMAudioOutputStream pout = (PCMAudioOutputStream) out;
					pout.nbchannels = 2;
					pout.sampleFrequency = 48000;
					pout.bitsperSample = 16;
					pout.init();
				}
				padding = pcm_wrapped_frame_size - framesize;
				
				if (bitspersample < 7) {
					bitspersample = bits[bitspersample];
					if (bitspersample == 20) // if 20 bits, no padding
						padding = 0;
				}
	
			}
		} else {
			// E-AC3 / DTS 768 kbps / FULL DTS-HD MA / TRUE HD ???
		}
	}

	@Override
	protected void beforeChunkSend() throws IOException {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		FileInputStream fis = new FileInputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\tmp\\24bits2.dts");
		//FileInputStream fis = new FileInputStream("H:\\Tests\\m2ts\\dtshd.dts");
		FileOutputStream out = new FileOutputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\tmp\\final.pcm");
		DTSAudioOutputStream h = new DTSAudioOutputStream(new PCMAudioOutputStream(out, 2, 48000, 16));
		long t1 = System.currentTimeMillis();
		byte b [] = new byte [512*1024];
		int n = -1;
		while ((n=fis.read(b)) > -1) {
			h.write(b, 0, n);
		}
		long t2 = System.currentTimeMillis();
		System.out.println(":" + (t2-t1) + " - "+ h.count);
		h.close();
		fis.close();
	}

	public boolean isDts() {
		return dts;
	}

	public boolean isDtsHD() {
		return dtsHD;
	}

}
