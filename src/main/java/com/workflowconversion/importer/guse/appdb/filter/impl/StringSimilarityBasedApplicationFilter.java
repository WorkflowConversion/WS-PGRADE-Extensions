package com.workflowconversion.importer.guse.appdb.filter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.importer.guse.Settings;
import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.filter.ApplicationField;
import com.workflowconversion.importer.guse.exception.ApplicationException;
import com.workflowconversion.importer.guse.filter.Filter;
import com.workflowconversion.importer.guse.text.StringSimilarityAlgorithm;

/**
 * It is sometimes useful to filter a list of applications given certain criteria. This class implements the
 * {@link Filter} interface and its {@link Filter#apply} method returns a filtered and sorted list of applications
 * according to their string similarity score.
 * 
 * @author delagarza
 *
 */
public class StringSimilarityBasedApplicationFilter implements Filter<Application> {

	private final double cutOffScore;
	private final StringSimilarityAlgorithm algorithm;
	private final String term;
	private final ApplicationField field;

	private StringSimilarityBasedApplicationFilter(final double cutOffScore, final StringSimilarityAlgorithm algorithm,
			final String term, final ApplicationField field) {
		Validate.isTrue(cutOffScore > 0.0,
				"cutOffScore must be greater or equal to zero, please use the Builder.setCutOffScore() method to set a proper value.");
		Validate.notNull(algorithm,
				"algorithm cannot be null, please use the Builder.setAlgorithm() method to set a proper instance.");
		Validate.isTrue(!StringUtils.isBlank(term),
				"term cannot be empty or contain only whitespace, please use the Builder.setTerm() method to set a proper value.");
		Validate.notNull(field,
				"field cannot be null, please use the Builder.setField() method to set a proper instance.");
		this.cutOffScore = cutOffScore;
		this.algorithm = algorithm;
		this.term = term;
		this.field = field;
	}

	@Override
	public Collection<Application> apply(final Collection<Application> applications) {
		final ApplicationScorer scorer = new ApplicationScorer(algorithm, cutOffScore) {

			@Override
			protected String getField(final Application application) {
				switch (field) {
				case Name:
					return application.getName();
				case Description:
					return application.getDescription();
				case Path:
					return application.getPath();
				case Resource:
					return application.getResource();
				case ResourceType:
					return application.getResourceType();
				case Version:
					return application.getVersion();
				default:
					throw new ApplicationException("Invalid application field [" + field
							+ "]. This is most probably a bug, please report it to your portal administrator.");

				}
			}
		};
		return scorer.applyByField(applications, term);
	}

	private static abstract class ApplicationScorer {

		private final StringSimilarityAlgorithm algorithm;
		private final double cutOffScore;

		private ApplicationScorer(final StringSimilarityAlgorithm algorithm, final double cutOffScore) {
			this.algorithm = algorithm;
			this.cutOffScore = cutOffScore;
		}

		protected abstract String getField(final Application application);

		private Collection<Application> applyByField(final Collection<Application> applications, final String term) {
			final List<ScoredApplication> scoredFilteredApplications = new ArrayList<ScoredApplication>(
					applications.size());
			for (final Application application : applications) {
				// get the score from the algorithm using the desired field
				final double score = algorithm.compare(this.getField(application), term);
				// filter
				if (score >= cutOffScore) {
					// adding to sorted set will automatically take care of the ordering
					scoredFilteredApplications.add(new ScoredApplication(score, application));
				}
			}
			// sort
			scoredFilteredApplications.sort(new ScoredApplicationComparator());
			// iterate to obtain the sorted elements
			final Collection<Application> filteredApplications = new LinkedList<Application>();
			for (final ScoredApplication scoredFilteredApplication : scoredFilteredApplications) {
				filteredApplications.add(scoredFilteredApplication.application);
			}
			return filteredApplications;
		}

	}

	// compares applications by their score
	private static class ScoredApplicationComparator implements Comparator<ScoredApplication> {
		@Override
		public int compare(final ScoredApplication left, final ScoredApplication right) {
			if (left.score > right.score) {
				return -1;
			}
			return (left.score < right.score ? 1 : 0);
		}
	}

	// add a score field
	private static class ScoredApplication {
		private final double score;
		private final Application application;

		ScoredApplication(final double score, final Application application) {
			this.score = score;
			this.application = application;
		}
	}

	/**
	 * Builder class for {@link StringSimilarityBasedApplicationFilter}.
	 * 
	 * Note that {@code cutOffScore} and {@code algorithm} are default to the values provided by
	 * {@link Settings#getStringSimilaritySettings()}.
	 * 
	 * @author delagarza
	 *
	 */
	public static class Builder {
		private double cutOffScore;
		private StringSimilarityAlgorithm algorithm;
		private String term;
		private ApplicationField field;

		/**
		 * Sets the cut off score for the filter. For an entry to pass the filter, it's score must be greater or equal
		 * to the cut off score.
		 * 
		 * @param cutOffScore
		 *            The cut off score.
		 * @return an instance to {@code this} builder.
		 */
		public Builder setCutOffScore(final double cutOffScore) {
			this.cutOffScore = cutOffScore;
			return this;
		}

		/**
		 * Sets the algorithm implementation for string similarity.
		 * 
		 * @param algorithm
		 *            the algorithm implementation.
		 * @return an instance to {@code this} builder.
		 */
		public Builder setStringSimilarityAlgorithm(final StringSimilarityAlgorithm algorithm) {
			this.algorithm = algorithm;
			return this;
		}

		/**
		 * Sets the term to filter against.
		 * 
		 * @param term
		 *            the term to filter against.
		 * @return an instance to {@code this} builder.
		 */
		public Builder setTerm(final String term) {
			this.term = term;
			return this;
		}

		/**
		 * Sets the field against which the provided {@code term} will be compared.
		 * 
		 * @param field
		 *            the field to compare with.
		 * @return an instance to {@code this} builder.
		 */
		public Builder setField(final ApplicationField field) {
			this.field = field;
			return this;
		}

		/**
		 * Builds a new instance of a {@link StringSimilarityBasedApplicationFilter}.
		 * 
		 * @return a new instance of a {@link StringSimilarityBasedApplicationFilter}.
		 */
		public StringSimilarityBasedApplicationFilter newStringSimilarityBasedApplicationFilter() {
			return new StringSimilarityBasedApplicationFilter(this.cutOffScore, this.algorithm, this.term, this.field);
		}
	}
}
