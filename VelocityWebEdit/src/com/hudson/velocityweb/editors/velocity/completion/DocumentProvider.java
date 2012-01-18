package com.hudson.velocityweb.editors.velocity.completion;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import com.hudson.velocityweb.editors.velocity.PartitionScanner;

public class DocumentProvider extends FileDocumentProvider {

	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner =
				new DefaultPartitioner(
					new PartitionScanner(),
					new String[] {
						PartitionScanner.XML_TAG,
						PartitionScanner.XML_COMMENT,
						PartitionScanner.FOREACH_PARTITION,
						PartitionScanner.FOREACH_END_PARTITION,
						PartitionScanner.SET_PARTITION,
						PartitionScanner.IF_PARTITION,
						PartitionScanner.ELSE_PARTITION,
						PartitionScanner.ELSE_IF_PARTITION,
						PartitionScanner.IF_END_PARTITION,
						PartitionScanner.END_PARTITION,
						PartitionScanner.MACRO_PARTITION,
						PartitionScanner.MACRO_INSTANCE_PARTITION,
						PartitionScanner.MACRO_END_PARTITION,
						PartitionScanner.VARIABLE_PARTITION,
						PartitionScanner.STOP_PARTITION,
						PartitionScanner.INCLUDE_PARTITION,
						PartitionScanner.PARSE_PARTITION,
						PartitionScanner.COMMENT_PARTITION,
					});
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
}