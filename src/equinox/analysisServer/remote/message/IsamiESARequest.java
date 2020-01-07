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

import equinox.analysisServer.remote.data.IsamiMaterial;

/**
 * Class for ISAMI equivalent stress analysis request network message.
 *
 * @author Murat Artim
 * @date 8 May 2017
 * @time 12:22:30
 *
 */
public final class IsamiESARequest extends AnalysisRequest {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** Equivalent stress analysis type. */
	public static final int FATIGUE = 0, PREFFAS = 1, LINEAR = 2;

	/** Equivalent stress analysis type. */
	private int analysisType_;

	/** True if fast analysis is requested. */
	private boolean isFastAnalysis_, applyCompression_;

	/** ISAMI versions. */
	private String isamiVersion_, isamiSubVersion_;

	/** Material. */
	private IsamiMaterial material_;

	/**
	 * No argument constructor for serialization.
	 */
	public IsamiESARequest() {
	}

	/**
	 * Sets ISAMI version.
	 *
	 * @param isamiVersion
	 *            ISAMI version.
	 */
	public void setIsamiVersion(String isamiVersion) {
		isamiVersion_ = isamiVersion;
	}

	/**
	 * Sets ISAMI sub-version.
	 *
	 * @param isamiSubVersion
	 *            ISAMI sub-version.
	 */
	public void setIsamiSubVersion(String isamiSubVersion) {
		isamiSubVersion_ = isamiSubVersion;
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
	 * Sets whether compression should be applied for propagation analyses.
	 *
	 * @param applyCompression
	 *            True to apply compression for propagation analyses.
	 */
	public void setApplyCompression(boolean applyCompression) {
		applyCompression_ = applyCompression;
	}

	/**
	 * Sets material.
	 *
	 * @param material
	 *            Material to set.
	 */
	public void setMaterial(IsamiMaterial material) {
		material_ = material;
	}

	/**
	 * Returns ISAMI version to be used.
	 *
	 * @return ISAMI version to be used.
	 */
	public String getIsamiVersion() {
		return isamiVersion_;
	}

	/**
	 * Returns ISAMI sub-version to be used.
	 *
	 * @return ISAMI sub-version to be used.
	 */
	public String getIsamiSubVersion() {
		return isamiSubVersion_;
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

	/**
	 * Returns true if compression should be applied for propagation analyses.
	 *
	 * @return True if compression should be applied for propagation analyses.
	 */
	public boolean getApplyCompression() {
		return applyCompression_;
	}

	/**
	 * Returns material.
	 *
	 * @return Material.
	 */
	public IsamiMaterial getMaterial() {
		return material_;
	}
}