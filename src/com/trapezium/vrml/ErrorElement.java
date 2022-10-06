/*
 * @(#)ErrorElement.java
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
 *  Scene graph component representing an error.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 25 Nov 1997
 *
 *  @since           1.0
 */
public class ErrorElement extends SingleTokenElement {
    /**  Create object associated with token, and associated error.
     *
     *  @param  tokenOffset  token associated with error
     *  @param  errorString  description of error
     */
    public ErrorElement( int tokenOffset, String errorString ) {
        super( tokenOffset );
        setError( errorString );
    }
}
