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
 * Class for stop analysis server response network message.
 *
 * @author Murat Artim
 * @date 10 Jul 2018
 * @time 00:53:07
 */
public class StopAnalysisServerResponse extends AnalysisMessage {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** True if server is stopped. */
	private boolean isStopped;

	/**
	 * Empty constructor for serialization.
	 */
	public StopAnalysisServerResponse() {
	}

	/**
	 * Returns true if server is stopped.
	 *
	 * @return True if server is stopped.
	 */
	public boolean isStopped() {
		return isStopped;
	}

	/**
	 * Sets whether or not server is stopped.
	 *
	 * @param isStopped
	 *            True if server is stopped.
	 */
	public void setServerStopped(boolean isStopped) {
		this.isStopped = isStopped;
	}
}
