/*
 * @(#)FieldDescriptor.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

import com.trapezium.parse.TokenEnumerator;

/**
 *  Describes attributes of a VRML node field.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 29 March 1998, added method "getDefaultValue"
 *  @version         1.1, 8 Jan 1998
 *
 *  @since           1.1
 */
public class FieldDescriptor {
    // one of VRML97.exposedField, VRML97.field, VRML97.eventIn, or VRML97.eventOut
    int declarationType;
    int fieldType;
    String initialValue;
    TokenEnumerator tokenEnumerator;
    
    /** class constructor for field with no initial value 
     *
     *  @param declarationType  indicates exposedField, field, eventIn, eventOut
     *  @param fieldType        VRML type of field
     */
    FieldDescriptor( int declarationType, int fieldType ) {
        this( declarationType, fieldType, null );
    }
    
    /** class constructor for field with initial value
     *
     *  @param declarationType  indicates exposedField, field, eventIn, eventOut
     *  @param fieldType        VRML type of field
     *  @param initialValue     String form of initial value
     *
     *  @see VRML97 for description of declaration type and field type constants
     */
    FieldDescriptor( int declarationType, int fieldType, String initialValue ) {
        this.declarationType = declarationType;
        this.fieldType = fieldType;
        this.initialValue = initialValue;
    }
    
    /** template method, can this field reference a DEF
     *
     *  @return  always returns false, overridden in DEFuserFieldDescriptor
     *  @see DEFuserFieldDescriptor
     */
    boolean usesDEF() {
        return( false );
    }

    /** is there a "TRUE" default boolean value? */
    boolean getDefaultBoolValue() {
        if ( initialValue != null ) {
            return( initialValue.compareTo( "TRUE" ) == 0 );
        } else {
            return( false );
        }
    }

    /** what type is this field, see VRML97 for list of field type constants */
    int getFieldType() {
        return( fieldType );
    }

    /** what type of declaration is this field, see VRML97 for list of declaration types */
    int getDeclarationType() {
        return( declarationType );
    }
 
    /** get TokenEnumerator representing default field value for field */
    TokenEnumerator getTokenEnumerator() {
        if ( initialValue == null ) {
            return( null );
        }
        // most fields not used, only create TokenEnumerator when necessary
        if ( tokenEnumerator == null ) {
            tokenEnumerator = new TokenEnumerator( initialValue );
        }
        return( tokenEnumerator );
    }
    
    /** Get the default value for the field */
    public String getDefaultValue() {
        return( initialValue );
    }
}

