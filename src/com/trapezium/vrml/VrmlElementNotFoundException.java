/*
 * @(#)VrmlElementNotFoundException.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml;

/**
 *  Exception thrown when a child VrmlElement is not found, for example,
 *  this exception is thrown when a VrmlElement indicated for removal is not
 *  a child element.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 6 June 1998
 *
 *  @since           1.12
 */
public class VrmlElementNotFoundException extends Exception {
    public VrmlElementNotFoundException() { super(); }
    public VrmlElementNotFoundException( String s ) { super(s); }
}
