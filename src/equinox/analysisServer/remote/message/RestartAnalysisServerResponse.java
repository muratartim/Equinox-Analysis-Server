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
 * Class for restart analysis server response network message.
 *
 * @author Murat Artim
 * @date 12 Jul 2018
 * @time 22:35:54
 */
public class RestartAnalysisServerResponse extends AnalysisMessage {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** True if server is restarted. */
	private boolean isRestarted;

	/**
	 * Empty constructor for serialization.
	 */
	public RestartAnalysisServerResponse() {
	}

	/**
	 * Returns true if server is restarted.
	 *
	 * @return True if server is restarted.
	 */
	public boolean isRestarted() {
		return isRestarted;
	}

	/**
	 * Sets whether or not server is restarted.
	 *
	 * @param isRestarted
	 *            True if server is restarted.
	 */
	public void setServerRestarted(boolean isRestarted) {
		this.isRestarted = isRestarted;
	}
}