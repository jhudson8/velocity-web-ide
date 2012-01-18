package com.hudson.velocityweb.widgets;


import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.hudson.velocityweb.util.UIUtil;


/**
 * @author Joe Hudson
 */
public class BooleanEditor {

	private BooleanFieldEditor editor;
	private Composite parent;
	private Composite subComp;
	private String name;
	private String labelRef;
	private Boolean defaultValue;
	private int colspan;

	public BooleanEditor (
			Composite parent,
			String name,
			String labelRef,
			Boolean defaultValue,
			int colspan) {
		this.parent = parent;
		this.name = name;
		this.labelRef = labelRef;
		this.defaultValue = defaultValue;
		this.colspan = colspan;
		load();
	}
	
	public boolean getBooleanValue () {
		return editor.getBooleanValue();
	}
	
	private void load () {
		subComp = new Composite(parent, SWT.NULL);
		GridData gridData = new GridData();
		gridData.horizontalSpan = colspan;
		subComp.setLayoutData(gridData);
		String s = UIUtil.getResourceLabel(labelRef);
		if (null == s) s = labelRef;
		this.editor = new BooleanFieldEditor(name, s, subComp);
		if (null != defaultValue) {
			PreferenceStore ps = new PreferenceStore();
			ps.setValue(name, defaultValue.booleanValue());
			editor.setPreferenceStore(ps);
			editor.load();
		}
	}

	public void dispose() {
		if (null != this.editor) this.editor.dispose();
		if (null != subComp) this.subComp.dispose();
		this.editor = null;
		this.subComp = null;
	}
	public void expose() {
		load();
	}
	public BooleanFieldEditor getEditor () {
		return editor;
	}
}