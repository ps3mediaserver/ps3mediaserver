package net.pms.util;

import java.io.IOException;
import java.lang.reflect.Field;

import com.sun.jna.Platform;

import net.pms.PMS;
import net.pms.io.Gob;

public class ProcessUtil {
	// work around a Java bug
	// see: http://kylecartmell.com/?p=9
	public static int waitFor(Process p) {
		int exit = -1;

		try {
			exit = p.waitFor();
		} catch (InterruptedException e) {
			Thread.interrupted();
		}

		return exit;
	}

	// get the process ID on Unix (returns null otherwise)
	public static Integer getProcessID(Process p) {
		Integer pid = null;

		if (p != null && p.getClass().getName().equals("java.lang.UNIXProcess")) {
			try {
				Field f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getInt(p);
			} catch (Throwable e) {
				PMS.info("Can't determine the Unix process ID: " + e.getMessage());
			}
		}

		return pid;
	}

	// kill -9 a Unix process
	public static void kill(Integer pid) {
		kill(pid, 9);
	}

	/*
	 * FIXME: this is a hack - destroy *should* work
	 *
	 * my best guess is that the process's stdout/stderr streams
	 * aren't being/haven't been fully/promptly consumed. From the
	 * abovelinked article:
	 *
	 *     The Java 6 API clearly states that failure to promptly
	 *     “read the output stream of the subprocess may cause the subprocess
	 *     to block, and even deadlock.”
	 */

	// send a Unix process the specified signal
	public static boolean kill(Integer pid, int signal) {
		boolean killed = false;
		// FIXME: this should really be a warning
		PMS.info("Sending kill -" + signal + " to the Unix process: " + pid);
		try {
			Process process = Runtime.getRuntime().exec("kill -" + signal + " " + pid);
			new Gob(process.getErrorStream()).start();
			new Gob(process.getInputStream()).start();
			int exit = waitFor(process);
			if (exit == 0) {
				killed = true;
				PMS.info("Successfully sent kill -" + signal + " to the Unix process: " + pid);
			}
		} catch (IOException ioe) {
			PMS.error("Error calling: kill -9 " + pid, ioe);
		}

		return killed;
	}

	// destroy a process safely (kill -TERM on Unix)
	public static void destroy(final Process p) {
		if (p != null) {
			final Integer pid = getProcessID(p);

			if (pid != null) { // Unix only
				PMS.debug("Killing the Unix process: " + pid);
				Runnable r = new Runnable() {
					public void run() {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {}

						try {
							p.exitValue();
						} catch (IllegalThreadStateException e) { // still running: nuke it
							// kill -14 (ALRM) works (for MEncoder) and is less dangerous than kill -9
							// so try that first 
							if (!kill(pid, 14))
								kill(pid, 9);
						}
					}
				};

				Thread failsafe = new Thread(r);
				failsafe.start();
			}

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
