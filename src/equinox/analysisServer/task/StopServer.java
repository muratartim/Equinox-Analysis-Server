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

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.remote.message.StopAnalysisServerRequest;
import equinox.analysisServer.remote.message.StopAnalysisServerRequestFailed;
import equinox.analysisServer.remote.message.StopAnalysisServerResponse;

/**
 * Class for stop server task.
 *
 * @author Murat Artim
 * @date 11 Jul 2018
 * @time 00:45:15
 */
public class StopServer extends ServerTask {

	/** Requesting client. */
	private final AnalysisClient client;

	/** Request message. */
	private final StopAnalysisServerRequest request;

	/**
	 * Creates stop server task.
	 *
	 * @param client
	 *            Requesting client.
	 * @param request
	 *            Request message.
	 */
	public StopServer(AnalysisClient client, StopAnalysisServerRequest request) {
		super(client.getLobby().getServer());
		this.client = client;
		this.request = request;
	}

	@Override
	protected void runTask() throws Exception {

		// create encryptor
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword("EquinoxAnalysisServer_2018");

		// get request password
		String password = request.getPassword();
		if (password == null || password.trim().isEmpty())
			throw new Exception("Invalid password supplied for stop analysis server command.");
		password = encryptor.decrypt(password);

		// check password
		boolean isCorrect = password.equals(server_.getProperties().getProperty("stop.password"));

		// create response
		StopAnalysisServerResponse response = new StopAnalysisServerResponse();
		response.setListenerHashCode(request.getListenerHashCode());
		response.setServerStopped(isCorrect);

		// send it
		client.sendMessage(response);

		// correct password
		if (isCorrect) {

			// log server stop
			server_.getLogger().info("Stopping analysis server on request from '" + client.getAlias() + "'.");

			// stop server
			server_.stopServer(false, true);
		}
	}

	@Override
	protected void failed(Exception e) {

		// log exception
		server_.getLogger().log(Level.WARNING, "Stop analysis server task failed.", e);

		// no client
		if (client == null)
			return;

		// send query failed message to client
		StopAnalysisServerRequestFailed message = new StopAnalysisServerRequestFailed();
		message.setListenerHashCode(request.getListenerHashCode());
		message.setException(e);
		client.sendMessage(message);
	}
}