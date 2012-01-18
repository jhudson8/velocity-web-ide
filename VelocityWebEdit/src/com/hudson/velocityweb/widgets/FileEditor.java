package com.hudson.velocityweb.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * @author Joe Hudson
 */
public class FileEditor implements SelectionListener {

	public static final int WIDTH_HINT_SHORT = 80;
	public static final int WIDTH_HINT_MEDIUM = 120;
	public static final int WIDTH_HINT_LONG = 200;
	public static final int WIDTH_HINT_EXTRA_LONG = 400;
	
	private Shell shell;
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

	public FileEditor (
	        Shell shell,
			Composite parent,
			String label,
			String defaultValue) {
		this(shell, parent, SWT.NATIVE, SWT.BORDER, label, defaultValue, 0, true, 1, true);
	}

	public FileEditor (
	        Shell shell,
			Composite parent,
			String label,
			String defaultValue,
			boolean exposed) {
		this(shell, parent, SWT.NATIVE, SWT.BORDER, label, defaultValue, 0, true, 1, exposed);
	}

	public FileEditor (
	        Shell shell,
			Composite parent,
			String label,
			int widthHint,
			String defaultValue) {
		this(shell, parent, SWT.NATIVE, SWT.BORDER, label, defaultValue, widthHint, true, 1, true);
	}

	public FileEditor (
	        Shell shell,
			Composite parent,
			String label,
			int widthHint,
			String defaultValue,
			boolean exposed) {
		this(shell, parent, SWT.NATIVE, SWT.BORDER, label, defaultValue, widthHint, true, 1, exposed);
	}

	public FileEditor (
	        Shell shell,
			Composite parent,
			int labelStyle,
			int textStyle,
			String labelRef,
			String defaultValue,
			int widthHint,
			boolean fillHorizontal,
			int colspan,
			boolean exposed) {
	    this.shell = shell;
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
		this.label.setText(labelRef);
		textComposite = new Composite(parent, SWT.NULL);
		textComposite.setLayout(new GridLayout(2, false));
		textComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		((GridLayout) textComposite.getLayout()).horizontalSpacing = 2;
		((GridLayout) textComposite.getLayout()).marginWidth = 0;
		((GridLayout) textComposite.getLayout()).marginHeight = 0;

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
		
		Button button = new Button(textComposite, SWT.NULL);
		button.setText("Browse");
		button.addSelectionListener(this);
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
	
    public void widgetDefaultSelected(SelectionEvent e) {
    }
    public void widgetSelected(SelectionEvent e) {
        FileDialog dialog = new FileDialog(shell);
        dialog.setFilterPath(text.getText());
        String s = dialog.open();
        if (null != s) {
            this.text.setText(s);
        }
    }
}