package com.hudson.velocityweb.editors.velocity;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import com.hudson.velocityweb.Plugin;

public class Contributor extends BasicTextEditorActionContributor {

	private RetargetTextEditorAction fContentAssist;

	/**
	 * Defines the menu actions and their action handlers.
	 */
	public Contributor() {
		createActions();
	}

	protected void createActions() {
		fContentAssist = new RetargetTextEditorAction(
				Plugin.getDefault().getResourceBundle(),
				"VelocityEditor.ContentAssist");
	}

	/**
	 * Sets the active editor to the actions provided by this contributor.
	 * @param aPart the editor
	 */
	private void doSetActiveEditor(IEditorPart aPart) {
		// Set the underlying action (registered by the according editor) in
		// the action handlers
		ITextEditor editor = null;
		if (aPart instanceof ITextEditor) {
			editor = (ITextEditor)aPart;
		}
		fContentAssist.setAction(getAction(editor, "Velocity.ContentAssist"));
	}
}