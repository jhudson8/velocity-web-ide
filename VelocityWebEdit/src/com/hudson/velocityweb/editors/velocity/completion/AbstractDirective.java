package com.hudson.velocityweb.editors.velocity.completion;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;

import com.hudson.velocityweb.Plugin;
import com.hudson.velocityweb.editors.velocity.ContextValue;
import com.hudson.velocityweb.manager.ConfigurationManager;

public abstract class AbstractDirective implements IDirective {

	protected int start;
	protected int length;
	protected int lineNumber;
	protected int lineOffset;
	protected int end = Integer.MIN_VALUE;
	protected IDocument document;
	private String content;
	private IDirective parent;
	private List childDirectives = new ArrayList();
	private IRegion region;

	/* (non-Javadoc)
	 * @see com.hudson.hibernatesynchronizer.editors.velocity.cursor.IDirective#load(int, int, org.eclipse.jface.text.Document)
	 */
	public void load(IRegion region, IDocument document) {
		this.start = region.getOffset();
		this.length = region.getLength();
		try {
			this.lineNumber = document.getLineOfOffset(region.getOffset());
			this.lineOffset = document.getLineOffset(region.getOffset());
		}
		catch (BadLocationException e) {}
		this.document = document;
		this.region = region;
	}

	/* (non-Javadoc)
	 * @see com.hudson.hibernatesynchronizer.editors.velocity.cursor.IDirective#addVariableAdditions(java.lang.ClassLoader, java.util.Map)
	 */
	public void addVariableAdditions(IFile file, ClassLoader classLoader, Map variables) {
		if (canAddVariables()) {
			loadVariables(file, classLoader, variables);
		}
	}
	/* (non-Javadoc)
	 * @see com.hudson.hibernatesynchronizer.editors.velocity.cursor.IDirective#isStackScope()
	 */
	public boolean isStackScope() {
		return true;
	}
	/* (non-Javadoc)
	 * @see com.hudson.hibernatesynchronizer.editors.velocity.cursor.IDirective#requiresEnd()
	 */
	public boolean requiresEnd() {
		return true;
	}
	
	protected boolean canAddVariables () {
		return true;
	}
	
	protected void loadVariables (IFile file, ClassLoader classLoader, Map variables) {}
	
	protected String getContent () {
		if (null == content) {
			try {
				content = document.get(start, length);
			}
			catch (BadLocationException e) {
				Plugin.trace(e);
			}
		}
		return content;
	}
	
	protected String getInsideText () {
		int index = getContent().indexOf("(");
		if (index >= 0) {
			return getContent().substring(index + 1, getContent().length() - 1);
		}
		else {
			return null;
		}
	}
	
	public boolean isCursorInDirective (int pos) {
		if (end == Integer.MIN_VALUE) {
			int i = findEndIndex();
			if (i >= 0) end = start + i;
		}
		return (pos >= start && pos <= end);
	}
	
	public int findEndIndex () {
		return getContent().indexOf(")");
	}

	public List getCompletionProposals (IFile file, int pos, Map addedValues, ClassLoader loader) throws Exception {
		return null;
	}
	
	public static List getCompletionProposals (IFile file, IDocument document, int pos, Map addedValues, ClassLoader loader, boolean listValue) throws Exception {
	    try {
			int i = pos-1;
			char c = document.getChar(i);
			boolean seenBrace = false;
			boolean seenExclamation = false;
			while (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '$' || c == '{' || c == '!') {
				if (c == '{') seenBrace = true;
				if (c == '$') {
					break;
				}
				else {
					if (seenExclamation) seenExclamation = false;
					if (c == '!') seenExclamation = true;
				}
				i--;
				if (i>0) c = document.getChar(i);
				else break;
			}
			String text = document.get(i, pos-i);
			if (text.startsWith("$")) {
				int removeSize = 0;
				if (text.startsWith("${")) removeSize = 2;
				else if (text.startsWith("$!{")) removeSize = 3;
				else if (text.startsWith("$!")) removeSize = 2;
				else removeSize = 1;
				text = text.substring(removeSize, text.length());
				int index = text.lastIndexOf('.');
				if (index > 0) {
					String parentToken = text.substring(0, index);
					String prefix = text.substring(index+1, text.length());
					Class parentClass = getObjectClass(file, parentToken, loader, addedValues, listValue);
					if (null != parentClass) {
						return getCompletionProposals(file, document, parentClass, prefix, i+index+removeSize+1, seenBrace);
					}
				}
				else {
					return getCompletionProposals(file, document, addedValues, i+removeSize, text, seenBrace);
				}
			}
		}
		catch (BadLocationException e) {}
		return null;
	}

