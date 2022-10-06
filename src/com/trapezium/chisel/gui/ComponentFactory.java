/*
 * @(#)ComponentFactory.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.peer.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import com.trapezium.util.ProgressIndicator;
import com.trapezium.chisel.GuiAdapter;
import com.trapezium.chisel.ProcessedFileViewer;
import com.trapezium.chisel.Chisel;
import com.trapezium.chisel.ChiselResources;
import com.trapezium.chisel.ChiselAction;
import com.trapezium.chisel.ActionConnector;

//import com.sun.java.swing.text.*;
//import com.sun.java.swing.*;


/**
 * The ComponentFactory builds functional user interface components
 * for Chisel.  This isolates Chisel from any knowledge of JFC.
 */
public class ComponentFactory implements DisplayConstants {


    /** true if JFC was looked for and found */
    public static boolean useJFC = false;

    //
    // public methods
    //

    /**
     * Create the frame.  If tryJFC is true, look for JFC and use if found.
     */
    public static Frame createFrame(boolean tryJFC, String title) {

        /*** JFC interface ********
        Class frameClass;

        if (tryJFC) {
            try {
                frameClass = Class.forName("com.sun.java.swing.JFrame");
                useJFC = true;
                System.out.println("Swing detected, will use.");

            } catch (ClassNotFoundException e) {
                useJFC = false;
                System.out.println("Swing not detected, will use AWT.");
            }
        } else {
            System.out.println("Swing option not set; will use AWT.");
            useJFC = false;
        }
        if (useJFC) {
            return new JFrame(title);
        } else { ******/
            Frame frame = new ChiselAWTFrame(title);
            frame.setLayout(new BorderLayout());
            return frame;
        //}
    }


    /**
     * Create the scene viewer panel.
     */
    public static ProcessedFileViewer createViewer() {

        ProcessedFileViewer viewer;
        //if (useJFC) {
        //    System.out.println("ComponentFactory creating ChiselJViewer");
        //    viewer = new ChiselJViewer();
        //} else {
        //    System.out.println("ComponentFactory creating ChiselAWTViewer");
            viewer = new ChiselAWTViewer();
        //}
        return viewer;
    }


    /**
     * Create the main panel (parent of workspace)
     */

    public static Container createMainPanel(Frame frame) {
        String[] menuKeys = tokenize(ChiselResources.getResourceString("menubar"));
        /********
        if (useJFC) {
            JFrame jframe = (JFrame) frame;
            JMenuBar jbar = new JMenuBar();
            try {
                for (int i = 0; i < menuKeys.length; i++) {
                    JMenu m = createJMenu( menuKeys[i], app);
                    if (m != null) {
                        System.out.println("..adding " + m + "to menu");
                        jbar.add(m);
                    }
                }

            } catch (Exception e) {
                System.out.println("Exception in createMainPanel: " + e);
            }
        	jframe.setJMenuBar(jbar);
        	jframe.setBackground(Color.lightGray);
            return jframe.getContentPane();
        } else {  *****/
            MenuBar mainmenu = new MenuBar();
            for (int i = 0; i < menuKeys.length; i++) {
                Menu m = createMenu( menuKeys[i]);
                if (m != null) {
                    mainmenu.add(m);
                }
            }
            frame.setMenuBar(mainmenu);

            Panel panel = new GlassPanel();
            Container main = createBufferedPane();
            panel.add(main);
            frame.add(panel);
            return main;
        //}
    }


    public static Container createBufferedPane() {
        return createPane(true, true, false);
    }

    public static Container createSimplePane() {
        return createPane(false, true, false);
    }

    public static Container createScrollablePane() {
        return createPane(false, true, true);
    }

    /**
     * Create a pane.
     */
    private static Container createPane(boolean buffered, boolean opaque, boolean scrollable) {
        Container pane = null;

        //if (useJFC) try {
        //    pane = new JBufferedPane();
        //} catch (Exception e) {
        //    pane = null;
        //}

        if (pane == null) {
            if (scrollable) {
                pane = new ScrollablePane();
            } else if (buffered) {
                pane = new BufferedPane(ChiselAWTPane.NO_BORDER);
            } else {
                pane = new ChiselAWTPane(ChiselAWTPane.NO_BORDER);
            }
            ((ChiselAWTPane)pane).setOpaque(opaque);
        }

        pane.setBackground(DEFAULT_BGCOLOR);
        pane.setForeground(DEFAULT_TEXTCOLOR);
        return pane;
    }

    public static void setPaneBorder(Container pane, int border) {
        if (pane instanceof ChiselAWTPane) {
            ((ChiselAWTPane)pane).setBorder(border);
        }
    }

