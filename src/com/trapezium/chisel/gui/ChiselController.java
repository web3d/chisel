/*  ChiselController
 *
 *  The ChiselController is a TableCellComponent which has a UI for controlling
 *  chisels.  The ChiselConnector has the state information of the Chisel-File
 *  combination, and uses that information to set the color of the ChiselController.
 */

package com.trapezium.chisel.gui;

import com.trapezium.chisel.IntegerConstraints;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ChiselController extends ChiselReporter  {

    static final int STDCOLUMNWIDTH = 240;

	Component control;
	Class type;
    boolean leftAligned;

	public ChiselController(ActionListener listener) {
	    this(listener, null);
	}

	public ChiselController(ActionListener listener, Class type) {
	    super();

        //System.out.println("creating cc of type " + (type == null ? "*null*" : type.getName()));

	    this.type = type;

	    if (type == null || type == Boolean.TYPE) {
	        leftAligned = true;
        	GlyphButton visualRep = new GlyphButton( ' ', GlyphButton.CHECK ); //CLOSE );
    		visualRep.setForeground(Color.darkGray);
    		visualRep.setSticky(true);
    		visualRep.setBooleanValue(true);
    		visualRep.setInsets(new Insets(3, 3, 3, 3));
    		visualRep.setMargins(new Insets(1, 1, 1, 1));
    		control = visualRep;

	    } else if (type == Integer.TYPE) {
	        leftAligned = false;
	        SpinBox spinBox = new SpinBox(0, 10, 1, 0);
	        control = spinBox;
        }
        if (control != null) {
      		add(control);
        }
        addActionListener(listener);
	}

    public Component getControl() {
        return( control );
    }

	public void setLabel( TextLabel l ) {
	    super.setLabel(l);
	    if (control instanceof MouseListener) {
    	    l.addMouseListener((MouseListener) control);
    	}
	}

	public void setConstraints(Object constraints) {
	    if (type == Integer.TYPE) {
	        SpinBox spinBox = (SpinBox) control;
	        if (constraints == null) {
                spinBox.setConstraints(Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
	        } else if (constraints instanceof IntegerConstraints) {
	            IntegerConstraints ic = (IntegerConstraints) constraints;
                spinBox.setConstraints(ic.getMinimum(), ic.getMaximum(), ic.getIncrement());
	        }
        }
    }



	public synchronized void addActionListener(ActionListener listener) {
		if (listener != null) {
    		((Control) control).addActionListener( listener );
        }
    }

	public synchronized void removeActionListener(ActionListener listener) {
		if (listener != null) {
    		((Control) control).removeActionListener( listener );
        }
    }


	public void setColor( Color c ) {
		control.setBackground( c );
		//tl.setForeground( c );
		repaint();
	}

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        control.setEnabled(enabled);
    }

    public void setValue( Object initialValue ) {
        ((Control) control).setValue( initialValue );
    }

    public Object getValue() {
        return ((Control) control).getValue();
    }

    public Dimension getPreferredSize() {
        Dimension pref = control.getPreferredSize();
        Dimension tlpref = (tl != null ? tl.getPreferredSize() : new Dimension(0, 0));
        pref.width += tlpref.width;
        if (tlpref.height > pref.height) {
            pref.height = tlpref.height;
        }
        Insets insets = getInsets();
        pref.height += insets.top + insets.bottom;
        pref.width += insets.right + insets.left;
        return pref;
    }

	public void doLayout() {
		Dimension d = getSize();
		Insets insets = getInsets();
		Dimension pref = control.getPreferredSize();

		control.setSize( pref.width, d.height - insets.top - insets.bottom );
		Dimension csize = control.getSize();
		int cx;
		int tlx;
		if (leftAligned) {
		    cx = insets.left;
		    tlx = insets.left * 2 + csize.width;

        } else {
            cx = d.width - insets.right - csize.width;
            if (cx > STDCOLUMNWIDTH) {
                cx = STDCOLUMNWIDTH;
            }
            tlx = insets.left;
        }
		if ( tl != null ) {
		    Dimension size = tl.getMinimumSize();
			tl.setSize( size );
		    tl.setBounds(tlx, insets.top, size.width, d.height - insets.top - insets.bottom);
			if (!leftAligned && cx < tlx + size.width) {
			    cx = tlx + size.width;
			}

		}
   		control.setLocation( cx, insets.top );
	}
}
