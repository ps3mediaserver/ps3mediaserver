package net.pms.util;

import java.lang.reflect.Field;

import com.sun.jna.Platform;

import net.pms.PMS;

public class ProcessUtil {

	public static void destroy(Process p) {
		if (p != null) {
			/* get the PID - only used for logging i.e. not directly */
			if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
				try {
					Field f = p.getClass().getDeclaredField("pid");
					f.setAccessible(true);
					int pid = f.getInt(p);
					PMS.debug("Killing the Unix process: " + pid);
				} catch (Throwable e) {
				        PMS.info("Can't determine the Unix process ID: " + e.getMessage());
				}
			}

			/*
			 * Squashed bug - send Unix processes a TERM signal rather than KILL.
			 *
			 * destroy() sends the spawned process a TERM signal.
			 * This ensures the process is given the opportunity
			 * to shutdown cleanly. *Extremely* rare cases where this doesn't
			 * happen are less of a problem than extremely common cases
			 * where kill -9 (KILL) creates orphan processes. In the former
			 * case, the stubborn processes may still be shut down when PMS
			 * exits.
			 */

			p.destroy();
		}
	}
	
	public static String getShortFileNameIfWideChars(String name) {
		if (Platform.isWindows()) {
			return PMS.get().getRegistry().getShortPathNameW(name);
		}
		return name;
	}

}
