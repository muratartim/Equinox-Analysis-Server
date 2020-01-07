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
import equinox.analysisServer.remote.message.RestartAnalysisServerRequest;
import equinox.analysisServer.remote.message.RestartAnalysisServerRequestFailed;
import equinox.analysisServer.remote.message.RestartAnalysisServerResponse;

/**
 * Class for restart server task.
 *
 * @author Murat Artim
 * @date 12 Jul 2018
 * @time 22:40:18
 */
public class RestartServer extends ServerTask {

	/** Requesting client. */
	private final AnalysisClient client;

	/** Request message. */
	private final RestartAnalysisServerRequest request;

	/**
	 * Creates restart server task.
	 *
	 * @param client
	 *            Requesting client.
	 * @param request
	 *            Request message.
	 */
	public RestartServer(AnalysisClient client, RestartAnalysisServerRequest request) {
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
			throw new Exception("Invalid password supplied for restart analysis server command.");
		password = encryptor.decrypt(password);

		// check password
		boolean isCorrect = password.equals(server_.getProperties().getProperty("stop.password"));

		// create response
		RestartAnalysisServerResponse response = new RestartAnalysisServerResponse();
		response.setListenerHashCode(request.getListenerHashCode());
		response.setServerRestarted(isCorrect);

		// send it
		client.sendMessage(response);

		// correct password
		if (isCorrect) {

			// log server stop
			server_.getLogger().info("Restarting analysis server on request from '" + client.getAlias() + "'.");

			// restart server
			server_.stopServer(true, true);
		}
	}

	@Override
	protected void failed(Exception e) {

		// log exception
		server_.getLogger().log(Level.WARNING, "Restart analysis server task failed.", e);

		// no client
		if (client == null)
			return;

		// send query failed message to client
		RestartAnalysisServerRequestFailed message = new RestartAnalysisServerRequestFailed();
		message.setListenerHashCode(request.getListenerHashCode());
		message.setException(e);
		client.sendMessage(message);
	}
}