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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.remote.message.FastESAComplete;
import equinox.analysisServer.remote.message.FullESAComplete;
import equinox.analysisServer.remote.message.IsamiESARequest;
import equinox.analysisServer.server.AnalysisServer;
import equinox.analysisServer.utility.Utility;

/**
 * Class for ISAMI equivalent stress analysis task.
 *
 * @author Murat Artim
 * @date 8 May 2017
 * @time 13:43:50
 *
 */
public final class IsamiESA extends IsamiAnalysis {

	/**
	 * Creates ISAMI equivalent stress analysis task.
	 *
	 * @param server
	 *            Server instance.
	 * @param client
	 *            Requesting client.
	 * @param request
	 *            Request message.
	 */
	public IsamiESA(AnalysisServer server, AnalysisClient client, IsamiESARequest request) {
		super(server, client, request);
	}

	@Override
	protected void runTask() throws Exception {

		// no client
		if (client_ == null)
			throw new Exception("No connected client found for analysis requester username.");

		// create analysis directory
		sendProgressMessage("Creating working directory...");
		Path workingDirectory = getWorkingDirectory();

		// download input files from server
		sendProgressMessage("Downloading input archive from database...");
		Path inputArchive = downloadInputFile();

		// extract input files from archive
		sendProgressMessage("Extracting input SIGMA file...");
		Utility.extractAllFilesFromZIP(inputArchive, workingDirectory);

		// input file doesn't exist
		Path sigmaFile = workingDirectory.resolve("input.sigma");
		if (!Files.exists(sigmaFile))
			throw new Exception("Cannot find input SIGMA file.");

		// create ISAMI run script file
		sendProgressMessage("Creating ISAMI run script file...");
		Path runScriptFile = createRunScriptFile(sigmaFile);

		// create ISAMI configuration file
		sendProgressMessage("Creating ISAMI run configuration file...");
		createConfigFile(runScriptFile);

		// run analysis
		sendProgressMessage("Analysis running...");
		runAnalysis();

		// extract results from CSV file
		sendProgressMessage("Extracting analysis results...");
		IsamiESARequest request = (IsamiESARequest) request_;
		boolean isFast = request.getFastAnalysis();
		int analysisType = request.getAnalysisType();

		// fast analysis
		if (isFast) {

			// fatigue
			if (analysisType == IsamiESARequest.FATIGUE) {
				extractFastFatigueAnalysisResults();
			}

			// preffas
			else if (analysisType == IsamiESARequest.PREFFAS || analysisType == IsamiESARequest.LINEAR) {
				extractFastPropagationAnalysisResults();
			}
		}

		// full analysis
		else {

			// fatigue
			if (analysisType == IsamiESARequest.FATIGUE) {
				extractFullFatigueAnalysisResults();
			}

			// preffas
			else if (analysisType == IsamiESARequest.PREFFAS || analysisType == IsamiESARequest.LINEAR) {
				extractFullPropagationAnalysisResults();
			}
		}
	}

	@Override
	protected ArrayList<Path> getOutputFiles(boolean isSucceeded) throws Exception {

		// create list to store output files
		ArrayList<Path> outputs = new ArrayList<>();

		// add html file (if exists)
		if (htmlFile_ != null && Files.exists(htmlFile_)) {
			outputs.add(htmlFile_);
		}

		// return outputs if succeeded
		if (isSucceeded)
			return outputs;

		// add log file (if exists)
		if (logFile_ != null && Files.exists(logFile_)) {
			outputs.add(logFile_);
		}

		// add out file (if exists)
		if (outFile_ != null && Files.exists(outFile_)) {
			outputs.add(outFile_);
		}

		// add csv file (if exists)
		if (csvFile_ != null && Files.exists(csvFile_)) {
			outputs.add(csvFile_);
		}

		// return outputs
		return outputs;
	}

