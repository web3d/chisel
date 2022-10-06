/*
 * @(#)HelpPanel.java
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
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.*;

import com.trapezium.chisel.gui.FontPool;

/**
 *  The help system.  Requires Swing.
 */
public class HelpPanel extends JPanel {

    private ResourceBundle resources;
    private JTextComponent textviewer;
    private Hashtable commands;
    private Hashtable menuItems;
    private JMenuBar menubar;
    private JToolBar toolbar;
    private JComponent status;

    public HelpPanel(ResourceBundle res) {
    	super(true);

        resources = res;

    	// Force SwingSet to come up in the Cross Platform L&F
    	try {
    	    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    	    // If you want the System L&F instead, comment out the above line and
    	    // uncomment the following:
    	    // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	} catch (Exception exc) {
    	    System.err.println("Error loading L&F: " + exc);
    	}

	    setBorder(BorderFactory.createEtchedBorder());
    	setLayout(new BorderLayout());

    	// create the embedded JTextComponent
	    textviewer = createViewer();
    	textviewer.setFont(FontPool.getFont(0));

    	// install the command table
    	commands = new Hashtable();
    	Action[] actions = getActions();
    	for (int i = 0; i < actions.length; i++) {
    	    Action a = actions[i];
    	    //commands.put(a.getText(Action.NAME), a);
    	    commands.put(a.getValue(Action.NAME), a);
    	}

    	JScrollPane scroller = new JScrollPane();
    	JViewport port = scroller.getViewport();
    	port.add(textviewer);
    	try {
    	    String vpFlag = resources.getString("ViewportBackingStore");
    	    Boolean bs = new Boolean(vpFlag);
    	    port.setBackingStoreEnabled(bs.booleanValue());
    	} catch (MissingResourceException mre) {
    	    // just use the viewport default
    	}

    	menuItems = new Hashtable();
    	menubar = createMenubar();
    	add("North", menubar);
    	JPanel panel = new JPanel();
    	panel.setLayout(new BorderLayout());
    	panel.add("North",createToolbar());
    	panel.add("Center", scroller);
    	add("Center", panel);
    }


    /**
     * Fetch the list of actions supported by this
     * editor.  It is implemented to return the list
     * of actions supported by the embedded JTextComponent
     * augmented with the actions defined locally.
     */
    public Action[] getActions() {
    	return TextAction.augmentList(textviewer.getActions(), defaultActions);
    }

    /**
     * Create an editor to represent the given document.
     */
    protected JTextComponent createViewer() {
    	return new JTextArea();
    }

    /**
     * Fetch the editor contained in this panel
     */
    protected JTextComponent getViewer() {
	    return textviewer;
    }


    /**
     * This is the hook through which all menu items are
     * created.  It registers the result with the menuitem
     * hashtable so that it can be fetched with getMenuItem().
     * @see #getMenuItem
     */
    protected JMenuItem createMenuItem(String cmd) {
    	JMenuItem mi = new JMenuItem(getResourceString(cmd + labelSuffix));
        URL url = getResource(cmd + imageSuffix);
    	if (url != null) {
//    	    mi.setHorizontalTextPosition(JButton.RIGHT);
//    	    mi.setIcon(new ImageIcon(url));
    	}
    	String astr = getResourceString(cmd + actionSuffix);
    	if (astr == null) {
    	    astr = cmd;
    	}
    	mi.setActionCommand(astr);
    	Action a = getAction(astr);
    	if (a != null) {
    	    mi.addActionListener(a);
    	    a.addPropertyChangeListener(createActionChangeListener(mi));
    	    mi.setEnabled(a.isEnabled());
    	} else {
    	    mi.setEnabled(false);
    	}
    	menuItems.put(cmd, mi);
    	return mi;
    }

    /**
     * Fetch the menu item that was created for the given
     * command.
     * @param cmd  Name of the action.
     * @returns item created for the given command or null
     *  if one wasn't created.
     */
    protected JMenuItem getMenuItem(String cmd) {
    	return (JMenuItem) menuItems.get(cmd);
    }

    protected Action getAction(String cmd) {
	    return (Action) commands.get(cmd);
    }

    protected String getResourceString(String nm) {
    	String str;
    	try {
    	    str = resources.getString(nm);
    	} catch (MissingResourceException mre) {
    	    str = null;
    	}
    	return str;
    }

    protected URL getResource(String key) {
    	String name = getResourceString(key);
    	if (name != null) {
    	    URL url = this.getClass().getResource(name);
    	    return url;
    	}
    	return null;
    }

    protected Container getToolbar() {
    	return toolbar;
    }

    protected JMenuBar getMenubar() {
	    return menubar;
    }


    /**
     * Create the toolbar.  By default this reads the
     * resource file for the definition of the toolbar.
     */
    private Component createToolbar() {
    	toolbar = new JToolBar();
    	String[] toolKeys = tokenize(getResourceString("helptoolbar"));
    	for (int i = 0; i < toolKeys.length; i++) {
    	    if (toolKeys[i].equals("-")) {
        		toolbar.add(Box.createHorizontalStrut(5));
    	    } else {
        		toolbar.add(createTool(toolKeys[i]));
    	    }
    	}
    	toolbar.add(Box.createHorizontalGlue());
    	return toolbar;
    }

    /**
     * Hook through which every toolbar item is created.
     */
    protected Component createTool(String key) {
    	return createToolbarButton(key);
    }

