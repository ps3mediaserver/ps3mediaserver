package net.pms.medialibrary.commons.helpers;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

public class GUIHelper {
	public static Point getCenterDialogOnParentLocation(Dimension dialogDimensions, JComponent initialComponent){
		Dimension containerDimension = initialComponent.getTopLevelAncestor().getSize();
		Point containerTopLeftCorner = initialComponent.getTopLevelAncestor().getLocationOnScreen();
		return new Point(containerTopLeftCorner.x + containerDimension.width / 2 - dialogDimensions.width / 2 , containerTopLeftCorner.y + containerDimension.height / 2 - dialogDimensions.height / 2);
    }
	
	public static<T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}
}
