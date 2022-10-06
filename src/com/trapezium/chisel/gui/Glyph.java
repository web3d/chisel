/*
 * Glyph.java
 *
 */

package com.trapezium.chisel.gui;

import java.awt.Graphics;
import java.awt.Color;

/**
 */
public final class Glyph {

    /* styles */

    /** triangular glyphs (like Win 3.1) */
    public static final int TRIANGULAR = 0;

    /** square glyphs (like Win95) */
    public static final int SQUARE = 1;

    /** default style */
    public static int style = TRIANGULAR; //SQUARE;

    /**
     * Not instantiable.
     */
    private Glyph() {
    }


    /**
     * Paint a phase in the run sequence.
     */
    public static void drawRun(Graphics g, int phase, int x, int y, int width, int height) {
        // same in all styles

        g.drawOval(x - 1, y - 1, width, height);

        // leave room for offset lines
        x++;
        y++;
        width -= 3;
        height -= 3;

        if (width - 1 > height) {
            int dx = width - height - 1;
            width -= dx;
            x += dx / 2;
        }


        // draw the exterior circle
        Color color = g.getColor();

        //g.setColor(color.brighter());
        //g.drawOval(x - 1, y - 1, width, height);
        //g.setColor(color.darker());
        //g.drawOval(x, y, width, height);
        //g.setColor(color);
        //g.drawOval(x, y - 1, width, height);

        int x0,x1,x2,x3;
        int y0,y1,y2,y3;

        // 4 phases
        phase &= 3;
        if (phase == 0) {
            int width2 = width / 2;
            int height2 = height / 2;
            x0 = x;
            y0 = y + height2;
            x1 = x + width;
            y1 = y + height2;
            x2 = x + width2;
            y2 = y;
            x3 = x + width2;
            y3 = y + height;
        } else if (phase == 2) {
            int width15 = (width * 15)/100;
            int height15 = (height * 15)/100;
            x0 = x + width15;
            y0 = y + height15;
            x1 = x + width - width15;
            y1 = y + height - height15;
            x2 = x + width - width15;
            y2 = y + height15;
            x3 = x + width15;
            y3 = y + height - height15;
        } else {                    // odd phase (1 or 3)
            int width09 = (width * 9)  / 100;
            int height09 = (height * 9) / 100;
            int width29 = (width * 29)/100;
            int height29 = (height * 29)/100;
            if (phase == 1) {
                x0 = x + width09;
                y0 = y + height29;
                x1 = x + width - width09;
                y1 = y + height - height29;
                x2 = x + width - width29;
                y2 = y + height09;
                x3 = x + width29;
                y3 = y + height - height09;
            } else {                // phase 3
                x0 = x + width29;
                y0 = y + height09;
                x1 = x + width - width29;
                y1 = y + height - height09;
                x2 = x + width - width09;
                y2 = y + height29;
                x3 = x + width09;
                y3 = y + height - height29;
            }
        }


        g.setColor(color.brighter());
        g.drawLine(x0 - 1, y0 - 1, x1 - 1, y1 - 1);
        g.drawLine(x2 - 1, y2 - 1, x3 - 1, y3 - 1);

        g.setColor(color.darker());
        g.drawLine(x0, y0, x1, y1);
        g.drawLine(x2, y2, x3, y3);

        g.setColor(color);
        g.drawLine(x0, y0 - 1, x1, y1 - 1);
        g.drawLine(x2, y2 - 1, x3, y3 - 1);

    }

    /**
     * Paint the icon for a checkmark.
     */
    public static void drawCheck(Graphics g, int x, int y, int width, int height) {
        // same in all styles

        x++;
        y++;
        width -= 2;
        height -= 2;

        if (width - 1 > height) {
            int dx = width - height - 1;
            width -= dx;
            x += dx / 2;
        }
        g.drawLine(x + 1, y + height/2 + 1, x + width/2, y + height);
        g.drawLine(x + 2, y + height/2 + 1, x + width/2 + 1, y + height);
        g.drawLine(x + width/2, y + height, x + width, y);
        g.drawLine(x + width/2 + 1, y + height, x + width + 1, y);
    }


