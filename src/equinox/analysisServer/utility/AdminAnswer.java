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
package equinox.analysisServer.utility;

/**
 * Enumeration for standard administrator answers.
 *
 * @author Murat Artim
 * @date 29 Mar 2017
 * @time 12:24:32
 *
 */
public enum AdminAnswer {

	/** Administrator answer. */
	CANCEL("cancel"), YES("yes"), NO("no");

	/** Command text. */
	private final String command_;

	/**
	 * Creates server command.
	 *
	 * @param command
	 *            Command text.
	 */
	AdminAnswer(String command) {
		command_ = command;
	}

	/**
	 * Returns true if given line matches this server command.
	 *
	 * @param line
	 *            Input line to check.
	 * @return True if given line matches this server command.
	 */
	public boolean matches(String line) {
		return command_.equalsIgnoreCase(line);
	}
}
