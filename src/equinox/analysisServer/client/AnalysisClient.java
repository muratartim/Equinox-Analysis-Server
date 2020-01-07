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

import java.util.HashMap;
import java.util.logging.Level;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import equinox.analysisServer.server.Lobby;
import equinox.serverUtilities.BigMessage;
import equinox.serverUtilities.NetworkMessage;
import equinox.serverUtilities.PartialMessage;
import equinox.serverUtilities.SplitMessage;

/**
 * Class for analysis client. This class stores all information regarding the analysis client.
 *
 * @version 1.0
 * @author Murat Artim
 * @time 12:11:19 AM
 * @date Aug 25, 2010
 */
public class AnalysisClient {

	/** Client's alias. */
	private final String alias_;

	/** Connection object to send messages to this client. */
	private final ClientConnection connection_;

	/** Map containing the partial messages and their ancestors' hash codes. */
	private final HashMap<Integer, PartialMessage[]> partialMessages_ = new HashMap<>();

	/** Server lobby. */
	private final Lobby lobby_;

	/**
	 * Creates client.
	 *
	 * @param connection
	 *            Connection of the client.
	 * @param alias
	 *            Client alias.
	 * @param lobby
	 *            Server lobby.
	 */
	public AnalysisClient(ClientConnection connection, String alias, Lobby lobby) {
		alias_ = alias;
		connection_ = connection.setClient(this);
		lobby_ = lobby;
	}

	/**
	 * Returns client alias.
	 *
	 * @return Client alias.
	 */
	public String getAlias() {
		return alias_;
	}

	/**
	 * Returns the lobby of this client.
	 *
	 * @return The lobby of this client.
	 */
	public Lobby getLobby() {
		return lobby_;
	}

	/**
	 * Sends given message to this client.
	 *
	 * @param message
	 *            Message to send.
	 * @return The number of bytes sent.
	 */
	synchronized public int sendMessage(NetworkMessage message) {

		try {

			// not a big message
			if (message instanceof BigMessage == false)
				return connection_.sendTCP(message);

			// not big
			if (!((BigMessage) message).isReallyBig())
				return connection_.sendTCP(message);

			// split message into partial messages
			PartialMessage[] parts = SplitMessage.splitMessage((BigMessage) message);

			// no need to split message
			if (parts == null)
				return connection_.sendTCP(message);

			// send parts
			int bytesSent = 0;
			for (PartialMessage part : parts) {
				bytesSent += connection_.sendTCP(part);
			}
			return bytesSent;
		}

		// exception occurred during sending message
		catch (Exception e) {
			lobby_.getServer().getLogger().log(Level.WARNING, "Exception occurred during sending message to client.", e);
			return 0;
		}
	}

	/**
	 * Disconnects this client by closing its connection.
	 */
	public void disconnect() {
		connection_.close();
	}

	/**
	 * Responds to partial messages received from the client.
	 *
	 * @param part
	 *            Partial message to respond.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	public void receivePartialMessage(PartialMessage part) throws Exception {

		// get message parameters
		int id = part.getID();
		int numParts = part.getNumParts();
		int index = part.getIndex();

		// get all current parts
		PartialMessage[] parts = partialMessages_.get(id);

		// ID not contained
		if (parts == null) {
			parts = new PartialMessage[numParts];
			parts[index] = part;
			partialMessages_.put(id, parts);
		}
		else {
			parts[index] = part;
		}

		// check whether all parts completed
		for (PartialMessage p : parts)
			if (p == null)
				return;

		// combine parts and respond with client lobby
		lobby_.respond(this, SplitMessage.combineMessages(parts));

		// remove parts
		partialMessages_.remove(id);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(47, 127).append(alias_).toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AnalysisClient))
			return false;
		if (o == this)
			return true;
		AnalysisClient client = (AnalysisClient) o;
		return new EqualsBuilder().append(alias_, client.alias_).isEquals();
	}
}
