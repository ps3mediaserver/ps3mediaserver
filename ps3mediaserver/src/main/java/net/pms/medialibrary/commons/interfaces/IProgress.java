package net.pms.medialibrary.commons.interfaces;

public interface IProgress {
	void reportProgress(int percentComplete);
	void workComplete();
}
