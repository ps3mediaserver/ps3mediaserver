package net.pms.util;

import java.lang.reflect.Field;

import net.pms.PMS;

public class ProcessUtil {

	public static void destroy(Process p) {
		if (p != null) {
			p.destroy();
			if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
				/* get the PID on unix/linux systems */
				try {
					Field f = p.getClass().getDeclaredField("pid");
					f.setAccessible(true);
					int pid = f.getInt(p);
					PMS.debug("Killing the Unix process: " + pid);
					Runtime.getRuntime().exec("kill -9 " + pid);
				} catch (Throwable e) {
				}
			}
		}
	}

}
