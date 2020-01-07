/*
 * Copyright 2018 Murat Artim (muratartim@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package equinox.analysisServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.remote.data.AnalysisServerStatistic;
import equinox.analysisServer.server.AnalysisServer;
import equinox.analysisServer.utility.AdminAnswer;
import equinox.analysisServer.utility.ServerCommand;

/**
 * Class for the entry point of the analysis server.
 *
 * @author Murat Artim
 * @date 28 Mar 2017
 * @time 14:46:52
 */
public class EntryPoint {

	/**
	 * Entry point to server application.
	 *
	 * @param args
	 *            Arguments, not used.
	 * @throws Exception
	 *             If exception occurs during starting server.
	 */
	public static void main(String[] args) throws Exception {

		// set default locale
		Locale.setDefault(Locale.US);

		// create and start server
		AnalysisServer server = new AnalysisServer();

		// add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {

			// stop server
			System.out.println("Shutting down server...");
			server.stopServer(false, false);

			// wait a while
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				// ignore
			}

			// shut down
			System.out.println("Server shut down.");
		}));

		// start server
		server.start();

		// create output stream writer
		try (BufferedWriter stdOut = new BufferedWriter(new OutputStreamWriter(System.out))) {

			// create input stream reader
			try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

				// loop as long as stop command entered
				while (true) {

					// wait for user input
					String line = stdIn.readLine();

					// stop
					if (ServerCommand.STOP.matches(line)) {
						if (stop(stdOut, stdIn)) {
							break;
						}
					}

					// list connected clients
					else if (ServerCommand.CLIENTS.matches(line)) {
						clients(stdOut, server);
					}

					// list server properties
					else if (ServerCommand.PROPERTIES.matches(line)) {
						properties(stdOut, server);
					}

					// list all commands
					else if (ServerCommand.HELP.matches(line)) {
						help(stdOut);
					}

					// temporary file deletion
					else if (ServerCommand.TEMPORARY_FILES.matches(line)) {
						temporaryFiles(stdOut, stdIn, server);
					}

					// print statistics
					else if (ServerCommand.STATISTICS.matches(line)) {
						statistics(stdOut, server);
					}
				}
			}
		}

		// stop server
		server.stopServer(false, true);
	}

	/**
	 * Asks confirmation for stopping the server.
	 *
	 * @param stdOut
	 *            Output stream.
	 * @param stdIn
	 *            Input stream.
	 * @return True if the server should be stopped.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private static boolean stop(BufferedWriter stdOut, BufferedReader stdIn) throws Exception {

		// ask for message body
		stdOut.write("Are you sure you want to stop the analysis server? (yes/no/cancel) {");
		stdOut.newLine();
		stdOut.write("\t");
		stdOut.flush();

		// wait for user inputs
		while (true) {

			// wait for user input
			String line = stdIn.readLine();

			// cancel
			if (AdminAnswer.CANCEL.matches(line) || AdminAnswer.NO.matches(line)) {
				stdOut.write("\tStop command canceled.");
				stdOut.newLine();
				stdOut.write("}");
				stdOut.newLine();
				stdOut.flush();
				return false;
			}

			// yes
			else if (AdminAnswer.YES.matches(line)) {
				stdOut.write("\tAs you wish... Stopping analysis server.");
				stdOut.newLine();
				stdOut.write("}");
				stdOut.newLine();
				stdOut.flush();
				return true;
			}

			// unknown answer
			else {
				stdOut.write("\tPlease answer with yes/no/cancel to proceed.");
				stdOut.newLine();
				stdOut.write("\t");
				stdOut.flush();
				continue;
			}
		}
	}

	/**
	 * Enables or disables temporary file deletion after server task completion.
	 *
	 * @param stdOut
	 *            Output stream.
	 * @param stdIn
	 *            Input stream.
	 * @param server
	 *            Exchange server.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private static void temporaryFiles(BufferedWriter stdOut, BufferedReader stdIn, AnalysisServer server) throws Exception {

		// ask for message body
		stdOut.write("Enable temporary file deletion? (yes/no/cancel) {");
		stdOut.newLine();
		stdOut.write("\t");
		stdOut.flush();

		// wait for user inputs
		while (true) {

			// wait for user input
			String line = stdIn.readLine();

			// cancel
			if (AdminAnswer.CANCEL.matches(line)) {
				stdOut.write("\tCommand canceled.");
				stdOut.newLine();
				stdOut.write("}");
				stdOut.newLine();
				stdOut.flush();
				return;
			}

			// yes
			else if (AdminAnswer.YES.matches(line)) {
				server.getProperties().setProperty("temp.delete", "yes");
				stdOut.write("\tServer property 'Delete temporary files' is set to 'yes'.");
				stdOut.newLine();
				stdOut.write("}");
				stdOut.newLine();
				stdOut.flush();
				break;
			}

			// no
			else if (AdminAnswer.NO.matches(line)) {
				server.getProperties().setProperty("temp.delete", "no");
				stdOut.write("\tServer property 'Delete temporary files' is set to 'no'.");
				stdOut.newLine();
				stdOut.write("}");
				stdOut.newLine();
				stdOut.flush();
				break;
			}

			// unknown answer
			else {
				stdOut.write("\tPlease answer with yes/no/cancel to proceed.");
				stdOut.newLine();
				stdOut.write("\t");
				stdOut.flush();
				continue;
			}
		}
	}

	/**
	 * Lists all exchange server commands.
	 *
	 * @param stdOut
	 *            Output stream.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private static void help(BufferedWriter stdOut) throws Exception {

		// write header
		stdOut.write("Analysis server commands {");
		stdOut.newLine();

		// write server commands and descriptions
		for (ServerCommand sc : ServerCommand.values()) {
			stdOut.write("\t" + sc.getDescription());
			stdOut.newLine();
		}

		// flush
		stdOut.write("}");
		stdOut.newLine();
		stdOut.flush();
	}

	/**
	 * Prints all current server statistics.
	 *
	 * @param stdOut
	 *            Output stream.
	 * @param server
	 *            Server instance.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private static void statistics(BufferedWriter stdOut, AnalysisServer server) throws Exception {

		// write header
		stdOut.write("Server statistics {");
		stdOut.newLine();

		// write server statistics
		for (AnalysisServerStatistic stat : server.getStatistics()) {
			stdOut.write("\t" + stat.toString());
			stdOut.newLine();
		}

		// flush
		stdOut.write("}");
		stdOut.newLine();
		stdOut.flush();
	}

	/**
	 * Lists all current server properties.
	 *
	 * @param stdOut
	 *            Output stream.
	 * @param server
	 *            Server instance.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private static void properties(BufferedWriter stdOut, AnalysisServer server) throws Exception {

		// write header
		stdOut.write("Server properties {");
		stdOut.newLine();

		// write server commands and descriptions
		Enumeration<?> properties = server.getProperties().propertyNames();
		while (properties.hasMoreElements()) {
			String key = (String) properties.nextElement();
			if (key.contains("password")) {
				continue;
			}
			String value = server.getProperties().getProperty(key);
			stdOut.write("\t<" + key + ">: " + value);
			stdOut.newLine();
		}

		// flush
		stdOut.write("}");
		stdOut.newLine();
		stdOut.flush();
	}

	/**
	 * Lists currently connected clients.
	 *
	 * @param stdOut
	 *            Output stream.
	 * @param server
	 *            Exchange server.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private static void clients(BufferedWriter stdOut, AnalysisServer server) throws Exception {

		// write header
		stdOut.write("Currently connected clients {");
		stdOut.newLine();

		// get clients
		List<AnalysisClient> clients = server.getClients();

		// sync over clients
		synchronized (clients) {

			// no client
			if (clients.isEmpty()) {
				stdOut.write("\tNo client connected.");
				stdOut.newLine();
			}

			// write client aliases
			else {
				Iterator<AnalysisClient> i = clients.iterator();
				while (i.hasNext()) {
					AnalysisClient c = i.next();
					stdOut.write("\t" + c.getAlias());
					stdOut.newLine();
				}
			}
		}

		// flush
		stdOut.write("}");
		stdOut.newLine();
		stdOut.flush();
	}
}