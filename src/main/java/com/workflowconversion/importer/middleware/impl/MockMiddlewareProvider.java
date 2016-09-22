package com.workflowconversion.importer.middleware.impl;

import java.util.Collection;

import com.workflowconversion.importer.middleware.MiddlewareProvider;

import dci.data.Item;
import dci.data.Middleware;

public class MockMiddlewareProvider implements MiddlewareProvider {

	public MockMiddlewareProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Collection<Middleware> getAvailableMiddlewares() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Middleware> getAvailableMiddlewares(String middlewareType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Item> getAvailableItems(String middlewareType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Middleware> getAllMiddlewares() {
		// TODO Auto-generated method stub
		return null;
	}

}
