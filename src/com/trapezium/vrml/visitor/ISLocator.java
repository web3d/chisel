/*
 * @(#)ISLocator.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import com.trapezium.pattern.Visitor;
import com.trapezium.vrml.fields.ISField;

/**
 *  A visitor used to find an "IS" field within a VrmlElement.
 */
public class ISLocator extends Visitor {
    boolean foundIS = false;

    /** class constructor */
    public ISLocator() {
        super( null );
    }

    /** In some cases, the same instance is used repeatedly on different
     *  VrmlElements, this method resets the "foundID" indicator.
     */
    public void reset() {
        foundIS = false;
    }

    /** Visitor interface imeplementation, sets flag when an IS field is 
     *  found.
     *
     *  @param a check this object to see if it is an IS field
     *
     *  @return always returns false once an IS field is found, a false
     *     return means that no more children are to be checked.  If an
     *     IS field has not been found, always returns true, which means
     *     the "traverse" method should continue searching children.
     */
    public boolean visitObject( Object a ) {
        if ( a instanceof ISField ) {
            foundIS = true;
        }
        // keep visiting children until an IS field is found
        return( !foundIS );
    }

    /** Check if the last traversal found any IS fields.
     *
     *  @return true if the last traversal found IS fields, otherwise false.
     */
    public boolean foundISField() {
        return( foundIS );
    }
}
