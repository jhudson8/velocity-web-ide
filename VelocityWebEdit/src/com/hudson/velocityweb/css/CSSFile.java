package com.hudson.velocityweb.css;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hudson.velocityweb.util.StringUtil;

/**
 * @author Joe Hudson
 */
public class CSSFile {
    public long lastModTime;
    public File file;
    private CSSStyle[] styles;
    
    public CSSFile (File file) {
        this.lastModTime = file.lastModified();
        this.file = file;
    }
    
    public CSSFile (CSSStyle[] styles) {
        this.styles = styles;
    }
        
    public CSSStyle[] getStyles() {
        if (null != file && file.lastModified() > this.lastModTime || null == styles) {
            try {
                parseFile();
            }
            catch (Exception e) {}
        }
        return styles;
    }

    public void parseFile () throws IOException, FileNotFoundException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            this.styles = CSSParser.parse(StringUtil.getStringFromStream(fis));
        }
        finally {
            if (null != fis) fis.close();
        }
    }
}
