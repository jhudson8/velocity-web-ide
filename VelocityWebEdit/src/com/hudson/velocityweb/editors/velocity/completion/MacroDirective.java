package com.hudson.velocityweb.editors.velocity.completion;

import java.util.Map;

import org.eclipse.core.resources.IFile;


public class MacroDirective extends AbstractDirective {

	/* (non-Javadoc)
	 * @see com.hudson.hibernatesynchronizer.editors.velocity.cursor.AbstractDirective#loadVariables()
	 */
	protected void addVariables(IFile file, ClassLoader loader, Map variables) {
		super.loadVariables(file, loader, variables);
	}
	
	public String getLabel () {
	    return "macro (" + getInsideText() + ")";
	}
	
	public String getMacroName () {
	    String s = getInsideText().trim();
	    StringBuffer name = new StringBuffer();
	    int index = 0;
	    char c = s.charAt(index++);
	    while (index < s.length() && !Character.isWhitespace(c)) {
	        name.append(c);
	        c = s.charAt(index++);
	    }
	    return name.toString();
	}

    public String getImage() {
        return "macro";
    }
}
