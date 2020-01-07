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
package equinox.analysisServer.task;

import java.util.logging.Level;

import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.client.ClientConnection;
import equinox.analysisServer.remote.message.HandshakeWithAnalysisServer;
import equinox.analysisServer.server.AnalysisServer;

/**
 * Class for process handshake task.
 *
 * @author Murat Artim
 * @date 20 Dec 2017
 * @time 13:34:52
 */
public class ProcessHandshake extends ServerTask {

	/** Client connection. */
	private final ClientConnection clientConnection_;

	/** Handshake message. */
	private final HandshakeWithAnalysisServer message_;

	/**
	 * Creates process handshake task.
	 *
	 * @param clientConnection
	 *            Client connection.
	 * @param message
	 *            Handshake message.
	 * @param server
	 *            Server instance.
	 */
	public ProcessHandshake(ClientConnection clientConnection, HandshakeWithAnalysisServer message, AnalysisServer server) {
		super(server);
		clientConnection_ = clientConnection;
		message_ = message;
	}

	@Override
	protected void runTask() throws Exception {

		// create client
		AnalysisClient client = new AnalysisClient(clientConnection_, message_.getAlias(), server_.getLobby());

		// add client to server
		server_.addClient(client);

		// set timeout for connection.
		clientConnection_.setTimeout(Integer.parseInt(server_.getProperties().getProperty("ns.connectionTimeout")));

		// set message parameters
		message_.setReply(true);

		// respond to client
		client.sendMessage(message_);
	}

	@Override
	protected void failed(Exception e) {
		server_.getLogger().log(Level.WARNING, "Exception occurred during responding to client handshake.", e);
	}
}