package com.hudson.velocityweb.editors.velocity;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;


/**
 * @author Joe Hudson
 */
public class MacroInstanceRule extends SingleLineRule {

	private static final char NBR_SGN = '#';
	private static String[] reservedDirectives = {"foreach", "macro", "if", "else", "end", "parse", "elseif"};
	
	public MacroInstanceRule(IToken token) {
		super(Character.toString(NBR_SGN), null, token);
	}

	protected boolean endSequenceDetected(ICharacterScanner scanner) {
	    StringBuffer sb = new StringBuffer();
		PartitionScanner partitionScanner = (PartitionScanner) scanner;
		boolean seenBrace = false;
		int offset = partitionScanner.getTokenOffset();
		try {
		    IDocument document = partitionScanner.getDocument();
		    if ('\\' == document.getChar(offset-1)) return false;
		}
		catch (BadLocationException e) {}
	    
		int stack = 0;
		int readChars = 1;
		boolean keepReading = true;
		char c = (char) scanner.read();
		while (c != '\n') {
			if (c == '(') {
				seenBrace = true;
			    stack++;
			    keepReading = false;
			}
			else if (c == ')') {
				if (stack <= 1) {
				    for (int i=0; i<reservedDirectives.length; i++) {
				        if (reservedDirectives[i].equals(sb.toString())) {
				    		for (int j=0; j<readChars; j++) scanner.unread();
				    		return false;
				        }
				    }
					return true;
				}
				else stack --;
			}
			else if (Character.isWhitespace(c)) {
			    keepReading = false;
			}
			else if (((int) c) == 65535) {
			    break;
			}
			else if (!seenBrace) {
				if (!(Character.isLetterOrDigit(c) || c == '_')) break;
			}
			if (keepReading) sb.append(c);
			c = (char) scanner.read();
			readChars++;
		}
		for (int i=0; i<readChars; i++) scanner.unread();
		return false;
	}
}