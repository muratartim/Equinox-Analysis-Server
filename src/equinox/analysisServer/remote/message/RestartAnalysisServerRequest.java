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
 * Class for restart analysis server network message.
 *
 * @author Murat Artim
 * @date 12 Jul 2018
 * @time 22:34:42
 */
public class RestartAnalysisServerRequest extends AnalysisMessage {

	/** Serial id. */
	private static final long serialVersionUID = 1L;

	/** Server restart password. */
	private String password;

	/**
	 * No argument constructor for serialization.
	 */
	public RestartAnalysisServerRequest() {
	}

	/**
	 * Returns server restart password. Note that returned string is 64-bit encrypted.
	 *
	 * @return Server restart password. Note that returned string is 64-bit encrypted.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets server restart password. Note that it must be 64-bit encrypted.
	 *
	 * @param password
	 *            Server restart password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}