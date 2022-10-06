/*
 * @(#)ScriptFileParsed.java
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
 *  Scene graph component used as a marker on a node to indicate that
 *  there is an associated javascript component.  Parsing uses this
 *  to verify the existence of javascript functions related to ROUTEs
 *  to a script node.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 31 Oct 1997
 *
 *  @since           1.0
 */
public class ScriptFileParsed extends SingleTokenElement {
	public ScriptFileParsed( int tokenOffset ) {
		super( tokenOffset );
	}
}

