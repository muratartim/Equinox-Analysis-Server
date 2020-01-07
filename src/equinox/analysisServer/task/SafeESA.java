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
import equinox.analysisServer.remote.message.FastESAComplete;
import equinox.analysisServer.remote.message.FullESAComplete;
import equinox.analysisServer.remote.message.SafeESARequest;
import equinox.analysisServer.server.AnalysisServer;
import equinox.analysisServer.utility.Utility;

/**
 * Class for SAFE equivalent stress analysis task.
 *
 * @author Murat Artim
 * @date 30 Mar 2017
 * @time 18:27:22
 *
 */
public final class SafeESA extends SafeAnalysis {

	/** Propagation material values for computing linear propagation equivalent stress from efficiency. */
	private double[] cgAcgM_;

	/**
	 * Creates SAFE equivalent stress analysis task.
	 *
	 * @param server
	 *            Server instance.
	 * @param client
	 *            Requesting client.
	 * @param request
	 *            Request message.
	 */
	public SafeESA(AnalysisServer server, AnalysisClient client, SafeESARequest request) {
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
		boolean isLinearPropagation = ((SafeESARequest) request_).getAnalysisType() == SafeESARequest.LINEAR;
		cgAcgM_ = copyMaterialFile(materialFile, analysisDirectory, isLinearPropagation);

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
		SafeESARequest safeRequest = (SafeESARequest) request_;
		boolean isFast = safeRequest.getFastAnalysis();
		int analysisType = safeRequest.getAnalysisType();

		// fast analysis
		if (isFast) {

			// fatigue
			if (analysisType == SafeESARequest.FATIGUE) {
				extractFastFatigueAnalysisResults();
			}

			// preffas
			else if (analysisType == SafeESARequest.PREFFAS) {
				extractFastPreffasAnalysisResults();
			}

			// linear
			else if (analysisType == SafeESARequest.LINEAR) {
				extractFastLinearAnalysisResults();
			}
		}

		// full analysis
		else {

			// fatigue
			if (analysisType == SafeESARequest.FATIGUE) {
				extractFullFatigueAnalysisResults();
			}

			// preffas
			else if (analysisType == SafeESARequest.PREFFAS) {
				extractFullPreffasAnalysisResults();
			}

			// linear
			else if (analysisType == SafeESARequest.LINEAR) {
				extractFullLinearAnalysisResults();
			}
		}
	}

	@Override
	protected ArrayList<Path> getOutputFiles(boolean isSucceeded) throws Exception {

		// create list to store output files
		ArrayList<Path> outputs = new ArrayList<>();

		// add dossier file (if exists)
		if (dossierFile_ != null && Files.exists(dossierFile_)) {
			outputs.add(dossierFile_);
		}

		// return outputs if succeeded
		if (isSucceeded)
			return outputs;

		// add log file (if exists)
		if (logFile_ != null && Files.exists(logFile_)) {
			outputs.add(logFile_);
		}

		// add erreurs file (if exists)
		if (erreursFile_ != null && Files.exists(erreursFile_)) {
			outputs.add(erreursFile_);
		}

		// return outputs
		return outputs;
	}

