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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.pms.PMS;
import net.pms.configuration.MapFileConfiguration;
import net.pms.dlna.virtual.TranscodeVirtualFolder;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.network.HTTPResource;

public class MapFile extends DLNAResource {
    private static final Collator collator;

    static {
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
    }

    // <editor-fold desc="Private Fields">
    private List<File> discoverable;
    public File potentialCover;
    // </editor-fold>
    // <editor-fold desc="Protected Fields">
    protected MapFileConfiguration conf;
    // </editor-fold>

    // <editor-fold desc="Constructor">
    public MapFile() {
        this.conf = new MapFileConfiguration();
        lastmodified = 0;
    }

    public MapFile(MapFileConfiguration conf) {
        this.conf = conf;
        lastmodified = 0;
    }

    // </editor-fold>
    // <editor-fold desc="Private Methods">
    private boolean isFileRelevant(File f) {
        String fileName = f.getName().toLowerCase();
        return (PMS.getConfiguration().isArchiveBrowsing() && (fileName.endsWith(".zip") || fileName.endsWith(".cbz")
                || fileName.endsWith(".rar") || fileName.endsWith(".cbr")))
                || fileName.endsWith(".iso") || fileName.endsWith(".img")
                || fileName.endsWith(".m3u") || fileName.endsWith(".m3u8") || fileName.endsWith(".pls") || fileName.endsWith(".cue");
    }

    private boolean isFolderRelevant(File f) {

        boolean excludeNonRelevantFolder = true;
        if (f.isDirectory() && PMS.getConfiguration().isHideEmptyFolders()) {
            File children[] = f.listFiles();
            for (File child : children) {
                if (child.isFile()) {
                    if (PMS.get().getAssociatedExtension(child.getName()) != null || isFileRelevant(child)) {
                        excludeNonRelevantFolder = false;
                        break;
                    }
                } else {
                    if (isFolderRelevant(child)) {
                        excludeNonRelevantFolder = false;
                        break;
                    }
                }
            }
        }

        return !excludeNonRelevantFolder;
    }

    private void manageFile(File f) {
        if ((f.isFile() || f.isDirectory()) && !f.isHidden()) {
            if (PMS.getConfiguration().isArchiveBrowsing() && (f.getName().toLowerCase().endsWith(".zip") || f.getName().toLowerCase().endsWith(".cbz"))) {
                addChild(new ZippedFile(f));
            } else if (PMS.getConfiguration().isArchiveBrowsing() && (f.getName().toLowerCase().endsWith(".rar") || f.getName().toLowerCase().endsWith(".cbr"))) {
                addChild(new RarredFile(f));
            } else if ((f.getName().toLowerCase().endsWith(".iso") || f.getName().toLowerCase().endsWith(".img")) || (f.isDirectory() && f.getName().toUpperCase().equals("VIDEO_TS"))) {
                addChild(new DVDISOFile(f));
            } else if (f.getName().toLowerCase().endsWith(".m3u") || f.getName().toLowerCase().endsWith(".m3u8") || f.getName().toLowerCase().endsWith(".pls")) {
                addChild(new PlaylistFolder(f));
            } else if (f.getName().toLowerCase().endsWith(".cue")) {
                addChild(new CueFolder(f));
            } else {

                /* Optionally ignore empty directories */
                if (f.isDirectory() && PMS.getConfiguration().isHideEmptyFolders() && !isFolderRelevant(f)) {
                    PMS.info("Ignoring empty/non relevant directory: " + f.getName());
                } /* Otherwise add the file */ else {
                    RealFile file = new RealFile(f);
                    addChild(file);
                }
            }
        }
        if (f.isFile()) {
            String fileName = f.getName().toLowerCase();
            if (fileName.equalsIgnoreCase("folder.jpg") || fileName.equalsIgnoreCase("folder.png") || (fileName.contains("albumart") && fileName.endsWith(".jpg"))) {
                potentialCover = f;
            }
        }
    }

    private List<File> getFileList() {
        List<File> out = new ArrayList<File>();
        for (File file : this.conf.getFiles()) {
            if (file!=null && file.canRead())
                out.addAll(Arrays.asList(file.listFiles()));
        }
        return out;
    }
    // </editor-fold>

    // <editor-fold desc="Public Methods">
    // <editor-fold desc="DLNAResource Override Methods">
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean analyzeChildren(int count) {
        int currentChildrenCount = children.size();
        int vfolder = 0;
        while ((children.size() - currentChildrenCount) < count || count == -1) {
            if (vfolder < conf.getChildren().size()) {
                addChild(new MapFile(conf.getChildren().get(vfolder)));
                ++vfolder;
            } else {
                if (discoverable.isEmpty()) {
                    break;
                }
                manageFile(discoverable.remove(0));
            }
        }
        return discoverable.isEmpty();
    }

