/*
 * @(#)ChiselSetDescriptor.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import java.util.Vector;
import java.util.Enumeration;

/** This is used for the external serialization of a ChiselSet
 *  description, used by plugins.
 */
public class ChiselSetDescriptor implements java.io.Serializable, ChiselPluginDescriptor {
    boolean greenable;
    String key;
    boolean initialCollapsedValue;
    int numberChisels;

    Vector classNames;
    Vector prompts;
    Vector checkVals;
    Vector listeners;
    String command;
    String tip;

    /** Constructor for a category of chisels
     *
     *  @param greenable if true, selecting an option in category causes
     *     main category button to turn green.
     *  @param key name of the category
     *  @param initialCollapsedValue whether category is collapsed initially
     */
    public ChiselSetDescriptor( String command, String tip,
        boolean greenable, String key,
        boolean initialCollapsedValue ) {
        this.command = command;
        this.tip = tip;
        this.greenable = greenable;
        this.key = key;
        this.initialCollapsedValue = initialCollapsedValue;
        numberChisels = 0;
        classNames = new Vector();
        prompts = new Vector();
        checkVals = new Vector();
        listeners = new Vector();
    }

    public String getCommand() {
        return( command );
    }
    
    public String getTip() {
        return( tip );
    }
    
    /** Add a entry to this category.
     *
     *  @param className java class to instantiate 
     *  @param prompt GUI prompt
     *  @param checkval whether category initially enabled
     *  @param listener constant indicating type of summary info associated
     *     with entry.
     */
    public void addChiselEntry( String className, String prompt, 
        boolean checkval, int listener ) {
        classNames.addElement( className );
        prompts.addElement( prompt );
        checkVals.addElement( new Boolean( checkval ));
        listeners.addElement( new Integer( listener ));
        numberChisels++;
    }
    
    public void addChiselEntry( String className, String prompt,
        boolean checkval ) {
        addChiselEntry( className, prompt, checkval, -1 );
    }

    public boolean getGreenable() {
        return( greenable );
    }

    public String getKey() {
        return( key );
    }

    public boolean getInitialCollapsedValue() {
        return( initialCollapsedValue );
    }

    public int getNumberChisels() {
        return( numberChisels );
    }
    
    public String getClassName( int offset ) {
        return( (String)classNames.elementAt( offset ));
    }

    public String getPrompt( int offset ) {
        return( (String)prompts.elementAt( offset ));
    }

    public boolean getCheckVal( int offset ) {
        Boolean b = (Boolean)checkVals.elementAt( offset );
        return( b.booleanValue() );
    }

    public int getListener( int offset ) {
        Integer i = (Integer)listeners.elementAt( offset );
        return( i.intValue() );
    }
}
