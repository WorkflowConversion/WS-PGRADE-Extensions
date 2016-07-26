/* Copyright 2007-2011 MTA SZTAKI LPDS, Budapest

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workflowconversion.guse.portlet.importer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * @author Luis de la Garza
 */
public class WorkflowImporterPortletServlet extends GenericPortlet {

	private String DEFAULT_PAGE = "/jsp/import.jsp";
	protected final org.slf4j.Logger logger = LoggerFactory
			.getLogger(WorkflowImporterPortletServlet.class);

	// TODO: do we need ASM at all? in the end we won't be submitting WFs via
	// ASM
	// ASMService asm_service = null;

	public WorkflowImporterPortletServlet() {
		logger.info("Initializing WorkflowImporterPortletServlet");
	}

	/**
	 * Handling generic actions
	 */
	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException {

		logger.trace("processaction called...");
		String action = "";
		request.getRemoteUser();
		// request.isUserInRole(role)
		/*
		 * if (request.getAttribute(SportletProperties.ACTION_EVENT) != null) {
		 * action
		 * =(""+request.getAttribute(SportletProperties.ACTION_EVENT)).split
		 * ("=")[1]; }
		 */
		// boolean isMultipart = PortletFileUpload.isMultipartContent(request);
		action = request.getParameter("action");
		if (StringUtils.isBlank(action)) {
			action = "doView";
		}
		logger.trace("*************" + action + "::"
				+ request.getParameter("action"));
		try {
			Method method = this.getClass().getMethod(action,
					new Class[] { ActionRequest.class, ActionResponse.class });
			method.invoke(this, new Object[] { request, response });
		} catch (NoSuchMethodException e) {
			logger.trace("-----------------------No such method");// +(""+request.getAttribute(SportletProperties.ACTION_EVENT)).split("=")[1]);
		} catch (IllegalAccessException e) {
			logger.trace("----------------------Illegal access");
		} catch (InvocationTargetException e) {
			logger.trace("-------------------Invoked function failed");
		}

	}

	/**
	 * View user notify settings informations...
	 */
	@Override
	public void doView(RenderRequest req, RenderResponse res)
			throws PortletException {
		try {
			// Setting next page
			String nextJSP = req.getParameter("nextJSP");
			if (nextJSP == null) {
				nextJSP = DEFAULT_PAGE;
			}
			PortletRequestDispatcher dispatcher = getPortletContext()
					.getRequestDispatcher(nextJSP);
			dispatcher.include(req, res);
		} catch (IOException ex) {
			Logger.getLogger(WorkflowImporterPortletServlet.class.getName())
					.log(Level.SEVERE, null, ex);
		}

	}

}
