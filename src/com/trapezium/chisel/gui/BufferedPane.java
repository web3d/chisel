/*  BufferedPane
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import com.trapezium.chisel.Workspace;

/** A pane which uses an offscreen buffer to avoid flashing and flickering when
  * the screen is painted.
 */
public class BufferedPane extends ChiselAWTPane {

    // off screen buffer
    Image buffer = null;
    int bufferWidth, bufferHeight;

    public BufferedPane() {
        this(NO_BORDER);
    }
    public BufferedPane(int border) {
        super(border);
    }

    public void paint(Graphics g) {

        Dimension size = size();

        if ((buffer == null) || (size.width != bufferWidth) || (size.height != bufferHeight)) {
            buffer = null;

            //System.out.println("Creating new offscreen buffer");
            bufferWidth = size.width > 0 ? size.width : 1;
            bufferHeight = size.height > 0 ? size.height : 1;
            try {
                buffer = createImage(bufferWidth, bufferHeight);
            } catch (Exception e) {
            }
        }

        // if there's a memory problem, let the superclass paint the screen
        if (buffer == null) {
            super.paint(g);
            return;
        }

        Graphics osg = buffer.getGraphics();
        Rectangle clip = g.getClipRect();
        osg.clipRect(clip.x, clip.y, clip.width, clip.height);

        // paint the offscreen buffer
		super.paint(osg);

		// don't need the Graphics object any more
		osg.dispose();

        // blast the offscreen buffer onto the screen
        g.drawImage(buffer, 0, 0, this);
    }
}


