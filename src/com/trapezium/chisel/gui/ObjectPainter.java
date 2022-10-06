/*  ObjectPainter
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;

public class ObjectPainter {

    /** paint an object in a rectangular cell */
    public static void paintCell(Graphics g, Rectangle cellRect, Object cellObject) {
        if (cellObject instanceof String) {
            int x = cellRect.x;
            int y = cellRect.y + g.getFontMetrics().getAscent() + (cellRect.height - g.getFontMetrics().getHeight()) / 2;

            g.drawString((String) cellObject, x, y);
        } else if ( cellObject instanceof FontString ) {
			Font f = g.getFont();
			FontString fs = (FontString)cellObject;
			Font fnew = new Font( f.getName(), fs.getStyle(), f.getSize() );
			g.setFont( fnew );
            int x = cellRect.x;
            int y = cellRect.y + g.getFontMetrics().getAscent() + (cellRect.height - g.getFontMetrics().getHeight()) / 2;
			g.drawString( fs.getString(), x, y );
			g.setFont( f );
		} else if ( cellObject instanceof GlyphButton ) {
			GlyphButton gb = (GlyphButton)cellObject;
			gb.setLocation( cellRect.x, cellRect.y );
			gb.paint( g );
		}
    }
}
