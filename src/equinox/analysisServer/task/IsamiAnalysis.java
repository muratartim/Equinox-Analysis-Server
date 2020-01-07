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

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.remote.message.AnalysisRequest;
import equinox.analysisServer.server.AnalysisServer;

/**
 * Abstract class for ISAMI analysis task.
 *
 * @author Murat Artim
 * @date 20 May 2017
 * @time 15:31:19
 *
 */
public abstract class IsamiAnalysis extends AnalysisTask {

	/** Analysis files. */
	protected Path logFile_, outFile_, htmlFile_, csvFile_;

	/**
	 * Creates ISAMI analysis task.
	 *
	 * @param server
	 *            Server instance.
	 * @param client
	 *            Requesting client.
	 * @param request
	 *            Request message.
	 */
	public IsamiAnalysis(AnalysisServer server, AnalysisClient client, AnalysisRequest request) {
		super(server, client, request);
	}

	/**
	 * Executes analysis script.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	protected void runAnalysis() throws Exception {

		// get working directory
		Path workingDirectory = getWorkingDirectory();

		// create paths to output files
		htmlFile_ = workingDirectory.resolve("input.caesamexternalfiles").resolve("spectrum_analysis").resolve("analysisName").resolve("analysisName.html");
		csvFile_ = workingDirectory.resolve("input.caesamexternalfiles").resolve("spectrum_analysis").resolve("analysisName").resolve("analysisName.csv");

		// create process builder
		ProcessBuilder pb = new ProcessBuilder("bsub.isami", "isamiConfigFile.txt");

		// execute process and wait to end
		pb.directory(workingDirectory.toFile());
		logFile_ = workingDirectory.resolve("submission.log");
		File log = logFile_.toFile();
		pb.redirectErrorStream(true);
		pb.redirectOutput(Redirect.appendTo(log));
		Process process = pb.start();
		assert pb.redirectInput() == Redirect.PIPE;
		assert pb.redirectOutput().file() == log;
		assert process.getInputStream().read() == -1;

		// script failed
		if (process.waitFor() != 0)
			throw new Exception("ISAMI analysis submission failed! See 'submission.log' file for details.");

		// wait for analysis to complete
		waitForAnalysis();

		// ISAMI analysis failed
		if (!Files.exists(htmlFile_) || !Files.exists(csvFile_))
			throw new Exception("ISAMI analysis failed! See '" + outFile_.getFileName().toString() + "' file for details.");
	}

	/**
	 * Waits for ISAMI analysis to complete.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void waitForAnalysis() throws Exception {

		// get working directory
		Path workingDirectory = getWorkingDirectory();

		// create watch service
		try (WatchService watcher = FileSystems.getDefault().newWatchService()) {

			// register directory for create event
			workingDirectory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);

			// watch for changes
			while (true) {

				// get a watch key
				WatchKey key = watcher.take();

				// loop over events
				for (WatchEvent<?> event : key.pollEvents()) {

					// overflow event
					if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
						continue;
					}

					// get newly created file
					@SuppressWarnings("unchecked")
					Path fileName = ((WatchEvent<Path>) event).context();

					// output file
					if (fileName.toString().startsWith("out_")) {
						key.reset();
						Path outputFile = workingDirectory.resolve(fileName.toString());
						outFile_ = workingDirectory.resolve(fileName.toString() + ".out");
						Files.move(outputFile, outFile_);
						return;
					}
				}

				// reset the watch key
				if (!key.reset()) {
					break;
				}
			}
		}
	}
}
