package com.hudson.velocityweb.javascript;

/**
 * @author Joe Hudson
 */
public class JavascriptFunction {
    public String name;
    public String[] parameters;
    public int offset;
    
    public JavascriptFunction (String name, String[] parameters, int offset) {
        this.name = name;
        this.parameters = parameters;
        this.offset = offset;
    }
}
