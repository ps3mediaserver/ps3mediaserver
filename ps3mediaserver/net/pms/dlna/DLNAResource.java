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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import net.pms.PMS;
import net.pms.dlna.virtual.TranscodeVirtualFolder;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.encoders.Player;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.network.HTTPResource;
import net.pms.util.FileUtil;

public abstract class DLNAResource extends HTTPResource implements Cloneable {
	
	protected static final int MAX_ARCHIVE_ENTRY_SIZE = 10000000;
	protected static String TRANSCODE_FOLDER = "#--TRANSCODED--#";
	
	public DLNAResource getParent() {
		return parent;
	}
	protected int specificType;
	protected String id;
	protected ArrayList<DLNAResource> children;
	protected DLNAResource parent;
	protected Format ext;
	public DLNAMediaInfo media;
	protected boolean notranscodefolder;
	protected long lastmodified;
	public long getLastmodified() {
		return lastmodified;
	}

	protected Player player;
	//protected DLNAResource original;
	public Format getExt() {
		return ext;
	}
	public abstract String getName();
	public abstract String getSystemName();
	public abstract long length();
	public abstract InputStream getInputStream() throws IOException;
	public abstract boolean isFolder();
	
	protected boolean discovered = false;
	private ProcessWrapper externalProcess;
	protected String ifoFileURI;
	protected boolean srtFile;
	public int updateId = 1;
	public static int systemUpdateId = 1;
	public boolean noName;
	private int nametruncate;
	private boolean second;
	protected int aid = -1;
	protected String alang;
	protected int sid = -1;
	protected String slang;
	protected String aformat;
	protected String fakeParentId;
	
	public String getFakeParentId() {
		return fakeParentId;
	}
	public void setFakeParentId(String fakeParentId) {
		this.fakeParentId = fakeParentId;
	}
	protected boolean avisynth;
	
	public int getUpdateId() {
		return updateId;
	}
	public DLNAResource() {
		specificType = Format.UNKNOWN;
		children = new ArrayList<DLNAResource>();
		updateId = 1;
	}
	
	public DLNAResource(int specificType) {
		this();
		this.specificType = specificType;
	}
	
	public DLNAResource searchByName(String name) {
		for(DLNAResource child:children) {
			if (child.getName().equals(name))
				return child;
		}
		return null;
	}
	
	public void addChild(DLNAResource child) {
		//child.expert = expert;
		if (child.isValid()) {
			PMS.info("Adding " + child.getName() + " / class: " + child.getClass().getName());
			VirtualFolder vf = null;
			
			children.add(child);
			child.parent = this;
			
			boolean skipTranscode = false;
			if (child.ext != null)
				skipTranscode = child.ext.skip(PMS.getConfiguration().getNoTranscode());
			
			if (!skipTranscode && child.ext != null && child.ext.transcodable() && child.media == null) {
			
				child.media = new DLNAMediaInfo();
				
				
					Player pl = null;
					if (pl == null) {
						if (child.ext.getProfiles() != null && child.ext.getProfiles().size() > 0) {
							int i=0;
							while (pl == null && i < child.ext.getProfiles().size()) {
								pl = PMS.get().getPlayer(child.ext.getProfiles().get(i), child.ext);
								i++;
							}
							String name = getName();
							for(Class<? extends Player> clazz:child.ext.getProfiles()) {
								for(Player p:PMS.get().getPlayers()) {
									if (p.getClass().equals(clazz)) {
										String end = "[" + p.id() + "]";
										if (name.endsWith(end)) {
											nametruncate = name.lastIndexOf(end);
											pl = p;
											break;
										}
									}
								}
							}
							
						}
					}
					if (pl != null) {
						boolean forceTranscode = false;
						if (child.ext != null)
							forceTranscode = child.ext.skip(PMS.getConfiguration().getForceTranscode());
						
						if (forceTranscode || !child.ext.ps3compatible() || (PMS.getConfiguration().getUseSubtitles() && child.srtFile)) {
							child.player = pl;
							PMS.info("Switching " + child.getName() + " to player: " + pl.toString());
						}
							
						if (child.ext.isVideo() && !child.notranscodefolder) {
							//search for transcode folder
							for(DLNAResource r:children) {
								if (r instanceof TranscodeVirtualFolder) {
									vf = (TranscodeVirtualFolder) r;
									break;
								}
							}
							if (vf == null) {
								vf = new TranscodeVirtualFolder(null);
								children.add(vf);
								vf.parent = this;
							}
							
							VirtualFolder fileFolder = new FileTranscodeVirtualFolder(child.getName(), null);
							
							
							DLNAResource newChild = (DLNAResource) child.clone();
							newChild.player = pl;
							newChild.media = child.media;
							//newChild.original = child;
							fileFolder.children.add(newChild);
							newChild.parent = fileFolder;
							PMS.info("Duplicate " + child.getName() + " with player: " + pl.toString());
							
							
							
							vf.addChild(fileFolder);
						}
						//}
					} else if (!child.ext.ps3compatible()/* && !child.ext.isImage()*/) {
						children.remove(child);
					}
				//}
			}
			
			if (child.ext != null && child.ext.getSecondaryFormat() != null && child.media != null) {
				DLNAResource newChild = (DLNAResource) child.clone();
				newChild.ext = newChild.ext.getSecondaryFormat();
				newChild.second = true;
				if (!newChild.ext.ps3compatible() && newChild.ext.getProfiles().size() > 0) {
					newChild.player = PMS.get().getPlayer(newChild.ext.getProfiles().get(0), newChild.ext);
				}
				addChild(newChild);
			}
		}
	}
	
