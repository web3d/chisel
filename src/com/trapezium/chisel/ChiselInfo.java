/*
 * @(#)ChiselInfo.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import java.beans.*;


/** A ChiselInfo is a generic information object for a chisel.
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.0, 09 Feb 1998
 *
 *  @since           1.0
 */
public class ChiselInfo extends SimpleBeanInfo {

    protected static Class chiselClass = Optimizer.class;

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor description = null;
        PropertyDescriptor node = null;

        try {
            description = new PropertyDescriptor("description", chiselClass);
            node = new PropertyDescriptor("node", chiselClass);

        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }

        description.setBound(true);
        node.setBound(true);
        PropertyDescriptor props[] = { description, node };
        return props;
    }
    public int getDefaultPropertyIndex() {
        return 0;
    }
}

