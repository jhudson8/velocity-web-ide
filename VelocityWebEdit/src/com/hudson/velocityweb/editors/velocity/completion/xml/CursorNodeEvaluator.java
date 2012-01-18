package com.hudson.velocityweb.editors.velocity.completion.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Joe Hudson
 */
public class CursorNodeEvaluator implements NodeEvaluator {

	private int offset;

	public Node currentNode;
	public Stack nodeStack = new Stack();
	public List parentNodes = new ArrayList();
	
	public CursorNodeEvaluator (int offset) {
		this.offset = offset;
	}
	
	/**
	 * 
	 */
	public CursorNodeEvaluator() {
		super();
	}

	/**
	 * @see com.hudson.velocityweb.editors.velocity.completion.xml.NodeEvaluator#pushNode(com.hudson.velocityweb.editors.velocity.completion.xml.Node)
	 */
	public boolean pushNode(Node node) {
        if (node.isPseudoFlatNode()) {
            return flatNode(node);
        }
        else {
            if (nodeStack.size() == 0) {
                parentNodes.add(node);
            }
            else {
                ((Node) nodeStack.peek()).addChildNode(node);
            }
            nodeStack.push(node);
        }
        currentNode = node;
        if (node.getOffsetEnd() > offset) return false;
        else return true;
	}

	/**
	 * @see com.hudson.velocityweb.editors.velocity.completion.xml.NodeEvaluator#popNode(com.hudson.velocityweb.editors.velocity.completion.xml.Node)
	 */
	public boolean popNode(Node node) {
        if (nodeStack.size() > 0 && !node.isPseudoFlatNode()) {
            Node n = (Node) nodeStack.peek();
            if (n.getName().equals(node.getName())) nodeStack.pop();
        }
        currentNode = node;
        if (node.getOffsetEnd() > offset) return false;
        else return true;
	}

	/**
	 * @see com.hudson.velocityweb.editors.velocity.completion.xml.NodeEvaluator#flatNode(com.hudson.velocityweb.editors.velocity.completion.xml.Node)
	 */
	public boolean flatNode(Node node) {
        currentNode = node;
        if (node.getOffsetEnd() > offset) return false;
        else return true;
	}

}
