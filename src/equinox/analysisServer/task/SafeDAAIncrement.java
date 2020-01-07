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

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.remote.message.DAAIncrementComplete;
import equinox.analysisServer.remote.message.SafeDAAIncrementRequest;
import equinox.analysisServer.server.AnalysisServer;
import equinox.analysisServer.utility.Utility;

/**
 * Class for SAFE damage angle analysis increment task.
 *
 * @author Murat Artim
 * @date 5 May 2017
 * @time 14:35:03
 *
 */
public final class SafeDAAIncrement extends SafeAnalysis {

	/**
	 * Creates SAFE damage angle analysis increment task.
	 *
	 * @param server
	 *            Server instance.
	 * @param client
	 *            Requesting client.
	 * @param request
	 *            Request message.
	 */
	public SafeDAAIncrement(AnalysisServer server, AnalysisClient client, SafeDAAIncrementRequest request) {
		super(server, client, request);
	}

	@Override
	protected void runTask() throws Exception {

		// no client
		if (client_ == null)
			throw new Exception("No connected client found for analysis requester username.");

		// create analysis directory
		sendProgressMessage("Creating analysis directory...");
		Path analysisDirectory = getWorkingDirectory();

		// download input files from server
		sendProgressMessage("Downloading input files from central database...");
		Path inputArchive = downloadInputFile();

		// extract input files from archive
		sendProgressMessage("Extracting input files...");
		Utility.extractAllFilesFromZIP(inputArchive, analysisDirectory);

		// input files don't exist
		Path materialFile = analysisDirectory.resolve("material.mat");
		Path sigmaFile = analysisDirectory.resolve("input.sigma");
		if (!Files.exists(materialFile) || !Files.exists(sigmaFile))
			throw new Exception("Cannot find input MAT and SIGMA files.");

		// copy material file to material database
		sendProgressMessage("Copying material file to material database...");
		copyMaterialFile(materialFile, analysisDirectory, false);

		// material file doesn't exist
		if (tempMaterialFile_ == null || !Files.exists(tempMaterialFile_))
			throw new Exception("Cannot copy material file to metarials directory.");

		// modify SIGMA file (set material name as analysis directory name)
		sendProgressMessage("Modifying SIGMA file...");
		modifySIGMAFile(sigmaFile, analysisDirectory);

		// run analysis
		sendProgressMessage("Analysis running...");
		runAnalysis(analysisDirectory);

		// extract results from dossier file
		sendProgressMessage("Extracting analysis results...");
		extractResults();
	}

	@Override
	protected ArrayList<Path> getOutputFiles(boolean isSucceeded) throws Exception {
		return null;
	}

	/**
	 * Extracts analysis results and send analysis complete message back to requesting client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractResults() throws Exception {

		// create file reader
		double fatEq = -1.0;
		try (BufferedReader reader = Files.newBufferedReader(dossierFile_, Charset.defaultCharset())) {

			// read file till the end
			String[] split;
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(" :  (Rain-Flow)")) {
					line = reader.readLine();
					line = reader.readLine();
					if (line != null) {
						split = line.split(":");
						fatEq = Double.parseDouble(split[3].trim());
					}
				}
			}
		}

		// no equivalent stress found
		if (fatEq == -1.0)
			throw new Exception("SAFE analysis failed! Cannot find fatigue equivalent stress in output dossier file.");

		// create and send analysis complete message
		DAAIncrementComplete message = new DAAIncrementComplete();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setEquivalentStress(fatEq);
		client_.sendMessage(message);
	}
}
