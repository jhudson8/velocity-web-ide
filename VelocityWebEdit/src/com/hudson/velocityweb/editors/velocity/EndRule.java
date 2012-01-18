package com.hudson.velocityweb.editors.velocity;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.WordPatternRule;


/**
 * @author Joe Hudson
 */
public class EndRule extends WordPatternRule {

	private static final char NBR_SGN = '#';
	private static final String[] WATCH_DIRECTIVES = new String[] {"if", "foreach", "end", "macro"};
	private static final String END_DIRECTIVE = "end";
	
	private String directive;
	
	public EndRule(IToken token, String directive) {
		super(new DirectiveDetector(), "#end", END_DIRECTIVE, token);
		this.directive = directive;
	}

	/**
	 * @see org.eclipse.jface.text.rules.PatternRule#sequenceDetected(org.eclipse.jface.text.rules.ICharacterScanner, char[], boolean)
	 */
	protected boolean sequenceDetected(ICharacterScanner scanner,
			char[] sequence, boolean eofAllowed) {

		PartitionScanner partitionScanner = (PartitionScanner) scanner;
		int offset = partitionScanner.getTokenOffset();
		boolean startFound = false;
		IDocument document = partitionScanner.getDocument();
		int stack = 0;
		try {
			for (int i=offset-1; i>=0; i--) {
				char c = document.getChar(i);
				if (c == NBR_SGN) {
				    if ((i > 0 && document.getChar(i-1) != '\\') || i == 0) {
						String directive = getDirective(i, document);
						if (null != directive) {
							if (stack == 0) {
								if (directive.equals(this.directive)) {
									return true;
								}
								else if (directive.equals(END_DIRECTIVE)) stack++;
								else return false;
							}
							else {
								if (directive.equals(END_DIRECTIVE)) stack++;
								else stack--;
							}
						}
				    }
				}
			}
		}
		catch (BadLocationException e) {}
		return false;
	}
	
	private String getDirective(int i, IDocument document) throws BadLocationException {
		i++;
		StringBuffer sb = new StringBuffer();
		char c = document.getChar(i);
		while (Character.isLetterOrDigit(c)) {
			sb.append(c);
			c = document.getChar(++i);
		}
		String s = sb.toString();
		for (int j=0; j<WATCH_DIRECTIVES.length; j++) {
			if (WATCH_DIRECTIVES[j].equals(s)) return s;
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.rules.PatternRule#endSequenceDetected(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	protected boolean endSequenceDetected(ICharacterScanner scanner) {
		int charsRead = 1;
		char c = (char) scanner.read();
		for (int i=0; i<END_DIRECTIVE.length(); i++) {
			if (c != END_DIRECTIVE.toCharArray()[i]) {
				for (int j=0; j<charsRead; j++) scanner.unread();
				return false;
			}
			c = (char) scanner.read();
			charsRead++;
		}
		return true;
	}
}