package com.hudson.velocityweb.editors.velocity;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.hudson.velocityweb.Plugin;
import com.hudson.velocityweb.editors.velocity.completion.DirectiveFactory;
import com.hudson.velocityweb.editors.velocity.completion.DocumentProvider;
import com.hudson.velocityweb.editors.velocity.completion.IDirective;
import com.hudson.velocityweb.editors.velocity.completion.xml.CursorState;
import com.hudson.velocityweb.editors.velocity.completion.xml.Node;
import com.hudson.velocityweb.editors.velocity.completion.xml.NodeEvaluator;
import com.hudson.velocityweb.editors.velocity.outline.OutlinePage;
import com.hudson.velocityweb.manager.ConfigurationManager;
import com.hudson.velocityweb.util.EditorUtil;

public class Editor extends TextEditor implements ITextListener, MouseListener, KeyListener, FocusListener {

	private ColorManager colorManager;
	private long nextAllowedValidation = Long.MIN_VALUE;
	private long MIN_VALIDATION_LIMIT = 3 * 1000;
	private boolean WAITING_VALIDATION = false;
	private boolean VALIDATING = false;

	public Editor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new Configuration(colorManager, this));
		setDocumentProvider(new DocumentProvider());
	}

	public void dispose() {
		ConfigurationManager.getInstance(getProject()).clearCache();
		colorManager.dispose();
		super.dispose();
	}

	public ITextViewer getViewer() {
		return getSourceViewer();
	}

	private OutlinePage outlinePage;

	public Object getAdapter(Class adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (outlinePage == null) {
				outlinePage = new OutlinePage(getSourceViewer(), this);
			}
			return outlinePage;
		}
		return super.getAdapter(adapter);
	}

	protected void createActions() {
		super.createActions();
		// Add content assist propsal action
		ContentAssistAction action = new ContentAssistAction(Plugin
				.getDefault().getResourceBundle(),
				"VelocityEditor.ContentAssist", this);
		action
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("Velocity.ContentAssist", action);
		action.setEnabled(true);
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		 getSourceViewer().addTextListener(this);
		 getSourceViewer().getTextWidget().addMouseListener(this);
		 getSourceViewer().getTextWidget().addKeyListener(this);
		 getSourceViewer().getTextWidget().addFocusListener(this);
	}

	/**
	 * Return the project associated with this resource or null if a workspace
	 * resource
	 */
	public IProject getProject() {
		if (getEditorInput() instanceof FileEditorInput) {
			return ((FileEditorInput) getEditorInput()).getFile().getProject();
		} else
			return null;
	}

	public IFile getFile() {
		if (getEditorInput() instanceof FileEditorInput) {
			return ((FileEditorInput) getEditorInput()).getFile();
		} else
			return null;
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorSaved()
	 */
	protected void editorSaved() {
		validateInput(true);
		super.editorSaved();
	}

	public IRegion getRegion(int offset) throws BadLocationException {
		try {
			return getSourceViewer().getDocument().getPartition(offset-1);
		}
		catch (BadLocationException e) {
			return null;
		}
	}

	public void mouseDoubleClick(MouseEvent e) {
	}

	public void mouseDown(MouseEvent event) {
	}

	public void mouseUp(MouseEvent e) {
		outlinePage.select(getCursorOffset());
	}

	public int getCursorOffset() {
		ISelection selection = getSourceViewer().getSelectionProvider()
				.getSelection();
		if (selection instanceof ITextSelection) {
			return ((ITextSelection) selection).getOffset();
		} else
			return -1;
	}

	private String[] chars = { "$", "\n", ")", "(", "\"", "d", "=", "<", ">", "e", ",", "]", "[", ".", "}", "{" };

	public void textChanged(TextEvent event) {
		try {
			if (null != event.getText()) {
				if (event.getText().length() == 0) {
					validateInput(false);
					refreshOutline();
				}
				if (event.getText().length() == 1) {
					for (int i = 0; i < chars.length; i++) {
						if (event.getText().equals(chars[i])) {
							validateInput(false);
							break;
						}
					}
					try {
						int offset = getCursorOffset();
						ITypedRegion region = (ITypedRegion) getRegion(offset-1);
						IDirective directive = DirectiveFactory.getDirective(region.getType(), region, getSourceViewer().getDocument());
						if (null != directive) {
							refreshOutline();
							return;
						}
					}
					catch (Exception e1) {}
					if (event.getText().equals("\n") || event.getText().equals("d")
							|| event.getText().equals(")")
							|| event.getText().equals(">")
							|| event.getText().equals("\"")) {
						refreshOutline();
					}
				} else {
					for (int i = 0; i < chars.length; i++) {
						if (event.getText().indexOf(chars[i]) >= 0) {
							validateInput(false);
							refreshOutline();
							break;
						}
					}
					if (event.getText().indexOf("\n") >= 0
							|| event.getText().indexOf(")") >= 0) {
						refreshOutline();
					}
				}
			} else {
				refreshOutline();
			}
		}
		catch (Throwable t) {}
	}
	
	private String getRegionSpacePrefix (IRegion region) {
		int headerOffset = region.getOffset();
		StringBuffer sb = new StringBuffer();
		try {
			for (int i=headerOffset-1; i>=0; i--) {
				char c = getSourceViewer().getDocument().getChar(i);
				if (c == '\n' || c == '\r') {
					return sb.toString();
				}
				else if (Character.isWhitespace(c)) {
					sb.append(c);
				}
				else {
					sb.append(' ');
				}
			}
		}
		catch (BadLocationException e) {
		}
		return "";
	}

	private static final VelocityContext vc = new VelocityContext();

	private void refreshOutline () {
		outlinePage.refresh();
		outlinePage.select(getCursorOffset());
	}

	public void validateInput(boolean force) {
		if (force || (!VALIDATING && System.currentTimeMillis() > nextAllowedValidation)) {
			VALIDATING = true;
			InputValidator inputValidator = new InputValidator ();
			inputValidator.start();
		}
		else {
			WAITING_VALIDATION = true;
		}
	}

	public void focusGained(FocusEvent e) {
		ConfigurationManager.getInstance(getProject()).clearProjectClassLoader();
	}

	public void focusLost(FocusEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		try {
			if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP
					|| e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT
					|| e.keyCode == SWT.PAGE_UP || e.keyCode == SWT.PAGE_DOWN) {
				outlinePage.select(getCursorOffset());
			}
			else if (e.character == '\n' || e.character == '\r') {
				int offset = getCursorOffset();
				ITypedRegion region = null;
				try {
					region = (ITypedRegion) getRegion(offset);
					if (null != region ) {
						IDirective directive = DirectiveFactory.getDirective(region.getType(), region, getSourceViewer().getDocument());
						if (null != directive) { return; }
						else {
							if (region.getType().equals(PartitionScanner.XML_TAG)) return;
						}
					}
				}
				catch (BadLocationException e1) {}
				try {
					int previousLine = getSourceViewer().getDocument().getLineOfOffset(offset)-1;
					int prevOffset = getSourceViewer().getDocument().getLineOffset(previousLine);
					StringBuffer sb = new StringBuffer();
					for (int i=prevOffset; i<getSourceViewer().getDocument().getLength(); i++) {
						char c = getSourceViewer().getDocument().getChar(i);
						if (Character.isWhitespace(c)) sb.append(c);
						else break;
					}
					
					boolean newlineReached = false;
					for (int i=offset-1; i>=0; i--) {
						char c = getSourceViewer().getDocument().getChar(i);
						if (c == '\n') {
							if (newlineReached) return;
							else newlineReached = true;
						}
						if (!Character.isWhitespace(c)) {
							region = getSourceViewer().getDocument().getPartition(i-1);
							if (null != region) {
								if (region.getType().equals(PartitionScanner.XML_TAG)) {
									if (isNodeHeader(region, getSourceViewer().getDocument())) {
										Node node = new Node(null, region, getSourceViewer().getDocument());
										if (!node.isPseudoFlatNode()) {
											getSourceViewer().getDocument().replace(offset, 0, "\t");
											getSourceViewer().setSelectedRange(offset+1, 0);
											if (isNodeOffset()) {
												String headerSpaces = getRegionSpacePrefix(region);
												if (null == headerSpaces) headerSpaces = "";
												String nl = "\n";
												if (e.character == '\r') nl = "\r\n";
												getSourceViewer().getDocument().replace(offset+1, 0, nl + headerSpaces + "</" + node.getName() + ">");
											}
										}
										return;
									}
								}
								else {
									if (DirectiveFactory.isEndDirective(region.getType())) return;
									IDirective directive = DirectiveFactory.getDirective(region.getType(), region, getSourceViewer().getDocument());
									if (null != directive) {
										if (directive.isStackScope()) {
											getSourceViewer().getDocument().replace(offset, 0, "\t");
											getSourceViewer().setSelectedRange(offset+1, 0);
											if (isDirectiveOffset()) {
												String headerSpaces = getRegionSpacePrefix(region);
												String nl = "\n";
												if (e.character == '\r') nl = "\r\n";
												getSourceViewer().getDocument().replace(offset+1, 0, nl + headerSpaces + "#end");
											}
											return;
										}
									}
								}
							}
							break;
						}
					}
				}
				catch (BadLocationException e1) {}
			}
			else if (e.character == ')') {
				int offset = getCursorOffset();
				try {
					if (getSourceViewer().getDocument().getLength() > offset && getSourceViewer().getDocument().getChar(offset) == ')') {
						ITypedRegion region = (ITypedRegion) getRegion(offset-1);
						IDirective directive = DirectiveFactory.getDirective(region.getType(), region, getSourceViewer().getDocument());
						if (null != directive) {
							int stackSize = 0;
							if (region.getOffset() + region.getLength() == offset)
								getSourceViewer().getDocument().replace(offset, 1, "");
						}
					}
				}
				catch (Exception e1) {}
			}
			else if (e.character == '(') {
				int offset = getCursorOffset();
				try {
					int lineOfOffset = getSourceViewer().getDocument().getLineOfOffset(offset);
					int lineOffset = getSourceViewer().getDocument().getLineOffset(lineOfOffset);
					boolean hitSpace = false;
					boolean hitContentAterSpace = false;
					for (int i=offset-2; i>=lineOffset; i--) {
						char c = getSourceViewer().getDocument().getChar(i);
						if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || c=='#') {
							if (c == '#') {
								if (getSourceViewer().getDocument().getLength() == offset
										|| getSourceViewer().getDocument().getChar(offset) == '\n'
										|| getSourceViewer().getDocument().getChar(offset) == '\r') {
									getSourceViewer().getDocument().replace(offset, 0, ")");
								}
							}
							else if (Character.isWhitespace(c)) {
								hitSpace = true;
								if (hitContentAterSpace) return;
							}
							else {
								if (hitSpace) hitContentAterSpace = true;
							}
						}
						else return;
					}
				}
				catch (Exception e1) {}
			}
		}
		catch (Throwable t) {}
	}

	public void keyReleased(KeyEvent e) {
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

	public boolean isDirectiveOffset () {
		Stack directiveStack = new Stack();
		IDocument doc = getSourceViewer().getDocument();
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

			ITypedRegion region = null;
			int lastOffset = -1;
			for (Iterator i=typedOffsets.iterator(); i.hasNext(); ) {
			    int tOffset = ((Integer) i.next()).intValue();
			    region = doc.getPartition(tOffset);
				if (tOffset != lastOffset) {
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
							if (directive.requiresEnd()) {
							    directiveStack.push(directive);
							    
							}
						}
					}
					lastOffset = tOffset;
				}
			}
			return directiveStack.size() > 0;
		}
		catch (Exception e) {
			return false;
		}
	}

	public boolean isNodeOffset () {
		NodeOffsetCheck nodeOffsetCheck = new NodeOffsetCheck(getCursorOffset());
		CursorState.evaluate(getSourceViewer().getDocument(), nodeOffsetCheck);
		return !nodeOffsetCheck.isLocalValid;
	}
	private class NodeOffsetCheck implements NodeEvaluator {
		public Stack nodeStack = new Stack();
		private List regions;
		private Node headerNode;
		private int offset;
		private boolean isLocalValid = false;
		
		public NodeOffsetCheck (int offset) {
			this.offset = offset;
		}
		
    	public boolean flatNode(Node node) {
            return true;
		}
		public boolean popNode(Node node) {
            if (nodeStack.size() > 0) {
                Node n = (Node) nodeStack.peek();
                if (n.getName().equals(node.getName())) nodeStack.pop();
                else {
                	while (nodeStack.size() > 0) {
                		n = (Node) nodeStack.peek();
                		if (n.isPseudoFlatNode()) nodeStack.pop();
                		if (n.getName().equals(node.getName())) break;
                		else if (!n.isPseudoFlatNode()) {
                			if (node.getOffsetStart() > offset) return false;
                			else return true;
                		}
                	}
                }
                if (n == headerNode && node.getOffsetEnd() > offset) {
                	isLocalValid = true;
                }
            }
	        return true;
		}

		public boolean pushNode(Node node) {
			if (node.getOffsetStart() < offset) headerNode = node;
               nodeStack.push(node);
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
					IDocument doc = getSourceViewer().getDocument();
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
	
	public class InputValidator extends Thread {
		public void run () {
			try {
				validate();
				sleep (MIN_VALIDATION_LIMIT);
				while (WAITING_VALIDATION) {
					WAITING_VALIDATION = false;
					validate();
					sleep (MIN_VALIDATION_LIMIT);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				VALIDATING = false;
				nextAllowedValidation = System.currentTimeMillis() + MIN_VALIDATION_LIMIT;
			}
		}
		
		private void validate () {
			deleteMarkers();
			String text = getSourceViewer().getDocument().get();
			try {
				Velocity.evaluate(vc, new StringWriter(), Velocity.class.getName(),
						text);
			} catch (ParseErrorException e) {
				addProblemMarker(e);
				return;
			} catch (Exception e) {
			}
			WAITING_VALIDATION = false;
		}
		
		private void deleteMarkers () {
			getViewer().getTextWidget().getDisplay().asyncExec(
					new Runnable () {
						public void run () {
							try {
								getFile().deleteMarkers(null, true, IResource.DEPTH_ONE);
							}
							catch (CoreException e) {}
						}
					});
		}

		public void addProblemMarker (Exception e) {
			final Exception exception = e;
			getViewer().getTextWidget().getDisplay().asyncExec(
					new Runnable () {
						public void run () {
							try {
								int lineNumber = 0;
								int index = exception.getMessage().indexOf("at line ");
								if (index >= 0) {
									int newIndex = index + 8;
									StringBuffer sb = new StringBuffer();
									char c = exception.getMessage().charAt(newIndex++);
									while (Character.isDigit(c)) {
										sb.append(c);
										c = exception.getMessage().charAt(newIndex++);
									}
									if (sb.length() > 0) {
										lineNumber = Integer.parseInt(sb.toString());
									}
								}
								int colNumber = 0;
//								index = exception.getMessage().indexOf("column ");
//								if (index >= 0) {
//									int newIndex = index + 8;
//									StringBuffer sb = new StringBuffer();
//									char c = exception.getMessage().charAt(newIndex++);
//									while (Character.isDigit(c)) {
//										sb.append(c);
//										c = exception.getMessage().charAt(newIndex++);
//									}
//									if (sb.length() > 0) {
//										colNumber = Integer.parseInt(sb.toString());
//										if (colNumber > 0) {
//											int offset = getViewer().getDocument().getLineOffset(lineNumber);
//											colNumber += offset - 4;
//										}
//									}
//								}
								EditorUtil.addProblemMarker(getFile(), exception.getMessage(), lineNumber, colNumber);
							}
							catch (Exception e) {}
						}
					});
		}
	}
}