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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import net.pms.io.SizeLimitInputStream;
import net.pms.network.HTTPResource;
import net.pms.util.FileUtil;
import net.pms.util.ImagesUtil;
import net.pms.util.Iso639;
import net.pms.util.MpegUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.pms.util.StringUtil.*;

/**
 * Represents any item that can be browsed via the UPNP ContentDirectory service.
 *
 */
public abstract class DLNAResource extends HTTPResource implements Cloneable, Runnable {
	private static final Logger logger = LoggerFactory.getLogger(DLNAResource.class);
	protected static final int MAX_ARCHIVE_ENTRY_SIZE = 10000000;
	protected static final int MAX_ARCHIVE_SIZE_SEEK = 800000000;
	protected static final String TRANSCODE_FOLDER = "#--TRANSCODE--#";
	private Map<String, Integer> requestIdToRefcount = new HashMap<String, Integer>();
	private static final int STOP_PLAYING_DELAY = 4000;

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
	 * In the DLDI queries, the UPNP server needs to give out the parent container where the item is. <i>parent</i> represents
	 * such a container.
	 */
	protected DLNAResource parent;

	protected Format ext;
	protected DLNAMediaInfo media;
	protected DLNAMediaAudio media_audio;
	protected DLNAMediaSubtitle media_subtitle;
	protected long lastmodified;

	/**
	 * Represents the transformation to be used to the file. If null, then 
	 * @see Player
	 */
	protected Player player;

	protected boolean discovered = false;
	private ProcessWrapper externalProcess;
	protected boolean srtFile;
	protected int updateId = 1;
	public static int systemUpdateId = 1;
	protected boolean noName;
	private int nametruncate;
	private DLNAResource first;
	private DLNAResource second;
	protected double splitStart;
	protected double splitLength;
	protected int splitTrack;
	protected String fakeParentId;
	// Ditlew - needs this in one of the derived classes
	protected RendererConfiguration defaultRenderer;
	private String dlnaspec;

	protected boolean avisynth;

	protected boolean skipTranscode = false;
	protected int childrenNumber;
	private boolean allChildrenAreFolders = true;
	private String flags;

	/**
	 * List of children objects associated with this DLNAResource. This is only valid when the DLNAResource is of the container type.
	 */
	protected List<DLNAResource> children;
	
	/**
	 * the id which the last child got, so the next child can get unique id with incrementing this value.
	 */
	protected int lastChildrenId;
	
	/**
	 * The last time when refresh is called.
	 */
	protected long lastRefreshTime;

	/**Returns parent object, usually a folder type of resource.
	 * @return Parent object.
	 * @see #parent
	 */
	public DLNAResource getParent() {
		return parent;
	}

	/**
	 * @return ID string
	 * @see #id
	 */
	public String getId() {
		return parent != null ? parent.getId() + '$' + id : id;
	}
	
	/**
	 * Set the id based on the index in their parent container. Main purpose
	 * is to be unique in the parent container, it's called automaticly 
	 * by addChildInternal, so most of the time it's unnecessary to call
	 * 
	 * @see #addChildInternal(DLNAResource)
	 * @param id
	 */
	protected void setIndexId(int id) {
		this.id = Integer.toString(id);
	}

	/**
	 * 
	 * @return the unique id which identifies the DLNAResource relative to it's parent. 
	 */
	public String getInternalId() {
		return id;
	}

	/**
	 * @return List of children objects
	 * @see #children
	 */
	public List<DLNAResource> getChildren() {
		return children;
	}

	/**
	 * @param parent Sets the parent folder.
	 * @see #parent
	 */
	public void setParent(DLNAResource parent) {
		this.parent = parent;
	}

	/**
	 * 
	 * @return true, if this contain can have a transcode folder
	 */
	public boolean isTranscodeFolderAvailable() {
		return true;
	}

	public long getLastmodified() {
		return lastmodified;
	}

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
	public long length(RendererConfiguration mediaRenderer) {
		return length();
	}

	public abstract InputStream getInputStream() throws IOException;

	public abstract boolean isFolder();

