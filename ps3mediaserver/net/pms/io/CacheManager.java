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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.pms.PMS;

public class CacheManager {
	
	private static final String CACHE_FILENAME = "PMS.CACHE";
	private static final String KEY_POSITION = "KEY_POSITION";
	
	private static final int TYPE_STRING = 0;
	private static final int TYPE_INT = 1;
	private static final int TYPE_LONG = 2;
	private static final int TYPE_DOUBLE = 3;
	private static final int TYPE_BYTES = 4;
	private static final int TYPE_STRINGLIST = 5;
	
	private static RandomAccessFile currentData;
	
	private static HashMap<String, HashMap<String, Object>> data;
	
	private synchronized static File getCacheFile() throws IOException {
		File cache = new File(PMS.get().getTempFolder(), CACHE_FILENAME);
		if (!cache.exists()) {
			FileOutputStream out = new FileOutputStream(cache);
			out.close();
		}
		return cache;
	}
	
	private synchronized static void closeFileReadOnly() throws IOException {
		if (currentData != null) {
			currentData.close();
			currentData = null;
		}
	}
	
	private synchronized static RandomAccessFile getFileReadOnly() throws IOException {
		if (currentData == null)
			currentData = new RandomAccessFile(getCacheFile(), "r");
		return currentData;
	}
	
	public synchronized static void closeCache() throws IOException {
		closeFileReadOnly();
	}
	
	public synchronized static void resetCache() throws IOException {
		closeFileReadOnly();
		if (!getCacheFile().delete()) {
			PMS.minimal("Unable to delete cache !");
		}
	}
	
	public synchronized static boolean openCache() {
		PMS.debug("Opening cache...");
		data = new HashMap<String, HashMap<String,Object>>();
		try {
			DataInputStream dataIn = new DataInputStream(new FileInputStream(getCacheFile()));
			long position = 0;
			while (dataIn.available() > 0) {
				String key = dataIn.readUTF(); position+=key.length()+2;
				HashMap<String, Object> value = new HashMap<String, Object>();
				value.put(KEY_POSITION, new Long(position));
				String version = dataIn.readUTF(); position += 2+version.length();
				int blocksize = dataIn.readInt(); position += 4;
				data.put(key, value);
				skip(dataIn, blocksize); position += blocksize;
			}
			dataIn.close();
			getFileReadOnly();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private synchronized static void skip(InputStream in, long n) throws IOException {
		long r = 0;
		while ((r=in.skip(n)) < n) {
			n = n-r;
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized static boolean manageCacheData(String key, HashMap<String, Object> element) throws IOException {
		if (data == null) {
			openCache();
			//return false;
		}
		HashMap<String, Object> value = data.get(key);
		if ( element.size() == 0 && value != null && value.size() == 1 && value.get(KEY_POSITION) != null) {
			int position = Integer.parseInt(value.get(KEY_POSITION).toString());
			value.remove(KEY_POSITION);
			RandomAccessFile r = getFileReadOnly();
			r.seek(position);
			String version = r.readUTF();
			int blocksize = r.readInt();
			int type = -1;
			int limit = position+blocksize+6+version.length();
			while ((type=r.read()) >= 0 && r.getFilePointer() < limit) {
				String a = r.readUTF();
				if (type == TYPE_STRING) {
					value.put(a, r.readUTF());
				} else if (type == TYPE_INT) {
					value.put(a, r.readInt());
				} else if (type == TYPE_DOUBLE) {
					value.put(a, r.readDouble());
				} else if (type == TYPE_LONG) {
					value.put(a, r.readLong());
				} else if (type == TYPE_BYTES) {
					int size = r.readInt();
					byte buffer [] = new byte [size];
					r.read(buffer);
					value.put(a, buffer);
				} else if (type == TYPE_STRINGLIST) {
					int size = r.readInt();
					ArrayList<String> list = new ArrayList<String>();
					for(int i=0;i<size;i++) {
						list.add(r.readUTF());
					}
					value.put(a, list);
				} else {
					PMS.minimal("Invalid type cache, seems corrupt, disabling it.");
					PMS.get().setUsecache(false);
				}
			}
			element.putAll(value);
			return true;
		} else if ( (value == null || value.size() == 0) && element.size() > 0) {
			closeFileReadOnly();
			
			String version = "1.0000";
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(bout);
			dout.writeUTF(key);
			dout.writeUTF(version);
			dout.writeInt(0);
			Iterator<Entry<String, Object>> it = element.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				if (entry.getValue() !=null){
					if (entry.getValue() instanceof String) {
						dout.write(TYPE_STRING);
						dout.writeUTF(entry.getKey());
						dout.writeUTF((String)entry.getValue());
					} else if (entry.getValue() instanceof Integer) {
						dout.write(TYPE_INT);
						dout.writeUTF(entry.getKey());
						dout.writeInt((Integer) entry.getValue());
					} else if (entry.getValue() instanceof Long) {
						dout.write(TYPE_LONG);
						dout.writeUTF(entry.getKey());
						dout.writeLong((Long) entry.getValue());
					} else if (entry.getValue() instanceof Double) {
						dout.write(TYPE_DOUBLE);
						dout.writeUTF(entry.getKey());
						dout.writeDouble((Double) entry.getValue());
					} else if (entry.getValue() instanceof byte []) {
						dout.write(TYPE_BYTES);
						dout.writeUTF(entry.getKey());
						dout.writeInt(((byte[]) entry.getValue()).length);
						dout.write((byte[]) entry.getValue());
					} else if (entry.getValue() instanceof ArrayList) {
						dout.write(TYPE_STRINGLIST);
						dout.writeUTF(entry.getKey());
						ArrayList<String> list = (ArrayList<String>) entry.getValue();
						dout.writeInt(list.size());
						for(String line:list)
							dout.writeUTF(line);
					}
				}
			}
			dout.close();
			
			long sz = getCacheFile().length();
			sz += key.length()+2;
			value = new HashMap<String, Object>();
			value.put(KEY_POSITION, sz);
			data.put(key, value);
			
			FileOutputStream fout = new FileOutputStream(getCacheFile(), true);
			byte data [] = bout.toByteArray();
			int pos = key.length() + version.length() + 4;
			ByteArrayOutputStream ba = new ByteArrayOutputStream(4);
			DataOutputStream d = new DataOutputStream(ba);
			d.writeInt(data.length-pos-4);
			System.arraycopy(ba.toByteArray(), 0, data, pos, 4);
			fout.write(data);
			fout.close();
			
			getFileReadOnly();
			return true;
		}
		return false;
	}
	
	public static void main(String args[]) {
		openCache();
		HashMap<String , Object> sample = new HashMap<String, Object>();
		sample.put("string1", "this is a test");
		sample.put("tata", new Integer(4567));
		sample.put("tata", "tuieotuioetuioetuioeutieo".getBytes());
		try {
			manageCacheData("key078", sample);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		HashMap<String , Object> sample2 = new HashMap<String, Object>();
		try {
			manageCacheData("key065", sample2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("test");
	}

}
