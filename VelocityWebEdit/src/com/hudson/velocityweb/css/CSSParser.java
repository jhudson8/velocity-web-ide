package com.hudson.velocityweb.css;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Joe Hudson
 */
public class CSSParser {

    public static CSSStyle[] parse (String contents) {
       	int start = 0;
    	int index = contents.indexOf('{');
    	List cssStyles = new ArrayList();
    	while (index >= 0) {
    	    int startIndex = index-1;
    	    char c = contents.charAt(startIndex);
    	    while (c != '\n' && c != '.') {
    	        if (startIndex == 0) {
    	            startIndex=startIndex-1;
    	            break;
    	        }
    	        c = contents.charAt(--startIndex);
    	    }
    	    String s = contents.substring(startIndex+1, index).trim();
    	    cssStyles.add(new CSSStyle(s, startIndex));
    	    index = index = contents.indexOf('{', index+2);
    	}
    	return (CSSStyle[]) cssStyles.toArray(new CSSStyle[cssStyles.size()]);
    }
}
