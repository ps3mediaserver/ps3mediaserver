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
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import net.pms.PMS;

public class BufferedOutputFile extends OutputStream  {
	
	class WaitBufferedInputStream extends InputStream {
		
		

		
		private BufferedOutputFile outputStream;
		private long readCount;
		private boolean firstRead;
		
		WaitBufferedInputStream(BufferedOutputFile outputStream) {
			this.outputStream = outputStream;
			firstRead = true;
		}

		public int read() throws IOException {
			int r = outputStream.read(firstRead, readCount++);
			firstRead = false;
			return r;
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			int returned = outputStream.read(firstRead, readCount, b);
			if (returned != -1)
				readCount += returned;
			firstRead = false;
			return returned;
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return read(b);
		}

		
		public int available() throws IOException {
			return (int) outputStream.writeCount;
		}
		
		public void close() throws IOException {
			inputStreams.remove(this);
			outputStream.detachInputStream();
		}

	}
	
	private static final int TEMP_SIZE = 50000000;
	private static final int CHECK_INTERVAL = 500;
	private static final int CHECK_END_OF_PROCESS = 2500; // must be superior to CHECK_INTERVAL
	private int minMemorySize;
	private int maxMemorySize;
	private int bufferOverflowWarning;
	private boolean eof;
	private long writeCount;
	private byte buffer [];
	
	private ArrayList<WaitBufferedInputStream> inputStreams;
	private ProcessWrapper attachedThread;
	private int secondread_minsize;
	private Timer timer;
	
