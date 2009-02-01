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
package net.pms.dlna;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.CharBuffer;

import net.n3.nanoxml.IXMLBuilder;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import net.pms.PMS;

public class Feed extends DLNAResource implements IXMLBuilder {
	
	private boolean catchGeneralTitle;
	private boolean catchItem;
	private boolean catchItemTitle;
	private boolean catchItemLink;
	private boolean catchItemMediaContent;
	private boolean catchItemThumb;
	
	@Override
	public void resolve() {
		super.resolve();
		try {
			InputStream is = downloadAndSend(url, false);
			byte buf [] = new byte [4096];
			int n = -1;
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			while ((n=is.read(buf)) > 0) {
				b.write(buf, 0, n);
			}
			is.close();
			b.close();
			String content = new String(b.toByteArray(), "UTF-8");
			parse(content);
		} catch (IOException e) {
			PMS.error(null, e);
		}
		
	}
	
	/*protected String [] extractText(String pattern) {
		
	}*/

	protected String name;
	protected String url;
	protected String title;
	
	protected String tempItemTitle;
	protected String tempItemLink;
	protected String tempFeedLink;
	protected String tempItemThumbURL;
	//private DLNAMediaInfo dlna;
	
	public Feed(String name, String url, int type) {
		super(type);
		this.url = url;
		this.name = name;
	}
	
	public void parse(String content) {
		try {
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			IXMLReader reader = StdXMLReader.stringReader(content);
			parser.setReader(reader);
			parser.setBuilder(this);
			parser.parse();
		} catch (Exception e) {
			PMS.error("Error in parsing " + url, e);
		}
	
	}

	public InputStream getInputStream() throws IOException {
		return null;
	}

	public String getName() {
		return name;
	}

	public boolean isFolder() {
		return true;
	}

	public long length() {
		return 0;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return url;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	public void addAttribute(String s, String s1, String s2, String s3,
			String s4) throws Exception {
		if (catchItemThumb && s.equals("url")) {
			tempItemThumbURL = s3;
		} else if (catchItemMediaContent && s.equals("url") && (tempItemLink == null || !tempItemLink.contains("youtube"))) {
			tempItemLink = s3;
			catchItemMediaContent = false;
		} else if (catchItemLink && s.equals("href")) {
			tempItemLink = s3;
		}
	}
	
	private String getString(Reader r) throws IOException {
		CharBuffer cb = CharBuffer.allocate(1000);
		r.read(cb);
		return new String(cb.array()).trim();
	}

	public void addPCData(Reader reader, String s, int i) throws Exception {
		if (catchGeneralTitle) {
			title = getString(reader);
			name = title;
		} else if (catchItem && catchItemLink) {
			if (tempItemLink == null)
				tempItemLink = getString(reader);
			catchItemLink = false;
		} else if (catchItem && catchItemTitle) {
			tempItemTitle = getString(reader);
			catchItemTitle = false;
		}
	}

	public void elementAttributesProcessed(String s, String s1, String s2)
			throws Exception {
		
	}

	public void endElement(String s, String s1, String s2) throws Exception {
		if (s.equals("title"))
			catchGeneralTitle = false;
		else if (catchItem && (s.equals("item") || s.equals("entry"))) {
			catchItem = false;
			manageItem();
		} else if (catchItem && s.equals("title")) {
			catchItemTitle = false;
		} else if (catchItem && s.equals("link")) {
			catchItemLink = false;
		} else if (catchItem && s.equals("thumbnail") && s1 != null && s1.equals("media")) {
			catchItemThumb = false;
		} else if ((catchItem && s.equals("content") && s1 != null && s1.equals("media")) || (catchItem && s.equals("enclosure"))) {
			catchItemMediaContent = false;
		}
	}

	public Object getResult() throws Exception {
		return null;
	}

	public void newProcessingInstruction(String s, Reader reader)
			throws Exception {
		
	}

	public void startBuilding(String s, int i) throws Exception {
		
	}

	public void startElement(String s, String s1, String s2, String s3, int i)
			throws Exception {
		if (title == null && s.equals("title"))
			catchGeneralTitle = true;
		else if (!catchItem && (s.equals("item") || s.equals("entry"))) {
			catchItem = true;
		} else if (catchItem && s.equals("title")) {
			catchItemTitle = true;
		} else if (catchItem && s.equals("link")) {
			catchItemLink = true;
			tempItemLink = null;
		} else if (catchItem && s.equals("thumbnail") && s1 != null && s1.equals("media")) {
			catchItemThumb = true;
		} else if ((catchItem && s.equals("content") && s1 != null && s1.equals("media")) || (catchItem && s.equals("enclosure"))) {
			catchItemMediaContent = true;
		}
	}
	
	protected void manageItem() {
		FeedItem fi = new FeedItem(tempItemTitle, tempItemLink, tempItemThumbURL, null, specificType);
		addChild(fi);
	}
}
