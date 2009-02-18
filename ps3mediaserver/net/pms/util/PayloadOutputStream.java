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

import java.io.IOException;
import java.io.OutputStream;

public abstract class PayloadOutputStream extends OutputStream {
	
	protected int rem = 0;
	protected int remSkip = 0;
	protected byte remSwapBytes [];
	protected int nbbyte = 2;
	protected boolean payload_before;
	protected int analyze_header_length;
	protected boolean swap;
	private OutputStream out;
	protected int blocksize;
	protected byte payload [];
	
	public PayloadOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public void write(int b) throws IOException {
		//
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (remSkip > 0) {
			if (off + remSkip > len) {
				remSkip = off + remSkip - len;
				return;
			}
 			off += remSkip;
 			remSkip = 0;
		}
		if (swap) {
			int r = 0;
			if (remSwapBytes != null) {
				byte tmp [] = new byte [remSwapBytes.length + (len-off)];
				System.arraycopy(remSwapBytes, 0, tmp, 0, remSwapBytes.length);
				System.arraycopy(b, off, tmp, remSwapBytes.length, (len - off));
				b = tmp;
				len += remSwapBytes.length;
				remSwapBytes = null;
			}
			if ((r = (len - off) % nbbyte) != 0) {
				len -= r;
				remSwapBytes = new byte [r];
				System.arraycopy(b, len, remSwapBytes, 0, r);
			}
			int end = off+len;
			if (nbbyte == 2) {
				for(int i=off;i<end;i+=2) {
					byte temp = b[i];
					b[i] = b[i+1];
					b[i+1] = temp;
				}
			} else if (nbbyte == 3) {
				for(int i=off;i<end;i+=3) {
					byte temp = b[i];
					b[i] = b[i+2];
					b[i+2] = temp;
				}
			} else if (nbbyte == 4) {
				for(int i=off;i<end;i+=4) {
					byte temp1 = b[i];
					byte temp2 = b[i+1];
					b[i] = b[i+3];
					b[i+3] = temp1;
					b[i+1] = b[i+2];
					b[i+2] = temp2;
				}
			}
		}

		if (rem > 0) {
		
			if (rem <= len) {
				out.write(b, off, rem);
				off += rem;
				len -= rem;
				rem = 0;
			} else {
				out.write(b, off, len);
				rem = rem - len;
				off += len;
				len = 0;
			}
			if (rem == 0 && !payload_before)
				out.write(payload);

		}

		if (rem == 0) {
			int towrite = len;
			if (analyze_header_length < towrite) {
				analyze_header(b, off, analyze_header_length);
			}
			if (blocksize == -1) {
				out.write(b, off, len);
			} else {
				while (towrite > blocksize) {
					if (payload_before)
						out.write(payload);
					out.write(b, off, blocksize);
					if (!payload_before)
						out.write(payload);
					
					off += blocksize;
					towrite = towrite - blocksize;
				}
				if (towrite > 0) {
					if (payload_before)
						out.write(payload);
					out.write(b, off, towrite);
					
					off += towrite;
					rem = blocksize - towrite;
				}
				
			}
		}
	}
	
	public abstract void analyze_header(byte b [], int off, int len);

	@Override
	public void close() throws IOException {
		if (rem == 0 && !payload_before)
			out.write(payload);
		out.close();
	}

}
