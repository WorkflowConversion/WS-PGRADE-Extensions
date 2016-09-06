package com.workflowconversion.importer.guse.text.impl;

import java.util.Locale;

import org.apache.commons.text.similarity.FuzzyScore;

import com.workflowconversion.importer.guse.text.StringSimilarityAlgorithm;

/**
 * Implements a fuzzy based similarity, based on {@link FuzzyScore}.
 * 
 * @author delagarza
 *
 */
public class FuzzySimilarity implements StringSimilarityAlgorithm {

	private final FuzzyScore algorithm;

	/**
	 * Constructor, defaults to use english as a locale.
	 */
	public FuzzySimilarity() {
		this(Locale.ENGLISH);
	}

	/**
	 * Constructor.
	 * 
	 * @param locale
	 *            Uses the given locale for string comparisons.
	 */
	public FuzzySimilarity(final Locale locale) {
		this.algorithm = new FuzzyScore(locale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.workflowconversion.importer.guse.text.StringSimilarity#compare(java.lang.String, java.lang.String)
	 */
	@Override
	public double compare(final String s1, final String s2) {
		if (s1 == null || s2 == null) {
			throw new NullPointerException("Neither string in a string comparison is allowed to be null.");
		}
		int score = algorithm.fuzzyScore(s1, s2);
		// from the javadocs, we know how to compute the max score
		int longestWordLength = s1.length() <= s2.length() ? s2.length() : s1.length();
		int maxScore = longestWordLength + (2 * (longestWordLength - 1));
		// normalize the score
		return ((1.0 * score) / maxScore);
	}

}
