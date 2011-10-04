package net.pms.io;

import java.io.File;

/**
 * Base implementation for the SystemUtils class for the generic cases.
 * @author zsombor
 *
 */
public class BasicSystemUtils implements SystemUtils {
    protected String vlcp;
    protected String vlcv;
    protected boolean avis;

    @Override
    public void disableGoToSleep() {

    }

    @Override
    public void reenableGoToSleep() {

    }

    @Override
    public File getAvsPluginsDir() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getShortPathNameW(String longPathName) {
        return longPathName;
    }

    @Override
    public String getWindowsDirectory() {
        return null;
    }

    @Override
    public String getDiskLabel(File f) {
        return null;
    }

    @Override
    public boolean isKerioFirewall() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.pms.io.SystemUtils#getVlcp()
     */
    @Override
    public String getVlcp() {
        return vlcp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.pms.io.SystemUtils#getVlcv()
     */
    @Override
    public String getVlcv() {
        return vlcv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.pms.io.SystemUtils#isAvis()
     */
    @Override
    public boolean isAvis() {
        return avis;
    }

}
