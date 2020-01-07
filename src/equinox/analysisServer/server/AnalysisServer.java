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
package equinox.analysisServer.server;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esotericsoftware.kryonet.Server;

import equinox.analysisServer.EntryPoint;
import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.remote.data.AnalysisServerStatistic;
import equinox.analysisServer.task.CollectServerStatistics;
import equinox.analysisServer.utility.Utility;

/**
 * Equinox analysis server application entry class.
 *
 * @author Murat Artim
 * @date Sep 16, 2014
 * @time 5:50:06 PM
 */
public class AnalysisServer extends Thread {

	/** Server properties. */
	private final Properties properties_;

	/** Server logger. */
	private final Logger logger_;

	/** Cached thread pool. */
	private final ExecutorService threadPool_;

	/** Scheduled thread pool. */
	private final ScheduledExecutorService scheduledThreadPool_;

	/** Client lobby. */
	private final Lobby lobby_;

	/** List containing the connected clients. */
	private final List<AnalysisClient> clients_;

	/** The network server. */
	private final Server networkServer_;

	/** Server health monitor parameters. */
	private final AtomicInteger analysisRequests_, failedAnalyses_;

	/** Data server statistics. */
	private final ArrayList<AnalysisServerStatistic> statistics_;

	/** True if the server is shut down. */
	private volatile boolean isShutDown_ = false;

	/**
	 * Creates equinox server.
	 *
	 * @throws Exception
	 *             If server cannot be created.
	 */
	public AnalysisServer() throws Exception {

		// create thread
		super("Equinox Analysis Server");

		// read server properties
		properties_ = Utility.loadProperties(Paths.get("resources/config.properties"));

		// setup server logger
		logger_ = Utility.setupLogger(properties_.getProperty("log.filename"), properties_.getProperty("log.level"));

		// setup network server
		networkServer_ = Utility.setupNetworkServer(this);

		// create thread pools
		threadPool_ = Executors.newCachedThreadPool();
		scheduledThreadPool_ = Executors.newSingleThreadScheduledExecutor();
		logger_.info("Thread pools created.");

		// initialize server statistic counters
		analysisRequests_ = new AtomicInteger();
		failedAnalyses_ = new AtomicInteger();

		// create client list
		clients_ = Collections.synchronizedList(new ArrayList<AnalysisClient>());

		// create client lobby
		lobby_ = new Lobby(this);

		// create server statistics
		statistics_ = new ArrayList<>();

		// log server creation info
		logger_.info("Server initialized.");
	}

	@Override
	public void run() {

		// log start message
		logger_.info("Starting server...");

		try {

			// bind server to its port
			networkServer_.bind(Integer.parseInt(properties_.getProperty("ns.port")));

			// start the network server
			networkServer_.start();

			// schedule statistics collection
			if (properties_.getProperty("stat.collect").equals("yes")) {
				long period = Long.parseLong(properties_.getProperty("stat.period"));
				scheduledThreadPool_.scheduleAtFixedRate(new CollectServerStatistics(this), 30, period, TimeUnit.SECONDS);
			}

			// schedule server stop
			if (properties_.getProperty("stop.schedule").equals("yes")) {
				long period = Long.parseLong(properties_.getProperty("stop.delay"));
				boolean restart = properties_.getProperty("stop.restart").equals("yes");
				scheduledThreadPool_.schedule(() -> stopServer(restart, true), period, TimeUnit.HOURS);
			}
		}

		// exception occurred during starting server
		catch (IOException e) {
			logger_.log(Level.SEVERE, "Exception occurred during starting server.", e);
			stopServer(false, true);
		}
	}

