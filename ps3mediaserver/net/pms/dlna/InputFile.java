package net.pms.dlna;

import java.io.File;

public class InputFile {
	public File file;
	public IPushOutput push;
	public String filename;
	public long size;

	public String toString() {
		return file!=null?file.getName():(push!=null?"pipe":null);
	}
}
