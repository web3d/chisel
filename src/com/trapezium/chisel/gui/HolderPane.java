package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
class HolderPane extends ChiselAWTPane {

    public HolderPane() {
        super();
    }

    public void paint(Graphics g) {
        paintBackground(g);

	    // draw components without clipping
	    int ncomponents = getComponentCount();
	    for (int i = ncomponents - 1; i >= 0; i--) {
    		Component comp = getComponent(i);
    		if (comp != null && comp.isVisible()) {

    		    Rectangle cr = comp.getBounds();
    			Graphics cg = g.create(cr.x, cr.y, cr.width, cr.height);
    			try {
    			    comp.paint(cg);
    			} finally {
    			    cg.dispose();
    			}
		    }
	    }

    }

    public void invalidate() {
        super.invalidate();
        Container parent = getParent();
        parent.invalidate();
    }

    //public void reshape(int x, int y, int width, int height) {
    //    System.out.println("Holder reshape " + x + "," + y + "," + width + "," + height);
    //    super.reshape(x, y, width, height);
    //}
}
