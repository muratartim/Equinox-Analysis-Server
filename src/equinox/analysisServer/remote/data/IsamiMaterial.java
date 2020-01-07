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
package equinox.analysisServer.remote.data;

import java.io.Serializable;

/**
 * Abstract class for ISAMI material.
 *
 * @author Murat Artim
 * @date 8 May 2017
 * @time 15:40:48
 *
 */
public final class IsamiMaterial implements Serializable {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	/** Common attributes of material. */
	private String name_, specification_, orientation_, configuration_;

	/**
	 * No argument constructor for serialization.
	 */
	public IsamiMaterial() {
	}

	/**
	 * Sets material name.
	 *
	 * @param name
	 *            Material name.
	 */
	public void setName(String name) {
		name_ = name;
	}

	/**
	 * Sets material specification.
	 *
	 * @param specification
	 *            Material specification.
	 */
	public void setSpecification(String specification) {
		specification_ = specification;
	}

	/**
	 * Sets orientation.
	 *
	 * @param orientation
	 *            Orientation.
	 */
	public void setOrientation(String orientation) {
		orientation_ = orientation;
	}

	/**
	 * Sets configuration.
	 *
	 * @param configuration
	 *            Configuration.
	 */
	public void setConfiguration(String configuration) {
		configuration_ = configuration;
	}

	/**
	 * Returns material name.
	 *
	 * @return Material name.
	 */
	public String getName() {
		return name_;
	}

	/**
	 * Returns material specification.
	 *
	 * @return Material specification.
	 */
	public String getSpecification() {
		return specification_;
	}

	/**
	 * Returns orientation.
	 *
	 * @return Orientation.
	 */
	public String getOrientation() {
		return orientation_;
	}

	/**
	 * Returns configuration.
	 *
	 * @return Configuration.
	 */
	public String getConfiguration() {
		return configuration_;
	}
}