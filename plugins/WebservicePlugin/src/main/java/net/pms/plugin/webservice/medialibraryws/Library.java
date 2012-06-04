package net.pms.plugin.webservice.medialibraryws;

import net.pms.medialibrary.commons.exceptions.InitialisationException;

public interface Library {
	void scanFolder(String folderPath, boolean scanSubFolders,
			boolean scanVideo, boolean tmdbEnabled, boolean scanAudio,
			boolean scanPictures, boolean useFileImportTemplate, int fileImportTemplateId) throws InitialisationException;

	void resetLibrary();

	void cleanLibrary();
}