	public void closeChildren(int index, boolean refresh) {
		if (id == null || id.equals("0")) {
			if (parent != null) {
				id = parent.id + "$" + index;
				PMS.debug("Setting DLNA id " + id + " to " + getName());
			}
		}
		if (!refresh)
			index = 0;
		if (children != null)
			for(DLNAResource f:children) {
				f.closeChildren(index++, false);
			}
	}
	
	public ArrayList<DLNAResource> getDLNAResources(String objectId, boolean children, int start, int count) throws IOException {
		PMS.debug("Searching for objectId: " + objectId + " with children option: " +children);
		ArrayList<DLNAResource> resources = new ArrayList<DLNAResource>();
		DLNAResource resource = search(objectId);
		
		if (resource != null) {
			/*if (resource instanceof VirtualFile) {
				((VirtualFile) resource).action();
			} else {*/
				if (!children) {
					resources.add(resource);
					if (resource.discovered) {
						if (resource.refreshChildren()) {
							resource.closeChildren(resource.childrenNumber(), true);
							resource.updateId++;
							/*TranscodeVirtualFolder vf = null;
							for(DLNAResource r:resource.children) {
								if (r instanceof TranscodeVirtualFolder) {
									vf = (TranscodeVirtualFolder) r;
									break;
								}
							}
							if (vf != null) {
								vf.closeChildren(vf.childrenNumber(), true);
								vf.updateId++;
							}*/
							systemUpdateId++;
						}
					}
				}
				else {
					// Discovering if not already done.
					if (!resource.discovered) {
						resource.discoverChildren();
						resource.closeChildren(0, false);
						resource.discovered = true;
					} /*else {
						resource.refreshChildren();
						resource.closeChildren(resource.childrenNumber());
					}*/
					
					if (count == 0) {
	                    count = resource.children.size();
	                }
					ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(count);
				 
					int parallel_thread_number = 3;
					if (resource instanceof DVDISOFile)
						parallel_thread_number = 1; // my dvd drive is dying wih 3 parallel threads 
					ThreadPoolExecutor tpe = new ThreadPoolExecutor(Math.min(count, parallel_thread_number), count, 20, TimeUnit.SECONDS, queue);
					
					for(int i=start;i<start+count;i++) {
						if (i < resource.children.size()) {
							final DLNAResource child = resource.children.get(i);
							if (child != null) {
								tpe.execute(new Runnable() {
									public void run() {
										child.resolve();
									}
								});
								resources.add(child);
							}
						}
					}
					try {
						tpe.shutdown();
						tpe.awaitTermination(20, TimeUnit.SECONDS);
					} catch (InterruptedException e) {}
					PMS.debug("End of analysis");
				}
			//}
		}
		return resources;
	}
	
	public DLNAResource search(String searchId) {
		DLNAResource found = null;
		if (id != null) {
			if (id.equals(searchId))
				return this;
			else {
				for(DLNAResource file:children) {
					found = file.search(searchId);
					if (found != null)
						break;
				}
			}
		}
		return found;
	}
	
	public void discoverChildren() {
		
	}
	
	public boolean refreshChildren() {
		return false;
	}
	
