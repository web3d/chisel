package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;

/**
 * A widget for selecting from a numerical sequence of values.
 *
 * @author	    Michael St. Hippolyte
 * @version 	1.0, 3/10/98
*/
public class SpinBox extends Container implements /*TimerClient,*/ LabelConstants, ActionListener, Control {

    GlyphButton up;
    GlyphButton down;
    TextLabel displayNum;

    ActionListener actionListener = null;

    int minValue;
    int maxValue;
    int incValue;
    int value;


    public SpinBox() {
        this(0, 0, 1, 0);
    }

    public SpinBox(int min, int max, int inc, int start) {
        minValue = min;
        maxValue = max;
        incValue = inc;
        value = start;

        setBackground(Color.gray);
        setForeground(Color.black);
        setLayout(null);
        up = new GlyphButton(GlyphButton.UP);
        up.setMargins(new Insets(0, 0, 1, 1));
        up.addActionListener(this);
        add(up);
        down = new GlyphButton(GlyphButton.DOWN);
        down.setMargins(new Insets(0, 0, 1, 1));
        down.addActionListener(this);
        add(down);
        Font font =  new Font("SansSerif", Font.PLAIN, 12);
        displayNum = new TextEdit(String.valueOf(start), font, TextEdit.TEXT_ALIGN_RIGHT | TextEdit.TEXT_ALIGN_VCENTER);
        displayNum.setBackground(Color.lightGray);
        displayNum.setForeground(Color.black);
        add(displayNum);
    }

	public void actionPerformed( ActionEvent e ) {
	    Object source = e.getSource();
	    if (source == up) {
	        setIntValue(value + incValue);
	    } else if (source == down) {
	        setIntValue(value - incValue);
	    }
	    if ( actionListener != null ) {
	        actionListener.actionPerformed( e );
	    }
	}

    /** set the value */
    public void setValue( Object value ) {
        setIntValue( Integer.parseInt(value.toString()) );
    }

    public Object getValue() {
        return String.valueOf(getIntValue());
    }

	public void setIntValue(int val) {
	    int oldvalue = value;
	    if (val < minValue) {
	        value = minValue;
	    } else if (val > maxValue) {
	        value = maxValue;
	    } else {
    	    value = val;
    	}
    	if (value != oldvalue) {
    	    displayNum.setText(String.valueOf(value));
    	}
	}

	public int getIntValue() {
	    return value;
	}

	public void setConstraints(int min, int max, int inc) {
        minValue = min;
        maxValue = max;
        incValue = inc;
        setIntValue(value);
    }


    /**
     * Adds the specified action listener to receive action events
     * from this control.
     * @param listener the action listener
     */
    public void addActionListener(ActionListener listener) {
        actionListener = AWTEventMulticaster.add(actionListener, listener);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Removes the specified action listener so it no longer receives
     * action events from this button.
     * @param listener the action listener
     */
    public void removeActionListener(ActionListener listener) {
        actionListener = AWTEventMulticaster.remove(actionListener, listener);
    }

    /**
    * The preferred size of the button.
    */
    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        Dimension buttonPref = up.getPreferredSize();
        Dimension numPref = displayNum.getPreferredSize();
        int width = numPref.width + buttonPref.width;
        int height = Math.max(numPref.height, 2 * up.getMinimumSize().height);
        return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
    }


    /**
     * The minimum size of the button.
     */
    public Dimension getMinimumSize() {
        Insets insets = getInsets();
        Dimension buttonMin = up.getMinimumSize();
        Dimension numMin = displayNum.getMinimumSize();
        int width = numMin.width + buttonMin.width;
        int height = Math.max(numMin.height, 2 * buttonMin.height);
        return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
    }

    public void doLayout() {
        Insets insets = getInsets();
        Dimension size = getSize();
        size.width -= insets.left + insets.right;
        size.height -= insets.top + insets.bottom;
        int bht = size.height / 2;
        int bwid = up.getPreferredSize().width; //Math.min(bht, size.width / 4);
        displayNum.setBounds(insets.left, insets.top, size.width - bwid, size.height);
        int x = insets.left + size.width - bwid;
        up.setBounds(x, insets.top, bwid, bht);
        down.setBounds(x, insets.top + size.height - insets.bottom - bht, bwid, bht);
    }
}
