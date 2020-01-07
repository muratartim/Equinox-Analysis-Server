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
 * Class for SAFE equivalent stress analysis request network message.
 *
 * @author Murat Artim
 * @date 30 Mar 2017
 * @time 18:14:25
 *
 */
public final class SafeESARequest extends AnalysisRequest {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** Equivalent stress analysis type. */
	public static final int FATIGUE = 0, PREFFAS = 1, LINEAR = 2;

	/** Equivalent stress analysis type. */
	private int analysisType_;

	/** True if fast analysis is requested. */
	private boolean isFastAnalysis_;

	/**
	 * No argument constructor for serialization.
	 */
	public SafeESARequest() {
	}

	/**
	 * Sets equivalent stress analysis type.
	 *
	 * @param analysisType
	 *            Equivalent stress analysis type.
	 */
	public void setAnalysisType(int analysisType) {
		analysisType_ = analysisType;
	}

	/**
	 * Sets whether fast analysis is requested or not.
	 *
	 * @param isFastAnalysis
	 *            True if fast analysis is requested.
	 */
	public void setFastAnalysis(boolean isFastAnalysis) {
		isFastAnalysis_ = isFastAnalysis;
	}

	/**
	 * Returns equivalent stress analysis type.
	 *
	 * @return Equivalent stress analysis type.
	 */
	public int getAnalysisType() {
		return analysisType_;
	}

	/**
	 * Returns true if fast analysis is requested.
	 *
	 * @return True if fast analysis is requested.
	 */
	public boolean getFastAnalysis() {
		return isFastAnalysis_;
	}
}
