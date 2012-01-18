package com.hudson.velocityweb.editors.velocity.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.ParseException;

/**
 * @author Joe Hudson
 */
public class VelocityMacroParser extends RuntimeInstance {

    private static VelocityMacroParser instance = new VelocityMacroParser();
    private List fMacros = new ArrayList();
    private String content;

    static {
        try {
            instance.init();
        }
        catch (Exception e) {}
    }
    
    public static VelocityMacro[] parse (String content) {
        try {
        	instance.content = content;
            return instance._parse(content);
        }
        catch (ParseException e) {
            return new VelocityMacro[0];
        }
    }
    
    private VelocityMacro[] _parse (String content) throws ParseException {
        this.fMacros.clear();
        this.parse(new InputStreamReader(new ByteArrayInputStream(content.getBytes())), "null");
        return (VelocityMacro[]) fMacros.toArray(new VelocityMacro[fMacros.size()]);
    }

    public boolean addVelocimacro(String name, String macro, String[] argArray, String sourceTemplate)
    {
        if (null != argArray && argArray.length > 0) {
            if (argArray[0].equals(name)) {
                String[] newArgArray = new String[argArray.length-1];
                for (int i=1; i<argArray.length; i++) {
                    newArgArray[i-1] = argArray[i];
                }
                argArray = newArgArray;
            }
        }
        int index = content.indexOf("(" + name + " ");
        if (index < 0) index = content.indexOf("( " + name + " ");
        if (index < 0) index = content.indexOf("(" + name + ")");
        if (index < 0) index = content.indexOf(" (" + name + ")");
        if (index < 0) index = 0;
        fMacros.add(new VelocityMacro(name, argArray, index));
        return true;
    }
}