    @Override
    public void discoverChildren() {
        super.discoverChildren();

        if (discoverable == null) {
            discoverable = new ArrayList<File>();
        } else {
            return;
        }
        List<File> files = getFileList();
        switch (PMS.getConfiguration().getSortMethod()) {
            case 2: // Sort by modified date, oldest first
                Collections.sort( files, new Comparator<File>() {
                    public int compare(File o1, File o2) {
                        return new Long(o1.lastModified()).compareTo(new Long(o2.lastModified()));
                    }
                });
                break;
            case 1: // Sort by modified date, newest first
                Collections.sort( files, new Comparator<File>() {
                    public int compare(File o1, File o2) {
                        return new Long(o2.lastModified()).compareTo(new Long(o1.lastModified()));
                    }
                });
                break;
            default: // sort A-Z
                Collections.sort(files, new Comparator<File>() {
                    public int compare(File o1, File o2) {
                        return collator.compare(o1.getName(), o2.getName());
                    }
                });
                break;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                discoverable.add(f);//manageFile(f);
            }
        }
        for (File f : files) {
            if (f.isFile()) {
                discoverable.add(f);//manageFile(f);
            }
        }
    }

    @Override
    public boolean refreshChildren() {
        List<File> files = getFileList();
        ArrayList<File> addedFiles = new ArrayList<File>();
        ArrayList<DLNAResource> removedFiles = new ArrayList<DLNAResource>();
        int i = 0;
        for (File f : files) {
            if (!f.isHidden()) {
                boolean present = false;
                for (DLNAResource d : children) {
                    if (i == 0 && (!(d instanceof VirtualFolder) || (d instanceof DVDISOFile))) // specific for video_ts, we need to refresh it
                    {
                        if (d.getClass() != MapFile.class)
                            removedFiles.add(d);
                    }
                    boolean video_ts_hack = (d instanceof DVDISOFile) && d.getName().startsWith(DVDISOFile.PREFIX) && d.getName().substring(DVDISOFile.PREFIX.length()).equals(f.getName());
                    if ((d.getName().equals(f.getName()) || video_ts_hack)
                            && ((d instanceof RealFile && d.isFolder()) || d.lastmodified == f.lastModified())) { // && (!addcheck || (addcheck && d.lastmodified == f.lastModified()))
                        removedFiles.remove(d);
                        present = true;
                    }
                }
                if (!present && (f.isDirectory() || PMS.get().getAssociatedExtension(f.getName()) != null)) {
                    addedFiles.add(f);
                }
            }
            i++;
        }

        for (DLNAResource f : removedFiles) {
            PMS.info("File automatically removed: " + f.getName());
        }

        for (File f : addedFiles) {
            PMS.info("File automatically added: " + f.getName());
        }


        TranscodeVirtualFolder vf = null;
        if (!PMS.getConfiguration().getHideTranscodeEnabled()) {
            for (DLNAResource r : children) {
                if (r instanceof TranscodeVirtualFolder) {
                    vf = (TranscodeVirtualFolder) r;
                    break;
                }
            }
        }

        for (DLNAResource f : removedFiles) {
            children.remove(f);
            if (vf != null) {
                for (int j = vf.children.size() - 1; j >= 0; j--) {
                    if (vf.children.get(j).getName().equals(f.getName())) {
                        vf.children.remove(j);
                    }
                }
            }
        }

        for (File f : addedFiles) {
            manageFile(f);
        }

        for (MapFileConfiguration f : this.conf.getChildren()) {
            addChild(new MapFile(f));
        }

        return !removedFiles.isEmpty() || !addedFiles.isEmpty();
    }

    @Override
    public String getSystemName() {
        return getName();
    }

    @Override
    public String getThumbnailContentType() {
        String thumbnailIcon = this.conf.getThumbnailIcon();
        if (thumbnailIcon != null && thumbnailIcon.toLowerCase().endsWith(".png")) {
            return HTTPResource.PNG_TYPEMIME;
        }
        return super.getThumbnailContentType();
    }

    @Override
    public InputStream getThumbnailInputStream() throws IOException {
        return this.conf.getThumbnailIcon() != null
                ? getResourceInputStream(this.conf.getThumbnailIcon())
                : super.getThumbnailInputStream();
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public String getName() {
        return this.conf.getName();
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public boolean allowScan() {
        return isFolder();
    }
    // </editor-fold>
    // </editor-fold>
}
