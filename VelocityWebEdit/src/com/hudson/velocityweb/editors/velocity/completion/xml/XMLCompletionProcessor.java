package com.hudson.velocityweb.editors.velocity.completion.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;

import com.hudson.velocityweb.css.CSSFile;
import com.hudson.velocityweb.css.CSSParser;
import com.hudson.velocityweb.css.CSSStyle;
import com.hudson.velocityweb.editors.velocity.PartitionScanner;
import com.hudson.velocityweb.editors.velocity.completion.Attribute;
import com.hudson.velocityweb.javascript.JavascriptFile;
import com.hudson.velocityweb.javascript.JavascriptFunction;
import com.hudson.velocityweb.javascript.JavascriptParser;
import com.hudson.velocityweb.manager.ConfigurationManager;

public class XMLCompletionProcessor {

	private IFile file;
	
	public XMLCompletionProcessor (IFile file) {
		this.file = file;
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset, IFile file) {
		IDocument doc = viewer.getDocument();
		CursorState cursorState = CursorState.getCursorState(doc, offset);
		Node currentNode = cursorState.getCurrentNode();
		if (null == currentNode) {
		    try {
		        for (int i=offset-1; i>=0; i--) {
		            char c = doc.getChar(i);
		            if (c == '>') break;
		            else if (c == '<') {
		            	Node parent = null;
		            	if (cursorState.getNodeHierarchy().size() > 0)
		            		parent = (Node) cursorState.getNodeHierarchy().peek();
		                currentNode = new Node(parent, i+1, offset, doc);
		                break;
		            }
		        }
		    }
		    catch (BadLocationException e) {}
		}
		if (null != currentNode) {
			try {
				int state = currentNode.getState(offset);
				if (state == CursorState.STATE_WAITING_FOR_NODE_END) {
					return new ICompletionProposal[]{new CompletionProposal(">", offset, 0, offset+1)};
				}
				else if (state == CursorState.STATE_NODE_NAME || state == CursorState.STATE_WAITING_FOR_NODE_NAME) {
					int type = currentNode.getType();
					if (type == CursorState.TYPE_FOOTER) {
						Node headerNode = currentNode.getParent();
						if (null != headerNode) {
							String text = headerNode.getName();
							String actual = headerNode.getName() + '>';
							// scan for end node
							int endIndex = currentNode.getNameStart() + currentNode.getName().length();
							try {
								int i = currentNode.getNameStart() + currentNode.getName().length();
								char c = doc.getChar(i);
								while (Character.isWhitespace(c)) c = doc.getChar(++i);
								if (c == '>') {
									endIndex = i + 1;
								}
							}
							catch (BadLocationException e) {}
							return new ICompletionProposal[]{new CompletionProposal(actual, currentNode.getNameStart()+1, endIndex - currentNode.getNameStart(), actual.length(), null, text, null, null)};
						}
						else
							return null;
					}
					else {
					    try {
							int start = currentNode.getNameStart();
							if (start == -1) start = currentNode.getOffsetStart();
							int end = start + currentNode.getName().length();
							String prefixUpper = doc.get(start, offset-start).toUpperCase();
							String[] proposalArr = XMLSuggestor.getNodeSuggestions(currentNode.getParent(), file);
							if (null == proposalArr) return null;
							List rtn = new ArrayList();
							for (int i=0; i<proposalArr.length; i++) {
								if (prefixUpper.length() == 0 || proposalArr[i].toUpperCase().startsWith(prefixUpper)) {
									rtn.add(new CompletionProposal(
											proposalArr[i],
											start,
											currentNode.getName().length(),
											proposalArr[i].length()));
								}
							}
							return getProposalArray(rtn);
					    }
					    catch (BadLocationException e) {
					        return null;
					    }
					}
				}
				else if (state == CursorState.STATE_WAITING_FOR_ATTRIBUTE_NAME || state == CursorState.STATE_ATTRIBUTE_NAME) {
					Attribute attribute = currentNode.getAttribute(offset);
					if (null == attribute) attribute = currentNode.getAttribute(offset+1);
					int start = offset;
					int replaceLength = 0;
					String currentAttributeName = null;
					if (null != attribute) {
						 start = attribute.getNameOffset();
						 replaceLength = attribute.getName().length();
						 currentAttributeName = attribute.getName();
					}
					else {
						int i=offset;
						try {
							while (Character.isLetterOrDigit(doc.getChar(--i)));
						}
						catch (BadLocationException e) {}
						i++;
						start = i;
						i=offset;
						try {
							while (Character.isLetterOrDigit(doc.getChar(++i)));
						}
						catch (BadLocationException e) {}
						replaceLength = i - start - 1;
						try {
						    currentAttributeName = doc.get(i, replaceLength);
						}
						catch (BadLocationException e) {
							try {
								currentAttributeName = doc.get(i-replaceLength, replaceLength);
							}
							catch (BadLocationException e1) {}
						}
					}
					StringBuffer postStr = new StringBuffer();
					int equalsIndex = -1;
					int index = start;
					if (null != currentAttributeName) index += currentAttributeName.length();
					try {
						char c = doc.getChar(index);
						while (Character.isWhitespace(c) || c == '=') {
							if (c == '=') {
								equalsIndex = index;
								break;
							}
							c = doc.getChar(++index);
						}
					}
					catch (BadLocationException e) {
					}
					if (equalsIndex == -1) {
						postStr.append("=\"\"");
					}
					
					String prefixUpper = doc.get(start, offset-start).toUpperCase();
					List proposalList = XMLSuggestor.getAttributeSuggestions(currentNode, currentAttributeName, file);
					if (null == proposalList) return null;
					List rtn = new ArrayList();
					for (Iterator i=proposalList.iterator(); i.hasNext(); ) {
						String attributeName = (String) i.next();
						String actual = attributeName;
						if (attributeName.toUpperCase().startsWith(prefixUpper)) {
							if (postStr.length() > 0) {
								actual += postStr.toString();
							}
							int cursorOffset = actual.length();
							if (actual.endsWith("\"\"")) cursorOffset--;
							rtn.add(new CompletionProposal(
									actual,
									start,
									replaceLength,
									cursorOffset,
									null,
									attributeName,
									null,
									null));
						}
					}
					return getProposalArray(rtn);
				}
				else if (state == CursorState.STATE_WAITING_FOR_ATTRIBUTE_VALUE_QUOTE) {
					char c = doc.getChar(offset);
					int i=offset;
					try {
						while (Character.isWhitespace(doc.getChar(++i))) {}
						if (doc.getChar(i) == '\"') {
							// trim the spaces
							return new ICompletionProposal[] {new CompletionProposal("\"", offset, i-offset+1, 1)};
						}
					}
					catch (BadLocationException e) {}
					return null;
				}
				else if (state == CursorState.STATE_ATTRIBUTE_VALUE) {
					Attribute attribute = currentNode.getAttribute(offset);
					if (null == attribute) return null;
					
					int start = attribute.getValueOffset();
					
					StringBuffer postStr = new StringBuffer();
					int quoteIndex = -1;
					int index = start + attribute.getValue().length();
					try {
						char c = doc.getChar(index);
						while (true) {
							if (c == '\"') {
								quoteIndex = index;
								break;
							}
							else if (Character.isWhitespace(c)) {
								break;
							}
							c = doc.getChar(++index);
						}
					}
					catch (BadLocationException e) {
					}
					if (quoteIndex == -1) {
						postStr.append("\"");
					}
					int replaceLength = attribute.getValue().length();
					int lastCharIndex = -1;
					boolean seenBreaker = false;
					for (int i=start; i<start+ attribute.getValue().length(); i++) {
						char c = doc.getChar(i);
						if (Character.isWhitespace(c)) seenBreaker = true;
						else if (c == '=') {
							if (lastCharIndex > 0) replaceLength = lastCharIndex - start;
							else replaceLength = 0;
							break;
						}
						else if (!seenBreaker) lastCharIndex = i;
					}
								
					String prefixUpper = doc.get(start, offset-start).toUpperCase();
					if (attribute.getName().startsWith("on")) return getJavascriptProposals(doc, prefixUpper, offset);
					else {
					    String[] proposalArr = null;
					    if (attribute.getName().equalsIgnoreCase("class")) {
					        proposalArr = getCssProposals(file, doc, offset);
					    }
					    else {
					        proposalArr = XMLSuggestor.getAttributeValueSuggestions(currentNode.getName(), attribute.getName());
					    }
						if (null == proposalArr) return null;
						List rtn = new ArrayList();
						for (int i=0; i<proposalArr.length; i++) {
							String attributeValue = proposalArr[i];
							String actual = attributeValue;
							if (postStr.length() > 0) {
								actual += postStr.toString();
							}
							if (attributeValue.toUpperCase().startsWith(prefixUpper)) {
								rtn.add(new CompletionProposal(
										actual,
										start,
										replaceLength,
										actual.length(),
										null,
										attributeValue,
										null,
										null));
							}
						}
						return getProposalArray(rtn);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		return null;
	}
	protected Image getImage(Template template) {
		return null;
	}
	protected Template[] getTemplates(String contextTypeId) {
		return null;
	}
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[]{'/', '<', '\"'};
	}
	
	private ICompletionProposal[] getProposalArray (List proposals) {
		if (null == proposals) return null;
		Collections.sort(proposals, new CompletionProposalComparator());
		ICompletionProposal[] proposalArr = new ICompletionProposal[proposals.size()];
		int index = 0;
		for (Iterator i=proposals.iterator(); i.hasNext(); ) {
			proposalArr[index++] = (ICompletionProposal) i.next();
		}
		return proposalArr;
	}

	private String[] getCssProposals (IFile file, IDocument doc, int offset) {
	    try {
	    	List projectCssFiles = ConfigurationManager.getInstance(file.getProject()).getCSSFiles();
		    List cssFiles = new ArrayList(projectCssFiles.size());
		    cssFiles.addAll(projectCssFiles);
		    addLocalCSS(doc, cssFiles, offset);
		    List suggestions = new ArrayList();
		    for (Iterator i=cssFiles.iterator(); i.hasNext(); ) {
		        CSSFile css = (CSSFile) i.next();
		        for (int j=0; j<css.getStyles().length; j++) {
		            suggestions.add(css.getStyles()[j].name);
		        }
		    }
		    return (String[]) suggestions.toArray(new String[suggestions.size()]);
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private ICompletionProposal[] getJavascriptProposals(IDocument doc, String aPrefix, int anOffset) {
	    try {
		    List projectJavascriptFiles = ConfigurationManager.getInstance(file.getProject()).getJavascriptFiles();
		    List javascriptFiles = new ArrayList(projectJavascriptFiles.size() + 1);
		    javascriptFiles.addAll(projectJavascriptFiles);
		    addLocalJavascript(doc, javascriptFiles, anOffset);
		    
			int newStart = -1;
			int newEnd = -1;
			int wordEnd = -1;
			boolean whitespaceEncountered = false;
			for (int i=anOffset; i<doc.getLength(); i++) {
				char c = doc.getChar(i);
				if (c == ')') {
					newEnd = i;
					break;
				}
				else if (c == '\"') {
				    newEnd = i-1;
				    break;
				}
				else {
					if (!Character.isWhitespace(c)) {
						if (whitespaceEncountered) break;
					}
					else {
						if (!whitespaceEncountered) {
							wordEnd = i;
							whitespaceEncountered = true;
						}
						else break;
					}
				}
			}
			if (null != aPrefix && aPrefix.length() > 0) {
				for (int i=anOffset-1; i>=anOffset-aPrefix.length()-1; i--) {
					char c = doc.getChar(i);
					if (Character.isWhitespace(c) || c == ';' || c == '"') {
						newStart = i + 1;
						break;
					}
					else if (c == ')') return new ICompletionProposal[0];
				}
			}

			String testPrefix = aPrefix;
			if (newEnd > -1 && newStart > -1) {
			    if (newStart > newEnd) newStart = newEnd;
			    if (anOffset < newEnd) {
			        testPrefix = doc.get(newStart, anOffset - newStart).toUpperCase();
			    }
			    else {
			        testPrefix = doc.get(newStart, newEnd - newStart).toUpperCase();
			    }
			}
			if (null != testPrefix) {
			    testPrefix = testPrefix.toUpperCase();
			    int index = testPrefix.indexOf("(");
			    if (index >= 0) {
			        testPrefix = testPrefix.substring(0, index);
			    }
			}
		    List proposals = new ArrayList();
		    for (Iterator i=javascriptFiles.iterator(); i.hasNext(); ) {
		        JavascriptFile jf = (JavascriptFile) i.next();
		        for (int j=0; j<jf.getFunctions().length; j++) {
		            JavascriptFunction func = jf.getFunctions()[j];
			        String name = func.name.toUpperCase();
					if (name.startsWith(testPrefix)) {
						StringBuffer insert = new StringBuffer();
						insert.append(func.name);
						insert.append("(");
						for (int k = 0; k < func.parameters.length; k++) {
							if (k > 0) insert.append(", ");
							insert.append("[" + func.parameters[k] + "]");
						}
						insert.append(");");
						
						StringBuffer buffer = new StringBuffer();
						buffer.append(func.name);
						buffer.append('(');
		
						if (func.parameters.length == 0) {
							buffer.append(')');
						} else {
							for (int k = 0; k < func.parameters.length; k++) {
								buffer.append(func.parameters[k]);
								if (k < (func.parameters.length - 1)) {
									buffer.append(", ");
								}
							}
		
							buffer.append(')');
						}
						int cursorPos = func.name.length() + 1;
						if (null != jf.file) {
							buffer.append(" - ");
							buffer.append(jf.file.getName());
						}
						int theOffset = anOffset;
						if (newStart > -1) {
							if (newEnd > -1) {
								if (aPrefix.length() == 0) newEnd ++;
								proposals.add(new CompletionProposal(func.name + "()", newStart, newEnd-newStart+1, cursorPos,
										null, buffer.toString(), null, null));
							}
							else {
								proposals.add(new CompletionProposal(insert.toString(), newStart, aPrefix.length()-(newStart-anOffset), cursorPos,
										null, buffer.toString(), null, null));
							}
						}
						else {
							if (newEnd > -1) {
								if (aPrefix.length() == 0) newEnd ++;
								proposals.add(new CompletionProposal(func.name, anOffset, aPrefix.length()+(newEnd-anOffset), cursorPos,
										null, buffer.toString(), null, null));
							}
							else {
								proposals.add(new CompletionProposal(insert.toString(), anOffset, aPrefix.length(), cursorPos,
										null, buffer.toString(), null, null));
							}
						}
					}
		        }
		    }
			Collections.sort(proposals, PROPOSAL_COMPARATOR);
			return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
	    }
	    catch (Exception e) {
	        return null;
	    }
	}

	public void addLocalJavascript (IDocument doc, List files, int offset) {
	    try {
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
			
			List javascriptFunctions = new ArrayList();
			for (Iterator i=regions.iterator(); i.hasNext(); ) {
				ITypedRegion region = (ITypedRegion) i.next();
				if (region.getType().equals(PartitionScanner.XML_TAG)) {
					if (isNodeHeader(region, doc)) {
						Node n = new Node(null, region, doc);
						if (null != n.getName() && n.getName().equalsIgnoreCase("script")) {
						    int index = doc.get().indexOf("</", n.getNameStart());
						    if (index > 0) {
						        JavascriptFunction[] functions = JavascriptParser.parse(doc.get(n.getNameStart(), index-n.getNameStart()));
						        for (int j=0; j<functions.length; j++) {
						            javascriptFunctions.add(functions[j]);
						        }
						    }
						}
					}
				}
			}
			if (javascriptFunctions.size() > 0) {
			    files.add(new JavascriptFile((JavascriptFunction[]) javascriptFunctions.toArray(new JavascriptFunction[javascriptFunctions.size()])));
			}
	    }
	    catch (Exception e) {
	    }
	}

	public void addLocalCSS (IDocument doc, List files, int offset) {
	    try {
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
			
			List cssStyles = new ArrayList();
			for (Iterator i=regions.iterator(); i.hasNext(); ) {
				ITypedRegion region = (ITypedRegion) i.next();
				if (region.getType().equals(PartitionScanner.XML_TAG)) {
					if (isNodeHeader(region, doc)) {
						Node n = new Node(null, region, doc);
						if (null != n.getName() && n.getName().equalsIgnoreCase("style")) {
						    int index = doc.get().indexOf("</", n.getNameStart());
						    if (index > 0) {
						        CSSStyle[] styles = CSSParser.parse(doc.get(n.getNameStart(), index-n.getNameStart()));
						        for (int j=0; j<styles.length; j++) {
						            cssStyles.add(styles[j]);
						        }
						    }
						}
					}
				}
			}
			if (cssStyles.size() > 0) {
			    files.add(new CSSFile((CSSStyle[]) cssStyles.toArray(new CSSStyle[cssStyles.size()])));
			}
	    }
	    catch (Exception e) {
	    }
	}
	
	public static final char CHAR_SLASH = '/';
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
	
	public class CompletionProposalComparator implements Comparator {
		
		public int compare(Object o1, Object o2) {
			if (null == o1) return -1;
			else if (null == o2) return 1;
			else return (((ICompletionProposal) o1).getDisplayString().compareTo(((ICompletionProposal) o2).getDisplayString()));
		}
	}

	private static Comparator PROPOSAL_COMPARATOR = new Comparator() {

		public int compare(Object aProposal1, Object aProposal2) {
			String text1 = ((CompletionProposal) aProposal1).getDisplayString();
			String text2 = ((CompletionProposal) aProposal2).getDisplayString();

			return text1.compareTo(text2);
		}

		public boolean equals(Object aProposal) {
			return false;
		}
	};
}