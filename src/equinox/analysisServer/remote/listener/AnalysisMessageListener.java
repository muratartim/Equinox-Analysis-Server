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
package equinox.analysisServer.remote.listener;

import java.io.Serializable;

import equinox.analysisServer.remote.message.AnalysisMessage;

/**
 * Interface for all analysis message listeners.
 *
 * @author Murat Artim
 * @date 31 Mar 2017
 * @time 10:07:46
 *
 */
public interface AnalysisMessageListener extends Serializable {

	/**
	 * Responds to given server message.
	 *
	 * @param message
	 *            Server message to respond.
	 * @throws Exception
	 *             If exception occurs during responding the server message.
	 */
	void respondToAnalysisMessage(AnalysisMessage message) throws Exception;
}
