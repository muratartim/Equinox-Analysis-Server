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
package equinox.analysisServer.remote.message;

/**
 * Abstract class for analysis request message.
 *
 * @author Murat Artim
 * @date 4 Apr 2017
 * @time 10:51:54
 */
public abstract class AnalysisRequest extends AnalysisMessage {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** URL to input file. */
	private String downloadUrl_;

	/** True if output files should be uploaded at the end of the analysis. */
	private boolean uploadOutputFiles_ = false;

	/**
	 * Sets input file download URL to this message.
	 *
	 * @param downloadUrl
	 *            URL to input file.
	 */
	public void setDownloadUrl(String downloadUrl) {
		downloadUrl_ = downloadUrl;
	}

	/**
	 * Sets whether the output files should be uploaded after the analysis is completed.
	 *
	 * @param uploadOutputFiles
	 *            True to upload the output files.
	 */
	public void setUploadOutputFiles(boolean uploadOutputFiles) {
		uploadOutputFiles_ = uploadOutputFiles;
	}

	/**
	 * Returns the input file download URL.
	 *
	 * @return Input file download URL.
	 */
	public String getDownloadUrl() {
		return downloadUrl_;
	}

	/**
	 * Returns true if output files should be uploaded after the analysis is completed.
	 *
	 * @return True if output files should be uploaded after the analysis is completed.
	 */
	public boolean getUploadOutputFiles() {
		return uploadOutputFiles_;
	}
}