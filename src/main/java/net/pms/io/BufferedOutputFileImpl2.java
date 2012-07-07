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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import net.pms.PMS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Circular memory buffer that can be used as {@link java.io.OutputStream OutputStream} and
 * provides methods that can read data from the memory buffer using an
 * {@link java.io.InputStream InputStream}. The name of this class is a bit misleading, as
 * there is typically no file involved in the process at all. Instead, the buffer is
 * typically used to hold data piped by a transcoding process in one thread until a request
 * for data comes in from another thread.
 * 
 * @see ProcessWrapperImpl
 * @see net.pms.network.Request Request
 * @see net.pms.network.RequestV2 RequestV2
 */
public class BufferedOutputFileImpl2 extends OutputStream implements BufferedOutputFile {
	private static final Logger logger = LoggerFactory.getLogger(BufferedOutputFileImpl2.class);
	
	/**
	 * Initial size for the buffer in bytes.
	 */
	private static final int INITIAL_BUFFER_SIZE = 50000000;
	
	/**
	 * Amount of extra bytes to increase the initial buffer with when memory allocation fails.
	 */
	private static final int MARGIN_LARGE = 20000000;
	private static final int MARGIN_MEDIUM = 2000000;
	private static final int MARGIN_SMALL = 600000;
	
	private static final int CHECK_INTERVAL = 500;
	private static final int CHECK_END_OF_PROCESS = 2500; // must be superior to CHECK_INTERVAL
	private int minMemorySize;
	private int maxMemorySize;
	private int bufferOverflowWarning;
	private boolean eof;
//	private long writeCount;
//	private byte buffer[];
	private boolean forcefirst = (PMS.getConfiguration().getTrancodeBlocksMultipleConnections() && PMS.getConfiguration().getTrancodeKeepFirstConnections());
	private ArrayList<WaitBufferedInputStream> inputStreams;
	private ProcessWrapper attachedThread;
	private int secondread_minsize;
	private Timer timer;
	private boolean shiftScr;
	private FileOutputStream debugOutput = null;
//	private boolean buffered = false;
	private DecimalFormat formatter = new DecimalFormat("#,###");
	private double timeseek;
	private double timeend;
	private long packetpos = 0;

	//ms777: added for detailed debugging
	private final static boolean DETAILED_DEBUG = true;
	private final static String DETAILED_DEBUG_FOLDER = System.getProperty("user.home")+ "\\desktop\\ps3_log\\";
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS", Locale.ENGLISH);
	private FileOutputStream debugRead = null;
	private PrintStream debugLog = null;
	private static String MillisToString(long lMillis) {
		return  sdf.format(new Date(lMillis));
	}
	private static String MillisToString() {
		return  sdf.format(new Date(System.currentTimeMillis()));
	}
	private int margin;

	//ms777: added as a new ring buffer implementation
	private Ringbuffer chbuf;
	// is used in OutputBufferConsumer and WindowsNamedPipe

	/**
	 * Try to increase the size of a memory buffer, while retaining its contents. The
	 * provided new size is considered to be a request, it is scaled down when an
	 * OutOfMemory error occurs. There is no guarantee about the exact length of the
	 * returned byte array, only that it is greater than or equal to the original buffer
	 * size. When null is passed as an argument, a fresh buffer will be allocated.
	 * Copying one byte array to another is a costly operation, both in memory usage
	 * and performance. It is best to avoid using this method.
	 * 
	 * @param buffer The byte array to resize, null is allowed.
	 * @param newSize The requested final size. Should be greater than the original size
	 * or the original buffer will be returned. 
	 * @return The resized byte array.
	 */
	private void growBuffer() {
		byte[] copy = null;
		
		int newSize = 0;
		int deltaSize = maxMemorySize - chbuf.length();
		
		while ((copy==null) && (deltaSize>10000000)) {
			newSize = chbuf.length() + deltaSize;
			try {
				// Try to allocate the requested new size
				copy = new byte[newSize];
			} catch (OutOfMemoryError e) {
				logger.debug("Cannot grow buffer size from " + formatter.format(chbuf.length()) + " bytes to " + formatter.format(newSize) + " bytes.");
				deltaSize = deltaSize/2;
			}
		}
		
		if (copy==null) {
			logger.debug("Cannot grow buffer size at all, staying with "  + formatter.format(chbuf.length()) + " bytes.");
			debugLog.println(MillisToString() + ", growBuffer: Cannot grow buffer size at all, staying with "  + formatter.format(chbuf.length()) + " bytes." );
		} else {
			chbuf.switchToLargerBuffer(copy);
			logger.debug("Could grow buffer size from "  + formatter.format(chbuf.length()) + " bytes to " + formatter.format(newSize) + " bytes.");
			debugLog.println(MillisToString() + ", growBuffer: Could grow buffer size from "  + formatter.format(chbuf.length()) + " bytes to " + formatter.format(newSize) + " bytes.");
		}
		debugLog.println(MillisToString() + ", growBuffer: freeMemory: " + formatter.format(Runtime.getRuntime().freeMemory()));
		debugLog.println(MillisToString() + ", growBuffer: totalMemory: " + formatter.format(Runtime.getRuntime().totalMemory()));
		debugLog.println(MillisToString() + ", growBuffer: maxMemory: " + formatter.format(Runtime.getRuntime().maxMemory()));
		return;
	}
	
