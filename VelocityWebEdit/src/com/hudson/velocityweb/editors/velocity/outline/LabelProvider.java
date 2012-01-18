package com.hudson.velocityweb.editors.velocity.outline;

import org.eclipse.swt.graphics.Image;

import com.hudson.velocityweb.Plugin;
import com.hudson.velocityweb.editors.velocity.completion.IDirective;
import com.hudson.velocityweb.editors.velocity.completion.xml.Node;

/**
 * @author Joe Hudson
 */
public class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
    
    public Image getImage(Object element) {
        if (element instanceof IDirective) {
            String imageName = ((IDirective) element).getImage();
            if (null != imageName) return Plugin.getDefault().getImage(imageName);
        }
        else if (element instanceof Node) {
            return ((Node) element).getImage();
        }
        return null;
    }

    public String getText(Object element) {
        if (element instanceof IDirective) {
            return ((IDirective) element).getLabel();
        }
        else if (element instanceof Node) {
            return ((Node) element).getLabel();
        }
        else return super.getText(element);
    }
}
