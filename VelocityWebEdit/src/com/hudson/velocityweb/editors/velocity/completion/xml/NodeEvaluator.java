package com.hudson.velocityweb.editors.velocity.completion.xml;


public interface NodeEvaluator {
	
	/**
	 * Evaluate the node given and return true to keep processing and false to stop processing
	 */
	public boolean pushNode (Node node);

	public boolean popNode (Node node);

	public boolean flatNode (Node node);
}
