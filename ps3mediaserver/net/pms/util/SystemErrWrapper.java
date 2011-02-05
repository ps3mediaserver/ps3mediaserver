package net.pms.util;

import java.io.IOException;
import java.io.OutputStream;

import net.pms.PMS;

public class SystemErrWrapper extends OutputStream {
	
	private int pos = 0;
	private byte line [] = new byte [5000];

	@Override
	public void write(int b) throws IOException {
		if (b == 10) {
			byte text [] = new byte [pos];
			System.arraycopy(line, 0, text, 0, pos);
			PMS.minimal(new String(text));
			pos = 0;
			line = new byte [5000];
		} else if (b != 13) {
			line[pos] = (byte) b;
			pos++;
		}
	}



}
