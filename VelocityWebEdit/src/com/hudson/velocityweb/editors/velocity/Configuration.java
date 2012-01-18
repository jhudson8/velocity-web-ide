package com.hudson.velocityweb.editors.velocity;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import com.hudson.velocityweb.editors.velocity.completion.CompletionProcessor;

public class Configuration extends SourceViewerConfiguration {
	private DoubleClickStrategy doubleClickStrategy;
	private ColorManager colorManager;
	private XMLTagScanner tagScanner;
	private Scanner scanner;
	private Editor editor;

	public Configuration(ColorManager colorManager, Editor editor) {
		this.editor = editor;
		this.colorManager = colorManager;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			PartitionScanner.FOREACH_PARTITION,
			PartitionScanner.END_PARTITION,
			PartitionScanner.IF_PARTITION,
			PartitionScanner.MACRO_PARTITION,
			PartitionScanner.SET_PARTITION,
			PartitionScanner.FOREACH_END_PARTITION,
			PartitionScanner.IF_END_PARTITION,
			PartitionScanner.ELSE_IF_PARTITION,
			PartitionScanner.ELSE_PARTITION,
			PartitionScanner.MACRO_END_PARTITION,
			PartitionScanner.MACRO_INSTANCE_PARTITION,
			PartitionScanner.VARIABLE_PARTITION,
			PartitionScanner.INCLUDE_PARTITION,
			PartitionScanner.PARSE_PARTITION,
			PartitionScanner.STOP_PARTITION,
			PartitionScanner.COMMENT_PARTITION,
			PartitionScanner.XML_COMMENT,
			PartitionScanner.XML_TAG
		};
	}
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new DoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected Scanner getXMLScanner() {
		if (scanner == null) {
			scanner = new Scanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(ColorManager.COLOR_DEFAULT))));
		}
		return scanner;
	}
	protected XMLTagScanner getXMLTagScanner() {
		if (tagScanner == null) {
			tagScanner = new XMLTagScanner(colorManager);
			tagScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(ColorManager.COLOR_TAG))));
		}
		return tagScanner;
	}
	
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		NonRuleBasedDamagerRepairer ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.COLOR_FOREACH_DIRECTIVE)));
		reconciler.setDamager(ndr, PartitionScanner.FOREACH_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.FOREACH_PARTITION);
		reconciler.setDamager(ndr, PartitionScanner.FOREACH_END_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.FOREACH_END_PARTITION);
		ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.COLOR_SET_DIRECTIVE)));
		reconciler.setDamager(ndr, PartitionScanner.SET_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.SET_PARTITION);
		ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.COLOR_PROC_INSTR)));
		reconciler.setDamager(ndr, PartitionScanner.STOP_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.STOP_PARTITION);
		ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.COLOR_PROC_INSTR)));
		reconciler.setDamager(ndr, PartitionScanner.INCLUDE_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.INCLUDE_PARTITION);
		ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.COLOR_PROC_INSTR)));
		reconciler.setDamager(ndr, PartitionScanner.PARSE_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.PARSE_PARTITION);
		ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.COLOR_IF_DIRECTIVE)));
		reconciler.setDamager(ndr, PartitionScanner.IF_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.IF_PARTITION);
		reconciler.setDamager(ndr, PartitionScanner.ELSE_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.ELSE_PARTITION);
		reconciler.setDamager(ndr, PartitionScanner.ELSE_IF_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.ELSE_IF_PARTITION);
		reconciler.setDamager(ndr, PartitionScanner.IF_END_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.IF_END_PARTITION);
		
		ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.COLOR_MACRO_DIRECTIVE)));
		reconciler.setDamager(ndr, PartitionScanner.MACRO_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.MACRO_PARTITION);
		reconciler.setDamager(ndr, PartitionScanner.MACRO_END_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.MACRO_END_PARTITION);
		reconciler.setDamager(ndr, PartitionScanner.MACRO_INSTANCE_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.MACRO_INSTANCE_PARTITION);
		ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.COLOR_VARIABLE)));
		reconciler.setDamager(ndr, PartitionScanner.VARIABLE_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.VARIABLE_PARTITION);

		ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.COLOR_COMMENT)));
		reconciler.setDamager(ndr, PartitionScanner.COMMENT_PARTITION);
		reconciler.setRepairer(ndr, PartitionScanner.COMMENT_PARTITION);
		
		DefaultDamagerRepairer dr =
			new DefaultDamagerRepairer(getXMLTagScanner());
		reconciler.setDamager(dr, PartitionScanner.XML_TAG);
		reconciler.setRepairer(dr, PartitionScanner.XML_TAG);

		dr = new DefaultDamagerRepairer(getXMLScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.COLOR_XML_COMMENT)));
		reconciler.setDamager(ndr, PartitionScanner.XML_COMMENT);
		reconciler.setRepairer(ndr, PartitionScanner.XML_COMMENT);
		
		return reconciler;
	}

    public IContentAssistant getContentAssistant(ISourceViewer aSourceViewer)
    {
        ContentAssistant assistant = new ContentAssistant();
        CompletionProcessor completionProcessor = new CompletionProcessor(editor);
        assistant.setContentAssistProcessor(completionProcessor, IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.FOREACH_PARTITION);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.IF_PARTITION);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.MACRO_PARTITION);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.MACRO_INSTANCE_PARTITION);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.INCLUDE_PARTITION);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.PARSE_PARTITION);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.SET_PARTITION);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.STOP_PARTITION);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.VARIABLE_PARTITION);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.XML_TAG);
        assistant.setContentAssistProcessor(completionProcessor, PartitionScanner.XML_DEFAULT);
        assistant.enableAutoInsert(true);
        assistant.enableAutoActivation(true);
        return assistant;
    }
    
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new AnnotationHover();
	}
}