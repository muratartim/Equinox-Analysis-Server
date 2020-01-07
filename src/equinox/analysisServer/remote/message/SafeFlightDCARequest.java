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
 * Class for SAFE typical flight damage contribution analysis request network message.
 *
 * @author Murat Artim
 * @date 3 May 2017
 * @time 16:41:04
 *
 */
public final class SafeFlightDCARequest extends AnalysisRequest {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/**
	 * No argument constructor for serialization.
	 */
	public SafeFlightDCARequest() {
	}
}
