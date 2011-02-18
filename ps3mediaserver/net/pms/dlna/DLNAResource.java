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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.pms.PMS;
import net.pms.configuration.FormatConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.virtual.TranscodeVirtualFolder;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.encoders.MEncoderVideo;
import net.pms.encoders.Player;
import net.pms.encoders.TSMuxerVideo;
import net.pms.encoders.VideoLanVideoStreaming;
import net.pms.external.AdditionalResourceFolderListener;
import net.pms.external.ExternalFactory;
import net.pms.external.ExternalListener;
import net.pms.external.StartStopListener;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.network.HTTPResource;
import net.pms.util.FileUtil;
import net.pms.util.ImagesUtil;
import net.pms.util.Iso639;
import net.pms.util.MpegUtil;

import org.apache.commons.lang.StringUtils;

/**
 * Represents any item that can be browsed via the UPNP ContentDirectory service.
 *
 */
public abstract class DLNAResource extends HTTPResource implements Cloneable, Runnable {
	
	protected static final int MAX_ARCHIVE_ENTRY_SIZE = 10000000;
	protected static final int MAX_ARCHIVE_SIZE_SEEK = 800000000;
	protected static String TRANSCODE_FOLDER = "#--TRANSCODE--#";
	
	/**Returns parent object, usually a folder type of resource.
	 * @return Parent object.
	 * @see #parent
	 */
	public DLNAResource getParent() {
		return parent;
	}
	protected int specificType;
	/**
	 * String representing this resource ID. This string is used by the UPNP ContentDirectory service.
	 * There is no hard spec on the actual numbering except for the root container that always has to be "0".
	 * In PMS, the format used is <i>number($number)+</i>. A common client that expects a given format,
	 * that is different that the one used here, is the XBox360. For more info, check 
	 * {@link http://www.mperfect.net/whsUpnp360/} . PMS translates the XBox360 queries on the fly.
	 */
	protected String id;
	/**
	 * @return ID string
	 * @see #id
	 */
	public String getId() {
		return id;
	}
	/**
	 * List of children objects associated with this DLNAResource. This is only valid when the DLNAResource is of the container type.
	 */
	protected ArrayList<DLNAResource> children;
	/**
	 * @return List of children objects
	 * @see #children
	 */
	public ArrayList<DLNAResource> getChildren() {
		return children;
	}
	/**
	 * In the DLDI queries, the UPNP server needs to give out the parent container where the item is. <i>parent</i> represents
	 * such a container.
	 */
	protected DLNAResource parent;
	/**
	 * @param parent Sets the parent folder.
	 * @see #parent
	 */
	public void setParent(DLNAResource parent) {
		this.parent = parent;
	}
	protected Format ext;
	public DLNAMediaInfo media;
	public DLNAMediaAudio media_audio;
	public DLNAMediaSubtitle media_subtitle;
	protected boolean notranscodefolder;
	public boolean isNotranscodefolder() {
		return notranscodefolder;
	}
	protected long lastmodified;
	public long getLastmodified() {
		return lastmodified;
	}

	/**
	 * Represents the transformation to be used to the file. If null, then 
	 * @see Player
	 */
	protected Player player;
	//protected DLNAResource original;
	public Format getExt() {
		return ext;
	}
	/**Any {@link DLNAResource} needs to represent the container or item with a String.
	 * @return String to be showed in the UPNP client.
	 */
	public abstract String getName();
	public abstract String getSystemName();
	public abstract long length();
	// Ditlew
	public long length(RendererConfiguration mediaRenderer) {return length();};	
	public abstract InputStream getInputStream() throws IOException;
	public abstract boolean isFolder();
	protected boolean discovered = false;
	private ProcessWrapper externalProcess;
	protected boolean srtFile;
	public int updateId = 1;
	public static int systemUpdateId = 1;
	public boolean noName;
	private int nametruncate;
	private DLNAResource first;
	private DLNAResource second;
	protected double splitStart;
	protected double splitLength;
	protected int splitTrack;
	protected String fakeParentId;
	// Ditlew - org
	//private RendererConfiguration defaultRenderer;
	// Ditlew - needs this in one of the derived classes
	protected RendererConfiguration defaultRenderer;
	private String dlnaspec;
	
	public String getDlnaContentFeatures() {
		return (dlnaspec!=null?(dlnaspec + ";"):"") + getFlags() + ";DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01700000000000000000000000000000";
	}
	public DLNAResource getPrimaryResource() {
		return first;
	}
	
	public DLNAResource getSecondaryResource() {
		return second;
	}
	
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
	
	/** Recursive function that searchs through all of the children until it finds
	 * a {@link DLNAResource} that matches the name.<p> Only used by {@link PMS#manageRoot(RendererConfiguration)}
	 * while parsing the web.conf file.
	 * @param name String to be compared the name to.
	 * @return Returns a {@link DLNAResource} whose name matches the parameter name
	 * @see #getName()
	 * @see PMS#manageRoot(RendererConfiguration)
	 */
	public DLNAResource searchByName(String name) {
		for(DLNAResource child:children) {
			if (child.getName().equals(name))
				return child;
		}
		return null;
	}
	