	public static Class getObjectClass (IFile file, String token, ClassLoader loader, Map additionalClasses, boolean listValue) {
		StringTokenizer st = new StringTokenizer(token, ".");
		Class parentClass = null;
		while (st.hasMoreTokens()) {
			if (null == parentClass) {
				// first time in
				parentClass = getClassMatch(file, st.nextToken(), additionalClasses, (listValue && !st.hasMoreTokens()));
				if (null == parentClass) break;
			}
			else {
				parentClass = getClassMatch (file, parentClass, st.nextToken(), additionalClasses, listValue && !st.hasMoreTokens());
				if (null == parentClass) break;
			}
		}
		return parentClass;
	}
	
	public static Class getClassMatch (IFile file, String token, Map additionalClasses, boolean listValue) {
		if (null == token) return null;
		if (listValue) {
		    ContextValue[] values = ConfigurationManager.getInstance(file.getProject()).getContextValues(file, true);
		    for (int i=0; i<values.length; i++) {
		        if (values[i].name.equals(token)) {
		            return values[i].singularClass;
		        }
		    }
		}
		else {
		    ContextValue[] values = ConfigurationManager.getInstance(file.getProject()).getContextValues(file, true);
		    for (int i=0; i<values.length; i++) {
		        if (values[i].name.equals(token)) {
		            return values[i].objClass;
		        }
		    }
			if (null != additionalClasses) return (Class) additionalClasses.get(token);
		}
		return null;
	}

	public static Class getClassMatch (IFile file, Class parentClass, String token, Map additionalClasses, boolean listValue) {
		int index = token.indexOf("(");
		if (index > 0) {
			// we have parameters
			token = token.substring(0, token.indexOf("(")-1);
		}
		else {
			token = "get" + token;
		}
		if (null == parentClass) return null;
		if (listValue) {
		    ContextValue[] values = ConfigurationManager.getInstance(file.getProject()).getContextValues(file, true);
		    for (int i=0; i<values.length; i++) {
		        if (values[i].name.equals(token)) {
		            return values[i].singularClass;
		        }
		    }
		}
		else {
			Method[] methods = parentClass.getMethods();
			for (int i=0; i<methods.length; i++) {
				if (methods[i].getName().equals(token)) {
					return methods[i].getReturnType();
				}
			}
		}
		return null;
	}

	public static List getCompletionProposals(IFile file, IDocument document, Class parentClass, String prefix, int startIndex, boolean seenBrace) {
		int endIndex = startIndex + prefix.length();
		boolean seenEndBrace = false;
		int dotIndex = -1;
		boolean inMethodParams = false;
		try {
			char c = document.getChar(endIndex);
			while (Character.isLetterOrDigit(c) || c == '_' || c == '}' || c == '.' || c == '(' || c == ')') {
				if (c == '.') {
					dotIndex = endIndex;
					break;
				}
				else if (c == '}') {
					if (!seenEndBrace) {
						seenEndBrace = true;
						break;
					}
				}
				else if (c == '(') {
					inMethodParams = true;
				}
				else if (c == ')') {
					if (inMethodParams) endIndex++;
					break;
				}
				c = document.getChar(++endIndex);
			}
		}
		catch (BadLocationException e) {}
		if (dotIndex >= 0) endIndex = dotIndex;
		List proposals = new ArrayList();
		if (null != parentClass) {
			String pUpper = prefix.toUpperCase();
			for (int i=0; i<parentClass.getMethods().length; i++) {
				Method m = parentClass.getMethods()[i];
				if (!m.getDeclaringClass().getName().equals(Object.class.getName())) {
					boolean added = false;
					if (m.getName().startsWith("get")  && m.getParameterTypes().length == 0) {
						String mName = m.getName().substring(3, m.getName().length());
						if (mName.toUpperCase().startsWith(pUpper)) {
							String actual = mName;
							if (seenBrace && !seenEndBrace && dotIndex < 0) actual = mName + "}";
							proposals.add(new CompletionProposal(
									actual,
									startIndex,
									endIndex-startIndex,
									mName.length(),
									null, mName + " - " + m.getReturnType().getName(), null, null));
							added = true;
						}
					}
					if (!added) {
						String mName = m.getName();
						if (mName.toUpperCase().startsWith(prefix.toUpperCase())) {
							StringBuffer display = new StringBuffer();
							display.append(mName);
							display.append("(");
							for (int j=0; j<m.getParameterTypes().length; j++) {
								if (j > 0) display.append(", ");
								display.append(m.getParameterTypes()[j].getName());
							}
							display.append(")");
							String actual = mName + "()";
							int tLength = actual.length();
							if (m.getParameterTypes().length > 0) tLength--;
							if (seenBrace && !seenEndBrace && dotIndex < 0) actual = actual + "}";
							proposals.add(new CompletionProposal(actual,
									startIndex, endIndex-startIndex, tLength,
									null, display.toString() + " - " + m.getReturnType().getName(), null, null));
						}
					}
				}
			}
		}
		return proposals;
	}

