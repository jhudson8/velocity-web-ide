package com.hudson.velocityweb.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.hudson.velocityweb.Plugin;

/**
 * @author Joe Hudson
 */
public class UIUtil {

	public static String LABEL = "label.";
	public static String ERROR = "error.";
	public static String TITLE = "title.";
	public static String TEXT = "text.";
	public static TabItem createNewTabItem(String labelRef, TabFolder folder, int numColumns) {
		TabItem tabItem = new TabItem(folder, SWT.NONE);
		Composite composite = new Composite(folder, SWT.NONE);
		composite.setLayout(new GridLayout(numColumns, false));		
		tabItem.setText(getResourceLabel(labelRef));
		tabItem.setControl(composite);
		return tabItem;
	}

	public static Button createButton (String labelRef, Composite composite) {
		return createButton(labelRef, composite, 1);
	}

	public static Button createButton (String labelRef, Composite composite, int colspan) {
		Button button = new Button(composite, SWT.NATIVE);
		button.setText(getResourceLabel(labelRef));
		button.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		if (colspan > 1) {
			GridData gd = new GridData();
			gd.horizontalSpan = colspan;
			button.setLayoutData(gd);
		}
		return button;
	}
	
	public static List createEditButtons (Composite parent, boolean horiz) {
		Composite composite = new Composite(parent, SWT.NULL);
		if (horiz) {
			FillLayout layout = new FillLayout(SWT.HORIZONTAL);
			layout.spacing = 2;
			layout.marginHeight = 1;
			composite.setLayout(layout);
		}
		else {
			FillLayout layout = new FillLayout(SWT.VERTICAL);
			layout.spacing = 2;
			layout.marginWidth = 4;
			composite.setLayout(layout);
		}
		ArrayList list = new ArrayList();
		list.add(UIUtil.createButton("New", composite));
		list.add(UIUtil.createButton("Edit", composite));
		list.add(UIUtil.createButton("Delete", composite));
		return list;
	}
	
	public static void validationError(String textRef, Shell shell) {
		MessageDialog.openError(shell, getResourceTitle("ValidationError"), getResourceError(textRef));
	}

	public static boolean confirmDelete(String labelRef, Shell shell) {
		return MessageDialog.openQuestion(shell, getResourceTitle("Confirmation"), getResourceText("ConfirmDelete") + " " + getResourceLabel(labelRef) + "?");
	}

	public static boolean confirm(String textRef, Shell shell) {
		return MessageDialog.openQuestion(shell, getResourceTitle("Confirmation"), getResourceText(textRef));
	}

	public static void pluginError(String textRef, Shell shell) {
		MessageDialog.openError(shell, getResourceTitle("PluginError"), getResourceError(textRef));
	}

	public static void pluginError(Throwable t, Shell shell) {
		MessageDialog.openError(shell, getResourceTitle("PluginError"), t.getMessage());
	}

	public static String getResourceLabel (String ref) {
		return Plugin.getResourceString(LABEL + ref);
	}

	public static String getResourceText (String ref) {
		return Plugin.getResourceString(TEXT + ref);
	}

	public static String getResourceError (String ref) {
		return Plugin.getResourceString(ERROR + ref);
	}

	public static String getResourceTitle (String ref) {
		return Plugin.getResourceString(TITLE + ref);
	}
}