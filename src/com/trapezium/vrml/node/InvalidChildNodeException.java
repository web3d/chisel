/*
 * @(#)InvalidChildNodeException.java
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
 *  Exception thrown when trying to do a grouping node operation on a non-grouping node.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 29 March 1998
 *
 *  @since           1.12
 */
public class InvalidChildNodeException extends Exception {
    public InvalidChildNodeException() { super(); }
    public InvalidChildNodeException( String s ) { super(s); }
}
