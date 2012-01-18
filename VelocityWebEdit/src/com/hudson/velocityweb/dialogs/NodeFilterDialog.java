package com.hudson.velocityweb.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.hudson.velocityweb.manager.ConfigurationManager;
import com.hudson.velocityweb.widgets.BooleanEditor;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDElement;

/**
 * @author Joe Hudson
 */
public class NodeFilterDialog extends Dialog {

	private Table table;
	IProject project;
	
	private BooleanEditor filteringEnabled;

	/**
	 * @param parentShell
	 */
	public NodeFilterDialog(Shell parentShell, IProject project) {
		super(parentShell);
		this.project = project;
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		newShell.setText("Visible Element Tags");
		super.configureShell(newShell);
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		
		filteringEnabled = new BooleanEditor(
				container,
				"FilteringEnabled",
				"FilteringEnabled",
				new Boolean(ConfigurationManager.getInstance(project).isXMLFilteringEnabled()),
				1);
		
		table = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);
		table.setLinesVisible (false);
		table.setHeaderVisible(false);
		GridData data = new GridData (GridData.FILL_BOTH);
		data.heightHint = 200;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		table.setLayoutData(data);

		try {
			DTD dtd = ConfigurationManager.getInstance(project).getDTD();
			if (null != dtd) {
				List elements = new ArrayList(dtd.elements.values());
				Collections.sort(elements, new ElementComparator());
				for (Iterator i=elements.iterator(); i.hasNext(); ) {
					DTDElement element = (DTDElement) i.next();
					TableItem item = new TableItem(table, SWT.NULL);
					item.setText(element.getName());
					if (ConfigurationManager.getInstance(project).isDTDFilterElement(element.getName()))
						item.setChecked(true);
						item.setImage(ConfigurationManager.getElementImage(element.getName()));
				}
			}
		}
		catch (IOException e) {}
		return container;
	}

	protected void okPressed() {
		TableItem[] items = table.getItems();
		List selectedItems = new ArrayList();
		for (int i=0; i<items.length; i++) {
			if (items[i].getChecked()) selectedItems.add(items[i].getText());
		}
		try {
			ConfigurationManager.getInstance(project).setDTDFilterElements((String[]) selectedItems.toArray(new String[selectedItems.size()]));
		}
		catch (CoreException e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			return;
		}
		ConfigurationManager.getInstance(project).setXMLFiltering(filteringEnabled.getBooleanValue());
		super.okPressed();
	}

	public class ElementComparator implements Comparator {
		
		public int compare(Object o1, Object o2) {
			return ((DTDElement) o1).getName().compareTo(((DTDElement) o2).getName());
		}
	}
}