package com.workflowconversion.importer.text;

/**
 * Interface for classes that want to implement a more sophisticated string comparison.
 * 
 * Implementations must make sure that comparisons are symmetric and transitive.
 * 
 * @author delagarza
 *
 */
public interface StringSimilarityAlgorithm {

	/**
	 * Compares the two given strings. The score varies between {@code 0} (no similarities) and {@code 1.0} (total
	 * similarity).
	 * 
	 * @param s1
	 *            The first string.
	 * @param s2
	 *            The second string.
	 * @return The comparison score.
	 */
	public double compare(final String s1, final String s2);

}
