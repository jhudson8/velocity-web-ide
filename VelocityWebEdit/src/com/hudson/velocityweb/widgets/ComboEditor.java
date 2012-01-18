package com.hudson.velocityweb.widgets;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.hudson.velocityweb.util.UIUtil;


/**
 * @author Joe Hudson
 */
public class ComboEditor {
	private String labelRef;
	private Label label;
	private Combo combo;
	private Composite parent;
	private boolean readOnly;
	private List elements = new ArrayList();
	private String defaultValue;

	public ComboEditor (
			Composite parent,
			String labelRef) {
		this(parent, labelRef, true, true);
	}

	public ComboEditor (
			Composite parent,
			String labelRef,
			boolean readOnly) {
		this(parent, labelRef, readOnly, true);
	}

	public ComboEditor (
			Composite parent,
			String labelRef,
			boolean readOnly,
			boolean exposed) {
		this.readOnly = readOnly;
		this.labelRef = labelRef;
		this.parent = parent;
		if (exposed) {
			this.label = new Label(parent, SWT.NULL);
			String s = UIUtil.getResourceLabel(labelRef);
			if (null == s) s = labelRef;
			this.label.setText(s);
			int flags = SWT.READ_ONLY;
			if (!readOnly)
				flags = SWT.NULL;
			this.combo = new Combo(parent, flags);
		}
	}
	
	public String getSelection () {
		if (combo.getSelectionIndex() >= 0)
			return combo.getItem(combo.getSelectionIndex());
		else
			return combo.getText();
	}
	
	public void add (String text) {
		elements.add(text);
		if (null != combo) {
			combo.add(text);
			if (combo.getItemCount() == 1)
				combo.select(0);
		}
	}

	public Combo getCombo() {
		return combo;
	}
	
	public void removeAll () {
		combo.removeAll();
		elements.clear();
	}
	
	public void setVisibile (boolean visible) {
		combo.setVisible(visible);
		label.setVisible(visible);
	}
	
	public void dispose () {
		if (null != label) label.dispose();
		if (null != combo) combo.dispose();
		combo = null;
		label = null;
	}
	
	public void expose () {
		this.label = new Label(parent, SWT.NULL);
		this.label.setText(UIUtil.getResourceLabel(labelRef));
		int flags = SWT.NULL;
		if (!readOnly)
			flags = SWT.NULL;
		this.combo = new Combo(parent, flags);
		for (Iterator i=elements.iterator(); i.hasNext(); ) {
			combo.add((String) i.next());
		}
		if (combo.getItemCount() > 0) combo.select(0);
	}
	
	public void select (String entry) {
		combo.deselectAll();
		if (null != entry) {
			for (int i=0; i<combo.getItemCount(); i++) {
				if (entry.equals(combo.getItem(i))) {
					combo.select(i);
					break;
				}
			}
		}
	}
}