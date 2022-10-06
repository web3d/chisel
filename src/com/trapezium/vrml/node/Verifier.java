/*
 * @(#)Verifier.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.vrml.Scene;
import com.trapezium.vorlon.ErrorSummary;

import java.util.Hashtable;

/**
 *  Interface common to all node specific verifiers.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.21, 16 Oct 1998
 *     Added verifiedList param to prevent unnecessary repeated checks
 *  @version         1.1, 4 Dec 1997
 *
 *  @since           1.0
 */
interface Verifier {
    /** Node specific verification.
     *
     *  @param toBeVerified the Node that is being verified
     *  @param s the Scene containing the Node
     *  @param errorSummary used in cases where there are too many errors or warnings
     *     to specify individually
     *  @param verifiedList used to prevent unnecessary repeated verifications
     */
	public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList );
}

