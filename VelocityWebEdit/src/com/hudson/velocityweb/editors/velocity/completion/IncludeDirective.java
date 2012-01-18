package com.hudson.velocityweb.editors.velocity.completion;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class IncludeDirective extends AbstractDirective {

	private String token;
	private boolean loaded;
	
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
		return "include (" + getInsideText() + ")";
	}
	
    public String getImage() {
        return "include";
    }

	private String getToken () {
	    if (!loaded) {
			String content = getInsideText();
			StringBuffer token = new StringBuffer();
			int index = content.lastIndexOf("$");
			if (index > 0) {
				char c = content.charAt(++index);
				while (!Character.isWhitespace(c) && c != ')') {
					token.append(c);
					index++;
					if (content.length() > index)
						c = content.charAt(index);
					else
						break;
				}
			}
			loaded = true;
			if (token.length() > 0) this.token = token.toString();
	    }
	    return this.token;
	}

	protected void loadVariables(IFile file, ClassLoader loader, Map variables) {
		if (null != getToken()) {
			variables.put(getToken(), String.class);
		}
	}
}