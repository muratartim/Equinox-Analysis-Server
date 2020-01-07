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
package equinox.analysisServer.client;

import com.esotericsoftware.kryonet.Connection;

/**
 * Client connection to communicate with the client.
 *
 * @version 1.0
 * @author Murat Artim
 * @time 10:17:21 PM
 * @date Jul 10, 2011
 *
 */
public class ClientConnection extends Connection {

	/** The client of this connection. */
	private AnalysisClient client_ = null;

	/**
	 * Sets the client to this connection. This method should only be called from the client constructor after all client attributes are set.
	 *
	 * @param client
	 *            Client to set.
	 * @return This connection.
	 */
	protected ClientConnection setClient(AnalysisClient client) {
		client_ = client;
		return this;
	}

	/**
	 * Returns the client of this connection, or null if the connection did not login yet.
	 *
	 * @return The client of this connection, or null if the connection did not login yet.
	 */
	public AnalysisClient getClient() {
		return client_;
	}
}
