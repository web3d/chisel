/*
 * ActionImpl.java
 *
 * ActionListener extension with methods common to all ActionListener
 * implementations
 */

package com.trapezium.chisel.gui;

import java.awt.event.*;

public interface ActionImpl extends ActionListener {
    String getName();
    void setName(String name);
}
