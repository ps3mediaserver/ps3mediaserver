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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.pms.PMS;
import net.pms.util.ProcessUtil;

public class ProcessWrapperImpl extends Thread implements ProcessWrapper {
	
	@Override
	public String toString() {
		return super.getName();
	}

	private boolean success;
	public boolean isSuccess() {
		return success;
	}

	private String cmdLine;
	private Process process;
	private OutputConsumer outConsumer;
	private OutputTextConsumer stderrConsumer;
	private OutputParams params;
	private boolean destroyed;
	private String [] cmdArray;
	private boolean nullable;
	private ArrayList<ProcessWrapper> attachedProcesses;
	private BufferedOutputFile bo = null;
	
	public ProcessWrapperImpl(String cmdArray [], OutputParams params) {
		super(cmdArray[0]);
		File exec = new File(cmdArray[0]);
		if (exec.exists() && exec.isFile())
			cmdArray[0] = exec.getAbsolutePath();
		this.cmdArray = cmdArray;
		StringBuffer sb = new StringBuffer("");
		for(int i=0;i<cmdArray.length;i++) {
			if (i>0)
				sb.append(" ");
			sb.append(cmdArray[i]);
		}
		cmdLine = sb.toString();
		this.params = params;
		attachedProcesses = new ArrayList<ProcessWrapper>();
	}
	
	public void attachProcess(ProcessWrapper process) {
		attachedProcesses.add(process);
	}

	public void run() {
		ProcessBuilder pb = new ProcessBuilder(cmdArray);
		try {
			PMS.info("Starting " + cmdLine);
			if (params.outputFile != null && params.outputFile.getParentFile().isDirectory())
				pb.directory(params.outputFile.getParentFile());
			//process = pb.start();
			process = Runtime.getRuntime().exec(cmdArray);
			PMS.get().currentProcesses.add(process);
			stderrConsumer = new OutputTextConsumer(process.getErrorStream(), true);
			outConsumer = null;
			if (params.outputFile != null) {
				PMS.info("Writing in " + params.outputFile.getAbsolutePath());
				outConsumer = new OutputTextConsumer(process.getInputStream(), false);
			} else if (params.input_pipes[0] != null) {
				PMS.info("Reading pipe: " + params.input_pipes[0].getInputPipe());
				//Thread.sleep(150);
				bo = params.input_pipes[0].getDirectBuffer();
				if (bo == null || params.losslessaudio || params.lossyaudio||params.no_videoencode) {
					InputStream is = params.input_pipes[0].getInputStream();
					outConsumer = new OutputBufferConsumer(/*(params.losslessaudio||params.lossyaudio||params.no_videoencode)?new AviDemuxerInputStream(is, params, attachedProcesses):*/is, params);
					bo = (BufferedOutputFile) outConsumer.getBuffer();
				}
				bo.attachThread(this);
				new OutputTextConsumer(process.getInputStream(), true).start();
			} else if (params.log) {
				outConsumer = new OutputTextConsumer(process.getInputStream(), true);
			} else {
				outConsumer = new OutputBufferConsumer(process.getInputStream(), params);
				bo = (BufferedOutputFile) outConsumer.getBuffer();
				bo.attachThread(this);
			}
			stderrConsumer.start();
			if (outConsumer != null) 
				outConsumer.start();
			process.waitFor();
			
			try {
				if (outConsumer != null) 
					outConsumer.join(1000);
			} catch (InterruptedException e) {}
			if (bo != null)
				bo.close();
		} catch (Exception e) {
			PMS.error("Fatal error in process starting: ", e);
			stopProcess();
		} finally {
			if (!destroyed && !params.noexitcheck) {
				try {
					success = true;
					if (process != null && process.exitValue() != 0) {
						PMS.minimal("Process " + cmdArray[0] + " has a return code of " + process.exitValue() + "! Maybe an error occured... check the log file");
						success = false;
					}
				} catch (IllegalThreadStateException itse) {
					PMS.error("An error occured", itse);
				}
			}
			if (attachedProcesses != null) {
				for(ProcessWrapper pw:attachedProcesses) {
					if (pw != null)
						pw.stopProcess();
				}
			}
			PMS.get().currentProcesses.remove(process);
		}
	}
	
	public void runInNewThread() {
		this.start();
	}
	
	public InputStream getInputStream(long seek) throws IOException {
		if (bo != null)
			return bo.getInputStream(seek);
		else if (outConsumer != null && outConsumer.getBuffer() != null)
			return outConsumer.getBuffer().getInputStream(seek);
		else if (params.outputFile != null) {
			BlockerFileInputStream fIn = new BlockerFileInputStream(this, params.outputFile, params.minFileSize);
			fIn.skip(seek);
			return fIn;
		}
		return null;
	}
	
	public List<String> getOtherResults() {
		if (outConsumer == null)
			return null;
			try {
				outConsumer.join(1000);
			} catch (InterruptedException e) {}
		return outConsumer.getResults();
	}
	
	public List<String> getResults() {
		try {
			stderrConsumer.join(1000);
		} catch (InterruptedException e) {}
		return stderrConsumer.getResults();
	}

	public void stopProcess() {
		PMS.info("Stopping process: " + this);
		destroyed = true;
		if (process != null) {
			ProcessUtil.destroy(process);
		}
		/*if (params != null && params.attachedPipeName != null) {
			File pipe = new File(params.attachedPipeName);
			if (pipe.exists()) {
				if (pipe.delete())
					pipe.deleteOnExit();
			}
		}*/
		if (attachedProcesses != null) {
			for(ProcessWrapper pw:attachedProcesses) {
				if (pw != null)
					pw.stopProcess();
			}
		}
		if (outConsumer != null && outConsumer.getBuffer() != null)
			outConsumer.getBuffer().reset();
	}
	
	public boolean isDestroyed() {
		return destroyed;
	}

	public boolean isReadyToStop() {
		return nullable;
	}

	public void setReadyToStop(boolean nullable) {
		if (nullable != this.nullable)
			PMS.debug("Ready to Stop: " + nullable);
		this.nullable = nullable;
	}

}
