package com.hudson.velocityweb.editors.velocity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.hudson.velocityweb.Plugin;

public class ColorManager {

	protected Map fColorTable = new HashMap(10);

	public static final String COLOR_FOREACH_DIRECTIVE = "Color.ForeachDirective";
	public static final String COLOR_SET_DIRECTIVE = "Color.SetDirective";
	public static final String COLOR_IF_DIRECTIVE = "Color.IfDirective";
	public static final String COLOR_MACRO_DIRECTIVE = "Color.MacroDirective";
	public static final String COLOR_COMMENT = "Color.VelocityComment";
	public static final String COLOR_VARIABLE = "Color.VelocityVariable";

	public static final String COLOR_XML_COMMENT = "Color.XMLComment";
	public static final String COLOR_STRING = "Color.XMLString";
	public static final String COLOR_TAG = "Color.XMLTag";
	public static final String COLOR_PROC_INSTR = "Color.XMLProcInstr";
	public static final String COLOR_DEFAULT = "Color.XMLDefault";
	public static final String COLOR_LINK = "Color.XMLLink";
	
	private RGB FOREACH_DIRECTIVE = new RGB(128, 0, 0);
	private RGB SET_DIRECTIVE = new RGB(0, 128, 0);
	private RGB IF_DIRECTIVE = new RGB(0, 0, 128);
	private RGB MACRO_DIRECTIVE = new RGB(0, 0, 0);
	private RGB VARIABLE = new RGB(113, 113, 32);
	private RGB COMMENT = new RGB(0, 88, 0);

	private RGB XML_COMMENT = new RGB(128, 0, 0);
	private RGB PROC_INSTR = new RGB(128, 128, 128);
	private RGB STRING = new RGB(0, 128, 0);
	private RGB DEFAULT = new RGB(0, 0, 0);
	private RGB TAG = new RGB(0, 0, 128);
	private RGB LINK = JFaceResources.getColorRegistry().get(JFacePreferences.HYPERLINK_COLOR).getRGB();
	
	private Map defaultColorMap = new HashMap();
	
	public ColorManager () {
		defaultColorMap.put(COLOR_FOREACH_DIRECTIVE, FOREACH_DIRECTIVE);
		defaultColorMap.put(COLOR_SET_DIRECTIVE, SET_DIRECTIVE);
		defaultColorMap.put(COLOR_IF_DIRECTIVE, IF_DIRECTIVE);
		defaultColorMap.put(COLOR_MACRO_DIRECTIVE, MACRO_DIRECTIVE);
		defaultColorMap.put(COLOR_VARIABLE, VARIABLE);
		defaultColorMap.put(COLOR_COMMENT, COMMENT);

		defaultColorMap.put(COLOR_XML_COMMENT, XML_COMMENT);
		defaultColorMap.put(COLOR_STRING, STRING);
		defaultColorMap.put(COLOR_TAG, TAG);
		defaultColorMap.put(COLOR_PROC_INSTR, PROC_INSTR);
		defaultColorMap.put(COLOR_DEFAULT, DEFAULT);
		defaultColorMap.put(COLOR_LINK, LINK);
	}

	public void dispose() {
		Iterator e = fColorTable.values().iterator();
		while (e.hasNext()) ((Color) e.next()).dispose();
		fColorTable.clear();
	}

	public Color getColor(RGB rgb) {
		Color color = (Color) fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}

	public Color getColor (String colorName) {
		RGB rgb = PreferenceConverter.getColor(Plugin.getDefault().getPreferenceStore(), colorName);
		if (null == rgb) rgb = (RGB) defaultColorMap.get(colorName);
		return getColor(rgb);
	}

	public Color getDefaultColor (String colorName) {
		return getColor((RGB) defaultColorMap.get(colorName));
	}
}