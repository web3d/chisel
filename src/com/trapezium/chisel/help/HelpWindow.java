/*
 * @(#)HelpWindow.java
 *
 * Copyright (c) 1998 by Trapezium Development Company.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development
 * Company and may be used only in accordance with the terms of the license
 * granted by Trapezium.
 *
 */
package com.trapezium.chisel.help;

import java.awt.*;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.io.*;

import com.trapezium.chisel.ChiselResources;
import com.trapezium.chisel.gui.FontPool;
import com.trapezium.chisel.gui.PopupViewer;

/** Help system main window */
public class HelpWindow extends PopupViewer {

    public HelpWindow(Frame frame) {
    	super(frame);
    }

    protected Component createContent() {
        ResourceBundle res = ChiselResources.getDefaultResourceBundle();
        Container helpPanel;
    	if (JFCAvailable()) {
    	    helpPanel = new HelpPanel(res);
    	} else {
    	    helpPanel = new Panel();
   	        helpPanel.setLayout(new BorderLayout());
    	    Panel body = new Panel();
    	    body.setLayout(new BorderLayout());
    	    try {
        	    Label header = new Label(res.getString("JFCNotFoundMessageHeader"));
        	    header.setFont(FontPool.getHeaderFont(0));
        	    helpPanel.add("Center", header);
        	    Label body1 = new Label(res.getString("JFCNotFoundMessage1"));
        	    body.add("North", body1);
        	    Label body2 = new Label(res.getString("JFCNotFoundMessage2"));
        	    body.add("North", body2);
        	    Label body3 = new Label(res.getString("JFCNotFoundMessage3"));
        	    body.add("North", body3);
            } catch (MissingResourceException e) {
            }
       	    helpPanel.add("South", body);
    	}
        return helpPanel;
    }

    static final String jfcclassname = "com.sun.java.swing.JPanel";
    static boolean JFCAvailable() {
        try {
            Class jfcclass = Class.forName(jfcclassname);
            System.out.println("JFC detected");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return false; // true;
    }

}