	private void growBufferOld(int newSize) {
		byte[] copy;
		
		if (newSize <= chbuf.length()) {
			// Cannot shrink the original
			return;
		}
		
		try {
			// Try to allocate the requested new size
			copy = new byte[newSize];
		} catch (OutOfMemoryError e) {
			if (chbuf.length() == 0) {
				logger.trace("Cannot initialize buffer to " + formatter.format(newSize) + " bytes.");
			} else {
				logger.debug("Cannot grow buffer size from " + formatter.format(chbuf.length()) + " bytes to " + formatter.format(newSize) + " bytes.");
				
			}

			// Could not allocate the requested new size, use 30% of free memory instead.
			// Rationale behind using 30%: multiple threads are running at the same time,
			// we do not want one threads memory usage to suffocate the others.
			// Using maxMemory() to ignore the initial Java heap space size that freeMemory()
			// takes into account.
			// See http://javarevisited.blogspot.com/2011/05/java-heap-space-memory-size-jvm.html
			long realisticSize = Runtime.getRuntime().maxMemory() * 3 / 10;
			
			if (realisticSize < chbuf.length()) {
				// A copy would be smaller in size, shrinking instead of growing the buffer.
				// Better to return the original and retain its size.
				return;
			} else {
				try {
					// Try to allocate the realistic alternative size
					copy = new byte[(int) realisticSize];
				} catch (OutOfMemoryError e2) {
					logger.debug("Cannot grow buffer size from " + formatter.format(chbuf.length()) + " bytes to "
							+ formatter.format(realisticSize) + " bytes either.");
					logger.trace("freeMemory: " + formatter.format(Runtime.getRuntime().freeMemory()));
					logger.trace("totalMemory: " + formatter.format(Runtime.getRuntime().totalMemory()));
					logger.trace("maxMemory: " + formatter.format(Runtime.getRuntime().maxMemory()));

					// Cannot allocate memory, no other option than to return the original.
					return;
				}
			}
		}

		if (chbuf.length() == 0) {
			logger.trace("Successfully initialized buffer to " + formatter.format(copy.length) + " bytes.");
		} else {
				logger.trace("Successfully grown buffer from " + formatter.format(chbuf.length()) + " bytes to "
						+ formatter.format(copy.length) + " bytes."); 
		}
		chbuf.switchToLargerBuffer(copy);
		return;
	}
	
