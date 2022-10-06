/*
 * @(#)AboutPanel.java
 *
 * Copyright (c) 1998-2001 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 */
package com.trapezium.chisel.gui;

import com.trapezium.chisel.DialogOwner;
import com.trapezium.chisel.ChiselResources;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;

/** information panel for about box and splash screen */
public class AboutPanel extends Panel implements KeyListener {
    private DialogOwner owner;
    
    private final String[] copyInfo = {
        "Copyright (c) 1998-2001.",
                "Trapezium Development. All rights reserved.",
                "Portions copyright (c) 2005-2010",
                "Michael N. Louka. All rights reserved.",
                "Portions copyright (c) 2008",
                "D.G. van der Laan. All rights reserved.",
                "Portions copyright (c) 1998",
                "Arthur Do Consulting. All rights reserved.",
                "",
                "This application uses HTML parsing technology from Arthur Do Consulting."
    };
    
    private final String[] info = {
        "Conceived, designed and programmed by",
                "Johannes Johannsen",
                "Michael St. Hippolyte",
                "",
                "Bug fixes and additions by",
                "Michael N. Louka",
                "Denis G. van der Laan"
    };
    
    String progtitle;
    String subtitle = "World Processor";
    String version;
    
    boolean border;
    
    FontMetrics fmTitle;
    FontMetrics fmSubtitle;
    FontMetrics fmVersion;
    FontMetrics fmReport;
    FontMetrics fmBody;
    FontMetrics fmCopy;
    
    static final int hmargin = 16;
    static final int vmargin = 16;
    static final int titlevmargin = 4;
    
    String javaInfo1;
    String javaInfo2;
    
    public AboutPanel(DialogOwner owner, String progtitle, String version) {
        this(owner, progtitle, version, false);
    }
    
    public AboutPanel(DialogOwner owner, String progtitle, String version, boolean border) {
        this.owner = owner;
        this.progtitle = "Chisel"; //progtitle;
        this.version = version;
        this.border = border;
        
        javaInfo1 = "Java Version: " + System.getProperty("java.version");
        javaInfo2 = "Virtual Machine: " + System.getProperty("java.vendor");
        
        setLayout(null);
        
        addKeyListener(this);
        
        // the body font depends on the access which is why this is down here
        fmTitle = getFontMetrics(getTitleFont());
        fmSubtitle = getFontMetrics(getSubtitleFont());
        fmVersion = getFontMetrics(getVersionFont());
        fmReport = getFontMetrics(getReportFont());
        fmCopy = getFontMetrics(getCopyrightFont());
        fmBody = getFontMetrics(getBodyFont());
    }
    
    private Font getTitleFont() {
        return FontPool.getHeaderFont(5);
    }
    private Font getSubtitleFont() {
        return FontPool.getHeaderFont(2);
    }
    private Font getVersionFont() {
        return FontPool.getHeaderFont(0);
    }
    private Font getReportFont() {
        return FontPool.getFont(-1);
    }
    private Font getCopyrightFont() {
        return FontPool.getFont(0);
    }
    private Font getBodyFont() {
        return FontPool.getHeaderFont(0);
    }
    
    /** determine the minimum size */
    public Dimension getMinimumSize() {
        Dimension min = new Dimension(2 * hmargin, 2 * (vmargin + titlevmargin));
        min.width = Math.max(fmTitle.stringWidth(progtitle), min.width);
        min.width = Math.max(fmSubtitle.stringWidth(subtitle), min.width);
        min.width = Math.max(fmVersion.stringWidth(version), min.width);
        for (int i = 0; i < copyInfo.length; i++) {
            min.width = Math.max(fmCopy.stringWidth(copyInfo[i]), min.width);
        }
        // add a margin to the above
        min.width += 64;
        
        for (int i = 0; i < info.length; i++) {
            min.width = Math.max(fmBody.stringWidth(info[i]), min.width);
        }
        
        // we don't control the content of the following, so filter out
        // ridiculous widths
        
        int mw = Math.max(fmReport.stringWidth(javaInfo1), min.width);
        mw = Math.max(fmReport.stringWidth(javaInfo2), mw);
        min.width = Math.min(mw, min.width + 64);
        
        min.width += 2 * hmargin;
        
        min.height += fmTitle.getHeight() + fmSubtitle.getHeight() + (2 * fmVersion.getHeight()) + (3 * fmReport.getHeight());
        for (int i = 0; i < copyInfo.length; i++) {
            if (copyInfo[i].length() > 0) {
                min.height += fmCopy.getHeight();
            } else {
                min.height += fmCopy.getHeight() / 2;
            }
        }
        min.height += fmBody.getHeight();
        for (int i = 0; i < info.length; i++) {
            if (info[i].length() > 0) {
                min.height += fmBody.getHeight();
            } else {
                min.height += fmBody.getHeight() / 2;
            }
        }
        
        return min;
    }
    
    
    /** determine the preferred size */
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
    
    
    public void drawBorder( Graphics g, int x, int y, int width, int height) {
        drawBorder(g, x, y, width, height, getBackground(), false, true);
    }
    
