package net.pms.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.InputFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H264AnnexBInputStream extends InputStream {
	public static final Logger logger = LoggerFactory.getLogger(H264AnnexBInputStream.class);

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
			//insertHeader = (h[0] == 101 && h[1] == -120/* && (h[2] == -128 || h[2] == -127)*/);
			insertHeader = ((h[0] & 37) == 37 && (h[1] & -120) == -120);
			/*if (insertHeader)
				logger.info("Must insert a header / nextTarget: " + nextTarget);*/
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
			//logger.info("header inserted / nextTarget: " + nextTarget);
			firstHeader = false;
		}
		
		if (h != null) {
			System.arraycopy(h, 0, b, off, 3);
			off += 3;
			//logger.info("frame start inserted");
		}
		
		if (nextTarget < (len - off)) {

			h = getArray(nextTarget);
			if (h == null)
				return -1;
			System.arraycopy(h, 0, b, off, nextTarget);
			//logger.info("Frame copied: " + nextTarget);
			off += nextTarget;
			
			nextTarget = -1;
			
		} else {
			
			h = getArray(len - off);
			if (h == null)
				return -1;
			System.arraycopy(h, 0, b, off, (len - off));
			//logger.info("Frame copied: " + (len - off));
			nextTarget = nextTarget - (len - off);
			off = len;
			
		}

		return off;
	}

	private byte[] getArray(int length) throws IOException {
		if (length < 0) {
			logger.trace("Negative array ?");
			return null;
		}
		byte bb [] = new  byte [length];
		int n=source.read(bb);
		if (n == -1)
			return null;
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
	
		InputFile newInput = new InputFile();
		newInput.filename = "D:\\Tests\\mov\\harry.hdmov";
		byte header [][] = new DLNAMediaInfo().getAnnexBFrameHeader(newInput);
		FileInputStream fis = new FileInputStream("D:\\eclipse3.4\\workspace\\ps3mediaserver\\win32\\harry");
		H264AnnexBInputStream h = new H264AnnexBInputStream(fis, header[1]);
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
