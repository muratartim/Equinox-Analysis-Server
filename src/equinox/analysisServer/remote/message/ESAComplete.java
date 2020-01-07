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
 * Abstract class for equivalent stress analysis complete network message.
 *
 * @author Murat Artim
 * @date 3 May 2017
 * @time 16:54:09
 *
 */
public abstract class ESAComplete extends AnalysisComplete {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** Equivalent stress value. */
	private double equivalentStress_;

	/** Analysis output file download URL. */
	private String downloadUrl_;

	/**
	 * Sets analysis output file download URL.
	 *
	 * @param downloadUrl
	 *            Analysis output file download URL or <code>null</code> if output files are not uploaded.
	 */
	public void setDownloadUrl(String downloadUrl) {
		downloadUrl_ = downloadUrl;
	}

	/**
	 * Sets equivalent stress.
	 *
	 * @param equivalentStress
	 *            Equivalent stress.
	 */
	public void setEquivalentStress(double equivalentStress) {
		equivalentStress_ = equivalentStress;
	}

	/**
	 * Returns analysis output file download URL or <code>null</code> if output files are not uploaded.
	 *
	 * @return Analysis output file download URL or <code>null</code> if output files are not uploaded.
	 */
	public String getDownloadUrl() {
		return downloadUrl_;
	}

	/**
	 * Returns equivalent stress.
	 *
	 * @return Equivalent stress.
	 */
	public double getEquivalentStress() {
		return equivalentStress_;
	}
}