	private FileOutputStream debugOutput = null;
	
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
		try {
			buffer = new byte [this.maxMemorySize<TEMP_SIZE?this.maxMemorySize:TEMP_SIZE];
		} catch (OutOfMemoryError ooe) {
			PMS.minimal("FATAL ERROR: OutOfMemory / dumping stats");
			PMS.debug("freeMemory: " + Runtime.getRuntime().freeMemory());
			PMS.debug("totalMemory: " + Runtime.getRuntime().totalMemory());
			PMS.debug("maxMemory: " + Runtime.getRuntime().maxMemory());
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
				PMS.debug("Buffered Space: " + space + " bytes / inputs: " + inputStreams.size());
				PMS.get().getFrame().setValue( (int) (100*space/maxMemorySize), space + " bytes");

			}
			
		}, 0, 2000);
		}
		
		/*try {
			debugOutput = new FileOutputStream("debug.mpg");
		} catch (Exception e) {}*/
	}

	public void close() throws IOException {
		PMS.debug("EOF");
		eof = true;
	}
	
	public WaitBufferedInputStream getCurrentInputStream() {
		if (inputStreams.size() > 0)
			return inputStreams.get(0);
		else
			return null;
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
			PMS.info("BufferedOutputFile is already attached to an InputStream: " + getCurrentInputStream());
			return null;
		}
		if (newReadPosition > 0) {
			PMS.info("Setting InputStream new position to: " + newReadPosition);
			atominputStream.readCount = newReadPosition;
		}
		return atominputStream;
	}

	public long getWriteCount() {
		return writeCount;
	}
	
	private double timeseek;
	//private int scr;
	
	private long packetpos = 0;
	
	public void write(byte b[], int off, int len) throws IOException {

		if (debugOutput != null) {
			debugOutput.write(b, off, len);
			debugOutput.flush();
		}
		//PMS.debug("Writing " + len + " into the buffer");
		 while ((getCurrentInputStream() !=null && (writeCount - getCurrentInputStream().readCount > bufferOverflowWarning)) || (getCurrentInputStream() == null && writeCount > bufferOverflowWarning)) {
				try {
					Thread.sleep(CHECK_INTERVAL);
				} catch (InterruptedException e) {
				}
			}
		 int mb = (int) (writeCount % maxMemorySize);
		 if (buffer != null) {
			 if (mb>=buffer.length - (len - off)) {
				 if (buffer.length == TEMP_SIZE) {
						PMS.debug("freeMemory: " + Runtime.getRuntime().freeMemory());
						PMS.debug("totalMemory: " + Runtime.getRuntime().totalMemory());
						PMS.debug("maxMemory: " + Runtime.getRuntime().maxMemory());
						PMS.debug("Extending buffer to " + maxMemorySize);
						
						try {
							//buffer = Arrays.copyOf(buffer, maxMemorySize);
							byte[] copy = new byte[maxMemorySize];
					       try {
					    	   System.arraycopy(buffer, 0, copy, 0, Math.min(buffer!=null?buffer.length:0, maxMemorySize));
					    	   buffer = copy;
					       } catch (NullPointerException npe) {
					    	   return;
					       }
					       
						} catch (OutOfMemoryError ooe) {
							PMS.minimal("FATAL ERROR: OutOfMemory / dumping stats");
							PMS.debug("freeMemory: " + Runtime.getRuntime().freeMemory());
							PMS.debug("totalMemory: " + Runtime.getRuntime().totalMemory());
							PMS.debug("maxMemory: " + Runtime.getRuntime().maxMemory());
							System.exit(1);
						}
						PMS.debug("Done extending");
					}
				 int s = (len - off);
				for (int i = 0 ; i < s; i++) {
				   buffer[modulo(mb+i)] = b[off+i];
				}
			 } else {
				 
				 System.arraycopy(b, off, buffer,mb  , (len - off));
				 
	
			 }
			 
			 writeCount += len - off;
			 if (timeseek > 0) {
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
		return (mb + maxMemorySize) % maxMemorySize;
	}
	
	
	public void write(int b) throws IOException {
		boolean bb = b % 100000 == 0;
		while (bb && ((getCurrentInputStream() !=null && (writeCount - getCurrentInputStream().readCount > bufferOverflowWarning)) || (getCurrentInputStream() == null && writeCount == bufferOverflowWarning))) {
			try {
				Thread.sleep(CHECK_INTERVAL);
				//PMS.debug("BufferedOutputFile Full");
			} catch (InterruptedException e) {
			}
		}
		int mb = (int) (writeCount++ % maxMemorySize);
		if (buffer != null) {
			buffer[mb] = (byte) b;
			if (writeCount == TEMP_SIZE) {
				PMS.debug("freeMemory: " + Runtime.getRuntime().freeMemory());
				PMS.debug("totalMemory: " + Runtime.getRuntime().totalMemory());
				PMS.debug("maxMemory: " + Runtime.getRuntime().maxMemory());
				PMS.debug("Extending buffer to " + maxMemorySize);
				
				try {
					//buffer = Arrays.copyOf(buffer, maxMemorySize);
					byte[] copy = new byte[maxMemorySize];
			        System.arraycopy(buffer, 0, copy, 0,
			                         Math.min(buffer.length, maxMemorySize));
			        buffer = copy;
				} catch (OutOfMemoryError ooe) {
					PMS.minimal("FATAL ERROR: OutOfMemory / dumping stats");
					PMS.debug("freeMemory: " + Runtime.getRuntime().freeMemory());
					PMS.debug("totalMemory: " + Runtime.getRuntime().totalMemory());
					PMS.debug("maxMemory: " + Runtime.getRuntime().maxMemory());
					System.exit(1);
				}
			}
			
			if (timeseek > 0 && writeCount > 19) 
				shiftByTimeSeek(mb, mb <= 20);
			
	
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
		boolean bb = (!mod && (buffer[mb-15] == -32|| buffer[mb-15] == -3) && buffer[mb-16] == 1 && buffer[mb-17] == 0 && buffer[mb-18] == 0 && /*&& buffer[mb-12] == -128*//*(buffer[mb-12]&128)==128&&*/ (buffer[mb-11]&128)==128 && (buffer[mb-9]&32)==32/* && (buffer[mb-4]&16)==16*/)
		|| (mod && (buffer[modulo(mb-15)] == -32 || buffer[modulo(mb-15)] == -3) && buffer[modulo(mb-16)] == 1 && buffer[modulo(mb-17)] == 0 && buffer[modulo(mb-18)] == 0 && /*buffer[modulo(mb-12)] == -128*//*(buffer[modulo(mb-12)]&128)==128 && */(buffer[modulo(mb-11)]&128)==128 && (buffer[modulo(mb-9)]&32)==32 /*&& (buffer[modulo(mb-4)]&16)==16*/);
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
			//PMS.debug("setting PTS");
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
		return (((((buffer[m3] & 0xff) << 8) + (buffer[m2] & 0xff)) >> 1) << 15)
				+ ((((buffer[m1] & 0xff) << 8) + (buffer[m0] & 0xff)) >> 1);
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
	
	private int read(boolean firstRead, long readCount, byte buf []) {
		if (readCount > TEMP_SIZE && readCount < maxMemorySize) {
			int newMargin = maxMemorySize - 2000000;
			if (bufferOverflowWarning != newMargin)
				PMS.info("Setting margin to 2Mb");
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
				PMS.debug("Suspend Read: readCount=" + readCount + " / writeCount=" + writeCount);
			c++;
			try {
				Thread.sleep(CHECK_INTERVAL);
			} catch (InterruptedException e) {
			}
		}
		if (attachedThread != null) {
			attachedThread.setReadyToStop(false);
		}
		if (c > 0)
			PMS.debug("Resume Read: readCount=" + readCount + " / writeCount=" + writeCount);

		if (buffer == null)
			return -1;
		
		int mb = (int) (readCount % maxMemorySize);
		int endOF = buffer.length;
		int cut = 0;
		if (eof) {
			if ((writeCount - readCount) < buf.length) {
				cut = (int) (buf.length - (writeCount - readCount));
			}
		}
		/*if (eof)
			endOF =(int) (writeCount % maxMemorySize);*/
		if (mb>=endOF - buf.length) {
			System.arraycopy(buffer, mb, buf, 0, endOF-mb-cut);
			return endOF-mb;
		} else {
			System.arraycopy(buffer, mb, buf, 0, buf.length-cut);
			return buf.length;
		}
		
	}
	
	private int read(boolean firstRead, long readCount) {
		if (readCount > TEMP_SIZE && readCount < maxMemorySize) {
			int newMargin = maxMemorySize - 2000000;
			if (bufferOverflowWarning != newMargin)
				PMS.info("Setting margin to 2Mb");
			this.bufferOverflowWarning = newMargin;
		}
		if (eof && readCount > writeCount)
			return -1;
		int c = 0;
		int minBufferS = firstRead?minMemorySize:secondread_minsize;
		while (writeCount - readCount <= minBufferS && !eof && c < 15) {
			if (c == 0)
				PMS.debug("Suspend Read: readCount=" + readCount + " / writeCount=" + writeCount);
			c++;
			try {
				Thread.sleep(CHECK_INTERVAL);
			} catch (InterruptedException e) {
			}
		}
		if (attachedThread != null) {
			attachedThread.setReadyToStop(false);
		}

		if (c > 0)
			PMS.debug("Resume Read: readCount=" + readCount + " / writeCount=" + writeCount);

		if (buffer == null)
			return -1;
		return 0xff & buffer[(int) (readCount % maxMemorySize)];
	}
	
	public void attachThread(ProcessWrapper thread) {
		if (attachedThread != null)
			throw new RuntimeException("BufferedOutputFile is already attached to a Thread: " + attachedThread);
		PMS.info("Attaching thread: " + thread);
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
					PMS.error(null, e);
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

	public void reset() {
		if (debugOutput != null)
			try {
				debugOutput.close();
			} catch (IOException e) {}
		PMS.info("Destroying buffer");
		timer.cancel();
		buffer = null;
		System.gc();
		System.gc();
		System.gc();
		if (maxMemorySize != 1048576) {
			PMS.get().getFrame().setValue(0, "Empty");
		}
	}
	
}
