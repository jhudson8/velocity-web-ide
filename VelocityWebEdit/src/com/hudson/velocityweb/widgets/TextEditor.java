package com.hudson.velocityweb.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.hudson.velocityweb.util.UIUtil;


/**
 * @author Joe Hudson
 */
public class TextEditor {

	public static final int WIDTH_HINT_SHORT = 80;
	public static final int WIDTH_HINT_MEDIUM = 120;
	public static final int WIDTH_HINT_LONG = 200;
	public static final int WIDTH_HINT_EXTRA_LONG = 400;
	
	protected Label label;
	protected String labelRef;
	protected Text text;
	
	protected int labelStyle;
	protected int textStyle;
	protected String defaultValue;
	protected int widthHint;
	protected boolean fillHorizontal;
	protected int colspan;
	protected Composite parent;
	protected Composite textComposite;

	public TextEditor (
			Composite parent,
			String label,
			String defaultValue) {
		this(parent, SWT.NATIVE, SWT.BORDER, label, defaultValue, 0, true, 1, true);
	}

	public TextEditor (
			Composite parent,
			String label,
			String defaultValue,
			boolean exposed) {
		this(parent, SWT.NATIVE, SWT.BORDER, label, defaultValue, 0, true, 1, exposed);
	}

	public TextEditor (
			Composite parent,
			String label,
			int widthHint,
			String defaultValue) {
		this(parent, SWT.NATIVE, SWT.BORDER, label, defaultValue, widthHint, true, 1, true);
	}

	public TextEditor (
			Composite parent,
			String label,
			int widthHint,
			String defaultValue,
			boolean exposed) {
		this(parent, SWT.NATIVE, SWT.BORDER, label, defaultValue, widthHint, true, 1, exposed);
	}

	public TextEditor (
			Composite parent,
			int labelStyle,
			int textStyle,
			String labelRef,
			String defaultValue,
			int widthHint,
			boolean fillHorizontal,
			int colspan,
			boolean exposed) {
		this.parent = parent;
		this.defaultValue = defaultValue;
		this.labelRef = labelRef;
		this.labelStyle = labelStyle;
		this.textStyle = textStyle;
		this.widthHint = widthHint;
		this.fillHorizontal = fillHorizontal;
		this.colspan = colspan;
		if (exposed) {
			addComponents();
		}
	}
	
	public void addComponents () {
		this.label = new Label(parent, labelStyle);
		String s = UIUtil.getResourceLabel(labelRef);
		if (null == s) s = labelRef;
		this.label.setText(s);
		textComposite = new Composite(parent, SWT.NULL);
		textComposite.setLayout(new GridLayout(getColspan(), false));
		textComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		this.text = new Text(textComposite, textStyle);
		if (null != defaultValue) this.text.setText(defaultValue);
		if (fillHorizontal || widthHint > 0 || colspan > 1) {
			GridData gd = null;
			if (fillHorizontal) gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
			else gd = new GridData();
			if (widthHint > 0) gd.widthHint = widthHint;
			if (colspan > 1) gd.horizontalSpan = colspan;
			text.setLayoutData(gd);
		}
	}
	
	protected int getColspan () {
		return 1;
	}

	public Text getText() {
		return text;
	}

	public void setVisibile (boolean visible) {
		text.setVisible(visible);
		label.setVisible(visible);
		textComposite.setVisible(visible);
		parent.redraw();
	}

	public void dispose () {
		if (null != label) label.dispose();
		if (null != text) text.dispose();
		if (null != textComposite) textComposite.dispose();
		label = null;
		text = null;
	}
	
	public void expose () {
		addComponents();
	}
}