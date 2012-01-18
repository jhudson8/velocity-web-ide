package com.hudson.velocityweb.editors.velocity.completion;



public class VariableDirective extends AbstractDirective {

	public String getLabel () {
	    return getVariableName();
	}
	
	private String variableName;
	public String getVariableName () {
	    if (null == variableName) {
		    String s = getContent();
		    int i = s.indexOf('{');
		    if (i >= 0) {
		        s = s.substring(i+1, s.length()-1);
		    }
		    if (s.startsWith("!")) s = s.substring(2, s.length());
		    else if (s.startsWith("$")) s = s.substring(1, s.length());
		    variableName = s;
	    }
	    return variableName;
	}

    public boolean requiresEnd() {
        return false;
    }

    public boolean isStackScope() {
        return false;
    }

    public String getImage() {
        return "variable";
    }
}