	/**
	 * Stops the server.
	 *
	 * @param restart
	 *            True to restart the server.
	 * @param exit
	 *            True to exit JVM.
	 */
	public void stopServer(boolean restart, boolean exit) {

		// already shut down
		if (isShutDown_)
			return;

		// set shut down
		isShutDown_ = true;

		// log stopping message
		logger_.info("Stopping server...");

		// stop lobby
		lobby_.stop();

		// shutdown thread pool
		Utility.shutdownThreadPool(threadPool_, logger_);
		Utility.shutdownThreadPool(scheduledThreadPool_, logger_);
		logger_.info("Thread pools shutdown.");

		// stop network server
		networkServer_.stop();
		logger_.info("Network server shutdown.");

		// close logger
		Arrays.stream(logger_.getHandlers()).forEach(h -> h.close());

		// restart
		if (restart) {

			// log
			logger_.info("Restarting server...");

			try {

				// get path to current java and jar file
				String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
				File currentJar = new File(EntryPoint.class.getProtectionDomain().getCodeSource().getLocation().toURI());

				// not a jar file?
				if (!currentJar.getName().endsWith(".jar")) {
					logger_.warning("Cannot restart server. Jar file not found. Exiting..");
					System.exit(0);
				}

				// get JVM arguments
				RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
				List<String> arguments = runtimeMxBean.getInputArguments();

				// build execution command
				ArrayList<String> command = new ArrayList<>();
				command.add(javaBin);
				command.add("-jar");
				command.add(currentJar.getPath());
				command.addAll(arguments);

				// restart server
				ProcessBuilder builder = new ProcessBuilder(command);
				builder.start();

				// exit
				System.exit(0);
			}

			// exception occurred during process
			catch (Exception e) {
				logger_.log(Level.WARNING, "Exception occurred during restarting server:", e);
			}
		}

		// exit
		if (exit) {
			System.exit(0);
		}
	}

	/**
	 * Returns server statistics.
	 *
	 * @return Server statistics.
	 */
	public ArrayList<AnalysisServerStatistic> getStatistics() {
		return statistics_;
	}

	/**
	 * Returns server properties.
	 *
	 * @return Server properties.
	 */
	public Properties getProperties() {
		return properties_;
	}

	/**
	 * Returns server logger.
	 *
	 * @return Server logger.
	 */
	public Logger getLogger() {
		return logger_;
	}

	/**
	 * Returns thread pool.
	 *
	 * @return Thread pool.
	 */
	public ExecutorService getThreadPool() {
		return threadPool_;
	}

	/**
	 * Returns network server.
	 *
	 * @return Network server.
	 */
	public Server getNetworkServer() {
		return networkServer_;
	}

	/**
	 * Returns client lobby.
	 *
	 * @return Client lobby.
	 */
	public Lobby getLobby() {
		return lobby_;
	}

	/**
	 * Returns the connected clients of the server.
	 *
	 * @return List containing the connected clients of the server.
	 */
	public List<AnalysisClient> getClients() {
		return clients_;
	}

	/**
	 * Checks whether the given alias matches with the alias of a connected client. If so, returns the client or <code>null</code>.
	 *
	 * @param alias
	 *            Client alias to check.
	 * @return The client with the given alias or <code>null</code> if no client is connected with the given alias.
	 */
	public AnalysisClient getClient(String alias) {

		// sync over clients
		synchronized (clients_) {

			// get clients
			Iterator<AnalysisClient> i = clients_.iterator();

			// loop over clients
			while (i.hasNext()) {

				// get client
				AnalysisClient c = i.next();

				// client alias matches
				if (c.getAlias().equals(alias))
					return c;
			}
		}

		// client doesn't exist
		return null;
	}

	/**
	 * Creates and adds the given client.
	 *
	 * @param client
	 *            Client to add.
	 */
	public void addClient(AnalysisClient client) {

		// add client
		synchronized (clients_) {
			clients_.add(client);
		}

		// log added info
		logger_.info("Client '" + client.getAlias() + "' added to connected clients.");
	}

	/**
	 * Removes the given client.
	 *
	 * @param client
	 *            Client to remove.
	 */
	public void removeClient(AnalysisClient client) {

		// remove client
		synchronized (clients_) {
			clients_.remove(client);
		}

		// log removal
		logger_.info("Client '" + client.getAlias() + "' removed from connected clients.");
	}

	/**
	 * Increments analysis requests.
	 *
	 * @return The updated value.
	 */
	public int incrementAnalysisRequests() {
		return analysisRequests_.incrementAndGet();
	}

	/**
	 * Increments failed analyses.
	 *
	 * @return The updated value.
	 */
	public int incrementFailedAnalyses() {
		return failedAnalyses_.incrementAndGet();
	}

	/**
	 * Returns analysis requests and resets the value.
	 *
	 * @return Analysis requests.
	 */
	public int getAnalysisRequests() {
		return analysisRequests_.getAndSet(0);
	}

	/**
	 * Returns failed analyses and resets the value.
	 *
	 * @return Failed analyses.
	 */
	public int getFailedAnalyses() {
		return failedAnalyses_.getAndSet(0);
	}
}