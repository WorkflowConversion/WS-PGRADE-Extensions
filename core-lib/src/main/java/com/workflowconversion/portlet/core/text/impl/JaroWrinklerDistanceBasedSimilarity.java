package com.workflowconversion.portlet.core.text.impl;

import org.apache.commons.text.similarity.JaroWrinklerDistance;

import com.workflowconversion.portlet.core.text.StringSimilarityAlgorithm;

/**
 * Computes similarities between strings using the {@link JaroWrinklerDistance}.
 * 
 * @author delagarza
 *
 */
public class JaroWrinklerDistanceBasedSimilarity implements StringSimilarityAlgorithm {

	private final JaroWrinklerDistance algorithm;

	/**
	 * Constructor.
	 */
	public JaroWrinklerDistanceBasedSimilarity() {
		this.algorithm = new org.apache.commons.text.similarity.JaroWrinklerDistance();
	}

	@Override
	public double compare(final String s1, final String s2) {
		if (s1 == null || s2 == null) {
			throw new NullPointerException("None of the parameters in a string comparison is allowed to be null.");
		}
		return this.algorithm.apply(s1, s2).doubleValue();
	}

}
