/*  ChiselAction
 *
 *  The purpose of this class is to handle generic action implementations.  It
 *  contains an action implementation specific to a gui flavor which can be
 *  plugged into that flavor of menu, toolbar etc.  The gui-specific
 *  implementation's actionPerformed method should call the container's
 *  actionPerformed method, i.e. the actionPerformed method of a subclass of
 *  this class.
 */

package com.trapezium.chisel;

import com.trapezium.chisel.gui.ComponentFactory;
import com.trapezium.chisel.gui.ActionImpl;

import java.awt.*;
import java.awt.event.*;

public abstract class ChiselAction implements ActionListener {
    ActionImpl realAction;


    public ChiselAction() {
        realAction = ComponentFactory.createAction(this, classNameToAction(getClass().getName()));
    }

    public abstract void actionPerformed(ActionEvent e);

    public String classNameToAction(String str) {
        int n = Math.max(str.lastIndexOf('.'), str.lastIndexOf('$'));
        str = Character.toLowerCase(str.charAt(n + 1)) +  str.substring(n + 2);
        return str;
    }

    public String getName() {
        return realAction.getName();
    }

    public ActionListener getAction() {
        return realAction;
    }

    // template, some actions depend on context (e.g. save)
    public boolean getEnabled() {
        return( true );
    }
}

