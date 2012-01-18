package com.hudson.velocityweb.editors.velocity.completion.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;

import com.hudson.velocityweb.editors.velocity.PartitionScanner;

public class CursorState {

	public static final int STATE_UNKNOWN = 0;
	public static final int STATE_NODE_NAME = 1;
	public static final int STATE_WAITING_FOR_NODE_NAME = 2;
	public static final int STATE_ATTRIBUTE_NAME = 3;
	public static final int STATE_WAITING_FOR_ATTRIBUTE_NAME = 4;
	public static final int STATE_WAITING_FOR_ATTRIBUTE_VALUE_QUOTE = 5;
	public static final int STATE_WAITING_FOR_EQUAL = 6;
	public static final int STATE_ATTRIBUTE_VALUE = 7;
	public static final int STATE_END_ATTRIBUTE_VALUE = 8;
	public static final int STATE_NODE_TEXT = 9;
	public static final int STATE_FLAT_NODE_END = 10;
	public static final int STATE_WAITING_FOR_NODE_FOOTER_NAME = 11;
	public static final int STATE_WAITING_FOR_NODE_END = 12;
	
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_HEADER = 1;
	public static final int TYPE_HEADER_FLAT = 2;
	public static final int TYPE_FOOTER = 3;
	
	public static final char CHAR_SLASH = '/';
	public static final char CHAR_GT_SIGN = '>';
	public static final char CHAR_LT_SIGN = '<';
	public static final char CHAR_QUOTE = '\"';
	public static final char CHAR_EQUAL = '=';
	public static final char CHAR_RTN = '\n';
	
	private Node currentNode;
	private Stack nodeHierarchy;
	private int state = -1;
	
	private CursorState () {}
	
	public static final CursorState evaluate (IDocument doc, NodeEvaluator evaluator) {
		return getCursorState(doc, doc.getLength() + 1, evaluator);
	}
	
	public static final CursorState getCursorState(IDocument doc, int offset) {
		return getCursorState(doc, offset, null);
	}
	
