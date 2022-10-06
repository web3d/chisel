/*
 * @(#)MFFieldDescriptor.java
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
 *  Describes limits of a VRML node MF field.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 19 March 1998
 *
 *  @since           1.12
 */
class MFFieldDescriptor extends FieldDescriptor {
    int limit;
    int factor;

    MFFieldDescriptor( int declarationType, int fieldType, String initialValue, int limit, int factor ) {
        super( declarationType, fieldType, initialValue );
        this.limit = limit;
        this.factor = factor;
    }

    int getLimit() {
        return( limit );
    }

    int getFactor() {
        return( factor );
    }
}