    public void drawBorder( Graphics g, int x, int y, int width, int height, Color bg, boolean indented, boolean thick ) {
        Color hiliteColor = bg.brighter();
        Color liteShadowColor = bg.darker();
        Color shadowColor = liteShadowColor.darker();
        
        g.setColor(bg);
        g.fillRect(x + 2, y + 2, width - 3, height - 3);
        
        g.setColor(indented ? shadowColor : hiliteColor);
        g.drawLine(x, y, x + width - 2, y);
        g.drawLine(x, y, x, y + height - 2);
        
        if (thick) {
            g.setColor(indented ? liteShadowColor : hiliteColor);
            g.drawLine(x + 1, y + 1, x + width - 4, y + 1);
            g.drawLine(x + 1, y + 1, x + 1, y + height - 4);
        }
        
        g.setColor(indented ? hiliteColor : shadowColor);
        g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
        g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
        
        if (thick) {
            g.setColor(indented ? hiliteColor : liteShadowColor);
            g.drawLine(x + width - 2, y + 1, x + width - 2, y + height - 2);
            g.drawLine(x + 1, y + height - 2, x + width - 2, y + height - 2);
        }
    }
    
    /** draw the image and the label in a specified rectangle. */
    public void paint(Graphics g) {
        
        Dimension size = getSize();
        
        int y = vmargin - 10;
        int xleft = hmargin;
        int x = hmargin - 10;
        int halfline = fmBody.getHeight() / 2;
        int xcenter = size.width / 2;
        int ybl = y + titlevmargin + fmTitle.getAscent() + 4;
        int titlewidth = fmTitle.stringWidth(progtitle);
        int titleheight = fmTitle.getHeight();
        
        Color bg = getBackground();
        Color bgbrighter = bg.brighter();
        Color bgdarker = bg.darker();
        
        if (border) {
            drawBorder(g, 0, 0, size.width, size.height);
            
        } else {
            Dimension boxdim = getMinimumSize();
            boxdim.width -= 2 * hmargin - 20;
            boxdim.height -= 2 * vmargin - 16;
            
            drawBorder(g, x, y, boxdim.width, boxdim.height, bg, true, false);
            
            x++;
            y++;
            boxdim.width -= 2;
            boxdim.height -= 2;
            int ht = titleheight + 2 * titlevmargin + fmSubtitle.getHeight() + (2 * fmVersion.getHeight()) + 12;
            for (int i = 0; i < copyInfo.length; i++) {
                if (copyInfo[i].length() > 0) {
                    ht += fmCopy.getHeight();
                } else {
                    ht += fmCopy.getHeight() / 2;
                }
            }
            
            drawBorder(g, x, y, boxdim.width, ht, bg, false, false);
            drawBorder(g, x, y + ht, boxdim.width, boxdim.height - ht, bgbrighter, true, false);
            
            // boxdim.width = titlewidth + 16;
            // boxdim.height = titleheight + 2 * titlevmargin - 4;
            // x = xcenter - (titlewidth / 2) - 8;
            
            // drawBorder(g, x, y + titlevmargin + 2, boxdim.width, boxdim.height, bgbrighter, true, false);
        }
        
        y += 2 * titlevmargin + 4;
        
        g.setFont(getTitleFont());
        x = xcenter - (titlewidth / 2);
        
        g.setColor(Color.black);
        g.drawString(progtitle, x, ybl);
        
        y += titleheight + titlevmargin;
        g.setFont(getSubtitleFont());
        x = xcenter - (fmSubtitle.stringWidth(subtitle) / 2);
        g.drawString(subtitle, x, y + fmSubtitle.getAscent());
        y += fmSubtitle.getHeight();
        g.setFont(getVersionFont());
        x = xcenter - (fmVersion.stringWidth(version) / 2);
        y += fmVersion.getHeight() / 2;
        g.drawString(version, x, y + fmVersion.getAscent());
        y += fmVersion.getHeight() + halfline;
        g.setFont(getCopyrightFont());
        
        for (int i = 0; i < copyInfo.length; i++) {
            if (copyInfo[i].length() > 0) {
                x = xcenter - (fmCopy.stringWidth(copyInfo[i]) / 2);
                g.drawString(copyInfo[i], x, y + fmCopy.getAscent());
                y += fmCopy.getHeight();
            } else {
                y += fmCopy.getHeight() / 2;
            }
        }
        
        g.setFont(getReportFont());
        y += halfline + 4;
        x = xcenter - (fmReport.stringWidth(javaInfo1) / 2);
        x = (x > xleft) ?  x : xleft;
        g.drawString(javaInfo1, x, y + fmReport.getAscent());
        y += fmReport.getHeight();
        x = xcenter - (fmReport.stringWidth(javaInfo2) / 2);
        x = (x > xleft) ?  x : xleft;
        g.drawString(javaInfo2, x, y + fmReport.getAscent());
        y += fmReport.getHeight();
        
        g.setFont(getBodyFont());
        y += halfline;
        
        
        //int w = 0;
        //for (int i = 1; i < info.length; i++) {
        //    if (info[i].length() > 0) {
        //        w = Math.max(fmBody.stringWidth(info[i]), w);
        //    }
        //}
        //x = xcenter - (w / 2);
        y += fmBody.getHeight();
        g.setColor(Color.black);
        for (int i = 0; i < info.length; i++) {
            if (info[i].length() > 0) {
                g.drawString(info[i], xcenter - (fmBody.stringWidth(info[i])) / 2, y + fmBody.getAscent() + 1);
                y += fmBody.getHeight();
            } else {
                y += halfline;
            }
        }
        
        super.paint(g);
    }
    
    public void keyTyped( KeyEvent e ) {}
    public void keyReleased( KeyEvent e ) {}
    public void keyPressed( KeyEvent e ) {
        dispose();
    }
    
    private void dispose() {
        Container c = getParent();
        if (c instanceof Window) {
            ((Window) c).dispose();
        } else if (c instanceof Frame) {
            ((Frame) c).dispose();
        }
    }
}
