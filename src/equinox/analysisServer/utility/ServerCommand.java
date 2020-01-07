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
 * Enumeration for server commands.
 *
 * @author Murat Artim
 * @date 28 Mar 2017
 * @time 11:50:11
 */
public enum ServerCommand {

	/** Server command. */
	// @formatter:off
	HELP("help", "Lists all server commands."),
	STOP("stop", "Stops server."),
	CLIENTS("clients", "Lists currently connected clients."),
	PROPERTIES("properties", "Lists all current server properties."),
	TEMPORARY_FILES("manage temporary files", "Enables/disables temporary file deletion."),
	STATISTICS("statistics", "Prints server statistics.");
	// @formatter:on

	/** Command and description text. */
	private final String command_, description_;

	/**
	 * Creates server command.
	 *
	 * @param command
	 *            Command text.
	 * @param description
	 *            Description of server command.
	 */
	ServerCommand(String command, String description) {
		command_ = command;
		description_ = description;
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

	/**
	 * Returns server command description.
	 *
	 * @return Server command description.
	 */
	public String getDescription() {
		return "<" + command_ + ">: " + description_;
	}
}
