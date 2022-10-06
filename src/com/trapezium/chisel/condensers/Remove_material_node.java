/*
 * @(#)Remove_material_node.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.condensers;

import com.trapezium.chisel.Optimizer;
import com.trapezium.chisel.TokenPrinter;
import com.trapezium.vrml.node.*;
import com.trapezium.vrml.fields.*;

/**
 * Remove_material_node is a custom chisel designed by Johannes Johannsen
 *
 * This code was generated by Chisel Developer Kit version 1.0
 */
public class Remove_material_node extends Optimizer {
	// constants defining options by offset
	static final int REMOVE_ONLY_IF_TEXTURE_AVAILABLE_OPTION = 0;

	// variables for storing option values
	boolean remove_only_if_texture_available;

	public Remove_material_node() {
		super( "Appearance", "Removing material nodes" );
		remove_only_if_texture_available = true;
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
		if ( offset == REMOVE_ONLY_IF_TEXTURE_AVAILABLE_OPTION ) {
			return( Boolean.TYPE );
		}
		return( null );
	}

	/** Get the label for an option */
	public String getOptionLabel( int offset ) {
		if ( offset == REMOVE_ONLY_IF_TEXTURE_AVAILABLE_OPTION ) {
			return( "remove only if texture available" );
		}
		return( null );
	}

	/** Get the value of an option */
	public Object getOptionValue( int offset ) {
		if ( offset == REMOVE_ONLY_IF_TEXTURE_AVAILABLE_OPTION ) {
			return( booleanToOptionValue( remove_only_if_texture_available ));
		}
		return( null );
	}

	/** Set the value of an option */
	public void setOptionValue( int offset, Object value ) {
		if ( offset == REMOVE_ONLY_IF_TEXTURE_AVAILABLE_OPTION ) {
			remove_only_if_texture_available = optionValueToBoolean( value );
		}
	}

	/** Get the constraints on an option */
	public Object getOptionConstraints( int offset ) {
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
			Field texture = n.getField( "texture" );
			Field material = n.getField( "material" );
			Field textureTransform = n.getField( "textureTransform" );
			// register entire node for regeneration
			// last parameter can be any object, it is passed to "optimize" method
			// as the "param" parameter
			if ( remove_only_if_texture_available ) {
			    if ( texture != null ) {
			        if ( material != null ) {
    			        replaceRange( material.getFirstTokenOffset(), material.getLastTokenOffset(), null );
    			    }
			    }
			} else {
			    if ( material != null )
			    replaceRange( material.getFirstTokenOffset(), material.getLastTokenOffset(), null );
			}
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
