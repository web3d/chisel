/*  FontPool
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;


/** global font pool sharable by all components */
public class FontPool {

    final static String DEFAULT_TEXTFAMILY = "Serif"; //"TimesRoman";
    final static int DEFAULT_TEXTSTYLE = Font.PLAIN;
    final static int DEFAULT_TEXTSIZE = 12;

    final static String DEFAULT_HEADERFAMILY = "SansSerif"; //"Helvetica"; //"Courier";
    final static int DEFAULT_HEADERSTYLE = Font.BOLD;
    final static int DEFAULT_HEADERSIZE = 12;

    final static String DEFAULT_LABELFAMILY = "SansSerif"; //"Helvetica";
    final static int DEFAULT_LABELSTYLE = Font.PLAIN;
    final static int DEFAULT_LABELSIZE = 12;

    final static String DEFAULT_TOOLTIPFAMILY = "Dialog";
    final static int DEFAULT_TOOLTIPSTYLE = Font.PLAIN;
    final static int DEFAULT_TOOLTIPSIZE = 12;

    public static final int NUMFONTS = 7;
    public static final int NORMAL = 2;
    public static final int MAXFONT = NUMFONTS - 1;

    static Font[] headerFont;
    static Font[] textFont;
    static Font labelFont;
    static Font tooltipFont;
    static {
        headerFont = new Font[NUMFONTS];
        textFont = new Font[NUMFONTS];

        int pts = DEFAULT_HEADERSIZE - NORMAL;
        for (int i = 0; i < NUMFONTS; i++) {
            headerFont[i] = new Font(DEFAULT_HEADERFAMILY, DEFAULT_HEADERSTYLE, pts);
            textFont[i] = new Font(DEFAULT_TEXTFAMILY, DEFAULT_TEXTSTYLE, pts);
            pts += (i <= NORMAL ? 1 : 4);
        }

        labelFont = new Font(DEFAULT_LABELFAMILY, DEFAULT_LABELSTYLE, DEFAULT_LABELSIZE);

        tooltipFont = new Font(DEFAULT_TOOLTIPFAMILY, DEFAULT_TOOLTIPSTYLE, DEFAULT_TOOLTIPSIZE);
    }

    public static Font getFont() {
        return getFont(NORMAL);
    }

    public static Font getFont(int n) {
        n += NORMAL;
        if (n < 0) {
            n = 0;
        } else if (n > MAXFONT) {
            n = MAXFONT;
        }
        return textFont[n];
    }

    public static Font getHeaderFont() {
        return getHeaderFont(NORMAL);
    }

    public static Font getHeaderFont(int n) {
        n += NORMAL;
        if (n < 0) {
            n = 0;
        } else if (n > MAXFONT) {
            n = MAXFONT;
        }
        return headerFont[n];
    }

    public static Font getLabelFont() {
        return labelFont;
    }

    public static Font getTooltipFont() {
        return tooltipFont;
    }
}

