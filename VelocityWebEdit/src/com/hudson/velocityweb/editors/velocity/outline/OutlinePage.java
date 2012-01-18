package com.hudson.velocityweb.editors.velocity.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.hudson.velocityweb.Plugin;
import com.hudson.velocityweb.dialogs.NodeFilterDialog;
import com.hudson.velocityweb.editors.velocity.Editor;
import com.hudson.velocityweb.editors.velocity.completion.DirectiveFactory;
import com.hudson.velocityweb.editors.velocity.completion.IDirective;
import com.hudson.velocityweb.editors.velocity.completion.xml.CursorState;
import com.hudson.velocityweb.editors.velocity.completion.xml.Node;
import com.hudson.velocityweb.editors.velocity.completion.xml.NodeEvaluator;
import com.hudson.velocityweb.manager.ConfigurationManager;

public class OutlinePage extends ContentOutlinePage implements MouseListener {
	ISourceViewer sourceViewer;
	Editor editor;
	
	private Action toggleViewAction;
	private Action filterAction;
	boolean isShowVelocity = true;
	
	
	public OutlinePage (ISourceViewer sourceViewer, Editor editor) {
		this.sourceViewer = sourceViewer;
		this.editor = editor;
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new ContentProvider(this));
		viewer.setLabelProvider(new LabelProvider());
		viewer.setInput(sourceViewer);
		viewer.getControl().addMouseListener(this);
	}

	public void refresh () {
	    getTreeViewer().refresh();
	    try {
		    if (getTreeViewer().getTree().getItems().length < 3) {
		    	getTreeViewer().expandToLevel(4);
		    }
	    }
	    catch (Exception e) {}
	    select(editor.getCursorOffset());
	}

	public void setActionBars(IActionBars actionBars) {
	    toggleViewAction = new Action () {
	        public void run () {
	            if (isShowVelocity) {
	                setImageDescriptor(Plugin.getDefault().getImageDescriptor("toggle_html.gif"));
	                setToolTipText("Switch to Velocity View");
	                filterAction.setEnabled(true);
	            }
	            else {
	                setImageDescriptor(Plugin.getDefault().getImageDescriptor("toggle_velocity.gif"));
	                setToolTipText("Switch to HTML View");
	                filterAction.setEnabled(false);
	            }
	            isShowVelocity = !isShowVelocity;
	            refresh();
	        }
	    };
	    toggleViewAction.setImageDescriptor(Plugin.getDefault().getImageDescriptor("toggle_velocity.gif"));
	    toggleViewAction.setToolTipText("Switch to HTML View");
	    actionBars.getToolBarManager().add(toggleViewAction);
		super.setActionBars(actionBars);

	    filterAction = new Action () {
	        public void run () {
	            NodeFilterDialog dialog = new NodeFilterDialog(getControl().getShell(), editor.getProject());
	            if (IDialogConstants.OK_ID == dialog.open()) {
	            	refresh();
	            }
	        }
	    };
	    filterAction.setImageDescriptor(Plugin.getDefault().getImageDescriptor("filter.gif"));
	    filterAction.setToolTipText("Filter XML Nodes");
	    filterAction.setEnabled(false);
	    actionBars.getToolBarManager().add(filterAction);
		super.setActionBars(actionBars);
	}
	
	public void select (int offset) {
		if (offset < -1) return;
		else offset = offset - 1;
		IDocument doc = sourceViewer.getDocument();
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

			if (isShowVelocity) {
				Stack directiveStack = new Stack();
				IDirective lastDirective = null;
	
				ITypedRegion region = null;
				int lastOffset = -1;
				for (Iterator i=typedOffsets.iterator(); i.hasNext(); ) {
				    int tOffset = ((Integer) i.next()).intValue();
				    region = doc.getPartition(tOffset);
				    if (region != null && region.getOffset() > offset) break;
					if (tOffset != lastOffset) {
						if (DirectiveFactory.isEndDirective(region.getType())) {
							// remove from the directiveStack
							if (directiveStack.size() > 0) {
								directiveStack.pop();
							}
						}
						else {
							IDirective directive = DirectiveFactory.getDirective(region.getType(), region, doc);
							lastDirective = directive;
							if (null != directive) {
							    IDirective d = null;
							    if (directiveStack.size() > 0) d =(IDirective) directiveStack.peek();
							    if (null != d) d.addChildDirective(directive);
								if (directive.requiresEnd()) {
								    directiveStack.push(directive);
								    
								}
							}
						}
						lastOffset = tOffset;
					}
				}
				if (null != lastDirective) {
				    if (lastDirective.getOffset() <= offset && lastDirective.getOffset() + lastDirective.getLength() >= offset) {
				        getTreeViewer().setSelection(new StructuredSelection(lastDirective), true);
				    }
				}
				else if (directiveStack.size() > 0) {
				    IDirective directive = (IDirective) directiveStack.peek();
				    getTreeViewer().setSelection(new StructuredSelection(directive), true);
				}
			}
			else {
				LocalNodeEvaluator lne = new LocalNodeEvaluator(offset);
				CursorState.evaluate(doc, lne);
				Node node = lne.getNode();
				if (null != node) {
					getTreeViewer().setSelection(new StructuredSelection(node), true);
				}
			}
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
	}

    public void mouseDoubleClick(MouseEvent e) {

    }
    public void mouseDown(MouseEvent e) {

    }
    public void mouseUp(MouseEvent e) {
        TreeItem treeItem = getTreeViewer().getTree().getItem(new Point(e.x, e.y));
        if (null != treeItem) {
	        if (treeItem.getData() instanceof IDirective) {
	            IDirective directive = (IDirective) treeItem.getData();
	            editor.selectAndReveal(directive.getOffset(), directive.getLength());
	        }
	        else if (treeItem.getData() instanceof Node) {
			    Node node = (Node) treeItem.getData();
			    editor.selectAndReveal(node.getOffsetStart(), node.getOffsetEnd()-node.getOffsetStart());
	        }
        }
     }
    
    public class LocalNodeEvaluator implements NodeEvaluator {
    	
    	private Node selectNode = null;
        private Stack nodeStack = new Stack();
        private int offset;
    	
        public LocalNodeEvaluator (int offset) {
        	this.offset = offset;
        }
        
        public Node getNode () {
        	if (null != selectNode) return selectNode;
        	else if (nodeStack.size() > 0) return (Node) nodeStack.peek();
        	else return null;
        }
        
    	public boolean flatNode(Node node) {
    		if (node.getOffsetStart() > offset) return false;
            if (node.getOffsetStart() <= offset && node.getOffsetEnd() >= offset) {
            	selectNode = node;
            	return false;
            }
            return true;
		}
		public boolean popNode(Node node) {
			if (node.getOffsetStart() > offset) return false;
            if (node.getOffsetStart() <= offset && node.getOffsetEnd() >= offset) {
            	return false;
            }
            else {
		        if (ConfigurationManager.getInstance(editor.getProject()).isDTDFilterElement(node.getName())
		        		|| !ConfigurationManager.getInstance(editor.getProject()).isXMLFilteringEnabled()) {
		            if (nodeStack.size() > 0 && !node.isPseudoFlatNode()) {
		                Node n = (Node) nodeStack.peek();
		                if (n.getName().equals(node.getName())) nodeStack.pop();
		            }
		        }
		        return true;
            }
		}
		public boolean pushNode(Node node) {
			if (node.getOffsetStart() > offset) return false;
            if (node.getOffsetStart() <= offset && node.getOffsetEnd() >= offset) {
            	selectNode = node;
            	return false;
            }
	        if (ConfigurationManager.getInstance(editor.getProject()).isDTDFilterElement(node.getName())
	        		|| !ConfigurationManager.getInstance(editor.getProject()).isXMLFilteringEnabled()) {
	            if (node.isPseudoFlatNode()) {
	                flatNode(node);
	            }
	            else {
	                boolean doContinue = true;
	                if (doContinue) {
			            nodeStack.push(node);
	                }
	            }
	        }
	        return true;
		}
    }
}