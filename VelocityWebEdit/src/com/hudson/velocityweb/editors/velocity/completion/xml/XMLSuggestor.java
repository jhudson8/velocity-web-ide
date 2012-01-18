package com.hudson.velocityweb.editors.velocity.completion.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.contentassist.CompletionProposal;

import com.hudson.velocityweb.editors.velocity.completion.Attribute;
import com.hudson.velocityweb.manager.ConfigurationManager;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDChoice;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDItem;
import com.wutka.dtd.DTDMixed;
import com.wutka.dtd.DTDName;
import com.wutka.dtd.DTDSequence;



/**
 * @author Joe Hudson
 */
public class XMLSuggestor {

	private static HashSet FLAT_VALUEs_SET;

	private static Comparator PROPOSAL_COMPARATOR = new Comparator() {

		public int compare(Object aProposal1, Object aProposal2) {
			String text1 = ((CompletionProposal) aProposal1).getDisplayString();
			String text2 = ((CompletionProposal) aProposal2).getDisplayString();

			return text1.compareTo(text2);
		}

		public boolean equals(Object aProposal) {
			return false;
		}
	};

	private static final String[] flatValues = new String[] { "compact",
			"checked", "declare", "readonly", "disabled", "selected", "defer",
			"ismap", "nohref", "noshade", "nowrap", "multiple", "noresize" };

	static {
		FLAT_VALUEs_SET = new HashSet();

		for (int i = 0; i < flatValues.length; i++) {
			FLAT_VALUEs_SET.add(flatValues[i]);
		}
	}
	
	
	public static String[] getNodeSuggestions (Node parentNode, IFile file) {
	    String parentNodeName = null;
		if (null != parentNode && null != parentNode.getName()) parentNodeName = parentNode.getName();
		else parentNodeName = "body";
		DTDElement element = getHTMLElement(parentNodeName, file);
		if (null != element) {
		    List list = new ArrayList();
			DTDItem item = element.getContent();
			dumpDTDItem(item, list);
			return (String[]) list.toArray(new String[list.size()]);
		}
		else return null;
	}

	public static List getAttributeSuggestions (Node node, Attribute currentAttribute, IFile file) {
		if (null == currentAttribute) return getAttributeSuggestions(node, (String) null, file);
		else return getAttributeSuggestions(node, currentAttribute.getName(), file);
	}

	public static List getAttributeSuggestions (Node node, String currentAttributeName, IFile file) {
		if (null == node) return null;
		DTDElement element = getHTMLElement(node.getName(), file);
		if (null == element) return null;
		Hashtable table = element.attributes;
		Collection collection = table.values();
		List attributes = new ArrayList();
		if (collection.size() > 0) {
			for (Iterator iter = collection.iterator(); iter.hasNext();) {
				DTDAttribute attrib = (DTDAttribute) iter.next();
				if (FLAT_VALUEs_SET.contains(attrib.getName())) {
				    attributes.add(attrib.getName());
				}
				else {
				    attributes.add(attrib.getName());
				}
			}
		}
		for (int i=attributes.size()-1; i>=0; i--) {
		    String s = (String) attributes.get(i);
			boolean used = false;
			for (Iterator iter1 = node.getAttributes().iterator(); iter1.hasNext(); ) {
				Attribute att = (Attribute) iter1.next();
				if (att.getName().equalsIgnoreCase(s)) {
					used = true;
					break;
				}
			}
			if (used && !(null != currentAttributeName && currentAttributeName.length() > 0 && s.startsWith(currentAttributeName))) {
				attributes.remove(i);
			}
		}
		return attributes;
	}

	public static String[] getAttributeValueSuggestions (String nodeName, String attributeName) {
//		NodeAttribute attr = new NodeAttribute(nodeName, attributeName);
//		return (String[]) valueSuggestions.get(attr);
	    return new String[0];
	}
	
    public static DTD dtd;
    public static DTDElement getHTMLElement(String name, IFile file) {
    	try {
    		return (DTDElement) ConfigurationManager.getInstance(file.getProject()).getDTD().elements.get(name);
    	}
    	catch (IOException e) {}
    	return null;
    }

	public static void dumpDTDItem(DTDItem item, List list) {
		if (item == null) {
			return;
		}

		else if (item instanceof DTDName) {
			list.add(((DTDName) item).value);
		} else if (item instanceof DTDChoice) {
			DTDItem[] items = ((DTDChoice) item).getItems();

			for (int i = 0; i < items.length; i++) {
				dumpDTDItem(items[i], list);
			}
		} else if (item instanceof DTDSequence) {
			DTDItem[] items = ((DTDSequence) item).getItems();

			for (int i = 0; i < items.length; i++) {
				dumpDTDItem(items[i], list);
			}
		} else if (item instanceof DTDMixed) {
			DTDItem[] items = ((DTDMixed) item).getItems();

			for (int i = 0; i < items.length; i++) {
				dumpDTDItem(items[i], list);
			}
		}
	}
}