	public String getDlnaContentFeatures() {
		return (dlnaspec != null ? (dlnaspec + ";") : "") + getFlags() + ";DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01700000000000000000000000000000";
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

	public int getUpdateId() {
		return updateId;
	}

	public DLNAMediaInfo getMedia() {
		return media;
	}

	public boolean isNoName() {
		return noName;
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
		for (DLNAResource child : children) {
			if (child.getName().equals(name)) {
				return child;
			}
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
			logger.error("Attempt to add a null child to " + getName(), new NullPointerException("Invalid DLNA resource"));
			return;
		}

		child.parent = this;

		if (parent != null) {
			defaultRenderer = parent.defaultRenderer;
		}
		if (child.isValid()) {
			logger.trace("Adding " + child.getName() + " / class: " + child.getClass().getName());
			VirtualFolder vf = null;
			//VirtualFolder vfCopy = null;

			if (allChildrenAreFolders && !child.isFolder()) {
				allChildrenAreFolders = false;
			}
			addChildInternal(child);

			boolean forceTranscodeV2 = false;
			boolean parserV2 = child.media != null && defaultRenderer != null && defaultRenderer.isMediaParserV2();
			if (parserV2) {
				// We already have useful info, just need to layout folders
				String mimeType = defaultRenderer.getFormatConfiguration().match(child.media);
				if (mimeType != null) {
					// This is streamable
					child.media.mimeType = mimeType.equals(FormatConfiguration.MIMETYPE_AUTO) ? child.media.mimeType : mimeType;
				} else {
					// This is transcodable
					forceTranscodeV2 = true;
				}
			}

			if (child.ext != null) {
				skipTranscode = child.ext.skip(PMS.getConfiguration().getNoTranscode(), defaultRenderer != null ? defaultRenderer.getStreamedExtensions() : null);
			}

			if (child.ext != null && (child.ext.transcodable() || parserV2) && (child.media == null || parserV2)) {

				if (!parserV2) {
					child.media = new DLNAMediaInfo();
				}

				// Try to determine a player to use for transcoding. 
				Player pl = null;
				
				if (child.ext.getProfiles() != null && child.ext.getProfiles().size() > 0) {
					// First try to match a player based on the format profiles.
					int i = 0;
					
					while (pl == null && i < child.ext.getProfiles().size()) {
						pl = PMS.get().getPlayer(child.ext.getProfiles().get(i), child.ext);
						i++;
					}
					
					// Next, try to match a player based on the name of the DLNAResource.
					// When a match is found it overrules the result of the first try.
					String name = getName();
					
					for (Class<? extends Player> clazz : child.ext.getProfiles()) {
						for (Player p : PMS.get().getPlayers()) {
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

				if (pl != null && !allChildrenAreFolders) {
					boolean forceTranscode = false;
					if (child.ext != null) {
						forceTranscode = child.ext.skip(PMS.getConfiguration().getForceTranscode(), defaultRenderer != null ? defaultRenderer.getTranscodedExtensions() : null);
					}

					boolean hasEmbeddedSubs = false;
					if (child.media != null) {
						for(DLNAMediaSubtitle s:child.media.subtitlesCodes) {
							hasEmbeddedSubs |= s.getSubType().equals("Embedded");
						}
					}

					// Force transcoding if
					// 1- MediaInfo support detected the file was not matched with supported codec configs and no SkipTranscode extension forced by user
					// or 2- ForceTranscode extension forced by user
					// or 3- FFmpeg support and the file is not ps3 compatible (need to remove this ?) and no SkipTranscode extension forced by user
					// or 4- There's some sub files or embedded subs to deal with and no SkipTranscode extension forced by user
					if (forceTranscode || !skipTranscode && (forceTranscodeV2 || (!parserV2 && !child.ext.ps3compatible()) || (PMS.getConfiguration().getUseSubtitles() && child.srtFile) || hasEmbeddedSubs)) {
						child.player = pl;
						logger.trace("Switching " + child.getName() + " to player: " + pl.toString());
					}

					if (child.ext.isVideo()) {
						vf = getTranscodeFolder(true);

						if (vf != null) {
        						VirtualFolder fileFolder = new FileTranscodeVirtualFolder(child.getName(), null);
        
        						DLNAResource newChild = (DLNAResource) child.clone();
        						newChild.player = pl;
        						newChild.media = child.media;
        						//newChild.original = child;
        						fileFolder.addChildInternal(newChild);
        						logger.trace("Duplicate " + child.getName() + " with player: " + pl.toString());
        
        						vf.addChild(fileFolder);
						}
					}

					for (ExternalListener listener : ExternalFactory.getExternalListeners()) {
						if (listener instanceof AdditionalResourceFolderListener) {
							((AdditionalResourceFolderListener) listener).addAdditionalFolder(this, child);
						}
					}
				} else if (!child.ext.ps3compatible() && !child.isFolder()) {
					children.remove(child);
				}
			}

			if (child.ext != null && child.ext.getSecondaryFormat() != null && child.media != null && defaultRenderer != null && defaultRenderer.supportsFormat(child.ext.getSecondaryFormat())) {
				DLNAResource newChild = (DLNAResource) child.clone();
				newChild.ext = newChild.ext.getSecondaryFormat();
				newChild.first = child;
				child.second = newChild;
				if (!newChild.ext.ps3compatible() && newChild.ext.getProfiles().size() > 0) {
					newChild.player = PMS.get().getPlayer(newChild.ext.getProfiles().get(0), newChild.ext);
				}
				if (child.media != null && child.media.secondaryFormatValid) {
					addChild(newChild);
				}
			}
		}
	}

	/**
	 * Return the transcode virtual folder if it's supported and allowed. If create set to true, it tries to create if not yet created.
	 * @param create
	 * @return
	 */
        TranscodeVirtualFolder getTranscodeFolder(boolean create) {
            if (!isTranscodeFolderAvailable()) {
                return null;
            }
            if (PMS.getConfiguration().getHideTranscodeEnabled()) {
                return null;
            }
            //search for transcode folder
            for (DLNAResource r : getChildren()) {
                if (r instanceof TranscodeVirtualFolder) {
                    return (TranscodeVirtualFolder) r;
                }
            }
            if (create) {
                TranscodeVirtualFolder vf = new TranscodeVirtualFolder(null);
                addChildInternal(vf);
                return vf;
            }
            return null;
        }
	
	/**
	 * Add to the internal list of child nodes, and sets the parent to the
	 * current node.
	 * 
	 * @param res
	 */
	protected synchronized void addChildInternal(DLNAResource res) {
		this.children.add(res);
		res.parent = this;
		if (res.getInternalId() == null) {
			res.setIndexId(lastChildrenId++);
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
	public synchronized List<DLNAResource> getDLNAResources(String objectId, boolean children, int start, int count, RendererConfiguration renderer) throws IOException {
		logger.trace("Searching for objectId: " + objectId + " with children option: " + children);
		ArrayList<DLNAResource> resources = new ArrayList<DLNAResource>();
		DLNAResource resource = search(objectId, count, renderer);
		logger.trace("looking up "+objectId+" found:"+resource);

		if (resource != null) {
			resource.defaultRenderer = renderer;

			if (!children) {
				resources.add(resource);
				resource.refreshChildrenIfNeeded();
			} else {
			        resource.discoverWithRenderer(renderer, count);

				if (count == 0) {
					count = resource.children.size();
				}

				if (count > 0) {
					ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(count);

					int parallel_thread_number = 3;
					if (resource instanceof DVDISOFile) {
						parallel_thread_number = 1; // my dvd drive is dying wih 3 parallel threads 
					}
					ThreadPoolExecutor tpe = new ThreadPoolExecutor(Math.min(count, parallel_thread_number), count, 20, TimeUnit.SECONDS, queue);

					for (int i = start; i < start + count; i++) {
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
					} catch (InterruptedException e) {
					}
					logger.trace("End of analysis");
				}
			}
		}
		return resources;
	}

	protected void refreshChildrenIfNeeded() {
		logger.trace("refreshChildrenIfNeeded() : discovered= " + discovered + ", name:" + getName() + ", id :" + getId());
		if (discovered) {
			if (isRefreshNeeded()) {
				refreshChildren();
				updateId++;
				systemUpdateId++;
				refreshHappened();
			}
		}
	}

        /**
         * update the last refresh time.
         */
        protected void refreshHappened() {
        	lastRefreshTime = System.currentTimeMillis();
        }
        
	protected void discoverWithRenderer(RendererConfiguration renderer, int count) {
		// Discovering if not already done.
		if (!discovered) {
			discoverChildren();
			boolean ready = true;
			if (renderer.isMediaParserV2() && renderer.isDLNATreeHack()) {
				ready = analyzeChildren(count);
			} else {
				ready = analyzeChildren(-1);
			}
			if (!renderer.isMediaParserV2() || ready) {
				discovered = true;
			}
			refreshHappened();
		} else {
			if (isRefreshNeeded()) {
				refreshChildren();
				refreshHappened();
			}
		}
	}

	@Override
	public void run() {
		if (first == null) {
			resolve();
			if (second != null) {
				second.resolve();
			}
		}
	}

	/**Recursive function that searches for a given ID.
	 * @param searchId ID to search for.
	 * @param renderer 
	 * @param count 
	 * @return Item found, or null otherwise. 
	 * @see #id
	 * 
	 */
	public DLNAResource search(String searchId, int count, RendererConfiguration renderer) {
		if (id != null && searchId != null) {
			String[] indexPath = searchId.split("\\$", 2);
			if (id.equals(indexPath[0])) {
				if (indexPath.length == 1 || indexPath[1].length() == 0) {
					return this;
				} else {
					discoverWithRenderer(renderer, count);
					for (DLNAResource file : children) {
						DLNAResource found = file.search(indexPath[1], count, renderer);
						if (found != null) {
							return found;
						}
					}
				}
			} else {
				return null;
			}
		}
		return null;
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
	 * Reload the list of children
	 * @return
	 */
	public void refreshChildren() {
	}
	

	/**
	 * 
	 * @return true, if the container is changed, so refresh is needed
	 */
	public boolean isRefreshNeeded() {
		return false;
	}

	protected void checktype() {
		if (ext == null) {
			ext = PMS.get().getAssociatedExtension(getSystemName());
		}
		if (ext != null) {
			if (ext.isUnknown()) {
				ext.setType(specificType);
			}
		}
	}

	/**
	 * TODO: (botijo) What is the intention of this function? Looks like a prototype to be overloaded.
	 * 
	 */
	public void resolve() {
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
			if (PMS.getConfiguration().isHideExtensions() && !isFolder()) {
				name = FileUtil.getFileNameWithoutExtension(name);
			}
		}
		if (player != null) {
			if (noName) {
				name = "[" + player.name() + "]";
			} else {
				// Ditlew - WDTV Live don't show durations otherwise, and this is useful for finding the main title
				if (mediaRenderer != null && mediaRenderer.isShowDVDTitleDuration() && media.dvdtrack > 0) {
					name += " - " + media.getDurationString();
				}

				if (!PMS.getConfiguration().isHideEngineNames()) {
					name += " [" + player.name() + "]";
				}
			}
		} else {
			if (noName) {
				name = "[No encoding]";
			} else if (nametruncate > 0) {
				name = name.substring(0, nametruncate).trim();
			}
		}

		if (srtFile && (media_audio == null && media_subtitle == null)) {
			if (player == null || player.isExternalSubtitlesSupported()) {
				name += " {External Subtitles}";
			}
		}

		if (media_audio != null) {
			name = (player != null ? ("[" + player.name() + "]") : "") + " {Audio: " + media_audio.getAudioCodec() + "/" + media_audio.getLang() + ((media_audio.flavor != null && mediaRenderer != null && mediaRenderer.isShowAudioMetadata()) ? (" (" + media_audio.flavor + ")") : "") + "}";
		}

		if (media_subtitle != null && media_subtitle.id != -1) {
			name += " {Sub: " + media_subtitle.getSubType() + "/" + media_subtitle.getLang() + ((media_subtitle.flavor != null && mediaRenderer != null && mediaRenderer.isShowSubMetadata()) ? (" (" + media_subtitle.flavor + ")") : "") + "}";
		}

		if (avisynth) {
			name = (player != null ? ("[" + player.name()) : "") + " + AviSynth]";
		}

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
		StringBuilder sb = new StringBuilder();
		sb.append(PMS.get().getServer().getURL());
		sb.append("/images/");
		String id = null;
		if (media_audio != null) {
			id = media_audio.lang;
		}
		if (media_subtitle != null && media_subtitle.id != -1) {
			id = media_subtitle.lang;
		}
		if ((media_subtitle != null || media_audio != null) && StringUtils.isBlank(id)) {
			id = DLNAMediaLang.UND;
		}
		if (id != null) {
			String code = Iso639.getISO639_2Code(id.toLowerCase());
			sb.append("codes/").append(code).append(".png");
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
		StringBuilder sb = new StringBuilder();
		sb.append(PMS.get().getServer().getURL());
		sb.append("/get/");
		sb.append(getId()); //id
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

	/**
	 * @return Number of children objects. This might be used in the DLDI response, as some renderers might
	 * not have enough memory to hold the list for every children.
	 */
	public int childrenNumber() {
		if (children == null) {
			return 0;
		}
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
			logger.error(null, e);
		}
		return o;
	}

	/**Does basic transformations between characters and their HTML representation with ampersands.
	 * @param s String to be encoded
	 * @return Encoded String
	 */
	private static String encodeXML(String s) {

		s = s.replace("&", "&amp;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		s = s.replace("\"", "&quot;");
		s = s.replace("'", "&apos;");
		s = s.replace("&", "&amp;");

		return s;
	}

	public String getFlags() {
		return flags;
	}

	/**Returns a representation using DIDL response lines. It gives a complete representation of the item, with as many tags as available.
	 * Recommendations as per UPNP specification are followed where possible.
	 * @param mediaRenderer Media Renderer for which to represent this information. Useful for some hacks.
	 * @return String representing the item. An example would start like this: {@code <container id="0$1" childCount=1 parentID="0" restricted="true">}
	 */
	public String toString(RendererConfiguration mediaRenderer) {
		StringBuilder sb = new StringBuilder();
		if (isFolder()) {
			openTag(sb, "container");
		} else {
			openTag(sb, "item");
		}

		addAttribute(sb, "id", getId());
		
		if (isFolder()) {
			if (!discovered && childrenNumber() == 0) {
				//  When a folder has not been scanned for resources, it will automatically have zero children.
				//  Some renderers like XBMC will assume a folder is empty when encountering childCount="0" and
				//  will not display the folder. By returning childCount="1" these renderers will still display
				//  the folder. When it is opened, its children will be discovered and childrenNumber() will be
				//  set to the right value.
				addAttribute(sb, "childCount", 1);
			} else {
				addAttribute(sb, "childCount", childrenNumber());
			}
		}
		addAttribute(sb, "parentID", fakeParentId != null ? fakeParentId : (parent == null ? -1 : parent.getId()));
		addAttribute(sb, "restricted", "true");
		endTag(sb);

		if (media != null && media.getFirstAudioTrack() != null && StringUtils.isNotBlank(media.getFirstAudioTrack().songname)) {
			addXMLTagAndAttribute(sb, "dc:title", encodeXML(media.getFirstAudioTrack().songname + (player != null && !PMS.getConfiguration().isHideEngineNames() ? (" [" + player.name() + "]") : "")));
		} else // Ditlew - org
		//addXMLTagAndAttribute(sb, "dc:title", encodeXML((isFolder()||player==null)?getDisplayName():mediaRenderer.getUseSameExtension(getDisplayName())));
		// Ditlew
		{
			addXMLTagAndAttribute(sb, "dc:title", encodeXML((isFolder() || player == null) ? getDisplayName() : mediaRenderer.getUseSameExtension(getDisplayName(mediaRenderer))));
		}

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
			if (mediaRenderer.isDLNALocalizationRequired()) {
				indexCount = getDLNALocalesCount();
			}
			for (int c = 0; c < indexCount; c++) {
				openTag(sb, "res");
				// DLNA.ORG_OP : 1er 10 = exemple: TimeSeekRange.dlna.org :npt=187.000-
				//                   01 = Range par octets
				//                   00 = pas de range, meme pas de pause possible
				flags = "DLNA.ORG_OP=01";
				if (player != null) {
					if (player.isTimeSeekable() && mediaRenderer.isSeekByTime()) {
						if (mediaRenderer.isPS3()) // ps3 doesn't like OP=11
						{
							flags = "DLNA.ORG_OP=10";
						} else {
							flags = "DLNA.ORG_OP=11";
						}
					}
				} else {
					if (mediaRenderer.isSeekByTime() && !mediaRenderer.isPS3()) {
						flags = "DLNA.ORG_OP=11";
					}
				}
				addAttribute(sb, "xmlns:dlna", "urn:schemas-dlna-org:metadata-1-0/");

				String mime = getRendererMimeType(mimeType(), mediaRenderer);
				if (mime == null) {
					mime = "video/mpeg";
				}
				if (mediaRenderer.isPS3()) { // TO REMOVE, OR AT LEAST MAKE THIS GENERIC // whole extensions/mime-types mess to rethink anyway
					if (mime.equals("video/x-divx")) {
						dlnaspec = "DLNA.ORG_PN=AVI";
					} else if (mime.equals("video/x-ms-wmv") && media != null && media.height > 700) {
						dlnaspec = "DLNA.ORG_PN=WMVHIGH_PRO";
					}
				} else {
					if (mime.equals("video/mpeg")) {
						if (player != null) {
							// do we have some mpegts to offer ?
							boolean mpegTsMux = TSMuxerVideo.ID.equals(player.id()) || VideoLanVideoStreaming.ID.equals(player.id());
							if (!mpegTsMux) { // maybe, like the ps3, mencoder can launch tsmuxer if this a compatible H264 video
								mpegTsMux = MEncoderVideo.ID.equals(player.id()) && ((media_subtitle == null && media != null && media.dvdtrack == 0 && media.isMuxable(mediaRenderer)
									&& PMS.getConfiguration().isMencoderMuxWhenCompatible() && mediaRenderer.isMuxH264MpegTS())
									|| mediaRenderer.isTranscodeToMPEGTSAC3());
							}
							if (mpegTsMux) {
								dlnaspec = media.isH264() && !VideoLanVideoStreaming.ID.equals(player.id()) && media.isMuxable(mediaRenderer) ? "DLNA.ORG_PN=AVC_TS_HD_24_AC3_ISO" : "DLNA.ORG_PN=" + getMPEG_TS_SD_EU_ISOLocalizedValue(c);
							} else {
								dlnaspec = "DLNA.ORG_PN=" + getMPEG_PS_PALLocalizedValue(c);
							}
						} else if (media != null) {
							if (media.isMpegTS()) {
								dlnaspec = media.isH264() ? "DLNA.ORG_PN=AVC_TS_HD_50_AC3" : "DLNA.ORG_PN=" + getMPEG_TS_SD_EULocalizedValue(c);
							} else {
								dlnaspec = "DLNA.ORG_PN=" + getMPEG_PS_PALLocalizedValue(c);
							}
						} else {
							dlnaspec = "DLNA.ORG_PN=" + getMPEG_PS_PALLocalizedValue(c);
						}
					} else if (mime.equals("video/vnd.dlna.mpeg-tts")) {
						// patters - on Sony BDP m2ts clips aren't listed without this
						dlnaspec = "DLNA.ORG_PN=" + getMPEG_TS_SD_EULocalizedValue(c);
					} else if (mime.equals("image/jpeg")) {
						dlnaspec = "DLNA.ORG_PN=JPEG_LRG";
					} else if (mime.equals("audio/mpeg")) {
						dlnaspec = "DLNA.ORG_PN=MP3";
					} else if (mime.substring(0, 9).equals("audio/L16") || mime.equals("audio/wav")) {
						dlnaspec = "DLNA.ORG_PN=LPCM";
					}
				}

				if (dlnaspec != null) {
					dlnaspec = "DLNA.ORG_PN=" + mediaRenderer.getDLNAPN(dlnaspec.substring(12));
				}

				if (!mediaRenderer.isDLNAOrgPNUsed()) {
					dlnaspec = null;
				}

				addAttribute(sb, "protocolInfo", "http-get:*:" + mime + ":" + (dlnaspec != null ? (dlnaspec + ";") : "") + flags);


				if (ext != null && ext.isVideo() && media != null && media.mediaparsed) {
					if (player == null && media != null) {
						addAttribute(sb, "size", media.size);
					} else {
						long transcoded_size = mediaRenderer.getTranscodedSize();
						if (transcoded_size != 0) {
							addAttribute(sb, "size", transcoded_size);
						}
					}
					if (media.getDurationInSeconds() != null) {
						if (splitStart > 0 && splitLength > 0) {
							addAttribute(sb, "duration", DLNAMediaInfo.getDurationString(splitLength));
						} else {
							addAttribute(sb, "duration", media.getDurationString());
						}
					}
					if (media.getResolution() != null) {
						addAttribute(sb, "resolution", media.getResolution());
					}
					addAttribute(sb, "bitrate", media.getRealVideoBitrate());
					if (media.getFirstAudioTrack() != null) {
						if (media.getFirstAudioTrack().nrAudioChannels > 0) {
							addAttribute(sb, "nrAudioChannels", media.getFirstAudioTrack().nrAudioChannels);
						}
						if (media.getFirstAudioTrack().sampleFrequency != null) {
							addAttribute(sb, "sampleFrequency", media.getFirstAudioTrack().sampleFrequency);
						}
					}
				} else if (ext != null && ext.isImage()) {
					if (media != null && media.mediaparsed) {
						addAttribute(sb, "size", media.size);
						if (media.getResolution() != null) {
							addAttribute(sb, "resolution", media.getResolution());
						}
					} else {
						addAttribute(sb, "size", length());
					}
				} else if (ext != null && ext.isAudio()) {
					if (media != null && media.mediaparsed) {
						addAttribute(sb, "bitrate", media.bitrate);
						if (media.getDurationInSeconds() != null) {
							addAttribute(sb, "duration", media.getDurationString());
						}
						if (media.getFirstAudioTrack() != null && media.getFirstAudioTrack().sampleFrequency != null) {
							addAttribute(sb, "sampleFrequency", media.getFirstAudioTrack().sampleFrequency);
						}
						if (media.getFirstAudioTrack() != null) {
							addAttribute(sb, "nrAudioChannels", media.getFirstAudioTrack().nrAudioChannels);
						}

						if (player == null) {
							addAttribute(sb, "size", media.size);
						} else {
							// calcul taille wav
							if (media.getFirstAudioTrack() != null) {
								int defaultFrequency = mediaRenderer.isTranscodeAudioTo441() ? 44100 : 48000;
								if (!PMS.getConfiguration().isAudioResample()) {
									try {
										defaultFrequency = media.getFirstAudioTrack().getSampleRate();
									} catch (Exception e) {
									}
								}
								int na = media.getFirstAudioTrack().nrAudioChannels;
								if (na > 2) // no 5.1 dump in mplayer
								{
									na = 2;
								}
								int finalsize = (int) (media.getDurationInSeconds() * defaultFrequency * 2 * na);
								logger.debug("Calculated size: " + finalsize);
								addAttribute(sb, "size", finalsize);
							}
						}
					} else {
						addAttribute(sb, "size", length());
					}
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

			if (getThumbnailContentType().equals(PNG_TYPEMIME) && !mediaRenderer.isForceJPGThumbnails()) {
				addAttribute(sb, "dlna:profileID", "PNG_TN");
			} else {
				addAttribute(sb, "dlna:profileID", "JPEG_TN");
			}
			endTag(sb);
			sb.append(thumbURL);
			closeTag(sb, "upnp:albumArtURI");
		}

		if ((isFolder() || mediaRenderer.isForceJPGThumbnails()) && thumbURL != null) {
			openTag(sb, "res");

			if (getThumbnailContentType().equals(PNG_TYPEMIME) && !mediaRenderer.isForceJPGThumbnails()) {
				addAttribute(sb, "protocolInfo", "http-get:*:image/png:DLNA.ORG_PN=PNG_TN");
			} else {
				addAttribute(sb, "protocolInfo", "http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN");
			}
			endTag(sb);
			sb.append(thumbURL);
			closeTag(sb, "res");
		}

		if (getLastmodified() > 0) {
			addXMLTagAndAttribute(sb, "dc:date", PMS.sdfDate.format(new Date(getLastmodified())));
		}

		String uclass = null;
		if (first != null && media != null && !media.secondaryFormatValid) {
			uclass = "dummy";
		} else {
			if (isFolder()) {
				uclass = "object.container.storageFolder";
				boolean xbox = mediaRenderer.isXBOX();
				if (xbox && fakeParentId != null && fakeParentId.equals("7")) {
					uclass = "object.container.album.musicAlbum";
				} else if (xbox && fakeParentId != null && fakeParentId.equals("6")) {
					uclass = "object.container.person.musicArtist";
				} else if (xbox && fakeParentId != null && fakeParentId.equals("5")) {
					uclass = "object.container.genre.musicGenre";
				} else if (xbox && fakeParentId != null && fakeParentId.equals("F")) {
					uclass = "object.container.playlistContainer";
				}
			} else if (ext != null && ext.isVideo()) {
				uclass = "object.item.videoItem";
			} else if (ext != null && ext.isImage()) {
				uclass = "object.item.imageItem.photo";
			} else if (ext != null && ext.isAudio()) {
				uclass = "object.item.audioItem.musicTrack";
			} else {
				uclass = "object.item.videoItem";
			}
		}
		if (uclass != null) {
			addXMLTagAndAttribute(sb, "upnp:class", uclass);
		}

		if (isFolder()) {
			closeTag(sb, "container");
		} else {
			closeTag(sb, "item");
		}

		return sb.toString();
	}

	private String getRequestId(String rendererId) {
		return String.format("%s|%x|%s", rendererId, hashCode(), getSystemName());
	}

	/**
	 * Plugin implementation. When this item is going to play, it will notify all the StartStopListener objects available.
	 * @see StartStopListener
	 */
	public void startPlaying(final String rendererId) {
		final String requestId = getRequestId(rendererId);
		synchronized (requestIdToRefcount) {
			Integer temp = (Integer) requestIdToRefcount.get(requestId);
			if (temp == null) {
				temp = 0;
			}
			final Integer refCount = temp;
			requestIdToRefcount.put(requestId, refCount + 1);
			if (refCount == 0) {
				final DLNAResource self = this;
				Runnable r = new Runnable() {
					public void run() {
						logger.trace("StartStopListener: event:    start");
						logger.trace("StartStopListener: renderer: " + rendererId);
						logger.trace("StartStopListener: file:     " + getSystemName());

						for (final ExternalListener listener : ExternalFactory.getExternalListeners()) {
							if (listener instanceof StartStopListener) {
								// run these asynchronously for slow handlers (e.g. logging, scrobbling) 
								Runnable fireStartStopEvent = new Runnable() {
									public void run() {
										((StartStopListener) listener).nowPlaying(media, self);
									}
								};
								new Thread(fireStartStopEvent).start();
							}
						}
					}
				};

				new Thread(r).start();
			}
		}
	}

	/**
	 * Plugin implementation. When this item is going to stop playing, it will notify all the StartStopListener
	 * objects available.
	 * @see StartStopListener
	 */
	public void stopPlaying(final String rendererId) {
		final DLNAResource self = this;
		final String requestId = getRequestId(rendererId);
		Runnable defer = new Runnable() {
			public void run() {
				try {
					Thread.sleep(STOP_PLAYING_DELAY);
				} catch (InterruptedException e) {
					logger.error("stopPlaying sleep interrupted", e);
				}

				synchronized (requestIdToRefcount) {
					final Integer refCount = (Integer) requestIdToRefcount.get(requestId);
					assert refCount != null;
					assert refCount > 0;
					requestIdToRefcount.put(requestId, refCount - 1);

					Runnable r = new Runnable() {
						public void run() {
							if (refCount == 1) {
								logger.trace("StartStopListener: event:    stop");
								logger.trace("StartStopListener: renderer: " + rendererId);
								logger.trace("StartStopListener: file:     " + getSystemName());

								for (final ExternalListener listener : ExternalFactory.getExternalListeners()) {
									if (listener instanceof StartStopListener) {
										// run these asynchronously for slow handlers (e.g. logging, scrobbling) 
										Runnable fireStartStopEvent = new Runnable() {
											public void run() {
												((StartStopListener) listener).donePlaying(media, self);
											}
										};
										new Thread(fireStartStopEvent).start();
									}
								}
							}
						}
					};

					new Thread(r).start();
				}
			}
		};

		new Thread(defer).start();
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

		logger.trace("Asked stream chunk [" + low + "-" + high + "] timeseek: " + timeseek + " of " + getName() + " and player " + player);

		// shagrath: small fix, regression on chapters
		boolean timeseek_auto = false;
		// Ditlew - WDTV Live
		// Ditlew - We convert byteoffset to timeoffset here. This needs the stream to be CBR!
		int cbr_video_bitrate = mediarenderer.getCBRVideoBitrate();
		if (player != null && low > 0 && cbr_video_bitrate > 0) {
			int used_bit_rated = (int) ((cbr_video_bitrate + 256) * 1024 / 8 * 1.04); // 1.04 = container overhead
			if (low > used_bit_rated) {
				timeseek = low / (used_bit_rated);
				low = 0;

				// WDTV Live - if set to TS it asks multiple times and ends by
				// asking for an invalid offset which kills mencoder
				if (timeseek > media.getDurationInSeconds()) {
					return null;
				}

				// Should we rewind a little (in case our overhead isn't accurate enough)
				int rewind_secs = mediarenderer.getByteToTimeseekRewindSeconds();
				timeseek = (timeseek > rewind_secs) ? timeseek - rewind_secs : 0;

				//shagrath:
				timeseek_auto = true;		
			}
		}

		if (player == null) {
			if (this instanceof IPushOutput) {
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream fis = new PipedInputStream(out);
				((IPushOutput) this).push(out);
				if (low > 0 && fis != null) {
					fis.skip(low);
				}

				// http://www.ps3mediaserver.org/forum/viewtopic.php?f=11&t=12035
				if (high > low && fis != null) {
					long bytes = (high - (low < 0 ? 0 : low)) + 1;

					PMS.debug("Using size-limiting stream (" + bytes + " bytes)");
					SizeLimitInputStream slis = new SizeLimitInputStream(fis, bytes);
					return slis;
				}

				return fis;
			}

			InputStream fis = null;
			if (ext != null && ext.isImage() && media != null && media.orientation > 1 && mediarenderer.isAutoRotateBasedOnExif()) {
				// seems it's a jpeg file with an orientation setting to take care of
				fis = ImagesUtil.getAutoRotateInputStreamImage(getInputStream(), media.orientation);
				if (fis == null) // error, let's return the original one
				{
					fis = getInputStream();
				}
			} else {
				fis = getInputStream();
			}
			if (low > 0 && fis != null) {
				fis.skip(low);
			}

			// http://www.ps3mediaserver.org/forum/viewtopic.php?f=11&t=12035
			if (high > low && fis != null) {
				long bytes = (high - (low < 0 ? 0 : low)) + 1;
				
				PMS.debug("Using size-limiting stream (" + bytes + " bytes)");
				fis = new SizeLimitInputStream(fis, bytes);
			}
			
			if (timeseek != 0 && this instanceof RealFile) {
				fis.skip(MpegUtil.getPossitionForTimeInMpeg(((RealFile) this).getFile(), (int) timeseek));
			}
			return fis;
		} else {
			OutputParams params = new OutputParams(PMS.getConfiguration());
			params.aid = media_audio;
			params.sid = media_subtitle;
			params.mediaRenderer = mediarenderer;
			params.timeseek = timeseek_auto ? timeseek : splitStart;
			params.timeend = splitLength;
			params.shift_scr = timeseek_auto;

			if (this instanceof IPushOutput) {
				params.stdin = (IPushOutput) this;
			}

			if (externalProcess == null || externalProcess.isDestroyed()) {
				logger.info("Starting transcode/remux of " + getName());
				externalProcess = player.launchTranscode(
					getSystemName(),
					this,
					media,
					params);
				if (params.waitbeforestart > 0) {
					logger.trace("Sleeping for " + params.waitbeforestart + " milliseconds");
					try {
						Thread.sleep(params.waitbeforestart);
					} catch (InterruptedException e) {
						logger.error(null, e);
					}
					logger.trace("Finished sleeping for " + params.waitbeforestart + " milliseconds");
				}
			} else if (timeseek > 0 && media != null && media.mediaparsed) {
				if (media.getDurationInSeconds() > 0) {
					logger.debug("Requesting time seek: " + timeseek + " seconds");
					params.timeseek = timeseek;
					params.minBufferSize = 1;
					Runnable r = new Runnable() {

						public void run() {
							externalProcess.stopProcess();
						}
					};
					new Thread(r).start();
					ProcessWrapper newExternalProcess = player.launchTranscode(
						getSystemName(),
						this,
						media,
						params);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.error(null, e);
					}
					if (newExternalProcess == null) {
						logger.trace("External process instance is null... sounds not good");
					}
					externalProcess = newExternalProcess;
				}
			}
			if (externalProcess == null) {
				return null;
			}
			InputStream is = null;
			int timer = 0;
			while (is == null && timer < 10) {
				is = externalProcess.getInputStream(low);
				timer++;
				if (is == null) {
					logger.trace("External input stream instance is null... sounds not good, waiting 500ms");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			}

			// fail fast: don't leave a process running indefinitely if it's
			// not producing output after params.waitbeforestart milliseconds + 5 seconds
			// this cleans up lingering MEncoder web video transcode processes that hang
			// instead of exiting
			if (is == null && externalProcess != null && !externalProcess.isDestroyed()) {
				Runnable r = new Runnable() {

					public void run() {
						logger.trace("External input stream instance is null... stopping process");
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
		if (player != null) {
			return player.mimeType();
		} else if (media != null && media.mediaparsed) {
			return media.mimeType;
		} else if (ext != null) {
			return ext.mimeType();
		} else {
			return getDefaultMimeType(specificType);
		}
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
		if (ext != null) {
			return ext.getType();
		} else {
			return Format.UNKNOWN;
		}
	}

	/**Prototype function.
	 * @return true if child can be added to other folder.
	 * @see #addChild(DLNAResource)
	 */
	public abstract boolean isValid();

	public boolean allowScan() {
		return false;
	}
	
	long getLastRefreshTime() {
		return lastRefreshTime;
        }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName()+" [id=" + id + ", name="+getName() + ", full path="+getId()+ ", ext=" + ext + ", discovered=" + discovered + "]";
    }

}
