package com.hudson.velocityweb.editors.velocity.completion;

import org.eclipse.jface.text.IDocument;

import com.hudson.velocityweb.editors.velocity.completion.xml.CursorState;
import com.hudson.velocityweb.editors.velocity.completion.xml.Node;

public class Attribute {
	
	private IDocument document;
	private String name;
	private String value;
	private int nameOffset;
	private int equalOffset;
	private int valueOffset;
	private Node node;
	
	public IDocument getDocument() {
		return document;
	}
	public void setDocument(IDocument document) {
		this.document = document;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNameOffset() {
		return nameOffset;
	}
	public void setNameOffset(int nameOffset) {
		this.nameOffset = nameOffset;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public String getValue() {
		if (null == value) return "";
		else return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getValueOffset() {
		return valueOffset;
	}
	public void setValueOffset(int valueOffset) {
		this.valueOffset = valueOffset;
	}
	public int getEqualOffset() {
		return equalOffset;
	}
	public void setEqualOffset(int equalOffset) {
		this.equalOffset = equalOffset;
	}
	
	public int getState (int offset) {
		if (offset == nameOffset) return CursorState.STATE_WAITING_FOR_ATTRIBUTE_NAME;
		else if (offset <= nameOffset + name.length()) return CursorState.STATE_ATTRIBUTE_NAME;
		else if (offset <= equalOffset) return CursorState.STATE_WAITING_FOR_EQUAL;
		else if (offset < valueOffset) return CursorState.STATE_WAITING_FOR_ATTRIBUTE_VALUE_QUOTE;
		else if (offset >= valueOffset && offset <= valueOffset + value.length()) return CursorState.STATE_ATTRIBUTE_VALUE;
		else if (offset == valueOffset + value.length() + 1) return CursorState.STATE_WAITING_FOR_ATTRIBUTE_VALUE_QUOTE;
		else return CursorState.STATE_UNKNOWN;
	}
}
