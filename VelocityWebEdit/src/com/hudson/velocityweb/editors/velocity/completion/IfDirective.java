package com.hudson.velocityweb.editors.velocity.completion;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class IfDirective extends AbstractDirective {

    private String evaluation;
    
	/* (non-Javadoc)
	 * @see com.hudson.hibernatesynchronizer.editors.velocity.cursor.AbstractDirective#canAddVariables()
	 */
	protected boolean canAddVariables() {
		return false;
	}

	public List getCompletionProposals (IFile file, int pos, Map addedValues, ClassLoader loader) throws Exception {
		return getCompletionProposals(file, document, pos, addedValues, loader, false);
	}
	
	public String getLabel () {
	    return "if (" + getInsideText() + ")";
	}
	
    public String getImage() {
        return "if";
    }
}
