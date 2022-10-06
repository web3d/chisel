/*
 * @(#)Tooltip.java
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
public class Tooltip extends Window implements DisplayConstants {

    final static Color bg = DEFAULT_TOOLTIPCOLOR;
    final static int hmargin = 4;
    final static int vmargin = 1;

    // how long before tooltip shows up
    final static int normalResponse = 1300;
    final static int quickResponse = 50;

    // how long between tooltips to get the quick response
    final static long quickLimit = 500;

    LabelledImageButton button = null;
    long lastDisplayed;
    int responseTime;

    Thread timer = null;

    public Tooltip(Frame frame) {
        super(frame);
        lastDisplayed = 0;
        setBackground(bg);
        setFont(FontPool.getTooltipFont());
    }

    public void activate(LabelledImageButton button) {
        if (timer != null) {
            timer.stop();
            timer = null;
            //lastDisplayed = System.currentTimeMillis();
        }

        this.button = button;

        // if there's no tooltip for this button, don't bother
        if (button.getTip() == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastDisplayed < quickLimit) {
            responseTime = quickResponse;
        } else {
            responseTime = normalResponse;
        }
        timer = new TooltipTimer();
        timer.start();
    }

    public void deactivate() {
        if (timer != null) {
            timer.stop();
            timer = null;
            //System.out.println("deactivated.");
        }
        this.button = null;
        if (isVisible()) {
            setVisible(false);
            lastDisplayed = System.currentTimeMillis();
        }
    }

    public void reactivate(LabelledImageButton button) {
        if (timer == null || button != this.button) {
            activate(button);
        } else {
            timer.interrupt();
        }
    }

    public void trigger() {
        if (button == null || button.getTip() == null) {
            return;
        }

        Point pt = button.getLocationOnScreen();
        Dimension compdim = button.getSize();
        Dimension screendim = Toolkit.getDefaultToolkit().getScreenSize();

        Dimension dim = getPreferredSize();

        pt.x += compdim.width / 2;
        if (pt.x + dim.width > screendim.width) {
            pt.x = screendim.width - dim.width;
        }
        pt.y += compdim.height + 6;
        if (pt.y + dim.height > screendim.height) {
            pt.y = screendim.height - dim.height;
        }

        setBounds(pt.x, pt.y, dim.width, dim.height);

        setVisible(true);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        Dimension dim = new Dimension(2 * hmargin, 2 * vmargin);
        if (button != null) {
            String tip = button.getTip();
            if (tip != null && tip.length() > 0) {
                FontMetrics fm = getFontMetrics(getFont());
                dim.width += fm.stringWidth(tip);
                dim.height += fm.getHeight();
            }
        }
        return dim;
    }


    public void paint(Graphics g) {

        Dimension dim = getSize();

        g.setColor(Color.black);
        g.drawRect(0, 0, dim.width - 1, dim.height - 1);

        if (button != null) {
            String tip = button.getTip();
            if (tip != null && tip.length() > 0) {
                FontMetrics fm = getFontMetrics(getFont());
                g.drawString(tip, hmargin, vmargin + fm.getAscent());
            }
        }
    }


    class TooltipTimer extends Thread {

        public void run() {

            // if the thread is interrupted, it stays in this loop,
            // otherwise it exits and calls trigger to display the tooltip

            while (responseTime > 0) {
                try {
                    Thread.sleep(responseTime);

                } catch (InterruptedException e) {
                    continue;
                }
                break;
            }
            trigger();
        }
    }
}
