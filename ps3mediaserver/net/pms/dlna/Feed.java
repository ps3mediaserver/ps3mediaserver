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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Content;
import org.jdom.Element;

import net.pms.PMS;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class Feed extends DLNAResource {
	
	@Override
	public void resolve() {
		super.resolve();
		/*String content = null;
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
			content = new String(b.toByteArray(), "UTF-8");
		} catch (IOException e) {
			PMS.error(null, e);
		}
		if (content != null) {
			parse(content);
		} else {
			//error
			
		}*/
		try {
			parse();
		} catch (Exception e) {
			PMS.error("Eror in parsing stream: " + url, e);
		}
	}
	
	
	protected String name;
	protected String url;
	protected String tempItemTitle;
	protected String tempItemLink;
	protected String tempFeedLink;
	protected String tempCategory;
	protected String tempItemThumbURL;
	
	
	public Feed(String name, String url, int type) {
		super(type);
		this.url = url;
		this.name = name;
	}
	
	@SuppressWarnings("unchecked")
	public void parse() throws Exception {
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(new URL(url)));
		name = feed.getTitle();
		if (feed.getCategories() != null && feed.getCategories().size() > 0) {
			SyndCategory category = (SyndCategory) feed.getCategories().get(0);
			tempCategory = category.getName();
		}
		List<SyndEntry> entries = feed.getEntries();
		for(SyndEntry entry:entries) {
			tempItemTitle = entry.getTitle();
			tempItemLink = entry.getLink();
			tempFeedLink = entry.getUri();
			tempItemThumbURL = null;
			
			ArrayList<Element> elements = (ArrayList<Element>) entry.getForeignMarkup();
			for(Element elt:elements) {
				if ("group".equals(elt.getName()) && "media".equals(elt.getNamespacePrefix())) {
					List<Content> subElts = elt.getContent();
					for(Content subelt:subElts) {
						if (subelt instanceof Element)
							parseElement( (Element)subelt, false);
					}
				}
				parseElement(elt, true);
			}
			List<SyndEnclosure> enclosures = entry.getEnclosures();
			for(SyndEnclosure enc:enclosures) {
				if (StringUtils.isNotBlank(enc.getUrl()))
					tempItemLink = enc.getUrl();
			}
			manageItem();
		}
		lastmodified = System.currentTimeMillis();
	}
	
	@SuppressWarnings("unchecked")
	private void parseElement(Element elt, boolean parseLink) {
		if ("content".equals(elt.getName()) && "media".equals(elt.getNamespacePrefix())) {
			if (parseLink)
				tempItemLink = elt.getAttribute("url").getValue();
			List<Content> subElts = elt.getContent();
			for(Content subelt:subElts) {
				if (subelt instanceof Element)
					parseElement( (Element)subelt, false);
			}
		}
		if ("thumbnail".equals(elt.getName()) && "media".equals(elt.getNamespacePrefix())) {
			if (tempItemThumbURL == null)
				tempItemThumbURL = elt.getAttribute("url").getValue();
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
	
	protected void manageItem() {
		FeedItem fi = new FeedItem(tempItemTitle, tempItemLink, tempItemThumbURL, null, specificType);
		addChild(fi);
	}

	@Override
	public boolean refreshChildren() {
		if (System.currentTimeMillis() - lastmodified > 3600000) {
			try {
				parse();
			} catch (Exception e) {
				PMS.error("Eror in parsing stream: " + url, e);
			}
			return true;
		}
		return false;
		
	}
}
