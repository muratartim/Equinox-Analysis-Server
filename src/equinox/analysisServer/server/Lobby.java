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

import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.remote.message.AnalysisRequest;
import equinox.analysisServer.remote.message.AnalysisServerStatisticsRequest;
import equinox.analysisServer.remote.message.IsamiESARequest;
import equinox.analysisServer.remote.message.RestartAnalysisServerRequest;
import equinox.analysisServer.remote.message.SafeDAAIncrementRequest;
import equinox.analysisServer.remote.message.SafeDCAIncrementRequest;
import equinox.analysisServer.remote.message.SafeESARequest;
import equinox.analysisServer.remote.message.SafeFlightDCARequest;
import equinox.analysisServer.remote.message.StopAnalysisServerRequest;
import equinox.analysisServer.task.AnalysisTask;
import equinox.analysisServer.task.GetStatistics;
import equinox.analysisServer.task.IsamiESA;
import equinox.analysisServer.task.RestartServer;
import equinox.analysisServer.task.SafeDAAIncrement;
import equinox.analysisServer.task.SafeDCAIncrement;
import equinox.analysisServer.task.SafeESA;
import equinox.analysisServer.task.SafeFlightDCA;
import equinox.analysisServer.task.StopServer;
import equinox.serverUtilities.NetworkMessage;

/**
 * Class for lobby class. This class manages all communication between clients and server.
 *
 * @author Murat Artim
 * @date Sep 16, 2014
 * @time 6:23:43 PM
 */
public class Lobby {

	/** Server. */
	private final AnalysisServer server_;

	/**
	 * Creates lobby.
	 *
	 * @param server
	 *            Central server of the lobby.
	 */
	public Lobby(AnalysisServer server) {
		server_ = server;
		server_.getLogger().info("Client lobby created.");
	}

	/**
	 * Returns the server of the lobby.
	 *
	 * @return The server of the lobby.
	 */
	public AnalysisServer getServer() {
		return server_;
	}

	/**
	 * Stops this lobby.
	 */
	public void stop() {
		server_.getLogger().info("Client lobby stopped.");
	}

	/**
	 * Responds to given message according to general room protocol.
	 *
	 * @param client
	 *            Client who sent the message.
	 * @param message
	 *            Message to respond.
	 * @throws Exception
	 *             If exception occurs during processing client message.
	 */
	public void respond(AnalysisClient client, NetworkMessage message) throws Exception {

		// analysis request
		if (message instanceof AnalysisRequest) {
			analysisRequest(client, (AnalysisRequest) message);
		}

		// statistics request
		else if (message instanceof AnalysisServerStatisticsRequest) {
			server_.getThreadPool().submit(new GetStatistics(client, (AnalysisServerStatisticsRequest) message));
		}

		// stop server
		else if (message instanceof StopAnalysisServerRequest) {
			Thread thread = new Thread(new StopServer(client, (StopAnalysisServerRequest) message));
			thread.setDaemon(true);
			thread.start();
		}

		// restart server
		else if (message instanceof RestartAnalysisServerRequest) {
			Thread thread = new Thread(new RestartServer(client, (RestartAnalysisServerRequest) message));
			thread.setDaemon(true);
			thread.start();
		}
	}

	/**
	 * Responds to analysis request message form client.
	 *
	 * @param client
	 *            Client who sent the message.
	 * @param message
	 *            Client message.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void analysisRequest(AnalysisClient client, AnalysisRequest message) throws Exception {

		// increment analysis request count statistic
		server_.incrementAnalysisRequests();

		// initialize analysis task
		AnalysisTask task = null;

		// SAFE equivalent stress analysis
		if (message instanceof SafeESARequest) {
			task = new SafeESA(server_, client, (SafeESARequest) message);
		}

		// SAFE flight damage contribution analysis
		else if (message instanceof SafeFlightDCARequest) {
			task = new SafeFlightDCA(server_, client, (SafeFlightDCARequest) message);
		}

		// SAFE damage contribution analysis increment
		else if (message instanceof SafeDCAIncrementRequest) {
			task = new SafeDCAIncrement(server_, client, (SafeDCAIncrementRequest) message);
		}

		// SAFE damage angle analysis increment
		else if (message instanceof SafeDAAIncrementRequest) {
			task = new SafeDAAIncrement(server_, client, (SafeDAAIncrementRequest) message);
		}

		// ISAMI equivalent stress analysis
		else if (message instanceof IsamiESARequest) {
			task = new IsamiESA(server_, client, (IsamiESARequest) message);
		}

		// submit task
		if (task != null) {
			server_.getThreadPool().submit(task);
		}
	}
}