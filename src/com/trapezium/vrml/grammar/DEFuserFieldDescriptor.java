/*
 * @(#)DEFuserFieldDescriptor.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

/**
 *  An MFFieldDescriptor which optionally references a DEF node.
 *  
 *  This is used only for the Anchor node "url" field, which may reference
 *  a Viewpoint node DEFfed in another file.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 22 Mar 1998, make it MFFieldDescriptor
 *  @version         1.1, 22 Dec 1997
 *
 *  @since           1.0
 */
class DEFuserFieldDescriptor extends MFFieldDescriptor {
    /** class constructor */
    DEFuserFieldDescriptor( int interfaceType, int fieldType ) {
        this( interfaceType, fieldType, null );
    }

    /** class constructor */
    DEFuserFieldDescriptor( int interfaceType, int fieldType, String initialValue ) {
        super( interfaceType, fieldType, initialValue, Table7.UrlLimit, 1 );
    }

    /** template method, overrides FieldDescriptor.usesDEF() */
    boolean usesDEF() {
        return( true );
    }
}
