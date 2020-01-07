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
package equinox.analysisServer.remote.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Class for analysis server statistic.
 *
 * @author Murat Artim
 * @date 22 Jun 2018
 * @time 20:22:05
 */
public class AnalysisServerStatistic implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = 1L;

	/** Record time. */
	private final Date recorded;

	/** Statistics. */
	private int analysisRequests, failedAnalyses, threadPoolSize, activeThreads;

	/**
	 * No argument constructor for serialization.
	 */
	public AnalysisServerStatistic() {
		recorded = new Date();
	}

	/**
	 * Sets analysis requests.
	 *
	 * @param analysisRequests
	 *            Analysis requests.
	 */
	public void setAnalysisRequests(int analysisRequests) {
		this.analysisRequests = analysisRequests;
	}

	/**
	 * Sets failed analyses.
	 *
	 * @param failedAnalyses
	 *            Failed analyses.
	 */
	public void setFailedAnalyses(int failedAnalyses) {
		this.failedAnalyses = failedAnalyses;
	}

	/**
	 * Sets the thread pool size.
	 *
	 * @param threadPoolSize
	 *            Thread pool size.
	 */
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	/**
	 * Sets number of active threads.
	 *
	 * @param activeThreads
	 *            Number of active threads.
	 */
	public void setActiveThreads(int activeThreads) {
		this.activeThreads = activeThreads;
	}

	/**
	 * Returns the record time.
	 *
	 * @return The record time.
	 */
	public Date getRecorded() {
		return recorded;
	}

	/**
	 * Returns number of analysis requests.
	 *
	 * @return Number of analysis requests.
	 */
	public int getAnalysisRequests() {
		return analysisRequests;
	}

	/**
	 * Returns number of failed analyses.
	 *
	 * @return Number of failed analyses.
	 */
	public int getFailedAnalyses() {
		return failedAnalyses;
	}

	/**
	 * Returns the thread pool size.
	 *
	 * @return The thread pool size.
	 */
	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	/**
	 * Returns the number of active threads.
	 *
	 * @return Number of active threads.
	 */
	public int getActiveThreads() {
		return activeThreads;
	}

	@Override
	public String toString() {
		String text = "Recorded: " + recorded.toString();
		text += ", Analysis Requests: " + analysisRequests;
		text += ", Failed Analyses: " + failedAnalyses;
		text += ", Thread Pool Size: " + threadPoolSize;
		text += ", Active Threads: " + activeThreads;
		return text;
	}
}