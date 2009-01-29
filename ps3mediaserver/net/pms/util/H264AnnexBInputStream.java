package net.pms.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.pms.PMS;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;

public class H264AnnexBInputStream extends InputStream {

	private InputStream source;
	private int nextTarget;
	private boolean firstHeader;
	private byte header[];
	//private int remaining;

	public H264AnnexBInputStream(InputStream source, byte header[]) {
		this.source = source;
		this.header = header;
		firstHeader = true;
		nextTarget = -1;
	}

	@Override
	public int read() throws IOException {
		return -1;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		/*if (remaining > 0) {
			System.arraycopy(header, header.length - remaining, b, off,
					remaining);
			off += remaining;
		}*/
		byte h[] = null;
		boolean insertHeader = false;
		
		if (nextTarget == -1) {
			h = getArray(4);
			if (h == null)
				return -1;
			nextTarget = 65536*256*(h[0]&0xff) + 65536*(h[1]&0xff) + 256*(h[2]&0xff) + (h[3]&0xff);
			h = getArray(3);
			if (h == null)
				return -1;
			insertHeader = (h[0] == 101 && h[1] == -120/* && (h[2] == -128 || h[2] == -127)*/);
			/*if (insertHeader)
				PMS.minimal("Must insert a header / nextTarget: " + nextTarget);*/
			if (!insertHeader) {
				System.arraycopy(new  byte [] { 0, 0, 0, 1 }, 0, b, off, 4);
				off += 4;
				
			}
			nextTarget = nextTarget-3;
		}
		
		if (nextTarget == -1)
			return -1;

		if (insertHeader) {
			byte defHeader [] = header;
			if (!firstHeader) {
				defHeader = new byte [header.length+1];
				System.arraycopy(header, 0, defHeader, 0, header.length);
				defHeader[defHeader.length-1] = 1;
				defHeader[defHeader.length-2] = 0;
			}
			if (defHeader.length < (len - off)) {
				System.arraycopy(defHeader, 0, b, off, defHeader.length);
				off += defHeader.length;
			} else {
				//remaining = defHeader.length - (len - off);
				System.arraycopy(defHeader, 0, b, off, (len - off));
				off = len;
			}
			//PMS.minimal("header inserted / nextTarget: " + nextTarget);
			firstHeader = false;
		}
		
		if (h != null) {
			System.arraycopy(h, 0, b, off, 3);
			off += 3;
			//PMS.minimal("frame start inserted");
		}
		
		if (nextTarget < (len - off)) {

			h = getArray(nextTarget);
			if (h == null)
				return -1;
			System.arraycopy(h, 0, b, off, nextTarget);
			//PMS.minimal("Frame copied: " + nextTarget);
			off += nextTarget;
			
			nextTarget = -1;
			
		} else {
			
			h = getArray(len - off);
			if (h == null)
				return -1;
			System.arraycopy(h, 0, b, off, (len - off));
			//PMS.minimal("Frame copied: " + (len - off));
			nextTarget = nextTarget - (len - off);
			off = len;
			
		}

		return off;
	}

	private byte[] getArray(int length) throws IOException {
		if (length < 0) {
			PMS.debug("Negative array ?");
			return null;
		}
		byte bb [] = new  byte [length];
		int n=source.read(bb);
		while (n < length) {
			int u = source.read(bb, n, length-n);
			if (u == -1) {
				break;
			}
			n += u;
		}
		return bb;
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (source != null)
			source.close();
	}
	
	public static byte [] getAnnexBFrameHeader(String f) {
		String cmdArray [] = new String [14];
		cmdArray[0] = PMS.getConfiguration().getFfmpegPath();
		//cmdArray[0] = "win32/ffmpeg.exe";
		cmdArray[1] = "-vframes";
		cmdArray[2] = "1";
		cmdArray[3] = "-i";
		cmdArray[4] = f;
		cmdArray[5] = "-vcodec";
		cmdArray[6] = "copy";
		cmdArray[7] = "-f";
		cmdArray[8] = "h264";
		cmdArray[9] = "-vbsf";
		cmdArray[10] = "h264_mp4toannexb";
		cmdArray[11] = "-an";
		cmdArray[12] = "-y";
		cmdArray[13] = "pipe:";
		/*
		try {
			PMS.configuration = new PmsConfiguration();
			PMS.get();
		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	*/
		OutputParams params = new OutputParams(PMS.getConfiguration());
		params.maxBufferSize = 1;
		
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		pw.run();
		
		InputStream is = null;
		ByteArrayOutputStream baot = new ByteArrayOutputStream();
		try {
			is = pw.getInputStream(0);
			byte b [] = new byte [4096];
			int n= -1;
			while ((n = is.read(b)) > 0) {
				baot.write(b, 0, n);
			}
			byte data [] = baot.toByteArray();
			baot.close();
			
			is.close();

			int kf = 0;
			for(int i=2;i<data.length;i++) {
				if (data[i-2] == 101 && data[i-1] == -120/* && (data[i] == -128 || data[i] == -127)*/) {
					kf = i - 2;
					break;
				}
			}
			int st = 0;
			if (kf > 0) {
				for(int i=kf;i>=5;i--) {
					if (data[i-5] == 0 && data[i-4] == 0 && data[i-3] == 0 && data[i-2] == 1 && data[i-1] == 103 && data[i] == 100) {
						st = i-5;
						break;
					}
				}
			}
			if (st >= 0) {
				byte header [] = new byte [kf - st];
				System.arraycopy(data, st, header, 0, kf-st);
				return header;
			}
		} catch (IOException e) {
			
		}
		return null;
	}
	
	public static void main(String args[]) throws Exception {
		byte header [] = getAnnexBFrameHeader(null);
		FileInputStream fis = new FileInputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\win32\\raw.h264");
		H264AnnexBInputStream h = new H264AnnexBInputStream(fis, header);
		FileOutputStream out = new FileOutputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\win32\\raw_new.h264");
		byte b [] = new byte [512*1024];
		int n = -1;
		while ((n=h.read(b)) > -1) {
			out.write(b, 0, n);
		}
		out.close();
		h.close();
	}

}
