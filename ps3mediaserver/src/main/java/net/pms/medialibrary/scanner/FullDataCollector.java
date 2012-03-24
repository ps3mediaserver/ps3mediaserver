package net.pms.medialibrary.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.RealFile;
import net.pms.formats.FLAC;
import net.pms.formats.GIF;
import net.pms.formats.ISO;
import net.pms.formats.JPG;
import net.pms.formats.M4A;
import net.pms.formats.MKV;
import net.pms.formats.MP3;
import net.pms.formats.MPG;
import net.pms.formats.OGG;
import net.pms.formats.PNG;
import net.pms.formats.RAW;
import net.pms.formats.TIF;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOAudioFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOImageFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.exceptions.InitialisationException;
import net.pms.medialibrary.commons.helpers.FileImportHelper;

public class FullDataCollector {	
	private static final Logger log = LoggerFactory.getLogger(FullDataCollector.class);
	private static FullDataCollector instance;

	private List<String> audioFileExtensions;
	private List<String> videoFileExtensions;
	private List<String> imageFileExtensions;
	private String videoCoverSaveFolderPath = "";
	private static FileScannerDlnaResource dummyParent = new FileScannerDlnaResource();
	
	private FullDataCollector(String videoCoverSaveFolderPath){
		if(videoCoverSaveFolderPath.endsWith(String.valueOf(File.separatorChar))){
			this.videoCoverSaveFolderPath = videoCoverSaveFolderPath;
		}else{
			this.videoCoverSaveFolderPath = videoCoverSaveFolderPath + File.separatorChar;
		}
		
		try{
			File saveFolder = new File(this.videoCoverSaveFolderPath);
			if(!saveFolder.isDirectory()){
				saveFolder.mkdirs();
			}
		}catch(Exception ex){
			log.error("Failed to create directory " + this.videoCoverSaveFolderPath 
					+ "for video cover storage", ex);
		}
		populateExtensions();
	}

	public static FullDataCollector getInstance() throws InitialisationException {
	    if(instance == null){
	    	throw new InitialisationException("The static configure() method has to be called to initialize the instance before it can be retrieved through the getInstance() method");
	    }else{
	    	return instance;
	    }
    }

	public static void configure(String videoCoverSaveFolderPath) {
	    instance = new FullDataCollector(videoCoverSaveFolderPath);
    }
	
	public DOFileInfo get(DOManagedFile mf) {
		DOFileInfo retVal = null;
		int sep = mf.getPath().lastIndexOf(java.io.File.separator) + 1;
		String folderPath = mf.getPath().substring(0, sep);
		String fileName = mf.getPath().substring(sep);
		switch(getMediaType(new File(mf.getPath()))){
			case VIDEO:
				if(mf.isVideoEnabled()){
    				DOVideoFileInfo tmpVideoFileInfo = new DOVideoFileInfo();
    				tmpVideoFileInfo.setFolderPath(folderPath);
    				tmpVideoFileInfo.setFileName(fileName);
    				tmpVideoFileInfo.setType(FileType.VIDEO);
    				//get the information from pms internal util (mediainfo or ffmpeg)
    				populateMovieInfo(tmpVideoFileInfo);
    				
    				//import the info with configured plugins
    				if(mf.isFileImportEnabled()) {
    					FileImportHelper.updateFileInfo(mf.getFileImportTemplate(), tmpVideoFileInfo);
    					//TODO: Parametrize the creation of the sort name
    					tmpVideoFileInfo.setSortName(tmpVideoFileInfo.getName());
    				}
    				
    				if(tmpVideoFileInfo.getName().equals("")) {
    					tmpVideoFileInfo.setName(tmpVideoFileInfo.getFileName(false));
    				}
    				if(tmpVideoFileInfo.getSortName().equals("")) {
    					tmpVideoFileInfo.setSortName(tmpVideoFileInfo.getName());
    				}
    				
    				retVal = tmpVideoFileInfo;
				}
				break;
			case AUDIO:
				if(mf.isAudioEnabled()){
    				DOAudioFileInfo tmpAudioFileInfo = new DOAudioFileInfo();
    				tmpAudioFileInfo.setFolderPath(folderPath);
    				tmpAudioFileInfo.setFileName(fileName);
    				tmpAudioFileInfo.setType(FileType.AUDIO);
    				//TODO: Implement
    				retVal = tmpAudioFileInfo;
				}
				break;
			case PICTURES:
				if(mf.isPicturesEnabled()){
    				DOImageFileInfo tmpImageFileInfo = new DOImageFileInfo();
    				tmpImageFileInfo.setFolderPath(folderPath);
    				tmpImageFileInfo.setFileName(fileName);
    				tmpImageFileInfo.setType(FileType.PICTURES);
    				//TODO: Implement
    				retVal = tmpImageFileInfo;
				}
				break;
			default:
				break;
		}

		File f = new File(mf.getPath());
		if (retVal != null) {
			retVal.setActive(true);
			if (f != null && f.exists()) {
				retVal.setSize(f.length());
				if (f.exists()) {
					retVal.setDateModifiedOs(new Date(f.lastModified()));
				}
			}
		}
		return retVal;
	}
	
