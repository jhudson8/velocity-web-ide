package com.hudson.velocityweb.manager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hudson.velocityweb.Constants;
import com.hudson.velocityweb.Plugin;
import com.hudson.velocityweb.css.CSSFile;
import com.hudson.velocityweb.editors.velocity.ContextValue;
import com.hudson.velocityweb.editors.velocity.completion.xml.XMLSuggestor;
import com.hudson.velocityweb.editors.velocity.parser.VelocityFile;
import com.hudson.velocityweb.javascript.JavascriptFile;
import com.hudson.velocityweb.util.ProjectClassLoader;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDParser;

/**
 * @author Joe Hudson
 */
public class ConfigurationManager {

	private static final String XML_FILTERING_ENABLED = "FilteringEnabled";
	private static final String DTD_FILTER_ELEMENTS = "DTDFilterElements";
	private static Map DEFAULT_FILTER_ELEMENTS = null;
	static {
		DEFAULT_FILTER_ELEMENTS = new HashMap();
		DEFAULT_FILTER_ELEMENTS.put("form", "form");
		DEFAULT_FILTER_ELEMENTS.put("div", "div");
		DEFAULT_FILTER_ELEMENTS.put("html", "html");
		DEFAULT_FILTER_ELEMENTS.put("body", "body");
		DEFAULT_FILTER_ELEMENTS.put("head", "head");
		DEFAULT_FILTER_ELEMENTS.put("script", "script");
		DEFAULT_FILTER_ELEMENTS.put("script", "script");
		DEFAULT_FILTER_ELEMENTS.put("input", "input");
		DEFAULT_FILTER_ELEMENTS.put("select", "select");
		DEFAULT_FILTER_ELEMENTS.put("a", "a");
		DEFAULT_FILTER_ELEMENTS.put("ul", "ul");
		DEFAULT_FILTER_ELEMENTS.put("ol", "ol");
		DEFAULT_FILTER_ELEMENTS.put("textarea", "textarea");
		DEFAULT_FILTER_ELEMENTS.put("li", "li");
		DEFAULT_FILTER_ELEMENTS.put("table", "table");
		DEFAULT_FILTER_ELEMENTS.put("tr", "tr");
		DEFAULT_FILTER_ELEMENTS.put("td", "td");
		DEFAULT_FILTER_ELEMENTS.put("th", "th");
	}
    private ProjectClassLoader projectClassLoader;

    private static ConfigurationManager instance;

    private IProject project;
    private Map contextValues = new HashMap();
    private DTD dtd;
    private List cssFiles;
    private List macroFiles;
    private List javascriptFiles;
    private Map filterElements;
    private Boolean xmlFilteringEnabled;

    private ConfigurationManager (IProject project) {
        this.project = project;
    }
   
    public static ConfigurationManager getInstance(IProject project) {
        if (null == project) return null;
        if (null == instance || !instance.project.equals(project)) {
            instance = new ConfigurationManager(project);
            instance.reload();
        }
        return instance;
    }

    private void reload() {
        IFile file = project.getFile(".vmweb-config.xml");
        if (file.exists()) {
        	try { file.refreshLocal(1, null); } catch (CoreException e) {}
            Map map = new HashMap();
            try {
                Document document = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder().parse(file.getContents());
                NodeList nl = document.getDocumentElement()
                        .getElementsByTagName("context-values");
                if (nl.getLength() > 0)
                    this.contextValues = loadContextValues((Element) nl.item(0));
                else
                    this.contextValues = new HashMap();
            } catch (Exception e) {
            	e.printStackTrace();
            	Plugin.log(e);
            }
        }
    }

