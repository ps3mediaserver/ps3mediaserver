package net.pms.medialibrary.commons.dataobjects;

import net.pms.medialibrary.commons.enumarations.ScanState;


public class DOScanReport {
	private ScanState scanState;
	private int nbScannedItems;
	private int nbItemsToScan;
	
	public DOScanReport(){
		this(ScanState.IDLE, 0, 0);
	}
	
	public DOScanReport(ScanState scanState, int nbScannedItems, int nbItemsToScan){
		setScanState(scanState);
		setNbScannedItems(nbScannedItems);
		setNbItemsToScan(nbItemsToScan);
	}

	public void setScanState(ScanState scanState) {
	    this.scanState = scanState;
    }

	public ScanState getScanState() {
	    return scanState;
    }

	public void setNbScannedItems(int nbScannedItems) {
	    this.nbScannedItems = nbScannedItems;
    }

	public int getNbScannedItems() {
	    return nbScannedItems;
    }

	public void setNbItemsToScan(int nbItemsToScan) {
	    this.nbItemsToScan = nbItemsToScan;
    }

	public int getNbItemsToScan() {
	    return nbItemsToScan;
    }
}
