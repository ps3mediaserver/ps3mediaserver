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
package net.pms.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import net.pms.PMS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferedOutputFile extends OutputStream  {
	private static final Logger logger = LoggerFactory.getLogger(BufferedOutputFile.class);

	private static final int TEMP_SIZE = 50000000;
	private static final int CHECK_INTERVAL = 500;
	private static final int CHECK_END_OF_PROCESS = 2500; // must be superior to CHECK_INTERVAL
	private int minMemorySize;
	private int maxMemorySize;
	private int bufferOverflowWarning;
	private boolean eof;
	private long writeCount;
	private byte buffer [];
	private boolean forcefirst = (PMS.getConfiguration().getTrancodeBlocksMultipleConnections() && PMS.getConfiguration().getTrancodeKeepFirstConnections());
	private ArrayList<WaitBufferedInputStream> inputStreams;
	private ProcessWrapper attachedThread;
	private int secondread_minsize;
	private Timer timer;
	private boolean shiftScr;
	private FileOutputStream debugOutput = null;
	private boolean buffered = false;
	private DecimalFormat formatter = new DecimalFormat("#,###");
	private double timeseek;
	private double timeend;
	// private int scr;
	private long packetpos = 0;
	
	class WaitBufferedInputStream extends InputStream {
		private BufferedOutputFile outputStream;
		private long readCount;
		private boolean firstRead;
		
		WaitBufferedInputStream(BufferedOutputFile outputStream) {
			this.outputStream = outputStream;
			firstRead = true;
		}

		public int read() throws IOException {
			int r = outputStream.read(firstRead, readCount);
			if (r != -1)
				readCount++;
			firstRead = false;
			return r;
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int returned = outputStream.read(firstRead, readCount, b, off, len);
			if (returned != -1)
				readCount += returned;
			firstRead = false;
			return returned;
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}

		public int available() throws IOException {
			return (int) outputStream.writeCount;
		}
		
		public void close() throws IOException {
			inputStreams.remove(this);
			outputStream.detachInputStream();
		}
	}

	public BufferedOutputFile(OutputParams params) {
		this.minMemorySize = (int) (1048576 * params.minBufferSize);
		this.maxMemorySize = (int) (1048576 * params.maxBufferSize);
		int margin = 20000000; // Issue 220: extends to 20Mb : readCount is wrongly set cause of the ps3's
								// 2nd request with a range like 44-xxx, causing the end of buffer margin to be first sent 
		if (this.maxMemorySize < margin) {// for thumbnails / small buffer usage
			margin = 2000000; // margin must be superior to the buffer size of OutputBufferConsumer or direct buffer size from WindowsNamedPipe class
			if (this.maxMemorySize < margin)
				margin = 600000;
		}
		this.bufferOverflowWarning = this.maxMemorySize - margin;
		this.secondread_minsize = params.secondread_minsize;
		this.timeseek = params.timeseek;
		this.timeend = params.timeend;
		this.shiftScr = params.shift_scr;
		try {
			buffer = new byte [this.maxMemorySize<TEMP_SIZE?this.maxMemorySize:TEMP_SIZE];
		} catch (OutOfMemoryError ooe) {
			logger.info("FATAL ERROR: OutOfMemory / dumping stats");
			logger.trace("freeMemory: " + Runtime.getRuntime().freeMemory());
			logger.trace("totalMemory: " + Runtime.getRuntime().totalMemory());
			logger.trace("maxMemory: " + Runtime.getRuntime().maxMemory());
			System.exit(1);
		}
		inputStreams = new ArrayList<WaitBufferedInputStream>();
		timer = new Timer();
		if (params.maxBufferSize > 15 && !params.hidebuffer) {
			timer.schedule(new TimerTask() {
				public void run() {
					long rc = 0;
					if (getCurrentInputStream() != null) {
						rc = getCurrentInputStream().readCount;
						PMS.get().getFrame().setReadValue(rc, "");
					}
					long space = (writeCount - rc);
					logger.trace("buffered: " + formatter.format(space) + " bytes / inputs: " + inputStreams.size());
					PMS.get().getFrame().setValue( (int) (100*space/maxMemorySize), formatter.format(space) + " bytes");
				}
			}, 0, 2000);
		}
	}

	public void close() throws IOException {
		logger.trace("EOF");
		eof = true;
	}
	
	public WaitBufferedInputStream getCurrentInputStream() {
		WaitBufferedInputStream wai = null;

		if (inputStreams.size() > 0) {
			try {
				wai = forcefirst ? inputStreams.get(0) : inputStreams.get(inputStreams.size() - 1);
			} catch (IndexOutOfBoundsException e) {
				// this should never happen unless there's a concurrency issue,
				// so log it if it does
				logger.error("Unexpected input stream removal", e); 
			}
		}

		return wai;
	}
	
	public InputStream getInputStream(long newReadPosition) {
		if (attachedThread != null) {
			attachedThread.setReadyToStop(false);
		}
		WaitBufferedInputStream atominputStream = null;
		if (!PMS.getConfiguration().getTrancodeBlocksMultipleConnections() || getCurrentInputStream() == null) {
			atominputStream = new WaitBufferedInputStream(this);
			inputStreams.add(atominputStream);
 		} else {
			if (PMS.getConfiguration().getTrancodeKeepFirstConnections()) {
				logger.debug("BufferedOutputFile is already attached to an InputStream: " + getCurrentInputStream());
			} else {
				// Ditlew - fixes the above (the above iterator breaks on items getting close, cause they will remove them self from the arraylist)
				while(inputStreams.size() > 0) {
					try {
						((WaitBufferedInputStream)inputStreams.get(0)).close();
					} catch (IOException e) {
						logger.error("Error: ", e);
					}
				}				
				
				inputStreams.clear();
				atominputStream = new WaitBufferedInputStream(this);
				inputStreams.add(atominputStream);
				logger.debug("Reassign inputstream: " + getCurrentInputStream());
			}
			return null;
		}
		if (newReadPosition > 0) {
			logger.debug("Setting InputStream new position to: " + newReadPosition);
			atominputStream.readCount = newReadPosition;
		}
		return atominputStream;
	}

	public long getWriteCount() {
		return writeCount;
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (debugOutput != null) {
			debugOutput.write(b, off, len);
			debugOutput.flush();
		}
		WaitBufferedInputStream input = getCurrentInputStream();
		 while ((input !=null && (writeCount - input.readCount > bufferOverflowWarning)) || (input == null && writeCount > bufferOverflowWarning)) {
				try {
					Thread.sleep(CHECK_INTERVAL);
				} catch (InterruptedException e) {}
				input = getCurrentInputStream();
			}
		 int mb = (int) (writeCount % maxMemorySize);
		 if (buffer != null) {
			 if (mb>=buffer.length - (len - off)) {
				 if (buffer.length == TEMP_SIZE) {
						logger.trace("freeMemory: " + Runtime.getRuntime().freeMemory());
						logger.trace("totalMemory: " + Runtime.getRuntime().totalMemory());
						logger.trace("maxMemory: " + Runtime.getRuntime().maxMemory());
						logger.trace("Extending buffer to " + maxMemorySize);
						
						try {
							//buffer = Arrays.copyOf(buffer, maxMemorySize);
							byte [] copy = new byte[maxMemorySize];
					       try {
					    	   System.arraycopy(buffer, 0, copy, 0, Math.min(buffer!=null?buffer.length:0, maxMemorySize));
					    	   buffer = copy;
					       } catch (NullPointerException npe) {
					    	   return;
					       }
					       
						} catch (OutOfMemoryError ooe) {
							logger.info("FATAL ERROR: OutOfMemory / dumping stats");
							logger.trace("freeMemory: " + Runtime.getRuntime().freeMemory());
							logger.trace("totalMemory: " + Runtime.getRuntime().totalMemory());
							logger.trace("maxMemory: " + Runtime.getRuntime().maxMemory());
							//System.exit(1);
							logger.info("Not enough memory to allocate " + maxMemorySize + " bytes... Using half of it");
							maxMemorySize = maxMemorySize/2;
							byte [] copy = new byte[maxMemorySize];
					       try {
					    	   System.arraycopy(buffer, 0, copy, 0, Math.min(buffer!=null?buffer.length:0, maxMemorySize));
					    	   buffer = copy;
					       } catch (NullPointerException npe) {
					    	   return;
					       }
						}
						logger.trace("Done extending");
					}
				 int s = (len - off);
				for (int i = 0 ; i < s; i++) {
				   buffer[modulo(mb+i)] = b[off+i];
				}
			 } else {
				 System.arraycopy(b, off, buffer,mb  , (len - off));
				 if ((len - off) > 0)
				     buffered = true;
			 }
			
			// Ditlew - WDTV Live
			if (timeseek > 0 && writeCount > 10) {
				for (int i = 0; i < len; i++) {
					if (buffer != null && shiftScr)
						shiftSCRByTimeSeek(mb+i, (int)timeseek); // Ditlew - update any SCR headers
					//shiftGOPByTimeSeek(mb+i, (int)timeseek); // Ditlew - update any GOP headers - Not needed for WDTV Live
				}
			}

			 writeCount += len - off;
			 if (timeseek > 0 && timeend == 0) {
				 int packetLength = 6; // minimum to get packet size
				 while (packetpos+packetLength < writeCount && buffer != null) {
					 int packetposMB = (int) (packetpos % maxMemorySize);
					 int streamPos = 0;
					 if (buffer[modulo(packetposMB)] == 71)  {// TS
						 packetLength = 188;
						 streamPos = 4;
	
						 // adaptation field
						 if ((buffer[modulo(packetposMB+3)] & 0x20) == 0x20)
							 streamPos += 1 + ((buffer[modulo(packetposMB+4)]+256)%256);
						 
						 if (streamPos == 188)
							 streamPos = -1;
						
					 } else if (buffer[modulo(packetposMB+3)] == -70) { // BA
						 packetLength = 14;
						 streamPos = -1;
					 } else {
						 packetLength = 6 + ((int) ((buffer[modulo(packetposMB+4)]+256)%256))*256 + ((buffer[modulo(packetposMB+5)]+256)%256);
					 }
					 if (streamPos != -1) {
						 mb = packetposMB + streamPos + 18;						 
						 if (!shiftVideo(mb, true )) {
							 mb = mb-5;
							 shiftAudio(mb, true);
						 }
					 }
					 packetpos+= packetLength;
				} 
			}		 
		}
	}
	
	private int modulo(int mb) {
		if (mb >= 0)
			return mb%maxMemorySize;
		return (mb + maxMemorySize) % maxMemorySize;
	}
	
	public void write(int b) throws IOException {
		boolean bb = b % 100000 == 0;
		WaitBufferedInputStream input = getCurrentInputStream() ;
		while (bb && ((input != null && (writeCount - input.readCount > bufferOverflowWarning)) || (input == null && writeCount == bufferOverflowWarning))) {
			try {
				Thread.sleep(CHECK_INTERVAL);
				//logger.trace("BufferedOutputFile Full");
			} catch (InterruptedException e) {}
			input = getCurrentInputStream();
		}
		int mb = (int) (writeCount++ % maxMemorySize);
		if (buffer != null) {
			buffer[mb] = (byte) b;
			buffered = true;
			if (writeCount == TEMP_SIZE) {
				logger.trace("freeMemory: " + Runtime.getRuntime().freeMemory());
				logger.trace("totalMemory: " + Runtime.getRuntime().totalMemory());
				logger.trace("maxMemory: " + Runtime.getRuntime().maxMemory());
				logger.trace("Extending buffer to " + maxMemorySize);
				
				try {
					//buffer = Arrays.copyOf(buffer, maxMemorySize);
					byte[] copy = new byte[maxMemorySize];
			        System.arraycopy(buffer, 0, copy, 0,
			                         Math.min(buffer.length, maxMemorySize));
			        buffer = copy;
				} catch (OutOfMemoryError ooe) {
					logger.info("FATAL ERROR: OutOfMemory / dumping stats");
					logger.trace("freeMemory: " + Runtime.getRuntime().freeMemory());
					logger.trace("totalMemory: " + Runtime.getRuntime().totalMemory());
					logger.trace("maxMemory: " + Runtime.getRuntime().maxMemory());
					System.exit(1);
				}
			}
			
			if (timeseek > 0 && writeCount > 19) 
				shiftByTimeSeek(mb, mb <= 20);
			
			// Ditlew - Update any GOP headers - Not needed by WDTV Live
			//if (timeseek > 0 && writeCount > 8)
				//shiftGOPByTimeSeek(mb, (int)timeseek);

			// Ditlew - WDTV Live - update any SCR headers
			if (timeseek > 0 && writeCount > 10)
				shiftSCRByTimeSeek(mb, (int)timeseek);
		}
	}

	// Ditlew - Modify SCR
	private void shiftSCRByTimeSeek(int buffer_index, int offset_sec) {
		int m9 = modulo(buffer_index - 9);
		int m8 = modulo(buffer_index - 8);
		int m7 = modulo(buffer_index - 7);
		int m6 = modulo(buffer_index - 6);
		int m5 = modulo(buffer_index - 5);
		int m4 = modulo(buffer_index - 4);
		int m3 = modulo(buffer_index - 3);
		int m2 = modulo(buffer_index - 2);
		int m1 = modulo(buffer_index - 1);
		int m0 = modulo(buffer_index);

		// SCR
		if (
		    buffer[m9] == 0 &&
		    buffer[m8] == 0 &&
		    buffer[m7] == 1 &&
		    buffer[m6] == -70 && // 0xBA - Java/PMS wants -70
		    // control bits
			 !((buffer[m5] & 128) == 128) &&
				((buffer[m5] & 64) == 64) &&
				((buffer[m5] & 4) == 4) &&
				((buffer[m3] & 4) == 4) &&
				((buffer[m1] & 4) == 4) &&
				((buffer[m0] & 1) == 1)
		)
		{
			long scr_32_30 = ((buffer[m5] & 56) >> 3);
			long scr_29_15 = ((buffer[m5] & 3) << 13) + (buffer[m4] << 5) + ((buffer[m3] & 248) >> 3);
			long scr_14_00 = ((buffer[m3] & 3) << 13) + (buffer[m2] << 5) + ((buffer[m1] & 248) >> 3);
			
			long scr = (scr_32_30 << 30) + (scr_29_15 << 15) + scr_14_00;
			long scr_new = scr + (offset_sec * 90000);
			
			long scr_32_30_new = (scr_new & 7516192768L) >> 30;  // 111000000000000000000000000000000
			long scr_29_15_new = (scr_new & 1073709056L) >> 15;  // 000111111111111111000000000000000
			long scr_14_00_new = (scr_new & 32767L);             // 000000000000000000111111111111111
			
			// scr_32_30_new
			buffer[m5] = (byte)((buffer[m5] & 199) + ((scr_32_30_new << 3) & 56)); // 11000111
			
			// scr_29_15_new
			buffer[m5] = (byte)((buffer[m5] & 252) + ((scr_29_15_new >> 13) & 3)); // 00000011
			buffer[m4] = (byte)(scr_29_15_new >> 5);                               // 11111111
			buffer[m3] = (byte)((buffer[m3] & 7) + ((scr_29_15_new << 3) & 248));  // 11111000
			
			// scr_14_00_new
			buffer[m3] = (byte)((buffer[m3] & 252) + ((scr_14_00_new >> 13) & 3)); // 00000011
			buffer[m2] = (byte)(scr_14_00_new >> 5);                               // 11111111
			buffer[m1] = (byte)((buffer[m1] & 7) + ((scr_14_00_new << 3) & 248));  // 11111000

			// Debug
			//logger.trace("Ditlew - SCR "+scr+" ("+(int)(scr/90000)+") -> "+scr_new+" ("+(int)(scr_new/90000)+")  "+offset_sec+" secs");
		}
	}

	// Ditlew - Modify GOP
	@SuppressWarnings("unused")
	private void shiftGOPByTimeSeek(int buffer_index, int offset_sec) {
		int m7 = modulo(buffer_index - 7);
		int m6 = modulo(buffer_index - 6);
		int m5 = modulo(buffer_index - 5);
		int m4 = modulo(buffer_index - 4);
		int m3 = modulo(buffer_index - 3);
		int m2 = modulo(buffer_index - 2);
		int m1 = modulo(buffer_index - 1);
		int m0 = modulo(buffer_index);
				
		// check if valid gop
		if (
				buffer[m7] == 0 &&
				buffer[m6] == 0 &&
				buffer[m5] == 1 &&
				buffer[m4] == -72 && // 0xB8 - Java/PMS wants -72
				// control bits
			((buffer[m2] & 0x08) == 0x08) &&
			((buffer[m0] & 31) == 0) &&
				// of interest
		 !((buffer[m3] & 128) == 128) && // not drop frm
		 !((buffer[m0] & 16) == 16) // not broken
		)
		{
			// org timecode
			byte h = (byte)((buffer[m3] & 124) >> 2);
			byte m = (byte)(((buffer[m3] & 3) << 4) + ((buffer[m2] & 240) >> 4));
			byte s = (byte)(((buffer[m2] & 7) << 3) + ((buffer[m1] & 224) >> 5));
			
			// updated offset
			int _offset = s + m * 60 + h * 60 + offset_sec;
			
			// new timecode
			byte _h = (byte)((int)(_offset / 3600) % 24);
			byte _m = (byte)((int)(_offset / 60) % 60);
			byte _s = (byte)(_offset % 60);
			
			// update gop
			// h - ok
			buffer[m3] = (byte)((buffer[m3] & 131) + (_h << 2)); // 10000011
			// m - ok
			buffer[m3] = (byte)((buffer[m3] & 252) + (_m >> 4)); // 11111100
			buffer[m2] = (byte)((buffer[m2] & 15)  + (_m << 4)); // 00001111
			// s - ok
			buffer[m2] = (byte)((buffer[m2] & 248) + (_s >> 3)); // 11111000
			buffer[m1] = (byte)((buffer[m1] & 31)  + (_s << 5)); // 00011111

			// Debug
			//logger.trace("Ditlew - GOP "+h+":"+m+":"+s+" -> "+_h+":"+_m+":"+_s+"  "+offset_sec+" secs");
		}
	}
	
	private void shiftByTimeSeek(int mb, boolean mod) {
		shiftVideo(mb, mod);
		shiftAudio(mb, mod);
	}
	
	private boolean shiftAudio(int mb, boolean mod) {
		boolean bb = (!mod && (buffer[mb-10] == -67 || buffer[mb-10] == -64) && buffer[mb-11] == 1 && buffer[mb-12] == 0 && buffer[mb-13] == 0 && /*(buffer[mb-7]&128)==128 &&*/ (buffer[mb-6]&128)==128/*buffer[mb-6] == -128*/)
		|| (mod && (buffer[modulo(mb-10)] == -67  || buffer[modulo(mb-10)] == -64) && buffer[modulo(mb-11)] == 1 && buffer[modulo(mb-12)] == 0 && buffer[modulo(mb-13)] == 0 && /*(buffer[modulo(mb-7)]&128)==128 && */(buffer[modulo(mb-6)]&128)==128/*buffer[modulo(mb-6)] == -128*/);
		if (bb) {
			int pts = (((((buffer[modulo(mb-3)]&0xff)<<8) + (buffer[modulo(mb-2)]&0xff))>>1)<<15) + ((((buffer[modulo(mb-1)]&0xff)<<8) + (buffer[modulo(mb)]&0xff))>>1);
			pts += (int) (timeseek*90000);
			
			setTS(pts, mb, mod);
			return true;
		}
		return false;
	}

	private boolean shiftVideo(int mb, boolean mod) {
		boolean bb = (
			!mod &&
		 (buffer[mb-15] == -32 || buffer[mb-15] == -3) &&
			buffer[mb-16] == 1 && 
			buffer[mb-17] == 0 && 
			buffer[mb-18] == 0 &&
		 (buffer[mb-11]&128)==128 && 
		 (buffer[mb-9]&32)==32
		 ) || (
		  mod &&
		 (buffer[modulo(mb-15)] == -32 || buffer[modulo(mb-15)] == -3) && 
		  buffer[modulo(mb-16)] == 1 && 
		  buffer[modulo(mb-17)] == 0 && 
		  buffer[modulo(mb-18)] == 0 && 
		 (buffer[modulo(mb-11)]&128)==128 && 
		 (buffer[modulo(mb-9)]&32)==32);
		 
		if (bb) { // check EO or FD (tsmuxer)
			int pts = getTS(mb-5, mod);
			int dts = 0;
			boolean dts_present = (buffer[modulo(mb-11)]&64)==64;
			if (dts_present) {
				if ((buffer[modulo(mb-4)]&15)==15) {
					dts = (((((255-(buffer[modulo(mb-3)]&0xff))<<8) + (255-(buffer[modulo(mb-2)]&0xff)))>>1)<<15) + ((((255-(buffer[modulo(mb-1)]&0xff))<<8) + (255-(buffer[modulo(mb)]&0xff)))>>1);
					dts = -dts;
				} else {
					dts = getTS(mb, mod);
				}
			}
			
			int ts = (int) (timeseek*90000);
			if (mb == 50 && writeCount < maxMemorySize) {
				/*scr = ts;
				scr = scr - (pts-dts);*/
				dts--;
			}
			pts += ts;
			
			setTS(pts, mb-5, mod);
			if (dts_present) {
				if (dts < 0)
					buffer[modulo(mb-4)] = 17;
				dts += ts;
				setTS(dts, mb, mod);
			}
			
			return true;
		}
		return false;
	}
	
	private int getTS(int mb, boolean modulo) {
		int m3 = mb - 3;
		int m2 = mb - 2;
		int m1 = mb - 1;
		int m0 = mb;
		if (modulo) {
			m3 = modulo(m3);
			m2 = modulo(m2);
			m1 = modulo(m1);
			m0 = modulo(m0);
		}
		
		return
			(((((buffer[m3] & 0xff) << 8) + (buffer[m2] & 0xff)) >> 1) << 15)
			+((((buffer[m1] & 0xff) << 8) + (buffer[m0] & 0xff)) >> 1);
	}

	private void setTS(int ts, int mb, boolean modulo) {
		int m3 = mb - 3;
		int m2 = mb - 2;
		int m1 = mb - 1;
		int m0 = mb;
		if (modulo) {
			m3 = modulo(m3);
			m2 = modulo(m2);
			m1 = modulo(m1);
			m0 = modulo(m0);
		}
		int pts_low = ts & 32767;
		int pts_high = (ts >> 15) & 32767;
		int pts_left_low = 1 + (pts_low << 1);
		int pts_left_high = 1 + (pts_high << 1);
		buffer[m3] = (byte) ((pts_left_high & 65280) >> 8);
		buffer[m2] = (byte) (pts_left_high & 255);
		buffer[m1] = (byte) ((pts_left_low & 65280) >> 8);
		buffer[m0] = (byte) (pts_left_low & 255);
	}
	
	private int read(boolean firstRead, long readCount, byte buf [], int off, int len) {
		if (readCount > TEMP_SIZE && readCount < maxMemorySize) {
			int newMargin = maxMemorySize - 2000000;
			if (bufferOverflowWarning != newMargin)
				logger.debug("Setting margin to 2Mb");
			this.bufferOverflowWarning = newMargin;
		}
		if (eof) {
			if (readCount >= writeCount)
				return -1;
		}
		int c = 0;
		int minBufferS = firstRead?minMemorySize:secondread_minsize;
		while (writeCount - readCount <= minBufferS && !eof && c < 15) {
			if (c == 0)
				logger.trace("Suspend Read: readCount=" + readCount + " / writeCount=" + writeCount);
			c++;
			try {
				Thread.sleep(CHECK_INTERVAL);
			} catch (InterruptedException e) {}
		}
		if (attachedThread != null) {
			attachedThread.setReadyToStop(false);
		}
		if (c > 0)
			logger.trace("Resume Read: readCount=" + readCount + " / writeCount=" + writeCount);

		if (buffer == null || !buffered)
			return -1;
		
		int mb = (int) (readCount % maxMemorySize);
		int endOF = buffer.length;
		int cut = 0;
		if (eof) {
			if ((writeCount - readCount) < len) {
				cut = (int) (len- (writeCount - readCount));
			}
		}
		/*if (eof)
			endOF =(int) (writeCount % maxMemorySize);*/
		if (mb>=endOF - len) {
			System.arraycopy(buffer, mb, buf, off, endOF-mb-cut);
			return endOF-mb;
		} else {
			System.arraycopy(buffer, mb, buf, off, len-cut);
			return len;
		}
	}
	
	private int read(boolean firstRead, long readCount) {
		if (readCount > TEMP_SIZE && readCount < maxMemorySize) {
			int newMargin = maxMemorySize - 2000000;
			if (bufferOverflowWarning != newMargin)
				logger.debug("Setting margin to 2Mb");
			this.bufferOverflowWarning = newMargin;
		}
		if (eof && readCount >= writeCount)
			return -1;
		int c = 0;
		int minBufferS = firstRead?minMemorySize:secondread_minsize;
		while (writeCount - readCount <= minBufferS && !eof && c < 15) {
			if (c == 0)
				logger.trace("Suspend Read: readCount=" + readCount + " / writeCount=" + writeCount);
			c++;
			try {
				Thread.sleep(CHECK_INTERVAL);
			} catch (InterruptedException e) {}
		}
		if (attachedThread != null) {
			attachedThread.setReadyToStop(false);
		}

		if (c > 0)
			logger.trace("Resume Read: readCount=" + readCount + " / writeCount=" + writeCount);

		if (buffer == null || !buffered)
			return -1;
		return 0xff & buffer[(int) (readCount % maxMemorySize)];
	}
	
	public void attachThread(ProcessWrapper thread) {
		if (attachedThread != null)
			throw new RuntimeException("BufferedOutputFile is already attached to a Thread: " + attachedThread);
		logger.debug("Attaching thread: " + thread);
		attachedThread = thread;
	}
	
	private void detachInputStream() {
		PMS.get().getFrame().setReadValue(0, "");
		if (attachedThread != null) {
			attachedThread.setReadyToStop(true);
		}
		Runnable checkEnd = new Runnable() {
			public void run() {
				try {
					Thread.sleep(CHECK_END_OF_PROCESS);
				} catch (InterruptedException e) {
					logger.error(null, e);
				}
				if (attachedThread != null && attachedThread.isReadyToStop()) {
					if (!attachedThread.isDestroyed()) {
						attachedThread.stopProcess();
					}
					reset();
				}
			}
		};
		new Thread(checkEnd).start();
	}

	public synchronized void reset() {
		if (debugOutput != null)
			try {
				debugOutput.close();
			} catch (IOException e) {}
		timer.cancel();
		if (buffer != null) {
			logger.debug("Destroying buffer");
			buffer = null;
		}
		buffered = false;
		// System.gc();
		if (maxMemorySize != 1048576) {
			PMS.get().getFrame().setValue(0, "Empty");
		}
	}
}
