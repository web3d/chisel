/*
 * @(#)FunctionCallException.java
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
 *  Exception thrown when template method called unexpectedly.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 25 Nov 1997
 *
 *  @since           1.0
 */
public class FunctionCallException extends Exception {
    public FunctionCallException() { super(); }
    public FunctionCallException( String s ) { super(s); }
}
