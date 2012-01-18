package com.hudson.velocityweb.editors.velocity;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

import com.hudson.velocityweb.editors.velocity.completion.VariableRule;


public class PartitionScanner extends RuleBasedPartitionScanner {
	public final static String FOREACH_PARTITION = "__foreach_partition";
	public final static String FOREACH_END_PARTITION = "__foreach_end_partition";
	public final static String SET_PARTITION = "__set_partition";
	public final static String IF_PARTITION = "__if_partition";
	public final static String ELSE_PARTITION = "__else_partition";
	public final static String ELSE_IF_PARTITION = "__else_if_partition";
	public final static String IF_END_PARTITION = "__if_end_partition";
	public final static String END_PARTITION = "__end_partition";
	public final static String MACRO_PARTITION = "__macro_partition";
	public final static String MACRO_INSTANCE_PARTITION = "__macro_instance_partition";
	public final static String MACRO_END_PARTITION = "__macro_end_partition";
	public final static String INCLUDE_PARTITION = "__include_partition";
	public final static String PARSE_PARTITION = "__parse_partition";
	public final static String STOP_PARTITION = "__stop_partition";
	public final static String VARIABLE_PARTITION = "__variable_partition";
	public final static String COMMENT_PARTITION = "__comment_partition";

	public final static String XML_DEFAULT = "__xml_default";
	public final static String XML_COMMENT = "__xml_comment";
	public final static String XML_TAG = "__xml_tag";

	
	private IDocument document;
	
	public PartitionScanner() {
		IPredicateRule[] predicateRules = new IPredicateRule[] {
			new MultiLineRule("#*", "*#", new Token(COMMENT_PARTITION)),
			new SingleLineRule("##", "\n", new Token(COMMENT_PARTITION)),
			new DirectiveRule(new Token(FOREACH_PARTITION), "foreach"),
			new DirectiveRule(new Token(SET_PARTITION), "set"),
			new DirectiveRule(new Token(IF_PARTITION), "if"),
			new DirectiveRule(new Token(MACRO_PARTITION), "macro"),
			new DirectiveRule(new Token(INCLUDE_PARTITION), "include"),
			new DirectiveRule(new Token(PARSE_PARTITION), "parse"),
			new EndRule(new Token(FOREACH_END_PARTITION), "foreach"),
			new EndRule(new Token(IF_END_PARTITION), "if"),
			new EndRule(new Token(MACRO_END_PARTITION), "macro"),
			new WordPatternRule(new DirectiveDetector(), "#end", "", new Token(END_PARTITION)),
			new DirectiveRule(new Token(ELSE_IF_PARTITION), "elseif"),
			new WordPatternRule(new DirectiveDetector(), "#else", "", new Token(ELSE_PARTITION)),
			new VariableRule(new Token(VARIABLE_PARTITION), "!$"),
			new VariableRule(new Token(VARIABLE_PARTITION), "$"),
			new SingleLineRule("#stop", null, new Token(STOP_PARTITION)),
			new MacroInstanceRule(new Token(MACRO_INSTANCE_PARTITION)),

			new MultiLineRule("<!--", "-->", new Token(XML_COMMENT)),
			new TagRule(new Token(XML_TAG))
		};
		
		setPredicateRules(predicateRules);
	}

	public IDocument getDocument () {
		return document;
	}

	/**
	 * @see org.eclipse.jface.text.rules.IPartitionTokenScanner#setPartialRange(org.eclipse.jface.text.IDocument, int, int, java.lang.String, int)
	 */
	public void setPartialRange(IDocument document, int offset, int length,
			String contentType, int partitionOffset) {
		this.document = document;
		super.setPartialRange(document, offset, length, contentType,
				partitionOffset);
	}
	/**
	 * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(org.eclipse.jface.text.IDocument, int, int)
	 */
	public void setRange(IDocument document, int offset, int length) {
		this.document = document;
		super.setRange(document, offset, length);
	}
}