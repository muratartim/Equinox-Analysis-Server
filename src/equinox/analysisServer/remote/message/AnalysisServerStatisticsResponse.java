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

import equinox.analysisServer.remote.data.AnalysisServerStatistic;
import equinox.serverUtilities.BigMessage;

/**
 * Class for exchange server statistics response network message.
 *
 * @author Murat Artim
 * @date 22 Feb 2018
 * @time 11:21:37
 */
public class AnalysisServerStatisticsResponse extends AnalysisMessage implements BigMessage {

	/** Serial id. */
	private static final long serialVersionUID = 1L;

	/** Time series dataset. */
	private AnalysisServerStatistic[] statistics;

	/**
	 * No argument constructor for serialization.
	 */
	public AnalysisServerStatisticsResponse() {
	}

	/**
	 * Returns statistics.
	 *
	 * @return Statistics.
	 */
	public AnalysisServerStatistic[] getStatistics() {
		return statistics;
	}

	/**
	 * Sets statistics.
	 *
	 * @param statistics
	 *            Statistics.
	 */
	public void setStatistics(AnalysisServerStatistic[] statistics) {
		this.statistics = statistics;
	}

	@Override
	public boolean isReallyBig() {
		return statistics == null ? false : statistics.length > 30;
	}
}