    public static void setPaneOpaque(Container pane, boolean opaque) {
        if (pane instanceof ChiselAWTPane) {
            ((ChiselAWTPane)pane).setOpaque(opaque);
        }
    }

    /**
     * Create a divided pane.
     */
    public static Container createDividedPane(int align, Component before, Component after) {
        Container pane = null;

        //if (useJFC) try {
        //    System.err.println("JFC divided pane not hooked up!!!");
        //} catch (Exception e) {
        //    pane = null;
        //}

        if (pane == null) {
            pane = new ChiselAWTDividedPane(align, before, after);
        }

        pane.setSize(pane.getPreferredSize());

        return pane;
    }

    /** Preload button images for toolbar, since this seems related to jdk1.1.7a hang
     *  and possibly SGI & Solaris java hangs on startup
     */
    static Hashtable imageHash = new Hashtable();
    public static void preloadImages() {
        String[] toolKeys = tokenize(ChiselResources.getResourceString("toolbar"));
        for (int i = 0; i < toolKeys.length; i++) {
            if (!toolKeys[i].equals("-")) {
                //System.out.println("ToolBar creating button for " + toolKeys[i]);
                String key = toolKeys[i];
                Image image = ChiselResources.getImage(key);
                imageHash.put(key,image);
            }
        }
    }
    
    /**
     * Create a toolbar.
     */
    public static Container createToolBar(Chisel app) {

        Container bar = null;
        if ( bar != null ) {
            return( bar );
        }

        //if (useJFC) try {
        //    bar = new JToolBar();
        //} catch (Exception e) {
        //    bar = null;
        //}

        if (bar == null) {
            bar = new ChiselAWTToolBar();
        }

        bar.setBackground(DEFAULT_TOOLBARCOLOR);
        bar.setForeground(DEFAULT_TEXTCOLOR);
        String[] toolKeys = tokenize(ChiselResources.getResourceString("toolbar"));
        for (int i = 0; i < toolKeys.length; i++) {
            if (toolKeys[i].equals("-")) {
                //if ( bar instanceof JToolBar) {
                //    bar.add(Box.createHorizontalStrut(5));
                //} else {
                bar.add(new ToolbarSeparator());
            } else {
                //System.out.println("ToolBar creating button for " + toolKeys[i]);
                Component b = createButton(toolKeys[i]);
                if (b != null) {
                    bar.add(b);
                }
            }
        }
        //if (bar instanceof JToolBar) {
        //    bar.add(Box.createHorizontalGlue());
        //}
        bar.setSize(bar.getPreferredSize());

        return bar;
    }
    public static Container createStatusBar(Chisel app) {
        Container bar = null;
        //if (useJFC) try {
        //    bar = new Box(BoxLayout.X_AXIS);
        //} catch (Exception e) {
        //    bar = null;
        //}
        if (bar == null) {
            bar = new ChiselAWTStatusBar();
        }

        bar.setBackground(DEFAULT_STATUSBARCOLOR);
        bar.setForeground(DEFAULT_TEXTCOLOR);
        return bar;
    }

