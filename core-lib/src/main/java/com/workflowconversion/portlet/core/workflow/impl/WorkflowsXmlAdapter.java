package com.workflowconversion.portlet.core.workflow.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.workflowconversion.portlet.core.workflow.Workflow;

public class WorkflowsXmlAdapter extends XmlAdapter<WorkflowsXmlAdapter.Workflows, Map<String, Workflow>> {

	@Override
	public Map<String, Workflow> unmarshal(final Workflows workflows) throws Exception {
		final Map<String, Workflow> workflowMap = new TreeMap<String, Workflow>();
		for (final Workflow workflow : workflows.workflowList) {
			workflowMap.put(workflow.getId(), workflow);
		}
		return workflowMap;
	}

	@Override
	public Workflows marshal(final Map<String, Workflow> workflowMap) throws Exception {
		final Workflows workflows = new Workflows();
		for (final Workflow workflow : workflowMap.values()) {
			workflows.workflowList.add(workflow);
		}
		return workflows;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Workflows {
		@XmlElement(name = "workflow")
		private final List<Workflow> workflowList = new LinkedList<Workflow>();
	}
}
