/*
 * DisplayConstants.java
 */


package com.trapezium.chisel.gui;

import java.awt.Color;

/** Global predefined display info */
public interface DisplayConstants {
    
    // default values
    final static int LEFT_MARGIN = 8;
    final static int TOP_MARGIN = 4;
    final static int RIGHT_MARGIN = 8;
    final static int BOTTOM_MARGIN = 4;
    final static int HGAP = 5;
    final static int VGAP = 5;
    
    // border styles
    static final int LOWERED_BORDER = -1;
    static final int NO_BORDER = 0;
    static final int THIN_BORDER = 1;
    static final int RAISED_BORDER = 2;
    static final int FRAMED_BORDER = 3;
    static final int THICK_BORDER = 4;
    
    final static Color DEFAULT_TABLECOLOR = new Color(169, 175, 187);
    final static Color DEFAULT_FRAMECOLOR = new Color(183, 189, 201);
    final static Color DEFAULT_ROWHEADERCOLOR = new Color(122, 122, 122);
    final static Color DEFAULT_PANECOLOR = new Color(183, 189, 201);
    final static Color DEFAULT_WORKSPACECOLOR = new Color(141, 147, 159);
    final static Color DEFAULT_ACTIVECOLOR = new Color(139, 162, 204);
    final static Color DEFAULT_INACTIVECOLOR = new Color(198, 204, 216);
    final static Color DEFAULT_SELECTEDCOLOR = Color.yellow;
    final static Color DEFAULT_SELECTEDTEXTCOLOR = Color.magenta.darker();
    final static Color DEFAULT_BGCOLOR = Color.white;
    final static Color DEFAULT_TEXTCOLOR = Color.black;
    final static Color DEFAULT_FADEDTEXTCOLOR = Color.darkGray;
    final static Color DEFAULT_TITLETEXTCOLOR = Color.white;
    final static Color DEFAULT_BORDERCOLOR = Color.black;
    final static Color DEFAULT_SHADOWCOLOR = new Color(139, 145, 157);
    final static Color DEFAULT_LITESHADOWCOLOR = new Color(183, 189, 201);
    final static Color DEFAULT_RULECOLOR = Color.cyan.brighter();
    final static Color DEFAULT_TOOLBARCOLOR = new Color(198, 204, 216);
    final static Color DEFAULT_STATUSBARCOLOR = new Color(198, 204, 216);
    final static Color DEFAULT_PROGRESSBARCOLOR = new Color( 102, 204, 102 );
    final static Color DEFAULT_CONTROLCOLOR = new Color(91, 151, 115);
    final static Color DEFAULT_GROOVECOLOR = new Color(182, 188, 200);
    final static Color DEFAULT_TOPTABLECOLOR = new Color(183, 189, 201);
    final static Color DEFAULT_TOOLTIPCOLOR = new Color(255, 255, 153);
    final static Color DEFAULT_POPUPCOLOR = new Color(204, 187, 153);
    final static Color DEFAULT_POPUPTEXTCOLOR = Color.black;
    
    final static String DEFAULT_TILE = "tile.jpg";
    final static int DEFAULT_TILEX = -10;      // Netscape default left margin is 10
    final static int DEFAULT_TILEY = -15;      // Netscape default top margin is 15
    final static String DEFAULT_DECAL = "decal.gif";
}
