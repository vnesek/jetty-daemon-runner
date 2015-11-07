/*
 * Copyright (c) Nmote Ltd. 2003-2014. All rights reserved. 
 * See LICENSE doc in a root of project folder for additional information.
 */

package com.nmote.jetty.daemon;

import static com.sun.akuma.CLibrary.LIBC;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jetty.runner.Runner;

import com.sun.akuma.Daemon;
import com.sun.akuma.JavaVMArguments;

public class JettyRunner {

	private static class RunnerInternal extends Runner {

		@Override
		public void usage(String error) {
			System.err.println("Addition daemon server opts:");
			System.err.println(" --start                             - detach from a terminal and run in background");
			System.err.println(" --stop                              - stop running server by pid");
			System.err.println(" --restart                           - restarts server by pid");
			System.err.println(" --pid file                          - PID file");
			System.err.println(" --chdir dir                         - change running directory");
			System.err.println();
			super.usage(error);
		}

		@Override
		public void version() {
			System.err.println("com.nmote.jetty.daemon.JettyRunner: 0.5.0");
			super.version();
		}

		void serverJoin() throws InterruptedException {
			_server.join();
		}

		void serverStart() throws Exception {
			_server.start();
		}
	}

	public static void main(String[] javaArgs) throws Exception {
		// All command line arguments, including VM ones
		JavaVMArguments args = JavaVMArguments.current();

		// Check if args are correctly set
		getArg("--chdir", args);

		String pidFile = getArg("--pid", args);
		Path pidPath = pidFile != null ? Paths.get(pidFile) : null;

		// What should we do
		boolean doStop = hasArg("--stop", args);
		boolean doStart = hasArg("--start", args);
		if (hasArg("--restart", args)) {
			doStart = true;
			doStop = true;
		}

		// Read PID from file
		int pid = readPid(pidPath);

		// Stop running server
		if (doStop && pid != -1) {
			stopServer(pidPath, pid);
			if (!doStart) {
				System.exit(0);
			}
			pid = -1;
		}

		if (doStart && pid != -1) {
			System.exit(3);
		}

		// Daemonize
		Daemon d = new Daemon();
		if (!d.isDaemonized() && doStart) {
			d.daemonize(args);
			System.exit(0);
		}

		if (d.isDaemonized()) {
			// Customized Daemon::init procedure
			LIBC.setsid();

			// Save PID to file
			writePid(pidPath, LIBC.getpid());

			try {
				// Change running directory
				String chDir = getArg("--chdir", args);
				if (chDir != null) {
					LIBC.chdir(chDir);
				}

				// Save out,in,err. Jetty could redirect'em
				Closeable[] streams = { System.out, System.err, System.in };

				RunnerInternal runner = new RunnerInternal();
				runner.configure(jettyCleanedArgs(javaArgs));
				runner.serverStart();

				// Close out,in,err
				for (Closeable c : streams) {
					c.close();
				}

				// Wait for server to shutdown
				runner.serverJoin();
			} finally {
				// Delete PID file
				if (pidFile != null) {
					Files.delete(Paths.get(pidFile));
				}
			}
		} else {
			// Run in foreground
			RunnerInternal runner = new RunnerInternal();
			if (hasArg("--help", args)) {
				runner.usage(null);
			} else if (hasArg("--version", args)) {
				runner.version();
			} else {
				runner.configure(jettyCleanedArgs(javaArgs));
				runner.run();
			}
		}
	}

	protected static String getArg(String arg, Iterable<String> args) {
		Iterator<String> i = args.iterator();
		while (i.hasNext()) {
			String a = i.next();
			if (arg.equals(a)) {
				try {
					String value = i.next();
					if (value.startsWith("--")) {
						throw new NoSuchElementException();
					}
					return value;
				} catch (NoSuchElementException e) {
					System.err.println("ERROR: Missing argument for " + arg);
					System.exit(1);
				}
			}
		}
		return null;
	}

	protected static boolean hasArg(String arg, Collection<String> args) {
		return args.contains(arg);
	}

	protected static String[] jettyCleanedArgs(String[] javaArgs) {
		ArrayList<String> jettyArgs = new ArrayList<String>(Arrays.asList(javaArgs));
		removeArg("--pid", jettyArgs, true);
		removeArg("--chdir", jettyArgs, true);
		removeArg("--start", jettyArgs, false);
		removeArg("--stop", jettyArgs, false);
		removeArg("--restart", jettyArgs, false);
		return jettyArgs.toArray(new String[jettyArgs.size()]);
	}

	protected static void removeArg(String arg, Iterable<String> args, boolean hasValue) {
		Iterator<String> i = args.iterator();
		while (i.hasNext()) {
			String a = i.next();
			if (arg.equals(a)) {
				i.remove();
				if (hasValue) {
					i.next();
					i.remove();
				}
			}
		}
	}

	private static int readPid(Path pidPath) throws IOException {
		if (pidPath != null) {
			if (Files.exists(pidPath)) {
				return Integer.parseInt(Files.readAllLines(pidPath).get(0));
			}
		}
		return -1;
	}

	private static void stopServer(Path pidPath, int pid) throws IOException, InterruptedException {
		// Wait up-to 10 seconds for exit (deletion of pid file)
		System.err.print("INFO: Stopping " + pid);
		if (LIBC.kill(pid, 1) == 0 && !waitTillDeleted(pidPath, 100)) {
			// Send SIGKILL
			System.err.println("\nERROR: Stop failed, sending SIGKILL " + pid);
			LIBC.kill(pid, 9);
			Files.delete(pidPath);
		}

		if (Files.exists(pidPath)) {
			System.err.println(String.format( //
					"\nERROR: Stop failed, check if process %d is running and delete %s", pid, pidPath));
			System.exit(2);
		}

		System.err.println("\nINFO: Stopped");
	}

	private static boolean waitTillDeleted(Path pidPath, int max) throws InterruptedException {
		for (int i = 0; i < max; ++i) {
			Thread.sleep(100);
			if (!Files.exists(pidPath)) {
				return true;
			}
			if ((i % 10) == 9) {
				System.err.print('.');
			}
		}
		return false;
	}

	private static void writePid(Path pidPath, int pid) throws IOException {
		if (pidPath != null) {
			Files.write(pidPath, Collections.singleton(Integer.toString(pid)));
		}
	}
}
