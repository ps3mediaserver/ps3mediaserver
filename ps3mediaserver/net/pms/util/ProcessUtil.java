package net.pms.util;

import java.lang.reflect.Field;

import com.sun.jna.Platform;

import net.pms.PMS;
import net.pms.io.Gob;

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
					Process process = Runtime.getRuntime().exec("kill -9 " + pid);
					new Gob(process.getErrorStream()).start();
					new Gob(process.getInputStream()).start();
					int exit = process.waitFor();
					if (exit != 0) {
						//PMS.debug("\"kill -9 " + pid + "\" not successful... process was obviously already terminated");
					} else {
						PMS.debug("\"kill -9 " + pid + "\" successful !");
					}
				} catch (Throwable e) {
					PMS.info("\"unexpected error: " + e.getMessage());
				}
			}
		}
	}
	
	public static String getShortFileNameIfWideChars(String name) {
		if (Platform.isWindows()) {
			return PMS.get().getRegistry().getShortPathNameW(name);
		}
		return name;
	}

}
