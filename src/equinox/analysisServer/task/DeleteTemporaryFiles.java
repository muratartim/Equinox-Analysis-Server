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

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import equinox.analysisServer.server.AnalysisServer;

/**
 * Class for delete temporary files task.
 *
 * @author Murat Artim
 * @date 5 Apr 2017
 * @time 10:24:49
 *
 */
public final class DeleteTemporaryFiles extends ServerTask {

	/** Files to delete. */
	private final List<Path> files_;

	/**
	 * Creates delete temporary files task.
	 *
	 * @param server
	 *            Server instance.
	 * @param files
	 *            Temporary files to delete.
	 */
	public DeleteTemporaryFiles(AnalysisServer server, List<Path> files) {

		// create server task
		super(server);

		// set files to delete
		files_ = files;
	}

	@Override
	protected void runTask() throws Exception {

		// no files to delete
		if (files_ == null)
			return;

		// loop over files
		for (Path file : files_)
			if (file != null) {
				deleteFile(file);
			}
	}

	@Override
	protected void failed(Exception e) {
		server_.getLogger().log(Level.WARNING, "Exception occurred during deleting temporary files.", e);
	}

	/**
	 * Deletes file recursively.
	 *
	 * @param file
	 *            Path to file to delete.
	 * @throws IOException
	 *             If exception occurs during process.
	 */
	private void deleteFile(Path file) throws IOException {

		// directory
		if (Files.isDirectory(file)) {

			// create directory stream
			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(file)) {

				// get iterator
				Iterator<Path> iterator = dirStream.iterator();

				// loop over files
				while (iterator.hasNext()) {
					deleteFile(iterator.next());
				}
			}

			// delete directory (if not excluded)
			try {
				Files.delete(file);
			}

			// directory not empty
			catch (DirectoryNotEmptyException e) {
				// ignore
			}
		}
		else {
			Files.delete(file);
		}
	}
}
