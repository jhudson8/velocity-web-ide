package com.hudson.velocityweb.editors.velocity.parser;

/**
 * @author Joe Hudson
 */
public class VelocityMacro {

    public String name;
    public String[] parameters;
    public int offset;

    public VelocityMacro (String name, String[] parameters, int offset) {
        this.name = name;
        this.parameters = parameters;
        this.offset = offset;
    }
}
