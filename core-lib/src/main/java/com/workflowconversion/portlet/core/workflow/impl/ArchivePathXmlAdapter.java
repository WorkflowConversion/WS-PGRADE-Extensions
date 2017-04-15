package com.workflowconversion.portlet.core.workflow.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter to marshal/unmarshal {@link Path}.
 * 
 * @author delagarza
 *
 */
public class ArchivePathXmlAdapter extends XmlAdapter<String, Path> {

	@Override
	public Path unmarshal(final String stringPath) throws Exception {
		return Paths.get(stringPath);
	}

	@Override
	public String marshal(final Path path) throws Exception {
		return path.toString();
	}

}