    /**
     * Paint the icon for a close button.
     */
    public static void drawClose(Graphics g, int x, int y, int width, int height) {
        // same in all styles

        x++;
        y++;
        width -= 2;
        height -= 2;

        if (width - 1 > height) {
            int dx = width - height - 1;
            width -= dx;
            x += dx / 2;
        }
        g.drawLine(x, y, x + width, y + height);
        g.drawLine(x + 1, y, x + width + 1, y + height);
        g.drawLine(x + width, y, x, y + height);
        g.drawLine(x + width + 1, y, x + 1, y + height);
    }

    /**
     * Paint the icon for a maximize button.
     */
    public static void drawMaximize(Graphics g, int x, int y, int width, int height) {
        switch (style) {
            case TRIANGULAR:
                x = x + ((width + 1) / 2) - 1;
                y += 2;
                height -= 3;
                for (int i = 0; i < height; i++) {
                    g.drawLine(x - i, y + i, x + i, y + i);
                }
                break;

            case SQUARE:
                g.drawLine(x, y, x + width, y);
                g.drawRect(x, y + 1, width, height - 1);
                break;
        }
    }


    /**
     * Paint the icon for an up triangle.  Same in all styles.
     */
    public static void drawUp(Graphics g, int x, int y, int width, int height) {
        x = x + ((width + 1) / 2) - 1;
        y += 2;
        height -= 3;
        for (int i = 0; i < height; i++) {
            g.drawLine(x - i, y + i, x + i, y + i);
        }
    }

    /**
     * Paint the icon for a down triangle.  Same in all styles.
     */
    public static void drawDown(Graphics g, int x, int y, int width, int height) {
        x = x + ((width + 1) / 2) - 1;
        y += 2;
        height -= 3;
        for (int i = 0; i < height; i++) {
            g.drawLine(x - (height - 1 - i), y + i, x + (height - 1 - i), y + i);
        }
    }

    /**
     * Paint a dot.  Same in all styles.
     */
    public static void drawDot(Graphics g, int x, int y, int width, int height) {
        x = x + ((width + 1) / 2) - 1;
        y = y + (height / 2);
        g.fillOval(x - 3, y - 3, 6 + (width & 1), 6 + (height & 1));
    }

    /**
     * Paint the icon for a restore button.
     */
    public static void drawRestore(Graphics g, int x, int y, int width, int height) {
        switch (style) {
            case TRIANGULAR:
                int i;
                x = x + ((width + 1) / 2) - 1;
                for (i = 0; i < height/2; i++) {
                    g.drawLine(x - i, y + i, x + i, y + i);
                }
                for (i = height/2 + 1; i < height; i++) {
                    g.drawLine(x - height + i + 1, y + i, x + height - i - 1, y + i);
                }
                break;

            case SQUARE:
                g.drawLine(x + 3, y, x + width, y);
                g.drawRect(x + 3, y + 1, width - 3, height - 4);

                g.drawLine(x, y + 3, x + width - 3, y + 3);
                g.drawRect(x, y + 4, width - 3, height - 4);
                break;
        }
    }

    /**
     * Paint the icon for a minimize button.
     */
    public static void drawMinimize(Graphics g, int x, int y, int width, int height) {
        switch (style) {
            case TRIANGULAR:
                x = x + ((width + 1) / 2) - 1;
                y += 2;
                height -= 3;
                for (int i = 0; i < height; i++) {
                    g.drawLine(x - (height - 1 - i), y + i, x + (height - 1 - i), y + i);
                }
                break;

            case SQUARE:
                g.drawLine(x + 2, y + height - 1, x + width - 4, y + height - 1);
                g.drawLine(x + 2, y + height, x + width - 4, y + height);
                break;
        }
    }
}