	public static List getCompletionProposals(IFile file, IDocument document, Map addedValues, int startIndex, String prefix, boolean seenBrace) {
		int endIndex = startIndex + prefix.length();
		boolean seenEndBrace = false;
		int dotIndex = -1;
		try {
			char c = document.getChar(endIndex);
			boolean inMethodParams = false;
			while (Character.isLetterOrDigit(c) || c == '_' || c == '}' || c == '.' || c == '(' || c == ')') {
				if (c == '.') {
					dotIndex = endIndex;
					break;
				}
				else if (c == '}') {
					if (!seenEndBrace) seenEndBrace = true;
					else break;
				}
				else if (c == '(') {
					inMethodParams = true;
				}
				else if (c == ')') {
					if (inMethodParams) endIndex++;
					break;
				}
				c = document.getChar(++endIndex);
			}
		}
		catch (BadLocationException e) {}
		if (dotIndex >= 0) endIndex = dotIndex;
		List proposals = new ArrayList();
		String pUpper = prefix.toUpperCase();
		ContextValue[] values = ConfigurationManager.getInstance(file.getProject()).getContextValues(file, true);
		for (int i=0; i<values.length; i++) {
			String propName = values[i].name;
			boolean keyFound = false;
			Object[] keys = addedValues.keySet().toArray();
			for (int j=0; j<keys.length; j++) {
				if (keys[j].equals(propName)) keyFound = true;
			}
			if (!keyFound) {
				if (propName.toUpperCase().startsWith(pUpper)) {
					String actual = propName;
					if (seenBrace && !seenEndBrace && dotIndex < 0) actual = propName + "}";
					proposals.add(new CompletionProposal(
							actual,
							startIndex,
							endIndex-startIndex,
							propName.length(),
							null, propName, null, null));
				}
			}
		}
		if (null != addedValues) {
			for (Iterator i=addedValues.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry entry = (Map.Entry) i.next();
				String propName = (String) entry.getKey();
				if (propName.toUpperCase().startsWith(pUpper)) {
					String actual = propName;
					if (seenBrace && !seenEndBrace && dotIndex < 0) actual = propName + "}";
					proposals.add(new CompletionProposal(
							actual,
							startIndex,
							endIndex-startIndex,
							propName.length(),
							null, propName, null, null));
				}
			}
		}
		return proposals;
	}

	public String getImage () {
	    return "template.gif";
	}

    public IDirective getParent() {
        return parent;
    }
    public void setParent(IDirective parent) {
        this.parent = parent;
    }
    
    public void addChildDirective(IDirective directive) {
        childDirectives.add(directive);
        directive.setParent(this);

    }
    public List getChildDirectives() {
        return childDirectives;
    }

    public int getLength() {
        return length;
    }
    public int getOffset() {
        return start;
    }
    public int getLineNumber () {
        return lineNumber;
    }
    public int getLineOffset() {
        return lineOffset;
    }
    public IRegion getRegion () {
    	return region;
    }
    public boolean equals (Object obj) {
        if (obj.getClass().getName().equals(getClass().getName())) {
            IDirective directive = (IDirective) obj;
            if (directive.getLineNumber() == getLineNumber()) {
                if (directive.getLineOffset() >= getLineOffset() - 5 && directive.getLineOffset() <= getLineOffset() + 5) return true;
            }
        }
        return false;
    }
    
    public int hashCode () {
        return new String(getClass().getName() + getLineNumber()).hashCode();
    }
}