/*
 * @(#)ScriptFunction.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml;
import java.lang.reflect.Method;
import java.awt.Event;

/**
 *  Scene graph component representing a javascript function, found by
 *  looking in either the "javascript:..." url or a javascript file 
 *  referenced by a url.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 31 Oct 1997
 *
 *  @since           1.0
 */
public class ScriptFunction extends SingleTokenElement {
	Class myclass;
	final static Class paramClasses[] = { java.awt.Event.class };
	final static String eventMethod = "processEvent";
	String myName = null;

    /** Create a ScriptFunction VrmlElement with a specific name,
     *  used for Javascript functions.
     */
    public ScriptFunction( String name ) {
        super( -1 );
        myName = name;
    }
    
    /** Create a ScritpFunction VrmlElement for a Java class file */
	public ScriptFunction( Class c ) {
		super( -1 );
		myclass = c;
	}

    /** Get the name of the Javascript function */
    public String getName() {
        if ( myName == null ) {
            return( super.getName() );
        } else {
            return( myName );
        }
    }

    /** Compare the name of the Javascript function to a particular name,
     *  if this Script function is for a Java class, check only for a
     *  "processEvent" method (i.e. parameter is useless in this case)
     */
	public boolean nameIs( String name ) {
		if ( myclass != null ) {
			return( javaClassFuncExists( name ));
		}
		return( getName().compareTo( name ) == 0 );
	}

	public boolean javaClassFuncExists( String name ) {
		// best I can do is look for a "processEvent( java.awt.Event )"
		try {
			Method mthd = myclass.getMethod( eventMethod, paramClasses );
		} catch ( NoSuchMethodException e ) {
			return( false );
		} catch ( Exception anythingElse ) {
			// maybe its 1.0, just return true 
			return( true );
		}
		return( true );
	}
}

