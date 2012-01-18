package com.hudson.velocityweb.editors.velocity;

/**
 * @author Joe Hudson
 */
public class ContextValue {

    public String name;
    public Class objClass;
    public Class singularClass;

    public ContextValue (String name, Class objClass, Class singularClass) {
        this.name = name;
        this.objClass = objClass;
        this.singularClass = singularClass;
    }
}
