package com.workflowconversion.importer.guse.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;

/**
 * Class that deals with cleaning up of this webapp.
 * 
 * @author delagarza
 *
 */
public class CleanUpShutdownContextListener implements ServletContextListener {

	public CleanUpShutdownContextListener() {
		// nop
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("INFO: Performing cleanup tasks for WorkflowImporterPortlet");
		// 1. shutdown mysql cleanup thread
		try {
			AbandonedConnectionCleanupThread.shutdown();
		} catch (InterruptedException e) {
			System.err.print("SEVERE: could not shutdown MySQL's Cleanup Thread: " + e.getMessage());
			e.printStackTrace(System.err);
			// but there's not much we can do anyway...
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// nop
	}

}
