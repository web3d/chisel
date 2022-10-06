/*
 * @(#)test.java
 *
 * Copyright (c) 1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.codeveloper;

import com.trapezium.chisel.Optimizer;
import com.trapezium.chisel.TokenPrinter;
import com.trapezium.chisel.IntegerConstraints;
import com.trapezium.vrml.node.*;
import com.trapezium.vrml.fields.*;

/**
 * test is a custom chisel designed by test
 *
 * This code was generated by Chisel Developer Kit version 1.0
 */
public class test extends Optimizer {
	// constants defining options by offset
	static final int TEST_OPTION = 0;

	// variables for storing option values
	int test;

	public test() {
		super( "test", "test" );
		test = 0;
	}

	/** reset called just before chisel is run on a new file */
	public void reset() {
	}

	/** Get the number of options available for this chisel */
	public int getNumberOptions() {
		return( 1 );
	}

	/** Get the data type of an option */
	public Class getOptionClass( int offset ) {
		if ( offset == TEST_OPTION ) {
			return( Integer.TYPE );
		}
		return( null );
	}

	/** Get the label for an option */
	public String getOptionLabel( int offset ) {
		if ( offset == TEST_OPTION ) {
			return( "test" );
		}
		return( null );
	}

	/** Get the value of an option */
	public Object getOptionValue( int offset ) {
		if ( offset == TEST_OPTION ) {
			return( intToOptionValue( test ));
		}
		return( null );
	}

	/** Set the value of an option */
	public void setOptionValue( int offset, Object value ) {
		if ( offset == TEST_OPTION ) {
			test = optionValueToInt( value );
		}
	}

	/** Get the constraints on an option */
	public Object getOptionConstraints( int offset ) {
		if ( offset == TEST_OPTION ) {
			return( new IntegerConstraints( 0, 5, 0 ));
		}
		return( null );
	}

	/** Check if Node should be modified.
	 * TODO: user defined
	 */
	boolean optimizationOK( Node n ) {
		// MUST BE USER DEFINED
		return( true );
	}

	/** Called when node of specified type found in graph traversal */
	public void attemptOptimization( Node n ) {
		if ( optimizationOK( n )) {
			// if you need to access any of the fields, here they are
			// Field value is null if field not specified in file
			// 'test' is not a build in node type

			// register entire node for regeneration
			// last parameter can be any object, it is passed to "optimize" method
			// as the "param" parameter
			replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), n );
		}
	}

	/** Called to regenerate a portion of the text in a file.
	 *
	 *  @param tp object that takes regenerated text, similar to a PrintStream
	 *  @param param Object passed to "replaceRange" in "attemptOptimization" method above
	 *  @param startTokenOffset offset of first token in sequence being regenerated
	 *  @param endTokenOffset offset of last token in sequence being regenerated
	 */
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
		// TODO: user defined
		// if nothing done here, entire range is removed
	}

}