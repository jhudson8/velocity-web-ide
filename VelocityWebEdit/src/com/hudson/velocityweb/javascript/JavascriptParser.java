package com.hudson.velocityweb.javascript;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Joe Hudson
 */
public class JavascriptParser {

    public static JavascriptFunction[] parse (String contents) {
       	int start = 0;
    	int index = contents.indexOf("function ", start);
    	List javascriptFunctions = new ArrayList();
    	while (index >= 0) {
    		StringBuffer functionName = new StringBuffer();
    		StringBuffer currentParam = null;
    		List params = new ArrayList();
    		int state = 0;
    		int iTemp = index + 9;
    		boolean keepGoing = true;
    		boolean done = false;
    		int startOffset = -1;
    		while (keepGoing) {
    			if (contents.length() > iTemp) {
    				char c = contents.charAt(iTemp++);
    				if (state == 0) {
    					// waiting for function name
    					if (c == ' ' || c == '\t') keepGoing = true;
    					else if (Character.isLetter(c) || Character.isDigit(c) || c == '_') {
    						state = 1;
    						functionName.append(c);
    						startOffset = index;
    					}
    					else keepGoing = false;
    				}
    				else if (state == 1) {
    					// retrieving function name
    					if (c == ' ' || c == '\t') state = 2;
    					else if (Character.isLetter(c) || Character.isDigit(c) || c == '_') 
    						functionName.append(c);
    					else if (c == '(') {
    						state = 3;
    					}
    					else keepGoing = false;
    				}
    				else if (state == 2) {
    					// waiting for (
    					if (c == ' ' || c == '\t') keepGoing = true;
    					else if (c == '(') state = 3;
    					else keepGoing = false;
    				}
    				else if (state == 3) {
    					// waiting for params
    					if (c == ' ' || c == '\t' || c == ',') keepGoing = true;
    					else if (Character.isLetter(c) || Character.isDigit(c) || c == '_') {
    						currentParam = new StringBuffer();
    						currentParam.append(c);
    						state = 4;
    					}
    					else if (c == ')') {
    						done = true;
    					}
    					else keepGoing = false;
    				}
    				else if (state == 4) {
    					if (c == ' ' || c == '\t' || c == ',') {
    						params.add(currentParam.toString());
    						currentParam = null;
    						state = 3;
    					}
    					else if (Character.isLetter(c) || Character.isDigit(c) || c == '_') {
    						currentParam.append(c);
    					}
    					else if (c == ')') {
    						params.add(currentParam.toString());
    						currentParam = null;
    						done = true;
    					}
    				}
    				if (done) {
						String[] args = new String[params.size()];
						for (int j=0; j<params.size(); j++) {
							args[j] = (String) params.get(j);
						}
						JavascriptFunction function = new JavascriptFunction(functionName.toString(), args, startOffset);
						javascriptFunctions.add(function);
						keepGoing = false;
						startOffset = -1;
    				}
    			}
    			else keepGoing = false;
    		}
    		start = index + 1;
    		index = contents.indexOf("function ", start);
    	}
    	return (JavascriptFunction[]) javascriptFunctions.toArray(new JavascriptFunction[javascriptFunctions.size()]);
    }
}
