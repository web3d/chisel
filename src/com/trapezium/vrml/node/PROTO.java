/*
 * @(#)PROTO.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.FieldId;
import com.trapezium.vrml.grammar.Spelling;

/**
 *  Scene graph component for a PROTO declaration.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 17 Dec 1997
 *
 *  @since           1.0
 */
public class PROTO extends PROTObase {
    // used by humanoid verification, because unused fields so common
    // maybe shouldn't even do this
	static public boolean suppressISwarning = false;

	/** PROTO nodes can be used in place of a particular built in type */
	String builtInNodeType = null;

	public PROTO( int tokenOffset ) {
		super( tokenOffset );
	}

    /** template method, overrides Node.isPROTOnode() which returns false. */
    public boolean isPROTOnode() {
        return( true );
    }
    
	/**
	 *  Get the built-in type for this PROTO, i.e. the type of the first Node in the PROTO.
	 */
	public String getBuiltInNodeType() {
		return( builtInNodeType );
	}

	public String getBaseName() {
		return( "PROTO " + getId() );
	}

	/**
	 *  Set the built in type that this PROTO can be substituted for.
	 */
	public void setBuiltInNodeType( String type ) {
		builtInNodeType = type;
		if ( type == null ) {
		    setError( "node missing from PROTO body" );
		}
	}

    /** get the PROTO interface field with the name that best matches input name.
     *  This is used when a PROTO instance has an unknown field name.  We
     *  assume this is a typo, and search for a valid field name that matches.
     *
     *  @param  fieldName  a field name that is not part of the PROTO interface
     *  
     *  @return  a Field that has a name that has the best spelling match with
     *           the fieldName input parameter, returns null if no close matches
     *           found.
     */
	public Field getClosestInterfaceDeclaration( String fieldName ) {
		Field returnField = null;
		int biggestMatch = 0;
		int childCount = numberChildren();
		for ( int i = 0; i < childCount; i++ ) {
			VrmlElement vle = getChildAt( i );
			if ( vle instanceof Field ) {
				Field f = (Field)vle;
				String fName = f.getFieldId();
				int testDistance = Spelling.getMatchScore( fName, fieldName );
				if ( testDistance > biggestMatch ) {
					biggestMatch = testDistance;
					returnField = f;
				}
			}
		}
		return( returnField );
	}

    /**  check if all the PROTO interface fields are referenced by an IS statement */
	public void checkInUse() {
		if ( suppressISwarning ) return;
		for ( int i = 0; i < numberChildren(); i++ ) {
			VrmlElement vle = getChildAt( i );
			if ( vle instanceof Field ) {
				Field f = (Field)vle;
				if ( !f.isInUse() ) {
					f.setError( "Warning, field not referenced by IS" );
				}
			}
		}
	}
}