	/**
	 * Constructor to create a memory buffer based on settings that are passed on. Will also
	 * start up a timer task to display buffer size and usage in the PMS main screen.
	 * 
	 * @param params {@link OutputParams} object that contains preferences for the buffers
	 * 				dimensions and behavior.
	 */
	public BufferedOutputFileImpl2(OutputParams params) {
		this.minMemorySize = (int) (1048576 * params.minBufferSize);
		this.maxMemorySize = (int) (1048576 * params.maxBufferSize);

		// FIXME: Better to relate margin directly to maxMemorySize instead of using arbitrary fixed values

		int margin = MARGIN_LARGE; // Issue 220: extends to 20Mb : readCount is wrongly set cause of the ps3's
		// 2nd request with a range like 44-xxx, causing the end of buffer margin to be first sent 
		if (this.maxMemorySize < margin) {// for thumbnails / small buffer usage
			margin = MARGIN_MEDIUM; // margin must be superior to the buffer size of OutputBufferConsumer or direct buffer size from WindowsNamedPipe class
			if (this.maxMemorySize < margin) {
				margin = MARGIN_SMALL;
			}
		}
		this.margin = margin;
		this.bufferOverflowWarning = this.maxMemorySize - margin;
		this.secondread_minsize = params.secondread_minsize;
		this.timeseek = params.timeseek;
		this.timeend = params.timeend;
		this.shiftScr = params.shift_scr;

		if (maxMemorySize > INITIAL_BUFFER_SIZE) {
			// Try to limit memory usage a bit.
			// Start with a modest allocation initially, grow to max when needed later.
//			buffer = growBuffer(null, INITIAL_BUFFER_SIZE);
			chbuf = new Ringbuffer(INITIAL_BUFFER_SIZE);
		} else {
//			buffer = growBuffer(null, maxMemorySize);
			chbuf = new Ringbuffer(maxMemorySize);
		}
/*
		if (buffer.length == 0) {
			// Cannot transcode without a buffer
			logger.info("FATAL ERROR: OutOfMemory / dumping stats");
			logger.trace("freeMemory: " + Runtime.getRuntime().freeMemory());
			logger.trace("totalMemory: " + Runtime.getRuntime().totalMemory());
			logger.trace("maxMemory: " + Runtime.getRuntime().maxMemory());
			System.exit(1);			
		}
*/		
		inputStreams = new ArrayList<WaitBufferedInputStream>();
		timer = new Timer();
		if (params.maxBufferSize > 15 && !params.hidebuffer) {
			timer.schedule(new TimerTask() {
				public void run() {
					long rc = 0;
					if (getCurrentInputStream() != null) {
						rc = getCurrentInputStream().getReadCount();
						PMS.get().getFrame().setReadValue(rc, "");
					}
//					long space = (writeCount - rc);
					long space = (chbuf.getWriteCount() - rc);
					logger.trace("buffered: " + formatter.format(space) + " bytes / inputs: " + inputStreams.size());
					PMS.get().getFrame().setValue((int) (100 * space / maxMemorySize), formatter.format(space) + " bytes");
				}
			}, 0, 2000);
		}
		//ms777: added for detailed debugging
		if (DETAILED_DEBUG) {
			String sNameWrite = DETAILED_DEBUG_FOLDER + "BufferedOutputFileImpl." + MillisToString();
			try {
				debugOutput = new FileOutputStream(sNameWrite + ".write.dat");
				debugRead = new FileOutputStream(sNameWrite + ".read.dat");
				debugLog = new PrintStream(new FileOutputStream(sNameWrite + ".log"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
			debugLog.println(MillisToString() + ": constructor, timeseek: " + timeseek + ", timeend: " + timeend + ", shiftScr: " + shiftScr );

		}
		//ms777 end

	}

	@Override
	public void close() throws IOException {
		logger.trace("EOF");
		eof = true;
//ms777		
		if (DETAILED_DEBUG) {
			debugLog.println(MillisToString() + ": close");
		}
	}

	@Override
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
	
	@Override
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
				while (inputStreams.size() > 0) {
					try {
						inputStreams.get(0).close();
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
			logger.debug("Setting InputStream new position to: " + formatter.format(newReadPosition));
			debugLog.println(MillisToString() + ": getInputStream, newReadPosition: " + newReadPosition + ", getReadCount: " + atominputStream.getReadCount());
			Exception e = new Exception("getInputStream");
			e.printStackTrace(debugLog);
			atominputStream.setReadCount(newReadPosition);
		}
		return atominputStream;
	}

	@Override
	public long getWriteCount() {
		return chbuf.getWriteCount();
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (DETAILED_DEBUG) debugLog.println(MillisToString() + ", write: off:"  + off + ", len:" + len 
				 + ", readCount = " + (getCurrentInputStream() != null ? getCurrentInputStream().getReadCount() : "null")
				+ ", writeCount: " + chbuf.getWriteCount() 
				 + ", attThreadReadyToStop: " + ((attachedThread==null)?"null":attachedThread.isReadyToStop()));
		if (debugOutput != null) {
			debugOutput.write(b, off, len);
			debugOutput.flush();
		}
		
/*
		WaitBufferedInputStream input = getCurrentInputStream();

		//logger.trace("write(" + b.length + ", " + off + ", " + len + "), writeCount = " + writeCount + ", readCount = " + (input != null ? input.getReadCount() : "null"));
		while ((input != null && (writeCount - input.getReadCount() > bufferOverflowWarning)) || (input == null && writeCount > bufferOverflowWarning)) {
			try {
				Thread.sleep(CHECK_INTERVAL);
			} catch (InterruptedException e) {
			}
			input = getCurrentInputStream();
		}
*/
		while ((chbuf.writableBytes()<(len-margin))) {
//			growBuffer(maxMemorySize);
			growBuffer();
			try {
				Thread.sleep(CHECK_INTERVAL);
			} catch (InterruptedException e) {
			}
		}

		synchronized(chbuf) {
			if (DETAILED_DEBUG) debugLog.println(MillisToString() + ", write 1: off:"  + off + ", len:" + len	+ ", writeCount: " + chbuf.getWriteCount()); 
			chbuf.write(b, off, len);
			if (DETAILED_DEBUG) debugLog.println(MillisToString() + ", write 2: off:"  + off + ", len:" + len	+ ", writeCount: " + chbuf.getWriteCount()); 

//			buffered = (len > 0) ;
		}
		/*
		// Ditlew - WDTV Live
		if (timeseek > 0 && writeCount > 10) {
			for (int i = 0; i < len; i++) {
				if (buffer != null && shiftScr) {
					shiftSCRByTimeSeek(mb + i, (int) timeseek); // Ditlew - update any SCR headers
				}					//shiftGOPByTimeSeek(mb+i, (int)timeseek); // Ditlew - update any GOP headers - Not needed for WDTV Live
			}
		}

		writeCount += len - off;
		if (timeseek > 0 && timeend == 0) {
			int packetLength = 6; // minimum to get packet size
			while (packetpos + packetLength < writeCount && buffer != null) {
				int packetposMB = (int) (packetpos % maxMemorySize);
				int streamPos = 0;
				if (buffer[modulo(packetposMB, buffer.length)] == 71) {// TS
					packetLength = 188;
					streamPos = 4;

					// adaptation field
					if ((buffer[modulo(packetposMB + 3, buffer.length)] & 0x20) == 0x20) {
						streamPos += 1 + ((buffer[modulo(packetposMB + 4, buffer.length)] + 256) % 256);
					}

					if (streamPos == 188) {
						streamPos = -1;
					}

				} else if (buffer[modulo(packetposMB + 3, buffer.length)] == -70) { // BA
					packetLength = 14;
					streamPos = -1;
				} else {
					packetLength = 6 + (((buffer[modulo(packetposMB + 4, buffer.length)] + 256) % 256)) * 256 + ((buffer[modulo(packetposMB + 5, buffer.length)] + 256) % 256);
				}
				if (streamPos != -1) {
					mb = packetposMB + streamPos + 18;
					if (!shiftVideo(mb, true)) {
						mb = mb - 5;
						shiftAudio(mb, true);
					}
				}
				packetpos += packetLength;
			}
		}
*/
	}

	
	/**
	 * Determine a modulo value that is guaranteed to be zero or positive, as opposed to
	 * the standard Java % operator which can return a negative value. 
	 * 
	 * @param number Number to divide
	 * @param divisor Number that is used to divide
	 * @return The rest value of the division.
	 */
	private int modulo(int number, int divisor) {
		if (number >= 0) {
			return number % divisor;
		}
		return ((number % divisor) + divisor) % divisor;
	}

	
	@Override
	public void write(int b) throws IOException {
//ms777
		if (DETAILED_DEBUG) debugLog.println(MillisToString() + ", write: off:"  + 0 + ", len:" + 1 +  
				 ", writeCount: " + chbuf.getWriteCount() 
				 + ", attThreadReadyToStop: " + ((attachedThread==null)?"null":attachedThread.isReadyToStop())
				 + ", readCount = " + (getCurrentInputStream() != null ? getCurrentInputStream().getReadCount() : "null"));
		if (debugOutput != null) {
			debugOutput.write(b);
			debugOutput.flush();
		}
//ms777 end
		
		boolean bb = b % 100000 == 0;
		WaitBufferedInputStream input = getCurrentInputStream();
		while (bb && ((input != null && (chbuf.getWriteCount() - input.getReadCount() > bufferOverflowWarning)) || (input == null && chbuf.getWriteCount() == bufferOverflowWarning))) {
			try {
				Thread.sleep(CHECK_INTERVAL);
				//logger.trace("BufferedOutputFile Full");
			} catch (InterruptedException e) {
			}
			input = getCurrentInputStream();
		}
		
		chbuf.write(new byte[]{(byte) b}, 0, 1);
//		buffered = true;
//		writeCount++;

		if (chbuf.getWriteCount() == INITIAL_BUFFER_SIZE) {
//			growBuffer(maxMemorySize);
			growBuffer();
		}
/*
			if (timeseek > 0 && writeCount > 19) {
				shiftByTimeSeek(mb, mb <= 20);
			}

			// Ditlew - WDTV Live - update any SCR headers
			if (timeseek > 0 && writeCount > 10) {
				shiftSCRByTimeSeek(mb, (int) timeseek);
			}
		}
*/		
	}
/*
	// Ditlew - Modify SCR
	private void shiftSCRByTimeSeek(int buffer_index, int offset_sec) {
		int m9 = modulo(buffer_index - 9, buffer.length);
		int m8 = modulo(buffer_index - 8, buffer.length);
		int m7 = modulo(buffer_index - 7, buffer.length);
		int m6 = modulo(buffer_index - 6, buffer.length);
		int m5 = modulo(buffer_index - 5, buffer.length);
		int m4 = modulo(buffer_index - 4, buffer.length);
		int m3 = modulo(buffer_index - 3, buffer.length);
		int m2 = modulo(buffer_index - 2, buffer.length);
		int m1 = modulo(buffer_index - 1, buffer.length);
		int m0 = modulo(buffer_index, buffer.length);

		// SCR
		if (buffer[m9] == 0
			&& buffer[m8] == 0
			&& buffer[m7] == 1
			&& buffer[m6] == -70 && // 0xBA - Java/PMS wants -70
			// control bits
			!((buffer[m5] & 128) == 128)
			&& ((buffer[m5] & 64) == 64)
			&& ((buffer[m5] & 4) == 4)
			&& ((buffer[m3] & 4) == 4)
			&& ((buffer[m1] & 4) == 4)
			&& ((buffer[m0] & 1) == 1)) {
			long scr_32_30 = ((buffer[m5] & 56) >> 3);
			long scr_29_15 = ((buffer[m5] & 3) << 13) + (buffer[m4] << 5) + ((buffer[m3] & 248) >> 3);
			long scr_14_00 = ((buffer[m3] & 3) << 13) + (buffer[m2] << 5) + ((buffer[m1] & 248) >> 3);

			long scr = (scr_32_30 << 30) + (scr_29_15 << 15) + scr_14_00;
			long scr_new = scr + (90000L * offset_sec);

			long scr_32_30_new = (scr_new & 7516192768L) >> 30;  // 111000000000000000000000000000000
			long scr_29_15_new = (scr_new & 1073709056L) >> 15;  // 000111111111111111000000000000000
			long scr_14_00_new = (scr_new & 32767L);             // 000000000000000000111111111111111

			// scr_32_30_new
			buffer[m5] = (byte) ((buffer[m5] & 199) + ((scr_32_30_new << 3) & 56)); // 11000111

			// scr_29_15_new
			buffer[m5] = (byte) ((buffer[m5] & 252) + ((scr_29_15_new >> 13) & 3)); // 00000011
			buffer[m4] = (byte) (scr_29_15_new >> 5);                               // 11111111
			buffer[m3] = (byte) ((buffer[m3] & 7) + ((scr_29_15_new << 3) & 248));  // 11111000

			// scr_14_00_new
			buffer[m3] = (byte) ((buffer[m3] & 252) + ((scr_14_00_new >> 13) & 3)); // 00000011
			buffer[m2] = (byte) (scr_14_00_new >> 5);                               // 11111111
			buffer[m1] = (byte) ((buffer[m1] & 7) + ((scr_14_00_new << 3) & 248));  // 11111000

			// Debug
			//logger.trace("Ditlew - SCR "+scr+" ("+(int)(scr/90000)+") -> "+scr_new+" ("+(int)(scr_new/90000)+")  "+offset_sec+" secs");
		}
	}

	// Ditlew - Modify GOP
	@SuppressWarnings("unused")
	private void shiftGOPByTimeSeek(int buffer_index, int offset_sec) {
		int m7 = modulo(buffer_index - 7, buffer.length);
		int m6 = modulo(buffer_index - 6, buffer.length);
		int m5 = modulo(buffer_index - 5, buffer.length);
		int m4 = modulo(buffer_index - 4, buffer.length);
		int m3 = modulo(buffer_index - 3, buffer.length);
		int m2 = modulo(buffer_index - 2, buffer.length);
		int m1 = modulo(buffer_index - 1, buffer.length);
		int m0 = modulo(buffer_index, buffer.length);

		// check if valid gop
		if (buffer[m7] == 0
			&& buffer[m6] == 0
			&& buffer[m5] == 1
			&& buffer[m4] == -72 && // 0xB8 - Java/PMS wants -72
			// control bits
			((buffer[m2] & 0x08) == 0x08)
			&& ((buffer[m0] & 31) == 0)
			&& // of interest
			!((buffer[m3] & 128) == 128) && // not drop frm
			!((buffer[m0] & 16) == 16) // not broken
			) {
			// org timecode
			byte h = (byte) ((buffer[m3] & 124) >> 2);
			byte m = (byte) (((buffer[m3] & 3) << 4) + ((buffer[m2] & 240) >> 4));
			byte s = (byte) (((buffer[m2] & 7) << 3) + ((buffer[m1] & 224) >> 5));

			// updated offset
			int _offset = s + m * 60 + h * 60 + offset_sec;

			// new timecode
			byte _h = (byte) ((_offset / 3600) % 24);
			byte _m = (byte) ((_offset / 60) % 60);
			byte _s = (byte) (_offset % 60);

			// update gop
			// h - ok
			buffer[m3] = (byte) ((buffer[m3] & 131) + (_h << 2)); // 10000011
			// m - ok
			buffer[m3] = (byte) ((buffer[m3] & 252) + (_m >> 4)); // 11111100
			buffer[m2] = (byte) ((buffer[m2] & 15) + (_m << 4)); // 00001111
			// s - ok
			buffer[m2] = (byte) ((buffer[m2] & 248) + (_s >> 3)); // 11111000
			buffer[m1] = (byte) ((buffer[m1] & 31) + (_s << 5)); // 00011111

			// Debug
			//logger.trace("Ditlew - GOP "+h+":"+m+":"+s+" -> "+_h+":"+_m+":"+_s+"  "+offset_sec+" secs");
		}
	}

	private void shiftByTimeSeek(int mb, boolean mod) {
		shiftVideo(mb, mod);
		shiftAudio(mb, mod);
	}
	
	private boolean shiftAudio(int mb, boolean mod) {
		boolean bb = (!mod && (buffer[mb - 10] == -67 || buffer[mb - 10] == -64) && buffer[mb - 11] == 1 && buffer[mb - 12] == 0 && buffer[mb - 13] == 0 &&  (buffer[mb - 6] & 128) == 128)
			|| (mod && (buffer[modulo(mb - 10, buffer.length)] == -67 || buffer[modulo(mb - 10, buffer.length)] == -64) && buffer[modulo(mb - 11, buffer.length)] == 1 && buffer[modulo(mb - 12, buffer.length)] == 0 && buffer[modulo(mb - 13, buffer.length)] == 0 &&  (buffer[modulo(mb - 6, buffer.length)] & 128) == 128);
		if (bb) {
			int pts = (((((buffer[modulo(mb - 3, buffer.length)] & 0xff) << 8) + (buffer[modulo(mb - 2, buffer.length)] & 0xff)) >> 1) << 15) + ((((buffer[modulo(mb - 1, buffer.length)] & 0xff) << 8) + (buffer[modulo(mb, buffer.length)] & 0xff)) >> 1);
			pts += (int) (timeseek * 90000);

			setTS(pts, mb, mod);
			return true;
		}
		return false;
	}

	private boolean shiftVideo(int mb, boolean mod) {
		boolean bb = (!mod
			&& (buffer[mb - 15] == -32 || buffer[mb - 15] == -3)
			&& buffer[mb - 16] == 1
			&& buffer[mb - 17] == 0
			&& buffer[mb - 18] == 0
			&& (buffer[mb - 11] & 128) == 128
			&& (buffer[mb - 9] & 32) == 32) || (mod
			&& (buffer[modulo(mb - 15, buffer.length)] == -32 || buffer[modulo(mb - 15, buffer.length)] == -3)
			&& buffer[modulo(mb - 16, buffer.length)] == 1
			&& buffer[modulo(mb - 17, buffer.length)] == 0
			&& buffer[modulo(mb - 18, buffer.length)] == 0
			&& (buffer[modulo(mb - 11, buffer.length)] & 128) == 128
			&& (buffer[modulo(mb - 9, buffer.length)] & 32) == 32);

		if (bb) { // check EO or FD (tsmuxer)
			int pts = getTS(mb - 5, mod);
			int dts = 0;
			boolean dts_present = (buffer[modulo(mb - 11, buffer.length)] & 64) == 64;
			if (dts_present) {
				if ((buffer[modulo(mb - 4, buffer.length)] & 15) == 15) {
					dts = (((((255 - (buffer[modulo(mb - 3, buffer.length)] & 0xff)) << 8) + (255 - (buffer[modulo(mb - 2, buffer.length)] & 0xff))) >> 1) << 15) + ((((255 - (buffer[modulo(mb - 1, buffer.length)] & 0xff)) << 8) + (255 - (buffer[modulo(mb, buffer.length)] & 0xff))) >> 1);
					dts = -dts;
				} else {
					dts = getTS(mb, mod);
				}
			}

			int ts = (int) (timeseek * 90000);
			if (mb == 50 && writeCount < maxMemorySize) {
				dts--;
			}
			pts += ts;

			setTS(pts, mb - 5, mod);
			if (dts_present) {
				if (dts < 0) {
					buffer[modulo(mb - 4, buffer.length)] = 17;
				}
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
			m3 = modulo(m3, buffer.length);
			m2 = modulo(m2, buffer.length);
			m1 = modulo(m1, buffer.length);
			m0 = modulo(m0, buffer.length);
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
			m3 = modulo(m3, buffer.length);
			m2 = modulo(m2, buffer.length);
			m1 = modulo(m1, buffer.length);
			m0 = modulo(m0, buffer.length);
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
*/
	@Override
	public synchronized int read(boolean firstRead, long readCount, byte buf[], int off, int len) {
		if (DETAILED_DEBUG) debugLog.println(MillisToString() + ", read: off:" + off + ", len:" + len + 
				", readCount: " + readCount + ", writeCount: " + chbuf.getWriteCount() + ", attThreadReadyToStop: " + ((attachedThread==null)?"null":attachedThread.isReadyToStop()));
		if (readCount > INITIAL_BUFFER_SIZE && readCount < maxMemorySize) {
			int newMargin = maxMemorySize - MARGIN_MEDIUM;
			if (bufferOverflowWarning != newMargin) {
				logger.debug("Setting margin to 2Mb");
			}
			this.bufferOverflowWarning = newMargin;
		}
		if (eof && readCount >= chbuf.getWriteCount()) {
			if (DETAILED_DEBUG) 
				debugLog.println(MillisToString() + ", read: eof: returning -1");
			return -1;
		}
		int c = 0;
		int minBufferS = firstRead ? minMemorySize : secondread_minsize;
//		while (chbuf.getWriteCount() - readCount <= minBufferS && !eof && c < 15) {
		while (chbuf.readableBytes()<=0 && !eof && c < 15) {
			if (c == 0) {
				logger.trace("Suspend Read: readCount=" + readCount + " / writeCount=" + chbuf.getWriteCount());
			}
			c++;
			try {
				if (DETAILED_DEBUG) 
					debugLog.println(MillisToString() + ", read: start sleeping");
				Thread.sleep(CHECK_INTERVAL);
				if (DETAILED_DEBUG) 
					debugLog.println(MillisToString() + ", read: stop sleeping");
			} catch (InterruptedException e) {
			}
		}
		if (attachedThread != null) {
			attachedThread.setReadyToStop(false);
		}
		if (c > 0) {
			logger.trace("Resume Read: readCount=" + readCount + " / writeCount=" + chbuf.getWriteCount());
			if (DETAILED_DEBUG) 
				debugLog.println(MillisToString() + ", read: resume read, c: " + c);
			if (c==15) {
				debugLog.println(MillisToString() + ", read: failed because c: " + c);
				eof=true;
				return -1;
			}
		}
/*
		if (!buffered) {
			if (DETAILED_DEBUG) 
				debugLog.println(MillisToString() + ", read: !buffered: returning -1");
			return -1;
		}
*/

		int iRead = chbuf.read(buf, off, len);
		//ms777
		if (debugRead!=null) {
				try {
					debugRead.write(buf, off, iRead);
					debugRead.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//ms777 end
		
		if (DETAILED_DEBUG) 
			debugLog.println(MillisToString() + ", read end: returning: " + iRead);
		return iRead;
	}

	@Override
	public synchronized int read(boolean firstRead, long readCount) {
		if (DETAILED_DEBUG) debugLog.println(MillisToString() + ", read one byte: off:" + 0 + ", len:" + 1 +  
				", readCount: " + readCount + ", writeCount: " + chbuf.getWriteCount() + ", attThreadReadyToStop: " + ((attachedThread==null)?"null":attachedThread.isReadyToStop()));
		if (readCount > INITIAL_BUFFER_SIZE && readCount < maxMemorySize) {
			int newMargin = maxMemorySize - MARGIN_MEDIUM;
			if (bufferOverflowWarning != newMargin) {
				logger.debug("Setting margin to 2Mb");
			}
			this.bufferOverflowWarning = newMargin;
		}
		if (eof && readCount >= chbuf.getWriteCount()) {
			//ms777
			if (debugRead!=null) {
				try {
					debugRead.write(-1);
					debugRead.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//ms777 end
			if (DETAILED_DEBUG) 
				debugLog.println(MillisToString() + ", read: eof: returning -1");
			return -1;
		}
		int c = 0;
		int minBufferS = firstRead ? minMemorySize : secondread_minsize;
//		while (chbuf.getWriteCount() - readCount <= minBufferS && !eof && c < 15) {
		while (chbuf.readableBytes()<=0 && !eof && c < 15) {
			if (c == 0) {
				logger.trace("Suspend Read: readCount=" + readCount + " / writeCount=" + chbuf.getWriteCount());
			}
			c++;
			try {
				if (DETAILED_DEBUG) 
					debugLog.println(MillisToString() + ", read: start sleeping");
				Thread.sleep(CHECK_INTERVAL);
				if (DETAILED_DEBUG) 
					debugLog.println(MillisToString() + ", read: stop sleeping");
			} catch (InterruptedException e) {
			}
		}
		if (attachedThread != null) {
			attachedThread.setReadyToStop(false);
		}

		if (c > 0) {
			logger.trace("Resume Read: readCount=" + readCount + " / writeCount=" + chbuf.getWriteCount());
			if (DETAILED_DEBUG) 
				debugLog.println(MillisToString() + ", read: resume read, c: " + c);
			if (c==15) {
				debugLog.println(MillisToString() + ", read: failed because c: " + c);
				eof=true;
				return -1;
			}
		}
/*
		if (!buffered) {
			//ms777
			if (DETAILED_DEBUG) 
				debugLog.println(MillisToString() + ", read: !buffered: returning -1");
			//ms777 end
			return -1;
		}
*/
		byte[] b = new byte[1];
		int iRead = chbuf.read(b, 0, 1);
		
		int iReturn = (iRead>0) ? b[0] : -1;
		b[0] = (byte) iReturn;
		//ms777
		if (debugRead!=null) {
			try {
				debugRead.write(b, 0, 1);
				debugRead.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//ms777 end
		if (DETAILED_DEBUG) 
			debugLog.println(MillisToString() + ", read one byte end: returning: " + iReturn);
		return iReturn;
	}

	@Override
	public void attachThread(ProcessWrapper thread) {
		if (attachedThread != null) {
			throw new RuntimeException("BufferedOutputFile is already attached to a Thread: " + attachedThread);
		}
		logger.debug("Attaching thread: " + thread);
		attachedThread = thread;
	}

	@Override
	public void removeInputStream(WaitBufferedInputStream inputStream) {
		inputStreams.remove(inputStream);
	}
	
	@Override
	public void detachInputStream() {
		if (DETAILED_DEBUG) {
			debugLog.println(MillisToString() + ": detachInputStream start");
		}
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
		new Thread(checkEnd, "Buffered IO End Checker").start();
	}

	@Override
	public synchronized void reset() {
		if (DETAILED_DEBUG) {
			debugLog.println(MillisToString() + ", reset 1");
		}
		timer.cancel();
		//		buffered = false;
		if (chbuf != null) {
			logger.trace("Destroying buffer");
			chbuf.destroyBuffer();
			//			chbuf = null;
		}
		if (maxMemorySize != 1048576) {
			PMS.get().getFrame().setValue(0, "Empty");
		}
		//ms777
		if (DETAILED_DEBUG) {
			debugLog.println(MillisToString() + ", reset 2");
			if (debugRead != null) {
				try {
					debugRead.close();
				} catch (IOException e) {
					logger.debug("Caught exception", e);
				}
			}
			if (debugLog != null) {
				debugLog.close();
			}
		}
		if (debugOutput != null) {
			try {
				debugOutput.close();
			} catch (IOException e) {
				logger.debug("Caught exception", e);
			}
		}
		//ms777 end		
	}
}
class Ringbuffer {
	/**
	 * writePos: The position in the buffer, where the new bytes can be written. Initially 0
	 * readPos: The position in the buffer, from where valid bytes can be read from. Initially 0.
	 * 
	 * reading the buffer:
	 * if readPos<writePos, all bytes from [readPos] to [writePos-1] are valid, i.e. read(buf, readPos, writePos-readPos)
	 * if readPos==writePos, there is nothing to read. Wait until new data are written
	 * if readPos>writePos, all bytes from [readPos] to [buf.length-1] and from [0] to [writePos-1] are valid
	 * 
	 * writing the buffer:
	 * if readPos<=writePos, all bytes from [writePos] to [buf.length-1] and from [0] to [readPos-1] can be written
	 * if readPos>writePos, all bytes from [writePos] to [readPos-1] can be written
	 * 
	 * increasing the buffer size:
	 * if readPos<writePos, move [readPos]..[writePos-1] to newbuf [0]..[writePos-readPos-1]. Set newreadPos to 0, newritePos to writePos-readPos-1
	 * if readPos==writePos, there is nothing to move. Set newreadPos = newritePos = 0
	 * if readPos>writePos, move [readPos]..[buf.length-1] to newbuf [0]..[buf.length-readPos-1], then move [0] to [writePos-1] to newbuf [buf.length-readPos]..[buf.length-readPos+writePos-1]
	 *   set newreadpos = 0, newritePos = buf.length-readPos+writePos
	 */
	
	boolean DEBUG = false;
	PrintStream debugLog = System.out;

	private final static int CHECK_INTERVAL_WRITE = 500; //ms to wait, if data cannot be written
	private final static int CHECK_INTERVAL_READ = 500; //ms to wait, if data cannot be read
	private Thread readThread = null;
	boolean eof = false; //set to true in close(). No more data shall be written.
	int iReadPos = 0;
	int iWritePos = 0;
	int iWriteCount = 0;

	private byte[] buffer;

	public Ringbuffer(int initialCapacity) {
		buffer = new byte[initialCapacity];
	}
	
	public int length() {
		if (buffer==null) return -1;
		synchronized(buffer) {
			return buffer.length;
		}
	}
	
	public void destroyBuffer() {
		if (buffer!=null) synchronized(buffer) {
			buffer = null;
		}
	}
	
	public int readableBytes() {
		if (buffer==null) return -1;
		synchronized(buffer) {
			if (iReadPos<iWritePos) {
				return iWritePos-iReadPos;
			} else if (iReadPos==iWritePos) {
				return 0;
			} else {
				return buffer.length-iReadPos+iWritePos;
			} 
		}
	}
	
	public int writableBytes() {
		if (buffer==null) return -1;
		synchronized(buffer) {
			if (iReadPos<iWritePos) {
				return  (buffer.length+iReadPos-iWritePos);
			} else if (iReadPos==iWritePos) {
				return  buffer.length;
			} else {
				return (iReadPos-iWritePos);
			} 
		}
	}
	
	public int getWriteCount() {
		if (buffer==null) return -1;
		
		synchronized(buffer) {
			return iWriteCount;
		}
	}
	
	
	public void switchToLargerBuffer(byte[] copy) {
		synchronized(buffer) {
			if (iReadPos<iWritePos) {
				System.arraycopy(buffer, iReadPos, copy, 0, iWritePos-iReadPos);
				iWritePos = iWritePos-iReadPos;
				iReadPos = 0;
			} else if (iReadPos==iWritePos) {
				iWritePos = 0;
				iReadPos = 0;
			} else if (iReadPos>iWritePos) {
				System.arraycopy(buffer, iReadPos, copy, 0, buffer.length-iReadPos);
				System.arraycopy(buffer, 0, copy, buffer.length-iReadPos, iWritePos);
				iWritePos = buffer.length-iReadPos+iWritePos;
				iReadPos = 0;
			} 
			buffer = copy;
		}
	}
	
	
	public void write(byte b[], int off, int len) {
		if (eof) return;
		if (buffer==null) return;
		
		while (len > writableBytes()) {
			synchronized(buffer) {
//				buffer = growBuffer(buffer, 70000000);
			}
			//ms777: new !!				
			if (len > writableBytes()) {
				if (DEBUG) debugLog.println(System.currentTimeMillis() + ", write: sleep: off:"  + off + ", len:" + len + ", iReadPos:" + iReadPos + ", iWritePos:" + iWritePos + ", buf.len: " + buffer.length+ 
						", writable: " + writableBytes() + ", readable: " + readableBytes());
				try {
					Thread.sleep(CHECK_INTERVAL_WRITE);
				} catch (InterruptedException e) {
					if (DEBUG) debugLog.println(System.currentTimeMillis() + ", Write thread sleep interrupted");
					return;
				}
			}
		}
		
		if (DEBUG) debugLog.println(System.currentTimeMillis() + ", write: sleep over: off:"  + off + ", len:" + len + ", iReadPos:" + iReadPos + ", iWritePos:" + iWritePos + ", buf.len: " + buffer.length+ 
				", writable: " + writableBytes() + ", readable: " + readableBytes());

		synchronized(buffer) {
			/*
			 * writing the buffer:
			 * if readPos<=writePos, all bytes from [writePos] to [buf.length-1] and from [0] to [readPos-1] can be written
			 * if readPos>writePos, all bytes from [writePos] to [readPos-1] can be written
			 * 			
			 */
			if (iReadPos<=iWritePos) {
				if (iWritePos+len<buffer.length) {
					System.arraycopy(b, off, buffer, iWritePos, len);
					iWritePos+=len;
				} else {
					System.arraycopy(b, off, buffer, iWritePos, buffer.length-iWritePos);
					System.arraycopy(b, off + buffer.length-iWritePos, buffer, 0, len-(buffer.length-iWritePos));
					iWritePos+=len-buffer.length; 
				}
			} else {
				if (len<iReadPos-iWritePos) {
					System.arraycopy(b, off, buffer, iWritePos, len);
					iWritePos+=len;
				} else {
					System.exit(1);
				}
			}
			iWriteCount += len;
		}
	}
	public int read(byte buf[], int off, int len) {
		if (DEBUG) debugLog.println(System.currentTimeMillis() + ", read: off:" + off + ", len:" + len 
				+ ", iReadPos:" + iReadPos + ", iWritePos:" + iWritePos + ", buf.len: " + buffer.length );
		while ((readableBytes() <= 0) && !eof) {
			readThread = Thread.currentThread();
			try {
				Thread.sleep(CHECK_INTERVAL_READ);
			} catch (InterruptedException e) {
				if (DEBUG) debugLog.println(System.currentTimeMillis() + ", read thread sleep interrupted");
			}
			readThread = null;
			if (DEBUG) debugLog.println(System.currentTimeMillis() + ", Read!!: sleep after unsuccessful read: off:"  + off + ", len:" + len + ", iReadPos:" + iReadPos + ", iWritePos:" + iWritePos + ", buf.len: " + buffer.length+ 
					", writable: " + writableBytes() + ", readable: " + readableBytes());
		}
		
		if ((readableBytes() <= 0)) {
			if (eof) {
				return -1;
			} else {
				return 0;
			}
		}
		/*
		 *  * reading the buffer:
		 * if readPos<writePos, all bytes from [readPos] to [writePos-1] are valid, i.e. read(buf, readPos, writePos-readPos)
		 * if readPos==writePos, there is nothing to read. Wait until new data are written
		 * if readPos>writePos, all bytes from [readPos] to [buf.length-1] and from [0] to [writePos-1] are valid
		 */
		synchronized(buffer) {
			if (iReadPos < iWritePos) {
				int iToRead = Math.min(readableBytes(), len);
				System.arraycopy(buffer, iReadPos, buf, off, iToRead);

				iReadPos+=iToRead;
				if (DEBUG) debugLog.println(System.currentTimeMillis() + ", read: out1: iReturn:" + (iToRead ) + ", len:" + len + ", iReadPos:" + iReadPos + ", iWritePos:" + iWritePos + ", buf.len: " + buffer.length+ 
						", writable: " + writableBytes() + ", readable: " + readableBytes());
				return iToRead;
			} else if (iReadPos > iWritePos) {
				int iToRead = Math.min(buffer.length-iReadPos, len);
				System.arraycopy(buffer, iReadPos, buf, off, iToRead);
				iReadPos+=iToRead;
				int iToRead2 = Math.min(readableBytes(), len-iToRead);
				if (iToRead2>0) {
					System.arraycopy(buffer, 0, buf, off+iToRead, iToRead2);
					iReadPos = iToRead2;
				}
				if (DEBUG) debugLog.println(System.currentTimeMillis() + ", read: out2: iReturn:" + (iToRead + iToRead2) + ", len:" + len + ", iReadPos:" + iReadPos + ", iWritePos:" + iWritePos + ", buf.len: " + buffer.length+ 
						", writable: " + writableBytes() + ", readable: " + readableBytes());
				return iToRead + iToRead2;
			}
		}
		if (DEBUG) debugLog.println(System.currentTimeMillis() + ", read: out3: iReturn:" + (0) + ", len:" + len + ", iReadPos:" + iReadPos + ", iWritePos:" + iWritePos + ", buf.len: " + buffer.length+ 
				", writable: " + writableBytes() + ", readable: " + readableBytes());
		return 0;

	}
	
	public void close() {
		eof = true;
		if (readThread!=null) {
			readThread.interrupt();
		}
	}


}

