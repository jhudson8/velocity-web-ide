package com.hudson.velocityweb.editors.velocity;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;

/**
 * @author Joe Hudson
 */
public class EndDirectiveDamagerRepairer extends NonRuleBasedDamagerRepairer {

	/**
	 * @param defaultTextAttribute
	 */
	public EndDirectiveDamagerRepairer(TextAttribute defaultTextAttribute) {
		super(defaultTextAttribute);
	}

	
	protected void addRange(TextPresentation presentation, int offset,
			int length, TextAttribute attr) {
		super.addRange(presentation, offset, length, attr);
	}
	public void createPresentation(TextPresentation presentation,
			ITypedRegion region) {
		super.createPresentation(presentation, region);
	}
	protected int endOfLineOf(int offset) throws BadLocationException {
		return super.endOfLineOf(offset);
	}
	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event,
			boolean documentPartitioningChanged) {
		return super.getDamageRegion(partition, event,
				documentPartitioningChanged);
	}
}
