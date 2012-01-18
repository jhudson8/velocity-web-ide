package com.hudson.velocityweb.editors.velocity;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;


/**
 * @author Joe Hudson
 */
public class DirectiveRule extends SingleLineRule {

	private static final char NBR_SGN = '#';
	
	public DirectiveRule(IToken token, String directive) {
		super(NBR_SGN + directive, null, token);
	}

	protected boolean endSequenceDetected(ICharacterScanner scanner) {
		PartitionScanner partitionScanner = (PartitionScanner) scanner;
		int offset = partitionScanner.getTokenOffset();
		try {
		    IDocument document = partitionScanner.getDocument();
		    if ('\\' == document.getChar(offset-1)) return false;
		}
		catch (BadLocationException e) {}
	    
		int stack = 0;
		int readChars = 1;
		char c = (char) scanner.read();
		while (c != '\n') {
			if (c == '(') stack++;
			else if (c == ')') {
				if (stack <= 1) {
					return true;
				}
				else stack --;
			}
			else if (((int) c) == 65535) {
			    break;
			}
			c = (char) scanner.read();
			readChars++;
		}
		for (int i=0; i<readChars; i++) scanner.unread();
		return false;
	}
}