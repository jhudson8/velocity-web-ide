package com.hudson.velocityweb.editors.velocity.completion.xml;

/**
 * @author Joe Hudson
 */
public class NodeAttribute {

	private String node;
	private String attribute;
	private int hashCode = Integer.MIN_VALUE;
	
	public NodeAttribute (String node, String attribute) {
		this.node = node;
		this.attribute = attribute;
	}

	/**
	 * @return
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * @return
	 */
	public String getNode() {
		return node;
	}
	
	public boolean equals (Object object) {
		if (null == object) return false;
		else if (object instanceof NodeAttribute) {
			NodeAttribute na = (NodeAttribute) object;
			if (null == getAttribute() || null == getNode()) {
				return false;
			}
			else {
				return (getAttribute().equals(na.getAttribute()) && getNode().equals(na.getNode()));
			}
		}
		else return false;
	}
	
	public int hashCode() {
		if (hashCode == Integer.MIN_VALUE) {
			hashCode = new String(getNode() + ":" + getAttribute()).hashCode();
		}
		return hashCode;
	}
}