	protected void checktype() {
		if (ext == null)
			ext = PMS.get().getAssociatedExtension(getSystemName());
		if (ext != null) {
			if (ext.isUnknown())
				ext.setType(specificType);
		}
	}
	
	public void resolve() {
		
	}
	
	private void openTag(StringBuffer sb, String tag) {
		sb.append("&lt;");
		sb.append(tag);
		sb.append(" ");
	}
	
	private void endTag(StringBuffer sb) {
		sb.append("&gt;");
	}
	
	private void closeTag(StringBuffer sb, String tag) {
		sb.append("&lt;/");
		sb.append(tag);
		sb.append("&gt;");
	}
	
	private void addAttribute(StringBuffer sb, String attribute, Object value) {
		sb.append(attribute);
		sb.append("=\"");
		sb.append(value);
		sb.append("\" ");
	}
	
	private void addXMLTagAndAttribute(StringBuffer sb, String tag, Object value) {
		sb.append("&lt;");
		sb.append(tag);
		sb.append("&gt;");
		sb.append(value);
		sb.append("&lt;/");
		sb.append(tag);
		sb.append("&gt;");
	}
	
	public String getDisplayName() {
		String name = getName();
		if (this instanceof RealFile) {
			if (PMS.getConfiguration().isHideExtensions())
				name = FileUtil.getFileNameWithoutExtension(name);
		}
		if (player != null) {
			if (noName)
				name = " [" + player.name() + "]";
			else {
				if (!PMS.getConfiguration().isHideEngineNames()) {
					name += " [" + player.name() + "]";
				}
			}
		} else {
			if (nametruncate > 0)
				name = name.substring(0, nametruncate).trim();
		}
		
		if (srtFile && (alang == null && slang == null))
			name += " { subtitles found }"; 
		
		if (alang != null)
			name = (player!=null?player.name():"") + " {audio: " + (aformat!=null?(aformat+" "):"") + alang + "}";
		
		if (slang != null)
			name += " {subs: " + slang + "}";
		
		if (avisynth)
			name = (player!=null?player.name():"") + " + AviSynth";
		
		return name;
	}
	
	protected String getFileURL() {
		return getURL("");
	}
	
	protected String getThumbnailURL() {
		StringBuffer sb = new StringBuffer();
		sb.append(PMS.get().getServer().getURL());
		sb.append("/images/");
		String id = alang;
		if (slang != null)
			id = slang;
		if (id != null) {
			String code = id.toLowerCase();
			if (code.length() == 2) {
				code=code.equals("fr")?"fre":code.equals("en")?"eng":code.equals("de")?"ger":code.equals("ja")?"jpn":code.equals("it")?"ita":code.equals("es")?"spa":code.equals("nl")?"dut":code.equals("no")?"nor":code.equals("sv")?"swe":code.equals("da")?"dan":"nul";
			}
			sb.append("codes/" + code + ".png");
			return sb.toString();
		}
		if (avisynth) {
			sb.append("avisynth-logo-gears-mod.png");
			return sb.toString();
		}
		return getURL("thumbnail0000");
	}
	
	protected String getURL(String prefix) {
		StringBuffer sb = new StringBuffer();
		sb.append(PMS.get().getServer().getURL());
		sb.append("/get/");
		sb.append(id); //id
		sb.append("/");
		sb.append(prefix);
		sb.append(encode(getName()));
		return sb.toString();
	}