	private FileType getMediaType(String fileName){
		FileType retVal = FileType.UNKNOWN;
		
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
		if(this.videoFileExtensions.contains(extension)){
			retVal = FileType.VIDEO;
		}
		else if(this.audioFileExtensions.contains(extension)){
			retVal = FileType.AUDIO;
		}
		else if(this.imageFileExtensions.contains(extension)){
			retVal = FileType.PICTURES;
		}

		return retVal;
	}
	
	private FileType getMediaType(File file){
		FileType retVal = FileType.UNKNOWN;
		
		if(file.isFile()){
			retVal = getMediaType(file.getName());
		}

		return retVal;
	}
	
	private void populateMovieInfo(DOVideoFileInfo fi) {
		if(fi.getType() == FileType.VIDEO){
			
			File inFile = new File(fi.getFilePath());
			if(!inFile.exists() && !inFile.canRead()){
				log.error("File " + fi.getFilePath() + " doesn't exist or couldn't be opened as a file for reading");
				return;
			}
			RealFile rf = new RealFile(inFile);
			//add the parent to avoid a null pointer exception when calling isValid
			rf.setParent(dummyParent);
			if(!rf.isValid()){
				return;
			}
			rf.resolve();
			DLNAMediaInfo mi = rf.getMedia();
			
			try{
				fi.setAspectRatio(mi.getAspect());
				fi.setBitrate(mi.getBitrate());
				fi.setBitsPerPixel(mi.getBitsPerPixel());
				if(mi.getCodecV() != null) fi.setCodecV(mi.getCodecV());
				if(mi.getContainer() != null) fi.setContainer(mi.getContainer());
				fi.setDurationSec(mi.getDurationInSeconds());
				fi.setDvdtrack(mi.getDvdtrack());
				if(mi.getFrameRate() != null) fi.setFrameRate(mi.getFrameRate());
				if(mi.getH264AnnexB() != null) fi.setH264_annexB(mi.getH264AnnexB());
				fi.setHeight(mi.getHeight());
				if(mi.getMimeType() != null) fi.setMimeType(mi.getMimeType());
				if(mi.getModel() != null) fi.setModel(mi.getModel());
				fi.setSize(mi.getSize());
				fi.setWidth(mi.getWidth());
				fi.setMuxingMode(mi.getMuxingMode());
				if(mi.getSubtitlesCodes() != null) fi.setSubtitlesCodes(mi.getSubtitlesCodes());
				if(mi.getAudioCodes() != null) fi.setAudioCodes(mi.getAudioCodes());
			}catch(Exception ex){
				log.error("Failed to parse file info", ex);
			}
		}
	}

	private void populateExtensions(){
		this.audioFileExtensions = new ArrayList<String>();
		this.audioFileExtensions.addAll(Arrays.asList(new M4A().getId()));
		this.audioFileExtensions.addAll(Arrays.asList(new MP3().getId()));
		this.audioFileExtensions.addAll(Arrays.asList(new OGG().getId()));
		this.audioFileExtensions.addAll(Arrays.asList(new FLAC().getId()));

		this.videoFileExtensions = new ArrayList<String>();
		this.videoFileExtensions.addAll(Arrays.asList(new MKV().getId()));
		this.videoFileExtensions.addAll(Arrays.asList(new ISO().getId()));
		this.videoFileExtensions.addAll(Arrays.asList(new MPG().getId()));

		this.imageFileExtensions = new ArrayList<String>();
		this.imageFileExtensions.addAll(Arrays.asList(new JPG().getId()));
		this.imageFileExtensions.addAll(Arrays.asList(new PNG().getId()));
		this.imageFileExtensions.addAll(Arrays.asList(new GIF().getId()));
		this.imageFileExtensions.addAll(Arrays.asList(new TIF().getId()));
		this.imageFileExtensions.addAll(Arrays.asList(new RAW().getId()));
	}

}
