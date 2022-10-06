package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;

public interface Control {

    /** set the listener for actions. */
    void addActionListener(ActionListener listener);

    /** remove the listener for actions. */
    void removeActionListener(ActionListener listener);

    /** set the value */
    Object getValue();

    /** get the value */
    void setValue( Object value );
}