	/**
	 * Extracts full fatigue analysis results from the output HTML file and sends analysis complete message back to the client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractFullFatigueAnalysisResults() throws Exception {

		// initialize variables
		int validity = -1, totCycles = -1;
		double fatEq = -1.0, minStress = -1.0, maxStress = -1.0, rRatio = -1.0;

		// create file reader
		try (BufferedReader reader = Files.newBufferedReader(htmlFile_, Charset.defaultCharset())) {

			// read file till the end
			String line = null;
			end: while ((line = reader.readLine()) != null) {

				// rainflow info
				if (line.contains("Total Number of Flights:")) {

					// parse validity
					validity = Integer.parseInt(line.split(":")[1].trim().split("<BR>")[0].trim());

					// parse rainflow information
					while ((line = reader.readLine()) != null) {
						if (line.contains("Values")) {
							String[] split = line.split("</TD><TD>");
							minStress = Double.parseDouble(split[0].trim().split("</TH><TD>")[1].trim());
							maxStress = Double.parseDouble(split[1].trim());
							rRatio = Double.parseDouble(split[4].trim());
							totCycles = Integer.parseInt(split[5].trim().split("</TD></TR>")[0].trim());
							break;
						}
					}
				}

				// equivalent stress
				else if (line.contains("FATIGUE RESULTS:")) {
					while ((line = reader.readLine()) != null) {
						if (line.contains("EQUIVALENT STRESS")) {
							fatEq = Double.parseDouble(line.split("</TH><TD ALIGN=CENTER>")[1].trim().split("</TD></TR>")[0].trim());
							break end;
						}
					}
				}
			}
		}

		// no equivalent stress found
		if (fatEq == -1.0)
			throw new Exception("ISAMI analysis failed! Cannot find fatigue equivalent stress in output HTML file.");

		// upload produced output file (if any) to database and get download URL
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
	 * Extracts full propagation analysis results from the output HTML file and sends analysis complete message back to the client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractFullPropagationAnalysisResults() throws Exception {

		// initialize variables
		int validity = -1, totCycles = -1;
		double eqStress = -1.0, minStress = -1.0, maxStress = -1.0, rRatio = -1.0;

		// create file reader
		try (BufferedReader reader = Files.newBufferedReader(htmlFile_, Charset.defaultCharset())) {

			// read file till the end
			String line = null;
			end: while ((line = reader.readLine()) != null) {

				// rainflow info
				if (line.contains("Total Number of Flights:")) {

					// parse validity
					validity = Integer.parseInt(line.split(":")[1].trim().split("<BR>")[0].trim());

					// parse rainflow information
					while ((line = reader.readLine()) != null) {
						if (line.contains("Values")) {
							String[] split = line.split("</TD><TD>");
							minStress = Double.parseDouble(split[0].trim().split("</TH><TD>")[1].trim());
							maxStress = Double.parseDouble(split[1].trim());
							rRatio = Double.parseDouble(split[4].trim());
							totCycles = Integer.parseInt(split[5].trim().split("</TD></TR>")[0].trim());
							break;
						}
					}
				}

				// equivalent stress
				else if (line.contains("MISSION PROPAGATION RESULTS")) {
					while ((line = reader.readLine()) != null) {
						if (line.contains("EQUIVALENT STRESS")) {
							eqStress = Double.parseDouble(line.split("</TH><TD ALIGN=CENTER>")[1].trim().split("</TD></TR>")[0].trim());
							break end;
						}
					}
				}
			}
		}

		// no equivalent stress found
		if (eqStress == -1.0)
			throw new Exception("ISAMI analysis failed! Cannot find propagation equivalent stress in output HTML file.");

		// upload produced output file (if any) to database and get download ID
		String downloadUrl = uploadOutputFiles(true);

		// create and send analysis complete message
		FullESAComplete message = new FullESAComplete();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setDownloadUrl(downloadUrl);
		message.setEquivalentStress(eqStress);
		message.setMaximumStress(maxStress);
		message.setMinimumStress(minStress);
		message.setRRatio(rRatio);
		message.setTotalNumberOfCycles(totCycles);
		message.setValidity(validity);
		client_.sendMessage(message);
	}

	/**
	 * Extracts fast fatigue analysis results from the output CSV file and sends analysis complete message back to the client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractFastFatigueAnalysisResults() throws Exception {

		// initialize variables
		double eqStress = -1.0;

		// create file reader
		try (BufferedReader reader = Files.newBufferedReader(csvFile_, Charset.defaultCharset())) {

			// read till fatigue results found
			String[] split;
			String line = null;
			found: while ((line = reader.readLine()) != null) {

				// fatigue results found
				if (line.startsWith("FATIGUE INITIATION RESULTS")) {

					// read till equivalent stress found
					while ((line = reader.readLine()) != null) {

						// equivalent stress found
						if (line.contains("Equivalent stress")) {
							split = line.trim().split(";");
							eqStress = Double.parseDouble(split[1].trim());
							break found;
						}
					}
				}
			}
		}

		// no equivalent stress found
		if (eqStress == -1.0)
			throw new Exception("ISAMI analysis failed! Cannot find fatigue equivalent stress in output csv file.");

		// upload produced output file (if any) to database and get download URL
		String downloadUrl = uploadOutputFiles(true);

		// create and send analysis complete message
		FastESAComplete message = new FastESAComplete();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setDownloadUrl(downloadUrl);
		message.setEquivalentStress(eqStress);
		client_.sendMessage(message);
	}

	/**
	 * Extracts fast propagation analysis results from the output CSV file and sends analysis complete message back to the client.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void extractFastPropagationAnalysisResults() throws Exception {

		// initialize variables
		double eqStress = -1.0;

		// create file reader
		try (BufferedReader reader = Files.newBufferedReader(csvFile_, Charset.defaultCharset())) {

			// read till fatigue results found
			String[] split;
			String line = null;
			found: while ((line = reader.readLine()) != null) {

				// fatigue results found
				if (line.startsWith("PROPAGATION RESULTS")) {

					// read till equivalent stress found
					while ((line = reader.readLine()) != null) {

						// equivalent stress found
						if (line.contains("Equivalent stress")) {
							split = line.trim().split(";");
							eqStress = Double.parseDouble(split[1].trim());
							break found;
						}
					}
				}
			}
		}

		// no equivalent stress found
		if (eqStress == -1.0)
			throw new Exception("ISAMI analysis failed! Cannot find propagation equivalent stress in output csv file.");

		// upload produced output file (if any) to database and get download ID
		String downloadUrl = uploadOutputFiles(true);

		// create and send analysis complete message
		FastESAComplete message = new FastESAComplete();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setDownloadUrl(downloadUrl);
		message.setEquivalentStress(eqStress);
		client_.sendMessage(message);
	}

	/**
	 * Creates ISAMI run script file.
	 *
	 * @param sigmaFile
	 *            Path to input sigma file.
	 * @return Path to the newly created ISAMI run script file.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private Path createRunScriptFile(Path sigmaFile) throws Exception {

		// create path to run script file
		Path runScriptFile = getWorkingDirectory().resolve("isamiRunScript.py");

		// get analysis request message
		IsamiESARequest request = (IsamiESARequest) request_;

		// fatigue analysis
		if (request.getAnalysisType() == IsamiESARequest.FATIGUE) {
			createRunScriptFileForFatigue(sigmaFile, runScriptFile, request);
		}

		// propagation analysis
		else {
			createRunScriptFileForPropagation(sigmaFile, runScriptFile, request);
		}

		// return run script file
		return runScriptFile;
	}

	/**
	 * Creates ISAMI run script file for fatigue analysis.
	 *
	 * @param sigmaFile
	 *            Path to input sigma file.
	 * @param outputFile
	 *            Path to output script file.
	 * @param request
	 *            Analysis request message.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void createRunScriptFileForFatigue(Path sigmaFile, Path outputFile, IsamiESARequest request) throws Exception {

		// get path to default run script file
		Path defaultRunScriptFile = Paths.get("resources/isamiRunScriptForFatigue.py");

		// create file writer
		try (BufferedWriter writer = Files.newBufferedWriter(outputFile, Charset.defaultCharset())) {

			// create file reader
			try (BufferedReader reader = Files.newBufferedReader(defaultRunScriptFile, Charset.defaultCharset())) {

				// read default material file till the end
				String line = null;
				while ((line = reader.readLine()) != null) {

					// material creation
					if (line.startsWith("MySession.LoadMaterial")) {
						line = "MySession.LoadMaterial('materialName','";
						line += request.getMaterial().getName() + "','";
						line += request.getMaterial().getSpecification() + "','";
						line += "Referenced')";
					}

					// step parameters
					else if (line.contains("StepProcessParameter")) {
						line = "   ['/CsmMbr_MapProcessParameterSet/CsmMbr_ProcessParameterMap[CaesamStd_StepProcessParameter]/Execute', 'BA:TRUE:4::";
						line += "TRUE;TRUE;FALSE;FALSE'], # Check Validity; Initiation; Propagation; Residual Strength";
					}

					// sigma file path
					else if (line.contains("Caesam_Url:file:")) {
						line = "   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING/SpectrumUrl/Url','Caesam_Url:file:";
						line += sigmaFile.toAbsolutePath().toString() + "'], # Sigma file path";
					}

					// material orientation
					else if (line.contains("Orientation_init")) {
						line = "   ['EO[FatigueLaw]/Orientation_init','Enum_Orientation:" + request.getMaterial().getOrientation() + "'], # Damage law orientation (LS/LT/SL/TL/TS)";
					}

					// material configuration
					else if (line.contains("Configuration_init")) {
						line = "   ['EO[FatigueLaw]/Configuration_init','S:Configuration:" + request.getMaterial().getConfiguration() + "'], # Damage law configuration or failure mode";
					}

					// save session
					else if (line.startsWith("MySession.Save")) {
						String pathToCZMFile = getWorkingDirectory().resolve("input.czm").toAbsolutePath().toString();
						line = "MySession.Save('" + pathToCZMFile + "')";
					}

					// write
					writer.write(line);
					writer.newLine();
				}
			}
		}
	}

	/**
	 * Creates ISAMI run script file for propagation analysis.
	 *
	 * @param sigmaFile
	 *            Path to input sigma file.
	 * @param outputFile
	 *            Path to output script file.
	 * @param request
	 *            Analysis request message.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private void createRunScriptFileForPropagation(Path sigmaFile, Path outputFile, IsamiESARequest request) throws Exception {

		// get path to default run script file
		Path defaultRunScriptFile = Paths.get("resources/isamiRunScriptForPropagation.py");

		// create file writer
		try (BufferedWriter writer = Files.newBufferedWriter(outputFile, Charset.defaultCharset())) {

			// create file reader
			try (BufferedReader reader = Files.newBufferedReader(defaultRunScriptFile, Charset.defaultCharset())) {

				// read default material file till the end
				String line = null;
				while ((line = reader.readLine()) != null) {

					// material creation
					if (line.startsWith("MySession.LoadMaterial")) {
						line = "MySession.LoadMaterial('materialName','";
						line += request.getMaterial().getName() + "','";
						line += request.getMaterial().getSpecification() + "','";
						line += "Referenced')";
					}

					// step parameters
					else if (line.contains("StepProcessParameter")) {
						line = "   ['/CsmMbr_MapProcessParameterSet/CsmMbr_ProcessParameterMap[CaesamStd_StepProcessParameter]/Execute', 'BA:TRUE:4::";
						line += "TRUE;FALSE;TRUE;FALSE'], # Check Validity; Initiation; Propagation; Residual Strength";
					}

					// sigma file path
					else if (line.contains("Caesam_Url:file:")) {
						line = "   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING/SpectrumUrl/Url','Caesam_Url:file:";
						line += sigmaFile.toAbsolutePath().toString() + "'], # Sigma file path";
					}

					// material orientation
					else if (line.contains("Orientation_propa")) {
						line = "   ['EO[FatigueLaw]/Orientation_propa','Enum_Orientation:" + request.getMaterial().getOrientation() + "'], # Propagation law orientation (LS/LT/SL/TL/TS)";
					}

					// material configuration
					else if (line.contains("Configuration_propa")) {
						line = "   ['EO[FatigueLaw]/Configuration_propa','S:Configuration:" + request.getMaterial().getConfiguration() + "'], # Propagation law configuration";
					}

					// retardation model
					else if (line.contains("RetardationModel")) {
						line = "   ['EO[FatigueLaw]/RetardationModel','Enum_RetardationModel:";
						line += request.getAnalysisType() == IsamiESARequest.PREFFAS ? "Preffas" : "None";
						line += "'], # Retardation model";
					}

					// spectrum compression
					else if (line.contains("ConsideredCompression")) {
						line = "   ['EO[FatigueLaw]/ConsideredCompression','CaesamEnum_YesNo:";
						line += request.getApplyCompression() ? "Yes" : "No";
						line += "'], # Considered compression";
					}

					// save session
					else if (line.startsWith("MySession.Save")) {
						String pathToCZMFile = getWorkingDirectory().resolve("input.czm").toAbsolutePath().toString();
						line = "MySession.Save('" + pathToCZMFile + "')";
					}

					// write
					writer.write(line);
					writer.newLine();
				}
			}
		}
	}

	/**
	 * Creates ISAMI run configuration file.
	 *
	 * @param isamiRunScript
	 *            Path to ISAMI run script.
	 * @return Path to the newly created ISAMI run configuration file.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private Path createConfigFile(Path isamiRunScript) throws Exception {

		// get path to default configuration file
		Path defaultConfigFile = Paths.get("resources/isamiConfigFile.txt");

		// create path to configuration file
		Path configFile = getWorkingDirectory().resolve(defaultConfigFile.getFileName().toString());

		// get analysis request message
		IsamiESARequest request = (IsamiESARequest) request_;

		// create file writer
		try (BufferedWriter writer = Files.newBufferedWriter(configFile, Charset.defaultCharset())) {

			// create file reader
			try (BufferedReader reader = Files.newBufferedReader(defaultConfigFile, Charset.defaultCharset())) {

				// read default material file till the end
				String line = null;
				while ((line = reader.readLine()) != null) {

					// set ISAMI version
					if (line.startsWith("ISAMI_VERSION")) {
						line = "ISAMI_VERSION " + request.getIsamiVersion();
					}

					// set ISAMI sub-version
					else if (line.startsWith("LAUNCHER_PARAMETER")) {
						line = "LAUNCHER_PARAMETER -application " + request.getIsamiSubVersion();
					}

					// set job name
					else if (line.startsWith("JOB_NAME")) {
						line = "JOB_NAME " + getWorkingDirectory().getFileName().toString();
					}

					// set results directory
					else if (line.startsWith("DATA_HOST_RESULT_DIR")) {
						line = "DATA_HOST_RESULT_DIR " + getWorkingDirectory().toAbsolutePath().toString() + File.separator;
					}

					// set path to ISAMI run script
					else if (line.startsWith("INCLUDE")) {
						line = "INCLUDE " + isamiRunScript.toAbsolutePath().toString();
					}

					// execution command
					else if (line.startsWith("RUN")) {
						line = "RUN " + isamiRunScript.toAbsolutePath().toString();
					}

					// write
					writer.write(line);
					writer.newLine();
				}
			}
		}

		// return configuration file
		return configFile;
	}
}
