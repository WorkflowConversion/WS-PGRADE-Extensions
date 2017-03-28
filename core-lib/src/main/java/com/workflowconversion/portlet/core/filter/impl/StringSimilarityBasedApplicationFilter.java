package com.workflowconversion.portlet.core.filter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.exception.InvalidFieldException;
import com.workflowconversion.portlet.core.filter.Filter;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.FormField;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.core.text.StringSimilarityAlgorithm;

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
	private final FormField formField;

	private StringSimilarityBasedApplicationFilter(final double cutOffScore, final StringSimilarityAlgorithm algorithm,
			final String term, final FormField formField) {
		Validate.isTrue(cutOffScore > 0.0,
				"cutOffScore must be greater or equal to zero, please use the Builder.setCutOffScore() method to set a proper value.");
		Validate.notNull(algorithm,
				"algorithm cannot be null, please use the Builder.setAlgorithm() method to set a proper instance.");
		Validate.isTrue(!StringUtils.isBlank(term),
				"term cannot be empty or contain only whitespace, please use the Builder.setTerm() method to set a proper value.");
		Validate.notNull(formField,
				"formField cannot be null, please use the Builder.setField() method to set a proper instance.");
		this.cutOffScore = cutOffScore;
		this.algorithm = algorithm;
		this.term = term;
		this.formField = formField;
	}

	@Override
	public Collection<Application> apply(final Collection<Application> applications) {
		final ApplicationScorer scorer = new ApplicationScorer(algorithm, cutOffScore) {

			@Override
			protected String getField(final Application application) {
				if (Application.Field.class.isAssignableFrom(formField.getClass())) {
					switch ((Application.Field) formField) {
					case Description:
						return application.getDescription();
					case Path:
						return application.getPath();
					case Version:
						return application.getVersion();
					default:
						throw new InvalidFieldException(formField);
					}
				} else if (Resource.Field.class.isAssignableFrom(formField.getClass())) {
					switch ((Resource.Field) formField) {
					case Name:
						return application.getResource().getName();
					case Type:
						return application.getResource().getType();
					default:
						throw new InvalidFieldException(formField);
					}
				} else {
					throw new InvalidFieldException(formField);
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
			Collections.sort(scoredFilteredApplications, new ScoredApplicationComparator());
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
		private FormField formField;

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
		 * @param formField
		 *            the field to compare with.
		 * @return an instance to {@code this} builder.
		 */
		public Builder setFormField(final FormField formField) {
			this.formField = formField;
			return this;
		}

		/**
		 * Builds a new instance of a {@link StringSimilarityBasedApplicationFilter}.
		 * 
		 * @return a new instance of a {@link StringSimilarityBasedApplicationFilter}.
		 */
		public StringSimilarityBasedApplicationFilter newStringSimilarityBasedApplicationFilter() {
			return new StringSimilarityBasedApplicationFilter(this.cutOffScore, this.algorithm, this.term,
					this.formField);
		}
	}
}
