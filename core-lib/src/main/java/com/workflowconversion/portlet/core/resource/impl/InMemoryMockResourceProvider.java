package com.workflowconversion.portlet.core.resource.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.annotation.NotThreadSafe;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.utils.KeyUtils;

import dci.data.Middleware;

/**
 * Mock resource provider (not thread safe).
 * 
 * @author delagarza
 *
 */
@NotThreadSafe
public class InMemoryMockResourceProvider implements ResourceProvider {

	private static final long serialVersionUID = 9085026519196444948L;

	private final static Logger LOG = LoggerFactory.getLogger(InMemoryMockResourceProvider.class);

	private final static int N_INITIAL_RESOURCES = 8;
	private final static int N_INITIAL_APPS = 9;
	private final static int N_INITIAL_QUEUES = 4;

	private final String name;
	private final MiddlewareProvider middlewareProvider;
	private final Map<String, Resource> resources;
	private final boolean canAddApplications;

	public InMemoryMockResourceProvider(final String name, final MiddlewareProvider middlewareProvider,
			final boolean canAddApplications) {
		this.name = name;
		this.middlewareProvider = middlewareProvider;
		this.resources = new TreeMap<String, Resource>();
		this.canAddApplications = canAddApplications;
		fillInitialResources();
	}

	private void fillInitialResources() {
		final Middleware[] middlewares = middlewareProvider.getAllMiddlewares().toArray(new Middleware[] {});

		for (int i = 0; i < N_INITIAL_RESOURCES; i++) {
			final Resource.Builder resourceBuilder = new Resource.Builder();
			resourceBuilder.withType(getRandomResourceType(middlewares));
			resourceBuilder.withName("Fake resource " + i);
			resourceBuilder.canModifyApplications(canAddApplications);
			final Collection<Queue> queues = new LinkedList<Queue>();
			for (int j = 0; j < N_INITIAL_QUEUES; j++) {
				final Queue.Builder queueBuilder = new Queue.Builder();
				queueBuilder.withName("queue_" + i + '_' + j);
				queues.add(queueBuilder.newInstance());
			}
			resourceBuilder.withQueues(queues);
			final Collection<Application> applications = new LinkedList<Application>();
			for (int j = 0; j < N_INITIAL_APPS; j++) {
				final Application.Builder applicationBuilder = new Application.Builder();
				applicationBuilder.withName("Fake application " + i + ", " + j);
				applicationBuilder.withDescription("Description of fake application " + j);
				applicationBuilder.withPath("/path/of/fake/app_" + j);
				applicationBuilder.withVersion("1.1." + j);
				applications.add(applicationBuilder.newInstance());
			}
			resourceBuilder.withApplications(applications);
			addResource_internal(resourceBuilder.newInstance());
		}
	}

	private String getRandomResourceType(final Middleware[] middlewares) {
		return middlewares[(int) ((middlewares.length - 1) * Math.random())].getType();
	}

	@Override
	public boolean canAddApplications() {
		return canAddApplications;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void init() {
		// nop
	}

	@Override
	public Collection<Resource> getResources() {
		return resources.values();
	}

	@Override
	public Resource getResource(final String name, final String type) {
		return resources.get(KeyUtils.generateResourceKey(name, type));
	}

	private void addResource_internal(final Resource resource) {
		Validate.notNull(resource);
		resources.put(KeyUtils.generate(resource), resource);
	}

	@Override
	public void save() {
		LOG.info("COMMITTING CHANGES");
		// does nothing, since we're actually not storing anything
	}

	@Override
	public boolean hasInitErrors() {
		// TODO Auto-generated method stub
		return false;
	}
}
