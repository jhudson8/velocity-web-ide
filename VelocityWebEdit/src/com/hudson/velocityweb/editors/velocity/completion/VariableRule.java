package com.hudson.velocityweb.editors.velocity.completion;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;

/**
 * @author Joe Hudson
 */
public class VariableRule extends SingleLineRule {

	/**
	 * @param startSequence
	 * @param endSequence
	 * @param token
	 */
	public VariableRule(IToken token, String start) {
		super(start, null, token);
	}


	/**
	 * @see org.eclipse.jface.text.rules.PatternRule#endSequenceDetected(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	protected boolean endSequenceDetected(ICharacterScanner scanner) {
		char c;
		char[][] delimiters= scanner.getLegalLineDelimiters();
		boolean previousWasEscapeCharacter = false;	
		boolean seenBracket = false;
		int braceStack = 0;
		int bracketStack = 0;
		int charsRead = 0;
		boolean seenFirstChar = false;
		StringBuffer sb = new StringBuffer();
		while ((c = (char) scanner.read()) != ICharacterScanner.EOF) {
			charsRead++;
			if (c == fEscapeCharacter) {
				// Skip the escaped character.
				scanner.read();
			}
			else if (c == '(') {
				braceStack++;
			} else if (c == ')') {
				if (braceStack > 0) braceStack --;
				else {
					break;
				}
				if (braceStack == 0 && bracketStack == 0) {
					if (varCheck(sb.toString())) return true;
					else break;
				}
			} else if (c == '{') {
				bracketStack++;
			} else if (c == '}') {
				if (bracketStack > 0) bracketStack --;
				else {
					break;
				}
				if (bracketStack == 0) return true;
			}
			else if (Character.isWhitespace(c) && c != '\n') {
				if (braceStack == 0 && bracketStack == 0 && seenFirstChar) {
					scanner.unread();
					charsRead--;
					if (varCheck(sb.toString())) return true;
					else break;
				}
				else if (braceStack == 0) break;
			}
			else if (c == '\n') {
				if (braceStack == 0 && bracketStack == 0 && seenFirstChar) {
					scanner.unread();
					charsRead--;
					if (varCheck(sb.toString())) return true;
					else break;
				}
				else if (braceStack == 0) break;
			}
			else if (c == '>' || c == '<') {
				if (bracketStack == 0 && braceStack == 0 && seenFirstChar) {
					scanner.unread();
					charsRead--;
					if (varCheck(sb.toString())) return true;
					else break;
				}
				else break;
			}
			else if (((int) c) == 65535) {
			    break;
			}
			else {
				// Check for end of line since it can be used to terminate the pattern.
				for (int i= 0; i < delimiters.length; i++) {
					if (c == delimiters[i][0] && sequenceDetected(scanner, delimiters[i], true)) {
						if (!fEscapeContinuesLine || !previousWasEscapeCharacter)
							if (varCheck(sb.toString())) return true;
							else break;
					}
				}
				seenFirstChar = true;
			}
			previousWasEscapeCharacter = (c == fEscapeCharacter);
			sb.append(c);
		}
		for (int i=0; i<charsRead; i++) scanner.unread();
		return false;
	}

	public boolean varCheck (String var) {
		try {
			Long.parseLong(var);
		}
		catch (NumberFormatException e) {
			return true;
		}
		return false;
	}
}
