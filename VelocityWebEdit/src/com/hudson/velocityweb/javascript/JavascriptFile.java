package com.hudson.velocityweb.javascript;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hudson.velocityweb.util.StringUtil;

/**
 * @author Joe Hudson
 */
public class JavascriptFile {
    public long lastModTime;
    public File file;
    private JavascriptFunction[] functions;
    
    public JavascriptFile (File file) {
        this.lastModTime = file.lastModified();
        this.file = file;
    }
    
    public JavascriptFile (JavascriptFunction[] functions) {
        this.functions = functions;
    }
        
    public JavascriptFunction[] getFunctions() {
        if (null != file && file.lastModified() > this.lastModTime || null == functions) {
            try {
                parseFile();
            }
            catch (Exception e) {}
        }
        return functions;
    }

    public void parseFile () throws IOException, FileNotFoundException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            this.functions = JavascriptParser.parse(StringUtil.getStringFromStream(fis));
        }
        finally {
            if (null != fis) fis.close();
        }
    }
}
