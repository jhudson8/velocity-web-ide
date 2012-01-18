package com.hudson.velocityweb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.velocity.app.Velocity;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.hudson.velocityweb.editors.velocity.ColorManager;


/**
 * The main plugin class to be used in the desktop.
 */
public class Plugin extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID = "com.hudson.velocityweb";
	
	//The shared instance.
	private static Plugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	public static boolean devMode = false;
	
	/**
	 * The constructor.
	 */
	public Plugin() {
		super();
		plugin = this;

		try {
			resourceBundle = ResourceBundle.getBundle("com.hudson.velocityweb.resources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		try {
		    Velocity.init();
		}
		catch (Exception e) {
		}
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		try {
			initializePreferences();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initializePreferences() {
		IPreferenceStore store = getPreferenceStore();
		ColorManager velocityColorManager = new ColorManager();
		store.setDefault(ColorManager.COLOR_FOREACH_DIRECTIVE,
				getDefaultStoreValue(velocityColorManager.getDefaultColor(ColorManager.COLOR_FOREACH_DIRECTIVE)));
		store.setDefault(ColorManager.COLOR_IF_DIRECTIVE,
				getDefaultStoreValue(velocityColorManager.getDefaultColor(ColorManager.COLOR_IF_DIRECTIVE)));
		store.setDefault(ColorManager.COLOR_MACRO_DIRECTIVE,
				getDefaultStoreValue(velocityColorManager.getDefaultColor(ColorManager.COLOR_MACRO_DIRECTIVE)));
		store.setDefault(ColorManager.COLOR_SET_DIRECTIVE,
				getDefaultStoreValue(velocityColorManager.getDefaultColor(ColorManager.COLOR_SET_DIRECTIVE)));
		store.setDefault(ColorManager.COLOR_VARIABLE,
				getDefaultStoreValue(velocityColorManager.getDefaultColor(ColorManager.COLOR_VARIABLE)));
		store.setDefault(ColorManager.COLOR_COMMENT,
				getDefaultStoreValue(velocityColorManager.getDefaultColor(ColorManager.COLOR_COMMENT)));

		ColorManager xmlColorManager = new ColorManager();
		store.setDefault(ColorManager.COLOR_DEFAULT,
				getDefaultStoreValue(xmlColorManager.getDefaultColor(ColorManager.COLOR_DEFAULT)));
		store.setDefault(ColorManager.COLOR_PROC_INSTR,
				getDefaultStoreValue(xmlColorManager.getDefaultColor(ColorManager.COLOR_PROC_INSTR)));
		store.setDefault(ColorManager.COLOR_STRING,
				getDefaultStoreValue(xmlColorManager.getDefaultColor(ColorManager.COLOR_STRING)));
		store.setDefault(ColorManager.COLOR_TAG,
				getDefaultStoreValue(xmlColorManager.getDefaultColor(ColorManager.COLOR_TAG)));
		store.setDefault(ColorManager.COLOR_XML_COMMENT,
				getDefaultStoreValue(xmlColorManager.getDefaultColor(ColorManager.COLOR_XML_COMMENT)));
	}

	private String getDefaultStoreValue (Color color) {
		if (null == color) return "0,0,0";
		else return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static Plugin getDefault() {
		return plugin;
	}

	public File getActualStateLocation () {
		return getStateLocation().makeAbsolute().toFile();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = Plugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	protected void initializeImageRegistry(ImageRegistry reg) {
	}

	public ImageDescriptor getImageDescriptor(String key) {
		URL url = null;
		try {
			url = new URL(getDescriptor().getInstallURL(),
					"icons/" + key);
		} catch (MalformedURLException e) {
		}
		return ImageDescriptor.createFromURL(url);
	}
	
	private Map images = new HashMap();
	public Image getImage(String name) {
	    Image image = (Image) images.get(name);
	    if (null == image) {
	        image = getImageDescriptor(name + ".gif").createImage();
	        images.put(name, image);
	    }
	    return image;
	}
	
	public static String getProperty (IAdaptable project, String propName) {
		if (project instanceof IProject)
			return getProperty((IProject) project, propName);
		else
			return null;
	}

	public static void log (String message, Throwable t) {
        StringWriter sw = new StringWriter();
        sw.write("\n------------------ " + new Date().toString() + "\n");
        if (null != message) sw.write(message + "\n");
        t.printStackTrace(new PrintWriter(sw));
        log(sw.toString(), true);
	}
	public static void log (Throwable t) {
        log(null, t);
	}
	public static void log (String message) {
        log(message, false);
	}

	public static void log (String message, boolean error) {
	    if (devMode) {
	        if (error) System.err.println(message);
	        else System.out.println(message);
	    }
	    else {
	        FileOutputStream fos = null;
	        try {
	            String logFileName = Platform.resolve(
	                    plugin.getDescriptor().getInstallURL()).getFile()
	                    + "error.log";
	            fos = new FileOutputStream(logFileName, true);
	            fos.write(message.getBytes());
	            fos.write("\n".getBytes());
	        } catch (Exception e) {
	        } finally {
	            if (null != fos) {
	                try {
	                    fos.close();
	                } catch (Exception e) {
	                }
	            }
	        }
	    }
	}

	public static void trace (Throwable t) {
	    if (devMode) {
	        log(t);
	    }
	}

	public static void trace (String s) {
	    if (devMode) {
	        log(s);
	    }
	}
}