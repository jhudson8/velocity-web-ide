package com.hudson.velocityweb.editors.velocity.completion;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class SetDirective extends AbstractDirective {

	private static final int STATE_WAITING_FOR_1 = 1;
	private static final int STATE_IN_1 = 2;
	private static final int STATE_WAITING_FOR_EQUAL = 3;
	private static final int STATE_WAITING_FOR_2 = 5;
	private static final int STATE_IN_2 = 6;

	boolean loaded = false;
	private String token;
	private Class valueClass;
	private String evaluation;
	
	protected void loadVariables(IFile file, ClassLoader loader, Map variables) {
		Class c = getValueClass();
		if (null == c) {
		    String s = getEvaluation();
		    if (null != s) {
			    c = getObjectClass(file, getEvaluation(), loader, variables, false);
			    variables.put(getToken(), c);
		    }
		}
	}

	private String getEvaluation () {
	    getToken();
	    return this.evaluation;
	}

	private Class getValueClass () {
	    getToken();
	    return this.valueClass;
	}

	private String getToken () {
	    if (!loaded) {
			String content = getInsideText();
			StringBuffer token = new StringBuffer();
			StringBuffer value = new StringBuffer();
			Class valueClass = null;
			int state = STATE_WAITING_FOR_1;
			char[] arr = content.toCharArray();
			for (int i=0; i<arr.length; i++) {
				char c = arr[i];
				if (Character.isLetterOrDigit(c)) {
					if (state == STATE_WAITING_FOR_1 || state == STATE_IN_1) {
						if (state == STATE_WAITING_FOR_1) state = STATE_IN_1;
						token.append(c);
					}
					else if (state == STATE_WAITING_FOR_2) {
						if (Character.isDigit(c)) {
							valueClass = Integer.class;
							break;
						}
						else break;
					}
					else if (state == STATE_IN_2) {
						value.append(c);
					}
					else break;
				}
				else if (c == '.') {
					if (state == STATE_IN_1) {
						break;
					}
					else if (state == STATE_IN_2) {
						state = STATE_IN_2;
						value.append(c);
					}
					else break;
				}
				else if (c == '"') {
					if (state == STATE_WAITING_FOR_2) {
						valueClass = String.class;
						break;
					}
					else break;
				}
				else if (c == '=') {
					if (state == STATE_IN_1 || state == STATE_WAITING_FOR_EQUAL) {
						state = STATE_WAITING_FOR_2;
					}
					else break;
				}
				else if (Character.isWhitespace(c)) {
					if (state == STATE_IN_1) {
						state = STATE_WAITING_FOR_EQUAL;
					}
					else if (state == STATE_IN_2) {
						break;
					}
				}
				else if (c == '(') {
					if (state == STATE_IN_2) {
						for (int j=i+1; j<arr.length; j++) {
							if (arr[j] != ')') value.append(arr[j]);
							else break;
						}
					}
					break;
				}
				else if (c == ')') {
					break;
				}
				else if (c == '$') {
					if (state == STATE_WAITING_FOR_1) state = STATE_IN_1;
					else if (state == STATE_WAITING_FOR_2) state = STATE_IN_2;
					else break;
				}
			}
			this.token = token.toString();
			if (state == STATE_IN_2 || null != valueClass) {
			    this.valueClass = valueClass;
			    this.evaluation = value.toString();
			    
			}
			loaded = true;
	    }
	    return this.token;
	}

	/* (non-Javadoc)
	 * @see com.hudson.hibernatesynchronizer.editors.velocity.cursor.IDirective#isStackScope()
	 */
	public boolean isStackScope() {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see com.hudson.hibernatesynchronizer.editors.velocity.cursor.IDirective#requiresEnd()
	 */
	public boolean requiresEnd() {
		// TODO Auto-generated method stub
		return false;
	}

	public List getCompletionProposals (IFile file, int pos, Map addedValues, ClassLoader loader) throws Exception {
		return getCompletionProposals(file, document, pos, addedValues, loader, false);
	}
	
	public String getLabel () {
	    return "set ($" + getToken() + ")";
	}
	
    public String getImage() {
        return "set";
    }
}