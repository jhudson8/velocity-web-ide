package com.hudson.velocityweb.editors.velocity.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.hudson.velocityweb.editors.velocity.PartitionScanner;
import com.hudson.velocityweb.editors.velocity.completion.DirectiveFactory;
import com.hudson.velocityweb.editors.velocity.completion.IDirective;
import com.hudson.velocityweb.editors.velocity.completion.xml.CursorState;
import com.hudson.velocityweb.editors.velocity.completion.xml.Node;
import com.hudson.velocityweb.editors.velocity.completion.xml.NodeEvaluator;
import com.hudson.velocityweb.manager.ConfigurationManager;

/**
 * @author Joe Hudson
 */
public class ContentProvider implements ITreeContentProvider, NodeEvaluator {

    private OutlinePage outlinePage;
    
    public ContentProvider (OutlinePage outlinePage) {
        this.outlinePage = outlinePage;
    }
    
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IDirective) {
            return ((IDirective) parentElement).getChildDirectives().toArray();
        }
        else if (parentElement instanceof Node) {
            return ((Node) parentElement).getChildNodes().toArray();
        }
        else return null;
    }
    public Object getParent(Object element) {
        if (element instanceof IDirective) {
            return ((IDirective) element).getParent();
        }
        else if (element instanceof Node) {
            return ((Node) element).getParent();
        }
        else return null;
    }
    public boolean hasChildren(Object element) {
        if (element instanceof IDirective) {
            return ((IDirective) element).getChildDirectives().size() > 0;
        }
        else if (element instanceof Node) {
            return ((Node) element).getChildNodes().size() > 0;
        }
        else return false;
    }
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof ISourceViewer) {
            ISourceViewer viewer = (ISourceViewer) inputElement;
            
            if (outlinePage.isShowVelocity) {
				IDocument doc = viewer.getDocument();
				List typedOffsets = new ArrayList();
				String[] categories = doc.getPositionCategories();
				
				try {
					for (int i=0; i<categories.length; i++) {
						Position[] positions = doc.getPositions(categories[i]);
						for (int j=0; j<positions.length; j++) {
							typedOffsets.add(new Integer(positions[j].getOffset()));
						}
					}
					Collections.sort(typedOffsets);
	
					List parentDirectives = new ArrayList();
					Stack directiveStack = new Stack();
	
					ITypedRegion region = null;
					IDirective lastDirective = null;
					int lastOffset = -1;
					for (Iterator i=typedOffsets.iterator(); i.hasNext(); ) {
						int tOffset = ((Integer) i.next()).intValue();
						if (tOffset > lastOffset) {
							region = doc.getPartition(tOffset);
							if (region.getOffset() > lastOffset) {
								if (DirectiveFactory.isEndDirective(region.getType())) {
									// remove from the directiveStack
									if (directiveStack.size() > 0) {
										directiveStack.pop();
									}
								}
								else {
									IDirective directive = DirectiveFactory.getDirective(region.getType(), region, doc);
									if (null != directive) {
									    IDirective d = null;
									    if (directiveStack.size() > 0) d =(IDirective) directiveStack.peek();
									    if (null != d) d.addChildDirective(directive);
									    else parentDirectives.add(directive);
										if (directive.requiresEnd()) {
										    directiveStack.push(directive);
										    
										}
										lastDirective = directive;
									}
								}
							}
							lastOffset = tOffset;
						}
					}
					return parentDirectives.toArray();
				}
				catch (Exception e) {
				    e.printStackTrace();
				}
            }
            else {
                nodeStack = new Stack();
                parentNodes = new ArrayList();
                regions = null;
                CursorState cursorState = CursorState.evaluate(viewer.getDocument(), this);
                return parentNodes.toArray();
            }
        }
        return null;
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
    
    private List parentNodes = null;
    private Stack nodeStack = null;
    private List regions = null;

    public boolean flatNode(Node node) {
        if (ConfigurationManager.getInstance(outlinePage.editor.getProject()).isDTDFilterElement(node.getName())
        		|| !ConfigurationManager.getInstance(outlinePage.editor.getProject()).isXMLFilteringEnabled()) {
	        if (nodeStack.size() == 0) {
	            parentNodes.add(node);
	        }
	        else {
	            ((Node) nodeStack.peek()).addChildNode(node);
	        }
        }
        return true;
    }
    public boolean popNode(Node node) {
        if (ConfigurationManager.getInstance(outlinePage.editor.getProject()).isDTDFilterElement(node.getName())
        		|| !ConfigurationManager.getInstance(outlinePage.editor.getProject()).isXMLFilteringEnabled()) {
            if (nodeStack.size() > 0 && !node.isPseudoFlatNode()) {
                boolean doContinue = true;
                int lastElse = -1;
                ITypedRegion region = getLowestRegion(node.getOffsetStart(), PartitionScanner.ELSE_PARTITION);
                if (null != region) lastElse = region.getOffset();
                region = getLowestRegion(node.getOffsetStart(), PartitionScanner.ELSE_IF_PARTITION);
                if (null != region && region.getOffset() > lastElse) lastElse = region.getOffset();
                if (lastElse >= 0) {
                	region = getHighestRegion(lastElse, PartitionScanner.IF_END_PARTITION);
                	if (null != region) {
                		if (region.getOffset() > node.getOffsetStart()) {
                			try {
                				int lastElseLine = outlinePage.sourceViewer.getDocument().getLineOfOffset(lastElse);
                				int lastEndLine = outlinePage.sourceViewer.getDocument().getLineOfOffset(region.getOffset());
                				if (lastEndLine >= 0 && lastEndLine < lastElseLine + 5) doContinue = false;
                			}
                			catch (BadLocationException e) {}
                		}
                	}
                }
                if (doContinue) {
	                Node n = (Node) nodeStack.peek();
	                if (n.getName().equals(node.getName())) nodeStack.pop();
	                else {
	                	while (nodeStack.size() > 0) {
	                		n = (Node) nodeStack.peek();
	                		if (n.isPseudoFlatNode()) nodeStack.pop();
	                		if (n.getName().equals(node.getName())) break;
	                		else if (!n.isPseudoFlatNode()) break;
	                	}
	                }
                }
            }
        }
        return true;
    }
    public boolean pushNode(Node node) {
        if (ConfigurationManager.getInstance(outlinePage.editor.getProject()).isDTDFilterElement(node.getName())
        		|| !ConfigurationManager.getInstance(outlinePage.editor.getProject()).isXMLFilteringEnabled()) {
            boolean doContinue = true;
            int lastElse = -1;
            ITypedRegion region = getLowestRegion(node.getOffsetStart(), PartitionScanner.ELSE_PARTITION);
            if (null != region) lastElse = region.getOffset();
            region = getLowestRegion(node.getOffsetStart(), PartitionScanner.ELSE_IF_PARTITION);
            if (null != region && region.getOffset() > lastElse) lastElse = region.getOffset();
            if (lastElse >= 0) {
            	region = getHighestRegion(lastElse, PartitionScanner.IF_END_PARTITION);
            	if (null != region) {
            		if (region.getOffset() > node.getOffsetStart()) {
            			try {
            				int lastElseLine = outlinePage.sourceViewer.getDocument().getLineOfOffset(lastElse);
            				int lastEndLine = outlinePage.sourceViewer.getDocument().getLineOfOffset(region.getOffset());
            				if (lastEndLine < lastElseLine + 4) doContinue = false;
            			}
            			catch (BadLocationException e) {}
            		}
            	}
            }
            if (doContinue) {
	            if (nodeStack.size() == 0) {
	                parentNodes.add(node);
	            }
	            else {
	                ((Node) nodeStack.peek()).addChildNode(node);
	            }
	            nodeStack.push(node);
            }
        }
        return true;
    }

    private ITypedRegion getHighestRegion(int topOffset, String regionType) {
    	ITypedRegion region = null;
    	ITypedRegion selectedRegion = null;
    	for (int i=getRegions().size()-1; i>=0; i--) {
    		region = (ITypedRegion) getRegions().get(i);
    		if (region.getOffset() < topOffset) return selectedRegion;
    		if (region.getType().equals(regionType)) selectedRegion = region;
    	}
    	return selectedRegion;
    }

    private ITypedRegion getLowestRegion(int bottomOffset, String regionType) {
    	ITypedRegion region = null;
    	ITypedRegion selectedRegion = null;
    	for (int i=0; i<getRegions().size(); i++) {
    		region = (ITypedRegion) getRegions().get(i);
    		if (region.getOffset() > bottomOffset) return selectedRegion;
    		if (region.getType().equals(regionType)) selectedRegion = region;
    	}
    	return selectedRegion;
    }

    private List getRegions() {
    	try {
	    	if (null == regions) {
				IDocument doc = outlinePage.sourceViewer.getDocument();
				List typedOffsets = new ArrayList();
				String[] categories = doc.getPositionCategories();
	    		regions = new ArrayList();
				for (int i=0; i<categories.length; i++) {
					Position[] positions = doc.getPositions(categories[i]);
					for (int j=0; j<positions.length; j++) {
						typedOffsets.add(new Integer(positions[j].getOffset()));
					}
				}
				Collections.sort(typedOffsets);
	
				int lastOffset = -1;
				for (Iterator i=typedOffsets.iterator(); i.hasNext(); ) {
					int tOffset = ((Integer) i.next()).intValue();
					if (tOffset != lastOffset) {
						regions.add(doc.getPartition(tOffset));
					}
				}
	    	}
	    	return regions;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
}