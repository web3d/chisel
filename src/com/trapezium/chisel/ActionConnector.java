/*  ActionConnector
 *
 */

package com.trapezium.chisel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;


/** This static class connects command strings with ActionListeners,
    allowing user interface elements such as buttons initialize
    themselves without having to know who is listening to them.
*/

public class ActionConnector {

    protected static Hashtable commands = new Hashtable();

    public static void addActions(ChiselAction[] actions) {
        for (int i = 0; i < actions.length; i++) {
            ChiselAction a = actions[i];
            if (a.getName() == null) {
                System.out.println("Action " + i + " (" + a.getClass().getName() + ") name is null!");
                continue;
            }
            commands.put(a.getName(), a);
        }
    }

    public static ActionListener getAction(String cmd) {
        ActionListener a = (ActionListener) commands.get(cmd);
        return a;
    }
}
