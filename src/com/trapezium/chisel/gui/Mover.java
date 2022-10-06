/*  Mover
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;

//import com.trapezium.chisel.*;


public class Mover extends MouseAdapter implements MouseMotionListener {

    int anchorx, anchory;
    Component component;
    int trackAncestor;

    public Mover() {
        this(null);
    }

    public Mover(Component component) {
        this(component, 0);
    }

    public Mover(Component component, boolean trackParent) {
        this(component, 1);
    }

    public Mover(Component component, int trackAncestor) {
        this.component = component;
        this.trackAncestor = trackAncestor;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public Component getTarget() {
        Component target = component;
        for (int i = 0; i < trackAncestor; i++) {
            target = target.getParent();
        }
        return target;
    }

    public void mousePressed(MouseEvent evt) {
        anchorx = evt.getX();
        anchory = evt.getY();
    }

    public void mouseMoved(MouseEvent evt) {
    }

    public void mouseDragged(MouseEvent evt) {

        Component target = getTarget();
        synchronized (target) {
            int x = evt.getX();
            int y = evt.getY();

            if (x != anchorx || y != anchory) {
                int dx = x - anchorx;
                int dy = y - anchory;

                if (dx != 0 || dy != 0) {
                    Point pt = target.getLocation();
                    target.setLocation(pt.x + dx, pt.y + dy);
                }
            }
        }
    }
}

