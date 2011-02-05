package net.pms.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PCMAudioOutputStream extends FlowParserOutputStream {
	
	protected int nbchannels;
	protected int sampleFrequency;
	protected int bitsperSample;
	protected int blocksize;
	protected byte payload [];
	
	public PCMAudioOutputStream(OutputStream source, int nbchannels, int sampleFrequency, int bitsperSample) {
		super(source, 600000);
		this.nbchannels = nbchannels;
		this.sampleFrequency = sampleFrequency;
		this.bitsperSample = bitsperSample;
		swapOrderBits = 2; // swap endian
		init();
	}
	
	protected void init() {
		blocksize = (2*(int) ((nbchannels+1)/2)) * sampleFrequency * bitsperSample / 1600;
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

	@Override
	protected void afterChunkSend() throws IOException {
		
	}

	@Override
	protected void analyzeBuffer(byte[] data, int off, int len) {
		streamableByteNumber = blocksize;
	}

	@Override
	protected void beforeChunkSend() throws IOException {
		writePayload(payload);
	}

	public static void main(String args[]) throws Exception {
		FileInputStream fis = new FileInputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\tmp\\24bits.dts");
		FileOutputStream out = new FileOutputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\tmp\\final.pcm");
		PCMAudioOutputStream h = new PCMAudioOutputStream(out, 2, 48000, 16);
		byte b [] = new byte [512*1024];
		int n = -1;
		while ((n=fis.read(b)) > -1) {
			h.write(b, 0, n);
		}
		h.close();
		fis.close();
	}


}
