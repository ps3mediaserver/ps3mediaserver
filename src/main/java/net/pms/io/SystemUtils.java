package net.pms.io;

import java.io.File;

public interface SystemUtils {

    public abstract void disableGoToSleep();

    public abstract void reenableGoToSleep();

    public abstract File getAvsPluginsDir();

    public abstract String getShortPathNameW(String longPathName);

    public abstract String getWindowsDirectory();

    public abstract String getDiskLabel(File f);

    public abstract boolean isKerioFirewall();

    public abstract String getVlcp();

    public abstract String getVlcv();

    public abstract boolean isAvis();

}