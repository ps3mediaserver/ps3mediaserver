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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import net.pms.PMS;
import net.pms.util.H264AnnexBInputStream;

public class PipeIPCProcess extends Thread implements ProcessWrapper {
	
	private PipeProcess mkin;
	private PipeProcess mkout;
	private byte header[];
	private boolean h264_annexb;
	
	public void setH264_annexb(boolean h264_annexb) {
		this.h264_annexb = h264_annexb;
	}

	public byte[] getHeader() {
		return header;
	}

	public void setHeader(byte[] header) {
		this.header = header;
	}

	public PipeIPCProcess(String pipeName, String pipeNameOut, boolean forcereconnect1, boolean forcereconnect2) {
		mkin = new PipeProcess(pipeName, forcereconnect1?"reconnect":"dummy");
		mkout = new PipeProcess(pipeNameOut, "out", forcereconnect2?"reconnect":"dummy");
	}
	
	public void run() {
		byte b [] = new byte [512*1024];
		int n = -1;
		InputStream in = null;
		OutputStream out = null;
		OutputStream debug = null;
		/*try {
			debug = new FileOutputStream(System.currentTimeMillis() + "debug");
		} catch (Exception e1) {}*/
		try {
			in = mkin.getInputStream();
			out = mkout.getOutputStream();
			
			if (h264_annexb) {
				in = new H264AnnexBInputStream(in, header);
			}
			
			if (header != null && !h264_annexb)
				out.write(header);
			if (debug != null && header != null && !h264_annexb)
				debug.write(header);
			while ((n=in.read(b)) > -1) {
				out.write(b, 0, n);
				if (debug != null)
					debug.write(b, 0, n);
			}
		} catch (IOException e) {
			PMS.info("Error :" + e.getMessage());
		} finally {
			try {
				in.close();
				out.close();
				if (debug != null)
					debug.close();
			} catch (IOException e) {
				PMS.info("Error :" + e.getMessage());
			}
		}
		
	}
	
	public String getInputPipe() {
		return mkin.getInputPipe();
	}

	public String getOutputPipe() {
		return mkout.getOutputPipe();
	}
	
	public ProcessWrapper getPipeProcess() {
		return this;
	}
	
	public void deleteLater() {
		mkin.deleteLater();
		mkout.deleteLater();
	}
	
	public InputStream getInputStream() throws IOException {
		return mkin.getInputStream();
	}
	
	public OutputStream getOutputStream() throws IOException {
		return mkout.getOutputStream();
	}

	@Override
	public InputStream getInputStream(long seek) throws IOException {
		return null;
	}

	@Override
	public ArrayList<String> getResults() {
		return null;
	}

	@Override
	public boolean isDestroyed() {
		return isAlive();
	}

	@Override
	public void runInNewThread() {
		start();
	}

	@Override
	public boolean isReadyToStop() {
		return false;
	}

	@Override
	public void setReadyToStop(boolean nullable) {
		
	}

	@Override
	public void stopProcess() {
		this.interrupt();
		mkin.getPipeProcess().stopProcess();
		mkout.getPipeProcess().stopProcess();
	}
}
