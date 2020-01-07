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
 * Class for full equivalent stress analysis complete message.
 *
 * @author Murat Artim
 * @date 3 Apr 2017
 * @time 11:27:37
 *
 */
public final class FullESAComplete extends ESAComplete {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** Integer results. */
	private int validity_, totCycles_;

	/** Double results. */
	private double minStress_, maxStress_, rRatio_;

	/**
	 * No argument constructor for serialization.
	 */
	public FullESAComplete() {
	}

	/**
	 * Sets spectrum validity.
	 *
	 * @param validity
	 *            Spectrum validity.
	 */
	public void setValidity(int validity) {
		validity_ = validity;
	}

	/**
	 * Sets total number of cycles.
	 *
	 * @param totCycles
	 *            Total number of cycles.
	 */
	public void setTotalNumberOfCycles(int totCycles) {
		totCycles_ = totCycles;
	}

	/**
	 * Sets minimum stress level.
	 *
	 * @param minStress
	 *            Minimum stress level.
	 */
	public void setMinimumStress(double minStress) {
		minStress_ = minStress;
	}

	/**
	 * Sets maximum stress level.
	 *
	 * @param maxStress
	 *            Maximum stress level.
	 */
	public void setMaximumStress(double maxStress) {
		maxStress_ = maxStress;
	}

	/**
	 * Sets r-ratio.
	 *
	 * @param rRatio
	 *            R-ratio.
	 */
	public void setRRatio(double rRatio) {
		rRatio_ = rRatio;
	}

	/**
	 * Returns spectrum validity.
	 *
	 * @return Spectrum validity.
	 */
	public int getValidity() {
		return validity_;
	}

	/**
	 * Returns total number of cycles.
	 *
	 * @return Total number of cycles.
	 */
	public int getTotalNumberOfCycles() {
		return totCycles_;
	}

	/**
	 * Returns minimum stress level.
	 *
	 * @return Minimum stress level.
	 */
	public double getMinimumStress() {
		return minStress_;
	}

	/**
	 * Returns maximum stress level.
	 *
	 * @return Maximum stress level.
	 */
	public double getMaximumStress() {
		return maxStress_;
	}

	/**
	 * Returns r-ratio.
	 *
	 * @return R-ratio.
	 */
	public double getRRatio() {
		return rRatio_;
	}
}
