/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
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