    /**
     * Take the given string and chop it up into a series
     * of strings on whitespace boundries.  This is useful
     * for trying to get an array of strings out of the
     * resource file.
     */
    private static String[] tokenize(String input) {
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
     * Create a JMenu for the app.  By default this pulls the
     * definition of the menu from the associated resource file.
     */
     /*************************
    private static JMenu createJMenu(String key, Chisel app) {
        String[] itemKeys = tokenize(getResourceString(key));
        JMenu menu = new JMenu(getResourceString(key + labelSuffix));
        for (int i = 0; i < itemKeys.length; i++) {
            if (itemKeys[i].equals("-")) {
                menu.addSeparator();
            } else try {
                JMenuItem mi = createJMenuItem(itemKeys[i], app);
                menu.add(mi);
            } catch (Exception e) {
                System.out.println("Exception in createJMenu: " + e);
                e.printStackTrace();
            }
        }
        return menu;
    }
    *******************/

    /** Create the JMenuItem for a command.  The owning application is passed in
        to get the Action for the command */
    /***************************
    private static JMenuItem createJMenuItem(String cmd, Chisel app) {
        JMenuItem mi = new JMenuItem(getResourceString(cmd + labelSuffix));
        URL url = getResource(cmd + imageSuffix);
        if (url != null) {
            mi.setHorizontalTextPosition(JButton.RIGHT);
            mi.setIcon(new ImageIcon(url));
        }
        String astr = getResourceString(cmd + actionSuffix);
        if (astr == null) {
            astr = cmd;
        }
        mi.setActionCommand(astr);

        ChiselAction a = app.getAction(astr);
        if (a != null) {
            mi.addActionListener(a.getAction());
        } else {
            mi.setEnabled(false);
        }
        return mi;
    }
    *****************/

    /** keep track of undo/redo menu items */
    private static MenuItem undoMenuItem = null;
    private static MenuItem redoMenuItem = null;
    public static MenuItem getUndoMenuItem() {
        return undoMenuItem;
    }
    public static MenuItem getRedoMenuItem() {
        return undoMenuItem;
    }

    /**
     * Create a Menu for the app.  By default this pulls the
     * definition of the menu from the associated resource file.
     * This method is public so the app can add individual menus dynamically.
     */
    public static Menu createMenu(String key) {
        String[] itemKeys = tokenize(ChiselResources.getResourceString(key));
        Menu menu = new Menu(ChiselResources.getLabel(key));
        for (int i = 0; i < itemKeys.length; i++) {
            if (itemKeys[i].equals("-")) {
                menu.addSeparator();
            } else {
                MenuItem mi = createMenuItem(itemKeys[i]);
                if ( itemKeys[i].compareTo( "undo" ) == 0 ) {
                    undoMenuItem = mi;
                } else if ( itemKeys[i].compareTo( "redo" ) == 0 ) {
                    redoMenuItem = mi;
                }
                menu.add(mi);
            }
        }
        return menu;
    }

    /** Create the menu item for a command. */
    private static MenuItem createMenuItem(String cmd) {
        String state = ChiselResources.getState(cmd);
        String label = ChiselResources.getLabel(cmd);
        MenuItem mi;
        if ("checked".equalsIgnoreCase(state)) {
            mi = new CheckboxMenuItem(label, true);
        } else if ("unchecked".equalsIgnoreCase(state)) {
            mi = new CheckboxMenuItem(label, false);
        } else {
            mi = new MenuItem(label);
        }
        String astr = ChiselResources.getActionCommand(cmd);
        if (astr == null) {
            astr = cmd;
        }
        mi.setActionCommand(astr);
        ActionListener a = ActionConnector.getAction(astr);
        if (a != null) {
            mi.addActionListener(a);
            if ( a instanceof ChiselAction ) {
                ChiselAction ca = (ChiselAction)a;
                mi.setEnabled( ca.getEnabled() );
            }
        } else {
            mi.setEnabled(false);
        }
        return mi;
    }

    /**
     * Create a button suitable for a toolbar.
     */
    public static Component createButton(String key) {

        Image image = (Image)imageHash.get(key);
        if ( image == null ) {
            image = ChiselResources.getImage(key);
        }
        if (image == null) {
            System.err.println("Image not available for " + key);
            return null;
        }
        String astr = ChiselResources.getActionCommand(key);
        if (astr == null) {
            astr = key;
        }
        ActionListener a = ActionConnector.getAction(astr);

        Component button = null;
        /*
        if (useJFC) try {

            JButton b = new JButton(new ImageIcon(url)) {
                public void requestFocus() {}  // not allowed on toolbar buttons
                public float getAlignmentY() { return 0.5f; }
            };
            b.setPad(new Insets(1,1,1,1));

            String tip = getResourceString(key + tipSuffix);
            if (tip != null) {
                b.setToolTipText(tip);
            }
            if (a != null) {
                b.setActionCommand(astr);
                b.addActionListener(a);
            } else {
                b.setEnabled(false);
            }
            button = b;

        } catch (Exception e) {
            button = null;
        }
        */
        if (button == null) {


            LabelledImageButton libutton = new LabelledImageButton(ChiselResources.getLabel(key), image, ChiselResources.getTip(key));
            libutton.setLabelVisible(false);
            if (a != null) {
                libutton.setActionCommand(astr);
                libutton.addActionListener(a);
            } else {
                libutton.setEnabled(false);
            }
            button = libutton;
        }
        return button;
    }

    public static ActionImpl createAction(ActionListener owner, String name) {
        String actionstr = ChiselResources.getResourceString(name);
        //if (useJFC) {
        //    return new ChiselJAction(owner, actionstr);
        //} else {
            return new ChiselAWTAction(owner, actionstr);
        //}
    }
}

/*
class ChiselJAction extends DefaultAction implements ActionImpl {

    ActionListener owner;

    public ChiselJAction(ActionListener owner, String name) {
        super(name);
        this.owner = owner;
    }

    public void actionPerformed(ActionEvent e) {
        owner.actionPerformed(e);
    }

    public String getName() {
        return getText(NAME);
    }

    public void setName(String name) {
        setText(NAME, name);
    }
}
*/

class ChiselAWTAction implements ActionImpl {

    ActionListener owner;
    String name;

