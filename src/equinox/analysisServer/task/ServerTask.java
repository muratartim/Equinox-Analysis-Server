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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.jcraft.jsch.JSchException;

import equinox.analysisServer.server.AnalysisServer;
import equinox.analysisServer.utility.Utility;
import equinox.serverUtilities.FilerConnection;

/**
 * Abstract class for server task.
 *
 * @author Murat Artim
 * @date 30 Mar 2017
 * @time 16:49:36
 */
public abstract class ServerTask implements Runnable {

	/** Working directory. */
	private Path workingDirectory_ = null;

	/** Server instance. */
	protected final AnalysisServer server_;

	/**
	 * Creates server task.
	 *
	 * @param server
	 *            Server instance.
	 */
	public ServerTask(AnalysisServer server) {
		server_ = server;
	}

	@Override
	public void run() {

		// execute and return result of task
		try {
			runTask();
		}

		// exception occurred during execution
		catch (Exception e) {
			failed(e);
		}

		// clean up
		finally {
			deleteTemporaryFiles();
		}
	}

	/**
	 * Runs this task and returns the result.
	 *
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	protected abstract void runTask() throws Exception;

	/**
	 * Called if the task execution fails.
	 *
	 * @param e
	 *            Exception occurred.
	 */
	protected abstract void failed(Exception e);

	/**
	 * Returns a list of temporary files to delete, or null if no temporary file was produced.
	 *
	 * @return A list of temporary files to delete, or null if no temporary file was produced.
	 */
	protected List<Path> getTemporaryFiles() {
		if (workingDirectory_ != null && Files.exists(workingDirectory_))
			return Arrays.asList(workingDirectory_);
		return null;
	}

	/**
	 * Creates and returns working directory (if not already exists).
	 *
	 * @return Working directory.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	protected Path getWorkingDirectory() throws Exception {
		if (workingDirectory_ == null) {
			workingDirectory_ = Utility.createWorkingDirectory(getClass().getSimpleName());
		}
		return workingDirectory_;
	}

	/**
	 * Creates and returns connection to SFTP filer server. Note that, the supplied session, channel and sftpChannel objects must be disconnected after usage.
	 *
	 * @return Newly created filer connection.
	 * @throws JSchException
	 *             If exception occurs during process.
	 */
	protected FilerConnection getFilerConnection() throws JSchException {
		return Utility.createFilerConnection(server_.getLogger());
	}

	/**
	 * Deletes all temporary files after the task is complete.
	 */
	private void deleteTemporaryFiles() {

		// don't delete temporary files
		if (server_.getProperties().getProperty("temp.delete").equals("no"))
			return;

		// no temporary file was produced
		List<Path> tempFiles = getTemporaryFiles();
		if (tempFiles == null || tempFiles.isEmpty())
			return;

		// delete temporary files
		server_.getThreadPool().submit(new DeleteTemporaryFiles(server_, tempFiles));
	}
}