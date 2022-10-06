/*
 * @(#)ChiselResources.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import java.util.*;
import java.io.File;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

/**
 *  Localizable resources for Chisel.
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.1, 29 Jan 1998
 *
 *  @since           1.0
 */
public class ChiselResources extends ListResourceBundle {

    public final static int LOADFROMFILE = 0;
    public final static int LOADFROMCLASSPATH = 1;

    public static int loadMode = LOADFROMCLASSPATH;
    public static void setLoadFromJar(boolean loadFromJar) {
        loadMode = loadFromJar ? LOADFROMCLASSPATH : LOADFROMFILE;
    }


    //
    // private stuff
    //

	private static ResourceBundle resources;
    static {
		System.out.println("Loading resources...");
		try {
            resources = ResourceBundle.getBundle("com.trapezium.chisel.ChiselResources", Locale.getDefault());
        } catch (MissingResourceException mre) {
            System.err.println("Chisel resource class not found");
   			System.exit(1);
        }
    }

	public static ResourceBundle getDefaultResourceBundle() {
		return resources;
	}

    public static String getResourceString(String nm) {
        String str;
        try {
            str = resources.getString(nm);
        } catch (MissingResourceException mre) {
            str = null;
        }
        return str;
    }

    /** Get a URL for a resource, look in JAR file if possible, or disk if not */
    public static URL getResource(String key) {
        String name = getResourceString(key);
        return( getResourceByName( name ));
    }
    
    public static URL getResourceByName( String name ) {
        if (name != null) {
            URL url = resourceURL(name, loadMode);
            // couldn't find it?  look in the other possible place
            if (url == null) {
                url = resourceURL(name, loadMode == LOADFROMCLASSPATH ? LOADFROMFILE : LOADFROMCLASSPATH);
            }
            return url;
        }
        return null;
    }
    
    public static URL getResourceByName( String name, int mode ) {
        if (name != null) {
            return resourceURL(name, mode);
        } else {
            return null;
        }
    }
        

    private static URL base = null;
    private static String resourceName(String file) {
        if (file.startsWith("~")) {
            file = System.getProperty("user.home") + file.substring(1);
        }
        return "file:" + (new File(file)).getAbsolutePath().replace(File.separatorChar, '/');
    }

    private static String resourceURLString(String file) {
        URL url = resourceURL(file);
        if (url == null) {
            return null;
        }
        return url.toExternalForm();
    }

    /** Get a URL of a file using the current load mode -- LOADFROMCLASSPATH or LOADFROMDISK */
    public static URL resourceURL(String file) {
        return resourceURL(file, loadMode);
    }
    
    public static URL resourceURL(String file, int mode) {
        if (mode == LOADFROMCLASSPATH) {
            //System.out.println( "LOADFROMCLASSPATH trying resource '/" + file + "'" );
            return file.getClass().getResource("/" + file);
        } else {
            String resource = resourceName(file);
            //System.out.println( "LOADFROMFILE converted '" + file + "' to '" + resource + "'" );
            URL url;
            try {
                url = new URL(base, resource);
            } catch (Exception e) {
                url = null;
            }
            return url;
        }
    }

    /**
     * Suffix applied to the key used in resource file
     * lookups for an image.
     */
    private static final String imageSuffix = "Image";

    /**
     * Suffix applied to the key used in resource file
     * lookups for a label.
     */
    private static final String labelSuffix = "Label";

    /**
     * Suffix applied to the key used in resource file
     * lookups for an action.
     */
    private static final String actionSuffix = "Action";

    /**
     * Suffix applied to the key used in resource file
     * lookups for tooltip text.
     */
    private static final String tipSuffix = "Tooltip";
    /**
     * Suffix applied to the key used in resource file
     * lookups for initial item state.
     */
    private static final String stateSuffix = "State";


    public static String getImageName(String key) {
        return getResourceString(key + imageSuffix);
    }

    public static String getLabel(String key) {
        return getResourceString(key + labelSuffix);
    }

    public static String getActionCommand(String key) {
        return getResourceString(key + actionSuffix);
    }

    public static String getTip(String key) {
        return getResourceString(key + tipSuffix);
    }

    public static String getState(String key) {
        return getResourceString(key + stateSuffix);
    }

    static Image chiselImage = null;
    public static Image getImage(String key) {
        if ( key.compareTo( "chisel" ) == 0 ) {
            if ( chiselImage != null ) {
                return( chiselImage );
            }
        }
        URL url = getResource(key + imageSuffix);
        if (url == null) {
            System.out.println("No image resource for " + key);
            return null;
        } else {
            //System.out.println("Loading image resource for " + key + " URL: " + url.toExternalForm());
            Image image = null;
            try {
                image = Toolkit.getDefaultToolkit().createImage((java.awt.image.ImageProducer) url.getContent());
            } catch (Exception e) {
                System.out.println("Error loading image " + url.toExternalForm());
                image = null;
            }
            if ( key.compareTo( "chisel" ) == 0 ) {
                chiselImage = image;
            }
            return image;
        }
    }

