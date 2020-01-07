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

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;

import equinox.analysisServer.remote.data.AnalysisServerStatistic;
import equinox.analysisServer.server.AnalysisServer;

/**
 * Class for collect server statistics task.
 *
 * @author Murat Artim
 * @date 5 Apr 2017
 * @time 13:20:34
 *
 */
public final class CollectServerStatistics extends ServerTask {

	/**
	 * Creates collect server statistics task.
	 *
	 * @param server
	 *            Server instance.
	 */
	public CollectServerStatistics(AnalysisServer server) {
		super(server);
	}

	@Override
	protected void runTask() throws Exception {

		// log info
		if (server_.getProperties().getProperty("stat.log").equals("yes")) {
			server_.getLogger().info("Collecting server statistics...");
		}

		// remove expired statistics
		removeExpiredStatistics();

		// insert new statistics
		AnalysisServerStatistic stats = new AnalysisServerStatistic();
		stats.setAnalysisRequests(server_.getAnalysisRequests());
		stats.setFailedAnalyses(server_.getFailedAnalyses());
		stats.setActiveThreads(((ThreadPoolExecutor) server_.getThreadPool()).getActiveCount());
		stats.setThreadPoolSize(((ThreadPoolExecutor) server_.getThreadPool()).getPoolSize());
		server_.getStatistics().add(stats);
	}

	@Override
	protected void failed(Exception e) {
		server_.getLogger().log(Level.WARNING, "Exception occurred during collecting server statistics.", e);
	}

	/**
	 * Removes the expired statistics.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void removeExpiredStatistics() throws Exception {

		// get statistics expiry days
		int expiryDays = Integer.parseInt(server_.getProperties().getProperty("stat.expiry"));

		// compute epoch
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -expiryDays);
		Date epoch = new Date(calendar.getTimeInMillis());

		// remove expired
		server_.getStatistics().removeIf(x -> x.getRecorded().before(epoch));
	}
}