    public ChiselAWTAction(ActionListener owner, String name) {
        super();
        this.owner = owner;
        this.name = name;
    }

    public void actionPerformed(ActionEvent e) {
        owner.actionPerformed(e);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


class ChiselAWTToolBar extends ChiselAWTPane {

    final static int PADDING = 4;

    public ChiselAWTToolBar() {
        super();
        setBorder(NO_BORDER, PADDING);
        Font font = new Font("Helvetica", Font.PLAIN, 11);
        setFont(font);
        GuiAdapter ga = new GuiAdapter();
        ga.setContainer(this);
    }
}

/** A lightweight separator component */
class ToolbarSeparator extends Component {
    static final int SEPARATOR_MINWIDTH = 8;
    static final int SEPARATOR_PREFWIDTH = 10;
    /** The preferred size. */
    public Dimension getPreferredSize() {
        return new Dimension(SEPARATOR_PREFWIDTH, 1);
    }
    /** The minimum size. */
    public Dimension getMinimumSize() {
        return new Dimension(SEPARATOR_MINWIDTH, 1);
    }
}


class ChiselAWTStatusBar extends ChiselAWTPane implements ProgressIndicator {

    static String message = "";

    ProgressBar pb;

    public ChiselAWTStatusBar() {
        super();

        setLayout(null);
        setBorder(FRAMED_BORDER);
        setFont(FontPool.getLabelFont());
        setSize(getPreferredSize());
        System.setOut(new SystemOutFilter(this, System.out));

        pb = new ProgressBar();
        pb.setVisible(false);
        add(pb);
    }

    public void setTitle(String title) {
        pb.setTitle(title);
    }
    
    public String getTitle() {
        return( pb.getTitle() );
    }
    
    public void setText( String text ) {
        pb.setText( text );
    }
    
    public void setColor( Color color ) {
        pb.setColor( color );
    }
    
    public Color getColor() {
        return( pb.getColor() );
    }
    
    public void setPercent(int n) {
        if (!pb.isVisible()) {
            pb.setVisible(true);
        }
        pb.setPercent(n);
    }
    
    public void setAlternatePercent(int n) {
        pb.setAlternatePercent( n );
    }

    public void reset() {
        pb.setPercent( -1 );
    }


    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.charWidth('M') * 128 + 12, fm.getHeight() + 8);
    }

    public Dimension getMinimumSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(12, fm.getHeight() + 8);
    }

    public void doLayout() {
        Dimension size = getSize();
        pb.setBounds(0, 0, size.width, size.height);
    }

    public void paint(Graphics g) {
        super.paint(g);

        Font font = getFont();
        FontMetrics fm = getFontMetrics(font);
        g.setFont(font);
        g.setColor(getForeground());

        // the y coordinate is calculated as follows:
        //    -- start the calculation at the bottom (getSize().height)
        //    -- subtract the bottom margin (4)
        //    -- subtract the font descent to get to the baseline
        g.drawString(message, 6, getSize().height - 4 - fm.getDescent());
    }


    class SystemOutFilter extends PrintStream {

        //ChiselAWTStatusBar owner;
        public SystemOutFilter(ChiselAWTStatusBar bar, PrintStream ps) {
            super(ps);
            //owner = bar;
        }
        public void print(String s) {
            super.print(s);
            super.flush();
        }


        public void println(String s) {
            if ( s == null ) {
                return;
            }
            message = s;

            // erase the progress bar if it's visible
            if (pb.isVisible()) {
                int pct = pb.getPercent();
                if (pct >= 0 && pct < 100) {
                    super.println(s);
                    return;
                } else {
                    pb.setVisible(false);
                }
            }

            //owner.repaint();
            repaint();
            if (s.length() > 0) {
                super.println(s);
                super.flush();
            }
        }
    }
}

class ChiselAWTFrame extends Frame implements DisplayConstants {
    public ChiselAWTFrame(String title) {
        super(title);
        setBackground(DEFAULT_WORKSPACECOLOR);
		setLayout(null);
		setSize(430,270);
//		setTitle("Untitled");
        setTitle( title );

    }
    public void update(Graphics g) {
        Insets insets = getInsets();
        g.translate(insets.left, insets.top);
        getComponent(0).update(g);
    }

    public void paint(Graphics g) {
        Insets insets = getInsets();
        g.translate(insets.left, insets.top);
        getComponent(0).paint(g);
    }
}

class GlassPanel extends Panel {
    public GlassPanel() {
        super(new BorderLayout());
    }
    public void update(Graphics g) {
        getComponent(0).paint(g);
    }
    public void paint(Graphics g) {
        getComponent(0).paint(g);
    }
}