    /**
     * Create a button to go inside of the toolbar.  By default this
     * will load an image resource.  The image filename is relative to
     * the classpath (including the '.' directory if its a part of the
     * classpath), and may either be in a JAR file or a separate file.
     *
     * @param key The key in the resource file to serve as the basis
     *  of lookups.
     */

    class ToolButton extends JButton {
        public ToolButton(ImageIcon icon) {
            super(icon);
        }
        public ToolButton(String str) {
            super(str);
        }
        public float getAlignmentY() {
            return 0.5f;
        }
    }

    protected JButton createToolbarButton(String key) {

        ToolButton b;
    	try {
        	URL url = getResource(key + imageSuffix);
    	    ImageIcon ii = new ImageIcon(url);
    	    b = new ToolButton(ii);
    	} catch (Exception e) {
  	        b = new ToolButton(getResourceString(key + labelSuffix));
    	}

        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1,1,1,1));

    	String astr = getResourceString(key + actionSuffix);
    	if (astr == null) {
    	    astr = key;
    	}
    	Action a = getAction(astr);
    	if (a != null) {
    	    b.setActionCommand(astr);
    	    b.addActionListener(a);
    	} else {
    	    b.setEnabled(false);
    	}

    	String tip = getResourceString(key + tipSuffix);
    	if (tip != null) {
    	    b.setToolTipText(tip);
    	}

        return b;
    }

    /**
     * Take the given string and chop it up into a series
     * of strings on whitespace boundries.  This is useful
     * for trying to get an array of strings out of the
     * resource file.
     */
    protected String[] tokenize(String input) {
    	Vector v = new Vector();
    	StringTokenizer t = new StringTokenizer(input);
    	String cmd[];

    	while (t.hasMoreTokens()) {
    	    v.addElement(t.nextToken());
    	}
    	cmd = new String[v.size()];
    	for (int i = 0; i < cmd.length; i++) {
    	    cmd[i] = (String) v.elementAt(i);
    	}

    	return cmd;
    }

    /**
     * Create the menubar for the app.  By default this pulls the
     * definition of the menu from the associated resource file.
     */
    protected JMenuBar createMenubar() {
    	JMenuItem mi;
    	JMenuBar mb = new JMenuBar();

    	String[] menuKeys = tokenize(getResourceString("helpmenubar"));
    	for (int i = 0; i < menuKeys.length; i++) {
    	    JMenu m = createMenu(menuKeys[i]);
    	    if (m != null) {
    		    mb.add(m);
    	    }
    	}
    	return mb;
    }

    /**
     * Create a menu for the app.  By default this pulls the
     * definition of the menu from the associated resource file.
     */
    protected JMenu createMenu(String key) {
    	String[] itemKeys = tokenize(getResourceString(key));
    	JMenu menu = new JMenu(getResourceString(key + "Label"));
    	for (int i = 0; i < itemKeys.length; i++) {
    	    if (itemKeys[i].equals("-")) {
        		menu.addSeparator();
    	    } else {
    	    	JMenuItem mi = createMenuItem(itemKeys[i]);
        		menu.add(mi);
    	    }
    	}
    	return menu;
    }

    // Yarked from JMenu, ideally this would be public.
    protected PropertyChangeListener createActionChangeListener(JMenuItem b) {
    	return new ActionChangedListener(b);
    }

    // Yarked from JMenu, ideally this would be public.
    private class ActionChangedListener implements PropertyChangeListener {
        JMenuItem menuItem;

        ActionChangedListener(JMenuItem mi) {
            super();
            this.menuItem = mi;
        }
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (e.getPropertyName().equals(Action.NAME)) {
                String text = (String) e.getNewValue();
                menuItem.setText(text);
            } else if (propertyName.equals("enabled")) {
                Boolean enabledState = (Boolean) e.getNewValue();
                menuItem.setEnabled(enabledState.booleanValue());
            }
        }
    }


    /**
     * Suffix applied to the key used in resource file
     * lookups for an image.
     */
    public static final String imageSuffix = "Image";

    /**
     * Suffix applied to the key used in resource file
     * lookups for a label.
     */
    public static final String labelSuffix = "Label";

    /**
     * Suffix applied to the key used in resource file
     * lookups for an action.
     */
    public static final String actionSuffix = "Action";

    /**
     * Suffix applied to the key used in resource file
     * lookups for tooltip text.
     */
    public static final String tipSuffix = "Tooltip";

    public static final String upAction = "up";
    public static final String backAction  = "back";
    public static final String forwardAction = "forward";
    public static final String indexAction = "index";

    // used by NavigateAction
    static final int UP = 0;
    static final int BACK = -1;
    static final int FORWARD = 1;
    static final int INDEX = 100;

    /** all-purpose navigation action */
    class NavigateAction extends javax.swing.AbstractAction {

        int direction;

        public NavigateAction(String name) {
            super(name);

            if (upAction.equalsIgnoreCase(name)) {
                direction = UP;
            } else if (backAction.equalsIgnoreCase(name)) {
                direction = BACK;
            } else if (forwardAction.equalsIgnoreCase(name)) {
                direction = FORWARD;
            } else {
                direction = INDEX;
            }
        }
        public void actionPerformed(ActionEvent e) {
            switch (direction) {
                case UP:
                    break;
                case BACK:
                    break;
                case FORWARD:
                    break;
                case INDEX:
                    break;
            }
    	}
    }

    /**
     * Actions
     */
    private Action[] defaultActions = {
    	new NavigateAction(upAction),
    	new NavigateAction(backAction),
    	new NavigateAction(forwardAction),
    	new NavigateAction(indexAction)
    };
}