	private static final CursorState getCursorState(IDocument doc, int offset, NodeEvaluator evaluator) {
		try {
			CursorState cs = new CursorState();
			List typedOffsets = new ArrayList();
			String[] categories = doc.getPositionCategories();
			for (int i=0; i<categories.length; i++) {
				Position[] positions = doc.getPositions(categories[i]);
				for (int j=0; j<positions.length; j++) {
					typedOffsets.add(new Integer(positions[j].getOffset()));
				}
			}
			Collections.sort(typedOffsets);
			
			List regions = new ArrayList();
			ITypedRegion saveRegion = null;
			ITypedRegion currentRegion = null;
			for (Iterator i=typedOffsets.iterator(); i.hasNext(); ) {
				int tOffset = ((Integer) i.next()).intValue();
				if (tOffset > offset) break;
				saveRegion = doc.getPartition(tOffset);
				if (null == currentRegion || !currentRegion.equals(saveRegion)) {
					currentRegion = saveRegion;
					regions.add(currentRegion);
				}
			}
			
			Stack nodeHierarchy = new Stack();
			for (Iterator i=regions.iterator(); i.hasNext(); ) {
				ITypedRegion region = (ITypedRegion) i.next();
				if (region.getType().equals(PartitionScanner.XML_TAG)) {
					if (region.getOffset() + region.getLength() > offset) {
						String regionText = doc.get(region.getOffset(), region.getLength());
						int index = regionText.indexOf(CHAR_LT_SIGN, 1);
						if (index > -1) {
							if (index < offset) {
								// we're in the working part
								Node parentNode = null;
								if (nodeHierarchy.size() > 0) parentNode = (Node) nodeHierarchy.peek();
								cs.currentNode = new Node(parentNode, region.getOffset(), region.getOffset() + index-1, doc);
								if (null != evaluator) if (!evaluator.pushNode(cs.currentNode)) return cs;
								break;
							}
							else {
								// we can't reconcile because the previous error must be fixed
								cs.state = STATE_UNKNOWN;
							}
						}
						else {
							Node parentNode = null;
							if (nodeHierarchy.size() > 0) parentNode = (Node) nodeHierarchy.peek();
							cs.currentNode = new Node(parentNode, region.getOffset(), region.getOffset() + region.getLength() - 1, doc);
							if (null != evaluator) if (!evaluator.pushNode(cs.currentNode)) return cs;
							break;
						}
					}
					if (isNodeHeader(region, doc)) {
						Node parentNode = null;
						if (nodeHierarchy.size() > 0) parentNode = (Node) nodeHierarchy.peek();
						Node n = new Node(parentNode, region, doc);
						if (n.isPseudoFlatNode()) {
							if (nodeHierarchy.size() > 0) parentNode = (Node) nodeHierarchy.peek();
							if (region.getOffset() + region.getLength() > offset) {
								// we're in a flat node
								cs.currentNode = new Node(parentNode, region, doc);
								if (null != evaluator) if (!evaluator.flatNode(cs.currentNode)) return cs;
							}
							else {
								if (null != evaluator) if (!evaluator.flatNode(new Node(parentNode, region, doc))) return cs;
							}
						}
						else {
							nodeHierarchy.push(n);
							if (null != evaluator) if (!evaluator.pushNode(n)) return cs;
							if (region.getOffset() + region.getLength() > offset) cs.currentNode = (Node) nodeHierarchy.pop();
						}
					}
					else if (isNodeFooter(region, doc)) {
						Node parentNode = null;
						if (nodeHierarchy.size() > 0) parentNode = (Node) nodeHierarchy.peek();
						if (region.getOffset() + region.getLength() > offset) {
							// we're in the node footer
							cs.currentNode = new Node(parentNode, region, doc);
						}
						else {
							if (null != evaluator) if (!evaluator.popNode(new Node(parentNode, region, doc))) return cs;
							if (nodeHierarchy.size() > 0) {
								Node node = (Node) nodeHierarchy.pop();
								if (null != cs.currentNode && !node.getName().equals(cs.currentNode.getName())) nodeHierarchy.push(node);
							}
						}
					}
					else if (isNodeHeaderAndFooter(region, doc)) {
						Node parentNode = null;
						if (nodeHierarchy.size() > 0) parentNode = (Node) nodeHierarchy.peek();
						if (region.getOffset() + region.getLength() > offset) {
							// we're in a flat node
							cs.currentNode = new Node(parentNode, region, doc);
							if (null != evaluator) if (!evaluator.flatNode(cs.currentNode)) return cs;
						}
						else {
							if (null != evaluator) if (!evaluator.flatNode(new Node(parentNode, region, doc))) return cs;
						}
					}
				}
				else if (region.getOffset() + region.getLength() > offset) {
					// we might be working on a node
					String text = doc.get(region.getOffset(), region.getLength()).trim();
					if (text.length() > 0 && text.charAt(0) == CHAR_LT_SIGN) {
						// were working on a node
						int offsetT = region.getOffset();
						while (Character.isWhitespace(doc.getChar(offsetT++)) && offsetT <= region.getOffset() + region.getLength()) {
						}
						Node parentNode = null;
						if (nodeHierarchy.size() > 0) parentNode = (Node) nodeHierarchy.peek();
						cs.currentNode = new Node(parentNode, offsetT, region.getOffset()+region.getLength(), doc);
						if (null != evaluator) if (!evaluator.pushNode(cs.currentNode)) return cs;
					}
				}
			}
			cs.nodeHierarchy = nodeHierarchy;
			return cs;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static boolean isNodeHeader(ITypedRegion region, IDocument document) throws BadLocationException {
		for (int i=region.getOffset() + region.getLength()-2; i>=0; i--) {
			char c = document.getChar(i);
			if (!Character.isWhitespace(c)) {
				if (c == CHAR_SLASH) return false;
				else break;
			}
		}
		for (int i=region.getOffset()+1; i<document.getLength(); i++) {
			char c = document.getChar(i);
			if (!Character.isWhitespace(c)) {
				if (c == CHAR_SLASH) return false;
				else break;
			}
		}
		return true;
	}

	private static boolean isNodeFooter(ITypedRegion region, IDocument document) throws BadLocationException {
		for (int i=region.getOffset()+1; i<document.getLength(); i++) {
			char c = document.getChar(i);
			if (!Character.isWhitespace(c)) {
				if (c == CHAR_SLASH) return true;
				else break;
			}
		}
		return false;
	}

	private static boolean isNodeHeaderAndFooter(ITypedRegion region, IDocument document) throws BadLocationException {
		for (int i=region.getOffset() + region.getLength()-2; i>=0; i--) {
			char c = document.getChar(i);
			if (!Character.isWhitespace(c)) {
				if (c == CHAR_SLASH) return true;
				else break;
			}
		}
		return false;
	}

	public Node getCurrentNode() {
		return currentNode;
	}
	public void setCurrentNode(Node currentNode) {
		this.currentNode = currentNode;
	}
	public Stack getNodeHierarchy() {
		return nodeHierarchy;
	}
	public void setNodeHierarchy(Stack nodeHierarchy) {
		this.nodeHierarchy = nodeHierarchy;
	}
	public int getState(int offset) {
		if (state == -1) {
			if (null != currentNode) state = currentNode.getState(offset);
			else state = STATE_UNKNOWN;
		}
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
}