	/**
	 * @param renderer Renderer for which to check if file is supported.
	 * @return true if the given {@see RendererConfiguration} can understand type of media. Returns also true
	 * if this DLNAResource is a container.
	 */
	public boolean isCompatible(RendererConfiguration renderer) {
		return ext == null
			|| ext.isUnknown()
			|| (ext.isVideo() && renderer.isVideoSupported())
			|| (ext.isAudio() && renderer.isAudioSupported())
			|| (ext.isImage() && renderer.isImageSupported());
	}
	
	public boolean skipTranscode = false;
	protected int childrenNumber;
	private boolean allChildrenAreFolders = true;

	/**Adds a new DLNAResource to the child list. Only useful if this object is of the container type.<P>
	 * TODO: (botijo) check what happens with the child object. This function can and will transform the child
	 * object. If the transcode option is set, the child item is converted to a container with the real
	 * item and the transcode option folder. There is also a parser in order to get the right name and type,
	 * I suppose. Is this the right place to be doing things like these? 
	 * @param child DLNAResource to add to a container type.
	 */

	public void addChild(DLNAResource child) {
		// child may be null (spotted - via rootFolder.addChild() - in a misbehaving plugin

	    if (child == null) {
			PMS.error("Attempt to add a null child to " + getName(), new NullPointerException("Invalid DLNA resource"));
			return;
		}
	    
	    child.parent = this;

		if (parent != null)
			defaultRenderer = parent.defaultRenderer;
		if (child.isValid()) {
			
			PMS.info("Adding " + child.getName() + " / class: " + child.getClass().getName());
			VirtualFolder vf = null;
			//VirtualFolder vfCopy = null;

			if (allChildrenAreFolders && !child.isFolder())
				allChildrenAreFolders = false;
			
			children.add(child);
			childrenNumber++;
			
			boolean forceTranscodeV2 = false;
			boolean parserV2 = child.media != null && defaultRenderer != null && defaultRenderer.isMediaParserV2();
			if (parserV2) {
				// We already have useful info, just need to layout folders
				String mimeType = defaultRenderer.getFormatConfiguration().match(child.media);
				if (mimeType != null) {
					// This is streamable
					child.media.mimeType = mimeType.equals(FormatConfiguration.MIMETYPE_AUTO)?child.media.mimeType:mimeType;
				} else {
					// This is transcodable
					forceTranscodeV2 = true;
				}
			}
			
			if (child.ext != null)
				skipTranscode = child.ext.skip(PMS.getConfiguration().getNoTranscode(), defaultRenderer!=null?defaultRenderer.getStreamedExtensions():null);
			
			if (child.ext != null && (child.ext.transcodable() || parserV2) && (child.media == null || parserV2)) {
			
				if (!parserV2)
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
										} else if (getParent() != null && getParent().getName().endsWith(end)) {
											getParent().nametruncate = getParent().getName().lastIndexOf(end);
											pl = p;
											break;
										}
									}
								}
							}
							
						}
					}

					if (pl != null && !allChildrenAreFolders) {
						boolean forceTranscode = false;
						if (child.ext != null)
							forceTranscode = child.ext.skip(PMS.getConfiguration().getForceTranscode(), defaultRenderer!=null?defaultRenderer.getTranscodedExtensions():null);
						
						// Force transcoding if
						// 1- MediaInfo support detected the file was not matched with supported codec configs and no SkipTranscode extension forced by user
						// or 2- ForceTranscode extension forced by user
						// or 3- FFmpeg support and the file is not ps3 compatible (need to remove this ?) and no SkipTranscode extension forced by user
						// or 4- There's some sub files to deal with
						if ((forceTranscodeV2 && !skipTranscode) || forceTranscode || (!parserV2 && !child.ext.ps3compatible() && !skipTranscode) || (PMS.getConfiguration().getUseSubtitles() && child.srtFile)) {
							child.player = pl;
							PMS.info("Switching " + child.getName() + " to player: " + pl.toString());
						}
							
						if (child.ext.isVideo() && (!child.notranscodefolder) && (!PMS.getConfiguration().getHideTranscodeEnabled())) {
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
								childrenNumber++;
								vf.parent = this;
							}
							
							VirtualFolder fileFolder = new FileTranscodeVirtualFolder(child.getName(), null);
							
							
							DLNAResource newChild = (DLNAResource) child.clone();
							newChild.player = pl;
							newChild.media = child.media;
							//newChild.original = child;
							fileFolder.children.add(newChild);
							fileFolder.childrenNumber++;
							newChild.parent = fileFolder;
							PMS.info("Duplicate " + child.getName() + " with player: " + pl.toString());
							
							
							
							vf.addChild(fileFolder);
							
						}
						
						for(ExternalListener listener:ExternalFactory.getExternalListeners()) {
							if (listener instanceof AdditionalResourceFolderListener) {
								((AdditionalResourceFolderListener) listener).addAdditionalFolder(this, child);
							}
						}
						
						//}
					} else if (!child.ext.ps3compatible() && !child.isFolder()/* && !child.ext.isImage()*/) {
						children.remove(child);
					}
				//}
			}
			
			if (child.ext != null && child.ext.getSecondaryFormat() != null && child.media != null && defaultRenderer != null && defaultRenderer.supportsFormat(child.ext.getSecondaryFormat())) {
				DLNAResource newChild = (DLNAResource) child.clone();
				newChild.ext = newChild.ext.getSecondaryFormat();
				newChild.first = child;
				child.second = newChild;
				if (!newChild.ext.ps3compatible() && newChild.ext.getProfiles().size() > 0) {
					newChild.player = PMS.get().getPlayer(newChild.ext.getProfiles().get(0), newChild.ext);
				}
				if (child.media != null && child.media.secondaryFormatValid)
					addChild(newChild);
			}
		}
	}
	
	/**Recursive function that assigns proper IDs to the items. It follows the format number($number)+.
	 * It is used after all the root directories have been set. 
	 * @param index Index where to start numbering. 
	 * @param refresh If true, then the IDs are not reset to zero for each child item.
	 * @see #id
	 * @see #children
	 * @see PMS#manageRoot(RendererConfiguration)
	 */
	public synchronized void closeChildren(int index, boolean refresh) {
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
	
	/**First thing it does it searches for an item matching the given objectID.
	 * If children is false, then it returns the found object as the only object in the list.
	 * If item or children have not been discovered already, then the {@link #closeChildren(int, boolean)} is called.<p>
	 * TODO: (botijo) This function does a lot more than this!
	 * @param objectId ID to search for.
	 * @param children State if you want all the children in the returned list.
	 * @param start
	 * @param count
	 * @param renderer Renderer for which to do the actions.
	 * @return List of DLNAResource items. 
	 * @throws IOException
	 * @see #closeChildren(int, boolean)
	 */
	public synchronized ArrayList<DLNAResource> getDLNAResources(String objectId, boolean children, int start, int count, RendererConfiguration renderer) throws IOException {
		PMS.debug("Searching for objectId: " + objectId + " with children option: " +children);
		ArrayList<DLNAResource> resources = new ArrayList<DLNAResource>();
		DLNAResource resource = search(objectId);
		
		if (resource != null) {
			resource.defaultRenderer = renderer;
			
				if (!children) {
					resources.add(resource);
					if (resource.discovered) {
						if (resource.refreshChildren()) {
							resource.closeChildren(resource.childrenNumber, true);
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
						boolean ready = true;
						if (renderer.isMediaParserV2())
							ready = resource.analyzeChildren(count);
						else
							ready = resource.analyzeChildren(-1);
						resource.closeChildren(0, false);
						if (!renderer.isMediaParserV2() || ready)
							resource.discovered = true;
					} /*else {
						resource.refreshChildren();
						resource.closeChildren(resource.childrenNumber());
					}*/
					
					if (count == 0) {
	                    count = resource.children.size();
	                }
					if (count > 0) {
						ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(count);
					 
						int parallel_thread_number = 3;
						if (resource instanceof DVDISOFile)
							parallel_thread_number = 1; // my dvd drive is dying wih 3 parallel threads 
						ThreadPoolExecutor tpe = new ThreadPoolExecutor(Math.min(count, parallel_thread_number), count, 20, TimeUnit.SECONDS, queue);
						
						for(int i=start;i<start+count;i++) {
							if (i < resource.children.size()) {
								final DLNAResource child = resource.children.get(i);
								if (child != null) {
									tpe.execute(child);
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
				}
			//}
		}
		return resources;
	}
	
	@Override
	public void run() {
		if (first == null) {
			resolve();
			if (second != null)
				second.resolve();
		}
	}
	
	/**Recursive function that searches for a given ID.
	 * @param searchId ID to search for.
	 * @return Item found, or null otherwise. 
	 * @see #id
	 * 
	 */
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
	
	/**
	 * TODO: (botijo) What is the intention of this function? Looks like a prototype to be overloaded.
	 */
	public void discoverChildren() {
		
	}
	/**
	 * TODO: (botijo) What is the intention of this function? Looks like a prototype to be overloaded.
	 * @param count
	 * @return
	 */
	public boolean analyzeChildren(int count) {
		return true;
	}
	
	/**
	 * TODO: (botijo) What is the intention of this function? Looks like a prototype to be overloaded.
	 * @return
	 */
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
	
	/**
	 * TODO: (botijo) What is the intention of this function? Looks like a prototype to be overloaded.
	 * 
	 */
	public void resolve() {
		
	}
	
	/**Appends "&lt;<u>tag</u> " to the StringBuffer. This is a typical HTML/DIDL/XML tag opening.
	 * @param sb String to append the tag beginning to.
	 * @param tag String that represents the tag
	 */
	private void openTag(StringBuffer sb, String tag) {
		sb.append("&lt;");
		sb.append(tag);
	}
	
	/**Appends the closing symbol &gt; to the StringBuffer. This is a typical HTML/DIDL/XML tag closing.
	 * @param sb String to append the ending character of a tag.
	 */
	private void endTag(StringBuffer sb) {
		sb.append("&gt;");
	}
	
	/**Appends "&lt;/<u>tag</u>&gt;" to the StringBuffer. This is a typical closing HTML/DIDL/XML tag.
	 * @param sb
	 * @param tag
	 */
	private void closeTag(StringBuffer sb, String tag) {
		sb.append("&lt;/");
		sb.append(tag);
		sb.append("&gt;");
	}
	
	private void addAttribute(StringBuffer sb, String attribute, Object value) {
		sb.append(" ");
		sb.append(attribute);
		sb.append("=\"");
		sb.append(value);
		sb.append("\"");
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
	
	// Ditlew
	/**Returns the DisplayName for the default renderer.
	 * @return
	 * @see #getDisplayName(RendererConfiguration)
	 */
	public String getDisplayName() {
		return getDisplayName(null);
	}
	
	// Ditlew - org
	//public String getDisplayName() {
	// Ditlew
	/**Returns the DisplayName that is shown to the Renderer. Depending on the settings,
	 * extra info might be appended, like item duration.<p>
	 * This is based on {@link #getName()}.
	 * @param mediaRenderer Media Renderer for which to show information.
	 * @return String representing the item.
	 */
	public String getDisplayName(RendererConfiguration mediaRenderer) {
		String name = getName();
		if (this instanceof RealFile) {
			if (PMS.getConfiguration().isHideExtensions() && !isFolder())
				name = FileUtil.getFileNameWithoutExtension(name);
		}
		if (player != null) {
			if (noName)
				name = "[" + player.name() + "]";
			else {
				// Ditlew - WDTV Live don't show durations otherwize, and this is usefull for finding the main title
				if (mediaRenderer != null && mediaRenderer.isShowDVDTitleDuration() && media.dvdtrack > 0)
				{
					name += " - " + media.duration;
				}

				if (!PMS.getConfiguration().isHideEngineNames()) {
					name += " [" + player.name() + "]";
				}
			}
		} else {
			if (noName)
				name = "[No encoding]";
			else if (nametruncate > 0)
				name = name.substring(0, nametruncate).trim();
		}
		
		if (srtFile && (media_audio == null && media_subtitle == null))
			if (player == null || player.isExternalSubtitlesSupported())
				name += " {External Subtitles}"; 
		
		if (media_audio != null)
			name = (player!=null?("[" + player.name() + "]"):"") + " {Audio: " + media_audio.getAudioCodec() + "/" + media_audio.getLang() + "}";
		
		if (media_subtitle != null && media_subtitle.id != -1)
			name += " {Sub: " + media_subtitle.getSubType() + "/" + media_subtitle.getLang() + (media_subtitle.flavor!=null?("/"+media_subtitle.flavor):"") +  "}";
		
		if (avisynth)
			name = (player!=null?("[" + player.name()):"") + " + AviSynth]";
		
		if (splitStart > 0 && splitLength > 0) {
			name = ">> " + DLNAMediaInfo.getDurationString(splitStart);
		}
		
		return name;
	}
	
	/**Prototype for returning URLs.
	 * @return
	 */
	protected String getFileURL() {
		return getURL("");
	}
	
	/**
	 * @return Returns an URL pointing to a image representing the item. It none is available, "thumbnail0000.png" is used.
	 * The idea is to use is in the UPNP ContentBrowser service.
	 */
	protected String getThumbnailURL() {
		StringBuffer sb = new StringBuffer();
		sb.append(PMS.get().getServer().getURL());
		sb.append("/images/");
		String id = null;
		if (media_audio != null)
			id = media_audio.lang;
		if (media_subtitle != null && media_subtitle.id != -1)
			id = media_subtitle.lang;
		if ((media_subtitle != null || media_audio != null) && StringUtils.isBlank(id))
			id = DLNAMediaLang.UND;
		if (id != null) {
			String code = Iso639.getISO639_2Code(id.toLowerCase());
			sb.append("codes/" + code + ".png");
			return sb.toString();
		}
		if (avisynth) {
			sb.append("avisynth-logo-gears-mod.png");
			return sb.toString();
		}
		return getURL("thumbnail0000");
	}
	
	/**
	 * @param prefix
	 * @return Returns an URL for a given media item. Not used for container types.
	 */
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

	/**Transforms a String to UTF-8.
	 * @param s
	 * @return Transformed string s in UTF-8 encoding.
	 */
	private static String encode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return "";
	}
	
	/**Removes any children associated to this DLNAResource.<p>Note: (botijo) this function is not called anywhere.
	 * 
	 * @param level This integer represents how deep the items are in the tree. For the root
	 * container (level==0), you do want to keep the children. For the rest of the levels,
	 * children is set to null.
	 * @see #children
	 */
//	@Deprecated
	public void reset(int level) {
		for(DLNAResource r:children) {
			r.reset(level++);
		}
		if (level > 0)
			children.clear();
		if (level > 1)
			children = null;
	}
	
	/**
	 * @return Number of children objects. This might be used in the DLDI response, as some renderers might
	 * not have enough memory to hold the list for every children.
	 */
	public int childrenNumber() {
		if (children == null)
			return 0;
		return children.size();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
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
	
	/**Does basic transformations between characters and their HTML representation with ampersands.
	 * @param s String to be encoded
	 * @return Encoded String
	 */
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
	/**Returns a representation using DIDL response lines. It gives a complete representation of the item, with as many tags as available.
	 * Recommendations as per UPNP specification are followed where possible.
	 * @param mediaRenderer Media Renderer for which to represent this information. Useful for some hacks.
	 * @return String representing the item. An example would start like this: {@code <container id="0$1" childCount=1 parentID="0" restricted="true">}
	 */
	public String toString(RendererConfiguration mediaRenderer) {
		StringBuffer sb = new StringBuffer();
		if (isFolder())
			openTag(sb, "container");
		else
			openTag(sb, "item");
		
		addAttribute(sb, "id", id);
		if (isFolder()) {
			if (mediaRenderer.isXBMC()) // todo: make that generic ?
				addAttribute(sb, "childCount", 1);
			else
				addAttribute(sb, "childCount", childrenNumber());
		}
		addAttribute(sb, "parentID", fakeParentId!=null?fakeParentId:(parent==null?-1:parent.id));
		addAttribute(sb, "restricted", "true");
		endTag(sb);
		
		if (media != null && media.getFirstAudioTrack() != null && StringUtils.isNotBlank(media.getFirstAudioTrack().songname))
			addXMLTagAndAttribute(sb, "dc:title", encodeXML(media.getFirstAudioTrack().songname + (player!=null&&!PMS.getConfiguration().isHideEngineNames()?(" [" + player.name() + "]"):"")));
		else
			// Ditlew - org
			//addXMLTagAndAttribute(sb, "dc:title", encodeXML((isFolder()||player==null)?getDisplayName():mediaRenderer.getUseSameExtension(getDisplayName())));
			// Ditlew
			addXMLTagAndAttribute(sb, "dc:title", encodeXML((isFolder()||player==null)?getDisplayName():mediaRenderer.getUseSameExtension(getDisplayName(mediaRenderer))));
			
		if (media != null && media.getFirstAudioTrack() != null && StringUtils.isNotBlank(media.getFirstAudioTrack().album)) {
			addXMLTagAndAttribute(sb, "upnp:album", encodeXML(media.getFirstAudioTrack().album));
		}
		if (media != null && media.getFirstAudioTrack() != null && StringUtils.isNotBlank(media.getFirstAudioTrack().artist)) {
			addXMLTagAndAttribute(sb, "upnp:artist", encodeXML(media.getFirstAudioTrack().artist));
			addXMLTagAndAttribute(sb, "dc:creator", encodeXML(media.getFirstAudioTrack().artist));
		}
		if (media != null && media.getFirstAudioTrack() != null && StringUtils.isNotBlank(media.getFirstAudioTrack().genre)) {
			addXMLTagAndAttribute(sb, "upnp:genre", encodeXML(media.getFirstAudioTrack().genre));
		}
		if (media != null && media.getFirstAudioTrack() != null && media.getFirstAudioTrack().track > 0) {
			addXMLTagAndAttribute(sb, "upnp:originalTrackNumber", "" + media.getFirstAudioTrack().track);
		}
		
		if (!isFolder()) {
			int indexCount = 1;
			if (mediaRenderer.isDLNALocalizationRequired())
				indexCount = getDLNALocalesCount();
			for(int c=0;c<indexCount;c++) {
			openTag(sb, "res");
			// DLNA.ORG_OP : 1er 10 = exemple: TimeSeekRange.dlna.org :npt=187.000-
			//                   01 = Range par octets
			//                   00 = pas de range, meme pas de pause possible
			flags = "DLNA.ORG_OP=01";
			if (player != null) {
				if (player.isTimeSeekable() && mediaRenderer.isSeekByTime()) {
					if (mediaRenderer.isPS3()) // ps3 doesn't like OP=11
						flags = "DLNA.ORG_OP=10";
					else
						flags = "DLNA.ORG_OP=11";
				}
			} else {
				if (mediaRenderer.isSeekByTime() && !mediaRenderer.isPS3())
					flags = "DLNA.ORG_OP=11";
			}
			addAttribute(sb, "xmlns:dlna", "urn:schemas-dlna-org:metadata-1-0/");
			
			String mime = getRendererMimeType(mimeType(), mediaRenderer);
			if (mime == null)
				mime = "video/mpeg";
			if (mediaRenderer.isPS3()) { // TO REMOVE, OR AT LEAST MAKE THIS GENERIC // whole extensions/mime-types mess to rethink anyway
				if (mime.equals("video/x-divx"))
					dlnaspec = "DLNA.ORG_PN=AVI";
				else if (mime.equals("video/x-ms-wmv") && media != null && media.height > 700)
					dlnaspec = "DLNA.ORG_PN=WMVHIGH_PRO";
			} else {
				if (mime.equals("video/mpeg")) {
					if(player != null){
						// do we have some mpegts to offer ?
						boolean mpegTsMux = TSMuxerVideo.ID.equals(player.id()) || VideoLanVideoStreaming.ID.equals(player.id());
						if (!mpegTsMux) { // maybe, like the ps3, mencoder can launch tsmuxer if this a compatible H264 video
							mpegTsMux = MEncoderVideo.ID.equals(player.id()) && ((media_subtitle == null && media != null && media.dvdtrack == 0 && media.isMuxable(mediaRenderer)
								&& PMS.getConfiguration().isMencoderMuxWhenCompatible() && mediaRenderer.isMuxH264MpegTS())
								|| mediaRenderer.isTranscodeToMPEGTSAC3());
						}
	                  if (mpegTsMux)
	                	  dlnaspec = media.isH264()&&!VideoLanVideoStreaming.ID.equals(player.id())&&media.isMuxable(mediaRenderer)?"DLNA.ORG_PN=AVC_TS_HD_24_AC3_ISO":"DLNA.ORG_PN=" + getMPEG_TS_SD_EU_ISOLocalizedValue(c);
	                  else   
	                     dlnaspec = "DLNA.ORG_PN=" + getMPEG_PS_PALLocalizedValue(c);
	               } else if(media != null){
	                  if(media.isMpegTS())
	                     dlnaspec = media.isH264()?"DLNA.ORG_PN=AVC_TS_HD_50_AC3":"DLNA.ORG_PN=" + getMPEG_TS_SD_EULocalizedValue(c);
	                  else
	                     dlnaspec = "DLNA.ORG_PN=" + getMPEG_PS_PALLocalizedValue(c);
	               }
	               else
	            	   dlnaspec = "DLNA.ORG_PN=" + getMPEG_PS_PALLocalizedValue(c);
				} else if (mime.equals("image/jpeg"))
					dlnaspec = "DLNA.ORG_PN=JPEG_LRG";
				else if (mime.equals("audio/mpeg"))
					dlnaspec = "DLNA.ORG_PN=MP3";
				else if (mime.substring(0,9).equals("audio/L16") || mime.equals("audio/wav"))
					dlnaspec = "DLNA.ORG_PN=LPCM";
			}
			
			if(dlnaspec != null)
				dlnaspec = "DLNA.ORG_PN=" + mediaRenderer.getDLNAPN(dlnaspec.substring(12));
			
			if (!mediaRenderer.isDLNAOrgPNUsed())
				dlnaspec = null;
			
			addAttribute(sb, "protocolInfo", "http-get:*:" + mime + ":" + (dlnaspec!=null?(dlnaspec + ";"):"") + flags);
			
			
			if (ext != null && ext.isVideo() && media != null && media.mediaparsed) {
				if (player == null && media != null)
					addAttribute(sb, "size", media.size);
				else {
					long transcoded_size = mediaRenderer.getTranscodedSize();
					if (transcoded_size != 0)
						addAttribute(sb, "size", transcoded_size);
				}
				if (media.duration != null) {
					if (splitStart > 0 && splitLength > 0) {
						addAttribute(sb, "duration", DLNAMediaInfo.getDurationString(splitLength));
					} else
						addAttribute(sb, "duration", media.duration);
				}
				if (media.getResolution() != null)
					addAttribute(sb, "resolution", media.getResolution());
				addAttribute(sb, "bitrate", media.getRealVideoBitrate());
				if (media.getFirstAudioTrack() != null) {
					if (media.getFirstAudioTrack().nrAudioChannels > 0)
						addAttribute(sb, "nrAudioChannels", media.getFirstAudioTrack().nrAudioChannels);
					if (media.getFirstAudioTrack().sampleFrequency != null)
						addAttribute(sb, "sampleFrequency", media.getFirstAudioTrack().sampleFrequency);
				}
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
					if (media.getFirstAudioTrack() != null && media.getFirstAudioTrack().sampleFrequency != null)
						addAttribute(sb, "sampleFrequency", media.getFirstAudioTrack().sampleFrequency);
					if (media.getFirstAudioTrack() != null)
						addAttribute(sb, "nrAudioChannels", media.getFirstAudioTrack().nrAudioChannels);
					
					if (player == null)
						addAttribute(sb, "size", media.size);
					else {
						// calcul taille wav
						if (media.getFirstAudioTrack() != null) {
							int defaultFrequency = mediaRenderer.isTranscodeAudioTo441()?44100:48000;
							if (!PMS.getConfiguration().isAudioResample()) {
								try {
									defaultFrequency = media.getFirstAudioTrack().getSampleRate();
								} catch (Exception e) {}
							}
							int na = media.getFirstAudioTrack().nrAudioChannels;
							if (na > 2) // no 5.1 dump in mplayer
								na = 2;
							int finalsize=(int) media.getDurationInSeconds() *defaultFrequency* 2*na;
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
		}
		
		String thumbURL = getThumbnailURL();
		if (!isFolder() && (ext == null || (ext != null && thumbURL != null))) {
			openTag(sb, "upnp:albumArtURI");
			addAttribute(sb, "xmlns:dlna", "urn:schemas-dlna-org:metadata-1-0/");
			if (getThumbnailContentType().equals(PNG_TYPEMIME) && !mediaRenderer.isBRAVIA())
				addAttribute(sb, "dlna:profileID", "PNG_TN");
			else
				addAttribute(sb, "dlna:profileID", "JPEG_TN");
			endTag(sb);
			sb.append(thumbURL);
			closeTag(sb, "upnp:albumArtURI");
		}
		
		if ((isFolder() || mediaRenderer.isBRAVIA()) && thumbURL != null){
			openTag(sb, "res");
			if (getThumbnailContentType().equals(PNG_TYPEMIME) && !mediaRenderer.isBRAVIA())
				addAttribute(sb, "protocolInfo", "http-get:*:image/png:DLNA.ORG_PN=PNG_TN");
			else
				addAttribute(sb, "protocolInfo", "http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN");
			endTag(sb);
			sb.append(thumbURL);
			closeTag(sb, "res");
		}
		
		//if (isFolder()) {
		if (getLastmodified() > 0)
			addXMLTagAndAttribute(sb, "dc:date", PMS.sdfDate.format(new Date(getLastmodified())));
		//}
		
		String uclass = null;
		if (first != null && media != null && !media.secondaryFormatValid) {
			uclass = "dummy";
		} else {
			if (isFolder()) {
				uclass = "object.container.storageFolder";
				boolean xbox = mediaRenderer.isXBOX();
				if (xbox && fakeParentId != null && fakeParentId.equals("7"))
					uclass = "object.container.album.musicAlbum";
				else if (xbox && fakeParentId != null && fakeParentId.equals("6"))
					uclass = "object.container.person.musicArtist";
				else if (xbox && fakeParentId != null && fakeParentId.equals("5"))
					uclass = "object.container.genre.musicGenre";
				else if (xbox && fakeParentId != null && fakeParentId.equals("F"))
					uclass = "object.container.playlistContainer";
			} else if (ext != null && ext.isVideo()) {
				uclass = "object.item.videoItem";
			} else if (ext != null && ext.isImage()) {
				uclass = "object.item.imageItem.photo";
			} else if (ext != null && ext.isAudio()) {
				uclass = "object.item.audioItem.musicTrack";
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
	
	/**
	 * Plugin implementation. When this item is going to play, it will notify all the StartStopListener objects available.
	 * @see StartStopListener
	 */
	public void startPlaying() {
		for(ExternalListener listener:ExternalFactory.getExternalListeners()) {
			if (listener instanceof StartStopListener)
				((StartStopListener) listener).nowPlaying(media, this);
		}
	}
	
	/**
	 * Plugin implementation. When this item is going to play, it will notify all the StartStopListener objects available.
	 * @see StartStopListener
	 */
	public void stopPlaying() {
		for(ExternalListener listener:ExternalFactory.getExternalListeners()) {
			if (listener instanceof StartStopListener)
				((StartStopListener) listener).donePlaying(media, this);
		}
	}
	
	/**Returns an InputStream of this DLNAResource that starts at a given time, if possible. Very useful if video chapters are being used.
	 * @param low
	 * @param high
	 * @param timeseek
	 * @param mediarenderer
	 * @return
	 * @throws IOException
	 */
	public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
				
		PMS.debug( "Asked stream chunk [" + low + "-" + high + "] timeseek: " + timeseek + " of " + getName() + " and player " + player);
		
		// shagrath: small fix, regression on chapters
		boolean timeseek_auto = false;
		// Ditlew - WDTV Live
		// Ditlew - We convert byteoffset to timeoffset here. This needs the stream to be CBR!
		int cbr_video_bitrate = mediarenderer.getCBRVideoBitrate();
		if (player != null && low > 0 && cbr_video_bitrate > 0)
		{
			int used_bit_rated = (int)((cbr_video_bitrate + 256) * 1024 / 8 * 1.04); // 1.04 = container overhead
			if (low > used_bit_rated)
			{
				timeseek = low / (used_bit_rated);
				low = 0;

				// WDTV Live - if set to TS it ask multible times and ends by asking for a vild offset which kills mencoder
				if (timeseek > media.getDurationInSeconds())
				{
					return null;
				}   
					
				// Should we rewind a little (in case our overhead isn't accurate enough)
				int rewind_secs = mediarenderer.getByteToTimeseekRewindSeconds();
				timeseek = (timeseek > rewind_secs) ? timeseek - rewind_secs : 0;
					
				//shagrath:
				timeseek_auto = true;
				
				//PMS.debug( "Ditlew - calculated timeseek: " + timeseek);			
			}				
		}

		if (player == null) {
			
			if (this instanceof IPushOutput) {
				
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream fis = new PipedInputStream(out);
				((IPushOutput) this).push(out);
				if (low > 0 && fis != null)
					fis.skip(low);
				return fis;
			}
			
			InputStream fis = null;
			if (ext != null && ext.isImage() && media != null && media.orientation > 1 && mediarenderer.isAutoRotateBasedOnExif()) {
				// seems it's a jpeg file with an orientation setting to take care of
				fis = ImagesUtil.getAutoRotateInputStreamImage(getInputStream(), media.orientation);
				if (fis == null) // error, let's return the original one
					fis = getInputStream();
			} else
				fis = getInputStream();
			if (low > 0 && fis != null)
				fis.skip(low);
			if (timeseek != 0 && this instanceof RealFile)
				fis.skip(MpegUtil.getPossitionForTimeInMpeg(((RealFile)this).getFile(), (int) timeseek));
			return fis;
		} else {
			
			/*if (timeseek == -1) {
				if (mediarenderer == HTTPResource.PS3 && PMS.getConfiguration().isDisableFakeSize() && !(this instanceof IPushOutput) && ext != null && ext.isVideo())
					return null;
				else
					timeseek = 0;
			}
			*/
			OutputParams params = new OutputParams(PMS.getConfiguration());
			params.aid = media_audio;
			params.sid = media_subtitle;
			params.mediaRenderer = mediarenderer;
			params.timeseek = timeseek_auto?timeseek:splitStart; 
			params.timeend = splitLength;
			params.shift_scr = timeseek_auto;
			
			if (this instanceof IPushOutput)
				params.stdin = (IPushOutput) this;
				
			if (externalProcess == null || externalProcess.isDestroyed()) {
				PMS.minimal("Starting transcode/remux of " + getName());
				externalProcess = player.launchTranscode(getSystemName(), this, media, params);
				if (params.waitbeforestart > 0) {
					PMS.debug("Sleeping for " + params.waitbeforestart + " milliseconds");
					try {
						Thread.sleep(params.waitbeforestart);
					} catch (InterruptedException e) {
						PMS.error(null, e);
					}
					PMS.debug("Finished sleeping for " + params.waitbeforestart + " milliseconds");
				}
			} else if (timeseek > 0 && media != null && media.mediaparsed) {
				if (media.getDurationInSeconds() > 0) {
					
					PMS.info("Requesting Time Seeking: " + timeseek + " seconds");
					params.timeseek = timeseek;
					params.minBufferSize = 1;
					Runnable r = new Runnable() {
						public void run() {
							externalProcess.stopProcess();
						}
					};
					new Thread(r).start();
					ProcessWrapper newExternalProcess = player.launchTranscode(getSystemName(), this, media, params);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						PMS.error(null, e);
					}
					if (newExternalProcess == null)
						PMS.debug("External process instance is null... sounds not good");
					externalProcess = newExternalProcess;
				}
			}
			if (externalProcess == null)
				return null;
			InputStream is = null;
			int timer = 0;
			while (is == null && timer < 10) {
				is = externalProcess.getInputStream(low);
				timer++;
				if (is == null) {
					PMS.debug("External input stream instance is null... sounds not good, waiting 500ms");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
				}
			}

			// fail fast: don't leave a process running indefinitely if it's
			// not producing output after params.waitbeforestart milliseconds + 5 seconds
			// this cleans up lingering MEncoder web video transcode processes that hang
			// instead of exiting
			if (is == null && externalProcess != null && !externalProcess.isDestroyed()) {
				Runnable r = new Runnable() {
					public void run() {
						PMS.debug("External input stream instance is null... stopping process");
						externalProcess.stopProcess();
					}
				};
				new Thread(r).start();
			}

			return is;
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
	
	/**
	 * Prototype function. Original comment: need to override if some thumbnail works are to be done when mediaparserv2 enabled
	 */
	public void checkThumbnail() {
		// need to override if some thumbnail works are to be done when mediaparserv2 enabled
	}
	
	/**Checks if a thumbnail exists, and if possible, generates one.
	 * @param input InputFile to check or generate the thumbnail that is being asked for.
	 */
	protected void checkThumbnail(InputFile input) {
		if (media != null && !media.thumbready && PMS.getConfiguration().getThumbnailsEnabled()) {
			media.thumbready = true;
			media.generateThumbnail(input, ext, getType());
			if (media.thumb != null && PMS.getConfiguration().getUseCache() && input.file != null) {
				PMS.get().getDatabase().updateThumbnail(input.file.getAbsolutePath(), input.file.lastModified(), getType(), media);
			}
		}
	}
	
	/**TODO: (botijo) Prototype function?
	 * @return
	 * @throws IOException
	 */
	public InputStream getThumbnailInputStream() throws IOException {
		/*if (specificType == 0)
			return getResourceInputStream("images/clapperboard-256x256.png");*/
		return getResourceInputStream("images/thumbnail-256.png");
	}
	
	public String getThumbnailContentType() {
		return HTTPResource.JPEG_TYPEMIME;
	}
	public int getType() {
		if (ext != null)
			return ext.getType();
		else
			return Format.UNKNOWN;
	}
	/**Prototype function.
	 * @return true if child can be added to other folder.
	 * @see #addChild(DLNAResource)
	 */
	public abstract boolean isValid();
	
	public boolean allowScan() {
		return false;
	};
	
}
