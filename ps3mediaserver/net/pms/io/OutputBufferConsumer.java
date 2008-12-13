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
import java.util.ArrayList;

import net.pms.PMS;

public class OutputBufferConsumer extends OutputConsumer {
	
	private BufferedOutputFile outputBuffer;
	
	public OutputBufferConsumer(InputStream inputStream, OutputParams params) {
		super(inputStream);
		outputBuffer = new BufferedOutputFile(params);
	}
	
	 public void run() {
		try {
			//PMS.debug("Starting read from pipe");
 			byte buf [] = new byte [500000];
 			int n = 0;
 			while ( (n=inputStream.read(buf)) > 0) {
 				//PMS.debug("Fetched " + n + " from pipe");
 				outputBuffer.write(buf, 0, n);
 			}
 			//PMS.info("Finished to read");
 		} catch (IOException ioe) {
 			PMS.info("Error consuming stream of spawned process:" +  ioe.getMessage());
        } finally {
        	//PMS.debug("Closing read from pipe");
            if(inputStream != null)
                try { inputStream.close(); } catch(Exception ignore) {}
        }
	 }
	
	public BufferedOutputFile getBuffer() {
		return outputBuffer;
	}

	public ArrayList<String> getResults() {
		return null;
	}
}