	private static String encode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return "";
	}
	
	public void reset(int level) {
		for(DLNAResource r:children) {
			r.reset(level++);
		}
		if (level > 0)
			children.clear();
		if (level > 1)
			children = null;
	}
	
	public int childrenNumber() {
		if (children == null)
			return 0;
		return children.size();
	}
	@Override
	protected Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException e) {
			PMS.error(null, e);
		}
		return o;
	}
	
	private String encodeXML(String s) {
		
		s = s.replace("&", "&amp;"); 
		s = s.replace("<", "&lt;"); 
		s = s.replace(">", "&gt;"); 
		s = s.replace("\"", "&quot;"); 
		s = s.replace("'", "&apos;"); 
		s = s.replace("&", "&amp;");
		
		return s;
	}
	
	private String flags;
	
	public String getFlags() {
		return flags;
	}
	public String toString(int mediaRenderer) {
		StringBuffer sb = new StringBuffer();
		if (isFolder())
			openTag(sb, "container");
		else
			openTag(sb, "item");
		
		addAttribute(sb, "id", id);
		if (isFolder()) {
			addAttribute(sb, "childCount", childrenNumber());
		}
		addAttribute(sb, "parentId", fakeParentId!=null?fakeParentId:(parent==null?-1:parent.id));
		addAttribute(sb, "restricted", "true");
		endTag(sb);
		
		if (media != null && StringUtils.isNotBlank(media.songname))
			addXMLTagAndAttribute(sb, "dc:title", encodeXML(media.songname + (player!=null&&!PMS.getConfiguration().isHideEngineNames()?(" [" + player.name() + "]"):"")));
		else
			addXMLTagAndAttribute(sb, "dc:title", encodeXML(getDisplayName()));
			
		if (media != null && StringUtils.isNotBlank(media.album)) {
			addXMLTagAndAttribute(sb, "upnp:album", encodeXML(media.album));
		}
		if (media != null && StringUtils.isNotBlank(media.artist)) {
			addXMLTagAndAttribute(sb, "upnp:artist", encodeXML(media.artist));
		}
		if (media != null && StringUtils.isNotBlank(media.genre)) {
			addXMLTagAndAttribute(sb, "upnp:genre", encodeXML(media.genre));
		}
		if (media != null && media.track > 0) {
			addXMLTagAndAttribute(sb, "upnp:originalTrackNumber", "" + media.track);
		}
		
		if (!isFolder()) {
			openTag(sb, "res");
			// DLNA.ORG_OP : 1er 10 = exemple: TimeSeekRange.dlna.org :npt=187.000-
			//                   01 = Range par octets
			//                   00 = pas de range, meme pas de pause possible
			flags = ":DLNA.ORG_OP=01";
			if (player != null) {
				if (player.isTimeSeekable())
					flags = ":DLNA.ORG_OP=10";
				/*else
					flags = ":DLNA.ORG_OP=00";*/ // 00 not working with ps3
			}
			addAttribute(sb, "xmlns:dlna", "urn:schemas-dlna-org:metadata-1-0/");
			
			//flags += ";DLNA.ORG_PN=MPEG_TS_PAL";
			addAttribute(sb, "protocolInfo", "http-get:*:" + getRendererMimeType(mimeType(), mediaRenderer) + flags);
			
			if (ifoFileURI != null) {
				 // not working with ps3 it seems
				addAttribute(sb, "dlna:ifoFileURI", getURL("ifo0000") + ".ifo");
			}
			
			if (ext != null && ext.isVideo() && media != null && media.mediaparsed) {
				if (player == null && media != null)
					addAttribute(sb, "size", media.size);
				else
					addAttribute(sb, "size", DLNAMediaInfo.TRANS_SIZE);
				if (media.duration != null)
					addAttribute(sb, "duration", media.duration);
				if (media.getResolution() != null)
					addAttribute(sb, "resolution", media.getResolution());
				if (media.nrAudioChannels > 0)
					addAttribute(sb, "nrAudioChannels", media.nrAudioChannels);
				if (player == null)
					addAttribute(sb, "bitrate", media.bitrate);
				else
					addAttribute(sb, "bitrate", "1000000");
				if (media.sampleFrequency != null)
					addAttribute(sb, "sampleFrequency", media.sampleFrequency);
			} else if (ext != null && ext.isImage()) {
				if (media != null && media.mediaparsed) {
					addAttribute(sb, "size", media.size);
					if (media.getResolution() != null)
						addAttribute(sb, "resolution", media.getResolution());
				} else
					addAttribute(sb, "size", length());
			} else if (ext != null && ext.isAudio()) {
				if (media != null && media.mediaparsed) {
					addAttribute(sb, "bitrate", media.bitrate);
					if (media.duration != null)
						addAttribute(sb, "duration", media.duration);
					if (media.sampleFrequency != null)
						addAttribute(sb, "sampleFrequency", media.sampleFrequency);
					addAttribute(sb, "nrAudioChannels", media.nrAudioChannels);
					
					if (player == null)
						addAttribute(sb, "size", media.size);
					else {
						// calcul taille wav
						if (media.sampleFrequency != null) {
							int ns = Integer.parseInt(media.sampleFrequency);
							int finalsize=(int) media.getDurationInSeconds() *ns* 2*media.nrAudioChannels;
							PMS.info("Calculated size: " + finalsize);
							addAttribute(sb, "size", finalsize);
						}
					}
				} else
					addAttribute(sb, "size", length());
			} else {
				addAttribute(sb, "size", DLNAMediaInfo.TRANS_SIZE);
				addAttribute(sb, "duration", "09:59:59");
				addAttribute(sb, "bitrate", "1000000");
			}
			endTag(sb);
			sb.append(getFileURL());
			closeTag(sb, "res");

		}
		
		String thumbURL = getThumbnailURL();
		if (ext == null || (ext != null && thumbURL != null)) {
			openTag(sb, "upnp:albumArtURI");
			addAttribute(sb, "xmlns:dlna", "urn:schemas-dlna-org:metadata-1-0/");
			if (getThumbnailContentType().equals(PNG_TYPEMIME))
				addAttribute(sb, "dlna:profileID", "PNG_TN");
			else
				addAttribute(sb, "dlna:profileID", "JPEG_TN");
			endTag(sb);
			sb.append(thumbURL);
			closeTag(sb, "upnp:albumArtURI");
		}
		
		//if (isFolder()) {
		if (getLastmodified() > 0)
			addXMLTagAndAttribute(sb, "dc:date", PMS.sdfDate.format(new Date(getLastmodified())));
		//}
		
		String uclass = null;
		if (second && media != null && !media.secondaryFormatValid) {
			uclass = "dummy";
		} else {
			if (isFolder()) {
				uclass = "object.container.storageFolder";
			} else if (ext != null && ext.isVideo()) {
				uclass = "object.item.videoItem";
			} else if (ext != null && ext.isImage()) {
				uclass = "object.item.imageItem";
			} else if (ext != null && ext.isAudio()) {
				uclass = "object.item.audioItem";
			} else
				uclass = "object.item.videoItem";
		}
		if (uclass != null)
			addXMLTagAndAttribute(sb, "upnp:class", uclass);
		
		if (isFolder())
			closeTag(sb, "container");
		else
			closeTag(sb, "item");
		
		return sb.toString();
	}
	
	public InputStream getInputStream(long low, long high, double timeseek, int mediarenderer) throws IOException {
		
		PMS.debug( "Asked stream chunk [" + low + "-" + high + "] timeseek: " + timeseek + " of " + getName() + " and player " + player);
		
		if (player == null) {
			
			InputStream fis = getInputStream();
			if (low > 0 && fis != null)
				fis.skip(low);
			return fis;
		} else {
			
			OutputParams params = new OutputParams(PMS.getConfiguration());
			params.aid = aid;
			params.sid = sid;
			params.mediaRenderer = mediarenderer;
			
			if (externalProcess == null || externalProcess.isDestroyed()) {
				PMS.minimal("Starting transcode/remux of " + getName());
				externalProcess = player.launchTranscode(getSystemName(), media, params);
				try {
					Thread.sleep(params.waitbeforestart);
				} catch (InterruptedException e) {
					PMS.error(null, e);
				}
			} else if (timeseek > 0 && media != null && media.mediaparsed) {
				if (media.getDurationInSeconds() > 0) {
					
					PMS.info("Requesting Time Seeking: " + timeseek + " seconds");
					params.timeseek = timeseek;
					params.minBufferSize = 1;
					externalProcess.stopProcess();
					externalProcess = player.launchTranscode(getSystemName(), media, params);
					try {
						Thread.sleep(800);
					} catch (InterruptedException e) {
						PMS.error(null, e);
					}
				}
			}
			if (externalProcess == null)
				return null;
			return externalProcess.getInputStream(low);
			
		}
		
	
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public String mimeType() {
		if (player != null)
			return player.mimeType();
		else if (media != null && media.mediaparsed)
			return media.mimeType;
		else if (ext != null)
			return ext.mimeType();
		else
			return getDefaultMimeType(specificType);
	}
	
	public InputStream getThumbnailInputStream() throws IOException {
		/*if (specificType == 0)
			return getResourceInputStream("images/clapperboard-256x256.png");*/
		return getResourceInputStream("images/Play1Hot_256.png");
	}
	
	public String getThumbnailContentType() {
		return HTTPResource.PNG_TYPEMIME;
	}
	public int getType() {
		if (ext != null)
			return ext.getType();
		else
			return Format.UNKNOWN;
	}
	public abstract boolean isValid();
	
}
