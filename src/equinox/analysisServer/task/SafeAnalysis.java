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
import java.io.BufferedWriter;
import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.remote.message.AnalysisRequest;
import equinox.analysisServer.server.AnalysisServer;

/**
 * Abstract class for SAFE analysis task.
 *
 * @author Murat Artim
 * @date 5 May 2017
 * @time 11:24:41
 *
 */
public abstract class SafeAnalysis extends AnalysisTask {

	/** File paths. */
	protected Path tempMaterialFile_, logFile_, erreursFile_, dossierFile_;

	/**
	 * Creates SAFE analysis task.
	 *
	 * @param server
	 *            Server instance.
	 * @param client
	 *            Requesting client.
	 * @param request
	 *            Analysis request message.
	 */
	public SafeAnalysis(AnalysisServer server, AnalysisClient client, AnalysisRequest request) {
		super(server, client, request);
	}

	@Override
	protected List<Path> getTemporaryFiles() {

		// call ancestor
		List<Path> tempFiles = super.getTemporaryFiles();
		if (tempFiles == null) {
			tempFiles = new ArrayList<>();
		}

		// add material file (if exists)
		if (tempMaterialFile_ != null && Files.exists(tempMaterialFile_)) {
			tempFiles.add(tempMaterialFile_);
		}

		// return
		return tempFiles;
	}

	/**
	 * Executes analysis script.
	 *
	 * @param analysisDirectory
	 *            Analysis directory.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	protected void runAnalysis(Path analysisDirectory) throws Exception {

		// create paths to output files
		dossierFile_ = analysisDirectory.resolve("output.dossier");
		erreursFile_ = analysisDirectory.resolve("output.erreurs");

		// create process builder
		ProcessBuilder pb = new ProcessBuilder("safe_run", "aspectre", "output.sigma");

		// execute process and wait to end
		pb.directory(analysisDirectory.toFile());
		logFile_ = analysisDirectory.resolve("output.log");
		File log = logFile_.toFile();
		pb.redirectErrorStream(true);
		pb.redirectOutput(Redirect.appendTo(log));
		Process process = pb.start();
		assert pb.redirectInput() == Redirect.PIPE;
		assert pb.redirectOutput().file() == log;
		assert process.getInputStream().read() == -1;

		// perl script failed
		if (process.waitFor() != 0)
			throw new Exception("SAFE analysis failed! See 'output.log' file for details.");

		// SAFE analysis failed
		if (!Files.exists(dossierFile_) || Files.exists(erreursFile_))
			throw new Exception("SAFE analysis failed! See 'output.erreurs' file for details.");
	}

	/**
	 * Copies material file to SAFE materials directory.
	 *
	 * @param materialFile
	 *            Path to material file.
	 * @param analysisDirectory
	 *            Analysis directory.
	 * @param isLinearPropagation
	 *            True if this is linear propagation analysis. In this case, the Elber constants A and M will be returned, otherwise null will be returned.
	 * @return Returns an array containing the Elber constants 'A' and 'M', respectively, or null if this is not a linear propagation analysis.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	protected double[] copyMaterialFile(Path materialFile, Path analysisDirectory, boolean isLinearPropagation) throws Exception {

		// initialize array
		double[] elberConstants = null;

		// analysis directory could not be found
		Path fileName = analysisDirectory.getFileName();
		if (fileName == null)
			throw new Exception("Cannot copy material file to SAFE materials directory. No analysis directory found.");

		// create path to temporary material file
		tempMaterialFile_ = Paths.get(server_.getProperties().getProperty("safe.materialDirectory")).resolve(fileName.toString() + ".mat");

		// create file writer
		try (BufferedWriter writer = Files.newBufferedWriter(tempMaterialFile_, Charset.defaultCharset())) {

			// create file reader
			try (BufferedReader reader = Files.newBufferedReader(materialFile, Charset.defaultCharset())) {

				// read default material file till the end
				String line = null;
				while ((line = reader.readLine()) != null) {

					// linear propagation analysis
					if (isLinearPropagation) {

						// propagation elber constant A
						if (line.startsWith("ABREMOD '%ELBA'")) {
							if (elberConstants == null) {
								elberConstants = new double[2];
							}
							elberConstants[0] = Double.parseDouble(line.split("'")[3].trim());
						}

						// propagation elber constant M
						else if (line.startsWith("ABREMOD '%ELBN'")) {
							if (elberConstants == null) {
								elberConstants = new double[2];
							}
							elberConstants[1] = Double.parseDouble(line.split("'")[3].trim());
						}
					}

					// write
					writer.write(line);
					writer.newLine();
				}
			}
		}

		// return Elber constants
		return elberConstants;
	}

	/**
	 * Modifies SIGMA file by setting material name as analysis directory name.
	 *
	 * @param sigmaFile
	 *            SIGMA file.
	 * @param analysisDirectory
	 *            Analysis directory.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	protected static void modifySIGMAFile(Path sigmaFile, Path analysisDirectory) throws Exception {

		// analysis directory could not be found
		Path fileName = analysisDirectory.getFileName();
		if (fileName == null)
			throw new Exception("Cannot modify sigma file. No analysis directory found.");

		// create new path
		Path newSigmaFile = analysisDirectory.resolve("output.sigma");

		// create file writer
		try (BufferedWriter writer = Files.newBufferedWriter(newSigmaFile, Charset.defaultCharset())) {

			// create file reader
			try (BufferedReader reader = Files.newBufferedReader(sigmaFile, Charset.defaultCharset())) {

				// read default material file till the end
				String line = null;
				while ((line = reader.readLine()) != null) {

					// set material name
					if (line.startsWith("ABREMOD '%NOMMAT'")) {
						line = "ABREMOD '%NOMMAT' '" + fileName.toString() + "' ! MATERIAL NAME";
					}

					// write
					writer.write(line);
					writer.newLine();
				}
			}
		}
	}
}