	/**
	 * Extracts full fatigue analysis results from the output dossier file and sends analysis complete message back to the client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractFullFatigueAnalysisResults() throws Exception {

		// initialize variables
		int validity = -1, totCycles = -1;
		double fatEq = -1.0, minStress = -1.0, maxStress = -1.0, rRatio = -1.0;

		// create file reader
		try (BufferedReader reader = Files.newBufferedReader(dossierFile_, Charset.defaultCharset())) {

			// read file till the end
			String[] split;
			String line = null;
			while ((line = reader.readLine()) != null) {

				// validity
				if (line.startsWith(" : Nb flight tot")) {
					line = reader.readLine();
					line = reader.readLine();
					if (line != null) {
						split = line.split(":");
						validity = Integer.parseInt(split[1].trim());
						minStress = Double.parseDouble(split[2].trim());
						maxStress = Double.parseDouble(split[3].trim());
						rRatio = Double.parseDouble(split[6].trim());
						totCycles = Integer.parseInt(split[7].trim());
					}
				}

				// others
				else if (line.startsWith(" :  (Rain-Flow)")) {
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

		// upload produced output file (if any) to database and get download ID
		String downloadUrl = uploadOutputFiles(true);

		// create and send analysis complete message
		FullESAComplete message = new FullESAComplete();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setDownloadUrl(downloadUrl);
		message.setEquivalentStress(fatEq);
		message.setMaximumStress(maxStress);
		message.setMinimumStress(minStress);
		message.setRRatio(rRatio);
		message.setTotalNumberOfCycles(totCycles);
		message.setValidity(validity);
		client_.sendMessage(message);
	}

	/**
	 * Extracts full preffas analysis results from the output dossier file and sends analysis complete message back to the client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractFullPreffasAnalysisResults() throws Exception {

		// initialize variables
		int validity = -1, totCycles = -1;
		double cgEqPref = -1.0, minStress = -1.0, maxStress = -1.0, rRatio = -1.0;

		// create file reader
		try (BufferedReader reader = Files.newBufferedReader(dossierFile_, Charset.defaultCharset())) {

			// read file till the end
			String[] split;
			String line = null;
			while ((line = reader.readLine()) != null) {

				// validity
				if (line.startsWith(" : Nb flight tot")) {
					line = reader.readLine();
					line = reader.readLine();
					if (line != null) {
						split = line.split(":");
						validity = Integer.parseInt(split[1].trim());
						minStress = Double.parseDouble(split[2].trim());
						maxStress = Double.parseDouble(split[3].trim());
						rRatio = Double.parseDouble(split[6].trim());
						totCycles = Integer.parseInt(split[7].trim());
					}
				}

				// others
				else if (line.startsWith(" :  (Rain-Flow)")) {
					line = reader.readLine();
					line = reader.readLine();
					if (line != null) {
						split = line.split(":");
						cgEqPref = Double.parseDouble(split[7].trim());
					}
				}
			}
		}

		// no equivalent stress found
		if (cgEqPref == -1.0)
			throw new Exception("SAFE analysis failed! Cannot find preffas equivalent stress in output dossier file.");

		// upload produced output file (if any) to database and get download ID
		String downloadUrl = uploadOutputFiles(true);

		// create and send analysis complete message
		FullESAComplete message = new FullESAComplete();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setDownloadUrl(downloadUrl);
		message.setEquivalentStress(cgEqPref);
		message.setMaximumStress(maxStress);
		message.setMinimumStress(minStress);
		message.setRRatio(rRatio);
		message.setTotalNumberOfCycles(totCycles);
		message.setValidity(validity);
		client_.sendMessage(message);
	}

	/**
	 * Extracts full linear analysis results from the output dossier file and sends analysis complete message back to the client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractFullLinearAnalysisResults() throws Exception {

		// initialize variables
		int validity = -1, totCycles = -1;
		double cgLinEff = -1.0, minStress = -1.0, maxStress = -1.0, rRatio = -1.0;

		// read dossier file
		try (BufferedReader reader = Files.newBufferedReader(dossierFile_, Charset.defaultCharset())) {

			// read file till the end
			String[] split;
			String line = null;
			while ((line = reader.readLine()) != null) {

				// validity
				if (line.startsWith(" : Nb flight tot")) {
					line = reader.readLine();
					line = reader.readLine();
					if (line != null) {
						split = line.split(":");
						validity = Integer.parseInt(split[1].trim());
						minStress = Double.parseDouble(split[2].trim());
						maxStress = Double.parseDouble(split[3].trim());
						rRatio = Double.parseDouble(split[6].trim());
						totCycles = Integer.parseInt(split[7].trim());
					}
				}
			}
		}

		// read log file
		try (BufferedReader reader = Files.newBufferedReader(logFile_, Charset.defaultCharset())) {

			// read file till the end
			String[] split;
			String line = null, previousLine = null;
			while ((line = reader.readLine()) != null) {

				// others
				if (line.startsWith(" subroutine FTEQPRO:")) {
					split = previousLine.trim().split(":");
					cgLinEff = Double.parseDouble(split[1].trim());
					break;
				}

				// save previous line
				previousLine = line;
			}
		}

		// no equivalent stress found
		if (cgLinEff == -1.0)
			throw new Exception("SAFE analysis failed! Cannot find linear propagation equivalent stress in output dossier file.");

		// compute linear equivalent propagation stress
		double a = cgAcgM_[0];
		double b = 1.0 - cgAcgM_[0];
		double m = cgAcgM_[1];
		double c = 0.9 * (a + b * 0.1);
		double cgEqLin = Math.pow(cgLinEff / validity, 1.0 / m) / c;

		// upload produced output file (if any) to database and get download ID
		String downloadUrl = uploadOutputFiles(true);

		// create and send analysis complete message
		FullESAComplete message = new FullESAComplete();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setDownloadUrl(downloadUrl);
		message.setEquivalentStress(cgEqLin);
		message.setMaximumStress(maxStress);
		message.setMinimumStress(minStress);
		message.setRRatio(rRatio);
		message.setTotalNumberOfCycles(totCycles);
		message.setValidity(validity);
		client_.sendMessage(message);
	}

	/**
	 * Extracts fast fatigue analysis results from the output dossier file and sends analysis complete message back to the client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractFastFatigueAnalysisResults() throws Exception {

		// initialize variables
		double fatEq = -1.0;

		// create file reader
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

		// upload produced output file (if any) to database and get download ID
		String downloadUrl = uploadOutputFiles(true);

		// create and send analysis complete message
		FastESAComplete message = new FastESAComplete();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setDownloadUrl(downloadUrl);
		message.setEquivalentStress(fatEq);
		client_.sendMessage(message);
	}

	/**
	 * Extracts fast preffas analysis results from the output dossier file and sends analysis complete message back to the client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractFastPreffasAnalysisResults() throws Exception {

		// initialize variables
		double cgEqPref = -1.0;

		// create file reader
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
						cgEqPref = Double.parseDouble(split[7].trim());
					}
				}
			}
		}

		// no equivalent stress found
		if (cgEqPref == -1.0)
			throw new Exception("SAFE analysis failed! Cannot find preffas equivalent stress in output dossier file.");

		// upload produced output file (if any) to database and get download ID
		String downloadUrl = uploadOutputFiles(true);

		// create and send analysis complete message
		FastESAComplete message = new FastESAComplete();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setDownloadUrl(downloadUrl);
		message.setEquivalentStress(cgEqPref);
		client_.sendMessage(message);
	}

	/**
	 * Extracts fast linear propagation analysis results from the output dossier file and sends analysis complete message back to the client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractFastLinearAnalysisResults() throws Exception {

		// initialize variables
		int validity = -1;
		double cgLinEff = -1.0;

		// create file reader
		try (BufferedReader reader = Files.newBufferedReader(dossierFile_, Charset.defaultCharset())) {

			// read file till the end
			String[] split;
			String line = null;
			while ((line = reader.readLine()) != null) {

				// validity
				if (line.startsWith(" : Nb flight tot")) {
					line = reader.readLine();
					line = reader.readLine();
					if (line != null) {
						split = line.split(":");
						validity = Integer.parseInt(split[1].trim());
					}
				}
			}
		}

		// read log file
		try (BufferedReader reader = Files.newBufferedReader(logFile_, Charset.defaultCharset())) {

			// read file till the end
			String[] split;
			String line = null, previousLine = null;
			while ((line = reader.readLine()) != null) {

				// others
				if (line.startsWith(" subroutine FTEQPRO:")) {
					split = previousLine.trim().split(":");
					cgLinEff = Double.parseDouble(split[1].trim());
					break;
				}

				// save previous line
				previousLine = line;
			}
		}

		// no equivalent stress found
		if (cgLinEff == -1.0)
			throw new Exception("SAFE analysis failed! Cannot find linear propagation equivalent stress in output dossier file.");

		// compute linear equivalent propagation stress
		double a = cgAcgM_[0];
		double b = 1.0 - cgAcgM_[0];
		double m = cgAcgM_[1];
		double c = 0.9 * (a + b * 0.1);
		double cgEqLin = Math.pow(cgLinEff / validity, 1.0 / m) / c;

		// upload produced output file (if any) to database and get download ID
		String downloadUrl = uploadOutputFiles(true);

		// create and send analysis complete message
		FastESAComplete message = new FastESAComplete();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setDownloadUrl(downloadUrl);
		message.setEquivalentStress(cgEqLin);
		client_.sendMessage(message);
	}
}
