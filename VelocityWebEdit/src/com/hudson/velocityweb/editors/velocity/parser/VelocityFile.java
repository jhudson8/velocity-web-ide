package com.hudson.velocityweb.editors.velocity.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hudson.velocityweb.util.StringUtil;

/**
 * @author Joe Hudson
 */
public class VelocityFile {
    public long lastModTime;
    public File file;
    private VelocityMacro[] macros;
    
    public VelocityFile (File file) {
        this.lastModTime = file.lastModified();
        this.file = file;
    }
    
    public VelocityFile (VelocityMacro[] macros) {
        this.macros = macros;
    }
        
    public VelocityMacro[] getMacros() {
        if (null != file && file.lastModified() > this.lastModTime || null == macros) {
            try {
                parseFile();
            }
            catch (Exception e) {}
        }
        return macros;
    }

    public void parseFile () throws IOException, FileNotFoundException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            this.macros =  VelocityMacroParser.parse(StringUtil.getStringFromStream(fis));
        }
        finally {
            if (null != fis) fis.close();
        }
    }
}
