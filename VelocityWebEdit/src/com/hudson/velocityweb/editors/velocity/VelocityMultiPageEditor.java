package com.hudson.velocityweb.editors.velocity;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.hudson.velocityweb.Plugin;
import com.hudson.velocityweb.dialogs.ContextValueDialog;
import com.hudson.velocityweb.manager.ConfigurationManager;


public class VelocityMultiPageEditor extends MultiPageEditorPart {

    OleControlSite controlSitePreview;
    OleFrame framePreview;
    OleAutomation automationPreview;
    OleControlSite controlSiteView;
    OleFrame frameView;
    OleAutomation automationView;
    private Editor vEditor;
    private String text;

    static String currentProject;
    static Map projectClassLoaders = new HashMap();
    static Map currentProjectCacheMap = new HashMap();

    protected void createPages()
    {
        createPage0();
        createContextPage();
    }

    void createPage0()
    {
        try {
            vEditor = new Editor();
            int index = addPage(vEditor, getEditorInput());
            setPageText(index, "Source");
            setPartName(vEditor.getTitle());
        }
        catch (PartInitException e) {
            ErrorDialog.openError(getSite().getShell(), "Error creating nested text vEditor", null, e.getStatus());
        }
    }

    public Editor getEditor () {
    	return vEditor;
    }

    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
        if (newPageIndex == 1) {
            reloadContextValues();
        }
    }
    private Table contextValuesTable;
	private Button editContextValueButton;
	private Button deleteContextValueButton;
	private Button addContextValueButton;
	private Properties contextValues;
    void createContextPage() {
    	contextValues = new Properties();
    	Composite composite = new Composite(getContainer(), SWT.NULL);
    	composite.setLayout(new FillLayout());
    	contextValuesTable = new Table(composite, SWT.BORDER | SWT.H_SCROLL | SWT.FULL_SELECTION);
    	contextValuesTable.setVisible(true);
    	contextValuesTable.setLinesVisible (false);
    	contextValuesTable.setHeaderVisible(true);
    	contextValuesTable.addSelectionListener(new SelectionListener () {
			public void widgetSelected(SelectionEvent e) {
				editContextValueButton.setEnabled(true);
				deleteContextValueButton.setEnabled(true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
    	contextValuesTable.addKeyListener(new ContextValueDeleteKeyListener());
    	contextValuesTable.addMouseListener(new EditContextValueButtonListener());

		// create the columns
		TableColumn keyColumn = new TableColumn(contextValuesTable, SWT.LEFT);
		TableColumn valueColumn = new TableColumn(contextValuesTable, SWT.LEFT);
		keyColumn.setText("Reference");
		valueColumn.setText("Object");
		ColumnLayoutData keyColumnLayout = new ColumnWeightData(30, false);
		ColumnLayoutData valueColumnLayout = new ColumnWeightData(70, false);

		// set columns in Table layout
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(keyColumnLayout);
		tableLayout.addColumnData(valueColumnLayout);
		contextValuesTable.setLayout(tableLayout);

		GridData data = new GridData (GridData.FILL_BOTH);
		data.heightHint = 50;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		contextValuesTable.setLayoutData(data);
		
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		data = new GridData ();
		data.horizontalAlignment = GridData.BEGINNING;
		data.verticalAlignment = GridData.BEGINNING;
		buttonComposite.setLayoutData(data);
		GridLayout gl = new GridLayout(1, true);
		buttonComposite.setLayout(gl);
		buttonComposite.setVisible(true);
		addContextValueButton = new Button(buttonComposite, SWT.NATIVE);
		addContextValueButton.setText("New");
		addContextValueButton.setVisible(true);
		addContextValueButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
		addContextValueButton.addSelectionListener(new AddContextValueButtonListener());
		data = new GridData();
		data.widthHint = 45;
		data.grabExcessHorizontalSpace = true;
		addContextValueButton.setLayoutData(data);
		editContextValueButton = new Button(buttonComposite, SWT.NATIVE);
		editContextValueButton.setText("Edit");
		editContextValueButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
		editContextValueButton.addSelectionListener(new EditContextValueButtonListener());
		data = new GridData();
		data.widthHint = 45;
		data.grabExcessHorizontalSpace = true;
		editContextValueButton.setLayoutData(data);
		deleteContextValueButton = new Button(buttonComposite, SWT.NATIVE);
		deleteContextValueButton.setText("Delete");
		deleteContextValueButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
		deleteContextValueButton.addSelectionListener(new ContextValueDeleteKeyListener());
		data = new GridData();
		data.widthHint = 45;
		data.grabExcessHorizontalSpace = true;
		deleteContextValueButton.setLayoutData(data);
		
		reloadContextValues();
        int index = addPage(composite);
        setPageText(index, "Context");
    }


	public void reloadContextValues () {
		try {
			contextValuesTable.removeAll();
			ContextValue[] values = ConfigurationManager.getInstance(vEditor.getFile().getProject()).getContextValues(vEditor.getFile(), false);
			for (int i=0; i<values.length; i++) {
				TableItem item = new TableItem(contextValuesTable, SWT.NULL);
				String[] arr = {values[i].name, values[i].objClass.getName()};
				item.setText(arr);
			}
			editContextValueButton.setEnabled(false);
			deleteContextValueButton.setEnabled(false);
		}
		catch (Exception e) {
			Plugin.log(e);
		}
		contextValuesTable.redraw();
	}

	public class AddContextValueButtonListener implements SelectionListener {
		public void mouseDoubleClick(MouseEvent e) {
			doWork();
		}
		public void mouseDown(MouseEvent e) {}
		public void mouseUp(MouseEvent e) {}
		public void widgetSelected(SelectionEvent e) {
			doWork();
		}
		public void widgetDefaultSelected(SelectionEvent e) {}

		public void doWork() {
			ContextValueDialog dialog = new ContextValueDialog(new Shell(), null, vEditor.getFile());
			if (IDialogConstants.OK_ID == dialog.open()) {
				reloadContextValues();
			}
		}
	}

	public class EditContextValueButtonListener implements SelectionListener, MouseListener {
		public void mouseDoubleClick(MouseEvent e) {
			doWork();
		}
		public void mouseDown(MouseEvent e) {}
		public void mouseUp(MouseEvent e) {}
		public void widgetSelected(SelectionEvent e) {
			doWork();
		}
		public void widgetDefaultSelected(SelectionEvent e) {}

		public void doWork() {
			int index = contextValuesTable.getSelectionIndex();
			if (index >= 0) {
				String key = contextValuesTable.getSelection()[0].getText(0);
				ContextValue value = ConfigurationManager.getInstance(vEditor.getFile().getProject()).getContextValue(key, vEditor.getFile(), false);
				ContextValueDialog dialog = new ContextValueDialog(new Shell(), value, vEditor.getFile());
				if (IDialogConstants.OK_ID == dialog.open()) {
					reloadContextValues();
				}
			}
		}
	}
	
	public class ContextValueDeleteKeyListener implements SelectionListener, KeyListener {
		public void widgetSelected(SelectionEvent e) {
			doWork();
		}
		public void widgetDefaultSelected(SelectionEvent e) {}
		public void keyPressed(KeyEvent e) {
			if (e.keyCode == SWT.DEL) {
				doWork();
			}
		}
		public void keyReleased(KeyEvent e) {}

		public void doWork () {
			int index = contextValuesTable.getSelectionIndex();
			if (index >= 0) {
				try {
					boolean confirm = MessageDialog.openConfirm(new Shell(), "Confirmation", "Are you sure you want to delete this context value?");
					if (confirm) {
						String key = contextValuesTable.getSelection()[0].getText(0);
						ContextValue value = ConfigurationManager.getInstance(vEditor.getFile().getProject()).getContextValue(key, vEditor.getFile(), false);
						ConfigurationManager.getInstance(vEditor.getFile().getProject()).removeContextValue(value.name, vEditor.getFile());
						reloadContextValues();
					}
				}
				catch (Exception e1) {
					Plugin.log(e1);
				}
			}
		}
	}

    /**
     * Saves the multi-page vEditor's document.
     */
    public void doSave(IProgressMonitor monitor)
    {
        getEditor(0).doSave(monitor);
    }

    /**
     * Saves the multi-page vEditor's document as another file. Also updates the
     * text for page 0's tab, and updates this multi-page vEditor's input to
     * correspond to the nested vEditor's.
     */
    public void doSaveAs()
    {
        IEditorPart editor = getEditor(0);
        editor.doSaveAs();
        setPageText(0, editor.getTitle());
        setInput(editor.getEditorInput());
    }

    public void gotoMarker(IMarker marker)
    {
        setActivePage(0);
    }

    /**
     * The <code>MultiPageEditorExample</code> implementation of this method
     * checks that the input is an instance of <code>IFileEditorInput</code>.
     */
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException
    {
        if (!(editorInput instanceof IFileEditorInput)) { throw new PartInitException("Invalid Input: Must be IFileEditorInput"); }
        super.init(site, editorInput);
    }

    public boolean isSaveAsAllowed()
    {
        return true;
    }

    public Object getAdapter(Class aClass)
    {
        return vEditor.getAdapter(aClass);
    }
}