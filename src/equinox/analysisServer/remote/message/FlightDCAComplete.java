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

import java.util.HashMap;

import equinox.serverUtilities.BigMessage;

/**
 * Class for typical flight damage contribution analysis complete network message.
 *
 * @author Murat Artim
 * @date 3 May 2017
 * @time 17:03:38
 *
 */
public final class FlightDCAComplete extends AnalysisComplete implements BigMessage {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** Mapping storing typical flight number versus damage. */
	private HashMap<Integer, Double> flightDamages_;

	/** Total damage. */
	private double totalDamage_ = 0.0;

	/**
	 * No argument constructor for serialization.
	 */
	public FlightDCAComplete() {
	}

	/**
	 * Adds damage.
	 *
	 * @param damage
	 *            Total damage.
	 */
	public void addDamage(double damage) {
		totalDamage_ += damage;
	}

	/**
	 * Puts given damage for given flight number.
	 *
	 * @param flightNumber
	 *            Typical flight number.
	 * @param damage
	 *            Damage.
	 */
	public void putDamage(int flightNumber, double damage) {
		if (flightDamages_ == null) {
			flightDamages_ = new HashMap<>();
		}
		flightDamages_.put(flightNumber, damage);
	}

	/**
	 * Returns total damage.
	 *
	 * @return Total damage.
	 */
	public double getTotalDamage() {
		return totalDamage_;
	}

	/**
	 * Returns flight damages.
	 *
	 * @return Flight damages.
	 */
	public HashMap<Integer, Double> getDamages() {
		return flightDamages_;
	}

	@Override
	public boolean isReallyBig() {
		if (flightDamages_ == null || flightDamages_.size() < 500)
			return false;
		return true;
	}
}
