package com.hudson.velocityweb.editors.velocity;

import org.eclipse.jface.text.rules.IWordDetector;

public class DirectiveDetector implements IWordDetector {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
	 */
	public boolean isWordPart(char c) {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
	 */
	public boolean isWordStart(char c) {
		return (c == '#');
	}
}
