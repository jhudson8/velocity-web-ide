package com.hudson.velocityweb.editors.velocity.completion;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class ElseIfDirective extends AbstractDirective {

    public boolean requiresEnd() {
        return false;
    }

    public boolean isStackScope() {
        return false;
    }

	public List getCompletionProposals (IFile file, int pos, Map addedValues, ClassLoader loader) throws Exception {
		return getCompletionProposals(file, document, pos, addedValues, loader, false);
	}
	
	public String getLabel () {
	    return "elseif (" + getInsideText() + ")";
	}
	
    public String getImage() {
        return "elseif";
    }
}