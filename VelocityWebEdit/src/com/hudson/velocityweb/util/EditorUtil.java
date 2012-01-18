package com.hudson.velocityweb.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.JavaFileEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.hudson.velocityweb.Plugin;

public class EditorUtil {

	public static int countMarkers (IFile file) throws CoreException {
		return file.findMarkers(null, false, IResource.DEPTH_ONE).length;
	}

	public static void addWarningMarker (IFile file, String message, int line) {
		addMarker(file, message, line, 0, IMarker.SEVERITY_WARNING);
	}

	public static void addProblemMarker (IFile file, String message, int line) {
		addMarker(file, message, line, 0, IMarker.SEVERITY_ERROR);
	}

	public static void addWarningMarker (IFile file, String message, int line, int col) {
		addMarker(file, message, line, col, IMarker.SEVERITY_WARNING);
	}

	public static void addProblemMarker (IFile file, String message, int line, int col) {
		addMarker(file, message, line, col, IMarker.SEVERITY_ERROR);
	}

	public static void addMarker(IFile file, String message, int line, int colPos, int markerType) {
		try {
			Map attributes = new HashMap(5);
			attributes.put(IMarker.SEVERITY, new Integer(markerType));
			attributes.put(IMarker.MESSAGE, message);
			attributes.put(IMarker.TEXT, message);
			if (colPos > 0) {
				attributes.put(IMarker.CHAR_START, new Integer(colPos));
				attributes.put(IMarker.CHAR_END, new Integer(colPos));
			}
			else {
				attributes.put(IMarker.LINE_NUMBER, new Integer(line));
			}
			MarkerUtilities.createMarker(file, attributes, IMarker.PROBLEM);
		} catch (Exception e) {}
	}

	public static ITextEditor openExternalFile (File file, IWorkbenchWindow window) throws PartInitException {
		try {
			if (null != file && file.exists()) {
				IEditorInput input = createEditorInput(file);
				String editorId = getEditorId(file, window);
				IWorkbenchPage page = window.getActivePage();
				return (ITextEditor) page.openEditor(input, "org.eclipse.ui.DefaultTextEditor");
			}
			else return null;
		}
		catch (Exception e) {
			Plugin.log(e);
			return null;
		}
	}

	private static String getEditorId(File file, IWorkbenchWindow window) {
		IWorkbench workbench= window.getWorkbench();
		IEditorRegistry editorRegistry= workbench.getEditorRegistry();
		IEditorDescriptor descriptor= editorRegistry.getDefaultEditor(file.getName());
		if (descriptor != null)
			return descriptor.getId();
		return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
	}

	private static IEditorInput createEditorInput(File file) {
		IFile workspaceFile= getWorkspaceFile(file);
		if (workspaceFile != null)
			return new FileEditorInput(workspaceFile);
		return new ExternalFileEditorInput(file);
	}

	private static IFile getWorkspaceFile(File file) {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IPath location= new Path(file.getAbsolutePath());
		IFile[] files= workspace.getRoot().findFilesForLocation(location);
		if (files == null || files.length == 0)
			return null;
		if (files.length == 1)
			return files[0];
		else return null;
	}
}