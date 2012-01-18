package com.hudson.velocityweb.editors.velocity.completion;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public interface IDirective {

	public void load (IRegion region, IDocument document);

	public boolean requiresEnd();
	
	public void addVariableAdditions(IFile file, ClassLoader classLoader, Map addedValues);
	
	public boolean isStackScope();
	
	public boolean isCursorInDirective (int pos);

	public List getCompletionProposals(IFile file, int pos, Map addedValues, ClassLoader loader) throws Exception;

	
	/** TREE STUFF **/
	public void setParent (IDirective parent);
	
	public IDirective getParent();

	public List getChildDirectives();
	
	public void addChildDirective(IDirective directive);

	public String getLabel();
	
	public String getImage();
	
	public int getOffset();
	
	public int getLength();
	
	public int getLineNumber();
	
	public int getLineOffset();
	
	public IRegion getRegion ();
}
