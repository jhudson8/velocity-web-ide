package com.hudson.velocityweb.editors.velocity.completion;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class MacroInstanceDirective extends AbstractDirective {

    private String macroName;
    public String getMacroName () {
        if (null == macroName) {
            StringBuffer sb = new StringBuffer();
            String content = getContent();
            for (int i=1; i<content.length(); i++) {
                char c = content.charAt(i);
                if (Character.isWhitespace(c) || c == '(') break;
                else sb.append(c);
            }
            macroName = sb.toString();
        }
        return macroName;
    }

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
	    return getMacroName() + "(" + getInsideText() + ")";
	}
	
    public String getImage() {
        return "macro";
    }
}