package com.hudson.velocityweb.editors.velocity.completion;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import com.hudson.velocityweb.editors.velocity.PartitionScanner;

public class DirectiveFactory {

	public static boolean isEndDirective (String directiveType) {
		return PartitionScanner.END_PARTITION.equals(directiveType)
			|| PartitionScanner.FOREACH_END_PARTITION.equals(directiveType)
			|| PartitionScanner.IF_END_PARTITION.equals(directiveType)
			|| PartitionScanner.MACRO_END_PARTITION.equals(directiveType);
	}

	public static IDirective getDirective (String directiveType, ITypedRegion region, IDocument document) throws BadLocationException {
		IDirective directive = null;
		if (region.getType().equals(PartitionScanner.FOREACH_PARTITION)) {
			directive = new ForeachDirective();
		}
		else if (region.getType().equals(PartitionScanner.IF_PARTITION)) {
			directive = new IfDirective();
		}
		else if (region.getType().equals(PartitionScanner.ELSE_PARTITION)) {
			directive = new ElseDirective();
		}
		else if (region.getType().equals(PartitionScanner.ELSE_IF_PARTITION)) {
			directive = new ElseIfDirective();
		}
		else if (region.getType().equals(PartitionScanner.MACRO_PARTITION)) {
			directive = new MacroDirective();
		}
		else if (region.getType().equals(PartitionScanner.SET_PARTITION)) {
			directive = new SetDirective();
		}
		else if (region.getType().equals(PartitionScanner.MACRO_INSTANCE_PARTITION)) {
			directive = new MacroInstanceDirective();
		}
		else if (region.getType().equals(PartitionScanner.VARIABLE_PARTITION)) {
			directive = new VariableDirective();
		}
		else if (region.getType().equals(PartitionScanner.STOP_PARTITION)) {
			directive = new StopDirective();
		}
		else if (region.getType().equals(PartitionScanner.PARSE_PARTITION)) {
			directive = new ParseDirective();
		}
		else if (region.getType().equals(PartitionScanner.INCLUDE_PARTITION)) {
			directive = new IncludeDirective();
		}
		int lineNum = document.getLineOfOffset(region.getOffset());
		int lineOffset = region.getOffset() - document.getLineOffset(lineNum);
		if (null != directive) directive.load(region, document);
		return directive;
	}
}
