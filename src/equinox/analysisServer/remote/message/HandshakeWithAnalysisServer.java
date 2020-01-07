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
package equinox.analysisServer.remote.message;

/**
 * Class for handshake message.
 *
 * @version 1.0
 * @author Murat Artim
 * @time 1:43:02 PM
 * @date Jan 29, 2011
 *
 */
public final class HandshakeWithAnalysisServer extends AnalysisMessage {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** User alias. */
	private String alias_;

	/** Server reply to handshake message. */
	private boolean isSuccessful_;

	/**
	 * No argument constructor for serialization.
	 */
	public HandshakeWithAnalysisServer() {
	}

	/**
	 * Creates a handshake message.
	 *
	 * @param alias
	 *            User alias.
	 */
	public HandshakeWithAnalysisServer(String alias) {
		alias_ = alias;
	}

	/**
	 * Returns the user alias.
	 *
	 * @return The user alias.
	 */
	public String getAlias() {
		return alias_;
	}

	/**
	 * Returns true if handshake is successful.
	 *
	 * @return True if handshake is successful.
	 */
	public boolean isHandshakeSuccessful() {
		return isSuccessful_;
	}

	/**
	 * Sets reply to this handshake message.
	 *
	 * @param isSuccessful
	 *            True if handshake is successful.
	 */
	public void setReply(boolean isSuccessful) {
		isSuccessful_ = isSuccessful;
	}
}