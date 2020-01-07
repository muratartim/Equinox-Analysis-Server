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
 * Class for damage contribution analysis increment complete network message.
 *
 * @author Murat Artim
 * @date 5 May 2017
 * @time 10:27:23
 *
 */
public final class DCAIncrementComplete extends AnalysisComplete {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** Fatigue damage and equivalent stress. */
	private Double damage_ = null, stress_ = null;

	/**
	 * No argument constructor for serialization.
	 */
	public DCAIncrementComplete() {
	}

	/**
	 * Sets fatigue damage.
	 *
	 * @param damage
	 *            Damage.
	 */
	public void setDamage(Double damage) {
		damage_ = damage;
	}

	/**
	 * Sets fatigue equivalent stress.
	 *
	 * @param stress
	 *            Equivalent stress.
	 */
	public void setStress(Double stress) {
		stress_ = stress;
	}

	/**
	 * Returns fatigue damage.
	 *
	 * @return Fatigue damage.
	 */
	public Double getDamage() {
		return damage_;
	}

	/**
	 * Returns fatigue equivalent stress.
	 *
	 * @return Fatigue equivalent stress.
	 */
	public Double getStress() {
		return stress_;
	}
}
