package com.hudson.velocityweb.exceptions;

import org.w3c.dom.Node;

public class AttributeNotSpecifiedException extends HibernateSynchronizerException {

	private static final long serialVersionUID = 1L;
	private String attribute;
	
	public AttributeNotSpecifiedException(Node node, String attribute) {
		setNode(node);
		this.attribute = attribute;
	}

	public String getAttribute() {
		return attribute;
	}

	public String getMessage() {
		return "the \"" + attribute + "\" attribute is required for the \"" + getNode().getLocalName() + "\" node";
	}
}