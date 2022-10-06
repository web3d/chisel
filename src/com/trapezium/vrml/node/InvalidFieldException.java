/*
 * @(#)InvalidFieldException.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

/**
 *  Exception thrown when trying to access a field which doesn't exist for a node.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 29 March 1998
 *
 *  @since           1.12
 */
public class InvalidFieldException extends Exception {
    public InvalidFieldException() { super(); }
    public InvalidFieldException( String s ) { super(s); }
}