    public Object[][] getContents() {
        return contents;
    }

    static final Object[][] contents = {
        // icon
        { "chiselImage", "images/chisel.gif" },
        
        //{ "menubar", "file edit action" },
        { "menubar", "file edit process view help" },

        { "file", "open fetch - save saveas gzipsave gzipsaveas - x3dsaveas - genhtml - exit" },
        //{ "file", "open fetch - save saveas gzipsave gzipsaveas x3dsave x3dsaveas - genhtml - exit" },
        //{ "file", "new open fetch - save saveas gzipsave gzipsaveas - exit" },
        { "fileLabel", "File" },
        { "openLabel", "Open..." },
        { "openImage", "images/open.gif" },
        { "openAction", "open" },
        { "openTooltip", "Open a file" },
        { "newLabel", "New" },
        { "newImage", "images/new.gif" },
        { "newAction", "new" },
        { "newTooltip", "Create a new file" },
        { "saveLabel", "Save" },
        { "saveImage", "images/save.gif" },
        { "saveAction", "save" },
        { "saveTooltip", "Save current file" },
        { "saveasLabel", "Save As..." },           // lame
        { "saveasImage", "images/saveas.gif" },
        { "saveasTooltip", "Save under new name"},
        { "saveAsLabel", "Save As" },
        { "saveAsImage", "images/saveas.gif" },
        { "saveAsAction", "saveas" },
        { "gzipsaveLabel", "GZIP Save" },       // lame
        { "gzipsaveImage", "images/gzipsave.gif" },
        { "gzipsaveTooltip", "Save gzipped"},
        { "gzipSaveLabel", "GZIP Save" },
        { "gzipSaveImage", "images/gzipsave.gif" },
        { "gzipSaveAction", "gzipsave" },
        { "gzipsaveasLabel", "GZIP Save As..." },  // really lame
        { "gzipsaveasImage", "images/gzipsaveas.gif" },
        { "gzipsaveasTooltip", "Save gzipped under new name"},
        { "gzipSaveAsLabel", "GZIP Save As" },
        { "gzipSaveAsImage", "images/gzipsaveas.gif" },
        { "gzipSaveAsAction", "gzipsaveas" },
        //{ "x3dsaveLabel", "X3D Save" },       // lame
        //{ "x3dsaveImage", "images/x3dsave.gif" },
        //{ "x3dsaveTooltip", "Save current file in X3D format"},
        //{ "x3dSaveLabel", "X3D Save" },
        //{ "x3dSaveImage", "images/x3dsave.gif" },
        //{ "x3dSaveAction", "x3dsave" },
        //{ "x3dsaveasLabel", "X3D Save As..." },  // really lame
        { "x3dsaveasLabel", "Export X3D..." },  // really lame
        { "x3dsaveasImage", "images/x3dsaveas.gif" },
        { "x3dsaveasTooltip", "Export in X3D format under new name"},
        //{ "x3dSaveAsLabel", "X3D Save As" },
        { "x3dSaveAsLabel", "Export X3D" },
        { "x3dSaveAsImage", "images/x3dsaveas.gif" },
        { "x3dSaveAsAction", "x3dsaveas" },
        { "fetchLabel", "Fetch..." },
        { "fetchImage", "images/fetch.gif" },
        { "fetchAction", "fetch" },
        { "fetchTooltip", "Retrieve a URL" },
        { "genhtmlLabel", "Generate HTML report" },
        { "genhtmlAction", "genhtml" },
        { "exitLabel", "Exit" },
        { "exitImage", "images/exit.gif" },
        { "exitAction", "exit" },

        { "action", "chisel" },
        { "actionLabel", "Action" },
        { "chiselLabel", "Run chisels" },
        { "chiselImage", "images/chisel.gif" },

        { "process", "stop validate format clean condense reduce reorganize" },
        { "processLabel", "Process" },

        { "edit", "undo redo - copy" },
        { "editLabel", "Edit" },
        { "undoLabel", "Undo" },
        { "undoAction", "undo-last-command" },
        { "undoImage", "images/undo.gif" },
        { "undoTooltip", "Undo" },
        { "redoLabel", "Redo" },
        { "redoAction", "redo-last-command" },
        { "redoImage", "images/redo.gif" },
        { "redoTooltip", "Redo" },
        { "copyLabel", "Copy" },
        { "copyAction", "copy-to-clipboard" },
        { "copyImage", "images/copy.gif" },
        { "copyTooltip", "Copy selection to clipboard" },

        // not used right now
        { "cutLabel", "Cut" },
        { "cutAction", "cut-to-clipboard" },
        { "cutImage", "images/cut.gif" },
        { "cutTooltip", "Move selection to clipboard" },
        { "pasteLabel", "Paste" },
        { "pasteAction", "paste-from-clipboard" },
        { "pasteImage", "images/paste.gif" },
        { "pasteTooltip", "Paste clipboard to selection" },

        { "stopLabel", "Stop" },
        { "stopAction", "stop" },
        { "stopImage", "images/stop.gif" },
        { "stopTooltip", "Stop the current process" },
        { "validateLabel", "Validate" },
        { "validateAction", "validate" },
        { "validateImage", "images/validate.gif" },
        { "validateTooltip", "Look for errors in current world" },
        { "formatLabel", "Format" },
        { "formatAction", "format" },
        { "formatImage", "images/format.gif" },  // note, no such image
        { "formatTooltip", "Format text in current world" },
        { "cleanLabel", "Clean" },
        { "cleanAction", "clean" },
        { "cleanImage", "images/clean.gif" },
        { "cleanTooltip", "Remove unnecessary and unusable code" },
        { "condenseLabel", "Condense" },
        { "condenseAction", "condense" },
        { "condenseImage", "images/condense.gif" },
        { "condenseTooltip", "Reduce the size of the current file" },
        { "reduceLabel", "Reduce" },
        { "reduceAction", "reduce" },
        { "reduceImage", "images/reduce.gif" },
        { "reduceTooltip", "Reduce the number of polygons in the current world" },
        { "mutateLabel", "Mutate" },
        { "mutateAction", "mutate" },
        { "mutateImage", "images/mutate.gif" },
        { "mutateTooltip", "Modify the geometry in the current world" },
        { "reorganizeLabel", "Reorganize" },
        { "reorganizeAction", "reorganize" },
        { "reorganizeImage", "images/reorganize.gif" },
        { "reorganizeTooltip", "Reorganize the nodes and files that make up the current world" },

        { "view", "preverror nexterror - options" },
        { "viewLabel", "View" },
        { "showtoolbarLabel", "Toolbar" },
        { "showtoolbarAction", "show-toolbar" },
        { "showtoolbarState", "checked" },
        { "showstatusLabel", "Status line" },
        { "showstatusAction", "show-statusline" },
        { "showstatusState", "checked" },
        { "preverrorLabel", "Previous Error" },
        { "preverrorAction", "prev-error" },
        { "preverrorImage", "images/preverror.gif" },
        { "preverrorTooltip", "Go to previous error" },
        { "nexterrorLabel", "Next Error" },
        { "nexterrorAction", "next-error" },
        { "nexterrorImage", "images/nexterror.gif" },
        { "nexterrorTooltip", "Go to next error" },
        { "optionsLabel", "Options..." }, // MLo added ... since options dialog is modal
        { "optionsAction", "options" },
        { "optionsImage", "images/options.gif" },
        { "optionsTooltip", "Set font size and other options" },

        { "help", "about" }, // MLo removed version check as it is no longer operational
        { "helpLabel", "Help" },
        { "aboutLabel", "About..." },
        { "aboutAction", "about" },
        
        { "toolbar", "open fetch - save saveas gzipsave gzipsaveas - copy - redo undo - stop - preverror nexterror" },
        //{ "toolcell", "open fetch" },

        // button labels for dialogs
        { "okLabel", "OK" },
        { "cancelLabel", "Cancel" },
        { "purchaseLabel", "Purchase..." },
        { "unlockLabel", "Unlock..." },

        // specific dialog labels
        { "fetchDialogTitle", "Retrieve URL" },
        { "urlLabel", "URL:" },
        { "recurseLabel", "Retrieve referenced files" },
        { "choicePlain", "Plain" },
        { "choiceBold", "Bold" },
        { "choiceItalic", "Italic" },
        { "choiceBoldItalic", "Bold Italic" },
        { "nameLabel", "Name:" },
        { "emailLabel", "Email:" },
        { "keyLabel", "Key:" },
        { "optionsDialogTitle", "Options" },
        { "font.nameLabel", "Font" },
        { "font.styleLabel", "Style" },
        { "font.sizeLabel", "Size" },
        { "loadWorkspaceLabel", "Reload workspace on launch" },
        { "saveInUnixFormat", "Save in Unix format" },
        { "automaticClean", "Automatically clean after processing" },
        { "includeWarnings", "Include warnings in next/prev error search" },
        { "includeNonconformances", "Include nonconformance in next/prev error search" },
        { "allowNurbsExtension", "Allow Blaxxun nurbs specification" },
    };
}
