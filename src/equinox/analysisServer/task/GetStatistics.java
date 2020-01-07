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
package equinox.analysisServer.task;

import java.util.logging.Level;

import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.remote.data.AnalysisServerStatistic;
import equinox.analysisServer.remote.message.AnalysisServerStatisticsRequest;
import equinox.analysisServer.remote.message.AnalysisServerStatisticsRequestFailed;
import equinox.analysisServer.remote.message.AnalysisServerStatisticsResponse;

/**
 * Class for get statistics task.
 *
 * @author Murat Artim
 * @date 18 Jun 2018
 * @time 13:20:15
 */
public class GetStatistics extends ServerTask {

	/** Requesting client. */
	private final AnalysisClient client;

	/** Request message. */
	private final AnalysisServerStatisticsRequest request;

	/**
	 * Creates get statistics task.
	 *
	 * @param client
	 *            Requesting client.
	 * @param request
	 *            Request message.
	 */
	public GetStatistics(AnalysisClient client, AnalysisServerStatisticsRequest request) {
		super(client.getLobby().getServer());
		this.client = client;
		this.request = request;
	}

	@Override
	protected void runTask() throws Exception {

		// get requested statistics
		AnalysisServerStatistic[] stats = server_.getStatistics().stream().filter(x -> x.getRecorded().after(request.getFrom()) && x.getRecorded().before(request.getTo())).toArray(AnalysisServerStatistic[]::new);

		// create response
		AnalysisServerStatisticsResponse response = new AnalysisServerStatisticsResponse();
		response.setListenerHashCode(request.getListenerHashCode());
		response.setStatistics(stats);

		// send it
		client.sendMessage(response);
	}

	@Override
	protected void failed(Exception e) {

		// log exception
		server_.getLogger().log(Level.WARNING, "Get analysis server statistics failed.", e);

		// no client
		if (client == null)
			return;

		// send query failed message to client
		AnalysisServerStatisticsRequestFailed message = new AnalysisServerStatisticsRequestFailed();
		message.setListenerHashCode(request.getListenerHashCode());
		message.setException(e);
		client.sendMessage(message);
	}
}