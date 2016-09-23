package com.workflowconversion.portlet.core.text;

import org.apache.commons.lang.Validate;

/**
 * Class containing the settings for the string similarity operations.
 * 
 * @author delagarza
 *
 */
public class StringSimilaritySettings {

	private final double cutOffValue;
	private final StringSimilarityAlgorithm algorithm;

	private StringSimilaritySettings(final double cutOffValue, final StringSimilarityAlgorithm algorithm) {
		Validate.isTrue(cutOffValue > 0,
				"cutOffValue must be greater than zero, please use the StringSimilaritySettingsBuilder.setCutOffValue() method to set a valid value");
		Validate.notNull(algorithm,
				"algorithm cannot be null, please use the StringSimilaritySettingsBuilder.setAlgorithm() method to set a proper instance");
		this.cutOffValue = cutOffValue;
		this.algorithm = algorithm;
	}

	/**
	 * @return the cutOffValue
	 */
	public double getCutOffValue() {
		return cutOffValue;
	}

	/**
	 * @return the algorithm
	 */
	public StringSimilarityAlgorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * Builder for {@link StringSimilaritySettings}.
	 * 
	 * @author delagarza
	 *
	 */
	public static class Builder {
		private double cutOffValue;
		private StringSimilarityAlgorithm algorithm;

		/**
		 * Sets the cut off value.
		 * 
		 * @param cutOffValue
		 *            The cut off value.
		 * @return {@code this} builder.
		 */
		public Builder setCutOffValue(final double cutOffValue) {
			this.cutOffValue = cutOffValue;
			return this;
		}

		/**
		 * Sets the string similarity algorithm.
		 * 
		 * @param algorithm
		 *            The algorithm.
		 * @return {@code this} builder.
		 */
		public Builder setAlgorithm(final StringSimilarityAlgorithm algorithm) {
			this.algorithm = algorithm;
			return this;
		}

		/**
		 * Constructs a new instance of {@link StringSimilaritySettings}.
		 * 
		 * @return
		 */
		public StringSimilaritySettings newStringSimilaritySettings() {
			return new StringSimilaritySettings(cutOffValue, algorithm);
		}
	}

}
