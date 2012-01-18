package com.hudson.velocityweb.editors.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.hudson.velocityweb.Plugin;
import com.hudson.velocityweb.css.CSSFile;
import com.hudson.velocityweb.editors.velocity.Editor;
import com.hudson.velocityweb.editors.velocity.PartitionScanner;
import com.hudson.velocityweb.editors.velocity.completion.Attribute;
import com.hudson.velocityweb.editors.velocity.completion.DirectiveFactory;
import com.hudson.velocityweb.editors.velocity.completion.IDirective;
import com.hudson.velocityweb.editors.velocity.completion.MacroInstanceDirective;
import com.hudson.velocityweb.editors.velocity.completion.xml.CursorState;
import com.hudson.velocityweb.editors.velocity.completion.xml.Node;
import com.hudson.velocityweb.editors.velocity.parser.VelocityFile;
import com.hudson.velocityweb.javascript.JavascriptFile;
import com.hudson.velocityweb.manager.ConfigurationManager;
import com.hudson.velocityweb.util.EditorUtil;

/**
 * @author Joe Hudson
 */
public class GoToDefinition  implements IEditorActionDelegate {

	private Editor editor;
	private int offset;
	
	public void setActiveEditor(IAction arg0, IEditorPart arg1) {
		if (arg1 instanceof Editor) {
			arg0.setEnabled(true);
			this.editor = (Editor) arg1;
		}
		else {
			arg0.setEnabled(false);
		}
	}

	public void run (IAction action) {
		try {
			ISelection selection = editor.getEditorSite().getSelectionProvider().getSelection();
			ITextSelection textSelection = (ITextSelection) selection;
			TypedRegion region = (TypedRegion) editor.getRegion(offset);
			if (null != region) {
				IDirective directive = DirectiveFactory.getDirective(region.getType(), region, editor.getViewer().getDocument());
				if (null != directive) {
					if (directive instanceof MacroInstanceDirective) {
						List macroFiles = ConfigurationManager.getInstance(editor.getProject()).getMacroFiles();
						for (Iterator i=macroFiles.iterator(); i.hasNext(); ) {
							VelocityFile file = (VelocityFile) i.next();
							for (int j=0; j<file.getMacros().length; j++) {
								if (file.getMacros()[j].name.equals(((MacroInstanceDirective) directive).getMacroName())) {
									ITextEditor textEditor = EditorUtil.openExternalFile(
											file.file,
											Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow());
									try {
										textEditor.selectAndReveal(file.getMacros()[j].offset, 0);
									}
									catch (Exception e) {
										e.printStackTrace();
									}
									return;
								}
							}
						}
					}
				}
				else if (region.getType().equals(PartitionScanner.XML_TAG)) {
					Node node =  new Node(null, region.getOffset(), region.getOffset() + region.getLength() - 1, editor.getViewer().getDocument());
					int state = node.getState(textSelection.getOffset());
					if (state == CursorState.STATE_ATTRIBUTE_VALUE) {
						Attribute attribute = node.getAttribute(textSelection.getOffset());
						if (attribute.getName().toUpperCase().startsWith("ON")) {
							// javascript function
							StringBuffer sb = new StringBuffer();
							int startOffset = textSelection.getOffset();
							IDocument doc = editor.getViewer().getDocument();
							char c = doc.getChar(startOffset);
							while ((Character.isLetterOrDigit(c) || c == '_' || c == '-') && startOffset > 0) {
								startOffset--;
								c = doc.getChar(startOffset);
							}
							startOffset++;
							while (!(Character.isLetterOrDigit(c) || c == '_' || c == '-') && startOffset < doc.getLength()) {
								startOffset++;
								c = doc.getChar(startOffset);
							}
							startOffset--;
							int endOffset = startOffset;
							c = doc.getChar(endOffset);
							while ((Character.isLetterOrDigit(c) || c == '_' || c == '-') && endOffset < doc.getLength()) {
								endOffset++;
								c = doc.getChar(endOffset);
							}
							String javascriptName = doc.get(startOffset, endOffset-startOffset);
							List javascriptFiles = ConfigurationManager.getInstance(editor.getProject()).getJavascriptFiles();
							for (Iterator i=javascriptFiles.iterator(); i.hasNext(); ) {
								JavascriptFile file = (JavascriptFile) i.next();
								for (int j=0; j<file.getFunctions().length; j++) {
									if (file.getFunctions()[j].name.equals(javascriptName)) {
										ITextEditor textEditor = EditorUtil.openExternalFile(
												file.file,
												Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow());
										try {
											textEditor.selectAndReveal(file.getFunctions()[j].offset, 0);
										}
										catch (Exception e) {
											e.printStackTrace();
										}
										return;
									}
								}
							}
						}
						else if (attribute.getName().equalsIgnoreCase("class")) {
							String cssName = attribute.getValue();
							List cssFiles = ConfigurationManager.getInstance(editor.getProject()).getCSSFiles();
							for (Iterator i=cssFiles.iterator(); i.hasNext(); ) {
								CSSFile file = (CSSFile) i.next();
								for (int j=0; j<file.getStyles().length; j++) {
									if (file.getStyles()[j].name.equals(cssName)) {
										ITextEditor textEditor = EditorUtil.openExternalFile(
												file.file,
												Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow());
										try {
											textEditor.selectAndReveal(file.getStyles()[j].offset, 0);
										}
										catch (Exception e) {
											e.printStackTrace();
										}
										return;
									}
								}
							}
						}
					}
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction arg0, ISelection arg1) {
		if (null != editor) arg0.setEnabled(true);
		try {
			this.offset = ((ITextSelection) arg1).getOffset();
		}
		catch (Exception e) {}
	}
}
