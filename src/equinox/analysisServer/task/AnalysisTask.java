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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;

import equinox.analysisServer.client.AnalysisClient;
import equinox.analysisServer.remote.message.AnalysisFailed;
import equinox.analysisServer.remote.message.AnalysisProgress;
import equinox.analysisServer.remote.message.AnalysisRequest;
import equinox.analysisServer.server.AnalysisServer;
import equinox.analysisServer.utility.Utility;
import equinox.serverUtilities.FilerConnection;

/**
 * Abstract class for analysis task.
 *
 * @author Murat Artim
 * @date 31 Mar 2017
 * @time 12:51:25
 */
public abstract class AnalysisTask extends ServerTask {

	/** Client who requests the analysis. */
	protected final AnalysisClient client_;

	/** Analysis request message. */
	protected final AnalysisRequest request_;

	/**
	 * Creates analysis task.
	 *
	 * @param server
	 *            Server instance.
	 * @param client
	 *            Requesting client.
	 * @param request
	 *            Analysis request message.
	 */
	public AnalysisTask(AnalysisServer server, AnalysisClient client, AnalysisRequest request) {

		// create server task
		super(server);

		// set attributes
		client_ = client;
		request_ = request;
	}

	@Override
	protected void failed(Exception e) {

		try {

			// increment failed analysis count for server statistics
			server_.incrementFailedAnalyses();

			// log exception
			server_.getLogger().log(Level.WARNING, "Analysis failed for client '" + client_.getAlias() + "'.", e);

			// no client
			if (client_ == null)
				return;

			// upload produced output files (if any) to database and get download URL
			String downloadUrl = uploadOutputFiles(false);

			// send analysis failed message to client
			AnalysisFailed message = new AnalysisFailed();
			message.setListenerHashCode(request_.getListenerHashCode());
			message.setException(e);
			message.setDownloadUrl(downloadUrl);
			client_.sendMessage(message);
		}

		// exception occurred during process
		catch (Exception e1) {

			// log exception
			server_.getLogger().log(Level.WARNING, "Exception occurred during processing failed analysis for client '" + client_.getAlias() + "'.", e1);

			// send analysis failed message to client without download ID
			AnalysisFailed message = new AnalysisFailed();
			message.setListenerHashCode(request_.getListenerHashCode());
			message.setException(e1);
			client_.sendMessage(message);
		}
	}

	/**
	 * Sends progress message to client.
	 *
	 * @param progressMessage
	 *            Message text.
	 */
	protected void sendProgressMessage(String progressMessage) {
		AnalysisProgress message = new AnalysisProgress();
		message.setListenerHashCode(request_.getListenerHashCode());
		message.setProgressMessage(progressMessage);
		client_.sendMessage(message);
	}

	/**
	 * Downloads input file from central database.
	 *
	 * @return Input data file.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	protected Path downloadInputFile() throws Exception {

		// create path to input file
		Path input = getWorkingDirectory().resolve("inputs.zip");

		// download from server
		try (FilerConnection filer = getFilerConnection()) {
			filer.getSftpChannel().get(request_.getDownloadUrl(), input.toString());
		}

		// return input
		return input;
	}

	/**
	 * Uploads analysis output file to central database and returns download URL, or <code>null</code> if no output file was uploaded or produced.
	 *
	 * @param isSucceeded
	 *            True if task is successfully completed.
	 * @return Analysis output file download URL, or <code>null</code> if no output file was uploaded or produced.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	protected String uploadOutputFiles(boolean isSucceeded) throws Exception {

		// don't upload output files
		if (!request_.getUploadOutputFiles())
			return null;

		// get output files
		ArrayList<Path> outputs = getOutputFiles(isSucceeded);

		// no output files found
		if (outputs == null || outputs.isEmpty())
			return null;

		// zip files
		sendProgressMessage("Zipping output files...");
		Path zipFile = getWorkingDirectory().resolve("outputs.zip");
		Utility.zipFiles(outputs, zipFile.toFile());

		// upload output file
		sendProgressMessage("Uploading output files to central database...");
		return uploadFile(zipFile);
	}

	/**
	 * Returns a list of output files.
	 *
	 * @param isSucceeded
	 *            True if task is successfully completed.
	 * @return A list of output files.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	protected abstract ArrayList<Path> getOutputFiles(boolean isSucceeded) throws Exception;

	/**
	 * Uploads given output file to central database.
	 *
	 * @param path
	 *            Path to output file.
	 * @return Download URL.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private String uploadFile(Path path) throws Exception {

		// initialize download URL
		String downloadUrl = null;

		// get filer connection
		try (FilerConnection filer = getFilerConnection()) {

			// set path to destination file
			downloadUrl = filer.getDirectoryPath(FilerConnection.EXCHANGE) + "/" + client_.getAlias() + "_" + this.getClass().getSimpleName() + "_" + System.currentTimeMillis() + ".zip";

			// upload file to filer
			filer.getSftpChannel().put(path.toString(), downloadUrl);
		}

		// return download URL
		return downloadUrl;
	}
}