    private  Map loadContextValues(Element element) {
        Map map = new HashMap();
        try {
            NodeList nl = element
                    .getElementsByTagName("resource");
            for (int i = 0; i < nl.getLength(); i++) {
                try {
                    Node n = nl.item(i);
                    String path = ((Element) n).getAttribute("path");
                    List contextValues = new ArrayList();
                    NodeList nl2 = ((Element) n)
                            .getElementsByTagName("value");
                    for (int j = 0; j < nl2.getLength(); j++) {
                        Node n2 = nl2.item(j);
                        String key = ((Element) n2).getAttribute("key");
                        Class value = getClass(((Element) n2)
                                .getAttribute("object-class"));
                        String singularName = ((Element) n2)
                                .getAttribute("item-class");
                        Class singularClass = null;
                        if (null != singularName && singularName.trim().length()>0)
                            singularClass = getClass(singularName);
                        contextValues.add(new ContextValue(key, value,
                                singularClass));
                    }
                    map.put(path,
                            contextValues
                                    .toArray(new ContextValue[contextValues
                                            .size()]));
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
        return map;
    }

    public ContextValue[] getContextValues(IResource resource, boolean recurse) {
        Map newValues = new HashMap();
        addRootContextValues(resource, newValues, recurse);
        return (ContextValue[]) newValues.values().toArray(new ContextValue[newValues.size()]);
    }
    
    private void addRootContextValues(IResource resource, Map newValues, boolean recurse) {
        String key = null;
        if (null != resource.getParent()) {
            key = resource.getProjectRelativePath().toString();
            if (recurse) addRootContextValues(resource.getParent(), newValues, true);
        }
        else key = "";
        if (null != resource.getProject()) {
	        ContextValue[] values = (ContextValue[]) contextValues.get(key);
	        if (null != values) {
	            for (int i=0; i<values.length; i++) {
	                newValues.put(values[i].name, values[i]);
	            }
	        }
        }
    }

    public ContextValue getContextValue(String name, IResource resource, boolean recurse) {
        ContextValue[] values = getContextValues(resource, recurse);
        for (int i = 0; i < values.length; i++) {
            if (values[i].name.equals(name))
                return values[i];
        }
        return null;
    }

    public void addContextValue(ContextValue contextValue, IResource resource) {
        ContextValue[] contextValues = getContextValues(resource, false);
        boolean found = false;
        for (int i = 0; i < contextValues.length; i++) {
            if (contextValues[i].name.equals(contextValue.name)) {
                found = true;
                contextValues[i] = contextValue;
                this.contextValues.put(
                		resource.getProjectRelativePath().toString(),
						contextValues);
                break;
            }
        }
        if (!found) {
            ContextValue[] newContextValues = new ContextValue[contextValues.length + 1];
            int index = 0;
            while (index < contextValues.length) {
                newContextValues[index] = contextValues[index++];
            }
            newContextValues[index] = contextValue;
            this.contextValues.put(resource.getProjectRelativePath().toString(), newContextValues);
        }
        save();
    }

    public void updateContextValue(ContextValue contextValue, IFile file) {
        addContextValue(contextValue, file);
    }

    public void removeContextValue(String name, IResource resource) {
        ContextValue[] values = getContextValues(resource, false);
        int index = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].name.equals(name)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            ContextValue[] newValues = new ContextValue[values.length - 1];
            int j = 0;
            for (int i = 0; i < values.length; i++) {
                if (i != index)
                    newValues[j++] = values[i];
            }
            this.contextValues.put(resource.getProjectRelativePath().toString(), newValues);
            save();
        }
    }

    private void save() {
        StringBuffer sb = new StringBuffer();
        sb.append("<config>\n");
        sb.append("\t<context-values>\n");
        writeContextValues(sb);
        sb.append("\t</context-values>\n");
        sb.append("</config>");
        IFile file = project.getFile(".vmweb-config.xml");
        try {
            if (file.exists())
                file.setContents(new ByteArrayInputStream(sb.toString()
                        .getBytes()), true, true, null);
            else
                file.create(new ByteArrayInputStream(sb.toString().getBytes()),
                        true, null);
        } catch (Exception e) {
            Plugin.log(e);
        }
        reload();
    }

    private void writeContextValues(StringBuffer sb) {
        for (Iterator i = contextValues.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String fileName = (String) entry.getKey();
            ContextValue[] values = (ContextValue[]) entry.getValue();
            if (null != values && values.length > 0) {
                sb.append("\t\t<resource path=\"" + fileName + "\">\n");
                for (int j = 0; j < values.length; j++) {
                    sb.append("\t\t\t<value key=\"" + values[j].name
                            + "\" object-class=\"" + values[j].objClass.getName()
                            + "\"");
                    if (null != values[j].singularClass)
                        sb.append(" item-class=\""
                                + values[j].singularClass.getName() + "\"");
                    sb.append("/>\n");
                }
                sb.append("\t\t</resource>\n");
            }
        }
    }

    public Class getClass(String className)
            throws JavaModelException, ClassNotFoundException {
        if (null == this.projectClassLoader)
            this.projectClassLoader = new ProjectClassLoader(JavaCore
                    .create(project));
        return this.projectClassLoader.loadClass(className);
    }

    public DTD getDTD() throws IOException {
    	if (null == dtd) {
    		InputStream is = null;
    		try {
    			String fileName = project.getPersistentProperty(Constants.newQualifiedName(Constants.FILE_DTD));
    			if (null != fileName) {
    				File f = new File(fileName);
    				if (f.exists()) is = new FileInputStream(f);
    			}
    		}
    		catch (Exception e) {}
    		try {
    			if (null == is) is = XMLSuggestor.class.getResourceAsStream("/com/hudson/velocityweb/editors/velocity/completion/html.dtd");
    			DTDParser parser = new DTDParser(new InputStreamReader(is));
    			try {
    				dtd = parser.parse();
    			}
    			catch (IOException ioe) {
    				Plugin.log(ioe);
    				throw ioe;
    			}
    		}
    		finally {
    			if (null != is) try { is.close(); } catch (IOException e) {}
    		}
    	}
    	return dtd;
    }

    public void setDTDFile (String fileName) {
    	try {
    		project.setPersistentProperty(Constants.newQualifiedName(Constants.FILE_DTD), fileName);
    		dtd = null;
    	}
    	catch (Exception e) {}
    }

    public boolean isDTDFilterElement (String name) {
    	return null != getDTDFilterElements().get(name);
    }

    public Map getDTDFilterElements () {
    	if (null == filterElements) {
    		try {
    			String s = project.getPersistentProperty(Constants.newQualifiedName(DTD_FILTER_ELEMENTS));
    			if (null == s) {
    				filterElements = DEFAULT_FILTER_ELEMENTS;
    			}
    			else {
    				filterElements = new HashMap();
    				StringTokenizer st = new StringTokenizer(s, ",");
    				while (st.hasMoreTokens()) {
    					String s1 = st.nextToken();
    					filterElements.put(s1, s1);
    				}
    			}
    		}
    		catch (CoreException e) {
    			filterElements = new HashMap();
    		}
    	}
    	return filterElements;
    }

    public void setDTDFilterElements (String[] elements) throws CoreException {
    	StringBuffer sb = new StringBuffer();
    	for (int i=0; i<elements.length; i++) {
    		if (i>0) sb.append(",");
    		sb.append(elements[i]);
    	}
    	project.setPersistentProperty(
    			Constants.newQualifiedName(DTD_FILTER_ELEMENTS),
    			sb.toString());
    	filterElements = null;
    }

    public boolean isXMLFilteringEnabled () {
    	if (null == xmlFilteringEnabled) {
    		try {
    			String s = project.getPersistentProperty(
    					Constants.newQualifiedName(XML_FILTERING_ENABLED));
    			if (null != s) xmlFilteringEnabled = new Boolean(s);
    			else xmlFilteringEnabled = Boolean.TRUE;
    		}
    		catch (CoreException e) {}
    	}
    	return xmlFilteringEnabled.booleanValue();
    }

    public void setXMLFiltering (boolean enabled) {
		try {
	    	project.setPersistentProperty(
	    			Constants.newQualifiedName(XML_FILTERING_ENABLED),
	    			Boolean.toString(enabled));
			xmlFilteringEnabled = null;
		}
		catch (CoreException e) {}
    }

	public static Image getElementImage (String elementName) {
	    if (elementName.equals("form")) return Plugin.getDefault().getImage("tag_form");
	    else if (elementName.equals("div")) return Plugin.getDefault().getImage("tag_div");
	    else if (elementName.equals("html")) return Plugin.getDefault().getImage("tag_html");
	    else if (elementName.equals("body")) return Plugin.getDefault().getImage("tag_body");
	    else if (elementName.equals("head")) return Plugin.getDefault().getImage("tag_head");
	    else if (elementName.equals("script")) return Plugin.getDefault().getImage("tag_script");
	    else if (elementName.equals("style")) return Plugin.getDefault().getImage("tag_style");
	    else if (elementName.equals("input")) return Plugin.getDefault().getImage("input_text");
	    else if (elementName.equals("select")) return Plugin.getDefault().getImage("input_select");
	    else if (elementName.equals("a")) return Plugin.getDefault().getImage("tag_link");
	    else if (elementName.equals("ul") || elementName.equals("ol")) return Plugin.getDefault().getImage("tag_list");
	    else if (elementName.equals("li")) return Plugin.getDefault().getImage("tag_li");
	    else if (elementName.equals("table")) return Plugin.getDefault().getImage("tag_table");
	    else if (elementName.equals("tr")) return Plugin.getDefault().getImage("tag_tr");
	    else if (elementName.equals("td")) return Plugin.getDefault().getImage("tag_td");
	    else if (elementName.equals("th")) return Plugin.getDefault().getImage("tag_td");
	    else if (elementName.equals("textarea")) return Plugin.getDefault().getImage("tag_textarea");
	    else return Plugin.getDefault().getImage("starttag");
	}
	
	public List getCSSFiles () {
	    if (null == cssFiles) {
	        cssFiles = new ArrayList();
	        try {
		        String s = project.getPersistentProperty(Constants.newQualifiedName(Constants.DIR_CSS));
		        if (null == s) return cssFiles;
		        File dir = new File(s);
		        if (!dir.exists()) return cssFiles;
		        File[] files = dir.listFiles();
		        for (int i=0; i<files.length; i++) {
		            if (!files[i].isDirectory()) {
		                CSSFile css = new CSSFile(files[i]);
		                cssFiles.add(css);
		            }
		        }
	        }
	        catch (CoreException e) {}
	    }
	    return cssFiles;
	}

	public List getMacroFiles () {
	    if (null == macroFiles) {
	    	macroFiles = new ArrayList();
	        try {
		        String s = project.getPersistentProperty(Constants.newQualifiedName(Constants.DIR_MACROS));
		        if (null == s) return macroFiles;
		        File dir = new File(s);
		        if (!dir.exists()) return macroFiles;
		        File[] files = dir.listFiles();
		        for (int i=0; i<files.length; i++) {
		            if (!files[i].isDirectory()) {
		            	VelocityFile vf = new VelocityFile(files[i]);
		            	macroFiles.add(vf);
		            }
		        }
	        }
	        catch (CoreException e) {}
	    }
	    return macroFiles;
	}

	public List getJavascriptFiles () {
	    if (null == javascriptFiles) {
	    	javascriptFiles = new ArrayList();
	        try {
		        String s = project.getPersistentProperty(Constants.newQualifiedName(Constants.DIR_JAVASCRIPT));
		        if (null == s) return javascriptFiles;
		        File dir = new File(s);
		        if (!dir.exists()) return javascriptFiles;
		        File[] files = dir.listFiles();
		        for (int i=0; i<files.length; i++) {
		            if (!files[i].isDirectory()) {
		            	JavascriptFile jsf = new JavascriptFile(files[i]);
		            	javascriptFiles.add(jsf);
		            }
		        }
	        }
	        catch (CoreException e) {}
	    }
	    return javascriptFiles;
	}

	public void clearCache () {
		this.dtd = null;
		this.cssFiles = null;
		this.macroFiles = null;
		this.javascriptFiles = null;
		this.projectClassLoader = null;
		reload();
	}
	
	public void clearProjectClassLoader () {
		this.projectClassLoader = null;
		reload();
	}
}