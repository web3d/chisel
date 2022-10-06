/*  ChiselReporter
 *
 *  The ChiselReporter is a TableCellComponent which displays
 *  information about a chisel.
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.util.*;

public class ChiselReporter extends TableCellComponent {
	TextLabel tl;

	public ChiselReporter() {
        setLayout(null);
	}

	public void setLabel( TextLabel l ) {
		if ( tl != null ) {
		    tl.setVisible(false);
			remove( tl );
		}
		tl = l;
		tl.setAlign(TextLabel.TEXT_ALIGN_LEFT | TextLabel.TEXT_ALIGN_VCENTERASCENT);
		add( tl );
		doLayout();
	}

	public void setLabelText( String text ) {
	    if (tl == null) {
	        setLabel(new TextLabel(text));
	    } else {
	        tl.setText(text);
	    }
	}

    public Dimension getPreferredSize() {
        Dimension pref = (tl != null ? tl.getPreferredSize() : new Dimension(0, 0));
        Insets insets = getInsets();
        pref.height += insets.top + insets.bottom;
        pref.width += insets.right + insets.left;
        return pref;
    }
	public void doLayout() {
		if ( tl != null ) {
    		Dimension d = getSize();
            Insets insets = getInsets();
			tl.setBounds( insets.left, insets.top, d.width - insets.left - insets.right, d.height - insets.top - insets.bottom );
		}
	}
}
