package net.pms.plugin.webservice.medialibraryws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.exceptions.InitialisationException;
import net.pms.medialibrary.commons.interfaces.ILibraryManager;
import net.pms.medialibrary.storage.MediaLibraryStorage;
import net.pms.plugin.webservice.ServiceBase;

@WebService(serviceName = "Configure", targetNamespace = "http://ps3mediaserver.org/library")
public class LibraryWebService extends ServiceBase implements Library {
	private static final Logger log = LoggerFactory.getLogger(LibraryWebService.class);

	@Override
	@WebMethod(operationName = "scanFolder")
	public void scanFolder(@WebParam(name = "folderPath") String folderPath,
			@WebParam(name = "scanSubFolders") boolean scanSubFolders,
			@WebParam(name = "scanVideo") boolean scanVideo,
			@WebParam(name = "tmdbEnabled") boolean tmdbEnabled,
			@WebParam(name = "scanAudio") boolean scanAudio,
			@WebParam(name = "scanPictures") boolean scanPictures,
			@WebParam(name = "useFileImportTemplate") boolean useFileImportTemplate,
			@WebParam(name = "fileImportTemplateId") int fileImportTemplateId)
			throws InitialisationException {
		if (!isInitialized) {
			log.warn("Trying to access scanFolder when it's not initialized. Abort");
			return;
		}

		ILibraryManager lib;
		if ((lib = getLibraryManager()) != null) {
			DOFileImportTemplate fileImportTemplate = null;
			if(useFileImportTemplate) {
				fileImportTemplate = MediaLibraryStorage.getInstance().getFileImportTemplate(fileImportTemplateId);
			}
			lib.scanFolder(new DOManagedFile(false, folderPath, scanVideo, scanAudio, scanPictures, scanSubFolders, useFileImportTemplate, fileImportTemplate));
		}
	}

	@Override
	@WebMethod(operationName = "resetLibrary")
	public void resetLibrary() {
		ILibraryManager lib;
		if ((lib = getLibraryManager()) != null) {
			lib.resetStorage();
		}
	}

	@Override
	@WebMethod(operationName = "cleanLibrary")
	public void cleanLibrary() {
		ILibraryManager lib;
		if ((lib = getLibraryManager()) != null) {
			lib.cleanStorage();
		}
	}

	private ILibraryManager getLibraryManager() {
		ILibraryManager res = null;
		try {
			res = net.pms.medialibrary.library.LibraryManager.getInstance();
		} catch (InitialisationException e) {
			log.error("Failed to get LibraryManager", e);
		}
		return res;
	}

	// @Override
	// @WebMethod(operationName = "getFolder")
	// public DOMediaLibraryFolder getFolder(@WebParam(name = "id") int id) {
	// if (!isInitialized) {
	// log.warn("Trying to access scanFolder when it's not initialized. Abort");
	// return null;
	// }
	//
	// return MediaLibraryStorage.getInstance().getMediaLibraryFolder(id,
	// MediaLibraryStorage.ALL_CHILDREN);
	// }

}
