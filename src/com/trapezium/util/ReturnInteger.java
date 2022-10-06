/*
 * @(#)ReturnInteger.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.util;

/**
 *  Provides access to integer as an output parameter.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 10 Dec 1997
 *
 *  @since           1.0
 */
public class ReturnInteger implements java.io.Serializable {
	int ref;

    /** class constructor */
	public ReturnInteger() {
		ref = -1;
	}
	
	/** class constructor */
	public ReturnInteger( int x ) {
	    ref = x;
	}

    /** set the value */
	public void setValue( int tref ) {
		ref = tref;
	}

    /** get the value */
	public int getValue() {
		return( ref );
	}

    /** increment the value */
	public void incValue() {
	    ref++;
	}
}
