/*
 * @(#)ChiselDescriptor.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.reflect.*;
import java.util.Hashtable;

import com.trapezium.util.ZipClassLoader;
import com.trapezium.chisel.gui.ChiselController;
import com.trapezium.chisel.gui.TextLabel;

/**
 *  Holds information pertaining to a chisel
 */
 public class ChiselDescriptor implements ActionListener {
	String className;
	String shortDescription;
	Object initialValue;
	int chiselType;
	int numberOptions = 0;
	int specializedListener;
	Component[] options;

	Object chisel = null;  // prototype instance of this chisel

    // this approach has currently been abandoned.
    // Can be redone, concept is to categories chisels into categories.
    // Chisels in the same category conflict with each other.
    // This categorization is tricky... at the moment, all chisels treated
    // as conflicting
	static public final int NOTYPE_CHISEL = 0;
	static public final int FIELD_CHISEL = 1;
	static public final int ALLNODE_CHISEL = 2;
	static public final int IndexedFaceSet_CHISEL = 3;
	static public final int UnInline_CHISEL = 4;
	static public final int Inline_CHISEL = 5;
	static public final int UnEXTERNPROTO_CHISEL = 6;
	static public final int EXTERNPROTO_CHISEL = 7;

	/** The table of chisel prototypes: key is class name, value is chisel instance */
	static Hashtable factoryTable = new Hashtable();

	/** Get a chisel from the prototype table */
	static public Optimizer createChisel( String name ) {
	    Optimizer x = (Optimizer)factoryTable.get( name );
	    //if ( x == null ) System.out.println( "createChisel of " + name + " returned null" );
	    //else System.out.println( "createChisel of " + name + " got a chisel " + x.getClass().getName() );
	    if ( x != null ) {
	        x.reset();
	    }
	    return( x );
	}


    public ChiselDescriptor( String className, String shortDescription, boolean initialValue ) {
        this( className, shortDescription, initialValue, NOTYPE_CHISEL, -1 );
    }

	public ChiselDescriptor( String className, String shortDescription, boolean initialValue, int chiselType ) {
        this( className, shortDescription, String.valueOf(initialValue), chiselType, -1 );
    }

	public ChiselDescriptor( String className, String shortDescription, boolean initialValue, int chiselType, int specializedListener ) {
        this( className, shortDescription, String.valueOf(initialValue), chiselType, specializedListener );
    }

	public ChiselDescriptor( String className, String shortDescription, Object initialValue, int chiselType, int specializedListener ) {
		this.className = className;
		this.shortDescription = shortDescription;
		this.initialValue = initialValue;
		this.chiselType = chiselType;
		this.specializedListener = specializedListener;
	    try {
            chisel = instantiate();
            //System.out.println(className + " prototype created.");
            if ( chisel instanceof Optimizer ) {
//                Optimizer chiselOptimizer = (Optimizer)chisel;
//                String baseName = className.substring( className.lastIndexOf( '.' ) + 1 );
                factoryTable.put( className, chisel );
                //System.out.println( "added " + baseName + " to factoryTable" );
            }
            if ( chisel instanceof OptionHolder ) {
                OptionHolder chiselOptimizer = (OptionHolder)chisel;
                numberOptions = chiselOptimizer.getNumberOptions();
                if ( numberOptions > 0 ) {
                    options = new Component[ numberOptions ];
                    for ( int i = 0; i < numberOptions; i++ ) {
                	    ChiselController cc = new ChiselController(this, chiselOptimizer.getOptionClass( i ));
                	    cc.setLabel(new TextLabel( chiselOptimizer.getOptionLabel( i )));
                	    cc.setConstraints(chiselOptimizer.getOptionConstraints( i ));
                	    cc.setValue( chiselOptimizer.getOptionValue( i ) );
                	    options[i] = cc;
                	}
                }
            }
        } catch (Exception e) {
            if ( className.compareTo( "ParserFactory" ) != 0 )
            System.out.println("Class " + className + " not found (exception: " + e.toString() + ").");
            chisel = null;
        }
	}

	    /** ActionListener interface, sets option value in chisel prototypes */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        for ( int i = 0; i < numberOptions; i++ ) {
            if ( options[i] instanceof ChiselController ) {
                ChiselController cc = (ChiselController)options[i];
                if ( chisel instanceof OptionHolder ) {
                    OptionHolder optimizer = (OptionHolder)chisel;
                    optimizer.setOptionValue( i, cc.getValue() );
                }
            }
        }
    }


    /** Get the chisel, assuming it is an option holder (not used by optimizers) */
    public OptionHolder getOptionHolder() {
        return (OptionHolder) chisel;
    }

    /** Get the initial value of the chisel */
    public Object getInitialValue() {
        return( initialValue );
    }

	public String getClassName() {
		return( className );
	}

	public String getShortDescription() {
		return( shortDescription );
	}

	public int getChiselType() {
	    return( chiselType );
	}

	public int getSpecializedListener() {
	    return( specializedListener );
	}

    /** Get the number of options available for the chisel, derived from chisel instance prototype */
	public int getNumberOptions() {
	    return( numberOptions );
	}


	public Component getOptionEditor(int optionIndex, ActionListener listener) {
	    if ( options[ optionIndex ] instanceof ChiselController ) {
	        ChiselController cc = (ChiselController)options[ optionIndex ];
	        cc.addActionListener( listener );
	    }
	    return( options[ optionIndex ] );
	}
	
	public Component getOption( int offset ) {
	    return( options[ offset ] );
	}


    /** Create a prototype for a chisel */
    public Object instantiate() throws java.io.IOException, ClassNotFoundException {

    	Object chisel = null;

    	java.io.InputStream in;
    	java.io.ObjectInputStream objectin = null;

        if (this.chisel != null) {
            try {
                Class params[] = {};
                Object args[] = params;
                Method cloneMethod = this.chisel.getClass().getMethod("clone", params);
                chisel = cloneMethod.invoke(this.chisel, args);
    	    } catch (Exception e) {
    	        System.out.println("Exception in ChiselDescriptor.instantiate: " + e.toString());
    	        chisel = null;
    	    }
    	}

    	if (chisel == null) {
    	    /** Try loading in a serialized version of the chisel */
        	String serName = className.concat(".ser");
        	in = ClassLoader.getSystemResourceAsStream(serName);
        	if (in != null) {
        	    try {
        		    objectin = new ObjectInputStream(in);
        	        chisel = objectin.readObject();
        	        objectin.close();
        	    } catch (java.io.IOException e) {
            		in.close();
        	    } catch (ClassNotFoundException e) {
            		in.close();
            		throw e;
        	    }
        	}

        	if (chisel == null) {
        	    // No serialized object, try just instantiating the class
        	    String name = className;

        	    // special case for format options
        	    if (name.charAt(0) == '#') {
                    chisel = FormatOptions.newOption(name.substring(1));

                } else {
            	    if (name.indexOf('.') == -1) {
            	        name = "com.trapezium.chisel." + name;
            	    }
            	    Class chiselClass;
            	    try {
            	        chiselClass = Class.forName(name);
            	    } catch (ClassNotFoundException e) {

            	        // don't know why Class.forName should fail on classes loaded
            	        // by ZipClassLoader, but it does
            	        chiselClass = ZipClassLoader.classForName(name);
            	    }
            	        
            	    try {
            	    	chisel = chiselClass.newInstance();
            	    } catch (Exception ex) {
            	        throw new ClassNotFoundException();
            	    }
        	    }
        	}
        }
    	return chisel;
    }
}
