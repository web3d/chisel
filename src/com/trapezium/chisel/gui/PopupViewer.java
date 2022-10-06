/*
 * @(#)PopupViewer.java
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


/** A tooltip popup window */
public class PopupViewer extends Window implements DisplayConstants {

    final static Color bg = DEFAULT_POPUPCOLOR;
    final static Color fg = DEFAULT_POPUPTEXTCOLOR;
    final static int hmargin = 8;
    final static int vmargin = 10;

    TitleBar titlebar;
    ChiselAWTPane pane;

    public PopupViewer(Frame frame) {
        super(frame);
        setBackground(bg);
        setForeground(bg);
        setFont(FontPool.getFont());

        setLayout(null);

        // create a container with an offscreen buffer
        pane = new BufferedPane(FRAMED_BORDER);
        pane.setOpaque(false);
        pane.setLayout(null);
        add(pane);

        titlebar = new TitleBar("PopupViewer", TitleBar.CLOSEBUTTON, 2);
        pane.add(titlebar);

        BorderSizer sizer = new BorderSizer(this, true);
        addMouseListener(sizer);
        addMouseMotionListener(sizer);
    }

    public Insets getInsets() {
        return pane.getInsets();
    }

    public Dimension getMinimumSize() {
        return new Dimension(2 * hmargin + 200, 2 * vmargin + 64);
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public void update(Graphics g) {
    	pane.paint(g);
    }

    public void paint(Graphics g) {
        pane.paint(g);
    }

    public void doLayout() {
        Dimension dim = getSize();
        pane.setBounds(0, 0, dim.width, dim.height);

        Dimension tbdim = titlebar.getPreferredSize();
        Insets insets = pane.getInsets();
        titlebar.setBounds(insets.left, insets.top, dim.width - insets.left - insets.right, tbdim.height);
    }

}
