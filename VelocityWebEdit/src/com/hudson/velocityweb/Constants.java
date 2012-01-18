package com.hudson.velocityweb;

import org.eclipse.core.runtime.QualifiedName;

/**
 * @author Joe Hudson
 */
public class Constants {

    public static final String DIR_JAVASCRIPT = "JavascriptDirectory";
    public static final String DIR_MACROS = "MacrosDirectory";
    public static final String DIR_CSS = "CSSDirectory";
    public static final String FILE_DTD = "DTDFile";

    public static QualifiedName newQualifiedName (String name) {
        return new QualifiedName(Plugin.PLUGIN_ID, name);
    }
}
